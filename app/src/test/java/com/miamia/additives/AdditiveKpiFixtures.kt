package com.miamia.additives

import com.miamia.composition.CompositionBilan

internal object AdditiveKpiFixtures {

    fun minimalBilan(): CompositionBilan = CompositionBilan(
        ingredientLines = listOf("eau", "sucre"),
        compositionAnalysis = "Analyse courte.",
        disclaimer = "Non médical",
    )

    fun rawMultiLevel(): String = """
        ###LISTE
        - eau
        ###ANALYSE
        Texte d'analyse.
        ###ADDITIFS_RISQUE
        VERT|E300|Antioxydant courant
        ORANGE|E250|Conservateur
        ROUGE|E102|Colorant suspect
        INCERTAIN|E999|Données partielles
    """.trimIndent()

    fun rawEmptyAdditives(): String = """
        ###LISTE
        - eau
        ###ANALYSE
        Rien à signaler.
    """.trimIndent()

    fun rawFiveForKpi(): String = """
        ###LISTE
        - a
        ###ANALYSE
        x
        ###ADDITIFS_RISQUE
        ROUGE|E1|a
        ROUGE|E2|b
        ORANGE|E3|c
        VERT|E4|d
        VERT|E5|e
        INCERTAIN|E6|f
    """.trimIndent()
}
