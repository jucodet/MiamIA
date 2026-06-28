package com.miamia.ingredientknowledge

import kotlinx.serialization.Serializable

/**
 * Fiche référence allergène réglementaire UE (14 allergènes au maximum).
 *
 * [id] est l'identifiant réglementaire stable ; [aliases] sont des alias de recherche
 * (pas de synonyme métier — IKB-A-FR-006).
 *
 * `@Serializable` pour la persistance du cache offline (IKB-B — `FileKbCacheStore`).
 */
@Serializable
data class AllergenFactCard(
    val id: String,
    val regulatoryName: String,
    val aliases: List<String> = emptyList(),
    val source: KbSource,
)
