package com.miamia.ingredientknowledge

/**
 * Implémentation [ReferenceKb] exposant la **version courante disponible** (cache persisté →
 * à défaut baseline embarquée IKB-A) et déclenchant un refresh **non bloquant** via
 * [KbRefreshCoordinator] (IKB-B-FR-002, IKB-B-SC-006).
 *
 * Le swap d'index est atomique (`@Volatile`) : le chemin critique du lookup n'est jamais
 * bloqué par le refresh (IKB-B-FR-002). Le contrat `ReferenceKb` (IKB-A) reste inchangé pour
 * le core (Published Language `ReferenceContext` inchangé).
 */
class RefreshableReferenceKb(
    private val cacheStore: KbCacheStore,
    private val baseline: ReferenceKb,
) : ReferenceKb {

    @Volatile
    private var currentKb: ReferenceKb = loadInitial()

    /** Version courante disponible (cache si valide, sinon baseline). */
    fun current(): ReferenceKb = currentKb

    override fun lookup(designations: List<IngredientDesignation>): LookupOutcome =
        currentKb.lookup(designations)

    override fun baseVersion(): String = currentKb.baseVersion()

    /**
     * Publie la version rafraîchie (swap atomique) — appelée par [KbRefreshCoordinator] quand
     * le refresh aboutit. Les lookups suivants utilisent la nouvelle version.
     */
    fun publishRefreshed(refreshed: ReferenceKb) {
        currentKb = refreshed
    }

    private fun loadInitial(): ReferenceKb =
        cacheStore.read()?.toReferenceKb() ?: baseline

    private fun KbCache.toReferenceKb(): ReferenceKb =
        InMemoryReferenceKb(baseVersion, additives, allergens)
}
