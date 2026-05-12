package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoTextLatencyTest {
    @Test
    fun targetLatency_isUnder10SecondsForSanityCheck() {
        val helper = RecognitionTimingHelper()
        helper.start()
        assertTrue(helper.elapsedMs() < 10_000L)
    }
}
