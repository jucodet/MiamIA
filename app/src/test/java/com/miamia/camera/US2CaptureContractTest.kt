package com.miamia.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class US2CaptureContractTest {
    @Test
    fun capture_frame_command_roundtrip() {
        val cmd = CaptureFrameCommand("session", "action", "capture_button", 1L)
        assertEquals("capture_button", cmd.triggeredBy)
    }
}
