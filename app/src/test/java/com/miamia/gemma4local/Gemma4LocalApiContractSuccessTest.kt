package com.miamia.gemma4local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.miamia.gemma4local.model.AnalyseTextuelleStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Gemma4LocalApiContractSuccessTest {
    @Test
    fun analyzeText_returnsSuccessContract() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gateway = Gemma4LocalApiGateway { _ ->
            "###LISTE\n- eau\n###ANALYSE\nAnalyse courte."
        }
        val probe = object : Gemma4LocalAvailabilityProbe {
            override suspend fun ping(): Boolean = true
        }
        val client = Gemma4LocalClient(
            availabilityChecker = Gemma4LocalAvailabilityChecker(probe),
            requestMapper = Gemma4LocalRequestMapper(),
            errorMapper = Gemma4LocalErrorMapper(),
            metricsLogger = Gemma4LocalMetricsLogger(),
            deviceClassResolver = DeviceClassResolver(context),
            gateway = gateway
        )

        val result = client.analyze("eau")

        assertEquals(AnalyseTextuelleStatus.SUCCESS, result.status)
        assertNotNull(result.outputText)
        assertEquals(null, result.errorType)
    }
}
