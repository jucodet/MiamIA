package com.miamia.ingredientknowledge

/**
 * Fixture — cache store en mémoire pour les tests JVM pur.
 *
 * [corrupt] simule un cache illisible : `read()` renvoie `null` (repli baseline, IKB-B-FR-010).
 * Trace les écritures pour vérifier l'atomicité et la persistance attendue.
 */
class FakeKbCacheStore(
    initial: KbCache? = null,
    private val corrupt: Boolean = false,
) : KbCacheStore {

    @Volatile private var current: KbCache? = initial

    var written: KbCache? = null
        private set

    var writeCount: Int = 0
        private set

    var clearCount: Int = 0
        private set

    override fun read(): KbCache? = if (corrupt) null else current

    override fun write(cache: KbCache) {
        written = cache
        current = cache
        writeCount++
    }

    override fun clear() {
        current = null
        written = null
        clearCount++
    }
}
