package com.miamia.camera

import org.junit.Assert.assertTrue
import org.junit.Test

class US3CameraErrorContractTest {
    @Test
    fun camera_error_state_fields_present() {
        val err = CameraErrorState("id", "permission_denied", "refus", canRetry = true)
        assertTrue(err.canRetry)
    }
}
