package com.miamia.ingredientknowledge

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrateur du refresh au démarrage (contracts/kb-refresh-gateway-contract.md).
 *
 * `refreshAtStartup` est **non bloquant** : exécuté sur [dispatcher] (défaut `Dispatchers.IO`),
 * fire-and-forget au démarrage. L'index courant (cache/baseline) reste utilisé jusqu'à
 * publication de la version rafraîchie via [onRefreshed] (IKB-B-FR-002).
 *
 * Aucun échec n'est bloquant : une indisponibilité réseau/parse → [KbRefreshOutcome.Status.OFFLINE_FALLBACK]
 * (cache/baseline conservé, IKB-B-FR-003/005).
 */
class KbRefreshCoordinator(
    private val gateway: KbRefreshGateway,
    private val cacheStore: KbCacheStore,
    private val baseline: ReferenceKb,
    private val onRefreshed: (ReferenceKb) -> Unit,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {

    suspend fun refreshAtStartup(): KbRefreshOutcome = withContext(dispatcher) {
        try {
            val payload = gateway.fetch()
            val now = clock()
            cacheStore.write(
                KbCache(
                    baseVersion = payload.baseVersion,
                    additives = payload.additives,
                    allergens = payload.allergens,
                    refreshedAt = now,
                    sources = payload.sourcesConsulted,
                )
            )
            val refreshed: ReferenceKb =
                InMemoryReferenceKb(payload.baseVersion, payload.additives, payload.allergens)
            onRefreshed(refreshed)
            if (payload.partial) {
                KbRefreshOutcome.partial(
                    version = payload.baseVersion,
                    sources = payload.sourcesConsulted,
                    rejected = payload.rejectedEntries,
                    reason = "Source amont partiellement indisponible",
                    at = now,
                )
            } else {
                KbRefreshOutcome.success(
                    version = payload.baseVersion,
                    sources = payload.sourcesConsulted,
                    rejected = payload.rejectedEntries,
                    at = now,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: KbRefreshUnavailable) {
            KbRefreshOutcome.offlineFallback(e.message ?: "Source amont indisponible", clock())
        } catch (e: Throwable) {
            KbRefreshOutcome.offlineFallback(
                e.message ?: e::class.simpleName ?: "Erreur refresh",
                clock(),
            )
        }
    }
}
