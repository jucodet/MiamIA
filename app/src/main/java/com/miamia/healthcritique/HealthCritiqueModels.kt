package com.miamia.healthcritique

enum class PopulationKey {
    ENFANTS,
    FEMMES_ENCEINTES,
    ADULTES,
    PERSONNES_AGEES,
}

enum class InputInvalidReason {
    EMPTY,
    TOO_SHORT,
    /** Aucun segment ingrédients validé issu du scan (contrat `no_validated_segment`). */
    NO_VALIDATED_SEGMENT,
}

enum class HealthInferenceErrorCode {
    GEMMA_NOT_FOUND,
    GEMMA_LOAD_FAILED,
    GEMMA_TIMEOUT,
    INFERENCE_FAILED,
}

/**
 * Niveau de prudence du profil sélectionné (Feature N — IHI-N-FR-007).
 */
enum class PrudenceLevel(val label: String) {
    FAIBLE("Faible"),
    MODERE("Modéré"),
    ELEVE("Élevé"),
    ;

    companion object {
        fun parseOrNull(token: String): PrudenceLevel? =
            values().firstOrNull { it.label.equals(token.trim(), ignoreCase = true) }
    }
}

/**
 * Statut de vigilance d'un ingrédient pour le profil sélectionné (Feature N).
 */
enum class IngredientVigilanceStatut(val label: String) {
    RAS("RAS"),
    MODERE("Modéré"),
    ELEVE("Élevé"),
    ;

    companion object {
        fun parseOrNull(token: String): IngredientVigilanceStatut? =
            values().firstOrNull { it.label.equals(token.trim(), ignoreCase = true) }
    }
}

/**
 * Carte d'un ingrédient à vigilance (Modérée/Élevée) pour le profil sélectionné
 * (Feature N — IHI-N-FR-008). `nom` doit être ancrable dans le `ValidatedIngredientSegment`.
 */
data class IngredientRiskCard(
    val nom: String,
    val code: String?,
    val type: String,
    val impact: String,
    val faitEtabli: String,
    val nuance: String,
    val cibleParticulierement: String,
)

/**
 * Ligne de la liste compacte « Voir tous les ingrédients analysés »
 * (Feature N — IHI-N-FR-011).
 */
data class FullIngredientStatutEntry(
    val nom: String,
    val statut: IngredientVigilanceStatut,
)

/**
 * Résultat parsé d'une sortie critique à profil unique (Feature N).
 * Supersède `ParsedHealthSections` (4-sections, Feature L retiré).
 */
data class ProfileCritiqueResult(
    val profile: UserProfile,
    val evaluatedForHeader: String,
    val prudenceLevel: PrudenceLevel?,
    val prudenceJustification: String?,
    val riskCards: List<IngredientRiskCard>,
    val fullIngredientList: List<FullIngredientStatutEntry>,
    val warnings: List<String>,
    /** Vrai si la sortie contient ≥ 2 marqueurs du format legacy 4-profils (à rejeter). */
    val isRejectedLegacy4Markers: Boolean,
)

sealed class HealthCritiqueResult {
    data class CritiqueReady(
        val requestId: String,
        val llmRawText: String,
        val profile: UserProfile,
        val profileCritique: ProfileCritiqueResult,
        val parseWarnings: List<String>,
        val disclaimer: String,
        val isDefaultProfile: Boolean,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()

    data class InferenceError(
        val requestId: String,
        val errorCode: HealthInferenceErrorCode,
        val message: String,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()

    data class InputInvalid(
        val requestId: String,
        val reasonCode: InputInvalidReason,
        val message: String,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()
}

data class LastHealthAnalysisSnapshot(
    val savedAtEpochMs: Long,
    val ingredientRaw: String,
    val resultRaw: String,
    val systemPromptSnapshot: String,
)
