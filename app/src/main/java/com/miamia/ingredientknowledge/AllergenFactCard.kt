package com.miamia.ingredientknowledge

/**
 * Fiche référence allergène réglementaire UE (14 allergènes au maximum).
 *
 * [id] est l'identifiant réglementaire stable ; [aliases] sont des alias de recherche
 * (pas de synonyme métier — IKB-A-FR-006).
 */
data class AllergenFactCard(
    val id: String,
    val regulatoryName: String,
    val aliases: List<String> = emptyList(),
    val source: KbSource,
)
