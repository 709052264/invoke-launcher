package org.invokevirtual.launcher.ui.theme

import androidx.graphics.shapes.Morph
import org.invokevirtual.launcher.data.TileShapePreset
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapesMorphTest {

    @Test
    fun `all TileShapePresets build valid aspect polygons and morphs at custom aspect ratios`() {
        val aspects = listOf(0.4f, 0.7f, 1.0f, 1.8f, 3.2f, 4.2f)
        for (aspect in aspects) {
            for (preset in TileShapePreset.entries) {
                val rest = preset.toAspectPolygon(aspect)
                val pressed = preset.toPressedAspectPolygon(aspect)
                val pressMorph = Morph(rest, pressed)
                assertTrue(pressMorph.asCubics(0.5f).isNotEmpty())
            }
            // Also verify cross-shape morphs across presets at each aspect ratio
            val entries = TileShapePreset.entries
            for (i in entries.indices) {
                val next = entries[(i + 1) % entries.size]
                val crossMorph = Morph(
                    entries[i].toAspectPolygon(aspect),
                    next.toAspectPolygon(aspect),
                )
                assertTrue(crossMorph.asCubics(0.5f).isNotEmpty())
            }
        }
    }

    @Test
    fun `cross-aspect morphs succeed when resizing custom widget shapes`() {
        for (preset in TileShapePreset.entries) {
            val tallPoly = preset.toAspectPolygon(0.45f)
            val squarePoly = preset.toAspectPolygon(1.0f)
            val widePoly = preset.toAspectPolygon(3.6f)
            assertTrue(Morph(tallPoly, squarePoly).asCubics(0.5f).isNotEmpty())
            assertTrue(Morph(squarePoly, widePoly).asCubics(0.5f).isNotEmpty())
            assertTrue(Morph(widePoly, tallPoly).asCubics(0.5f).isNotEmpty())
        }
    }
}
