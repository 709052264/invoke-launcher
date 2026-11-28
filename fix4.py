import re
from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")

def sub_once(text, old, new, what):
    assert text.count(old) == 1, f"❌ 失败: {what} (预期 1 处，实际 {text.count(old)} 处)"
    return text.replace(old, new)

def patch_launcher_settings():
    print("正在修改 LauncherSettings.kt ...")
    p = BASE / "data/LauncherSettings.kt"
    t = p.read_text(encoding="utf-8")

    # 1. 数据类增加字段
    t = sub_once(
        t,
        "    val hiddenPwaIds: Set<String> = emptySet(),\n",
        "    val hiddenPwaIds: Set<String> = emptySet(),\n"
        "    val hiddenAppKeys: Set<AppKey> = emptySet(),\n",
        "LauncherSettings 数据类新增 hiddenAppKeys"
    )

    # 2. Keys 增加 Key
    t = sub_once(
        t,
        '        val HIDDEN_PWA_IDS = stringPreferencesKey("hidden_pwa_ids")\n',
        '        val HIDDEN_PWA_IDS = stringPreferencesKey("hidden_pwa_ids")\n'
        '        val HIDDEN_APP_KEYS = stringPreferencesKey("hidden_app_keys")\n',
        "Keys 对象新增 HIDDEN_APP_KEYS"
    )

    # 3. 读取逻辑
    t = sub_once(
        t,
        "            hiddenPwaIds = prefs[Keys.HIDDEN_PWA_IDS]\n                ?.split(\",\")\n                ?.filter { it.isNotBlank() }\n                ?.toSet() \n                ?: emptySet()\n",
        "            hiddenPwaIds = prefs[Keys.HIDDEN_PWA_IDS]\n"
        "                ?.split(\",\")\n"
        "                ?.filter { it.isNotBlank() }\n"
        "                ?.toSet() \n"
        "                ?: emptySet(),\n"
        "            hiddenAppKeys = prefs[Keys.HIDDEN_APP_KEYS]\n"
        "                ?.split(\",\")\n"
        "                ?.filter { it.isNotBlank() }\n"
        "                ?.map { \n"
        "                    val parts = it.split(\"|\")\n"
        "                    AppKey(parts[0], parts[1], parts.getOrNull(2)?.toLongOrNull() ?: 0L)\n"
        "                }\n"
        "                ?.toSet() \n"
        "                ?: emptySet()\n",
        "settings 读取新增 hiddenAppKeys"
    )

    # 4. 写入方法
    t = sub_once(
        t,
        "    suspend fun setHiddenPwaIds(ids: Set<String>) {\n        context.launcherDataStore.edit {\n            it[Keys.HIDDEN_PWA_IDS] = ids.joinToString(\",\")\n        }\n    }\n",
        "    suspend fun setHiddenPwaIds(ids: Set<String>) {\n"
        "        context.launcherDataStore.edit {\n"
        "            it[Keys.HIDDEN_PWA_IDS] = ids.joinToString(\",\")\n"
        "        }\n"
        "    }\n\n"
        "    suspend fun setHiddenAppKeys(keys: Set<AppKey>) {\n"
        "        context.launcherDataStore.edit {\n"
        "            it[Keys.HIDDEN_APP_KEYS] = keys.joinToString(\",\") { key -> \n"
        "                \"${key.packageName}|${key.className}|${key.userSerial}\" \n"
        "            }\n"
        "        }\n"
        "    }\n",
        "SettingsStore 新增 setHiddenAppKeys"
    )
    p.write_text(t, encoding="utf-8")

