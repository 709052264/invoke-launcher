package org.invokevirtual.launcher.ui.invoke

import org.invokevirtual.launcher.data.SettingsStore
import org.invokevirtual.launcher.system.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InvokeSearchEngineTest {

    private val sampleLabels = listOf(
        "1Password",
        "Android Studio",
        "Calendar",
        "Camera",
        "Clock",
        "Gmail",
        "Google Maps",
        "Messages",
        "Settings",
        "Spotify",
        "YouTube",
        "YouTube Music",
    )

    @Test
    fun `prefix and exact matches rank above substring and fuzzy matches`() {
        val ranked = InvokeSearchEngine.rankByLabel("cam", sampleLabels) { it }
        assertEquals("Camera", ranked.first())
    }

    @Test
    fun `camelCase and word-boundary initials match accurately`() {
        val ytRanked = InvokeSearchEngine.rankByLabel("yt", sampleLabels) { it }
        assertEquals("YouTube", ytRanked.first())
        assertTrue(ytRanked.contains("YouTube Music"))

        val gmRanked = InvokeSearchEngine.rankByLabel("gm", sampleLabels) { it }
        assertEquals(listOf("Gmail", "Google Maps"), gmRanked)

        val asRanked = InvokeSearchEngine.rankByLabel("as", sampleLabels) { it }
        assertEquals("Android Studio", asRanked.first())

        val ytmRanked = InvokeSearchEngine.rankByLabel("ytm", sampleLabels) { it }
        assertEquals("YouTube Music", ytmRanked.first())
    }

    @Test
    fun `ordered fuzzy subsequence matches vowel-dropped queries`() {
        val ranked = InvokeSearchEngine.rankByLabel("clndr", sampleLabels) { it }
        assertEquals("Calendar", ranked.first())
    }

    @Test
    fun `groupByLetter buckets digits and symbols under hash first then A to Z`() {
        val grouped = InvokeSearchEngine.groupByLetter(sampleLabels) { it }
        assertEquals('#', grouped.first().first)
        assertEquals(listOf("1Password"), grouped.first().second)

        val letters = grouped.map { it.first }
        assertEquals(listOf('#', 'A', 'C', 'G', 'M', 'S', 'Y'), letters)
        assertEquals(listOf("Calendar", "Camera", "Clock"), grouped[2].second)
    }

    @Test
    fun `inline math evaluates precedence parentheses and unicode operators`() {
        val tax = InvokeSearchEngine.tryEvaluateMath("145 * 1.18")
        assertNotNull(tax)
        assertEquals("171.1", tax?.formattedValue)

        val parens = InvokeSearchEngine.tryEvaluateMath("(480 + 120) / 4")
        assertNotNull(parens)
        assertEquals("150", parens?.formattedValue)

        val unicode = InvokeSearchEngine.tryEvaluateMath("12 × 8 − 0".replace('−', '-'))
        assertNotNull(unicode)
        assertEquals("96", unicode?.formattedValue)

        val division = InvokeSearchEngine.tryEvaluateMath("100 ÷ 8")
        assertNotNull(division)
        assertEquals("12.5", division?.formattedValue)
    }

    @Test
    fun `inline math ignores plain numbers app names and division by zero`() {
        assertNull(InvokeSearchEngine.tryEvaluateMath("2026"))
        assertNull(InvokeSearchEngine.tryEvaluateMath("1Password"))
        assertNull(InvokeSearchEngine.tryEvaluateMath("42 / 0"))
        assertNull(InvokeSearchEngine.tryEvaluateMath("(10 + 5"))
    }

    @Test
    fun `pinned drawer app keys serialization round-trips more than four apps in order`() {
        val keys = (1..6).map { i ->
            AppKey("com.example.app$i", "com.example.app$i.MainActivity", if (i == 6) 10L else 0L)
        }
        val encoded = SettingsStore.encodeAppKeys(keys + keys.first())
        val decoded = SettingsStore.decodeAppKeys(encoded)
        assertEquals(keys, decoded)

        val dockDecoded = SettingsStore.decodeDockApps(SettingsStore.encodeDockApps(keys))
        assertEquals(SettingsStore.MAX_DOCK_APPS, dockDecoded.size)
    }
}
