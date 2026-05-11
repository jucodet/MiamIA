package com.foodgpt.gemma4local

import com.foodgpt.composition.CompositionMessages
import com.foodgpt.composition.GemmaErrorCode
import com.foodgpt.gemma4local.model.AnalyseTextuelleErrorType
import java.io.IOException
import java.util.concurrent.TimeoutException
import kotlin.coroutines.cancellation.CancellationException

data class Gemma4LocalMappedError(
    val errorType: AnalyseTextuelleErrorType,
    val gemmaCode: GemmaErrorCode,
    val userMessage: String
)

class Gemma4LocalErrorMapper {
    fun map(error: Throwable): Gemma4LocalMappedError {
        return when {
            error is CancellationException -> Gemma4LocalMappedError(
                AnalyseTextuelleErrorType.TIMEOUT,
                GemmaErrorCode.GEMMA_TIMEOUT,
                CompositionMessages.GEMMA_TIMEOUT_USER
            )
            error is TimeoutException -> Gemma4LocalMappedError(
                AnalyseTextuelleErrorType.TIMEOUT,
                GemmaErrorCode.GEMMA_TIMEOUT,
                CompositionMessages.GEMMA_TIMEOUT_USER
            )
            error is IllegalStateException &&
                error.message?.contains("Modele Gemma local indisponible") == true ->
                Gemma4LocalMappedError(
                    AnalyseTextuelleErrorType.API_UNAVAILABLE,
                    GemmaErrorCode.GEMMA_NOT_FOUND,
                    Gemma4LocalMessages.MODEL_UNAVAILABLE
                )
            error is IllegalStateException -> Gemma4LocalMappedError(
                AnalyseTextuelleErrorType.UNKNOWN,
                GemmaErrorCode.GEMMA_LOAD_FAILED,
                Gemma4LocalMessages.MODEL_EXECUTION_FAILED
            )
            error is IOException -> Gemma4LocalMappedError(
                AnalyseTextuelleErrorType.NETWORK_LOCAL,
                GemmaErrorCode.GEMMA_LOAD_FAILED,
                Gemma4LocalMessages.LOCAL_CONNECTION_FAILED
            )
            else -> Gemma4LocalMappedError(
                AnalyseTextuelleErrorType.UNKNOWN,
                GemmaErrorCode.GEMMA_LOAD_FAILED,
                CompositionMessages.GEMMA_LOAD_FAILED_USER
            )
        }
    }
}
