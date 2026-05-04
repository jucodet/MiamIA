package com.foodgpt.healthcritique

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthCritiqueClipboardAndroidTest {

    @Test
    fun copyPlainText_writesPrimaryClip() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        HealthCritiqueClipboard.copyPlainText(ctx, "label", "contenu-critique")
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        assertNotNull(clip)
        assertEquals("contenu-critique", clip!!.getItemAt(0).text)
    }
}
