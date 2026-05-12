package com.miamia.welcome

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class US3EmptyCatalogNoMessageTest {

    @Test
    fun emptyCatalog_skipsDisplayWithoutCrash() {
        val source = WelcomeCatalogSource { WelcomeCatalog(messages = emptyList()) }
        val policy = WelcomeMessagePolicy(source, WelcomeMessageSelector())
        val event = policy.onLoginSucceeded("user")
        assertEquals(WelcomeDisplayStatus.NOT_DISPLAYED_EMPTY_CATALOG, event.displayStatus)
    }
}
