package org.invokevirtual.launcher.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.FolderItem
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.NotificationBadgeBox
import org.invokevirtual.launcher.ui.theme.InvokeIcons

private val CellWidth = 84.dp
private val IconSize = 52.dp
private val PopupPadding = 16.dp
private val TitleRowHeight = 48.dp
private val ScreenMargin = 12.dp

/**
 * Lawnchair 风格的文件夹弹窗：从文件夹磁贴所在位置放大展开，
 * 列数随图标数量自适应（约等于 √n，最少 2 列、最多 4 列）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderPopup(
    folder: CanvasBlock.FolderTile,
    appsByKey: Map<AppKey, InstalledApp>,
    anchor: Rect?,
    iconStyle: IconStyle,
    showLabels: Boolean,
    appCatalog: AppCatalog?,
    closeSignal: Int,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
) {
    val apps = remember(folder.effectiveItems, appsByKey) {
        folder.effectiveItems
            .filterIsInstance<FolderItem.AppItem>()
            .mapNotNull { appsByKey[it.appKey] }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val progressState = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (visible) 280 else 200,
            easing = FastOutSlowInEasing,
        ),
        finishedListener = { if (!visible) onDismiss() },
        label = "folderPopupProgress",
    )
    val close: () -> Unit = { visible = false }

    // 返回键 / Home 手势通过 closeSignal 通知：先播放收起动画再真正关闭
    val signalAtOpen = remember { closeSignal }
    LaunchedEffect(closeSignal) {
        if (closeSignal != signalAtOpen) visible = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()
        val insetTop = WindowInsets.safeDrawing.getTop(density).toFloat()
        val insetBottom = WindowInsets.safeDrawing.getBottom(density).toFloat()
        val margin = with(density) { ScreenMargin.toPx() }
        val padPx = with(density) { PopupPadding.toPx() }
        val cellWPx = with(density) { CellWidth.toPx() }
        val cellHPx = with(density) { (if (showLabels) 96.dp else 76.dp).toPx() }
        val titleHPx = with(density) { TitleRowHeight.toPx() }

        // ---- 尺寸：按图标数量自适应 ----
        val count = apps.size
        val maxCols = ((screenW - 2 * margin - 2 * padPx) / cellWPx).toInt().coerceAtLeast(1)
        val minCols = min(2, maxCols)
        val cols = ceil(sqrt(max(count, 1).toDouble())).toInt().coerceIn(minCols, min(4, maxCols))
        val rows = max((count + cols - 1) / cols, 1)

        val areaTop = insetTop + margin
        val areaBottom = screenH - insetBottom - margin
        val popupW = min(cols * cellWPx + 2 * padPx, screenW - 2 * margin)
        val popupH = min(rows * cellHPx + titleHPx + 2 * padPx, max(areaBottom - areaTop, 1f))

        // ---- 位置：以文件夹磁贴为中心，再限制在屏幕内 ----
        val anchorRect = anchor ?: Rect(Offset(screenW / 2f, screenH / 2f), 36f)
        val left = (anchorRect.center.x - popupW / 2f)
            .coerceIn(margin, max(screenW - popupW - margin, margin))
        val top = (anchorRect.center.y - popupH / 2f)
            .coerceIn(areaTop, max(areaBottom - popupH, areaTop))
        val popupCx = left + popupW / 2f
        val popupCy = top + popupH / 2f
        val startScaleX = (anchorRect.width / popupW).coerceIn(0.05f, 1f)
        val startScaleY = (anchorRect.height / popupH).coerceIn(0.05f, 1f)

        // 背景遮罩（点击空白处收起）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progressState.value.coerceIn(0f, 1f) }
                .background(Color.Black.copy(alpha = 0.32f))
                .pointerInput(Unit) { detectTapGestures(onTap = { close() }) },
        )

        // 弹窗本体：从磁贴位置缩放到最终位置
        Box(
            modifier = Modifier
                .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                .width(with(density) { popupW.toDp() })
                .height(with(density) { popupH.toDp() })
                .graphicsLayer {
                    val p = progressState.value.coerceIn(0f, 1f)
                    transformOrigin = TransformOrigin.Center
                    scaleX = lerp(startScaleX, 1f, p)
                    scaleY = lerp(startScaleY, 1f, p)
                    translationX = lerp(anchorRect.center.x - popupCx, 0f, p)
                    translationY = lerp(anchorRect.center.y - popupCy, 0f, p)
                    alpha = (p * 3f).coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                // 吃掉弹窗内空白区域的点击，避免穿透到遮罩
                .pointerInput(Unit) { detectTapGestures(onTap = { }) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PopupPadding)
                    .graphicsLayer {
                        val p = progressState.value.coerceIn(0f, 1f)
                        alpha = ((p - 0.35f) / 0.65f).coerceIn(0f, 1f)
                    },
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    apps.chunked(cols).forEach { rowApps ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            rowApps.forEach { app ->
                                FolderPopupApp(
                                    app = app,
                                    iconStyle = iconStyle,
                                    showLabel = showLabels,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TitleRowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = folder.title.ifBlank { "Folder" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderPopupApp(
    app: InstalledApp,
    iconStyle: IconStyle,
    showLabel: Boolean,
    appCatalog: AppCatalog?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }

    Column(
        modifier = Modifier
            .width(CellWidth)
            .height(if (showLabel) 96.dp else 76.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(top = 8.dp, start = 4.dp, end = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NotificationBadgeBox(apps = listOf(app.key), iconSize = IconSize) {
            ShapedAppIcon(
                iconAsset = iconAsset,
                fallbackGlyph = InvokeIcons.Apps,
                iconStyle = iconStyle,
                shape = when (iconStyle) {
                    IconStyle.CIRCLE -> CircleShape
                    else -> RoundedCornerShape(16.dp)
                },
                size = IconSize,
                monochromeBg = MaterialTheme.colorScheme.primaryContainer,
                monochromeFg = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        if (showLabel) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
        }
    }
}
