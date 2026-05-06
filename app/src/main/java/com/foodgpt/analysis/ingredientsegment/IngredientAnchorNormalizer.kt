package com.foodgpt.analysis.ingredientsegment

/**
 * Detects the ingredient list anchor as the first line-start occurrence (after optional spaces on that line)
 * of FR/EN labels per domain spec FR-002.
 */
class IngredientAnchorNormalizer {

    private val lineStartAnchorRegex =
        Regex("""(?m)(^\s*)(?i)(ingr[ée]dients|ingr[ée]dient|ingredients|ingredient)\b""")

    fun findFirstPhraseAnchorIndex(text: String): Int? {
        val match = lineStartAnchorRegex.find(text) ?: return null
        return match.groups[2]?.range?.first
    }
}
