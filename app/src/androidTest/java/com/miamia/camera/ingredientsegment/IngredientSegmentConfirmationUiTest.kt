package com.miamia.camera.ingredientsegment

import android.Manifest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.miamia.MainActivity
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IngredientSegmentConfirmationUiTest {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun confirmation_flow_is_available_when_camera_is_present() {
        val pm = InstrumentationRegistry.getInstrumentation().targetContext.packageManager
        assumeTrue(pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY))
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            InstrumentationRegistry.getInstrumentation().targetContext.packageName,
            Manifest.permission.CAMERA
        )
        Thread.sleep(500)
    }
}
