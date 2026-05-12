package com.miamia.additives

object AdditiveJustificationFormatter {

    const val MAX_SINGLE_LINE_CHARS = 120

    fun truncatedForPreview(text: String): String {
        if (text.length <= MAX_SINGLE_LINE_CHARS) return text
        val take = (MAX_SINGLE_LINE_CHARS - 1).coerceAtLeast(1)
        return text.take(take).trimEnd() + "…"
    }

    fun needsExpansion(text: String): Boolean = text.length > MAX_SINGLE_LINE_CHARS
}
