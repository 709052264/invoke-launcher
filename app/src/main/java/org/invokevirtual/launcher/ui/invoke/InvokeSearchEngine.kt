package org.invokevirtual.launcher.ui.invoke

import androidx.compose.runtime.Immutable
import org.invokevirtual.launcher.data.QuickContact
import org.invokevirtual.launcher.system.InstalledApp
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

@Immutable
data class LetterSection(
    val letter: Char,
    val apps: List<InstalledApp>,
)

@Immutable
data class MathEvalResult(
    val expression: String,
    val formattedValue: String,
)

/**
 * Pure-Kotlin fuzzy matcher, A–Z grouper, contact ranker, and inline arithmetic evaluator for the Invoke Sheet.
 */
object InvokeSearchEngine {

    private val CONTACT_INTENT_KEYWORDS = setOf("call", "dial", "text", "msg", "sms", "message", "contact", "contacts")
    private val CONTACT_VERB_PREFIXES = listOf("call ", "dial ", "text ", "msg ", "sms ", "message ")

    /**
     * Ranks [apps] against [rawQuery] using:
     * 1. Exact/prefix match (100)
     * 2. Word-boundary / camelCase initials match, e.g. `"yt"` -> `"YouTube"`, `"gm"` -> `"Google Maps"` (88)
     * 3. Word-start substring match (80)
     * 4. Anywhere substring match (70)
     * 5. Ordered fuzzy subsequence match with gap penalty (40..64)
     */
    fun rankApps(rawQuery: String, apps: List<InstalledApp>): List<InstalledApp> =
        rankByLabel(rawQuery, apps) { it.label }

    /**
     * Ranks pinned [QuickContact]s against [rawQuery], stripping optional natural verb prefixes
     * such as `"call mom"`, `"text alex"`, or `"msg sam"`.
     */
    fun rankContacts(rawQuery: String, contacts: List<QuickContact>): List<QuickContact> {
        if (contacts.isEmpty()) return emptyList()
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return emptyList()

        val lower = trimmed.lowercase()
        if (lower in CONTACT_INTENT_KEYWORDS) {
            return contacts
        }
        val matchedPrefix = CONTACT_VERB_PREFIXES.firstOrNull { lower.startsWith(it) }
        val effectiveQuery = if (matchedPrefix != null) {
            trimmed.substring(matchedPrefix.length).trim()
        } else {
            trimmed
        }
        if (effectiveQuery.isEmpty()) {
            return contacts
        }
        return rankByLabel(effectiveQuery, contacts) { it.displayName }
    }

    /** True when the user's query explicitly looks like a call/message intent (e.g., "call", "text"). */
    fun isContactIntentQuery(rawQuery: String): Boolean {
        val lower = rawQuery.trim().lowercase()
        return lower in CONTACT_INTENT_KEYWORDS ||
            CONTACT_VERB_PREFIXES.any { lower.startsWith(it) }
    }

    internal fun <T> rankByLabel(
        rawQuery: String,
        items: List<T>,
        labelOf: (T) -> String,
    ): List<T> {
        val query = rawQuery.trim()
        if (query.isEmpty()) return items

        val normalizedQuery = query.lowercase()
        val scored = ArrayList<Pair<Int, T>>(items.size)

        for (item in items) {
            val score = fuzzyScore(normalizedQuery, labelOf(item))
            if (score > 0) {
                scored += score to item
            }
        }
        return scored
            .sortedWith(
                compareByDescending<Pair<Int, T>> { it.first }
                    .thenBy { labelOf(it.second).lowercase() },
            )
            .map { it.second }
    }

    internal fun fuzzyScore(queryLower: String, candidateLabel: String): Int {
        if (queryLower.isEmpty()) return 1
        val lower = candidateLabel.lowercase()

        if (lower == queryLower) return 110
        if (lower.startsWith(queryLower)) return 100

        val initials = extractInitials(candidateLabel)
        if (initials.isNotEmpty()) {
            if (initials == queryLower) return 92
            if (initials.startsWith(queryLower)) return 88
        }

        val words = lower.split(' ', '-', '_', '.')
        if (words.any { it.startsWith(queryLower) }) return 80

        val subIndex = lower.indexOf(queryLower)
        if (subIndex >= 0) return (74 - subIndex.coerceAtMost(14)).coerceAtLeast(60)

        var qIdx = 0
        var gaps = 0
        var firstMatch = -1
        for (cIdx in lower.indices) {
            if (lower[cIdx] == queryLower[qIdx]) {
                if (firstMatch == -1) firstMatch = cIdx
                qIdx++
                if (qIdx == queryLower.length) break
            } else if (firstMatch != -1) {
                gaps++
            }
        }
        if (qIdx == queryLower.length) {
            return (56 - gaps * 2 - firstMatch).coerceAtLeast(20)
        }
        return 0
    }

