# Contrat — `KbRefreshGateway` + `KbCacheStore` + `KbRefreshCoordinator` (frontière sources amont + cache)

**Domaine** : `ingredient-knowledge` (Feature IKB-B)
**Rôle** : frontières d'anti-corruption entre le domaine et (a) les sources externes OpenFoodFacts/Ciqual, (b) le stockage offline. `KbRefreshCoordinator` orchestre le refresh au démarrage ; `RefreshableReferenceKb` implémente `ReferenceKb` (contrat IKB-A inchangé) sur la version courante disponible.

## `KbRefreshGateway` (interface)

Fetch + parse des sources amont. Isolle le réseau (testable avec un fake).

```kotlin
interface KbRefreshGateway {
    suspend fun fetch(): KbRefreshPayload
}

data class KbRefreshPayload(
    val additives: List<AdditiveFactCard>,   // taxonomie OFF (couverture exhaustive)
    val allergens: List<AllergenFactCard>,    // inchangé / source UE
    val baseVersion: String,
    val sourcesConsulted: List<String>,
    val rejectedEntries: Int,                 // entrées incohérentes rejetées (IKB-B-FR-011)
    val partial: Boolean,                     // true si une source était indisponible
)
```

**Comportement** :
- `fetch` : récupère taxonomie OFF (E-numbers, aliases, risque) + attributs Ciqual quand disponibles ; valide à l'ingestion (E-number unique, `risk_level` valide, attributs Ciqual cohérents) ; rejette/trace les entrées incohérentes (`IKB-B-FR-011`) ; n'invente rien (`IKB-B-FR-007`).
- Échec réseau → lève une erreur domaine (le coordinator transforme en `KbRefreshOutcome.OFFLINE_FALLBACK`).

## `KbCacheStore` (interface)

Cache offline persistant (testable avec un fake ; impl `FileKbCacheStore` via `filesDir`).

```kotlin
interface KbCacheStore {
    fun read(): KbCache?                  // null si absent/corrompu (IKB-B-FR-010)
    fun write(cache: KbCache)             // écriture atomique (.tmp → rename)
    fun clear()
}

data class KbCache(
    val baseVersion: String,
    val additives: List<AdditiveFactCard>,
    val allergens: List<AllergenFactCard>,
    val refreshedAt: Long,
    val sources: List<String>,
)
```

**Comportement** :
- `read` : retourne `null` si absent ou illisible (corrompu) — pas d'exception bloquante ; le coordinator replie sur la baseline.
- `write` : écriture atomique (`.tmp` → rename) pour éviter un cache corrompu en cas d'interruption.

## `KbRefreshCoordinator` (orchestrateur)

```kotlin
class KbRefreshCoordinator(
    private val gateway: KbRefreshGateway,
    private val cacheStore: KbCacheStore,
    private val baseline: ReferenceKb,           // EmbeddedReferenceKb (assets IKB-A)
    private val onRefreshed: (ReferenceKb) -> Unit,  // swap index dans RefreshableReferenceKb
) {
    suspend fun refreshAtStartup(): KbRefreshOutcome
}
```

**Comportement** :
- `refreshAtStartup` (sur `Dispatchers.IO`, non bloquant) :
  1. tente `gateway.fetch()` ;
  2. succès/partiel → `cacheStore.write(...)` + publie la nouvelle version via `onRefreshed` → `KbRefreshOutcome(SUCCESS|PARTIAL)` ;
  3. échec réseau/parse → `KbRefreshOutcome(OFFLINE_FALLBACK)` (cache/baseline conservé, raison tracée).
- Aucun blocage de l'UI ; l'index courant (cache ou baseline) reste utilisé jusqu'à publication de la version rafraîchie (`IKB-B-FR-002`).

## `RefreshableReferenceKb` (implémentation `ReferenceKb`)

```kotlin
class RefreshableReferenceKb(
    private val cacheStore: KbCacheStore,
    private val baseline: ReferenceKb,
) : ReferenceKb {
    fun current(): ReferenceKb            // cache si valide, sinon baseline
    override fun lookup(...): LookupOutcome  // délègue à current()
    override fun baseVersion(): String
    fun publishRefreshed(refreshed: ReferenceKb)   // swap atomique (volatile)
}
```

**Comportement** : expose immédiatement la version courante (cache → baseline) ; `publishRefreshed` swap l'index de façon atomique quand le refresh aboutit. Le contrat `ReferenceKb` (IKB-A) est inchangé pour le core.

## Tests de conformité minimaux

- **Refresh succès** : gateway fake renvoie payload valide → `KbRefreshOutcome.SUCCESS`, cache écrit, index swapped.
- **Offline fallback** : gateway fake lève erreur réseau → `KbRefreshOutcome.OFFLINE_FALLBACK`, cache/baseline conservé, pas d'exception propagée.
- **Cache corrompu** : `cacheStore.read()` retourne `null` → `current()` = baseline (`IKB-B-FR-010`).
- **Entrée incohérente** : gateway fake renvoie E-number dupliqué / `risk_level` invalide → entrée rejetée, `rejectedEntries > 0`, pas d'invention.
- **Non-blocage** : `refreshAtStartup` ne bloque pas `lookup` sur la version courante (`IKB-B-SC-006`).

## Non-objectifs

- Ce contrat **ne publie pas** de nouveau read-model : le `ReferenceContext` (IKB-A) reste le seul Published Language vers le core.
- Ce contrat **ne définit pas** de fréquence autre que « au démarrage » (pas de WorkManager périodique au P1).
- Ce contrat **ne couvre pas** le lookup code-barres produit OFF (feature ultérieure).
