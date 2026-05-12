package com.miamia.analysis.ingredientsegment.fixtures

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

    /** Point interne additif (E.621) — ne doit PAS déclencher la fin de capture. */
    const val DOT_INTERNAL_ADDITIVE = "Ingrédients: eau, colorant E.621, sucre, sel"

    /** Point interne abréviation (vit.B12) + fin de ligne — capture jusqu'au newline. */
    const val DOT_INTERNAL_ABBREVIATION = "Ingredients: vit.B12, iron, zinc\nNext section"

    /** Point suivi d'un espace — termine la capture au `. `. */
    const val DOT_SPACE_END = "Ingrédients: eau, sucre. Traces de lait."

    /** Point suivi d'un retour à la ligne — termine la capture au `.\n`. */
    const val DOT_NEWLINE_END = "Ingrédients: eau, sucre.\nTraces de lait."

    /** Point en fin absolue de texte — termine la capture. */
    const val DOT_EOF_END = "Ingrédients: eau, sucre."
}
