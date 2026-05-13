package com.miamia.composition

import kotlin.math.abs
import kotlin.math.min

/**
 * Politique d’ancrage textuel **v1** (spec Feature C / clarify 2026-05-13) :
 * normalisations **mécaniques** uniquement (casse, espaces) puis test de sous-chaîne
 * dans le segment validé. Aucun synonyme implicite.
 *
 * Repli **fuzzy** conservateur (2026-05) : lorsque le modèle déforme légèrement l’OCR
 * (fautes de génération proches du texte source), une fenêtre glissante avec distance
 * d’édition bornée peut valider l’ancrage — sans remplacer la contrainte « tout ou rien »
 * sur les lignes checkables.
 */
object SegmentAnchoringV1 {

    const val MIN_INGREDIENT_LINE_LENGTH: Int = 4

    private val percentOnlyParen = Regex("""\(\s*[0-9]+(?:[.,][0-9]+)?\s*%\s*\)""")

    /** borne perf / faux positifs sur l’entrée fuzzy */
    private const val FUZZY_MAX_NEEDLE = 96

    fun normalizeMechanical(text: String): String =
        text.lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

    /** Retire les segments `(12 %)` / `(3,5%)` qui enrichissent le libellé sans être dans l’OCR. */
    internal fun stripPercentOnlyParentheticals(raw: String): String =
        percentOnlyParen.replace(raw, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    /** Vrai si [needleRaw], une fois normalisé mécaniquement, est une sous-chaîne du segment normalisé. */
    fun isSubstringAnchored(needleRaw: String, segmentRaw: String): Boolean {
        val needle = normalizeMechanical(needleRaw)
        if (needle.isEmpty()) return true
        val haystack = normalizeMechanical(segmentRaw)
        return haystack.contains(needle)
    }

    /**
     * Ancrage strict puis repli fuzzy (pour typos modèle / OCR proches du segment).
     */
    fun isAnchoredInSegment(needleRaw: String, segmentRaw: String): Boolean {
        if (isSubstringAnchored(needleRaw, segmentRaw)) return true
        val stripped = stripPercentOnlyParentheticals(needleRaw)
        if (stripped.isNotBlank() && isSubstringAnchored(stripped, segmentRaw)) return true
        val needle = normalizeMechanical(stripped).take(FUZZY_MAX_NEEDLE)
        if (needle.length < 5) return false
        val haystack = normalizeMechanical(stripPercentOnlyParentheticals(segmentRaw))
        if (haystack.length < 4) return false
        return fuzzyWordAnchored(needle, haystack) || fuzzyWindowAnchored(needle, haystack)
    }

    /**
     * Toutes les lignes d’ingrédients « checkables » (longueur ≥ [MIN_INGREDIENT_LINE_LENGTH])
     * doivent être ancrées dans le segment (**100 %**, pas de ratio partiel).
     */
    fun allCheckableIngredientLinesAnchored(
        ingredientLines: List<String>,
        segmentRaw: String,
    ): Boolean {
        val checkable = ingredientLines.map { it.trim() }.filter { it.length >= MIN_INGREDIENT_LINE_LENGTH }
        if (checkable.isEmpty()) return true
        return checkable.all { line -> isAnchoredInSegment(line, segmentRaw) }
    }

    private fun fuzzyMaxDist(needleLen: Int): Int =
        min(10, maxOf(3, needleLen / 2 + 3))

    private fun fuzzyWordAnchored(needle: String, haystack: String): Boolean {
        val maxDist = fuzzyMaxDist(needle.length)
        val tokens = haystack.split(Regex("[^a-zà-ÿ0-9]+")).filter { it.length >= 4 }
        for (w in tokens) {
            if (abs(w.length - needle.length) > maxDist + 2) continue
            if (levenshteinAtMost(needle, w, maxDist)) return true
        }
        return false
    }

    private fun fuzzyWindowAnchored(needle: String, haystack: String): Boolean {
        val maxDist = fuzzyMaxDist(needle.length)
        val len = needle.length
        val winMin = (len - 4).coerceAtLeast(4)
        val winMax = min(len + 6, haystack.length)
        if (haystack.length < winMin) return false
        for (winLen in winMin..winMax) {
            val step = maxOf(1, winLen / 5)
            var i = 0
            while (i + winLen <= haystack.length) {
                val window = haystack.substring(i, i + winLen)
                if (levenshteinAtMost(needle, window, maxDist)) return true
                i += step
            }
        }
        return false
    }

    /** Vrai si la distance de Levenshtein est ≤ [maxDist] (abandon anticipé si dépassement). */
    internal fun levenshteinAtMost(a: String, b: String, maxDist: Int): Boolean {
        if (a == b) return true
        if (abs(a.length - b.length) > maxDist) return false
        var prev = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val cur = IntArray(b.length + 1)
            cur[0] = prev[0] + 1
            var rowMin = cur[0]
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                cur[j + 1] = minOf(
                    prev[j + 1] + 1,
                    cur[j] + 1,
                    prev[j] + cost,
                )
                if (cur[j + 1] < rowMin) rowMin = cur[j + 1]
            }
            if (rowMin > maxDist) return false
            prev = cur
        }
        return prev[b.length] <= maxDist
    }
}
