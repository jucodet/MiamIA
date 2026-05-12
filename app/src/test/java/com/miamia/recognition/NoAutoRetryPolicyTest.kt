package com.miamia.recognition

import com.miamia.ingredients.RetryScanActionHandler
import org.junit.Assert.assertFalse
import org.junit.Test

class NoAutoRetryPolicyTest {
    @Test
    fun automatic_retry_is_disabled() {
        assertFalse(RetryScanActionHandler().canRetryAutomatically())
    }
}
