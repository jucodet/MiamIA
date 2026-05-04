package com.foodgpt.healthcritique

import java.util.UUID

class HealthCritiqueEngine(
    private val validator: HealthIngredientInputValidator = HealthIngredientInputValidator(),
    private val promptBuilder: HealthCritiquePromptBuilder = HealthCritiquePromptBuilder(),
    private val sectionParser: HealthCritiqueSectionParser = HealthCritiqueSectionParser(),
    private val llmRunner: HealthCritiqueLlmRunner,
) {

    suspend fun analyze(
        requestId: String = UUID.randomUUID().toString(),
        ingredientText: String,
        maxInferenceMs: Long = HealthCritiqueConfig.DEFAULT_MAX_INFERENCE_MS,
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
        val system = promptBuilder.buildSystemInstruction()
        val user = promptBuilder.buildUserMessage(ingredientText)
        return when (val out = llmRunner.generate(system, user, maxInferenceMs)) {
            is HealthCritiqueLlmGenerateResult.Success -> {
                val parsed = sectionParser.parse(out.text)
                HealthCritiqueResult.CritiqueReady(
                    requestId = requestId,
                    llmRawText = out.text,
                    sections = parsed.sections,
                    parseWarnings = parsed.warnings,
                    disclaimer = HealthCritiquePromptBuilder.DISCLAIMER,
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
