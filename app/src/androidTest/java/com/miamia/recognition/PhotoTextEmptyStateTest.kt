package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoTextEmptyStateTest {
    @Test
    fun emptyState_containsExplicitMessage() {
        val state = PhotoTextRecognitionState(state = "empty", message = "Aucun texte détecté")
        assertEquals("empty", state.state)
        assertEquals("Aucun texte détecté", state.message)
    }
}
