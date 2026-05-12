package com.miamia.recognition

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientRecognitionContractTest {
    @Test
    fun contract_fields_are_mapped() {
        val cmd = StartIngredientRecognitionCommand("id", "/tmp/x.jpg", 1L, "camera")
        assertEquals("id", cmd.scanId)
    }
}
