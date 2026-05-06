package com.foodgpt.welcome

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class US3NoCrashWhenCatalogEmptyTest {

    @Test
    fun loginSucceeded_returnsSkipped_whenCatalogEmpty() {
        val source = WelcomeCatalogSource { WelcomeCatalog(messages = emptyList()) }
        val policy = WelcomeMessagePolicy(
            provider = source,
            selector = WelcomeMessageSelector()
        )

        val event = policy.onLoginSucceeded("u1")
        assertEquals(WelcomeDisplayStatus.NOT_DISPLAYED_EMPTY_CATALOG, event.displayStatus)
        assertEquals(null, event.messageId)
    }
}
