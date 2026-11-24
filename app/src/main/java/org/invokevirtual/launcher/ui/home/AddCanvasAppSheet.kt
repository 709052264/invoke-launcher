package org.invokevirtual.launcher.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.restPolygon
import org.invokevirtual.launcher.ui.theme.toContainerShape
import androidx.compose.material3.toShape

private enum class AddCanvasTab(val label: String) {
    APPS("Apps"),
    SHORTCUTS("Shortcuts"),
}

/**
 * M3 Expressive sheet for adding any installed app OR app shortcut onto the Living Home Canvas
 * with a chosen span (`1×1`, `2×1`, or `2×2`) and custom container/icon shape.
 */
@Composable
fun AddCanvasAppSheet(
    installedApps: List<InstalledApp>,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onSelectApp: (InstalledApp, TileSpan, TileShapePreset) -> Unit,
    onSelectShortcut: (AppShortcutItem, TileSpan, TileShapePreset) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val haptic = LocalHapticFeedback.current

    var activeTab by remember { mutableStateOf(AddCanvasTab.APPS) }
    var query by remember { mutableStateOf("") }
    var selectedSpan by remember { mutableStateOf(TileSpan.COMPACT) }
    var selectedShape by remember { mutableStateOf(TileShapePreset.SQUIRCLE) }

    val hasShortcutPermission = remember(appCatalog) {
        appCatalog?.hasShortcutPermission() == true
    }

    val allShortcuts by produceState<List<AppShortcutItem>>(
        initialValue = emptyList(),
        appCatalog,
        activeTab,
    ) {
        value = if (appCatalog != null && hasShortcutPermission) {
            withContext(Dispatchers.Default) { appCatalog.loadAllShortcuts() }
        } else {
            emptyList()
        }
    }

    val filteredApps = remember(installedApps, query) {
        val q = query.trim()
        if (q.isEmpty()) installedApps else installedApps.filter { it.label.contains(q, ignoreCase = true) }
    }

    val filteredShortcuts = remember(allShortcuts, query) {
        val q = query.trim()
        if (q.isEmpty()) {
            allShortcuts
        } else {
            allShortcuts.filter {
                it.label.contains(q, ignoreCase = true) || it.appLabel.contains(q, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (activeTab == AddCanvasTab.APPS) "Add App to Home" else "Pin Shortcut to Home",
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Choose Apps or Shortcuts, tile span, and container shape",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    val tabs = AddCanvasTab.entries
                    tabs.forEachIndexed { index, tab ->
                        val shapes = if (index == 0) {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        } else {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        }
                        FilledTonalToggleButton(
                            checked = activeTab == tab,
                            onCheckedChange = {
                                if (it) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTab = tab
                                    if (tab == AddCanvasTab.SHORTCUTS && selectedShape == TileShapePreset.SQUIRCLE) {
                                        selectedShape = TileShapePreset.LEAF
                                    }
                                }
                            },
                            shapes = shapes,
                            colors = FilledTonalToggleButtonDefaults.colors(),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1,
                            )
                        }
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = TileSpan.entries,
                        key = { it.name },
                    ) { span ->
                        FilterChip(
                            selected = selectedSpan == span,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedSpan = span
                            },
                            label = {
                                Text(
                                    text = span.label,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    maxLines = 1,
                                )
                            },
                            modifier = Modifier.heightIn(min = 40.dp),
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = TileShapePreset.entries,
                        key = { it.name },
                    ) { shapePreset ->
                        val chipShape = if (selectedSpan == TileSpan.COMPACT) {
                            RoundedCornerShape(percent = 50)
                        } else {
                            shapePreset.toContainerShape(isWide = true)
                        }
                        FilterChip(
                            selected = selectedShape == shapePreset,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedShape = shapePreset
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(shapePreset.toContainerShape(isHero = true))
                                        .background(
                                            if (selectedShape == shapePreset) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        ),
                                )
                            },
                            shape = chipShape,
                            label = {
                                Text(
                                    text = shapePreset.label,
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    maxLines = 1,
                                )
                            },
                            modifier = Modifier.heightIn(min = 40.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    placeholder = {
                        Text(
                            text = if (activeTab == AddCanvasTab.APPS) {
                                "Filter apps…"
                            } else {
                                "Filter shortcuts (e.g. Compose, Selfie, Alarm)…"
                            },
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
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Icon(
                                    imageVector = InvokeIcons.Close,
                                    contentDescription = "Clear filter",
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

                if (activeTab == AddCanvasTab.APPS) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        itemsIndexed(
                            items = filteredApps,
                            key = { _, app -> "${app.key.packageName}/${app.key.className}/${app.key.userSerial}" },
                        ) { index, app ->
                            val iconAsset by produceState<AppIconAsset?>(null, app, appCatalog) {
                                value = appCatalog?.loadIcon(app)
                            }
                            SegmentedListItem(
                                onClick = { onSelectApp(app, selectedSpan, selectedShape) },
                                shapes = ListItemDefaults.segmentedShapes(index = index, count = filteredApps.size),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(
                                                if (selectedSpan == TileSpan.COMPACT) {
                                                    selectedShape.restPolygon().toShape()
                                                } else {
                                                    selectedShape.toContainerShape()
                                                },
                                            )
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        TileIconGlyph(
                                            iconAsset = iconAsset,
                                            fallbackGlyph = InvokeIcons.Apps,
                                            iconStyle = iconStyle,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = InvokeIcons.Add,
                                        contentDescription = "Add ${app.label}",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                content = {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                } else if (!hasShortcutPermission || filteredShortcuts.isEmpty()) {
                    Surface(
                        shape = selectedShape.toContainerShape(isWide = true),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = InvokeIcons.ShortcutBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                text = when {
                                    !hasShortcutPermission -> "Set Invoke as default Home"
                                    query.isBlank() -> "No app shortcuts available"
                                    else -> "No shortcuts match \"${query.trim()}\""
                                },
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = if (!hasShortcutPermission) {
                                    "Android restricts live app shortcuts to the active default Home launcher."
                                } else {
                                    "You can also long-press any app or use \"Add to Home screen\" inside apps like Chrome or Contacts."
                                },
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
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        itemsIndexed(
                            items = filteredShortcuts,
                            key = { _, sc -> sc.id },
                        ) { index, shortcut ->
                            SegmentedListItem(
                                onClick = { onSelectShortcut(shortcut, selectedSpan, selectedShape) },
                                shapes = ListItemDefaults.segmentedShapes(index = index, count = filteredShortcuts.size),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(selectedShape.toContainerShape())
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (shortcut.icon != null) {
                                            Image(
                                                bitmap = shortcut.icon,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        } else {
                                            Icon(
                                                imageVector = InvokeIcons.ShortcutBolt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                },
                                supportingContent = {
                                    Text(
                                        text = shortcut.appLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = InvokeIcons.Pin,
                                        contentDescription = "Pin ${shortcut.label} to Home",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
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
                }
            }
        }
    }
}
