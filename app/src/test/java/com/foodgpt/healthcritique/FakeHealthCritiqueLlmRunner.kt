package com.foodgpt.healthcritique

class FakeHealthCritiqueLlmRunner(
    private val response: HealthCritiqueLlmGenerateResult,
) : HealthCritiqueLlmRunner {
    override suspend fun generate(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
    ): HealthCritiqueLlmGenerateResult = response
}
