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
    suspend fun analyze(rawText: String): AnalyseTextuelleResult {
        val request = requestMapper.map(rawText, sourceScreen = "camera")
        val started = SystemClock.elapsedRealtime()

        val availability = availabilityChecker.check()
        if (!availability.available) {
            if (availability.issue == Gemma4LocalAvailabilityIssue.TIMEOUT) {
                // Le health-check peut timeout alors que l'inference reelle finit quelques secondes plus tard.
                // Dans ce cas, on tente quand meme l'appel d'analyse pour eviter les faux negatifs.
                Log.w(
                    TAG,
                    "availability_timeout_non_blocking requestId=${request.requestId} details=${availability.details}"
                )
            } else {
            val failureMessage = when (availability.issue) {
                Gemma4LocalAvailabilityIssue.MODEL_MISSING_OR_INVALID ->
                    Gemma4LocalMessages.API_UNAVAILABLE
                Gemma4LocalAvailabilityIssue.TIMEOUT ->
                    Gemma4LocalMessages.MODEL_EXECUTION_FAILED
                Gemma4LocalAvailabilityIssue.RUNTIME_UNAVAILABLE ->
                    Gemma4LocalMessages.API_UNAVAILABLE
                Gemma4LocalAvailabilityIssue.UNKNOWN, null ->
                    Gemma4LocalMessages.MODEL_EXECUTION_FAILED
            }
            val failureType = when (availability.issue) {
                Gemma4LocalAvailabilityIssue.MODEL_MISSING_OR_INVALID -> AnalyseTextuelleErrorType.INVALID_RESPONSE
                Gemma4LocalAvailabilityIssue.RUNTIME_UNAVAILABLE -> AnalyseTextuelleErrorType.API_UNAVAILABLE
                Gemma4LocalAvailabilityIssue.TIMEOUT -> AnalyseTextuelleErrorType.TIMEOUT
                Gemma4LocalAvailabilityIssue.UNKNOWN, null -> AnalyseTextuelleErrorType.UNKNOWN
            }
            Log.w(
                TAG,
                "availability_failed requestId=${request.requestId} issue=${availability.issue} details=${availability.details}"
            )
            val latency = SystemClock.elapsedRealtime() - started
            val result = AnalyseTextuelleResult(
                requestId = request.requestId,
                status = AnalyseTextuelleStatus.FAILED,
                errorType = failureType,
                userMessage = failureMessage
            )
            metricsLogger.log(
                ApiCallMetric(
                    requestId = request.requestId,
                    outcome = AnalyseTextuelleStatus.FAILED,
                    latencyMs = latency,
                    errorType = failureType,
                    deviceClass = deviceClassResolver.resolve()
                )
            )
            return result
            }
        }

        return try {
            val output = withTimeout(Gemma4LocalConfig.DEFAULT_TIMEOUT_MS) {
                gateway.analyzeText(request.inputText)
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
