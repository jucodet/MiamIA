package com.foodgpt.gemma4local

object Gemma4LocalConfig {
    const val DEFAULT_TIMEOUT_MS: Long = 45_000
    // Le health-check execute une mini-inference reelle; 1.5 s est trop court sur CPU mobile.
    const val AVAILABILITY_TIMEOUT_MS: Long = 20_000
    const val MAX_INPUT_CHARS: Int = 12_000
}
