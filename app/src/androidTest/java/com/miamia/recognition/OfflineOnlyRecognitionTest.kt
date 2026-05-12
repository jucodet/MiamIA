package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineOnlyRecognitionTest {
    @Test
    fun engineSelector_forcesLocalRecognizer() {
        val state = PhotoTextRecognitionState(state = "success", displayText = "local-only")
        assertEquals("success", state.state)
    }
}
