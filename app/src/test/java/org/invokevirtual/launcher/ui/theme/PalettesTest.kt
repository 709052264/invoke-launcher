package org.invokevirtual.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class PalettesTest {

    private val extremeSeeds = listOf(
        0xFFFFFFFF.toInt(), // pure white
        0xFF000000.toInt(), // pure black
        0xFFFFEA00.toInt(), // neon yellow
        0xFF002171.toInt(), // deep navy
        0xFFD50000.toInt(), // vivid crimson
        0xFF00C853.toInt(), // bright emerald
    )

    @Test
    fun `seedColorScheme maintains WCAG AA contrast across extreme EyeDropper seeds`() {
        for (seed in extremeSeeds) {
            for (dark in listOf(false, true)) {
                val scheme = seedColorScheme(seed, dark)
                assertContrastAtLeast(scheme.onPrimary, scheme.primary, 4.5f, "primary ($seed, dark=$dark)")
                assertContrastAtLeast(
                    scheme.onPrimaryContainer,
                    scheme.primaryContainer,
                    4.5f,
                    "primaryContainer ($seed, dark=$dark)",
                )
                assertContrastAtLeast(
                    scheme.onSecondaryContainer,
                    scheme.secondaryContainer,
                    4.5f,
                    "secondaryContainer ($seed, dark=$dark)",
                )
                assertContrastAtLeast(
                    scheme.onTertiaryContainer,
                    scheme.tertiaryContainer,
                    4.5f,
                    "tertiaryContainer ($seed, dark=$dark)",
                )
                assertContrastAtLeast(scheme.onSurface, scheme.surface, 7.0f, "surface ($seed, dark=$dark)")
            }
        }
    }

    @Test
    fun `toAmoledBlack forces pure black base surfaces while keeping elevated cards distinct`() {
        val amoled = seedColorScheme(0xFFE85D4A.toInt(), dark = true).toAmoledBlack()
        assertEquals(Color.Black, amoled.background)
        assertEquals(Color.Black, amoled.surface)
        assertEquals(Color.Black, amoled.surfaceContainerLowest)
        assertTrue(amoled.surfaceContainerHigh != Color.Black)
    }

    private fun assertContrastAtLeast(fg: Color, bg: Color, minRatio: Float, label: String) {
        val l1 = fg.luminance()
        val l2 = bg.luminance()
        val ratio = (max(l1, l2) + 0.05f) / (min(l1, l2) + 0.05f)
        assertTrue("Expected $label contrast >= $minRatio, got $ratio", ratio >= minRatio)
    }
}
