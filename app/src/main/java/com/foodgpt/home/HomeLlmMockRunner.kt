package com.foodgpt.home

import com.foodgpt.analysis.AnalysisInputBuilder
import com.foodgpt.composition.AnalyzeCompositionResult
import com.foodgpt.composition.CompositionAnalysisEngine
import com.foodgpt.composition.GemmaErrorCode
import kotlinx.coroutines.withTimeoutOrNull

private const val HOMEPAGE_LLM_TIMEOUT_MS = 30_000L

interface HomeLlmMockRunner {
    suspend fun run(): HomeLlmMockOutcome
}

class CompositionEngineHomeLlmMockRunner(
    private val compositionEngine: CompositionAnalysisEngine
) : HomeLlmMockRunner {

    override suspend fun run(): HomeLlmMockOutcome {
        val payload = AnalysisInputBuilder.buildSegmentPayload(MOCK_INGREDIENTS_INPUT)
        val result = withTimeoutOrNull(HOMEPAGE_LLM_TIMEOUT_MS) {
            compositionEngine.analyze(payload, HOMEPAGE_LLM_TIMEOUT_MS)
        } ?: return HomeLlmMockOutcome.Failure(
            category = HomeLlmFailureCategory.TIMEOUT,
            message = "Le test a depasse 30 secondes sans reponse exploitable."
        )

        return when (result) {
            is AnalyzeCompositionResult.BilanSuccess -> {
                val response = buildString {
                    appendLine("###LISTE")
                    result.bilan.ingredientLines.forEach { appendLine("- $it") }
                    appendLine("###ANALYSE")
                    appendLine(result.bilan.compositionAnalysis)
                    appendLine("###DISCLAIMER")
                    append(result.bilan.disclaimer)
                }
                HomeLlmMockOutcome.Success(response)
            }
            is AnalyzeCompositionResult.CompositionLimit ->
                HomeLlmMockOutcome.Failure(
                    category = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE,
                    message = result.message
                )
            is AnalyzeCompositionResult.GemmaError -> {
                val category = if (result.code == GemmaErrorCode.GEMMA_TIMEOUT) {
                    HomeLlmFailureCategory.TIMEOUT
                } else {
                    HomeLlmFailureCategory.RUNTIME_UNAVAILABLE
                }
                HomeLlmMockOutcome.Failure(category = category, message = result.message)
            }
        }
    }
}

const val MOCK_INGREDIENTS_INPUT: String =
    "Ingredients. Sucre, farine de BLE 33 %, farine complete de BLE 15 %, huile de palme, " +
        "huile de colza, amidon de BLE, sirop de glucose, poudres a lever (carbonates d'ammonium, " +
        "carbonates de sodium), emulsifiant (lecithines de SOJA), sel, LAIT ecreme en poudre, " +
        "LAIT entier en poudre, aromes."

