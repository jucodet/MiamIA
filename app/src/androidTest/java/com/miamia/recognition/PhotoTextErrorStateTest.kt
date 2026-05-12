package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoTextErrorStateTest {
    @Test
    fun errorState_containsReason() {
        val state = PhotoTextRecognitionState(
            state = "error",
            message = "Impossible de lire le texte, réessayez",
            errorReason = "engine_unavailable"
        )
        assertEquals("error", state.state)
        assertEquals("engine_unavailable", state.errorReason)
    }
}
