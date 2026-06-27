package com.miamia.ingredientknowledge

/**
 * Niveau de risque indicatif d'un additif, dérivé des étiquettes de risque OpenFoodFacts.
 * Échelle fixe à 3 niveaux (IKB-A-FR-013). Utilisé pour la priorisation d'injection (IKB-A-FR-011).
 */
enum class RiskLevel {
    FAIBLE,
    MODERE,
    ELEVE;
}

/**
 * Ordre de priorisation pour l'injection dans le [ReferenceContext] :
 * allergènes d'abord, puis additifs par risque décroissant (IKB-A-FR-011).
 */
internal val RiskLevel.priorityOrder: Int
    get() = when (this) {
        RiskLevel.ELEVE -> 0
        RiskLevel.MODERE -> 1
        RiskLevel.FAIBLE -> 2
    }
