package org.invokevirtual.launcher.data

import org.invokevirtual.launcher.system.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeCanvasStoreTest {

    @Test
    fun `encode and decode preserves mixed AppTiles ShortcutTiles custom colors and WidgetBlocks`() {
        val original = listOf(
            CanvasBlock.WidgetBlock(
                id = "widget_42",
                appWidgetId = 42,
                heightTier = WidgetHeightTier.SQUARE_2X2,
                shape = TileShapePreset.LEAF_ALT,
            ),
            CanvasBlock.AppTile(
                id = "app_1",
                appKey = AppKey("com.example.music", ".MainActivity", 0L),
                span = TileSpan.HERO,
                shape = TileShapePreset.COOKIE,
                tone = TileTone.PRIMARY,
                customBgArgb = 0xFF6750A4.toInt(),
            ),
            CanvasBlock.ShortcutTile(
                id = "sc_1",
                packageName = "com.example.notes",
                shortcutId = "new_note",
                userSerial = 0L,
                label = "New note",
                span = TileSpan.WIDE_3X1,
                shape = TileShapePreset.ARCH,
                tone = TileTone.TERTIARY,
                customBgArgb = 0xFF006D3B.toInt(),
            ),
            CanvasBlock.AppTile(
                id = "app_2",
                appKey = AppKey("com.example.chat", ".ChatActivity", 10L),
                span = TileSpan.FEATURED,
                shape = TileShapePreset.CLOVER,
                tone = TileTone.SECONDARY,
            ),
            CanvasBlock.WidgetBlock(
                id = "widget_7",
                appWidgetId = 7,
                heightTier = WidgetHeightTier.WIDE_2X1,
                shape = TileShapePreset.HEXAGON,
            ),
        )

        val encoded = HomeCanvasStore.encodeBlocks(original)
        val decoded = HomeCanvasStore.decodeBlocks(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `decode handles empty string legacy records and unknown enum fallbacks gracefully`() {
        assertTrue(HomeCanvasStore.decodeBlocks("").isEmpty())

        val legacyAndUnknownRecords = listOf(
            // Legacy 8-part AppTile with unknown enums
            listOf("A", "app_x", "com.demo.app", ".Main", "0", "UNKNOWN_SPAN", "UNKNOWN_SHAPE", "UNKNOWN_TONE")
                .joinToString("\u001F"),
            // Legacy 9-part ShortcutTile without customBgArgb
            listOf("S", "sc_x", "com.demo.notes", "compose", "0", "Compose", "WIDE", "LEAF", "TERTIARY")
                .joinToString("\u001F"),
            // Legacy 4-part WidgetBlock without shape
            listOf("W", "widget_9", "9", "TALL")
                .joinToString("\u001F"),
        ).joinToString("\u001E")

        val decoded = HomeCanvasStore.decodeBlocks(legacyAndUnknownRecords)
        assertEquals(3, decoded.size)

        val tile = decoded[0] as CanvasBlock.AppTile
        assertEquals(TileSpan.COMPACT, tile.span)
        assertEquals(TileShapePreset.SQUIRCLE, tile.shape)
        assertEquals(TileTone.SURFACE, tile.tone)
        assertEquals(null, tile.customBgArgb)

        val shortcut = decoded[1] as CanvasBlock.ShortcutTile
        assertEquals(TileSpan.WIDE, shortcut.span)
        assertEquals(TileShapePreset.LEAF, shortcut.shape)
        assertEquals(TileTone.TERTIARY, shortcut.tone)
        assertEquals(null, shortcut.customBgArgb)

        val widget = decoded[2] as CanvasBlock.WidgetBlock
        assertEquals(WidgetHeightTier.TALL, widget.heightTier)
        assertEquals(TileShapePreset.SQUIRCLE, widget.shape)
    }

    @Test
    fun `span shape tone and widget height cycling wraps cleanly`() {
        assertEquals(TileSpan.WIDE, TileSpan.COMPACT.next())
        assertEquals(TileSpan.TALL, TileSpan.WIDE.next())
        assertEquals(TileSpan.HERO, TileSpan.TALL.next())
        assertEquals(TileSpan.WIDE_3X1, TileSpan.HERO.next())
        assertEquals(TileSpan.BANNER, TileSpan.WIDE_3X1.next())
        assertEquals(TileSpan.FEATURED, TileSpan.BANNER.next())
        assertEquals(TileSpan.COMPACT, TileSpan.FEATURED.next())

        assertEquals(WidgetHeightTier.SQUARE_2X2, WidgetHeightTier.WIDE_2X1.next())
        assertEquals(WidgetHeightTier.COMPACT, WidgetHeightTier.SQUARE_2X2.next())
        assertEquals(WidgetHeightTier.MEDIUM, WidgetHeightTier.COMPACT.next())
        assertEquals(WidgetHeightTier.TALL, WidgetHeightTier.MEDIUM.next())
        assertEquals(WidgetHeightTier.WIDE_2X1, WidgetHeightTier.TALL.next())

        assertEquals(TileShapePreset.SQUIRCLE, TileShapePreset.entries.last().next())
        assertEquals(TileTone.SURFACE, TileTone.entries.last().next())
    }

    @Test
    fun `dock apps encoding and decoding round trips and enforces max 4 slots`() {
        assertTrue(SettingsStore.decodeDockApps("").isEmpty())

        val apps = (1..6).map { index ->
            AppKey("com.example.app$index", ".Activity$index", index.toLong())
        }
        val encoded = SettingsStore.encodeDockApps(apps)
        val decoded = SettingsStore.decodeDockApps(encoded)

        assertEquals(SettingsStore.MAX_DOCK_APPS, decoded.size)
        assertEquals(apps.take(4), decoded)
    }

    @Test
    fun `custom widget size encodes and decodes cleanly`() {
        val customWidget = CanvasBlock.WidgetBlock(
            id = "widget_custom",
            appWidgetId = 108,
            heightTier = WidgetHeightTier.MEDIUM,
            shape = TileShapePreset.COOKIE,
            customColumns = 3,
            customHeightDp = 248,
        )
        val encoded = HomeCanvasStore.encodeBlocks(listOf(customWidget))
        val decoded = HomeCanvasStore.decodeBlocks(encoded)

        assertEquals(listOf(customWidget), decoded)
        assertEquals("3×248dp", customWidget.sizeLabel)
        assertTrue(customWidget.hasCustomSize)
    }

    @Test
    fun `custom app and shortcut tile sizes encode and decode cleanly`() {
        val customApp = CanvasBlock.AppTile(
            id = "app_custom",
            appKey = AppKey("com.example.reader", ".ReaderActivity", 0L),
            span = TileSpan.WIDE,
            shape = TileShapePreset.ARCH,
            tone = TileTone.PRIMARY,
            customBgArgb = 0xFF334455.toInt(),
            customColumns = 3,
            customHeightDp = 132,
        )
        val customShortcut = CanvasBlock.ShortcutTile(
            id = "sc_custom",
            packageName = "com.example.timer",
            shortcutId = "start_10m",
            userSerial = 0L,
            label = "10m timer",
            span = TileSpan.COMPACT,
            shape = TileShapePreset.COOKIE,
            tone = TileTone.TERTIARY,
            customColumns = 1,
            customHeightDp = 96,
        )
        val original = listOf(customApp, customShortcut)
        val encoded = HomeCanvasStore.encodeBlocks(original)
        val decoded = HomeCanvasStore.decodeBlocks(encoded)

        assertEquals(original, decoded)
        assertEquals("3×132dp", customApp.sizeLabel)
        assertTrue(customApp.hasCustomSize)
        assertEquals("1×96dp", customShortcut.sizeLabel)
        assertTrue(customShortcut.hasCustomSize)
    }

    @Test
    fun `contact tiles encode and decode with custom sizes and colors`() {
        val contactTile = CanvasBlock.ContactTile(
            id = "ct_1",
            lookupKey = "lookup_maya",
            displayName = "Maya Lin",
            phoneNumber = "+1 555-0142",
            span = TileSpan.WIDE,
            shape = TileShapePreset.PILL,
            tone = TileTone.TERTIARY,
            customBgArgb = 0xFF1E88E5.toInt(),
            customColumns = 2,
            customHeightDp = 92,
        )
        val encoded = HomeCanvasStore.encodeBlocks(listOf(contactTile))
        val decoded = HomeCanvasStore.decodeBlocks(encoded)

        assertEquals(listOf(contactTile), decoded)
        assertEquals("ML", contactTile.initials)
        assertEquals("2×92dp", contactTile.sizeLabel)
        assertTrue(contactTile.hasCustomSize)
    }
}
