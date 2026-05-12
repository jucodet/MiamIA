package com.miamia.scan

import android.content.Context
import java.io.File
import java.util.UUID

class TemporaryImageManager(private val context: Context) {
    fun createTempImageFile(): File {
        val fileName = "scan_${UUID.randomUUID()}.jpg"
        return File(context.cacheDir, fileName)
    }

    fun cleanupTempImage(file: File?): Boolean {
        if (file == null) return true
        if (!file.exists()) return true
        return file.delete()
    }

    fun cleanupAfterOutcome(file: File?, outcome: String): Boolean {
        if (outcome == "processing") return false
        return cleanupTempImage(file)
    }
}
