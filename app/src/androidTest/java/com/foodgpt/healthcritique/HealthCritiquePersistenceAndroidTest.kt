package com.foodgpt.healthcritique

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthCritiquePersistenceAndroidTest {

    @Test
    fun saveThenLoad_returnsSnapshot() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val prefsName = "health_critique_test_${System.nanoTime()}"
        val store1 = LastHealthAnalysisStore(ctx, prefsName)
        val snap = LastHealthAnalysisSnapshot(
            savedAtEpochMs = 12345L,
            ingredientRaw = "eau, sucre",
            resultRaw = "###ENFANTS\nok",
            systemPromptSnapshot = "sys",
        )
        store1.save(snap)
        val store2 = LastHealthAnalysisStore(ctx, prefsName)
        val loaded = store2.load()
        assertNotNull(loaded)
        assertEquals(snap.ingredientRaw, loaded!!.ingredientRaw)
        assertEquals(snap.resultRaw, loaded.resultRaw)
        assertEquals(snap.systemPromptSnapshot, loaded.systemPromptSnapshot)
        assertEquals(snap.savedAtEpochMs, loaded.savedAtEpochMs)
        store2.clear()
    }
}
