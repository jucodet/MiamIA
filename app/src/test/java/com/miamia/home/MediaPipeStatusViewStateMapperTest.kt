package com.miamia.home

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaPipeStatusViewStateMapperTest {

    @Test
    fun checkingStateUsesExpectedLabel() {
        val state = MediaPipeStatusViewState.checking()
        assertEquals("Verification...", state.label)
        assertEquals(MediaPipeStatusState.CHECKING, state.state)
    }

    @Test
    fun availableStateUsesExpectedLabel() {
        val state = MediaPipeStatusViewState.available()
        assertEquals("Disponible", state.label)
        assertEquals(MediaPipeStatusState.AVAILABLE, state.state)
    }

    @Test
    fun unavailableStateUsesExpectedLabel() {
        val state = MediaPipeStatusViewState.unavailable()
        assertEquals("Indisponible", state.label)
        assertEquals(MediaPipeStatusState.UNAVAILABLE, state.state)
    }
}
