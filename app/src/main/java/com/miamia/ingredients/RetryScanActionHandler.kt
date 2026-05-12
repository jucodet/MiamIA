package com.miamia.ingredients

class RetryScanActionHandler {
    fun canRetryAutomatically(): Boolean = false
    fun canRetryManually(): Boolean = true

    fun performManualRetry(onRetry: () -> Unit): Boolean {
        if (!canRetryManually()) return false
        onRetry()
        return true
    }
}
