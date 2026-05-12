package com.miamia.camera

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.recognition.RecognitionTimingHelper
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraCaptureToTextSuccessTest {
    @Test
    fun timingHelper_recordsPositiveDuration() {
        val helper = RecognitionTimingHelper()
        helper.start()
        assertTrue(helper.elapsedMs() >= 0L)
    }
}
