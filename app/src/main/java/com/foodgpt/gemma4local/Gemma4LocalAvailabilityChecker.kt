package com.foodgpt.gemma4local

import java.util.concurrent.TimeoutException
import kotlinx.coroutines.withTimeoutOrNull

interface Gemma4LocalAvailabilityProbe {
    suspend fun ping(): Boolean
}

enum class Gemma4LocalAvailabilityIssue {
    MODEL_MISSING_OR_INVALID,
    RUNTIME_UNAVAILABLE,
    TIMEOUT,
    UNKNOWN
}

data class Gemma4LocalAvailabilityStatus(
    val available: Boolean,
    val issue: Gemma4LocalAvailabilityIssue? = null,
    val details: String = ""
)

class Gemma4LocalAvailabilityChecker(
    private val probe: Gemma4LocalAvailabilityProbe
) {
    suspend fun check(): Gemma4LocalAvailabilityStatus {
        return try {
            val ok = withTimeoutOrNull(Gemma4LocalConfig.AVAILABILITY_TIMEOUT_MS) {
                probe.ping()
            } ?: return Gemma4LocalAvailabilityStatus(
                available = false,
                issue = Gemma4LocalAvailabilityIssue.TIMEOUT,
                details = "Health check timeout."
            )
            if (ok) {
                Gemma4LocalAvailabilityStatus(available = true)
            } else {
                Gemma4LocalAvailabilityStatus(
                    available = false,
                    issue = Gemma4LocalAvailabilityIssue.RUNTIME_UNAVAILABLE,
                    details = "Health check returned false."
                )
            }
        } catch (_: TimeoutException) {
            Gemma4LocalAvailabilityStatus(
                available = false,
                issue = Gemma4LocalAvailabilityIssue.TIMEOUT,
                details = "Health check timeout exception."
            )
        } catch (t: Throwable) {
            val message = t.message.orEmpty()
            val issue = when {
                message.contains("Modele Gemma local", ignoreCase = true) -> Gemma4LocalAvailabilityIssue.MODEL_MISSING_OR_INVALID
                message.contains("Format modele invalide", ignoreCase = true) -> Gemma4LocalAvailabilityIssue.MODEL_MISSING_OR_INVALID
                else -> Gemma4LocalAvailabilityIssue.UNKNOWN
            }
            Gemma4LocalAvailabilityStatus(
                available = false,
                issue = issue,
                details = "${t::class.java.simpleName}:${t.message.orEmpty()}"
            )
        }
    }

    suspend fun isAvailable(): Boolean {
        return check().available
    }
}
