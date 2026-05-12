package com.miamia.healthcritique

data class ParsedHealthSections(
    val sections: Map<PopulationKey, String>,
    val warnings: List<String>,
)

class HealthCritiqueSectionParser {

    private val orderedMarkers: List<Pair<PopulationKey, String>> = listOf(
        PopulationKey.ENFANTS to "###ENFANTS",
        PopulationKey.FEMMES_ENCEINTES to "###FEMMES_ENCEINTES",
        PopulationKey.ADULTES to "###ADULTES",
        PopulationKey.PERSONNES_AGEES to "###PERSONNES_AGEES",
    )

    fun parse(rawLlmText: String): ParsedHealthSections {
        val text = rawLlmText.trim()
        val warnings = mutableListOf<String>()
        val sections = mutableMapOf<PopulationKey, String>()
        for (key in PopulationKey.entries) {
            sections[key] = ""
        }
        for (i in orderedMarkers.indices) {
            val (key, marker) = orderedMarkers[i]
            val startIdx = text.indexOf(marker)
            if (startIdx < 0) {
                warnings.add("Section manquante ou marqueur absent : $marker")
                continue
            }
            val contentStart = startIdx + marker.length
            val endIdx = if (i + 1 < orderedMarkers.size) {
                val nextMarker = orderedMarkers[i + 1].second
                val next = text.indexOf(nextMarker, startIndex = contentStart)
                if (next < 0) {
                    warnings.add("Marqueur suivant introuvable après $marker — texte peut être tronqué.")
                    text.length
                } else {
                    next
                }
            } else {
                text.length
            }
            sections[key] = text.substring(contentStart, endIdx).trim()
        }
        return ParsedHealthSections(sections = sections.toMap(), warnings = warnings.toList())
    }
}
