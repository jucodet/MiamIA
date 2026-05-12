package com.miamia.welcome

import android.util.Log

class WelcomeDisplayLogger {
    fun log(event: WelcomeDisplayEvent) {
        Log.d(
            "WelcomeDisplay",
            "status=${event.displayStatus} user=${event.userId} messageId=${event.messageId ?: "none"}"
        )
    }
}
