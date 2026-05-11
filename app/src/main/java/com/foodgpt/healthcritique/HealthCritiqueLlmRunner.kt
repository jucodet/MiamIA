package com.foodgpt.healthcritique

sealed class HealthCritiqueLlmGenerateResult {
    data class Success(val text: String) : HealthCritiqueLlmGenerateResult()

    data class Failure(
        val code: HealthInferenceErrorCode,
        val message: String,
    ) : HealthCritiqueLlmGenerateResult()
}

fun interface HealthCritiqueLlmRunner {
    suspend fun generate(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)? = null,
    ): HealthCritiqueLlmGenerateResult
}
