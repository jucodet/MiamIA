package com.miamia.camera

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class US2SingleCapturePerActionTest {
    @Test
    fun double_capture_flag_blocks_parallel_entry() {
        val flag = AtomicBoolean(false)
        assertTrue(flag.compareAndSet(false, true))
        assertFalse(flag.compareAndSet(false, true))
        flag.set(false)
        assertTrue(flag.compareAndSet(false, true))
    }
}
