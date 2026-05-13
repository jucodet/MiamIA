package com.miamia.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaitingPhrasesCatalogFeatureETest {

    /** Annex Feature E — specs/domains/user-guidance-experience/spec.md */
    private val featureEAnnexPhrases = listOf(
        "Nos algorithmes goûtent virtuellement chaque ligne… le verdict arrive.",
        "Les conservateurs jouent à cache-cache ; on les débusque.",
        "Prise de pouls de votre tableau nutritionnel, un instant.",
        "Les lipides préparent leur plaidoirie ; le juge délibère.",
        "Recensement des sucres qui prétendent être naturels…",
        "Lecture entre les lignes des tout petits caractères en bas d'étiquette…",
        "Arbitrage tendu entre « bon pour la ligne » et « irrésistible ».",
        "Les protéines s'étirent pendant qu'on compte les grammes.",
        "Vérification que le « sans gluten » ne cache pas d'autres surprises…",
        "La casserole des données mijote à feu doux, encore un peu de patience.",
    )

    @Test
    fun waitingPhrases_has21DistinctEntries_andIncludesFeatureEAnnex() {
        assertEquals(21, WAITING_PHRASES.size)
        val trimmed = WAITING_PHRASES.map { it.trim() }
        assertEquals(21, trimmed.toSet().size)
        featureEAnnexPhrases.forEach { phrase ->
            assertTrue("Missing annex phrase: $phrase", phrase in WAITING_PHRASES)
        }
    }
}
