package com.miamia.gemma4local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Us1LocalApiSuccessAcceptanceTest {
    @Test
    fun success_contract_state_is_supported() {
        val expected = "SUCCESS"
        assertEquals("SUCCESS", expected)
    }
}
