package org.invokevirtual.launcher.ui.home

import org.invokevirtual.launcher.data.FolderItem
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.system.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderBentoTest {

    private fun <T> flattenRows(rows: List<BentoExtraRow<T>>): List<T> = buildList {
        rows.forEach { row ->
            when (row) {
                is BentoExtraRow.FullWide -> add(row.app)
                is BentoExtraRow.TwoHalves -> {
                    add(row.first)
                    add(row.second)
                }

                is BentoExtraRow.WideAndTwoCompact -> {
                    add(row.wide)
                    add(row.compactA)
                    add(row.compactB)
                }

                is BentoExtraRow.FourCompact -> addAll(row.apps)
            }
        }
    }

    @Test
    fun `buildBentoExtraRows preserves all items in order for counts 0 through 12`() {
        for (count in 0..12) {
            val items = (1..count).map { "App$it" }
            val rows = buildBentoExtraRows(items)
            assertEquals("Count $count should preserve exact sequence", items, flattenRows(rows))
        }
    }

    @Test
    fun `buildBentoExtraRows builds expected bento patterns and alternates wide position`() {
        assertTrue(buildBentoExtraRows(emptyList<String>()).isEmpty())

        assertEquals(
            listOf(BentoExtraRow.FullWide("A")),
            buildBentoExtraRows(listOf("A")),
        )

        assertEquals(
            listOf(BentoExtraRow.TwoHalves("A", "B")),
            buildBentoExtraRows(listOf("A", "B")),
        )

        assertEquals(
            listOf(BentoExtraRow.WideAndTwoCompact("A", "B", "C", wideOnLeft = true)),
            buildBentoExtraRows(listOf("A", "B", "C")),
        )

        assertEquals(
            listOf(BentoExtraRow.FourCompact(listOf("A", "B", "C", "D"))),
            buildBentoExtraRows(listOf("A", "B", "C", "D")),
        )

        // 10 extra items -> WideAndTwoCompact(left, 3) + FourCompact(4) + WideAndTwoCompact(right, 3)
        val tenItems = (1..10).map { "A$it" }
        val tenRows = buildBentoExtraRows(tenItems)
        assertEquals(3, tenRows.size)
        val firstWide = tenRows[0] as BentoExtraRow.WideAndTwoCompact
        assertTrue(firstWide.wideOnLeft)
        assertTrue(tenRows[1] is BentoExtraRow.FourCompact)
        val secondWide = tenRows[2] as BentoExtraRow.WideAndTwoCompact
        assertFalse(secondWide.wideOnLeft)
    }

    @Test
    fun `buildBentoRenderSegments splits clusters around spacers and skips missing apps`() {
        val appA = FolderItem.AppItem("ia", AppKey("pkg.a", ".A", 0L))
        val appMissing = FolderItem.AppItem("im", AppKey("pkg.missing", ".M", 0L))
        val appB = FolderItem.AppItem("ib", AppKey("pkg.b", ".B", 0L))
        val appC = FolderItem.AppItem("ic", AppKey("pkg.c", ".C", 0L))
        val topSpacer = FolderItem.SpacerItem("s_top", span = TileSpan.BANNER, customHeightDp = 120)
        val midSpacer = FolderItem.SpacerItem("s_mid", span = TileSpan.WIDE, customHeightDp = 64)

        val segments = buildBentoRenderSegments(
            items = listOf(topSpacer, appA, appMissing, appB, midSpacer, appC),
        ) { item ->
            if (item.appKey.packageName == "pkg.missing") null else item.id
        }

        assertEquals(
            listOf(
                BentoRenderSegment.Spacer(topSpacer),
                BentoRenderSegment.Cluster(listOf("ia", "ib")),
                BentoRenderSegment.Spacer(midSpacer),
                BentoRenderSegment.Cluster(listOf("ic")),
            ),
            segments,
        )
    }
}
