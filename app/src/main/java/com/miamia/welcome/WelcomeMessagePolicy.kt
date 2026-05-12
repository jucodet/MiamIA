package com.miamia.welcome

class WelcomeMessagePolicy(
    private val provider: WelcomeCatalogSource,
    private val selector: WelcomeMessageSelector,
    private val logger: WelcomeDisplayLogger = WelcomeDisplayLogger(),
    private val nowMs: () -> Long = { System.currentTimeMillis() }
) {
    fun onLoginSucceeded(userId: String): WelcomeDisplayEvent {
        val catalog = provider.loadCatalog()
        val selected = selector.select(catalog.messages)
        val event = if (selected == null) {
            WelcomeDisplayEvent(
                userId = userId,
                connectionTimestampMs = nowMs(),
                displayStatus = WelcomeDisplayStatus.NOT_DISPLAYED_EMPTY_CATALOG
            )
        } else {
            WelcomeDisplayEvent(
                userId = userId,
                connectionTimestampMs = nowMs(),
                displayStatus = WelcomeDisplayStatus.DISPLAYED,
                messageId = selected.id,
                messageText = selected.text
            )
        }
        logger.log(event)
        return event
    }
}

fun WelcomeDisplayEvent.toUiState(): WelcomeMessageUiState {
    return when (displayStatus) {
        WelcomeDisplayStatus.DISPLAYED -> {
            val text = messageText ?: return WelcomeMessageUiState.Hidden
            val id = messageId ?: return WelcomeMessageUiState.Hidden
            WelcomeMessageUiState.Displayed(text = text, messageId = id)
        }
        WelcomeDisplayStatus.NOT_DISPLAYED_EMPTY_CATALOG -> WelcomeMessageUiState.Hidden
    }
}
