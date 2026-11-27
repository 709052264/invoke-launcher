package org.invokevirtual.launcher.system

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.LruCache
import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppProfile {
    PERSONAL,
    WORK,
    PRIVATE,
}

@Immutable
data class AppKey(
    val packageName: String,
    val className: String,
    val userSerial: Long,
)

@Immutable
data class InstalledApp(
    val key: AppKey,
    val label: String,
    val componentName: ComponentName,
    val userHandle: UserHandle,
    val profile: AppProfile = AppProfile.PERSONAL,
) {
    val isWorkProfile: Boolean get() = profile == AppProfile.WORK
    val isPrivateSpace: Boolean get() = profile == AppProfile.PRIVATE
}

@Immutable
data class AppIconAsset(
    val fullColor: ImageBitmap,
    val monochrome: ImageBitmap?,
)

@Immutable
data class AppShortcutItem(
    val id: String,
    val label: String,
    val appLabel: String,
    val shortcutInfo: ShortcutInfo,
    val icon: ImageBitmap?,
    val userSerial: Long = 0L,
) {
    val shortcutId: String get() = shortcutInfo.id
    val packageName: String get() = shortcutInfo.`package`
}

@Immutable
sealed interface PrivateSpaceState {
    data object Unavailable : PrivateSpaceState

    data class Locked(val userHandle: UserHandle) : PrivateSpaceState

    data class Unlocked(
        val userHandle: UserHandle,
        val apps: List<InstalledApp>,
    ) : PrivateSpaceState
}

/**
 * Process-scoped repository bridging Android's [LauncherApps] + [UserManager] into reactive
 * [StateFlow]s for Personal, Work, and Android 15–17 Private Space profiles, with off-main-thread
 * icon decoding, live shortcut search, and bounded LRU caching.
 */
