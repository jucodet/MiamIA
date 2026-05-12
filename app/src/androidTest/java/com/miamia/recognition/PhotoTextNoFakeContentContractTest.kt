package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoTextNoFakeContentContractTest {
    @Test
    fun emptyAndErrorStates_doNotContainDisplayText() {
        val emptyState = PhotoTextRecognitionState(state = "empty", message = "Aucun texte détecté")
        val errorState = PhotoTextRecognitionState(state = "error", message = "Impossible de lire le texte")
        assertNull(emptyState.displayText)
        assertNull(errorState.displayText)
        assertEquals("empty", emptyState.state)
        assertEquals("error", errorState.state)
    }
}
