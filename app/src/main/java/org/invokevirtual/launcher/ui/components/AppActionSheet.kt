package org.invokevirtual.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme

@Composable
fun AppActionSheet(
    app: InstalledApp,
    folders: List<CanvasBlock.FolderTile> = emptyList(),
    activeFolder: CanvasBlock.FolderTile? = null,
    isFromDrawer: Boolean = false,
    isPinnedInDrawer: Boolean = false,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onDismiss: () -> Unit,
    onToggleFolderMembership: (CanvasBlock.FolderTile) -> Unit = {},
    onCreateFolderWithApp: () -> Unit = {},
    onRemoveFromActiveFolder: () -> Unit = {},
    onTogglePinInDrawer: (() -> Unit)? = null,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onOpenAppInfo: () -> Unit,
    onHideApp: (AppKey) -> Unit = {}, // 👈 新增
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
            appKey = app.key,
            appLabel = app.label,
            packageName = app.componentName.packageName,
            isWorkProfile = app.isWorkProfile,
            iconAsset = iconAsset,
            iconStyle = iconStyle,
            folders = folders,
            activeFolder = activeFolder,
            isFromDrawer = isFromDrawer,
            isPinnedInDrawer = isPinnedInDrawer,
            shortcuts = shortcuts,
            hasShortcutPermission = hasShortcutPermission,
            canUninstall = canUninstall,
            onToggleFolderMembership = onToggleFolderMembership,
            onCreateFolderWithApp = {
                onCreateFolderWithApp()
                onDismiss()
            },
            onRemoveFromActiveFolder = {
                onRemoveFromActiveFolder()
                onDismiss()
            },
            onTogglePinInDrawer = onTogglePinInDrawer?.let { toggle ->
                {
                    toggle()
                    onDismiss()
                }
            },
            onLaunchShortcut = { shortcut ->
                onDismiss()
                onLaunchShortcut(shortcut)
            },
            onOpenAppInfo = {
                onDismiss()
                onOpenAppInfo()
            },
            onUninstall = {
                onDismiss()
                onUninstall()
            },
            onHideApp = {
                onDismiss()
                onHideApp(app.key)
            },
        )
    }
}

@Composable
internal fun AppActionSheetContent(
    appKey: AppKey? = null,
    appLabel: String,
    packageName: String,
    isWorkProfile: Boolean,
    iconAsset: AppIconAsset?,
    iconStyle: IconStyle,
    folders: List<CanvasBlock.FolderTile> = emptyList(),
    activeFolder: CanvasBlock.FolderTile? = null,
    isFromDrawer: Boolean = false,
    isPinnedInDrawer: Boolean = false,
    shortcuts: List<AppShortcutItem>,
    hasShortcutPermission: Boolean,
    canUninstall: Boolean,
    onToggleFolderMembership: (CanvasBlock.FolderTile) -> Unit = {},
    onCreateFolderWithApp: () -> Unit = {},
    onRemoveFromActiveFolder: () -> Unit = {},
    onTogglePinInDrawer: (() -> Unit)? = null,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onOpenAppInfo: () -> Unit,
    onHideApp: (AppKey) -> Unit = {}, // 👈 新增
    onUninstall: () -> Unit,
) {
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
                    text = if (isWorkProfile) {
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

        if (!isFromDrawer && appKey != null) {
            SectionLabel("Folders")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(folders, key = { it.id }) { folder ->
                    val inFolder = appKey in folder.apps
                    FilterChip(
                        selected = inFolder,
                        onClick = { onToggleFolderMembership(folder) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (inFolder) InvokeIcons.Check else InvokeIcons.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = {
                            Text(
                                text = folder.title.ifBlank { "Folder" },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
                item(key = "new_folder_from_app") {
                    FilterChip(
                        selected = false,
                        onClick = onCreateFolderWithApp,
                        leadingIcon = {
                            Icon(
                                imageVector = InvokeIcons.CreateNewFolder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = { Text("New folder") },
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }
        }

        SectionLabel("Actions")
        val actions = buildList {
            if (activeFolder != null && !isFromDrawer) {
                add(
                    SheetActionRow(
                        label = "Remove from ${activeFolder.title.ifBlank { "Folder" }}",
                        icon = InvokeIcons.Folder,
                        tint = MaterialTheme.colorScheme.tertiary,
                        onClick = onRemoveFromActiveFolder,
                    ),
                )
            }
            if (onTogglePinInDrawer != null) {
                add(
                    SheetActionRow(
                        label = if (isPinnedInDrawer) "Unpin from drawer" else "Pin in drawer",
                        icon = InvokeIcons.Pin,
                        tint = if (isPinnedInDrawer) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        onClick = onTogglePinInDrawer,
                    ),
                )
            }
            add(
                SheetActionRow(
                    label = "App info",
                    icon = InvokeIcons.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenAppInfo,
                ),
            )
            if (isFromDrawer && appKey != null) {
                add(
                    SheetActionRow(
                        label = "Hide from All apps",
                        icon = InvokeIcons.Close,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { onHideApp(appKey) },
                    ),
                )
            }
            
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
                    appKey = AppKey("com.google.android.apps.messaging", ".Main", 0L),
                    appLabel = "Messages",
                    packageName = "com.google.android.apps.messaging",
                    isWorkProfile = false,
                    iconAsset = null,
                    iconStyle = IconStyle.THEMED_MONOCHROME,
                    folders = listOf(
                        CanvasBlock.FolderTile(
                            id = "f1",
                            title = "Comms",
                            apps = listOf(AppKey("com.google.android.apps.messaging", ".Main", 0L)),
                        ),
                    ),
                    shortcuts = emptyList(),
                    hasShortcutPermission = true,
                    canUninstall = true,
                    onTogglePinInDrawer = {},
                    onLaunchShortcut = {},
                    onOpenAppInfo = {},
                    onUninstall = {},
                )
            }
        }
    }
}
