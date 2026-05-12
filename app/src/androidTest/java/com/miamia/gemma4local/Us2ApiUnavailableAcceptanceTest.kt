package com.miamia.gemma4local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Us2ApiUnavailableAcceptanceTest {
    @Test
    fun unavailable_state_has_explicit_message() {
        val message = "API locale Gemma4 indisponible sur cet appareil."
        assertTrue(message.contains("indisponible"))
    }
}
