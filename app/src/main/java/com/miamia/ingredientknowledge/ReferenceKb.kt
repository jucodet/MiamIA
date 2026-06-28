package com.miamia.ingredientknowledge

/**
 * Frontière de domaine + couche d'anti-corruption (contracts/ingredient-kb-lookup-contract.md).
 *
 * `ReferenceKb` expose le lookup offline. Les implémentations [EmbeddedReferenceKb] (Android
 * assets, production) et [InMemoryReferenceKb] (fixtures, tests JVM pur) sont interchangeables.
 */
interface ReferenceKb {
    /**
     * Pour chaque désignation, recherche par sous-chaîne littérale après normalisation mécanique
     * (casse, espaces, accents) ; renvoie les fiches canoniques correspondantes.
     * Substance non référencée → présente dans [LookupOutcome.unmatchedDesignations] (repli silencieux).
     */
    fun lookup(designations: List<IngredientDesignation>): LookupOutcome

    /** Version de la base référence (depuis kb-version.json). */
    fun baseVersion(): String
}
