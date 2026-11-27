from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")


def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"{what}: 期望匹配 1 处，实际 {text.count(old)} 处"
    return text.replace(old, new)


# ---------- MainActivity.kt ----------
p = BASE / "MainActivity.kt"
t = p.read_text(encoding="utf-8")

t = sub_once(
    t,
    "import androidx.compose.runtime.mutableStateOf\n",
    "import androidx.compose.runtime.mutableIntStateOf\nimport androidx.compose.runtime.mutableStateOf\n",
    "import mutableIntStateOf",
)

field = "    private var pendingPinRequest by mutableStateOf<LauncherApps.PinItemRequest?>(null)\n"
t = sub_once(
    t,
    field,
    field
    + "\n    // 每次收到系统 Home 事件（上滑回桌面 / Home 键）就 +1，HomeCanvas 据此关闭抽屉\n"
    + "    private var homePressTick by mutableIntStateOf(0)\n",
    "homePressTick field",
)

t = sub_once(
    t,
    "        setIntent(intent)\n        extractPinItemRequest(intent)\n",
    "        setIntent(intent)\n        extractPinItemRequest(intent)\n"
    "        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {\n"
    "            homePressTick++\n"
    "        }\n",
    "onNewIntent",
)

call = (
    "                        onTogglePinInDrawer = { appKey ->\n"
    "                            scope.launch { container.settingsStore.togglePinnedDrawerApp(appKey) }\n"
    "                        },\n"
)
t = sub_once(
    t,
    call,
    call
    + "                        onHidePwa = { id ->\n"
    + "                            scope.launch {\n"
    + "                                container.settingsStore.setHiddenPwaIds(settings.hiddenPwaIds + id)\n"
    + "                            }\n"
    + "                        },\n"
    + "                        homePressTick = homePressTick,\n",
    "HomeCanvas call",
)
p.write_text(t, encoding="utf-8")

# ---------- HomeCanvas.kt ----------
p = BASE / "ui/home/HomeCanvas.kt"
t = p.read_text(encoding="utf-8")

t = sub_once(
    t,
    "    onTogglePinInDrawer: (AppKey) -> Unit = {},\n",
    "    onTogglePinInDrawer: (AppKey) -> Unit = {},\n    homePressTick: Int = 0,\n",
    "HomeCanvas param",
)

close = (
    "    val closeInvokeSheet: () -> Unit = {\n"
    "        invokeSheetOpen = false\n"
    "        onInvokeClearQuery()\n"
    "    }\n"
)
t = sub_once(
    t,
    close,
    close
    + "\n    // 系统 Home 手势（上滑回桌面）：MainActivity.onNewIntent 会递增 homePressTick\n"
    + "    LaunchedEffect(homePressTick) {\n"
    + "        if (homePressTick > 0 && invokeSheetOpen) closeInvokeSheet()\n"
    + "    }\n",
    "close on home",
)
p.write_text(t, encoding="utf-8")
print("OK")
