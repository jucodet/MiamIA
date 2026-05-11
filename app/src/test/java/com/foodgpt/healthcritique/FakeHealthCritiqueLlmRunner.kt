package com.foodgpt.healthcritique

class FakeHealthCritiqueLlmRunner(
    private val response: HealthCritiqueLlmGenerateResult,
    private val recordedUserMessages: MutableList<String> = mutableListOf(),
) : HealthCritiqueLlmRunner {

    fun lastUserMessage(): String? = recordedUserMessages.lastOrNull()

    override suspend fun generate(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?,
    ): HealthCritiqueLlmGenerateResult {
        recordedUserMessages.add(userMessage)
        return response
    }
}
