package com.miamia.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkTypeDetectorTest {

    @Test
    fun `NetworkType enum has exactly three values`() {
        val values = NetworkType.entries
        assertEquals(3, values.size)
        assertEquals(NetworkType.WIFI, values[0])
        assertEquals(NetworkType.MOBILE_DATA, values[1])
        assertEquals(NetworkType.OFFLINE, values[2])
    }
}
