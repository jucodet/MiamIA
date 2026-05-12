package com.miamia.welcome

import android.content.Context

fun interface WelcomeCatalogSource {
    fun loadCatalog(): WelcomeCatalog
}

class WelcomeMessageProvider(
    private val context: Context,
    private val assetPath: String = "welcome/messages_fr.json"
) : WelcomeCatalogSource {
    override fun loadCatalog(): WelcomeCatalog {
        val raw = runCatching {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        }.getOrDefault("[]")

        return WelcomeCatalog(
            messages = parseMessages(raw).filter { it.isActive && it.language == "fr" && WelcomeToneRules.isToneValid(it) },
            version = null
        )
    }

    internal fun parseMessages(rawJson: String): List<WelcomeMessage> {
        val objectRegex = Regex("\\{[^}]*\\}")
        return objectRegex.findAll(rawJson).mapNotNull { obj ->
            val source = obj.value
            val id = source.extract("id") ?: return@mapNotNull null
            val text = source.extract("text") ?: return@mapNotNull null
            val language = source.extract("language") ?: "fr"
            val isActive = source.extractBoolean("isActive") ?: true
            val toneTags = source.extractArray("toneTags")
            WelcomeMessage(
                id = id,
                text = text,
                language = language,
                toneTags = toneTags,
                isActive = isActive
            )
        }.toList()
    }
}

private fun String.extract(field: String): String? {
    val regex = Regex("\"$field\"\\s*:\\s*\"([^\"]+)\"")
    return regex.find(this)?.groupValues?.getOrNull(1)
}

private fun String.extractBoolean(field: String): Boolean? {
    val regex = Regex("\"$field\"\\s*:\\s*(true|false)")
    return regex.find(this)?.groupValues?.getOrNull(1)?.toBooleanStrictOrNull()
}

private fun String.extractArray(field: String): List<String> {
    val regex = Regex("\"$field\"\\s*:\\s*\\[([^\\]]*)\\]")
    val raw = regex.find(this)?.groupValues?.getOrNull(1) ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.split(",").mapNotNull { token ->
        token.trim().trim('"').takeIf { it.isNotBlank() }
    }
}