def patch_app_catalog():
    print("正在修改 AppCatalog.kt ...")
    p = BASE / "system/AppCatalog.kt"
    t = p.read_text(encoding="utf-8")

    target = "    fun syncPinnedShortcuts(\n"
    new_method = """    /** 解除固定快捷方式（将其从系统移除） */
    fun unpinShortcut(packageName: String, shortcutId: String, userSerial: Long) {
        val service = launcherApps ?: return
        if (!hasShortcutPermission()) return
        val handle = userManager?.getUserForSerialNumber(userSerial) ?: Process.myUserHandle()
        
        runCatching {
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            }
            val pinnedShortcuts = service.getShortcuts(query, handle).orEmpty()
            val remainingIds = pinnedShortcuts.filter { it.id != shortcutId }.map { it.id }
            service.pinShortcuts(packageName, remainingIds, handle)
        }
    }

"""
    t = sub_once(t, target, new_method + target, "AppCatalog 新增 unpinShortcut")
    p.write_text(t, encoding="utf-8")

def patch_app_action_sheet():
    print("正在修改 AppActionSheet.kt ...")
    p = BASE / "ui/components/AppActionSheet.kt"
    t = p.read_text(encoding="utf-8")

    # 1. 参数定义
    t = sub_once(
        t,
        "    onUninstall: () -> Unit,\n) {\n",
        "    onUninstall: () -> Unit,\n    onHideApp: (AppKey) -> Unit = {},\n) {\n",
        "AppActionSheet 参数列表"
    )
    t = sub_once(
        t,
        "    onUninstall: () -> Unit,\n) {\n    Column(\n",
        "    onUninstall: () -> Unit,\n    onHideApp: (AppKey) -> Unit = {},\n) {\n    Column(\n",
        "AppActionSheetContent 参数列表"
    )

    # 2. 传入参数
    t = sub_once(
        t,
        "            onUninstall = {\n                onDismiss()\n                onUninstall()\n            },\n        )\n",
        "            onUninstall = {\n                onDismiss()\n                onUninstall()\n            },\n            onHideApp = onHideApp,\n        )\n",
        "调用 Content 时传入 onHideApp"
    )

    # 3. 在 Actions 里新增 Hide
    target_action = """            if (onTogglePinInDrawer != null) {
                add(
                    SheetActionRow(
                        label = if (isPinnedInDrawer) "Unpin from drawer" else "Pin in drawer",
                        icon = InvokeIcons.Pin,
                        tint = if (isPinnedInDrawer) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        onClick = onTogglePinInDrawer,
                    ),
                )
            }
"""
    new_action = target_action + """            if (isFromDrawer && appKey != null) {
                add(
                    SheetActionRow(
                        label = "Hide from All apps",
                        icon = InvokeIcons.Close,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { onHideApp(appKey) },
                    ),
                )
            }
"""
    t = sub_once(t, target_action, new_action, "Actions 列表新增 Hide 选项")
    p.write_text(t, encoding="utf-8")

