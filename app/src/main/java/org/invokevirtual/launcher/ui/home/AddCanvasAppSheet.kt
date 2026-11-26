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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
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
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.toContainerShape

private enum class AddCanvasTab(val label: String) {
    APPS("Apps"),
    SHORTCUTS("Shortcuts"),
    FOLDER("Folder"),
    SPACE("Space"),
}

private data class SpacerPresetOption(
    val title: String,
    val subtitle: String,
    val span: TileSpan,
    val customColumns: Int? = null,
    val customHeightDp: Int? = null,
)

private val HOME_SPACER_PRESETS = listOf(
    SpacerPresetOption(
        title = "Full-Row Space (4×1)",
        subtitle = "82dp tall · pushes tiles down cleanly",
        span = TileSpan.BANNER,
    ),
    SpacerPresetOption(
        title = "Tall Reachability Gap (4×2)",
        subtitle = "172dp tall · brings content into thumb reach",
        span = TileSpan.FEATURED,
    ),
    SpacerPresetOption(
        title = "Deep One-Hand Gap (4×260dp)",
        subtitle = "260dp tall · large top breathing room",
        span = TileSpan.FEATURED,
        customColumns = 4,
        customHeightDp = 260,
    ),
    SpacerPresetOption(
        title = "Half-Row Space (2×1)",
        subtitle = "82dp tall · offset items side by side",
        span = TileSpan.WIDE,
    ),
    SpacerPresetOption(
        title = "Compact Slot Space (1×1)",
        subtitle = "76dp tall · leave a single grid cell open",
        span = TileSpan.COMPACT,
    ),
)

@Composable
fun AddCanvasAppSheet(
    installedApps: List<InstalledApp>,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onSelectApp: (InstalledApp) -> Unit,
    onSelectShortcut: (AppShortcutItem) -> Unit,
    onCreateFolder: (String, List<AppKey>) -> Unit = { _, _ -> },
    onSelectSpacer: (TileSpan, Int?, Int?) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val haptic = LocalHapticFeedback.current

    var activeTab by remember { mutableStateOf(AddCanvasTab.APPS) }
    var query by remember { mutableStateOf("") }
    var folderName by remember { mutableStateOf("") }
    var selectedFolderApps by remember { mutableStateOf<List<AppKey>>(emptyList()) }

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
                        text = when (activeTab) {
                            AddCanvasTab.APPS -> "Add App to Home"
                            AddCanvasTab.SHORTCUTS -> "Pin Shortcut to Home"
                            AddCanvasTab.FOLDER -> "Create Folder"
                            AddCanvasTab.SPACE -> "Add Empty Space"
                        },
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when (activeTab) {
                            AddCanvasTab.FOLDER -> "Name your folder and pick apps to include"
                            AddCanvasTab.SPACE -> "Insert empty space to bring tiles closer to your thumb"
                            else -> "Choose an app or shortcut to add to your Home Canvas"
                        },
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
                        val shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                        FilledTonalToggleButton(
                            checked = activeTab == tab,
                            onCheckedChange = {
                                if (it) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTab = tab
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

                if (activeTab == AddCanvasTab.FOLDER) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = folderName,
                            onValueChange = { folderName = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "Folder name…",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = InvokeIcons.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
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
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCreateFolder(
                                    folderName.trim().ifBlank { "Folder" },
                                    selectedFolderApps,
                                )
                            },
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier.heightIn(min = 52.dp),
                        ) {
                            Text(
                                text = if (selectedFolderApps.isEmpty()) {
                                    "Create"
                                } else {
                                    "Create (${selectedFolderApps.size})"
                                },
                                maxLines = 1,
                            )
                        }
                    }
                }

                if (activeTab != AddCanvasTab.SPACE) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        placeholder = {
                            Text(
                                text = when (activeTab) {
                                    AddCanvasTab.APPS -> "Filter apps…"
                                    AddCanvasTab.SHORTCUTS -> "Filter shortcuts (e.g. Compose, Selfie, Alarm)…"
                                    AddCanvasTab.FOLDER -> "Filter apps to include…"
                                    AddCanvasTab.SPACE -> ""
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
                }

                if (activeTab == AddCanvasTab.SPACE) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        itemsIndexed(
                            items = HOME_SPACER_PRESETS,
                            key = { _, preset -> preset.title },
                        ) { index, preset ->
                            SegmentedListItem(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectSpacer(
                                        preset.span,
                                        preset.customColumns,
                                        preset.customHeightDp
                                    )
                                },
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = index,
                                    count = HOME_SPACER_PRESETS.size
                                ),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(TileShapePreset.SQUIRCLE.toContainerShape())
                                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = InvokeIcons.Swap,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                },
                                supportingContent = {
                                    Text(
                                        text = preset.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = InvokeIcons.Add,
                                        contentDescription = "Add ${preset.title}",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                content = {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                } else if (activeTab == AddCanvasTab.APPS || activeTab == AddCanvasTab.FOLDER) {
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
                            val isSelectedForFolder =
                                activeTab == AddCanvasTab.FOLDER && app.key in selectedFolderApps
                            SegmentedListItem(
                                onClick = {
                                    if (activeTab == AddCanvasTab.FOLDER) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedFolderApps = if (isSelectedForFolder) {
                                            selectedFolderApps - app.key
                                        } else {
                                            selectedFolderApps + app.key
                                        }
                                    } else {
                                        onSelectApp(app)
                                    }
                                },
                                shapes = ListItemDefaults.segmentedShapes(index = index, count = filteredApps.size),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = if (isSelectedForFolder) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                ),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(TileShapePreset.SQUIRCLE.toContainerShape())
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
                                    if (activeTab == AddCanvasTab.FOLDER) {
                                        Checkbox(
                                            checked = isSelectedForFolder,
                                            onCheckedChange = null,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = InvokeIcons.Add,
                                            contentDescription = "Add ${app.label}",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
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
                        shape = RoundedCornerShape(24.dp),
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
                                onClick = { onSelectShortcut(shortcut) },
                                shapes = ListItemDefaults.segmentedShapes(index = index, count = filteredShortcuts.size),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(TileShapePreset.LEAF.toContainerShape())
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
