package com.miamia.splash

import android.content.Context
import android.provider.Settings

/** Réduction des animations système (splash plus court — Feature H). */
fun Context.isMotionReduced(): Boolean =
    Settings.System.getFloat(contentResolver, Settings.System.ANIMATOR_DURATION_SCALE, 1f) == 0f
