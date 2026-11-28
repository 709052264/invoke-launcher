import re
from pathlib import Path

p = Path("app/src/main/java/org/invokevirtual/launcher/ui/home/InvokeDock.kt")
t = p.read_text(encoding="utf-8")


def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"{what}: 期望匹配 1 处，实际 {text.count(old)} 处"
    return text.replace(old, new)


# 1) 固定高度常量（想调 dock 的高矮只改这一行）
t = sub_once(
    t,
    "@Composable\nfun InvokeBottomDock(\n",
    "/** 底部 dock 的固定高度：不再随图标数量变化 */\n"
    "private val DockHeight = 104.dp\n\n"
    "@Composable\nfun InvokeBottomDock(\n",
    "DockHeight",
)

# 2) 用固定高度的 Surface + Row 替换 TOOLBAR 分支
new_toolbar = '''            DockStyle.TOOLBAR -> {
                if (quickActions.isNotEmpty() || showModeButton) {
                    // 图标 ≤2 个：dock 收窄成胶囊；≥3 个：铺满宽度。两种情况高度都固定。
                    val compactDock = quickActions.size <= 2
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                            .then(if (compactDock) Modifier else Modifier.fillMaxWidth())
                            .height(DockHeight),
                    ) {
                        if (compactDock) {
                            Row(
                                modifier = Modifier.padding(horizontal = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                quickActions.forEach { action ->
                                    DockActionSlotButton(
                                        action = action,
                                        iconStyle = iconStyle,
                                        appCatalog = appCatalog,
                                    )
                                }
                                if (showModeButton) {
                                    InvokeSearchPillButton(
                                        searchCd = searchCd,
                                        onClick = onInvokeSearchClick,
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    quickActions.forEach { action ->
                                        DockActionSlotButton(
                                            action = action,
                                            iconStyle = iconStyle,
                                            appCatalog = appCatalog,
                                        )
                                    }
                                }
                                if (showModeButton) {
                                    InvokeSearchPillButton(
                                        searchCd = searchCd,
                                        onClick = onInvokeSearchClick,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DockStyle.MINIMAL_SEARCH -> {'''

pat = re.compile(
    r"            DockStyle\.TOOLBAR -> \{\n.*?\n            \}\n\n            DockStyle\.MINIMAL_SEARCH -> \{",
    re.S,
)
assert len(pat.findall(t)) == 1, f"TOOLBAR 分支: 期望匹配 1 处，实际 {len(pat.findall(t))} 处"
t = pat.sub(lambda m: new_toolbar, t, count=1)

p.write_text(t, encoding="utf-8")
print("OK")