def patch_invoke_sheet():
    print("正在修改 InvokeSheet.kt ...")
    p = BASE / "ui/invoke/InvokeSheet.kt"
    t = p.read_text(encoding="utf-8")

    # 1. 参数传递
    t = sub_once(
        t,
        "    hiddenPwaIds: Set<String> = emptySet(),\n    onHidePwa: (String) -> Unit = {},\n",
        "    hiddenPwaIds: Set<String> = emptySet(),\n    onHidePwa: (String) -> Unit = {},\n    hiddenAppKeys: Set<AppKey> = emptySet(),\n    onRemovePwa: (AppShortcutItem) -> Unit = {},\n",
        "InvokeSheet 参数列表"
    )
    t = sub_once(
        t,
        "            hiddenPwaIds = hiddenPwaIds,\n            onHidePwa = onHidePwa,\n",
        "            hiddenPwaIds = hiddenPwaIds,\n            onHidePwa = onHidePwa,\n            hiddenAppKeys = hiddenAppKeys,\n            onRemovePwa = onRemovePwa,\n",
        "调用 Content 时传入 hiddenAppKeys, onRemovePwa"
    )
    t = sub_once(
        t,
        "    hiddenPwaIds: Set<String> = emptySet(),\n    onHidePwa: (String) -> Unit = {},\n    uiState: InvokeUiState,\n",
        "    hiddenPwaIds: Set<String> = emptySet(),\n    onHidePwa: (String) -> Unit = {},\n    hiddenAppKeys: Set<AppKey> = emptySet(),\n    onRemovePwa: (AppShortcutItem) -> Unit = {},\n    uiState: InvokeUiState,\n",
        "InvokeSheetContent 参数列表"
    )

    # 2. 过滤普通 App
    t = sub_once(
        t,
        "                            val sectionApps = uiState.sections.find { it.letter == letter }?.apps ?: emptyList()\n",
        "                            val sectionApps = (uiState.sections.find { it.letter == letter }?.apps ?: emptyList())\n                                .filter { it.key !in hiddenAppKeys }\n",
        "在 All 列表中过滤 hiddenAppKeys"
    )

    # 3. 替换弹窗
    old_dialog = """    shortcutToRemove?.let { sc ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { shortcutToRemove = null },
            title = { Text("Remove Shortcut") },
            text = { Text("Are you sure you want to remove \\"${sc.label}\\"?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onHidePwa(sc.id) // 👈 调用回调隐藏，不再走系统 API
                        shortcutToRemove = null
                        pwaRefreshKey++ // 触发 UI 刷新
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { shortcutToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }"""
    new_dialog = """    shortcutToRemove?.let { sc ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { shortcutToRemove = null },
            title = { Text("Shortcut Options") },
            text = { Text("What would you like to do with \\"${sc.label}\\"?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onRemovePwa(sc)
                        shortcutToRemove = null
                        pwaRefreshKey++
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onHidePwa(sc.id)
                        shortcutToRemove = null
                        pwaRefreshKey++
                    }
                ) {
                    Text("Hide")
                }
            }
        )
    }"""
    t = sub_once(t, old_dialog, new_dialog, "替换弹窗为 Hide 和 Remove 双选项")
    p.write_text(t, encoding="utf-8")

def patch_home_canvas():
    print("正在修改 HomeCanvas.kt ...")
    p = BASE / "ui/home/HomeCanvas.kt"
    t = p.read_text(encoding="utf-8")

    # 1. HomeCanvas 增加参数
    t = sub_once(
        t,
        "    homePressTick: Int = 0,\n    onHidePwa: (String) -> Unit = {},\n",
        "    homePressTick: Int = 0,\n    onHidePwa: (String) -> Unit = {},\n    onHideApp: (AppKey) -> Unit = {},\n    onRemovePwa: (AppShortcutItem) -> Unit = {},\n",
        "HomeCanvas 参数列表"
    )

    # 2. 传给 InvokeSheet
    t = sub_once(
        t,
        "            hiddenPwaIds = settings.hiddenPwaIds,\n            onHidePwa = onHidePwa,\n",
        "            hiddenPwaIds = settings.hiddenPwaIds,\n            onHidePwa = onHidePwa,\n            hiddenAppKeys = settings.hiddenAppKeys,\n            onRemovePwa = onRemovePwa,\n",
        "调用 InvokeSheet 时传参"
    )

    # 3. 传给 AppActionSheet
    t = sub_once(
        t,
        "            onUninstall = {\n                closeInvokeSheet()\n                appCatalog?.uninstallApp(targetApp)\n            },\n        )\n",
        "            onUninstall = {\n                closeInvokeSheet()\n                appCatalog?.uninstallApp(targetApp)\n            },\n            onHideApp = onHideApp,\n        )\n",
        "调用 AppActionSheet 时传入 onHideApp"
    )
    p.write_text(t, encoding="utf-8")

if __name__ == "__main__":
    print("🚀 开始自动化修复 ...")
    patch_launcher_settings()
    patch_app_catalog()
    patch_app_action_sheet()
    patch_invoke_sheet()
    patch_home_canvas()
    print("✅ 所有源码修改完成！请直接编译运行。")
