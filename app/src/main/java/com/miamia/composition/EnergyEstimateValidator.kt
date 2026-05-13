package com.miamia.composition

/**
 * Garde-fous affichage estimation énergétique (kcal / 100 g) — Feature K (**IHI-K-FR-006**).
 * Plage spec / clarify : **1..1100** inclusivement.
 */
object EnergyEstimateValidator {

    /** Plage **IHI-K-FR-006** (clarify 1..1100) : rejeter l’affichage plutôt qu’exposer une valeur absurde. */
    val VALID_KCAL_PER_100G_RANGE: IntRange = 1..1100

    fun clampOrNull(value: Int?): Int? = value?.takeIf { it in VALID_KCAL_PER_100G_RANGE }

    /**
     * Extrait un entier kcal/100 g depuis le bloc texte sous `###ENERGIE_ESTIMEE`
     * (première valeur plausible : `420`, `kcal_pour_100g: 420`, etc.).
     */
    fun parseKcalFromEnergyBlock(block: String): Int? {
        val compact = block.trim()
        if (compact.isEmpty()) return null
        val firstLine = compact.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return null
        if (firstLine.equals("na", ignoreCase = true) || firstLine.equals("n/a", ignoreCase = true)) {
            return null
        }
        val fromLabel = Regex("""(?i)(?:kcal|énergie|energie)[^\d]{0,32}(\d{1,4})""").find(compact)
        val raw = fromLabel?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("""\b(\d{1,4})\b""").find(compact)?.groupValues?.get(1)?.toIntOrNull()
        return clampOrNull(raw)
    }

    fun sanitizeBilan(bilan: CompositionBilan): CompositionBilan =
        bilan.copy(estimatedKcalPer100g = clampOrNull(bilan.estimatedKcalPer100g))
}
