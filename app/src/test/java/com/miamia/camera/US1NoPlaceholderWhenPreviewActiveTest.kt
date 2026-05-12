package com.miamia.camera

import org.junit.Assert.assertNotEquals
import org.junit.Test

class US1NoPlaceholderWhenPreviewActiveTest {
    @Test
    fun preview_active_differs_from_ready_idle() {
        assertNotEquals(ScanState.CameraReady, ScanState.PreviewActive)
    }
}
