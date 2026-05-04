package com.foodgpt.ingredients

class ScanFailureMessageBuilder {
    fun build(code: String): String {
        return when (code) {
            "empty" -> "Aucun texte détecté. Recadrez l'étiquette puis réessayez."
            "blur" -> "Photo trop floue. Reprenez une image plus nette."
            "low-contrast" -> "Contraste insuffisant. Essayez avec plus de lumiere."
            "incomplete" -> "Liste ingredients incomplete. Recadrez l'etiquette."
            "no-canonical-anchor" -> "Ancre ingredients introuvable (format ingredients : ou ingredients:). Corrigez le texte ou recapturez."
            else -> "Extraction impossible. Reessayez manuellement."
        }
    }
}
