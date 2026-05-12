package com.miamia.welcome

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class US1WelcomeAfterLoginFlowTest {

    @Test
    fun loginEvent_canLeadToDisplayedWelcomeMessage() {
        val source = WelcomeCatalogSource {
            WelcomeCatalog(
                messages = listOf(
                    WelcomeMessage(
                        id = "w1",
                        text = "Bonjour",
                        language = "fr",
                        toneTags = listOf("positif", "chaleureux"),
                        isActive = true
                    )
                )
            )
        }
        val policy = WelcomeMessagePolicy(source, WelcomeMessageSelector())
        val event = policy.onLoginSucceeded("user")
        assertEquals(WelcomeDisplayStatus.DISPLAYED, event.displayStatus)
    }
}
