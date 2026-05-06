package com.foodgpt.analysis.ingredientsegment.fixtures

object OcrFixtures {
    const val NOMINAL_MULTI_LINE = """
        produit x super promo
        ingredients: sucre, farine, sel
        traces possibles d arachides
    """

    const val NO_ANCHOR = """
        produit x super promo
        composition generale: eau, amidon
    """

    /** Ancre en début de ligne / texte seul, sans saut de ligne (borne `text_end`). */
    const val NO_NEWLINE_AFTER_ANCHOR = "Ingredients: sucre, farine, sel, huile"

    const val MULTIPLE_ANCHORS = """
        ingredients: eau
        texte annexe
        ingredients: sucre, farine
    """

    /** FR + fin de phrase sur la meme ligne (borne `sentence_terminator`). */
    const val FR_WITH_SENTENCE_END = """
        Ingrédients: sucre, sel. Traces possibles de gluten.
    """

    /** EN pluriel + fin de ligne (borne `line_end`). */
    const val EN_LINE_END = """
        Ingredients sugar, salt
        May contain nuts
    """

    /** EN singulier monoligne sans ponctuation ni saut de ligne (borne `text_end`). */
    const val EN_MONOLINE_NO_PUNCT = "Ingredient sugar, salt and flour"

    /** Deux ancres : seule la premiere ligne-ancre compte. */
    const val FR_THEN_EN_ANCHOR = """
        Ingrédients: eau.
        Ingredients: sugar, flour
    """
}
