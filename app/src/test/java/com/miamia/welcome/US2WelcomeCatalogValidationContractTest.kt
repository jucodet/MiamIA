package com.miamia.welcome

import org.junit.Assert.assertEquals
import org.junit.Test

class US2WelcomeCatalogValidationContractTest {

    @Test
    fun toneRules_acceptOnlyPositiveAndWarmMessages() {
        val valid = WelcomeMessage(
            id = "ok",
            text = "Bienvenue",
            language = "fr",
            toneTags = listOf("positif", "chaleureux")
        )
        val invalid = WelcomeMessage(
            id = "ko",
            text = "Bienvenue",
            language = "fr",
            toneTags = listOf("neutre")
        )
        assertEquals(true, WelcomeToneRules.isToneValid(valid))
        assertEquals(false, WelcomeToneRules.isToneValid(invalid))
    }
}
