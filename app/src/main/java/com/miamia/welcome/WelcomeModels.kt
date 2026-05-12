package com.miamia.welcome

data class WelcomeMessage(
    val id: String,
    val text: String,
    val language: String = "fr",
    val toneTags: List<String> = emptyList(),
    val isActive: Boolean = true
)

data class WelcomeCatalog(
    val messages: List<WelcomeMessage>,
    val version: String? = null
)

enum class WelcomeDisplayStatus {
    DISPLAYED,
    NOT_DISPLAYED_EMPTY_CATALOG
}

data class WelcomeDisplayEvent(
    val userId: String,
    val connectionTimestampMs: Long,
    val displayStatus: WelcomeDisplayStatus,
    val messageId: String? = null,
    val messageText: String? = null
)
