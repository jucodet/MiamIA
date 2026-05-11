package com.foodgpt.gemma4local

import android.os.SystemClock
import android.util.Log
import com.foodgpt.gemma4local.model.AnalyseTextuelleErrorType
import com.foodgpt.gemma4local.model.AnalyseTextuelleResult
import com.foodgpt.gemma4local.model.AnalyseTextuelleStatus
import com.foodgpt.gemma4local.model.ApiCallMetric
import kotlinx.coroutines.withTimeout

fun interface Gemma4LocalApiGateway {
    suspend fun analyzeText(inputText: String): String
}

class Gemma4LocalClient(
    private val availabilityChecker: Gemma4LocalAvailabilityChecker,
    private val requestMapper: Gemma4LocalRequestMapper,
    private val errorMapper: Gemma4LocalErrorMapper,
    private val metricsLogger: Gemma4LocalMetricsLogger,
    private val deviceClassResolver: DeviceClassResolver,
    private val gateway: Gemma4LocalApiGateway
) {
    suspend fun analyze(rawText: String, onStreamPartial: ((String) -> Unit)? = null): AnalyseTextuelleResult {
        val request = requestMapper.map(rawText, sourceScreen = "camera")
        val started = SystemClock.elapsedRealtime()

        val availability = availabilityChecker.check()
        if (!availability.available) {
            Log.w(
                TAG,
                "availability_non_blocking requestId=${request.requestId} issue=${availability.issue} details=${availability.details}"
            )
        }

        return try {
            val output = withTimeout(Gemma4LocalConfig.DEFAULT_TIMEOUT_MS) {
                if (onStreamPartial != null && gateway is HybridGemma4LocalGateway) {
                    gateway.analyzeTextStreaming(request.inputText, onStreamPartial)
                } else {
                    gateway.analyzeText(request.inputText)
                }
            }.trim()
            val latency = SystemClock.elapsedRealtime() - started
            val result = if (output.isNotEmpty()) {
                AnalyseTextuelleResult(
                    requestId = request.requestId,
                    status = AnalyseTextuelleStatus.SUCCESS,
                    outputText = output
                )
            } else {
                AnalyseTextuelleResult(
                    requestId = request.requestId,
                    status = AnalyseTextuelleStatus.FAILED,
                    errorType = AnalyseTextuelleErrorType.INVALID_RESPONSE,
                    userMessage = Gemma4LocalMessages.INVALID_RESPONSE
                )
            }
            metricsLogger.log(
                ApiCallMetric(
                    requestId = request.requestId,
                    outcome = result.status,
                    latencyMs = latency,
                    errorType = result.errorType,
                    deviceClass = deviceClassResolver.resolve()
                )
            )
            result
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "analyze_failed requestId=${request.requestId} throwable=${t::class.java.simpleName} message=${t.message}",
                t
            )
            val mapped = errorMapper.map(t)
            val latency = SystemClock.elapsedRealtime() - started
            val failed = AnalyseTextuelleResult(
                requestId = request.requestId,
                status = AnalyseTextuelleStatus.FAILED,
                errorType = mapped.errorType,
                userMessage = mapped.userMessage
            )
            metricsLogger.log(
                ApiCallMetric(
                    requestId = request.requestId,
                    outcome = AnalyseTextuelleStatus.FAILED,
                    latencyMs = latency,
                    errorType = mapped.errorType,
                    deviceClass = deviceClassResolver.resolve()
                )
            )
            failed
        }
    }

    companion object {
        private const val TAG = "Gemma4LocalClient"
    }
}
