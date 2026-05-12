package com.miamia.welcome

sealed class WelcomeMessageUiState {
    data object Hidden : WelcomeMessageUiState()
    data class Displayed(val text: String, val messageId: String) : WelcomeMessageUiState()
}
