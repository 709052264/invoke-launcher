package org.invokevirtual.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.data.defaultHeightDpForSpan
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.rememberAnimatedContainerMorphShape
import org.invokevirtual.launcher.ui.theme.toContainerShape
import kotlin.math.roundToInt

@Composable
fun AppActionSheet(
    app: InstalledApp,
    canvasBlock: CanvasBlock.AppTile?,
    pinnedShortcutIds: Set<String> = emptySet(),
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onDismiss: () -> Unit,
    onAddToCanvas: (TileSpan) -> Unit,
    onSetCanvasSize: (String, TileSpan) -> Unit,
    onSetCanvasCustomSize: (String, Int, Int) -> Unit = { _, _, _ -> },
    onSetCanvasShape: (String, TileShapePreset) -> Unit,
    onSetCanvasTone: (String, TileTone) -> Unit = { _, _ -> },
    onSetCanvasCustomBg: (String, Int?) -> Unit = { _, _ -> },
    onRemoveFromCanvas: () -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onTogglePinShortcut: (AppShortcutItem) -> Unit = {},
    onLaunchFloating: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    val shortcuts by produceState<List<AppShortcutItem>>(initialValue = emptyList(), app, appCatalog) {
        value = appCatalog?.loadShortcuts(app) ?: emptyList()
    }
    val canUninstall = remember(app, appCatalog) {
        appCatalog?.canUninstall(app) == true
    }
    val hasShortcutPermission = remember(appCatalog) {
        appCatalog?.hasShortcutPermission() == true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        AppActionSheetContent(
            appLabel = app.label,
            packageName = app.componentName.packageName,
            isWorkProfile = app.isWorkProfile,
            iconAsset = iconAsset,
            iconStyle = iconStyle,
            canvasBlock = canvasBlock,
            shortcuts = shortcuts,
            pinnedShortcutIds = pinnedShortcutIds,
            hasShortcutPermission = hasShortcutPermission,
            canUninstall = canUninstall,
            onAddToCanvas = { span ->
                onAddToCanvas(span)
                onDismiss()
            },
            onSetCanvasSize = onSetCanvasSize,
            onSetCanvasCustomSize = onSetCanvasCustomSize,
            onSetCanvasShape = onSetCanvasShape,
            onSetCanvasTone = onSetCanvasTone,
            onSetCanvasCustomBg = onSetCanvasCustomBg,
            onRemoveFromCanvas = {
                onRemoveFromCanvas()
                onDismiss()
            },
            onLaunchShortcut = { shortcut ->
                onDismiss()
                onLaunchShortcut(shortcut)
            },
            onTogglePinShortcut = onTogglePinShortcut,
            onLaunchFloating = {
                onDismiss()
                onLaunchFloating()
            },
            onOpenAppInfo = {
                onDismiss()
                onOpenAppInfo()
            },
            onUninstall = {
                onDismiss()
                onUninstall()
            },
        )
    }
}

