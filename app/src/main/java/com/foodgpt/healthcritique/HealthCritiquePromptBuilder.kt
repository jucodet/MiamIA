package com.foodgpt.healthcritique

/**
 * Construit le couple instruction système / message utilisateur pour la critique santé (FR-002, FR-003, US2).
 * Les formulations prudence / grossesse / ambiguïté sont regroupées ici pour faciliter les tests unitaires (T014).
 */
class HealthCritiquePromptBuilder {

    companion object {
        const val DISCLAIMER: String =
            "Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé."
    }

    fun buildSystemInstruction(): String = buildString {
        appendLine("Tu aides à lire une liste d’ingrédients alimentaires (contexte UE, français).")
        appendLine("Objectif : critique aussi objective que possible de l’impact **potentiel** sur la santé, sans diagnostic.")
        appendLine("Tu dois distinguer clairement : faits établis, incertitudes scientifiques, et hypothèses.")
        appendLine("Évite les conclusions catégoriques (« toujours toxique », « interdit ») ; nuance selon les doses et le contexte.")
        appendLine("Pour les termes ambigus (ex. « arômes », additifs sans précision), signale l’ambiguïté et l’impact sur la confiance de l’analyse.")
        appendLine("Ne pose pas de diagnostic médical ; n’écris pas de prescription ; oriente vers un professionnel de santé en cas de doute ou grossesse.")
        appendLine("Pour la population « femmes enceintes », insiste sur la prudence et la consultation d’un professionnel si nécessaire.")
        appendLine("Réponds **uniquement** avec les marqueurs de section suivants (lignes exactes, dans cet ordre) :")
        appendLine("###ENFANTS")
        appendLine("###FEMMES_ENCEINTES")
        appendLine("###ADULTES")
        appendLine("###PERSONNES_AGEES")
        appendLine("Sous chaque marqueur, rédige un bloc structuré contenant obligatoirement :")
        appendLine("1) Points de vigilance (liste courte).")
        appendLine("2) Explication / nuances (faits vs incertitudes).")
        appendLine("3) Niveau de prudence (faible / modéré / élevé) avec justification prudente.")
        appendLine("Si la liste est dans une autre langue ou illisible, reste structuré et demande des précisions dans chaque section concernée.")
        appendLine("Si la liste est très longue, reste lisible : synthèse en tête de chaque section puis détails.")
        appendLine("Si l’utilisateur demande un diagnostic ou des conseils médicaux personnalisés, refuse poliment et rappelle qu’un professionnel de santé est requis.")
        appendLine("Aucun texte avant la ligne ###ENFANTS.")
    }

    fun buildUserMessage(ingredientList: String): String = buildString {
        appendLine("Liste d’ingrédients à analyser :")
        appendLine(
            ingredientList.trim().take(HealthCritiqueConfig.MAX_INGREDIENT_TEXT_CHARS)
        )
    }
}
