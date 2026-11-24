package org.invokevirtual.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import org.invokevirtual.launcher.data.LauncherFontPreset

fun googleFontFamily(
    googleFontName: String,
    inPreview: Boolean = false,
    isSerifFallback: Boolean = false,
): FontFamily {
    if (inPreview) return if (isSerifFallback) FontFamily.Serif else FontFamily.SansSerif
    val googleFont = GoogleFont(googleFontName)
    return FontFamily(
        listOf(
            FontWeight.Light,
            FontWeight.Normal,
            FontWeight.Medium,
            FontWeight.SemiBold,
            FontWeight.Bold,
            FontWeight.Black,
        ).map { weight ->
            Font(googleFont = googleFont, weight = weight)
        },
    )
}

/**
 * Pairs distinct typefaces across display, headline, and body roles on top of Material 3's default
 * [Typography] scale rather than recreating every token manually.
 */
fun invokeTypography(
    preset: LauncherFontPreset = LauncherFontPreset.GOOGLE_SANS_FLEX,
    inPreview: Boolean = false,
): Typography {
    val displayFamily = googleFontFamily(preset.clockFontName, inPreview, preset.clockIsSerif)
    val headlineFamily = googleFontFamily(preset.headlineFontName, inPreview)
    val bodyFamily = googleFontFamily(preset.bodyFontName, inPreview)

    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = displayFamily),
        displayMedium = base.displayMedium.copy(fontFamily = displayFamily),
        displaySmall = base.displaySmall.copy(fontFamily = displayFamily),
        displayLargeEmphasized = base.displayLargeEmphasized.copy(fontFamily = displayFamily),
        displayMediumEmphasized = base.displayMediumEmphasized.copy(fontFamily = displayFamily),
        displaySmallEmphasized = base.displaySmallEmphasized.copy(fontFamily = displayFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = headlineFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = headlineFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = headlineFamily),
        titleLarge = base.titleLarge.copy(fontFamily = headlineFamily),
        headlineLargeEmphasized = base.headlineLargeEmphasized.copy(fontFamily = headlineFamily),
        headlineMediumEmphasized = base.headlineMediumEmphasized.copy(fontFamily = headlineFamily),
        headlineSmallEmphasized = base.headlineSmallEmphasized.copy(fontFamily = headlineFamily),
        titleLargeEmphasized = base.titleLargeEmphasized.copy(fontFamily = headlineFamily),
        titleMedium = base.titleMedium.copy(fontFamily = bodyFamily),
        titleSmall = base.titleSmall.copy(fontFamily = bodyFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = bodyFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = bodyFamily),
        bodySmall = base.bodySmall.copy(fontFamily = bodyFamily),
        labelLarge = base.labelLarge.copy(fontFamily = bodyFamily),
        labelMedium = base.labelMedium.copy(fontFamily = bodyFamily),
        labelSmall = base.labelSmall.copy(fontFamily = bodyFamily),
        titleMediumEmphasized = base.titleMediumEmphasized.copy(fontFamily = bodyFamily),
        titleSmallEmphasized = base.titleSmallEmphasized.copy(fontFamily = bodyFamily),
        bodyLargeEmphasized = base.bodyLargeEmphasized.copy(fontFamily = bodyFamily),
        bodyMediumEmphasized = base.bodyMediumEmphasized.copy(fontFamily = bodyFamily),
        bodySmallEmphasized = base.bodySmallEmphasized.copy(fontFamily = bodyFamily),
        labelLargeEmphasized = base.labelLargeEmphasized.copy(fontFamily = bodyFamily),
        labelMediumEmphasized = base.labelMediumEmphasized.copy(fontFamily = bodyFamily),
        labelSmallEmphasized = base.labelSmallEmphasized.copy(fontFamily = bodyFamily),
    )
}
