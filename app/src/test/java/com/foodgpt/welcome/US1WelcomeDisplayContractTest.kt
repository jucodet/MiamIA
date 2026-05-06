package com.foodgpt.welcome

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class US1WelcomeDisplayContractTest {

    @Test
    fun loginSucceeded_returnsDisplayed_whenCatalogContainsMessage() {
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
        val policy = WelcomeMessagePolicy(
            provider = source,
            selector = WelcomeMessageSelector()
        )

        val event = policy.onLoginSucceeded("u1")
        assertEquals(WelcomeDisplayStatus.DISPLAYED, event.displayStatus)
        assertNotNull(event.messageId)
    }
}
