package com.miamia.ingredientknowledge

/**
 * Résultat tracé d'un refresh au démarrage (IKB-B-FR-001, IKB-B-SC-001/003).
 *
 * Aucun refresh ne lève d'exception bloquante : un échec réseau/parse se traduit par
 * [Status.OFFLINE_FALLBACK] (cache/baseline conservé, IKB-B-FR-003/005).
 */
data class KbRefreshOutcome(
    val status: Status,
    val refreshedVersion: String?,
    val sourcesConsulted: List<String>,
    val rejectedEntries: Int,
    val reason: String?,
    val refreshedAt: Long,
) {
    enum class Status {
        /** Refresh complet réussi : nouvelle version publiée + cache écrit. */
        SUCCESS,
        /** Refresh partiel : une source indisponible, version publiée avec couverture réduite. */
        PARTIAL,
        /** Échec réseau/parse : repli sur cache/baseline, version courante inchangée. */
        OFFLINE_FALLBACK,
    }

    companion object {
        fun success(
            version: String,
            sources: List<String>,
            rejected: Int,
            at: Long,
        ): KbRefreshOutcome = KbRefreshOutcome(
            status = Status.SUCCESS,
            refreshedVersion = version,
            sourcesConsulted = sources,
            rejectedEntries = rejected,
            reason = null,
            refreshedAt = at,
        )

        fun partial(
            version: String,
            sources: List<String>,
            rejected: Int,
            reason: String,
            at: Long,
        ): KbRefreshOutcome = KbRefreshOutcome(
            status = Status.PARTIAL,
            refreshedVersion = version,
            sourcesConsulted = sources,
            rejectedEntries = rejected,
            reason = reason,
            refreshedAt = at,
        )

        fun offlineFallback(reason: String, at: Long): KbRefreshOutcome = KbRefreshOutcome(
            status = Status.OFFLINE_FALLBACK,
            refreshedVersion = null,
            sourcesConsulted = emptyList(),
            rejectedEntries = 0,
            reason = reason,
            refreshedAt = at,
        )
    }
}
