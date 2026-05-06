package com.foodgpt.welcome

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WelcomeMessageProviderTest {

    @Test
    fun parseMessages_filtersOnlyActiveFrenchAndPositive() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val provider = WelcomeMessageProvider(app)
        val parsed = provider.parseMessages(
            """
            [
              {"id":"ok","text":"Salut","language":"fr","toneTags":["positif","chaleureux"],"isActive":true},
              {"id":"bad-tone","text":"Salut","language":"fr","toneTags":["neutre"],"isActive":true},
              {"id":"bad-lang","text":"Hello","language":"en","toneTags":["positif","chaleureux"],"isActive":true},
              {"id":"inactive","text":"Coucou","language":"fr","toneTags":["positif","chaleureux"],"isActive":false}
            ]
            """.trimIndent()
        )
        val filtered = parsed.filter { it.isActive && it.language == "fr" && WelcomeToneRules.isToneValid(it) }
        assertEquals(1, filtered.size)
        assertEquals("ok", filtered.first().id)
    }
}
