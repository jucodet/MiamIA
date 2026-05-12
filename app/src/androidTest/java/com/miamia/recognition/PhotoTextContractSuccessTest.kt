package com.miamia.recognition

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoTextContractSuccessTest {
    @Test
    fun successState_isPreserved() {
        val state = PhotoTextRecognitionState(
            state = "success",
            displayText = "ingredients: sucre"
        )
        assertEquals("success", state.state)
    }
}
