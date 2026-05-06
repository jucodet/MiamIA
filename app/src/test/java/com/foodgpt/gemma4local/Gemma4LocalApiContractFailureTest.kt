package com.foodgpt.gemma4local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.foodgpt.gemma4local.model.AnalyseTextuelleErrorType
import com.foodgpt.gemma4local.model.AnalyseTextuelleStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Gemma4LocalApiContractFailureTest {
    @Test
    fun analyzeText_returnsFailureWhenUnavailable() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gateway = Gemma4LocalApiGateway { "unused" }
        val probe = object : Gemma4LocalAvailabilityProbe {
            override suspend fun ping(): Boolean = false
        }
        val client = Gemma4LocalClient(
            availabilityChecker = Gemma4LocalAvailabilityChecker(probe),
            requestMapper = Gemma4LocalRequestMapper(),
            errorMapper = Gemma4LocalErrorMapper(),
            metricsLogger = Gemma4LocalMetricsLogger(),
            deviceClassResolver = DeviceClassResolver(context),
            gateway = gateway
        )

        val result = client.analyze("sucre")

        assertEquals(AnalyseTextuelleStatus.FAILED, result.status)
        assertEquals(AnalyseTextuelleErrorType.API_UNAVAILABLE, result.errorType)
    }
}
