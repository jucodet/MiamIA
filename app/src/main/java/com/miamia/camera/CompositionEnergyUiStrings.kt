package com.miamia.camera

/**
 * Libellés pastille estimation énergie (Feature K — **IHI-K-FR-003** / **UGE-A-FR-022**).
 */
object CompositionEnergyUiStrings {
    const val TITLE = "Analyse terminée"
    fun primaryLine(kcal: Int): String =
        "≈ $kcal kcal / 100 g — valeur estimée, indicative uniquement."
    const val UNAVAILABLE = "Estimation énergétique indisponible ou non fiable."
    const val HELPER =
        "Indication dérivée de la composition analysée, non valeur du tableau nutritionnel réglementaire."
}
