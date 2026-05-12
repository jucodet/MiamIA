package com.miamia.recognition

object IngredientAnchorFixtures {
    const val INTRO_THEN_LIST = """
        Les ingredients de ce produit sont issus de filieres locales.
        ingredients: sucre, farine, sel
        traces de lait
    """

    const val INTRO_THEN_LIST_WITH_SPACE = """
        Les ingredients de ce produit sont issus de filieres locales.
        ingredients : sucre, farine, sel
    """

    const val MULTIPLE_ANCHORS = """
        ingredients: eau
        texte annexe
        ingredients : sucre, farine
    """

    const val NO_CANONICAL_ANCHOR = """
        Les ingredients de ce produit sont naturels
        composition generale sans deux points
    """
}
