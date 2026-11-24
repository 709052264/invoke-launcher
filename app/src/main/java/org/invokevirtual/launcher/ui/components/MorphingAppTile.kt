package org.invokevirtual.launcher.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import android.graphics.Rect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.theme.InvokeShapes
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.rememberMorphShape
import kotlin.math.roundToInt

enum class BentoTone {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    SURFACE,
}

@Immutable
data class BentoTileModel(
    val id: String,
    val label: String,
    val subtitle: String? = null,
    val tone: BentoTone = BentoTone.SURFACE,
    val app: InstalledApp? = null,
    val fallbackGlyph: ImageVector,
    val pressedPolygon: RoundedPolygon = MaterialShapes.Cookie4Sided,
    val isPinned: Boolean = false,
)

@Composable
fun MorphingAppTile(
    model: BentoTileModel,
    iconStyle: IconStyle,
    showLabel: Boolean,
    appCatalog: AppCatalog?,
    onClick: (Rect?) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val morphProgress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else spatialSpec,
        label = "tileMorph",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (reducedMotion) snap() else spatialSpec,
        label = "tileScale",
    )

    val containerColor = model.tone.containerColor()
    val contentColor = model.tone.contentColor()
    val tileShape = rememberMorphShape(
        start = MaterialShapes.Square,
        end = model.pressedPolygon,
        progress = morphProgress,
    )

    var bounds by remember { mutableStateOf<Rect?>(null) }

    val iconAsset by produceState<AppIconAsset?>(initialValue = null, model.app, appCatalog) {
        value = if (model.app != null && appCatalog != null) {
            appCatalog.loadIcon(model.app)
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                bounds = Rect(
                    b.left.roundToInt(),
                    b.top.roundToInt(),
                    b.right.roundToInt(),
                    b.bottom.roundToInt(),
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(tileShape)
            .background(containerColor)
            .semantics {
                contentDescription = model.label
                if (model.app != null) {
                    customActions = listOf(
                        CustomAccessibilityAction("App actions") {
                            onLongClick()
                            true
                        },
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = { onClick(bounds) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TileIconGlyph(
                iconAsset = iconAsset,
                fallbackGlyph = model.fallbackGlyph,
                iconStyle = iconStyle,
                tint = contentColor,
                modifier = Modifier.size(32.dp),
            )
            if (showLabel) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = model.label,
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun FocusCapsuleCard(
    model: BentoTileModel,
    badgeLabel: String,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onClick: (Rect?) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = if (reducedMotion) snap() else spatialSpec,
        label = "capsuleScale",
    )
    val badgeMorph by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else spatialSpec,
        label = "capsuleBadgeMorph",
    )

    val badgeShape = rememberMorphShape(
        start = MaterialShapes.Clover4Leaf,
        end = MaterialShapes.SoftBurst,
        progress = badgeMorph,
    )

    val containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    var bounds by remember { mutableStateOf<Rect?>(null) }

    val iconAsset by produceState<AppIconAsset?>(initialValue = null, model.app, appCatalog) {
        value = if (model.app != null && appCatalog != null) {
            appCatalog.loadIcon(model.app)
        } else {
            null
        }
    }

    Surface(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                bounds = Rect(
                    b.left.roundToInt(),
                    b.top.roundToInt(),
                    b.right.roundToInt(),
                    b.bottom.roundToInt(),
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(InvokeShapes.BentoCapsuleShape)
            .semantics {
                contentDescription = "${model.label}. ${model.subtitle.orEmpty()}"
                if (model.app != null) {
                    customActions = listOf(
                        CustomAccessibilityAction("App actions") {
                            onLongClick()
                            true
                        },
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(bounds) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = InvokeShapes.BentoCapsuleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(badgeShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    TileIconGlyph(
                        iconAsset = iconAsset,
                        fallbackGlyph = model.fallbackGlyph,
                        iconStyle = iconStyle,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(contentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = if (model.isPinned) "Pinned" else badgeLabel,
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = model.label,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!model.subtitle.isNullOrBlank()) {
                    Text(
                        text = model.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.80f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun TileIconGlyph(
    iconAsset: AppIconAsset?,
    fallbackGlyph: ImageVector,
    iconStyle: IconStyle,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    when {
        // 1. 没有图标资源时，回退到单色 glyph
        iconAsset == null -> {
            Icon(
                imageVector = fallbackGlyph,
                contentDescription = null,
                tint = tint,
                modifier = modifier,
            )
        }

        // 2. 只有 THEMED_MONOCHROME 走单色渲染
        iconStyle == IconStyle.THEMED_MONOCHROME -> {
            Icon(
                bitmap = iconAsset.monochrome.asImageBitmap(),
                contentDescription = null,
                tint = tint,
                modifier = modifier.clip(getIconShape(iconStyle)),
            )
        }

        // 3. 其余（FULL_COLOR + 所有形状）都走全彩渲染
        else -> {
            Image(
                bitmap = iconAsset.fullColor,
                contentDescription = null,
                modifier = modifier.clip(getIconShape(iconStyle)),
            )
        }
    }
}

// 👇 ADD YOUR FUNCTION HERE 👇
private fun getIconShape(style: IconStyle): Shape = when (style) {
    IconStyle.CIRCLE -> CircleShape
    IconStyle.SQUIRCLE -> RoundedCornerShape(percent = 30)
    IconStyle.ROUNDED_SQUARE -> RoundedCornerShape(percent = 20)
    IconStyle.SYSTEM -> {
        // Android 系统自适应图标遮罩无公开 Compose API。
        // 多数 OEM 使用近似 squircle（圆角约 22%~25%），用此近似。
        RoundedCornerShape(percent = 22)
    }
    // FULL_COLOR 保持原来默认的圆形行为
    else -> CircleShape
}

@Composable
private fun BentoTone.containerColor(): Color = when (this) {
    BentoTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    BentoTone.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
    BentoTone.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f)
    BentoTone.SURFACE -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f)
}

@Composable
private fun BentoTone.contentColor(): Color = when (this) {
    BentoTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    BentoTone.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    BentoTone.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    BentoTone.SURFACE -> MaterialTheme.colorScheme.onSurface
}
