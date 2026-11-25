package org.invokevirtual.launcher.ui.widgets

import android.app.Activity
import android.appwidget.AppWidgetProviderInfo
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.system.AvailableWidget
import org.invokevirtual.launcher.system.WidgetHostController
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeShapes
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.toContainerShape

/**
 * M3 Expressive native Android AppWidget picker that binds, configures, and drops any installed
 * Android widget directly onto Invoke's Living Home Canvas.
 */
@Composable
fun WidgetPickerSheet(
    widgetHost: WidgetHostController?,
    onWidgetPicked: (appWidgetId: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        WidgetPickerContent(
            widgetHost = widgetHost,
            onWidgetPicked = { id ->
                onWidgetPicked(id)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .imePadding(),
        )
    }
}

@Composable
internal fun WidgetPickerContent(
    widgetHost: WidgetHostController?,
    onWidgetPicked: (appWidgetId: Int) -> Unit,
    modifier: Modifier = Modifier,
    previewWidgets: List<AvailableWidget>? = null,
) {
    val haptic = LocalHapticFeedback.current
    var pickerQuery by remember { mutableStateOf("") }
    var pendingWidgetId by remember { mutableIntStateOf(-1) }
    var pendingProvider by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }

    val emitPicked: (Int) -> Unit = onWidgetPicked

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = pendingWidgetId
        pendingWidgetId = -1
        pendingProvider = null
        if (id != -1) {
            if (result.resultCode == Activity.RESULT_OK) {
                emitPicked(id)
            } else {
                widgetHost?.deleteWidgetId(id)
            }
        }
    }

    val finalizeOrConfigure: (Int, AppWidgetProviderInfo) -> Unit = { id, info ->
        val configIntent = widgetHost?.createConfigureIntent(id, info)
        if (configIntent != null) {
            pendingWidgetId = id
            pendingProvider = info
            runCatching { configureLauncher.launch(configIntent) }.onFailure {
                pendingWidgetId = -1
                pendingProvider = null
                emitPicked(id)
            }
        } else {
            emitPicked(id)
        }
    }

    val bindLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = pendingWidgetId
        val info = pendingProvider
        pendingWidgetId = -1
        pendingProvider = null
        if (id != -1 && info != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                finalizeOrConfigure(id, info)
            } else {
                widgetHost?.deleteWidgetId(id)
            }
        }
    }

    val installedWidgets by produceState<List<AvailableWidget>>(
        initialValue = previewWidgets ?: emptyList(),
        widgetHost,
        previewWidgets,
    ) {
        value = previewWidgets ?: if (widgetHost != null) {
            withContext(Dispatchers.Default) { widgetHost.loadInstalledWidgets() }
        } else {
            emptyList()
        }
    }

    val filteredWidgets = remember(installedWidgets, pickerQuery) {
        val q = pickerQuery.trim()
        if (q.isEmpty()) {
            installedWidgets
        } else {
            installedWidgets.filter {
                it.label.contains(q, ignoreCase = true) || it.appName.contains(q, ignoreCase = true)
            }
        }
    }

    var expandedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(TileShapePreset.SQUIRCLE.toContainerShape(isHero = true))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = InvokeIcons.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Widget to Home",
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Choose an app to view its widgets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            OutlinedTextField(
                value = pickerQuery,
                onValueChange = { pickerQuery = it },
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                placeholder = {
                    Text(
                        text = "Filter widgets by app or name…",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = InvokeIcons.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = if (pickerQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = { pickerQuery = "" },
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(
                                imageVector = InvokeIcons.Close,
                                contentDescription = "Clear widget filter",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(percent = 50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                ),
            )

            val groupedWidgets = remember(filteredWidgets) {
                filteredWidgets.groupBy { it.packageName to it.appName }
            }

            if (filteredWidgets.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp),
                        )
                        Text(
                            text = if (pickerQuery.isBlank()) {
                                "No installed widgets found"
                            } else {
                                "No widgets match \"${pickerQuery.trim()}\""
                            },
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Install apps with Android widgets (Calendar, Weather, Music, Notes) to pin them directly onto your Home Canvas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    groupedWidgets.forEach { (pkgAndName, widgets) ->
                        val pkg = pkgAndName.first
                        val appName = pkgAndName.second
                        val firstWidget = widgets.firstOrNull()
                        val appIcon = firstWidget?.appIcon
                        val isExpanded = (pickerQuery.isNotBlank() && widgets.isNotEmpty()) || (pkg in expandedPackages)
                        val groupShape = RoundedCornerShape(24.dp)

                        item(key = "group_$pkg") {
                            Surface(
                                shape = groupShape,
                                color = if (isExpanded) {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(groupShape)
                                    .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                expandedPackages = if (pkg in expandedPackages) {
                                                    expandedPackages - pkg
                                                } else {
                                                    expandedPackages + pkg
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        if (appIcon != null) {
                                            val iconBitmap = remember(appIcon) {
                                                drawableToImageBitmap(appIcon, 36)
                                            }
                                            if (iconBitmap != null) {
                                                Image(
                                                    bitmap = iconBitmap,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape),
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = InvokeIcons.Apps,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appName,
                                                style = MaterialTheme.typography.titleMediumEmphasized,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${widgets.size} widget${if (widgets.size == 1) "" else "s"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) InvokeIcons.ArrowUp else InvokeIcons.ArrowDown,
                                            contentDescription = if (isExpanded) "Collapse $appName" else "Expand $appName",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                                            fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                                        exit = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                                            fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            widgets.forEachIndexed { index, widget ->
                                                val previewBitmap = remember(widget.previewDrawable) {
                                                    widget.previewDrawable?.let { drawableToImageBitmap(it, 160) }
                                                }
                                                val itemShape = InvokeShapes.segmentedItemShape(
                                                    index = index,
                                                    count = widgets.size,
                                                    outerRadius = 18.dp,
                                                    innerRadius = 6.dp,
                                                )

                                                Surface(
                                                    onClick = {
                                                        val host = widgetHost ?: return@Surface
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        val id = host.allocateWidgetId()
                                                        if (host.tryBindWidget(id, widget.providerInfo)) {
                                                            finalizeOrConfigure(id, widget.providerInfo)
                                                        } else {
                                                            pendingWidgetId = id
                                                            pendingProvider = widget.providerInfo
                                                            runCatching {
                                                                bindLauncher.launch(host.createBindIntent(id, widget.providerInfo))
                                                            }.onFailure {
                                                                pendingWidgetId = -1
                                                                pendingProvider = null
                                                                host.deleteWidgetId(id)
                                                            }
                                                        }
                                                    },
                                                    shape = itemShape,
                                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(TileShapePreset.SQUIRCLE.toContainerShape())
                                                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    imageVector = InvokeIcons.Widgets,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                                    modifier = Modifier.size(16.dp),
                                                                )
                                                            }

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = widget.label,
                                                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                )
                                                                Text(
                                                                    text = "Default min ${widget.minHeightDp}dp",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 1,
                                                                )
                                                            }

                                                            Icon(
                                                                imageVector = InvokeIcons.Add,
                                                                contentDescription = "Add ${widget.label} to Home",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(20.dp),
                                                            )
                                                        }

                                                        if (previewBitmap != null) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .heightIn(max = 130.dp)
                                                                    .clip(RoundedCornerShape(12.dp))
                                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                                    .padding(8.dp),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Image(
                                                                    bitmap = previewBitmap,
                                                                    contentDescription = "${widget.label} preview",
                                                                    contentScale = ContentScale.Fit,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun drawableToImageBitmap(
    drawable: Drawable,
    sizeDp: Int,
): androidx.compose.ui.graphics.ImageBitmap? = runCatching {
    val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: (sizeDp * 2)
    val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: (sizeDp * 2)
    val maxDim = (sizeDp * 3).coerceIn(48, 480)
    val scale = minOf(maxDim.toFloat() / intrinsicW, maxDim.toFloat() / intrinsicH, 1f)
    val targetW = (intrinsicW * scale).toInt().coerceAtLeast(1)
    val targetH = (intrinsicH * scale).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(targetW, targetH)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, targetW, targetH)
    drawable.draw(canvas)
    bitmap.asImageBitmap()
}.getOrNull()

@Preview(
    name = "Widget Picker · Dark",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun WidgetPickerPreview() {
    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxSize(),
        ) {
            WidgetPickerContent(
                widgetHost = null,
                onWidgetPicked = {},
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp),
            )
        }
    }
}

