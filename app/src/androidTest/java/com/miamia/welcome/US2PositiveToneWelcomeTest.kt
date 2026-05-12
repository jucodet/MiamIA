package com.miamia.welcome

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class US2PositiveToneWelcomeTest {

    @Test
    fun positiveAndWarmMessages_passToneRules() {
        val message = WelcomeMessage(
            id = "w1",
            text = "Bienvenue",
            language = "fr",
            toneTags = listOf("positif", "chaleureux"),
            isActive = true
        )
        assertEquals(true, WelcomeToneRules.isToneValid(message))
    }
}
