package com.miamia.ingredientknowledge

/**
 * Frontière de stockage offline (contracts/kb-refresh-gateway-contract.md).
 *
 * Isole la persistance : testable avec un fake (JVM pur) ; impl de production [FileKbCacheStore]
 * (JSON atomique dans `Context.filesDir`). Aucune méthode ne lève d'exception bloquante :
 * un cache absent/corrompu renvoie `null` (repli baseline, IKB-B-FR-010).
 */
interface KbCacheStore {
    /** Retourne le cache persisté, ou `null` si absent/illisible (corrompu). */
    fun read(): KbCache?

    /** Écriture atomique (`.tmp` → rename) — IKB-B-FR-004. */
    fun write(cache: KbCache)

    /** Efface le cache persisté. */
    fun clear()
}
