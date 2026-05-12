package com.miamia.composition

interface CompositionAnalysisEngine {
    /**
     * @param onStreamPartial Texte agrégé reçu au fil de l’eau (LiteRT-LM) ; ignoré si null.
     */
    suspend fun analyze(
        rawText: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)? = null
    ): AnalyzeCompositionResult
}
