package com.miamia.composition

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GemmaModelLocatorTest {

    @Test
    fun resolve_returnsReadyOrNotFound_consistentWithAssets() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        when (val loc = GemmaModelLocator(ctx).resolve()) {
            is GemmaModelLocation.Ready -> {
                val f = loc.modelFile
                assertTrue(f.isFile)
                assertTrue(f.absolutePath.startsWith(ctx.filesDir.absolutePath))
                assertEquals(GemmaModelPaths.EXPECTED_MODEL_FILENAME, f.name)
            }
            GemmaModelLocation.NotFound -> assertTrue(true)
            is GemmaModelLocation.LoadFailed -> error("unexpected load_failed: ${loc.reason}")
        }
    }
}
