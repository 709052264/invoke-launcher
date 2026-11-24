package org.invokevirtual.launcher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.invokevirtual.launcher.data.ExpressivePalette
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Curated Material 3 Expressive fallback palettes for devices below API 31 or when the user
 * disables wallpaper dynamic color. Inspired by samuelbeckman/expressive-clock.
 */
fun ExpressivePalette.colorScheme(dark: Boolean): ColorScheme = when (this) {
    ExpressivePalette.TANGERINE -> if (dark) TangerineDark else TangerineLight
    ExpressivePalette.SAGE -> if (dark) SageDark else SageLight
    ExpressivePalette.COBALT -> if (dark) CobaltDark else CobaltLight
    ExpressivePalette.PLUM -> if (dark) PlumDark else PlumLight
}

fun ExpressivePalette.swatchColor(): Color = when (this) {
    ExpressivePalette.TANGERINE -> Color(0xFFE87042)
    ExpressivePalette.SAGE -> Color(0xFF5FA25A)
    ExpressivePalette.COBALT -> Color(0xFF537EE0)
    ExpressivePalette.PLUM -> Color(0xFFB368D4)
}

/**
 * Generates an accessible Material 3 Expressive tonal [ColorScheme] from any arbitrary ARGB seed
 * pixel picked via Android 17's EyeDropper API (`Intent.ACTION_OPEN_EYE_DROPPER`).
 * Adjusts perceptual lightness per hue so WCAG AA contrast (>= 4.5:1) holds even for high-luminance
 * hues like yellow, lime, or cyan.
 */