    private fun extractInitials(label: String): String {
        val sb = StringBuilder(6)
        var prevSeparator = true
        var prevLower = false
        for (ch in label) {
            when {
                !ch.isLetterOrDigit() -> {
                    prevSeparator = true
                    prevLower = false
                }
                prevSeparator || (ch.isUpperCase() && prevLower) -> {
                    sb.append(ch.lowercaseChar())
                    prevSeparator = false
                    prevLower = ch.isLowerCase()
                }
                else -> {
                    prevLower = ch.isLowerCase()
                }
            }
        }
        return sb.toString()
    }

    fun groupAppsByLetter(apps: List<InstalledApp>): List<LetterSection> =
        groupByLetter(apps) { it.label }.map { (letter, list) -> LetterSection(letter, list) }

    internal fun <T> groupByLetter(
        items: List<T>,
        labelOf: (T) -> String,
    ): List<Pair<Char, List<T>>> {
        if (items.isEmpty()) return emptyList()
        val grouped = LinkedHashMap<Char, MutableList<T>>()
        for (item in items) {
            val first = labelOf(item).trimStart().firstOrNull()?.uppercaseChar()
            val bucket = if (first != null && first in 'A'..'Z') first else '#'
            grouped.getOrPut(bucket) { ArrayList() }.add(item)
        }
        return grouped.entries
            .sortedWith(compareBy<Map.Entry<Char, *>> { if (it.key == '#') 0 else 1 }.thenBy { it.key })
            .map { (letter, list) -> letter to list }
    }

    /**
     * Evaluates simple inline arithmetic expressions typed into the Invoke bar (e.g. `145 * 1.18`,
     * `(480 + 120) / 4`). Requires at least one operator so plain numbers like `"2026"` don't pop
     * up a calculator card while searching.
     */
    fun tryEvaluateMath(rawQuery: String): MathEvalResult? {
        val expr = rawQuery.trim()
            .replace('×', '*')
            .replace('÷', '/')
        if (expr.length < 3) return null
        if (!expr.any { it == '+' || it == '-' || it == '*' || it == '/' }) return null
        if (!expr.all { it.isDigit() || it in "+-*/(). " }) return null

        val value = runCatching { MathParser(expr).parse() }.getOrNull() ?: return null
        if (!value.isFinite()) return null

        val formatted = formatNumber(value)
        return MathEvalResult(expression = expr, formattedValue = formatted)
    }

    private fun formatNumber(value: Double): String {
        if (abs(value) >= 1e12) return "%.4g".format(value)
        val rounded = BigDecimal.valueOf(value)
            .setScale(6, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return rounded.toPlainString()
    }

    private class MathParser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            skipWhitespace()
            if (pos != input.length) error("Trailing characters")
            return result
        }

        private fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                skipWhitespace()
                left = when {
                    consume('+') -> left + parseTerm()
                    consume('-') -> left - parseTerm()
                    else -> return left
                }
            }
        }

        private fun parseTerm(): Double {
            var left = parseFactor()
            while (true) {
                skipWhitespace()
                left = when {
                    consume('*') -> left * parseFactor()
                    consume('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) error("Division by zero")
                        left / divisor
                    }
                    else -> return left
                }
            }
        }

        private fun parseFactor(): Double {
            skipWhitespace()
            if (consume('+')) return parseFactor()
            if (consume('-')) return -parseFactor()
            if (consume('(')) {
                val value = parseExpression()
                skipWhitespace()
                if (!consume(')')) error("Missing closing parenthesis")
                return value
            }
            val start = pos
            var hasDot = false
            while (pos < input.length) {
                val c = input[pos]
                if (c.isDigit()) {
                    pos++
                } else if (c == '.' && !hasDot) {
                    hasDot = true
                    pos++
                } else {
                    break
                }
            }
            if (pos == start) error("Expected number")
            return input.substring(start, pos).toDouble()
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun consume(expected: Char): Boolean {
            if (pos < input.length && input[pos] == expected) {
                pos++
                return true
            }
            return false
        }
    }
}
