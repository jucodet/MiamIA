package com.miamia.ingredientknowledge

/**
 * Qualification du contexte de référence : toujours [GENERAL] (contenu général au sens
 * IHI-C-FR-004). Aucun fait étiquette n'est publié (IKB-A-FR-004/005).
 */
enum class ReferenceContextQualification { GENERAL }

/**
 * Projection d'une fiche en entrée de contexte (forme sérialisable pour le prompt LLM).
 */
data class ReferenceContextEntry(
    val kind: Kind,
    val key: String,
    val display: String,
    val riskLevel: RiskLevel?,
    val role: String?,
) {
    enum class Kind { ADDITIVE, ALLERGEN }

    companion object {
        fun from(card: AdditiveFactCard): ReferenceContextEntry = ReferenceContextEntry(
            kind = Kind.ADDITIVE,
            key = card.eNumber,
            display = card.canonicalName,
            riskLevel = card.riskLevel,
            role = card.role,
        )

        fun from(card: AllergenFactCard): ReferenceContextEntry = ReferenceContextEntry(
            kind = Kind.ALLERGEN,
            key = card.id,
            display = card.regulatoryName,
            riskLevel = null,
            role = null,
        )
    }
}

/**
 * Contexte de référence publié pour injection LLM. **Ensemble borné** (IKB-A-FR-011) et
 * qualifié explicitement « contenu général » (IKB-A-FR-004/005).
 *
 * Contrat Published Language — voir contracts/reference-context-read-model.md.
 */
data class ReferenceContext(
    val qualification: ReferenceContextQualification = ReferenceContextQualification.GENERAL,
    val baseVersion: String,
    val cards: List<ReferenceContextEntry>,
)
