package com.miamia.healthcritique

import com.miamia.composition.SegmentAnchoringV1

/**
 * Garde-fous d’ancrage minimal pour la critique santé (post-inférence).
 * V1 : codes **E** mentionnés dans les sections doivent apparaître sur le segment validé.
 */
object HealthCritiqueAnchoring {

    private val eNumberRegex = Regex("\\bE\\d{2,4}[a-z]?\\b", RegexOption.IGNORE_CASE)

    /** Codes E cités dans la critique mais absents du segment (normalisation mécanique v1). */
    fun unanchoredENumbers(segment: String, sections: Map<PopulationKey, String>): List<String> {
        val hay = SegmentAnchoringV1.normalizeMechanical(segment)
        val seen = mutableSetOf<String>()
        val missing = mutableListOf<String>()
        val blob = sections.values.joinToString("\n")
        for (m in eNumberRegex.findAll(blob)) {
            val token = m.value
            if (!seen.add(token)) continue
            val norm = SegmentAnchoringV1.normalizeMechanical(token)
            if (norm.isNotEmpty() && !hay.contains(norm)) {
                missing += token
            }
        }
        return missing
    }
}
