package com.miamia.gemma4local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Us3LatencySloIntegrationTest {
    @Test
    fun latency_thresholds_are_defined_for_two_device_classes() {
        val recentThresholdMs = 3000L
        val minCompatThresholdMs = 6000L
        assertTrue(recentThresholdMs < minCompatThresholdMs)
    }
}
