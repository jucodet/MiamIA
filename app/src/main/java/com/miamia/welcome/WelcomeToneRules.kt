package com.miamia.welcome

object WelcomeToneRules {
    private val requiredTags = setOf("positif", "chaleureux")

    fun isToneValid(message: WelcomeMessage): Boolean {
        if (message.text.isBlank()) return false
        return requiredTags.all { message.toneTags.contains(it) }
    }
}
