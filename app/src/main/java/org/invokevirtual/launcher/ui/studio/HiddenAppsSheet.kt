package org.invokevirtual.launcher.ui.studio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons

@Composable
fun HiddenAppsSheet(
    installedApps: List<InstalledApp>,
    hiddenAppKeys: Set<AppKey>,
    hiddenPwaIds: Set<String>,
    appCatalog: AppCatalog?,
    onUnhideApp: (AppKey) -> Unit,
    onUnhidePwa: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    // 获取被隐藏的普通 App
    val hiddenApps = installedApps.filter { it.key in hiddenAppKeys }
    // 获取所有 PWA 快捷方式并过滤出被隐藏的
    val hiddenPwas by produceState<List<AppShortcutItem>>(initialValue = emptyList(), appCatalog) {
        value = if (appCatalog != null && appCatalog.hasShortcutPermission()) {
            appCatalog.loadPwaShortcuts().filter { it.id in hiddenPwaIds }
        } else {
            emptyList()
        }
    }

    val totalCount = hiddenApps.size + hiddenPwas.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Hidden Apps ($totalCount)",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (totalCount == 0) {
                Text(
                    text = "No hidden apps.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    items(hiddenApps, key = { it.key.packageName + it.key.className }) { app ->
                        val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
                            value = appCatalog?.loadIcon(app)
                        }
                        SegmentedListItem(
                            onClick = { onUnhideApp(app.key) }, // 点击整行取消隐藏
                            shapes = ListItemDefaults.segmentedShapes(0, 1),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            leadingContent = {
                                TileIconGlyph(
                                    iconAsset = iconAsset,
                                    fallbackGlyph = InvokeIcons.Apps,
                                    iconStyle = org.invokevirtual.launcher.data.IconStyle.CIRCLE,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(32.dp),
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
                            trailingContent = {
                                Checkbox(checked = true, onCheckedChange = null)
                            }
                        )
                    }

                    items(hiddenPwas, key = { it.id }) { pwa ->
                        SegmentedListItem(
                            onClick = { onUnhidePwa(pwa.id) },
                            shapes = ListItemDefaults.segmentedShapes(0, 1),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            leadingContent = {
                                if (pwa.icon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = pwa.icon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = InvokeIcons.ShortcutBolt,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            content = {
                                Column {
                                    Text(
                                        text = pwa.label,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = pwa.appLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            },
                            trailingContent = {
                                Checkbox(checked = true, onCheckedChange = null)
                            }
                        )
                    }
                }
            }
        }
    }
}