@Composable
internal fun AppActionSheetContent(
    appLabel: String,
    packageName: String,
    isWorkProfile: Boolean,
    iconAsset: AppIconAsset?,
    iconStyle: IconStyle,
    canvasBlock: CanvasBlock.AppTile?,
    shortcuts: List<AppShortcutItem>,
    pinnedShortcutIds: Set<String> = emptySet(),
    hasShortcutPermission: Boolean,
    canUninstall: Boolean,
    onAddToCanvas: (TileSpan) -> Unit,
    onSetCanvasSize: (String, TileSpan) -> Unit,
    onSetCanvasCustomSize: (String, Int, Int) -> Unit = { _, _, _ -> },
    onSetCanvasShape: (String, TileShapePreset) -> Unit,
    onSetCanvasTone: (String, TileTone) -> Unit = { _, _ -> },
    onSetCanvasCustomBg: (String, Int?) -> Unit = { _, _ -> },
    onRemoveFromCanvas: () -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onTogglePinShortcut: (AppShortcutItem) -> Unit = {},
    onLaunchFloating: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    var customSizeExpanded by remember(canvasBlock?.id) {
        mutableStateOf(canvasBlock?.hasCustomSize == true)
    }
    var draftCols by remember(canvasBlock?.id, canvasBlock?.effectiveColumns) {
        mutableIntStateOf(canvasBlock?.effectiveColumns ?: 1)
    }
    var draftHeightDp by remember(canvasBlock?.id, canvasBlock?.effectiveHeightDp) {
        mutableFloatStateOf((canvasBlock?.effectiveHeightDp ?: 76).toFloat())
    }
    val previewHasCustomSize = canvasBlock != null && (customSizeExpanded || canvasBlock.hasCustomSize)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiveActionTilePreview(
                appLabel = appLabel,
                iconAsset = iconAsset,
                iconStyle = iconStyle,
                canvasBlock = canvasBlock,
                previewColumns = draftCols,
                previewHeightDp = draftHeightDp.roundToInt(),
                previewHasCustomSize = previewHasCustomSize,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = appLabel,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (canvasBlock != null) {
                        val sizeText = if (previewHasCustomSize) {
                            "${draftCols}×${draftHeightDp.roundToInt()}dp"
                        } else {
                            canvasBlock.sizeLabel
                        }
                        "$sizeText · ${canvasBlock.shape.label}"
                    } else if (isWorkProfile) {
                        "Work · $packageName"
                    } else {
                        packageName
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (shortcuts.isNotEmpty()) {
            SectionLabel("Shortcuts")
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                shortcuts.forEachIndexed { index, shortcut ->
                    SegmentedListItem(
                        onClick = { onLaunchShortcut(shortcut) },
                        shapes = ListItemDefaults.segmentedShapes(index, shortcuts.size),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        leadingContent = {
                            if (shortcut.icon != null) {
                                Image(
                                    bitmap = shortcut.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                )
                            } else {
                                Icon(
                                    imageVector = InvokeIcons.ShortcutBolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        },
                        trailingContent = {
                            val isPinned = shortcut.shortcutId in pinnedShortcutIds
                            IconButton(
                                onClick = { onTogglePinShortcut(shortcut) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isPinned) InvokeIcons.Check else InvokeIcons.Pin,
                                    contentDescription = if (isPinned) {
                                        "Unpin ${shortcut.label} from Home"
                                    } else {
                                        "Pin ${shortcut.label} to Home"
                                    },
                                    tint = if (isPinned) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        content = {
                            Text(
                                text = shortcut.label,
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        } else if (!hasShortcutPermission) {
            Text(
                text = "Set Invoke as your default Home app to unlock live app shortcuts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Home Canvas controls
        SectionLabel("Home Canvas")
        if (canvasBlock == null) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(TileSpan.entries, key = { it.name }) { span ->
                    FilterChip(
                        selected = false,
                        onClick = { onAddToCanvas(span) },
                        label = {
                            Text(
                                text = "Add ${span.label}",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1,
                            )
                        },
                        modifier = Modifier.heightIn(min = 42.dp),
                    )
                }
            }
        } else {
            // Direct Size picker + Custom size morphing
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(TileSpan.entries, key = { it.name }) { span ->
                    FilterChip(
                        selected = !customSizeExpanded && !canvasBlock.hasCustomSize && canvasBlock.span == span,
                        onClick = {
                            customSizeExpanded = false
                            draftCols = span.columns
                            draftHeightDp = defaultHeightDpForSpan(span).toFloat()
                            onSetCanvasSize(canvasBlock.id, span)
                        },
                        label = { Text(span.label) },
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
                item(key = "tile_custom_size") {
                    FilterChip(
                        selected = customSizeExpanded || canvasBlock.hasCustomSize,
                        onClick = { customSizeExpanded = !customSizeExpanded },
                        label = { Text("Custom") },
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }

            AnimatedVisibility(visible = customSizeExpanded || canvasBlock.hasCustomSize) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Width",
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            (1..4).forEach { cols ->
                                FilterChip(
                                    selected = draftCols == cols,
                                    onClick = {
                                        draftCols = cols
                                        onSetCanvasCustomSize(
                                            canvasBlock.id,
                                            cols,
                                            draftHeightDp.roundToInt().coerceIn(64, 280),
                                        )
                                    },
                                    label = { Text("${cols} col") },
                                    modifier = Modifier.heightIn(min = 36.dp),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "Height",
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val clampedHeight = draftHeightDp.coerceIn(64f, 280f)
                            val heightSliderState = rememberSliderState(
                                value = clampedHeight,
                                trackRange = 64f..280f,
                            )
                            heightSliderState.value = clampedHeight
                            Slider(
                                state = heightSliderState,
                                onValueChange = { next ->
                                    heightSliderState.value = next
                                    draftHeightDp = next
                                },
                                onValueChangeFinished = {
                                    onSetCanvasCustomSize(
                                        canvasBlock.id,
                                        draftCols,
                                        draftHeightDp.roundToInt().coerceIn(64, 280),
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${clampedHeight.roundToInt()}dp",
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(TileShapePreset.entries, key = { it.name }) { shape ->
                    val isSelected = canvasBlock.shape == shape
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetCanvasShape(canvasBlock.id, shape) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(shape.toContainerShape(isHero = true))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    ),
                            )
                        },
                        shape = shape.toContainerShape(isWide = true),
                        label = { Text(shape.label) },
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }

            var showColorPicker by remember(canvasBlock.id) {
                mutableStateOf(canvasBlock.customBgArgb != null)
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(TileTone.entries, key = { it.name }) { tone ->
                    FilterChip(
                        selected = canvasBlock.customBgArgb == null && canvasBlock.tone == tone,
                        onClick = { onSetCanvasTone(canvasBlock.id, tone) },
                        label = { Text(tone.label) },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
                item(key = "custom_color_picker_toggle") {
                    FilterChip(
                        selected = showColorPicker || canvasBlock.customBgArgb != null,
                        onClick = { showColorPicker = !showColorPicker },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        canvasBlock.customBgArgb?.let(::Color)
                                            ?: MaterialTheme.colorScheme.primary,
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            )
                        },
                        label = { Text("Color Picker") },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
            }

            AnimatedVisibility(visible = showColorPicker) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    InteractiveColorPicker(
                        currentArgb = canvasBlock.customBgArgb,
                        onSelectColor = { argb -> onSetCanvasCustomBg(canvasBlock.id, argb) },
                        onReset = { onSetCanvasCustomBg(canvasBlock.id, null) },
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            SegmentedListItem(
                onClick = onRemoveFromCanvas,
                shapes = ListItemDefaults.segmentedShapes(0, 1),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                leadingContent = {
                    Icon(
                        imageVector = InvokeIcons.Pin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp),
                    )
                },
                content = {
                    Text(
                        text = "Remove from Home",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }

        SectionLabel("Actions")
        val actions = buildList {
            add(
                SheetActionRow(
                    label = "Open in floating window",
                    icon = InvokeIcons.BubbleWindow,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onLaunchFloating,
                ),
            )
            add(
                SheetActionRow(
                    label = "App info",
                    icon = InvokeIcons.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenAppInfo,
                ),
            )
            if (canUninstall) {
                add(
                    SheetActionRow(
                        label = "Uninstall",
                        icon = InvokeIcons.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onUninstall,
                    ),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            actions.forEachIndexed { index, action ->
                SegmentedListItem(
                    onClick = action.onClick,
                    shapes = ListItemDefaults.segmentedShapes(index, actions.size),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = action.tint,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    content = {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun LiveActionTilePreview(
    appLabel: String,
    iconAsset: AppIconAsset?,
    iconStyle: IconStyle,
    canvasBlock: CanvasBlock.AppTile?,
    previewColumns: Int,
    previewHeightDp: Int,
    previewHasCustomSize: Boolean,
) {
    if (canvasBlock == null) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            TileIconGlyph(
                iconAsset = iconAsset,
                fallbackGlyph = InvokeIcons.Apps,
                iconStyle = iconStyle,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp),
            )
        }
        return
    }

    val reducedMotion = LocalReducedMotion.current
    val defaultDpSpatial = MaterialTheme.motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.Dp>()
    val customColor = canvasBlock.customBgArgb?.let(::Color)
    val containerColor = customColor ?: when (canvasBlock.tone) {
        TileTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        TileTone.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
        TileTone.SURFACE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (customColor != null) {
        if (customColor.luminance() > 0.42f) Color(0xFF141218) else Color.White
    } else {
        when (canvasBlock.tone) {
            TileTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
            TileTone.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
            TileTone.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
            TileTone.SURFACE -> MaterialTheme.colorScheme.onSurface
        }
    }

    val useFullColor = iconAsset != null &&
        (iconStyle == IconStyle.FULL_COLOR || iconAsset.monochrome == null)
    val isFloatingIcon = previewColumns == 1 &&
        (if (previewHasCustomSize) previewHeightDp <= 116 && customColor == null else canvasBlock.span == TileSpan.COMPACT)

    if (isFloatingIcon) {
        val targetIconSize = if (previewHasCustomSize) {
            (previewHeightDp * 0.72f).coerceIn(40f, 88f).dp
        } else {
            60.dp
        }
        val animatedIconSize by animateDpAsState(
            targetValue = targetIconSize,
            animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
            label = "previewFloatingIconSize",
        )
        val shape = rememberAnimatedContainerMorphShape(
            shapePreset = canvasBlock.shape,
            aspectRatio = 1f,
            reducedMotion = reducedMotion,
        )
        Box(
            modifier = Modifier
                .size(animatedIconSize)
                .clip(shape)
                .background(if (useFullColor && customColor == null) Color.Transparent else containerColor),
            contentAlignment = Alignment.Center,
        ) {
            when {
                useFullColor && customColor == null -> {
                    Image(
                        bitmap = iconAsset.fullColor,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(animatedIconSize).clip(shape),
                    )
                }
                useFullColor -> {
                    Image(
                        bitmap = iconAsset.fullColor,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(animatedIconSize * 0.7f).clip(shape),
                    )
                }
                iconAsset?.monochrome != null -> {
                    Image(
                        bitmap = iconAsset.monochrome,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(contentColor),
                        modifier = Modifier.size(animatedIconSize * 0.52f),
                    )
                }
                else -> {
                    Icon(
                        imageVector = InvokeIcons.Apps,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(animatedIconSize * 0.47f),
                    )
                }
            }
        }
    } else {
        val targetWidthDp = (previewColumns * 29f).coerceIn(46f, 116f).dp
        val targetHeightDp = (previewHeightDp * 0.36f).coerceIn(40f, 84f).dp
        val animatedWidth by animateDpAsState(
            targetValue = targetWidthDp,
            animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
            label = "previewTileWidth",
        )
        val animatedHeight by animateDpAsState(
            targetValue = targetHeightDp,
            animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
            label = "previewTileHeight",
        )
        val previewAspect = ((previewColumns * 86f) / previewHeightDp.toFloat().coerceAtLeast(1f))
            .coerceIn(0.45f, 4.2f)
        val containerShape = rememberAnimatedContainerMorphShape(
            shapePreset = canvasBlock.shape,
            aspectRatio = previewAspect,
            reducedMotion = reducedMotion,
        )

        Surface(
            shape = containerShape,
            color = containerColor,
            contentColor = contentColor,
            modifier = Modifier
                .width(animatedWidth)
                .height(animatedHeight),
        ) {
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                val showLabel = previewAspect >= 1.25f && previewColumns >= 2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (useFullColor) Color.Transparent else contentColor.copy(alpha = 0.12f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TileIconGlyph(
                            iconAsset = iconAsset,
                            fallbackGlyph = InvokeIcons.Apps,
                            iconStyle = iconStyle,
                            tint = contentColor,
                            modifier = Modifier.size(if (useFullColor) 30.dp else 19.dp),
                        )
                    }
                    if (showLabel) {
                        Text(
                            text = appLabel,
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

private data class SheetActionRow(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Preview(name = "App Action Sheet · Dark", showBackground = true)
@Composable
private fun AppActionSheetPreview() {
    InvokeTheme(
        settings = LauncherSettings(
            darkMode = DarkMode.DARK,
            dynamicColor = false,
        ),
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Box(
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(top = 16.dp),
            ) {
                AppActionSheetContent(
                    appLabel = "Messages",
                    packageName = "com.google.android.apps.messaging",
                    isWorkProfile = false,
                    iconAsset = null,
                    iconStyle = IconStyle.THEMED_MONOCHROME,
                    canvasBlock = CanvasBlock.AppTile(
                        id = "sample",
                        appKey = AppKey("com.google.android.apps.messaging", ".Main", 0L),
                        span = TileSpan.WIDE,
                    ),
                    shortcuts = emptyList(),
                    hasShortcutPermission = true,
                    canUninstall = true,
                    onAddToCanvas = {},
                    onSetCanvasSize = { _, _ -> },
                    onSetCanvasShape = { _, _ -> },
                    onRemoveFromCanvas = {},
                    onLaunchShortcut = {},
                    onLaunchFloating = {},
                    onOpenAppInfo = {},
                    onUninstall = {},
                )
            }
        }
    }
}
