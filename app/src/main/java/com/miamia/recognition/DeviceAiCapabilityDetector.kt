package com.miamia.recognition

import android.content.Context
import android.content.pm.PackageManager

class DeviceAiCapabilityDetector(private val context: Context) {
    fun isAiEdgeGalleryAvailable(): Boolean {
        val pm = context.packageManager ?: return false
        return pm.hasSystemFeature("android.hardware.neuralnetworks") ||
            pm.hasSystemFeature(PackageManager.FEATURE_OPENGLES_EXTENSION_PACK)
    }

    fun explainAvailability(): String {
        return if (isAiEdgeGalleryAvailable()) {
            "NNAPI disponible: AI Edge prioritaire"
        } else {
            "AI Edge indisponible: fallback OCR local"
        }
    }
}
