package com.miamia.healthcritique

/** Copie ciblée pour [androidTest] : les sources `test/` ne sont pas sur le classpath instrumenté. */
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
