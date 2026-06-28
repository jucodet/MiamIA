package com.miamia.ingredientknowledge

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fixture — gateway amont configurable (succès / échec / latence) pour les tests JVM pur.
 * Anti-corruption : remplace [OffCiqualRefreshGateway] sans dépendance réseau.
 */
class FakeKbRefreshGateway(
    private val payload: KbRefreshPayload? = null,
    private val error: Throwable? = null,
    private val delayMs: Long = 0L,
) : KbRefreshGateway {

    val fetchInvoked = AtomicBoolean(false)
    var fetchCount: Int = 0
        private set

    override suspend fun fetch(): KbRefreshPayload {
        fetchInvoked.set(true)
        fetchCount++
        if (delayMs > 0L) delay(delayMs)
        error?.let { throw it }
        return payload ?: error("FakeKbRefreshGateway: aucun payload configuré")
    }
}
