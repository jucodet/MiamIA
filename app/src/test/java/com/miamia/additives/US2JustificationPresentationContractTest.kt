package com.miamia.additives

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class US2JustificationPresentationContractTest {

    @Test
    fun longText_truncatedWithEllipsis() {
        val long = "a".repeat(200)
        val t = AdditiveJustificationFormatter.truncatedForPreview(long)
        assertTrue(t.endsWith("…"))
        assertEquals(
            AdditiveJustificationFormatter.MAX_SINGLE_LINE_CHARS,
            t.length,
        )
    }

    @Test
    fun shortText_unchanged() {
        val s = "Court texte."
        assertEquals(s, AdditiveJustificationFormatter.truncatedForPreview(s))
        assertFalse(AdditiveJustificationFormatter.needsExpansion(s))
    }

    @Test
    fun unknownLevel_emptyJustification_stillShort() {
        val empty = ""
        assertEquals("", AdditiveJustificationFormatter.truncatedForPreview(empty))
        assertFalse(AdditiveJustificationFormatter.needsExpansion(empty))
    }
}
