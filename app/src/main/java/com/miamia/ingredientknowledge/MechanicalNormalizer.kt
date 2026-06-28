package com.miamia.ingredientknowledge

import java.text.Normalizer

/**
 * Normalisations **mécaniques et explicitement listées** admises pour le lookup (IKB-A-FR-002) :
 *  - casse → minuscules,
 *  - espaces → collapse en un seul espace + trim,
 *  - accents → forme ASCII (NFD + strip des combining marks).
 *
 * Aucune autre transformation (pas de table d'alias orthographiques étendue — IKB-A-FR-006).
 * Cohérent avec IHI-C-FR-005 (normalisations mécaniques listées du core).
 */
object MechanicalNormalizer {

    fun normalize(text: String): String {
        if (text.isEmpty()) return text
        val lower = text.lowercase()
        val noAccents = stripAccents(lower)
        return noAccents.replace(Regex("\\s+"), " ").trim()
    }

    private fun stripAccents(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
