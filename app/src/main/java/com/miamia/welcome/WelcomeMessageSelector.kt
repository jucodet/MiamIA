package com.miamia.welcome

import kotlin.random.Random

class WelcomeMessageSelector(
    private val random: Random = Random.Default
) {
    fun select(messages: List<WelcomeMessage>): WelcomeMessage? {
        if (messages.isEmpty()) return null
        return messages[random.nextInt(messages.size)]
    }
}
