package com.miamia.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeCaptureSpacingConsistencyTest {

    @Test
    fun spacingRuleIsFixedAndStable() {
        assertEquals(12.dp, HomeSpacingRules.standardFixedSpacing)
    }
}
