package org.invokevirtual.launcher.system

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import androidx.compose.runtime.Immutable

@Immutable
data class AvailableWidget(
    val id: String,
    val label: String,
    val appName: String,
    val packageName: String,
    val minHeightDp: Int,
    val providerInfo: AppWidgetProviderInfo,
    val appIcon: android.graphics.drawable.Drawable? = null,
    val previewDrawable: android.graphics.drawable.Drawable? = null,
)

/**
 * Simple lifecycle-aware controller over Android's [AppWidgetHost] & [AppWidgetManager] for
 * edge-to-edge native Android widgets directly on Invoke's Living Home Canvas.
 */
class WidgetHostController(private val context: Context) {
    private val manager: AppWidgetManager? = AppWidgetManager.getInstance(context)
    private val host: AppWidgetHost = AppWidgetHost(context.applicationContext, HOST_ID)

    fun startListening() {
        runCatching { host.startListening() }
    }

    fun stopListening() {
        runCatching { host.stopListening() }
    }

    fun allocateWidgetId(): Int = host.allocateAppWidgetId()

    fun deleteWidgetId(appWidgetId: Int) {
        runCatching { host.deleteAppWidgetId(appWidgetId) }
    }

    fun tryBindWidget(appWidgetId: Int, info: AppWidgetProviderInfo): Boolean =
        runCatching { manager?.bindAppWidgetIdIfAllowed(appWidgetId, info.profile, info.provider, null) == true }
            .getOrDefault(false)

    fun createBindIntent(appWidgetId: Int, info: AppWidgetProviderInfo): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.profile)
        }

    fun createConfigureIntent(appWidgetId: Int, info: AppWidgetProviderInfo): Intent? {
        val configure = info.configure ?: return null
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
    }

    fun getWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        runCatching { manager?.getAppWidgetInfo(appWidgetId) }.getOrNull()

    fun createHostView(
        hostContext: Context,
        appWidgetId: Int,
        info: AppWidgetProviderInfo,
    ): AppWidgetHostView = host.createView(hostContext, appWidgetId, info).apply {
        setAppWidget(appWidgetId, info)
        setPadding(0, 0, 0, 0)
    }

    fun updateWidgetSize(
        hostView: AppWidgetHostView,
        widthDp: Int,
        heightDp: Int,
    ) {
        if (widthDp <= 0 || heightDp <= 0) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                hostView.updateAppWidgetSize(
                    Bundle.EMPTY,
                    listOf(SizeF(widthDp.toFloat(), heightDp.toFloat())),
                )
            } else {
                @Suppress("DEPRECATION")
                hostView.updateAppWidgetSize(Bundle.EMPTY, widthDp, heightDp, widthDp, heightDp)
            }
        }
    }

    fun loadInstalledWidgets(): List<AvailableWidget> {
        val pm = context.packageManager
        val density = context.resources.displayMetrics.densityDpi
        val providers = runCatching { manager?.installedProviders }.getOrNull().orEmpty()
        return providers.mapNotNull { info ->
            val label = runCatching { info.loadLabel(pm) }.getOrNull()?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val packageName = info.provider.packageName
            val appInfo = runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
            val appName = if (appInfo != null) {
                runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(packageName)
            } else {
                packageName
            }
            val appIcon = runCatching {
                if (appInfo != null) pm.getApplicationIcon(appInfo) else null
            }.getOrNull()
            val previewDrawable = runCatching {
                info.loadPreviewImage(context, density)
            }.getOrNull() ?: runCatching {
                info.loadIcon(context, density)
            }.getOrNull()
            val minHeight = info.minHeight.coerceIn(96, 320)
            AvailableWidget(
                id = "${info.provider.flattenToShortString()}#${info.profile.hashCode()}",
                label = label,
                appName = appName,
                packageName = packageName,
                minHeightDp = minHeight,
                providerInfo = info,
                appIcon = appIcon,
                previewDrawable = previewDrawable,
            )
        }.sortedWith(compareBy({ it.appName.lowercase() }, { it.label.lowercase() }))
    }

    companion object {
        private const val HOST_ID = 0x494E564B // "INVK"
    }
}
