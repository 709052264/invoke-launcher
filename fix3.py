
import re
from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")


def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"{what}: 期望匹配 1 处，实际 {text.count(old)} 处"
    return text.replace(old, new)


# ---------- InvokeSheet.kt ----------
p = BASE / "ui/invoke/InvokeSheet.kt"
t = p.read_text(encoding="utf-8")

# 1) 给 InvokeSheet 增加 homePressTick 参数
t, n = re.subn(
    r"\) \{\n    val sheetState = rememberBottomSheetState\(",
    "    homePressTick: Int = 0,\n) {\n    val sheetState = rememberBottomSheetState(",
    t,
)
assert n == 1, f"InvokeSheet 参数: 期望匹配 1 处，实际 {n} 处"

# 2) 在 sheetState 定义之后加入：收到 Home 事件时先播放收起动画，再关闭
effect = """
    // 系统 Home 手势（上滑回桌面）：先播放和下滑/返回相同的收起动画，动画结束后再真正关闭
    val homeTickAtOpen = remember { homePressTick }
    LaunchedEffect(homePressTick) {
        if (homePressTick != homeTickAtOpen) {
            sheetState.hide()
            onDismiss()
        }
    }
"""
t, n = re.subn(
    r"(    val sheetState = rememberBottomSheetState\(.*?\n    \)\n)",
    lambda m: m.group(1) + effect,
    t,
    count=1,
    flags=re.S,
)
assert n == 1, f"sheetState 定义: 期望匹配 1 处，实际 {n} 处"
p.write_text(t, encoding="utf-8")

# ---------- HomeCanvas.kt ----------
p = BASE / "ui/home/HomeCanvas.kt"
t = p.read_text(encoding="utf-8")

# 3) 删掉上一版"直接关闭"的写法（那会跳过动画，造成卡顿）
t = sub_once(
    t,
    "\n    // 系统 Home 手势（上滑回桌面）：MainActivity.onNewIntent 会递增 homePressTick\n"
    "    LaunchedEffect(homePressTick) {\n"
    "        if (homePressTick > 0 && invokeSheetOpen) closeInvokeSheet()\n"
    "    }\n",
    "",
    "remove instant close",
)

# 4) 把 homePressTick 交给抽屉
t = sub_once(
    t,
    "            onDismiss = closeInvokeSheet,\n",
    "            onDismiss = closeInvokeSheet,\n            homePressTick = homePressTick,\n",
    "pass homePressTick",
)
p.write_text(t, encoding="utf-8")
print("OK")