fun seedColorScheme(seedArgb: Int, dark: Boolean): ColorScheme {
    val (rawHue, rawSat, _) = argbToHsl(seedArgb)
    val primaryHue = rawHue
    val primarySat = rawSat.coerceIn(0.32f, 0.78f)
    val secondaryHue = (rawHue + 22f) % 360f
    val secondarySat = (primarySat * 0.42f).coerceIn(0.14f, 0.36f)
    val tertiaryHue = (rawHue + 58f) % 360f
    val tertiarySat = (primarySat * 0.72f).coerceIn(0.26f, 0.65f)
    val neutralSat = (primarySat * 0.14f).coerceIn(0.05f, 0.14f)

    return if (dark) {
        val onPrimary = hslColor(primaryHue, primarySat, 0.14f)
        val primary = hslWithMinContrast(primaryHue, primarySat, 0.78f, against = onPrimary, lighter = true, minRatio = 5.5f)
        val primaryContainer = hslColor(primaryHue, primarySat * 0.85f, 0.25f)
        val onPrimaryContainer = hslWithMinContrast(primaryHue, primarySat * 0.90f, 0.92f, against = primaryContainer, lighter = true, minRatio = 6.5f)

        val onSecondary = hslColor(secondaryHue, secondarySat, 0.14f)
        val secondary = hslWithMinContrast(secondaryHue, secondarySat, 0.78f, against = onSecondary, lighter = true, minRatio = 5.5f)
        val secondaryContainer = hslColor(secondaryHue, secondarySat, 0.26f)
        val onSecondaryContainer = hslWithMinContrast(secondaryHue, secondarySat, 0.92f, against = secondaryContainer, lighter = true, minRatio = 6.5f)

        val onTertiary = hslColor(tertiaryHue, tertiarySat, 0.14f)
        val tertiary = hslWithMinContrast(tertiaryHue, tertiarySat, 0.78f, against = onTertiary, lighter = true, minRatio = 5.5f)
        val tertiaryContainer = hslColor(tertiaryHue, tertiarySat * 0.80f, 0.25f)
        val onTertiaryContainer = hslWithMinContrast(tertiaryHue, tertiarySat * 0.85f, 0.92f, against = tertiaryContainer, lighter = true, minRatio = 6.5f)

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = hslColor(primaryHue, neutralSat, 0.08f),
            onBackground = hslColor(primaryHue, neutralSat, 0.91f),
            surface = hslColor(primaryHue, neutralSat, 0.08f),
            onSurface = hslColor(primaryHue, neutralSat, 0.91f),
            surfaceVariant = hslColor(primaryHue, neutralSat * 1.4f, 0.27f),
            onSurfaceVariant = hslColor(primaryHue, neutralSat * 1.2f, 0.78f),
            surfaceContainerLowest = hslColor(primaryHue, neutralSat, 0.05f),
            surfaceContainerLow = hslColor(primaryHue, neutralSat, 0.11f),
            surfaceContainer = hslColor(primaryHue, neutralSat, 0.13f),
            surfaceContainerHigh = hslColor(primaryHue, neutralSat, 0.17f),
            surfaceContainerHighest = hslColor(primaryHue, neutralSat, 0.22f),
            outline = hslColor(primaryHue, neutralSat, 0.56f),
            outlineVariant = hslColor(primaryHue, neutralSat, 0.28f),
        )
    } else {
        val primary = hslWithMinContrast(primaryHue, primarySat, 0.34f, against = Color.White, lighter = false, minRatio = 5.0f)
        val primaryContainer = hslColor(primaryHue, primarySat * 0.85f, 0.89f)
        val onPrimaryContainer = hslWithMinContrast(primaryHue, primarySat, 0.15f, against = primaryContainer, lighter = false, minRatio = 6.5f)

        val secondary = hslWithMinContrast(secondaryHue, secondarySat, 0.36f, against = Color.White, lighter = false, minRatio = 5.0f)
        val secondaryContainer = hslColor(secondaryHue, secondarySat, 0.89f)
        val onSecondaryContainer = hslWithMinContrast(secondaryHue, secondarySat, 0.15f, against = secondaryContainer, lighter = false, minRatio = 6.5f)

        val tertiary = hslWithMinContrast(tertiaryHue, tertiarySat, 0.34f, against = Color.White, lighter = false, minRatio = 5.0f)
        val tertiaryContainer = hslColor(tertiaryHue, tertiarySat * 0.80f, 0.89f)
        val onTertiaryContainer = hslWithMinContrast(tertiaryHue, tertiarySat, 0.15f, against = tertiaryContainer, lighter = false, minRatio = 6.5f)

        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = Color.White,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = hslColor(primaryHue, neutralSat, 0.98f),
            onBackground = hslColor(primaryHue, neutralSat, 0.12f),
            surface = hslColor(primaryHue, neutralSat, 0.98f),
            onSurface = hslColor(primaryHue, neutralSat, 0.12f),
            surfaceVariant = hslColor(primaryHue, neutralSat * 1.3f, 0.90f),
            onSurfaceVariant = hslColor(primaryHue, neutralSat * 1.1f, 0.30f),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = hslColor(primaryHue, neutralSat, 0.95f),
            surfaceContainer = hslColor(primaryHue, neutralSat, 0.93f),
            surfaceContainerHigh = hslColor(primaryHue, neutralSat, 0.90f),
            surfaceContainerHighest = hslColor(primaryHue, neutralSat, 0.87f),
            outline = hslColor(primaryHue, neutralSat, 0.46f),
            outlineVariant = hslColor(primaryHue, neutralSat, 0.78f),
        )
    }
}

private fun hslWithMinContrast(
    hue: Float,
    sat: Float,
    startLight: Float,
    against: Color,
    lighter: Boolean,
    minRatio: Float,
): Color {
    val initial = hslColor(hue, sat, startLight)
    if (contrastRatio(initial, against) >= minRatio) return initial

    var low = if (lighter) startLight else 0.04f
    var high = if (lighter) 0.98f else startLight
    var best = if (lighter) hslColor(hue, sat, 0.98f) else hslColor(hue, sat, 0.04f)
    repeat(10) {
        val mid = (low + high) / 2f
        val candidate = hslColor(hue, sat, mid)
        if (contrastRatio(candidate, against) >= minRatio) {
            best = candidate
            if (lighter) high = mid else low = mid
        } else {
            if (lighter) low = mid else high = mid
        }
    }
    return best
}

