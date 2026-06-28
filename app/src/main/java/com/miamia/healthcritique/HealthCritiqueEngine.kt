package com.miamia.healthcritique

import com.miamia.BuildConfig
import java.util.UUID

class HealthCritiqueEngine(
    private val validator: HealthIngredientInputValidator = HealthIngredientInputValidator(),
    private val promptBuilder: HealthCritiquePromptBuilder = HealthCritiquePromptBuilder(),
    private val sectionParser: HealthCritiqueSectionParser = HealthCritiqueSectionParser(),
    private val llmRunner: HealthCritiqueLlmRunner,
) {

    /**
     * Analyse la critique santé pour le segment courant et le [profile] sélectionné
     * (Feature N — profil unique). En l'absence de profil explicite, l'appelant MUST
     * passer [UserProfile.DEFAULT] (Adulte) — fallback signalé en UI via `isDefaultProfile`.
     *
     * L'appelant MUST transmettre [ingredientText] **identique** au segment ingrédients
     * validé affiché en lecture seule (SC-005), ou [null] s'il n'y a pas encore de segment
     * issu du bilan.
     */
    suspend fun analyze(
        requestId: String = UUID.randomUUID().toString(),
        ingredientText: String?,
        profile: UserProfile = UserProfile.DEFAULT,
        maxInferenceMs: Long = HealthCritiqueConfig.DEFAULT_MAX_INFERENCE_MS,
        onStreamPartial: ((String) -> Unit)? = null,
    ): HealthCritiqueResult {
        val now = System.currentTimeMillis()
        when (val v = validator.validate(ingredientText)) {
            is HealthIngredientValidation.Invalid ->
                return HealthCritiqueResult.InputInvalid(
                    requestId = requestId,
                    reasonCode = v.reason,
                    message = v.message,
                    processedAtEpochMs = now,
                )

            HealthIngredientValidation.Valid -> Unit
        }
        val canonicalList = ingredientText!!.trim()
        if (BuildConfig.DEBUG) {
            val userPreview = promptBuilder.buildUserMessage(canonicalList)
            check(userPreview.contains(canonicalList)) {
                "SC-005 : le message utilisateur doit contenir la liste canonique affichée."
            }
        }
        val system = promptBuilder.buildSystemInstruction(profile)
        val user = promptBuilder.buildUserMessage(canonicalList)
        return when (val out = llmRunner.generate(system, user, maxInferenceMs, onStreamPartial)) {
            is HealthCritiqueLlmGenerateResult.Success -> {
                val parsed = sectionParser.parse(out.text, profile)
                if (parsed.isRejectedLegacy4Markers) {
                    return HealthCritiqueResult.InferenceError(
                        requestId = requestId,
                        errorCode = HealthInferenceErrorCode.INFERENCE_FAILED,
                        message = "Sortie 4-profils legacy rejetée (format profil unique attendu — Feature N).",
                        processedAtEpochMs = now,
                    )
                }
                val missingE = HealthCritiqueAnchoring.unanchoredENumbers(canonicalList, out.text)
                if (missingE.isNotEmpty()) {
                    return HealthCritiqueResult.InferenceError(
                        requestId = requestId,
                        errorCode = HealthInferenceErrorCode.INFERENCE_FAILED,
                        message = "Analyse non vérifiable depuis l'étiquette (mentions additives : ${
                            missingE.distinct().joinToString()
                        }).",
                        processedAtEpochMs = now,
                    )
                }
                HealthCritiqueResult.CritiqueReady(
                    requestId = requestId,
                    llmRawText = out.text,
                    profile = profile,
                    profileCritique = parsed,
                    parseWarnings = parsed.warnings,
                    disclaimer = HealthCritiquePromptBuilder.DISCLAIMER,
                    isDefaultProfile = profile == UserProfile.DEFAULT,
                    processedAtEpochMs = now,
                )
            }

            is HealthCritiqueLlmGenerateResult.Failure ->
                HealthCritiqueResult.InferenceError(
                    requestId = requestId,
                    errorCode = out.code,
                    message = out.message,
                    processedAtEpochMs = now,
                )
        }
    }
}
