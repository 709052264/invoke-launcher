package org.invokevirtual.launcher.ui.home

import android.graphics.Rect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.WorkspaceMode
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.ui.components.BentoTileModel
import org.invokevirtual.launcher.ui.components.FocusCapsuleCard
import org.invokevirtual.launcher.ui.components.MorphingAppTile

/**
 * Hero Moment 2: The Expressive Bento Stage.
 *
 * Replaces the uniform 4x5 grid with sculptural visual hierarchy:
 * - Everyday / Night OLED: 1 primary Focus Capsule (2x2) + 4 Tactile Morphing Tiles (1x1).
 * - Deep Focus: simplifies gracefully to 1 Focus Capsule + 2 essential tiles.
 */
@Composable
fun BentoStage(
    workspaceMode: WorkspaceMode,
    focusCapsule: BentoTileModel,
    tactileTiles: List<BentoTileModel>,
    iconStyle: IconStyle,
    showLabels: Boolean,
    appCatalog: AppCatalog?,
    onTileClick: (BentoTileModel, Rect?) -> Unit,
    onTileLongClick: (BentoTileModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileHeight = 104.dp
    val gridGap = 12.dp
    val capsuleHeight = tileHeight * 2 + gridGap

    AnimatedContent(
        targetState = workspaceMode == WorkspaceMode.DEEP_FOCUS,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth(),
        label = "bentoStageLayout",
    ) { isDeepFocus ->
        if (isDeepFocus) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                FocusCapsuleCard(
                    model = focusCapsule,
                    badgeLabel = "Focus",
                    iconStyle = iconStyle,
                    appCatalog = appCatalog,
                    onClick = { bounds -> onTileClick(focusCapsule, bounds) },
                    onLongClick = { onTileLongClick(focusCapsule) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridGap),
                ) {
                    tactileTiles.take(2).forEach { tile ->
                        MorphingAppTile(
                            model = tile,
                            iconStyle = iconStyle,
                            showLabel = showLabels,
                            appCatalog = appCatalog,
                            onClick = { bounds -> onTileClick(tile, bounds) },
                            onLongClick = { onTileLongClick(tile) },
                            modifier = Modifier
                                .weight(1f)
                                .height(tileHeight),
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                FocusCapsuleCard(
                    model = focusCapsule,
                    badgeLabel = if (workspaceMode == WorkspaceMode.NIGHT_AMOLED) "Night" else "Focus",
                    iconStyle = iconStyle,
                    appCatalog = appCatalog,
                    onClick = { bounds -> onTileClick(focusCapsule, bounds) },
                    onLongClick = { onTileLongClick(focusCapsule) },
                    modifier = Modifier
                        .weight(1.06f)
                        .height(capsuleHeight),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(gridGap),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gridGap),
                    ) {
                        tactileTiles.take(2).forEach { tile ->
                            MorphingAppTile(
                                model = tile,
                                iconStyle = iconStyle,
                                showLabel = showLabels,
                                appCatalog = appCatalog,
                                onClick = { bounds -> onTileClick(tile, bounds) },
                                onLongClick = { onTileLongClick(tile) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tileHeight),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gridGap),
                    ) {
                        tactileTiles.drop(2).take(2).forEach { tile ->
                            MorphingAppTile(
                                model = tile,
                                iconStyle = iconStyle,
                                showLabel = showLabels,
                                appCatalog = appCatalog,
                                onClick = { bounds -> onTileClick(tile, bounds) },
                                onLongClick = { onTileLongClick(tile) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tileHeight),
                            )
                        }
                    }
                }
            }
        }
    }
}