private fun contrastRatio(a: Color, b: Color): Float {
    val l1 = a.luminance()
    val l2 = b.luminance()
    return (max(l1, l2) + 0.05f) / (min(l1, l2) + 0.05f)
}

private fun argbToHsl(argb: Int): Triple<Float, Float, Float> {
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val delta = maxC - minC
    val l = (maxC + minC) / 2f
    if (delta == 0f) return Triple(24f, 0f, l)

    val s = delta / (1f - abs(2f * l - 1f))
    val h = when (maxC) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    return Triple((h + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

private fun hslColor(hue: Float, sat: Float, light: Float): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = sat.coerceIn(0f, 1f)
    val l = light.coerceIn(0f, 1f)
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

/**
 * True-black variant for OLED screens and Night OLED workspace mode.
 * Keeps accent and upper container roles intact so Bento tiles remain clearly separated
 * against a pure black canvas.
 */
fun ColorScheme.toAmoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0B0B0D),
    surfaceContainer = Color(0xFF141418),
    surfaceContainerHigh = Color(0xFF1D1D22),
    surfaceContainerHighest = Color(0xFF26262C),
    scrim = Color.Black,
)

private val TangerineDark = darkColorScheme(
    primary = Color(0xFFFFB599),
    onPrimary = Color(0xFF5A1D00),
    primaryContainer = Color(0xFF7D2E0C),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = Color(0xFFE7BDB0),
    onSecondary = Color(0xFF442A21),
    secondaryContainer = Color(0xFF5D4036),
    onSecondaryContainer = Color(0xFFFFDBCE),
    tertiary = Color(0xFFD8C68D),
    onTertiary = Color(0xFF3B3005),
    tertiaryContainer = Color(0xFF534619),
    onTertiaryContainer = Color(0xFFF5E2A7),
    background = Color(0xFF181210),
    onBackground = Color(0xFFEDE0DC),
    surface = Color(0xFF181210),
    onSurface = Color(0xFFEDE0DC),
    surfaceVariant = Color(0xFF53433E),
    onSurfaceVariant = Color(0xFFD8C2BA),
    surfaceContainerLowest = Color(0xFF120D0B),
    surfaceContainerLow = Color(0xFF201A18),
    surfaceContainer = Color(0xFF251E1C),
    surfaceContainerHigh = Color(0xFF302826),
    surfaceContainerHighest = Color(0xFF3B3330),
    outline = Color(0xFFA08D86),
    outlineVariant = Color(0xFF53433E),
)

private val TangerineLight = lightColorScheme(
    primary = Color(0xFF9C431E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCE),
    onPrimaryContainer = Color(0xFF380E00),
    secondary = Color(0xFF77574C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCE),
    onSecondaryContainer = Color(0xFF2C160E),
    tertiary = Color(0xFF6B5E2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E2A7),
    onTertiaryContainer = Color(0xFF231B00),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231916),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231916),
    surfaceVariant = Color(0xFFF5DED6),
    onSurfaceVariant = Color(0xFF53433E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1EC),
    surfaceContainer = Color(0xFFFCEAE4),
    surfaceContainerHigh = Color(0xFFF6E4DE),
    surfaceContainerHighest = Color(0xFFF0DFD9),
    outline = Color(0xFF85736D),
    outlineVariant = Color(0xFFD8C2BA),
)

