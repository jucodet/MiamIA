package com.miamia.gemma4local

import com.google.ai.edge.litertlm.Backend
import com.miamia.gemma4local.model.BackendExecution
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Contrat : mapping `com.google.ai.edge.litertlm.Backend` → `BackendExecution`.
 *
 * Couvre spec FR-002 (backend récupéré depuis le runtime, non inféré côté présentation)
 * et FR-008 (backend inconnu traité comme INDETERMINATE sans interruption).
 */
class BackendExecutionMappingTest {

    @Test
    fun mapsNpuBackend() {
        assertEquals(
            BackendExecution.NPU,
            BackendExecution.from(Backend.NPU(nativeLibraryDir = "/nonexistent"))
        )
    }

    @Test
    fun mapsGpuBackend() {
        assertEquals(BackendExecution.GPU, BackendExecution.from(Backend.GPU()))
    }

    @Test
    fun mapsCpuBackend() {
        assertEquals(BackendExecution.CPU, BackendExecution.from(Backend.CPU()))
    }

    @Test
    fun mapsNullBackendToIndeterminate() {
        assertEquals(BackendExecution.INDETERMINATE, BackendExecution.from(null))
    }

    @Test
    fun labelIsHumanReadablePerBackend() {
        assertEquals("NPU", BackendExecution.NPU.label)
        assertEquals("GPU", BackendExecution.GPU.label)
        assertEquals("CPU", BackendExecution.CPU.label)
        assertEquals("—", BackendExecution.INDETERMINATE.label)
    }
}
