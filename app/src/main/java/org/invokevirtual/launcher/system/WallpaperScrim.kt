package org.invokevirtual.launcher.system

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

@Immutable
data class WallpaperLegibility(
    val supportsDarkText: Boolean = false,
    val prefersDarkTheme: Boolean = true,
)

/**
 * Observes [WallpaperManager.getWallpaperColors] so the Home Canvas can automatically calibrate
 * status/navigation bar legibility and its protective wallpaper gradient scrim (>= 4.5:1 contrast).
 */
@Composable
fun rememberWallpaperLegibility(): WallpaperLegibility {
    val inPreview = LocalInspectionMode.current
    if (inPreview) return remember { WallpaperLegibility(supportsDarkText = false, prefersDarkTheme = true) }

    val context = LocalContext.current
    val wallpaperManager = remember(context) { WallpaperManager.getInstance(context) }
    var legibility by remember {
        mutableStateOf(
            readLegibility(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    runCatching { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) }.getOrNull()
                } else {
                    null
                },
            ),
        )
    }

    DisposableEffect(wallpaperManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
                if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                    legibility = readLegibility(colors)
                }
            }
            val handler = Handler(Looper.getMainLooper())
            runCatching { wallpaperManager.addOnColorsChangedListener(listener, handler) }
            onDispose {
                runCatching { wallpaperManager.removeOnColorsChangedListener(listener) }
            }
        } else {
            onDispose {}
        }
    }

    return legibility
}

private fun readLegibility(colors: WallpaperColors?): WallpaperLegibility {
    if (colors == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return WallpaperLegibility(supportsDarkText = false, prefersDarkTheme = true)
    }
    val hints = colors.colorHints
    val darkText = (hints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0
    val darkTheme = (hints and WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0
    return WallpaperLegibility(supportsDarkText = darkText, prefersDarkTheme = darkTheme)
}
