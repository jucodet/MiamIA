package com.miamia.gemma4local

import android.content.Context
import android.os.Build
import android.app.ActivityManager
import com.miamia.gemma4local.model.DeviceClass

class DeviceClassResolver(
    private val context: Context
) {
    fun resolve(): DeviceClass {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memClass = am?.memoryClass ?: 0
        val isRecent = Build.VERSION.SDK_INT >= 33 && memClass >= 256
        return if (isRecent) DeviceClass.RECENT else DeviceClass.MIN_COMPAT
    }
}
