package com.miamia.healthcritique

/**
 * Parse une sortie critique **à profil unique** (Feature N, 2026-06-28).
 *
 * Supersède l'ancien parseur 4-sections (Feature L retiré). Extrait, pour le profil
 * sélectionné :
 *  - le rappel « Évalué pour vous : <label> » ;
 *  - le bloc **Niveau de prudence** (palier Faible/Modéré/Élevé + texte court) ;
 *  - les **cartes d'ingrédients à vigilance** (• nom | code | type + Impact/Fait établi/Nuance/Cible particulièrement) ;
 *  - la **liste compacte** des ingrédients analysés (- nom : RAS/Modéré/Élevé).
 *
 * Rejet : si la sortie contient ≥ 2 marqueurs du format legacy 4-profils
 * (`###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`),
 * `isRejectedLegacy4Markers = true` (→ `non-analysable-response` côté engine — IHI-N-FR-013).
 */
class HealthCritiqueSectionParser {

    private val prudenceRegex =
        Regex("Niveau de prudence\\s*:\\s*(Faible|Modéré|Élevé)(?:\\s*[—-]\\s*(.+))?", RegexOption.IGNORE_CASE)

    private val cardHeaderRegex =
        Regex("^[•\\-*]\\s*(.+?)\\s*\\|\\s*(.*?)\\s*\\|\\s*(.+)$")

    private val fullListLineRegex =
        Regex("^-\\s*(.+?)\\s*:\\s*(RAS|Modéré|Élevé)$", RegexOption.IGNORE_CASE)

    fun parse(rawLlmText: String, profile: UserProfile): ProfileCritiqueResult {
        val text = rawLlmText.trim()
        val warnings = mutableListOf<String>()

        val legacyHits = UserProfile.legacyFourProfileMarkers.count { text.contains(it) }
        val isRejectedLegacy4Markers = legacyHits >= 2
        if (isRejectedLegacy4Markers) {
            warnings.add("Sortie 4-profils legacy détectée ($legacyHits marqueurs) — format rejeté (Feature N : profil unique attendu).")
        }

        val expectedHeader = UserProfile.evaluatedForHeader(profile)
        if (!text.contains(expectedHeader, ignoreCase = true)) {
            warnings.add("Rappel de profil absent : « $expectedHeader ».")
        }

        val markerIdx = text.indexOf(profile.marker)
        if (markerIdx < 0) {
            warnings.add("Marqueur de profil absent : ${profile.marker}.")
        }

        val prudenceLevel: PrudenceLevel?
        val prudenceJustification: String?
        val prudenceMatch = prudenceRegex.find(text)
        if (prudenceMatch != null) {
            prudenceLevel = PrudenceLevel.parseOrNull(prudenceMatch.groupValues[1])
            prudenceJustification = prudenceMatch.groupValues[2].trim().takeIf { it.isNotBlank() }
            if (prudenceLevel == null) {
                warnings.add("Niveau de prudence non reconnu : ${prudenceMatch.groupValues[1]}.")
            }
        } else {
            prudenceLevel = null
            prudenceJustification = null
            warnings.add("Niveau de prudence absent.")
        }

        val riskCards = parseRiskCards(text)
        val fullIngredientList = parseFullList(text)

        return ProfileCritiqueResult(
            profile = profile,
            evaluatedForHeader = expectedHeader,
            prudenceLevel = prudenceLevel,
            prudenceJustification = prudenceJustification,
            riskCards = riskCards,
            fullIngredientList = fullIngredientList,
            warnings = warnings,
            isRejectedLegacy4Markers = isRejectedLegacy4Markers,
        )
    }

    private fun parseRiskCards(text: String): List<IngredientRiskCard> {
        val lines = text.lines()
        val cards = mutableListOf<IngredientRiskCard>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val headerMatch = cardHeaderRegex.matchEntire(line)
            if (headerMatch != null) {
                val nom = headerMatch.groupValues[1].trim()
                val code = headerMatch.groupValues[2].trim().takeIf { it.isNotBlank() && it != "-" }
                val type = headerMatch.groupValues[3].trim()
                var impact = ""
                var faitEtabli = ""
                var nuance = ""
                var cible = ""
                var j = i + 1
                while (j < lines.size) {
                    val sub = lines[j].trim()
                    if (sub.isEmpty()) { j++; break }
                    if (cardHeaderRegex.matches(sub) || fullListLineRegex.matches(sub)) break
                    when {
                        sub.startsWith("Impact :", ignoreCase = true) ->
                            impact = sub.substringAfter("Impact:", "").trim()
                        sub.startsWith("Fait établi :", ignoreCase = true) ->
                            faitEtabli = sub.substringAfter("Fait établi:", "").trim()
                        sub.startsWith("Nuance :", ignoreCase = true) ->
                            nuance = sub.substringAfter("Nuance:", "").trim()
                        sub.startsWith("Cible particulièrement :", ignoreCase = true) ->
                            cible = sub.substringAfter("Cible particulièrement:", "").trim()
                    }
                    j++
                }
                cards.add(
                    IngredientRiskCard(
                        nom = nom,
                        code = code,
                        type = type,
                        impact = impact,
                        faitEtabli = faitEtabli,
                        nuance = nuance,
                        cibleParticulierement = cible,
                    )
                )
                i = j
            } else {
                i++
            }
        }
        return cards
    }

    private fun parseFullList(text: String): List<FullIngredientStatutEntry> {
        val entries = mutableListOf<FullIngredientStatutEntry>()
        val inListSection = text.contains("Liste complète des ingrédients analysés", ignoreCase = true)
        if (!inListSection) return entries
        for (line in text.lines()) {
            val m = fullListLineRegex.matchEntire(line.trim())
            if (m != null) {
                val statut = IngredientVigilanceStatut.parseOrNull(m.groupValues[2])
                if (statut != null) {
                    entries.add(FullIngredientStatutEntry(nom = m.groupValues[1].trim(), statut = statut))
                }
            }
        }
        return entries
    }
}
