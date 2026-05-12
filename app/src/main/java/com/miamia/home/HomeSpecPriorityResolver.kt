package com.miamia.home

object HomeSpecPriorityResolver {
    const val AUTHORITATIVE_HOME_UI_SPEC = "012-home-layout-mediapipe-status"

    fun resolveHomeUiOrderSpec(): String = AUTHORITATIVE_HOME_UI_SPEC

    fun keepsLegacyBusinessBehavior(): Boolean = true
}
