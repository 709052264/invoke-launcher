from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")


def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"{what}: 期望匹配 1 处，实际 {text.count(old)} 处"
    return text.replace(old, new)


# ---------- InvokeSheet.kt ----------
p = BASE / "ui/invoke/InvokeSheet.kt"
t = p.read_text(encoding="utf-8")

# 1) ModalBottomSheet 里重复的 onDismissRequest
dup = "        onDismissRequest = onDismiss,\n"
assert t.count(dup) == 2, f"重复的 onDismissRequest: 期望 2 处，实际 {t.count(dup)}"
first = t.index(dup)
second = t.index(dup, first + 1)
t = t[:second] + t[second + len(dup):]

# 2) 把 onHidePwa 传给 InvokeSheetContent
t = sub_once(
    t,
    "            hiddenPwaIds = hiddenPwaIds, // 👈 传给 Content\n",
    "            hiddenPwaIds = hiddenPwaIds,\n            onHidePwa = onHidePwa,\n",
    "forward onHidePwa",
)

# 3) 先加载全部 PWA 快捷方式，再按隐藏列表实时过滤
old_ps = """    val allShortcuts by produceState<List<AppShortcutItem>>(initialValue = emptyList(), appCatalog, pwaRefreshKey) {
        value = if (appCatalog != null && appCatalog.hasShortcutPermission()) {
            withContext(Dispatchers.Default) {
                appCatalog.loadPwaShortcuts().filter { it.id !in hiddenPwaIds } // 👈 这里过滤掉本地隐藏的
            }
        } else {
            emptyList()
        }
    }
"""
new_ps = """    val loadedShortcuts by produceState<List<AppShortcutItem>>(initialValue = emptyList(), appCatalog, pwaRefreshKey) {
        value = if (appCatalog != null && appCatalog.hasShortcutPermission()) {
            withContext(Dispatchers.Default) { appCatalog.loadPwaShortcuts() }
        } else {
            emptyList()
        }
    }
    // 隐藏列表一变就立即重新过滤，不依赖设置异步保存完成的时机
    val allShortcuts = remember(loadedShortcuts, hiddenPwaIds) {
        loadedShortcuts.filter { it.id !in hiddenPwaIds }
    }
"""
t = sub_once(t, old_ps, new_ps, "produceState")
p.write_text(t, encoding="utf-8")

# ---------- HomeCanvas.kt ----------
p = BASE / "ui/home/HomeCanvas.kt"
t = p.read_text(encoding="utf-8")

# 4) HomeCanvas 新增参数
t = sub_once(
    t,
    "    onTogglePinInDrawer: (AppKey) -> Unit = {},\n) {\n    val context = LocalContext.current",
    "    onTogglePinInDrawer: (AppKey) -> Unit = {},\n    onHidePwa: (String) -> Unit = {},\n) {\n    val context = LocalContext.current",
    "HomeCanvas param",
)

# 5) 用参数替换找不到 scope / settingsStore 的 lambda
old_cb = """            onHidePwa = { id ->
                scope.launch {
                    settingsStore.setHiddenPwaIds(settings.hiddenPwaIds + id)
                }
            }
"""
t = sub_once(t, old_cb, "            onHidePwa = onHidePwa,\n", "HomeCanvas callback")
p.write_text(t, encoding="utf-8")
print("OK")
