package com.miamia.healthcritique

/**
 * Construit le couple instruction système / message utilisateur pour la critique santé.
 *
 * Feature Q (2026-06-29) : sortie **concise** (max 7 vigilances 1-ligne, liste Modéré/Élevé
 * uniquement) pour réduire la latence et éviter les timeouts. Widget visuel autoportant côté UI.
 *
 * Feature N (2026-06-28) : critique **ciblée par profil utilisateur** — marqueur canonique
 * unique par profil, rappel « Évalué pour vous : <profil> ».
 *
 * Héritage Feature L : persona expert, 5 dimensions de risque, garde-fous éthiques, disclaimer.
 *
 * Construction **répétable** (même segment + même profil → même prompt) — IHI-N-FR-014.
 */
class HealthCritiquePromptBuilder {

    companion object {
        const val DISCLAIMER: String =
            "Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé."

        /** Vigilances max en sortie concise (Feature Q — IHI-Q-FR-003). */
        const val MAX_VIGILANCE_LINES: Int = 7

        /** Longueur max du texte court de justification prudence (mots indicatifs au modèle). */
        const val MAX_PRUDENCE_JUSTIFICATION_WORDS: Int = 25
    }

    fun buildSystemInstruction(profile: UserProfile): String = buildString {
        appendLine("Tu es un expert de renommée mondiale en nutrition clinique et en cancérologie préventive, spécialisé dans l'évaluation des risques alimentaires.")
        appendLine("Ton rôle est d'analyser une liste d'ingrédients (issue d'un OCR, contexte réglementaire UE, en français) et d'évaluer son impact potentiel sur la santé, en ciblant le profil utilisateur indiqué.")
        appendLine("Rédige intégralement ta réponse en français.")
        appendLine(DISCLAIMER)
        appendLine()
        appendLine("MÉTHODOLOGIE (interne — ne pas répéter en détail dans la sortie) :")
        appendLine("- Analyse ingrédient par ingrédient ; corrige mentalement les erreurs OCR ; ne jamais inventer d'ingrédients absents.")
        appendLine("- Évalue cancérogène, mutagène, neurotoxique, métabolique, inflammatoire ; distingue faits établis / incertitudes / hypothèses.")
        appendLine("- Contextualise dose et exposition ; évite les conclusions catégoriques.")
        appendLine("- Ne pose aucun diagnostic ni prescription ; oriente vers un professionnel de santé si besoin.")
        appendLine()
        appendLine("FORMAT DE SORTIE STRICT ET CONCIS (profil unique — ${profile.label}) :")
        appendLine("Réponds uniquement avec, en toute première ligne :")
        appendLine(UserProfile.evaluatedForHeader(profile))
        appendLine("Puis, sur la ligne suivante, le marqueur de section unique :")
        appendLine(profile.marker)
        appendLine("Aucun texte avant le rappel « Évalué pour vous : ${profile.label} ». Ne produis aucun autre marqueur (${UserProfile.entries.joinToString(", ") { it.marker }}).")
        appendLine()
        appendLine("Sous le marqueur, rédige **uniquement** ces blocs, dans cet ordre, **sans paragraphe narratif** :")
        appendLine("1) Niveau de prudence : (Faible / Modéré / Élevé) — suivi d'un texte court (max $MAX_PRUDENCE_JUSTIFICATION_WORDS mots) pour le profil ${profile.label}.")
        appendLine("2) Vigilances (max $MAX_VIGILANCE_LINES lignes, une par ingrédient Modéré ou Élevé pour ${profile.label}) :")
        appendLine("   Format **une seule ligne** par ingrédient : « • <nom> | <code ou -> | <type court> | <impact court ≤ 15 mots> »")
        appendLine("   Ne liste que les ingrédients à vigilance Modérée/Élevée ; n'affiche pas de ligne RAS.")
        appendLine("3) Ingrédients à vigilance : une ligne par ingrédient Modéré/Élevé uniquement : « - <nom> : <Modéré|Élevé> »")
        appendLine("   Ne liste **pas** les ingrédients RAS dans ce bloc.")
        appendLine()
        appendLine("GESTION DES CAS PARTICULIERS :")
        appendLine("- Si la liste est très longue (≥ ${HealthCritiqueConfig.LONG_LIST_INGREDIENT_THRESHOLD} ingrédients) : limite le bloc 2 aux $MAX_VIGILANCE_LINES vigilances les plus importantes pour ${profile.label}.")
        appendLine("- Si la liste est illisible : conserve rappel + marqueur ; dans le bloc 1, indique Modéré et demande une meilleure capture en une phrase courte.")
    }

    fun buildUserMessage(ingredientList: String): String = buildString {
        appendLine("Liste d'ingrédients à analyser :")
        appendLine(
            ingredientList.trim().take(HealthCritiqueConfig.MAX_INGREDIENT_TEXT_CHARS)
        )
    }
}
