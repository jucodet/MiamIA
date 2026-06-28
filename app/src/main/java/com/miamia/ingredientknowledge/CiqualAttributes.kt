package com.miamia.ingredientknowledge

import kotlinx.serialization.Serializable

/**
 * Attributs nutritionnels Ciqual optionnels rattachés à un additif (IKB-B-FR-008).
 *
 * Source traçable ([source] d'origine [KbSource.Origin.CIQUAL]). Attributs publiés comme
 * **contenu général** dans le [ReferenceContext] (Feature C inchangée — IKB-B-FR-009).
 * Tous les champs sont optionnels : un attribut absent/incohérent à l'ingestion est omis
 * (repli silencieux, IKB-B-FR-011) — aucune invention.
 */
@Serializable
data class CiqualAttributes(
    val energyKcal: Double? = null,
    val source: KbSource = KbSource(KbSource.Origin.CIQUAL, baseVersion = ""),
)
