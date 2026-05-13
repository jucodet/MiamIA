package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Aligné sur `specs/009-llm-bilan-composition-ingredients/contracts/gemma-composition-bilan-contract.md`.
 */
class GemmaCompositionContractMappingTest {

    @Test
    fun bilan_ready_contractState() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau"),
            compositionAnalysis = "Hydratation.",
            disclaimer = "info"
        )
        val r: AnalyzeCompositionResult = AnalyzeCompositionResult.BilanSuccess(bilan, rawModelOutput = "")
        assertTrue(r is AnalyzeCompositionResult.BilanSuccess)
        val mapped = when (r) {
            is AnalyzeCompositionResult.BilanSuccess -> "bilan_ready"
            is AnalyzeCompositionResult.GemmaError -> "gemma_error"
            is AnalyzeCompositionResult.CompositionLimit -> "composition_limit"
        }
        assertEquals("bilan_ready", mapped)
    }

    @Test
    fun gemma_error_contractState() {
        val r: AnalyzeCompositionResult = AnalyzeCompositionResult.GemmaError(
            GemmaErrorCode.GEMMA_NOT_FOUND,
            "Gemma introuvable"
        )
        val mapped = when (r) {
            is AnalyzeCompositionResult.BilanSuccess -> "bilan_ready"
            is AnalyzeCompositionResult.GemmaError -> "gemma_error"
            is AnalyzeCompositionResult.CompositionLimit -> "composition_limit"
        }
        assertEquals("gemma_error", mapped)
        val err = r as AnalyzeCompositionResult.GemmaError
        assertEquals(GemmaErrorCode.GEMMA_NOT_FOUND, err.code)
    }
}