class AppCatalog(private val context: Context) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Personal + Work profile apps (never includes locked Private Space apps). */
    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()

    private val _privateSpace = MutableStateFlow<PrivateSpaceState>(PrivateSpaceState.Unavailable)
    val privateSpace: StateFlow<PrivateSpaceState> = _privateSpace.asStateFlow()

    // Bounded to ~96 decoded icons to stay well within Android 17's MemoryLimiter.
    private val iconCache = object : LruCache<AppKey, AppIconAsset>(96) {}

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) {
            evictPackage(packageName)
            refresh()
        }

        override fun onPackagesAvailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()

        override fun onPackagesUnavailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()

        override fun onPackagesSuspended(packageNames: Array<out String>, user: UserHandle) = refresh()
        override fun onPackagesUnsuspended(packageNames: Array<out String>, user: UserHandle) = refresh()
    }

    private val profileReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            refresh()
        }
    }

    init {
        launcherApps?.registerCallback(callback)
        registerProfileReceivers()
        refresh()
    }

    fun refresh() {
        scope.launch {
            val (publicApps, privateState) = loadCatalogSnapshot()
            _apps.value = publicApps
            _privateSpace.value = privateState
        }
    }

    fun hasShortcutPermission(): Boolean =
        runCatching { launcherApps?.hasShortcutHostPermission() == true }.getOrDefault(false)

    /**
     * Locks or unlocks Android 15–17 Private Space via [UserManager.requestQuietModeEnabled].
     * Unlocking triggers the system Private Space biometric/PIN prompt automatically.
     */
    fun setPrivateSpaceLocked(locked: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val handle = when (val state = _privateSpace.value) {
            is PrivateSpaceState.Locked -> state.userHandle
            is PrivateSpaceState.Unlocked -> state.userHandle
            PrivateSpaceState.Unavailable -> return
        }
        runCatching {
            userManager?.requestQuietModeEnabled(locked, handle)
        }
        refresh()
    }

    suspend fun loadIcon(app: InstalledApp): AppIconAsset? {
        iconCache.get(app.key)?.let { return it }
        return withContext(Dispatchers.Default) {
            iconCache.get(app.key)?.let { return@withContext it }
            val info = launcherApps
                ?.getActivityList(app.componentName.packageName, app.userHandle)
                ?.firstOrNull { it.componentName == app.componentName }
                ?: return@withContext null

            val density = context.resources.displayMetrics.densityDpi
            val drawable = runCatching { info.getBadgedIcon(density) }.getOrNull()
                ?: return@withContext null

            val iconSizePx =
                (72 * context.resources.displayMetrics.density).toInt().coerceIn(144, 288)
            val fullColor = drawable.toImageBitmap(iconSizePx)
            val monochrome = extractMonochromeBitmap(drawable, iconSizePx)

            AppIconAsset(fullColor = fullColor, monochrome = monochrome).also {
                iconCache.put(app.key, it)
            }
        }
    }

    suspend fun loadShortcuts(app: InstalledApp, limit: Int = 4): List<AppShortcutItem> {
        val service = launcherApps ?: return emptyList()
        if (!hasShortcutPermission()) return emptyList()

        return withContext(Dispatchers.Default) {
            val serial = userManager?.getSerialNumberForUser(app.userHandle) ?: app.key.userSerial
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(app.componentName.packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            }
            val shortcuts = runCatching { service.getShortcuts(query, app.userHandle) }
                .getOrNull()
                .orEmpty()
                .filter { it.isEnabled }
                .sortedWith(compareBy<ShortcutInfo>({ !it.isDeclaredInManifest }, { it.rank }))
                .take(limit)

            val density = context.resources.displayMetrics.densityDpi
            val iconSizePx =
                (64 * context.resources.displayMetrics.density).toInt().coerceIn(128, 256)

            shortcuts.map { info ->
                val icon = runCatching {
                    service.getShortcutIconDrawable(info, density)?.toImageBitmap(iconSizePx)
                }.getOrNull()
                val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                    ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                    ?: info.id
                AppShortcutItem(
                    id = info.id,
                    label = label,
                    appLabel = app.label,
                    shortcutInfo = info,
                    icon = icon,
                    userSerial = serial,
                )
            }
        }
    }

    /**
     * Loads all enabled app shortcuts across Personal & Work profiles so users can pin any
     * shortcut directly onto the Living Home Canvas.
     */
        // 👇 这是你删掉的旧方法，必须补回来，供 AddCanvasAppSheet 使用
    suspend fun loadAllShortcuts(limit: Int = 80): List<AppShortcutItem> {
        val service = launcherApps ?: return emptyList()
        val um = userManager ?: return emptyList()
        if (!hasShortcutPermission()) return emptyList()

        return withContext(Dispatchers.Default) {
            val appLabelsByPackage = _apps.value.associateBy(
                keySelector = { it.componentName.packageName },
                valueTransform = { it.label },
            )
            val density = context.resources.displayMetrics.densityDpi
            val iconSizePx = (44 * context.resources.displayMetrics.density).toInt().coerceIn(72, 160)
            val collected = ArrayList<AppShortcutItem>(limit.coerceAtMost(64))

            for (profile in um.userProfiles) {
                if (classifyProfile(profile) == AppProfile.PRIVATE) continue
                val serial = um.getSerialNumberForUser(profile)
                val query = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                    )
                }
                val shortcuts = runCatching { service.getShortcuts(query, profile) }
                    .getOrNull()
                    .orEmpty()
                    .filter { it.isEnabled }
                    .sortedWith(
                        compareBy<ShortcutInfo>(
                            { (appLabelsByPackage[it.`package`] ?: it.`package`).lowercase() },
                            { !it.isDeclaredInManifest },
                            { it.rank },
                        ),
                    )

                for (info in shortcuts) {
                    val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: continue
                    val icon = runCatching {
                        service.getShortcutIconDrawable(info, density)?.toImageBitmap(iconSizePx)
                    }.getOrNull()
                    collected += AppShortcutItem(
                        id = "${info.`package`}/${info.id}/$serial",
                        label = label,
                        appLabel = appLabelsByPackage[info.`package`] ?: info.`package`,
                        shortcutInfo = info,
                        icon = icon,
                        userSerial = serial,
                    )
                    if (collected.size >= limit) return@withContext collected
                }
            }
            collected
        }
    }

    // 👇 这是我们刚刚新增的方法，仅加载 PWA（固定/网页）快捷方式，供 InvokeSheet 使用
    suspend fun loadPwaShortcuts(): List<AppShortcutItem> {
        val service = launcherApps ?: return emptyList()
        val um = userManager ?: return emptyList()
        if (!hasShortcutPermission()) return emptyList()

        return withContext(Dispatchers.Default) {
            val density = context.resources.displayMetrics.densityDpi
            val iconSizePx = (44 * context.resources.displayMetrics.density).toInt().coerceIn(72, 160)
            val collected = mutableListOf<AppShortcutItem>()

            for (profile in um.userProfiles) {
                if (classifyProfile(profile) == AppProfile.PRIVATE) continue
                val serial = um.getSerialNumberForUser(profile)
                val query = LauncherApps.ShortcutQuery().apply {
                    // 仅查询固定快捷方式
                    setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                }
                val shortcuts = runCatching { service.getShortcuts(query, profile) }
                    .getOrNull()
                    .orEmpty()
                    .filter { it.isEnabled }

                for (info in shortcuts) {
                    val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: continue
                    val icon = runCatching {
                        service.getShortcutIconDrawable(info, density)?.toImageBitmap(iconSizePx)
                    }.getOrNull()
                    collected += AppShortcutItem(
                        id = "${info.`package`}/${info.id}/$serial",
                        label = label,
                        appLabel = _apps.value.firstOrNull { it.componentName.packageName == info.`package` }?.label 
                            ?: "Web App",
                        shortcutInfo = info,
                        icon = icon,
                        userSerial = serial,
                    )
                }
            }
            collected.sortedBy { it.label.lowercase() }
        }
    }

    /** Resolves a pinned/saved shortcut for live icon & label rendering on the Home Canvas. */
    suspend fun resolveShortcut(
        packageName: String,
        shortcutId: String,
        userSerial: Long,
    ): AppShortcutItem? {
        val service = launcherApps ?: return null
        val um = userManager ?: return null
        if (!hasShortcutPermission()) return null

        return withContext(Dispatchers.Default) {
            val handle = um.getUserForSerialNumber(userSerial) ?: Process.myUserHandle()
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setShortcutIds(listOf(shortcutId))
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            }
            val info = runCatching { service.getShortcuts(query, handle) }
                .getOrNull()
                ?.firstOrNull()
                ?: return@withContext null

            val density = context.resources.displayMetrics.densityDpi
            val iconSizePx = (48 * context.resources.displayMetrics.density).toInt().coerceIn(96, 192)
            val icon = runCatching {
                service.getShortcutIconDrawable(info, density)?.toImageBitmap(iconSizePx)
            }.getOrNull()
            val appLabel = _apps.value.firstOrNull {
                it.componentName.packageName == packageName && it.key.userSerial == userSerial
            }?.label ?: packageName
            val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                ?: shortcutId
            AppShortcutItem(
                id = "$packageName/$shortcutId/$userSerial",
                label = label,
                appLabel = appLabel,
                shortcutInfo = info,
                icon = icon,
                userSerial = userSerial,
            )
        }
    }

    fun getUserSerial(userHandle: UserHandle): Long =
        userManager?.getSerialNumberForUser(userHandle) ?: 0L

    /** Keeps Android's system pinned-shortcut registry synchronized with pinned Home Canvas tiles. */
    fun syncPinnedShortcuts(
        packageName: String,
        shortcutIds: List<String>,
        userSerial: Long,
    ) {
        val service = launcherApps ?: return
        if (!hasShortcutPermission()) return
        val handle = userManager?.getUserForSerialNumber(userSerial) ?: Process.myUserHandle()
        runCatching {
            service.pinShortcuts(packageName, shortcutIds.distinct(), handle)
        }
    }

    /** 解除固定快捷方式（将其从抽屉移除） */
    fun unpinShortcuts(packageName: String, shortcutIds: List<String>, userSerial: Long) {
        val service = launcherApps ?: return
        if (!hasShortcutPermission()) return
        val handle = userManager?.getUserForSerialNumber(userSerial) ?: Process.myUserHandle()
        runCatching {
            service.unpinShortcuts(packageName, shortcutIds, handle)
        }
    }

    /**
     * Searches across Personal/Work app shortcuts for verb-first invocation (e.g., "new note",
     * "alarm", "selfie"). Private Space profiles are strictly excluded from global shortcut search.
     */
    suspend fun searchShortcuts(query: String, limit: Int = 5): List<AppShortcutItem> {
        val trimmed = query.trim()
        if (trimmed.length < 2 || !hasShortcutPermission()) return emptyList()
        val service = launcherApps ?: return emptyList()
        val um = userManager ?: return emptyList()

        return withContext(Dispatchers.Default) {
            val appLabelsByPackage = _apps.value.associate { it.componentName.packageName to it.label }
            val matches = ArrayList<AppShortcutItem>(limit)
            val density = context.resources.displayMetrics.densityDpi
            val iconSizePx = (32 * context.resources.displayMetrics.density).toInt().coerceIn(64, 128)

            for (profile in um.userProfiles) {
                if (classifyProfile(profile) == AppProfile.PRIVATE) continue
                val serial = um.getSerialNumberForUser(profile)
                val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                    )
                }
                val allShortcuts = runCatching { service.getShortcuts(shortcutQuery, profile) }
                    .getOrNull()
                    .orEmpty()

                for (info in allShortcuts) {
                    if (!info.isEnabled) continue
                    val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: continue
                    if (!label.contains(trimmed, ignoreCase = true)) continue

                    val icon = runCatching {
                        service.getShortcutIconDrawable(info, density)?.toImageBitmap(iconSizePx)
                    }.getOrNull()
                    matches += AppShortcutItem(
                        id = "${info.`package`}/${info.id}/$serial",
                        label = label,
                        appLabel = appLabelsByPackage[info.`package`] ?: info.`package`,
                        shortcutInfo = info,
                        icon = icon,
                        userSerial = serial,
                    )
                    if (matches.size >= limit) return@withContext matches
                }
            }
            matches
        }
    }

    fun startShortcut(
        shortcut: AppShortcutItem,
        sourceBounds: Rect? = null,
        sourceView: View? = null,
    ) {
        val (screenBounds, opts) = buildLaunchClipReveal(sourceBounds, sourceView)
        runCatching {
            launcherApps?.startShortcut(shortcut.shortcutInfo, screenBounds, opts)
        }.onFailure {
            runCatching {
                launcherApps?.startShortcut(shortcut.shortcutInfo, screenBounds, null)
            }
        }
    }

    fun startShortcutById(
        packageName: String,
        shortcutId: String,
        userSerial: Long,
        sourceBounds: Rect? = null,
        sourceView: View? = null,
    ) {
        val handle = userManager?.getUserForSerialNumber(userSerial) ?: Process.myUserHandle()
        val (screenBounds, opts) = buildLaunchClipReveal(sourceBounds, sourceView)
        runCatching {
            launcherApps?.startShortcut(packageName, shortcutId, screenBounds, opts, handle)
        }.onFailure {
            runCatching {
                launcherApps?.startShortcut(packageName, shortcutId, screenBounds, null, handle)
            }
        }
    }

    fun launch(
        app: InstalledApp,
        sourceBounds: Rect? = null,
        sourceView: View? = null,
    ) {
        val (screenBounds, opts) = buildLaunchClipReveal(sourceBounds, sourceView)
        runCatching {
            launcherApps?.startMainActivity(app.componentName, app.userHandle, screenBounds, opts)
        }.onFailure {
            runCatching {
                launcherApps?.startMainActivity(app.componentName, app.userHandle, screenBounds, null)
            }
        }
    }

    private fun buildLaunchClipReveal(
        sourceBounds: Rect?,
        sourceView: View?,
    ): Pair<Rect?, android.os.Bundle?> {
        val validBounds = sourceBounds?.takeIf { !it.isEmpty }
        val attachedView = sourceView?.takeIf { it.isAttachedToWindow }
        val screenBounds = if (validBounds != null && attachedView != null) {
            val windowOffset = IntArray(2)
            attachedView.getLocationOnScreen(windowOffset)
            Rect(validBounds).apply { offset(windowOffset[0], windowOffset[1]) }
        } else {
            validBounds
        }
        val opts = if (validBounds != null && attachedView != null) {
            runCatching {
                ActivityOptions.makeClipRevealAnimation(
                    attachedView,
                    validBounds.left,
                    validBounds.top,
                    validBounds.width(),
                    validBounds.height(),
                ).toBundle()
            }.getOrNull()
        } else {
            null
        }
        return screenBounds to opts
    }

    fun launchInFloatingWindow(app: InstalledApp) {
        val dm = context.resources.displayMetrics
        val marginX = (dm.widthPixels * 0.08f).toInt()
        val marginY = (dm.heightPixels * 0.14f).toInt()
        val floatingBounds = Rect(
            marginX,
            marginY,
            dm.widthPixels - marginX,
            dm.heightPixels - marginY,
        )

        val launchedFreeform = runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = app.componentName
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
                )
            }
            val options = ActivityOptions.makeBasic().setLaunchBounds(floatingBounds)
            context.startActivity(intent, options.toBundle())
            true
        }.getOrDefault(false)

        if (!launchedFreeform) {
            launch(app)
        }
    }

    fun openAppInfo(app: InstalledApp, sourceBounds: Rect? = null) {
        runCatching {
            launcherApps?.startAppDetailsActivity(app.componentName, app.userHandle, sourceBounds, null)
        }
    }

    fun canUninstall(app: InstalledApp): Boolean {
        val appInfo = runCatching {
            launcherApps?.getApplicationInfo(app.componentName.packageName, 0, app.userHandle)
        }.getOrNull() ?: return false
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return !isSystem || isUpdatedSystem
    }

    fun uninstallApp(app: InstalledApp) {
        runCatching {
            val intent = Intent(
                Intent.ACTION_DELETE,
                Uri.fromParts("package", app.componentName.packageName, null),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Intent.EXTRA_USER, app.userHandle)
            }
            context.startActivity(intent)
        }
    }

    private fun registerProfileReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                addAction(Intent.ACTION_PROFILE_ACCESSIBLE)
                addAction(Intent.ACTION_PROFILE_INACCESSIBLE)
            }
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(profileReceiver, filter)
            }
        }
    }

    private fun classifyProfile(profile: UserHandle): AppProfile {
        if (profile == Process.myUserHandle()) return AppProfile.PERSONAL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val userInfo = runCatching { launcherApps?.getLauncherUserInfo(profile) }.getOrNull()
            if (userInfo?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE) {
                return AppProfile.PRIVATE
            }
        }
        return AppProfile.WORK
    }

    private fun loadCatalogSnapshot(): Pair<List<InstalledApp>, PrivateSpaceState> {
        val service = launcherApps ?: return emptyList<InstalledApp>() to PrivateSpaceState.Unavailable
        val um = userManager ?: return emptyList<InstalledApp>() to PrivateSpaceState.Unavailable
        val myPackage = context.packageName

        val publicApps = ArrayList<InstalledApp>(64)
        var privateState: PrivateSpaceState = PrivateSpaceState.Unavailable

        for (profile in um.userProfiles) {
            val serial = um.getSerialNumberForUser(profile)
            val appProfile = classifyProfile(profile)

            if (appProfile == AppProfile.PRIVATE) {
                val locked = um.isQuietModeEnabled(profile)
                if (locked) {
                    privateState = PrivateSpaceState.Locked(profile)
                } else {
                    val privateActivities =
                        runCatching { service.getActivityList(null, profile) }.getOrDefault(emptyList())
                    val privateApps = privateActivities
                        .filter { it.componentName.packageName != myPackage }
                        .map { info -> info.toInstalledApp(serial, profile, AppProfile.PRIVATE) }
                        .sortedBy { it.label.lowercase() }
                    privateState = PrivateSpaceState.Unlocked(profile, privateApps)
                }
                continue
            }

            val activities: List<LauncherActivityInfo> =
                runCatching { service.getActivityList(null, profile) }.getOrDefault(emptyList())
            for (info in activities) {
                if (info.componentName.packageName == myPackage) continue
                publicApps += info.toInstalledApp(serial, profile, appProfile)
            }
        }
        return publicApps.sortedBy { it.label.lowercase() } to privateState
    }

    private fun LauncherActivityInfo.toInstalledApp(
        serial: Long,
        profile: UserHandle,
        appProfile: AppProfile,
    ): InstalledApp {
        val key = AppKey(
            packageName = componentName.packageName,
            className = componentName.className,
            userSerial = serial,
        )
        return InstalledApp(
            key = key,
            label = label?.toString().orEmpty().ifBlank { componentName.packageName },
            componentName = componentName,
            userHandle = profile,
            profile = appProfile,
        )
    }

    private fun evictPackage(packageName: String) {
        val snapshot = iconCache.snapshot()
        for (key in snapshot.keys) {
            if (key.packageName == packageName) {
                iconCache.remove(key)
            }
        }
    }

    private fun extractMonochromeBitmap(drawable: Drawable, sizePx: Int): ImageBitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        val adaptive = drawable as? AdaptiveIconDrawable ?: return null
        val mono = adaptive.monochrome ?: return null
        val bitmap = createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val inset = (-sizePx * 0.2f).toInt()
        mono.setBounds(inset, inset, sizePx - inset, sizePx - inset)
        mono.draw(canvas)
        return bitmap.asImageBitmap()
    }

    private fun Drawable.toImageBitmap(sizePx: Int): ImageBitmap {
        val bitmap = createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, sizePx, sizePx)
        draw(canvas)
        return bitmap.asImageBitmap()
    }
}
