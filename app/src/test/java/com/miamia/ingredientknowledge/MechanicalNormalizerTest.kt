package com.miamia.ingredientknowledge

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test unitaire — MechanicalNormalizer (casse/espaces/accents).
 * Variantes non couvertes par les normalisations listées → pas de match côté lookup (edge case spec).
 */
class MechanicalNormalizerTest {

    @Test
    fun normalise_casse_en_minuscules() {
        assertEquals("e300", MechanicalNormalizer.normalize("E300"))
        assertEquals("acide ascorbique", MechanicalNormalizer.normalize("Acide Ascorbique"))
    }

    @Test
    fun normalise_espaces_collapse_et_trim() {
        assertEquals("farine de ble", MechanicalNormalizer.normalize("  farine   de   blé  "))
    }

    @Test
    fun normalise_accents_vers_ascii() {
        assertEquals("ble", MechanicalNormalizer.normalize("blé"))
        assertEquals("celeri", MechanicalNormalizer.normalize("céléri"))
    }

    @Test
    fun chaine_vide_reste_vide() {
        assertEquals("", MechanicalNormalizer.normalize(""))
    }
}
