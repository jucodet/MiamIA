package com.miamia.gemma4local

import com.miamia.gemma4local.model.ApiCallMetric
import org.junit.Assert.assertFalse
import org.junit.Test

class Us3PrivacyMetricsTest {
    @Test
    fun apiMetric_doesNotContainRawUserContentFields() {
        val fieldNames = ApiCallMetric::class.java.declaredFields.map { it.name.lowercase() }
        assertFalse(fieldNames.any { it.contains("inputtext") })
        assertFalse(fieldNames.any { it.contains("outputtext") })
        assertFalse(fieldNames.any { it.contains("raw") })
    }
}
