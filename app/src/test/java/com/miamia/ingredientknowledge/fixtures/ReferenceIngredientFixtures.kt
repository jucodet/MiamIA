package com.miamia.ingredientknowledge.fixtures

import com.miamia.ingredientknowledge.AdditiveFactCard
import com.miamia.ingredientknowledge.AllergenFactCard
import com.miamia.ingredientknowledge.IngredientDesignation
import com.miamia.ingredientknowledge.InMemoryReferenceKb
import com.miamia.ingredientknowledge.KbSource
import com.miamia.ingredientknowledge.RiskLevel

/**
 * Jeu fixe d'ingrédients de référence (US-IKB-A3 / IKB-A-FR-008).
 *
 * Exécutable isolément, hors capture/OCR/runtime LLM. Sert à vérifier la stabilité du lookup
 * et de la constitution du contexte (répétabilité ≥ 3 exécutions, IKB-A-SC-004).
 */
object ReferenceIngredientFixtures {

    const val BASE_VERSION = "fixtures-0.1"

    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, BASE_VERSION)
    private val eu = KbSource(KbSource.Origin.EU_ALLERGEN_LIST, BASE_VERSION)

    /** KB référence du jeu fixe (extrait stable, déterministe). */
    val kb: InMemoryReferenceKb = InMemoryReferenceKb(
        baseVersion = BASE_VERSION,
        additives = listOf(
            AdditiveFactCard("E300", "acide ascorbique", listOf("vitamine c"), "antioxydant", RiskLevel.FAIBLE, off),
            AdditiveFactCard("E250", "nitrite de sodium", listOf("nitrite de soude"), "conservateur", RiskLevel.ELEVE, off),
            AdditiveFactCard("E621", "glutamate monosodique", listOf("msg"), "exhausteur de gout", RiskLevel.MODERE, off),
        ),
        allergens = listOf(
            AllergenFactCard("GLUTEN", "cereales contenant du gluten", listOf("ble", "seigle", "orge"), eu),
            AllergenFactCard("SOJA", "soja", listOf("lecithine de soja"), eu),
            AllergenFactCard("LAIT", "lait", listOf("lait ecreme", "lait entier"), eu),
        ),
    )

    /** Liste d'ingrédients fixe (style mockée, à la IHI-A-FR-002). */
    val designations: List<IngredientDesignation> = listOf(
        IngredientDesignation.fromRaw("farine de BLÉ 33 %"),
        IngredientDesignation.fromRaw("émulsifiant (lécithines de SOJA)"),
        IngredientDesignation.fromRaw("E300"),
        IngredientDesignation.fromRaw("conservateur : nitrite de sodium"),
        IngredientDesignation.fromRaw("exhausteur de gout : glutamate monosodique"),
        IngredientDesignation.fromRaw("LAIT entier en poudre"),
        IngredientDesignation.fromRaw("arômes"),
    )

    /** Additifs attendus (par E-number) après lookup. */
    val expectedAdditiveNumbers: Set<String> = setOf("E300", "E250", "E621")

    /** Allergènes attendus (par id) après lookup. */
    val expectedAllergenIds: Set<String> = setOf("GLUTEN", "SOJA", "LAIT")

    /** Désignation non référencée attendue dans unmatched. */
    val expectedUnmatchedRaw: Set<String> = setOf("arômes")
}
