package com.miamia.composition

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SegmentAnchoringV1Test {

    @Test
    fun modelTypo_nearOcr_passesAnchoring() {
        val segment =
            "ingrédients : céréales (50,2%), farines de blé et de seigle, sucre, graisses végétales (palme, colza)"
        val line = "érécles (50,2%)"
        assertTrue(SegmentAnchoringV1.isAnchoredInSegment(line, segment))
    }

    @Test
    fun farineTypo_passesAnchoring() {
        val segment = "farines de blé (49%) et de seigle, sucre"
        val line = "hforines de blé (49%) et de seigle"
        assertTrue(SegmentAnchoringV1.isAnchoredInSegment(line, segment))
    }

    @Test
    fun inventedIngredient_stillFails() {
        val segment = "eau, sucre, farine"
        assertFalse(SegmentAnchoringV1.isAnchoredInSegment("ingredient_invente_xyz", segment))
    }

    @Test
    fun stripPercent_thenSubstring() {
        val segment = "sucre, cacao maigre en poudre"
        val line = "cacao maigre (4,1%)"
        assertTrue(SegmentAnchoringV1.isAnchoredInSegment(line, segment))
    }
}
