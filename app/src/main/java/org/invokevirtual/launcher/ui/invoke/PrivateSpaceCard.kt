package org.invokevirtual.launcher.ui.invoke

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.PrivateSpaceState
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeShapes

/**
 * Android 15–17 Private Space vault anchored at the bottom of the Invoke Sheet.
 * Never leaks Private Space apps into global search or renders private labels when locked.
 */
@Composable
fun PrivateSpaceCard(
    state: PrivateSpaceState,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onSetLocked: (Boolean) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    cardAlpha: Float = 1f,
) {
    val haptic = LocalHapticFeedback.current

    when (state) {
        PrivateSpaceState.Unavailable -> Unit

        is PrivateSpaceState.Locked -> {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = cardAlpha),
                modifier = modifier
                    .fillMaxWidth()
                    .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Private Space",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Locked · Hidden from search",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetLocked(false)
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(
                            text = "Unlock",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        is PrivateSpaceState.Unlocked -> {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = cardAlpha),
                modifier = modifier
                    .fillMaxWidth()
                    .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = InvokeIcons.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Private Space",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Unlocked · ${state.apps.size} apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSetLocked(true)
                            },
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(
                                text = "Lock",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.apps.isEmpty()) {
                        Text(
                            text = "No apps installed in Private Space yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            state.apps.forEachIndexed { index, app ->
                                val itemShape = InvokeShapes.segmentedItemShape(
                                    index = index,
                                    count = state.apps.size,
                                    outerRadius = 18.dp,
                                    innerRadius = 4.dp,
                                )
                                val iconAsset by produceState<AppIconAsset?>(
                                    initialValue = null,
                                    app,
                                    appCatalog,
                                ) {
                                    value = appCatalog?.loadIcon(app)
                                }

                                Surface(
                                    shape = itemShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = cardAlpha),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(itemShape)
                                        .combinedClickable(
                                            onClick = { onLaunchApp(app) },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onLongPressApp(app)
                                            },
                                        ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 68.dp)
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            TileIconGlyph(
                                                iconAsset = iconAsset,
                                                fallbackGlyph = InvokeIcons.Lock,
                                                iconStyle = iconStyle,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.titleMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
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
