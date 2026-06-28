package com.miamia.ingredientknowledge

import kotlinx.serialization.Serializable

/**
 * Snapshot persistant de la base référence (IKB-B-FR-004).
 *
 * Sérialisé en JSON dans `Context.filesDir/ingredientkb/` par [FileKbCacheStore] (écriture
 * atomique `.tmp` → rename). Rechargé au démarrage avant le refresh ; sert de repli offline
 * intermédiaire (cache → baseline embarquée, IKB-B-FR-005).
 *
 * `@Serializable` : la persistance s'appuie directement sur les modèles de domaine (déjà
 * sérialisables via kotlinx.serialization), sans couche DTO supplémentaire (principe V).
 */
@Serializable
data class KbCache(
    val baseVersion: String,
    val additives: List<AdditiveFactCard>,
    val allergens: List<AllergenFactCard>,
    val refreshedAt: Long,
    val sources: List<String>,
)
