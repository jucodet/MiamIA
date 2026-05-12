package com.miamia.gemma4local

import com.miamia.gemma4local.model.AnalyseTextuelleRequest

class Gemma4LocalRequestMapper {
    fun map(rawText: String, sourceScreen: String? = null): AnalyseTextuelleRequest {
        val normalized = rawText.trim().replace(Regex("\\s+"), " ")
        require(normalized.isNotBlank()) { "inputText must not be blank" }
        val bounded = normalized.take(Gemma4LocalConfig.MAX_INPUT_CHARS)
        return AnalyseTextuelleRequest(
            inputText = bounded,
            sourceScreen = sourceScreen
        )
    }
}