private val SageDark = darkColorScheme(
    primary = Color(0xFFA1D39A),
    onPrimary = Color(0xFF0A390F),
    primaryContainer = Color(0xFF235024),
    onPrimaryContainer = Color(0xFFBCF0B4),
    secondary = Color(0xFFBACCb4),
    onSecondary = Color(0xFF253423),
    secondaryContainer = Color(0xFF3B4B38),
    onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFFA0CFD4),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF1F4D52),
    onTertiaryContainer = Color(0xFFBCEBF0),
    background = Color(0xFF10140F),
    onBackground = Color(0xFFE0E4DC),
    surface = Color(0xFF10140F),
    onSurface = Color(0xFFE0E4DC),
    surfaceContainerLowest = Color(0xFF0B0F0A),
    surfaceContainerLow = Color(0xFF181D17),
    surfaceContainer = Color(0xFF1C211B),
    surfaceContainerHigh = Color(0xFF272B25),
    surfaceContainerHighest = Color(0xFF323630),
)

private val SageLight = lightColorScheme(
    primary = Color(0xFF3B6939),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBCF0B4),
    onPrimaryContainer = Color(0xFF002204),
    secondary = Color(0xFF52634F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF111F0F),
    tertiary = Color(0xFF38656A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF002023),
    background = Color(0xFFF7FBF1),
    onBackground = Color(0xFF191D17),
    surface = Color(0xFFF7FBF1),
    onSurface = Color(0xFF191D17),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5EC),
    surfaceContainer = Color(0xFFEBEFE6),
    surfaceContainerHigh = Color(0xFFE6EAE0),
    surfaceContainerHighest = Color(0xFFE0E4DC),
)

private val CobaltDark = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF18448F),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFBFC6DC),
    onSecondary = Color(0xFF293041),
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = Color(0xFFDBE2F9),
    tertiary = Color(0xFFDEBCDF),
    onTertiary = Color(0xFF402843),
    tertiaryContainer = Color(0xFF583E5B),
    onTertiaryContainer = Color(0xFFFBD7FC),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF191B20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
)

private val CobaltLight = lightColorScheme(
    primary = Color(0xFF355CA8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF575E71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE2F9),
    onSecondaryContainer = Color(0xFF141B2C),
    tertiary = Color(0xFF715573),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFBD7FC),
    onTertiaryContainer = Color(0xFF29132D),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191B20),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191B20),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E8EE),
    surfaceContainerHighest = Color(0xFFE2E2E9),
)

private val PlumDark = darkColorScheme(
    primary = Color(0xFFECB4FF),
    onPrimary = Color(0xFF4D1567),
    primaryContainer = Color(0xFF662E80),
    onPrimaryContainer = Color(0xFFF8D8FF),
    secondary = Color(0xFFD5C0D8),
    onSecondary = Color(0xFF3A2B3D),
    secondaryContainer = Color(0xFF514155),
    onSecondaryContainer = Color(0xFFF2DCF4),
    tertiary = Color(0xFFF6B7B6),
    onTertiary = Color(0xFF4D2525),
    tertiaryContainer = Color(0xFF673B3B),
    onTertiaryContainer = Color(0xFFFFDAD9),
    background = Color(0xFF161217),
    onBackground = Color(0xFFEAE0E7),
    surface = Color(0xFF161217),
    onSurface = Color(0xFFEAE0E7),
    surfaceContainerLowest = Color(0xFF110D12),
    surfaceContainerLow = Color(0xFF1F1A20),
    surfaceContainer = Color(0xFF231E24),
    surfaceContainerHigh = Color(0xFF2D282E),
    surfaceContainerHighest = Color(0xFF383339),
)

private val PlumLight = lightColorScheme(
    primary = Color(0xFF80469A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF8D8FF),
    onPrimaryContainer = Color(0xFF320049),
    secondary = Color(0xFF69586D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2DCF4),
    onSecondaryContainer = Color(0xFF231727),
    tertiary = Color(0xFF825251),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD9),
    onTertiaryContainer = Color(0xFF331112),
    background = Color(0xFFFFF7FD),
    onBackground = Color(0xFF1F1A20),
    surface = Color(0xFFFFF7FD),
    onSurface = Color(0xFF1F1A20),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF0F9),
    surfaceContainer = Color(0xFFF4EBF3),
    surfaceContainerHigh = Color(0xFFEEE5ED),
    surfaceContainerHighest = Color(0xFFEAE0E7),
)
