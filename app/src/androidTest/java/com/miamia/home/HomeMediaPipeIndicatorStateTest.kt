package com.miamia.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMediaPipeIndicatorStateTest {

    @Test
    fun indicatorSupportsCheckingThenResolvedStates() {
        assertEquals("Verification...", MediaPipeStatusViewState.checking().label)
        assertEquals("Disponible", MediaPipeStatusViewState.available().label)
        assertEquals("Indisponible", MediaPipeStatusViewState.unavailable().label)
    }
}
