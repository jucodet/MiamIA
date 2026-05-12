package com.miamia.analysis

import com.miamia.composition.CompositionMessages
import com.miamia.composition.GemmaErrorCode
import com.miamia.gemma4local.Gemma4LocalMessages

object Gemma4LocalUiMessageResolver {
    fun resolve(code: GemmaErrorCode, backendMessage: String): String {
        return when (code) {
            GemmaErrorCode.GEMMA_NOT_FOUND -> backendMessage.ifBlank {
                Gemma4LocalMessages.MODEL_UNAVAILABLE
            }
            GemmaErrorCode.GEMMA_TIMEOUT -> CompositionMessages.GEMMA_TIMEOUT_USER
            GemmaErrorCode.GEMMA_LOAD_FAILED -> backendMessage.ifBlank {
                CompositionMessages.GEMMA_LOAD_FAILED_USER
            }
        }
    }
}
