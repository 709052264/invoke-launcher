import re
from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")


def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"{what}: 期望匹配 1 处，实际 {text.count(old)} 处"
    return text.replace(old, new)


# ---------------------------------------------------------------
# 1) 新文件：FolderPopup.kt
# ---------------------------------------------------------------
POPUP = r'''package org.invokevirtual.launcher.ui.home

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
'''
(BASE / "ui/home/FolderPopup.kt").write_text(POPUP, encoding="utf-8")

# ---------------------------------------------------------------
# 2) HomeCanvas.kt：点击文件夹改为打开弹窗
# ---------------------------------------------------------------
p = BASE / "ui/home/HomeCanvas.kt"
t = p.read_text(encoding="utf-8")

# a) 状态
decl = "    var openFolderId by rememberSaveable { mutableStateOf<String?>(null) }\n"
t = sub_once(
    t,
    decl,
    decl
    + "    // 文件夹弹窗（Lawnchair 式从文件夹位置展开）；原来的底部面板保留给「Edit」\n"
    + "    var popupFolderId by rememberSaveable { mutableStateOf<String?>(null) }\n"
    + "    var folderCloseTick by remember { androidx.compose.runtime.mutableIntStateOf(0) }\n"
    + "    val folderBounds = remember {\n"
    + "        androidx.compose.runtime.mutableStateMapOf<String, androidx.compose.ui.geometry.Rect>()\n"
    + "    }\n",
    "state",
)

# b) import boundsInRoot
if "import androidx.compose.ui.layout.boundsInRoot\n" not in t:
    t = sub_once(
        t,
        "import androidx.compose.ui.layout.onGloballyPositioned\n",
        "import androidx.compose.ui.layout.boundsInRoot\nimport androidx.compose.ui.layout.onGloballyPositioned\n",
        "import boundsInRoot",
    )

# c) 文件夹磁贴：记录位置 + 点击打开弹窗
pat = re.compile(r"CanvasFolderBlockCard\(.*?modifier = animateMod,\n", re.S)
matches = pat.findall(t)
assert len(matches) == 1, f"CanvasFolderBlockCard 调用: 期望 1 处，实际 {len(matches)} 处"


def patch_card(m):
    s = m.group(0)
    assert s.count("onClick = { openFolderId = block.id },") == 1, "文件夹磁贴 onClick"
    s = s.replace("onClick = { openFolderId = block.id },", "onClick = { popupFolderId = block.id },")
    s = s.replace(
        "modifier = animateMod,\n",
        "modifier = animateMod.onGloballyPositioned { folderBounds[block.id] = it.boundsInRoot() },\n",
    )
    return s


t = pat.sub(patch_card, t, count=1)

# d) 返回键：先收起弹窗
t = sub_once(
    t,
    "manageFolderAppsId != null ||\n",
    "popupFolderId != null ||\n                manageFolderAppsId != null ||\n",
    "back enabled",
)
t = sub_once(
    t,
    "manageFolderAppsId != null -> manageFolderAppsId = null\n",
    "popupFolderId != null -> folderCloseTick++\n                manageFolderAppsId != null -> manageFolderAppsId = null\n",
    "back branch",
)

# e) 渲染弹窗：插进主 Box 里面（最后一个子节点），叠在所有内容之上
anchor = "    }\n\n    if (invokeSheetOpen) {\n        InvokeSheet(\n"
assert t.count(anchor) == 1, f"插入点: 期望匹配 1 处，实际 {t.count(anchor)} 处"
k = t.index(anchor)


def match_end(s, j):
    """从 j 处的括号开始，返回与之配对的结束括号位置"""
    depth = 0
    while True:
        c = s[j]
        if c in "({":
            depth += 1
        elif c in ")}":
            depth -= 1
            if depth == 0:
                return j
        j += 1


# 校验：插入点前的 "}" 确实是主 Box 的结束括号，否则弹窗会放错位置
mark = t.index(".then(scrimModifier)")
box_start = t.rindex("\n    Box(\n", 0, mark) + 1
brace = match_end(t, t.index("(", box_start)) + 1
while t[brace] in " \n":
    brace += 1
assert t[brace] == "{" and match_end(t, brace) == k + 4, "插入点不是主 Box 的结尾，请把 HomeCanvas.kt 发给我"

popup_block = """        // 文件夹弹窗：放在主 Box 的最后，叠在所有内容之上
        popupFolderId?.let { folderId ->
            val popupFolder = foldersList.firstOrNull { it.id == folderId }
            if (popupFolder == null) {
                LaunchedEffect(folderId) { popupFolderId = null }
            } else {
                FolderPopup(
                    folder = popupFolder,
                    appsByKey = appsByKey,
                    anchor = folderBounds[folderId]?.translate(-rootOrigin),
                    iconStyle = settings.iconStyle,
                    showLabels = settings.showLabels,
                    appCatalog = appCatalog,
                    closeSignal = folderCloseTick + homePressTick,
                    onDismiss = { popupFolderId = null },
                    onEdit = {
                        popupFolderId = null
                        openFolderId = folderId
                    },
                    onLaunchApp = { app ->
                        popupFolderId = null
                        onLaunchApp(app, null)
                    },
                    onLongPressApp = { app ->
                        actionTargetFromDrawer = false
                        actionTargetApp = app
                    },
                )
            }
        }
"""
t = t[:k] + popup_block + t[k:]
p.write_text(t, encoding="utf-8")
print("OK")
