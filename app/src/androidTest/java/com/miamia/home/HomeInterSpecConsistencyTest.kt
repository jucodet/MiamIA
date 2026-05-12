package com.miamia.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeInterSpecConsistencyTest {

    @Test
    fun uiOrderUsesSpec012AsAuthority() {
        assertEquals(
            "012-home-layout-mediapipe-status",
            HomeSpecPriorityResolver.resolveHomeUiOrderSpec()
        )
        assertTrue(HomeSpecPriorityResolver.keepsLegacyBusinessBehavior())
    }
}
