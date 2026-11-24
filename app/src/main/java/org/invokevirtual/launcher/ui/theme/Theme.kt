package org.invokevirtual.launcher.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.LauncherSettings

/**
 * Exposes whether reduced motion is active (either system `ANIMATOR_DURATION_SCALE == 0` or the
 * user opted into Calm mode) so custom shape morphs and springs snap cleanly when needed.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun InvokeTheme(
    settings: LauncherSettings = LauncherSettings(),
    preferDarkWallpaperBars: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val dark = when (settings.effectiveDarkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }

    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current

    val systemAnimationsDisabled = remember(context, inPreview) {
        if (inPreview) {
            false
        } else {
            runCatching {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f,
                ) == 0f
            }.getOrDefault(false)
        }
    }
    val reducedMotion = systemAnimationsDisabled || !settings.expressiveMotion

    val baseScheme = when {
        settings.dynamicColor && !inPreview && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        settings.customSeedArgb != null -> seedColorScheme(settings.customSeedArgb, dark)
        else -> settings.palette.colorScheme(dark)
    }
    val amoled = dark && settings.effectiveAmoledBlack
    val colorScheme = remember(baseScheme, amoled) {
        if (amoled) baseScheme.toAmoledBlack() else baseScheme
    }

    val motionScheme = remember(reducedMotion) {
        if (reducedMotion) MotionScheme.standard() else MotionScheme.expressive()
    }

    val useDarkBars = preferDarkWallpaperBars ?: dark
    val activity = LocalActivity.current as? ComponentActivity
    if (!inPreview && activity != null) {
        LaunchedEffect(activity, useDarkBars) {
            val barStyle = if (useDarkBars) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            }
            activity.enableEdgeToEdge(
                statusBarStyle = barStyle,
                navigationBarStyle = barStyle,
            )
        }
    }

    val typography = remember(settings.fontPreset, inPreview) {
        invokeTypography(preset = settings.fontPreset, inPreview = inPreview)
    }

    CompositionLocalProvider(
        LocalTonalElevationEnabled provides !amoled,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = motionScheme,
            typography = typography,
            content = content,
        )
    }
}
