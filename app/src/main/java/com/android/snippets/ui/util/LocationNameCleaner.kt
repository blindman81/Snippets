package com.android.snippets.ui.util

object LocationNameCleaner {

    private val MAP_SUFFIXES = listOf(
        Regex("""\s*[-–—·|]\s*Google\s+Maps.*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*[-–—·|]\s*Maps.*$""", RegexOption.IGNORE_CASE)
    )

    private val DELIMITERS = listOf(" - ", " | ", " · ", " — ", " – ")

    private val PREPOSITION_PATTERNS = listOf(
        """\s+in\s+.+$""",
        """\s+at\s+.+$""",
        """\s+near\s+.+$""",
        """\s+on\s+.+$""",
        """\s+opp\.?\s+.+$""",
        """\s+opposite\s+.+$""",
        """\s+beside\s+.+$""",
        """\s+next\s+to\s+.+$"""
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    fun cleanLocationName(name: String): String {
        var cleaned = name.trim()

        for (pattern in MAP_SUFFIXES) {
            cleaned = cleaned.replace(pattern, "").trim()
        }

        for (delimiter in DELIMITERS) {
            val idx = cleaned.indexOf(delimiter)
            if (idx > 0) {
                cleaned = cleaned.substring(0, idx).trim()
            }
        }

        for (regex in PREPOSITION_PATTERNS) {
            val match = regex.find(cleaned)
            if (match != null && match.range.first > 2) {
                cleaned = cleaned.substring(0, match.range.first).trim()
            }
        }

        val commaIdx = cleaned.indexOf(',')
        if (commaIdx > 2) {
            cleaned = cleaned.substring(0, commaIdx).trim()
        }

        cleaned = cleaned.replace(Regex("""\s+"""), " ").trim()

        return cleaned.ifBlank { name.trim() }
    }

    fun unescapeHtml(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .trim()
    }

    fun isValidPlaceName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val trimmed = name.trim()
        if (trimmed.length < 2) return false
        if (trimmed.equals("Google Maps", ignoreCase = true)) return false
        if (trimmed.equals("Maps", ignoreCase = true)) return false
        if (trimmed.matches(Regex("""^-?\d+(\.\d+)?$"""))) return false
        if (trimmed.matches(Regex("""^-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?$"""))) return false
        return true
    }
}
