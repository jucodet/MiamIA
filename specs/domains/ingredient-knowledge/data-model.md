# Data Model — ingredient-knowledge (Feature IKB-A)

**Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Entities

### `RiskLevel` (enum)

Niveau de risque indicatif d'un additif, dérivé des étiquettes de risque OpenFoodFacts.

- `FAIBLE`
- `MODERE`
- `ELEVE`

### `KbSource`

Trace la provenance et la version d'une fiche / de la base.

- `origin`: `OFF_ADDITIVES_TAXONOMY` | `EU_ALLERGEN_LIST`
- `baseVersion`: `String` (version de la base, lue depuis `kb-version.json`)
- `sourceRef`: `String?` (référence optionnelle de la source)

### `AdditiveFactCard`

Fiche référence additif. **Clé primaire : `eNumber`** (`IKB-A-FR-012`).

- `eNumber`: `String` — identifiant réglementaire unique (ex. `E300`)
- `canonicalName`: `String` — dénomination canonique
- `aliases`: `List<String>` — dénominations courantes (alias de recherche, **pas** un synonyme métier)
- `role`: `String?` — rôle technologique (ex. antioxydant)
- `riskLevel`: `RiskLevel` — niveau de risque indicatif (`IKB-A-FR-013`)
- `source`: `KbSource`

**Invariants** :
- `eNumber` unique dans la base.
- Une recherche par alias normalisé renvoie la **même** fiche canonique (déduplication par `eNumber`).
- Aucun alias ne crée de règle d'équivalence hors `EquivalencePolicy` v1 stricte.

### `AllergenFactCard`

Fiche référence allergène réglementaire UE.

- `id`: `String` — identifiant réglementaire (ex. `GLUTEN`, `LAIT`)
- `regulatoryName`: `String` — dénomination réglementaire UE
- `aliases`: `List<String>` — dénominations courantes (alias de recherche)
- `source`: `KbSource`

**Invariants** : `id` unique ; 14 entrées au maximum (allergènes réglementaires UE).

### `IngredientDesignation`

Désignation d'ingrédient issue du segment, candidate au lookup.

- `rawText`: `String` — texte tel que présent dans le `ValidatedIngredientSegment`
- `normalized`: `String` — forme normalisée (casse, espaces, accents) via `MechanicalNormalizer`

**Règle** : la normalisation est **mécanique et listée** (casse → minuscules, espaces → collapse, accents → forme ASCII) ; aucune autre transformation (`IKB-A-FR-002`).

### `LookupOutcome`

Résultat du lookup depuis une liste d'ingrédients.

- `matchedAdditives`: `List<AdditiveFactCard>`
- `matchedAllergens`: `List<AllergenFactCard>`
- `unmatchedDesignations`: `List<IngredientDesignation>` — désignations non référencées (repli silencieux)
- `baseVersion`: `String` — version de la base utilisée

**Invariants** : aucune fiche inventée ; `unmatchedDesignations` ne déclenche aucun blocage (`IKB-A-FR-003`/`007`).

### `ReferenceContext`

Contexte de référence publié pour injection LLM. **Ensemble borné** (`IKB-A-FR-011`).

- `cards`: `List<ReferenceContextEntry>` — sélection **bornée** par plafond N (défaut 12)
- `qualification`: `GENERAL` (constante — contenu général, `IHI-C-FR-004`)
- `baseVersion`: `String`

**Priorisation de sélection** : allergènes d'abord, puis additifs `ELEVE`, puis `MODERE`, puis `FAIBLE`. Fiches au-delà du plafond : omises (repli silencieux).

**Invariants** :
- Aucune entrée ne présente un fait comme issu de l'étiquette (`IKB-A-FR-005`).
- `qualification` toujours `GENERAL`.

### `ReferenceContextEntry`

Projection d'une fiche en entrée de contexte (forme sérialisable pour le prompt).

- `kind`: `ADDITIVE` | `ALLERGEN`
- `key`: `String` — E-number ou id allergène
- `display`: `String` — libellé canonique
- `riskLevel`: `RiskLevel?` — pour additifs
- `role`: `String?` — pour additifs

## Relationships

```text
IngredientDesignation ──(lookup)──▶ LookupOutcome
                                    ├── AdditiveFactCard (key: eNumber)
                                    └── AllergenFactCard  (key: id)

LookupOutcome ──(build, cap N, prioritize)──▶ ReferenceContext
                                                └── ReferenceContextEntry
```

## Validation Rules (mapped to FRs)

| Règle | FR |
|-------|----|
| E-number unique, alias rattachés à la même fiche | `IKB-A-FR-012` |
| Matching sous-chaîne + normalisations mécaniques listées | `IKB-A-FR-002`, `IKB-A-FR-006` |
| Risque sur échelle 3 niveaux dérivée OFF | `IKB-A-FR-013` |
| Aucune fiche pour substance non référencée | `IKB-A-FR-003` |
| Repli silencieux (pas de blocage) | `IKB-A-FR-007` |
| Plafond N + priorisation allergènes→risque élevé | `IKB-A-FR-011` |
| Qualification GENERAL, aucun fait étiquette | `IKB-A-FR-004`/`005` |
| Traçabilité source/version par fiche | `IKB-A-FR-009` |
| Base absente/illisible → erreur domaine explicite | `IKB-A-FR-010` |

## State Transitions

N/A — base référence statique versionnée ; pas de cycle de vie mutable au P1.

---

# Data Model — Feature IKB-B (Auto-update + enrichissement OFF/Ciqual)

**Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md) (Feature IKB-B)

## Entities (IKB-B)

### `CiqualAttributes` (nouveau)

Attributs de composition Ciqual optionnels rattachés à un additif **quand disponibles**.

- `energyKcalPer100g`: `Double?` — énergie (kcal/100 g) si pertinent et disponible
- `otherAttributes`: `Map<String, String>` — attributs nutritionnels additionnels (clé normalisée)
- `source`: `KbSource` — `origin = Ciqual` (à étendre : ajouter `CIQUAL` à `KbSource.Origin`)

**Invariants** : attributs absents → `null`/map vide (repli silencieux, `IKB-B-FR-007`) ; aucune donnée inventée ; tout attribut est traçable (`IKB-B-FR-008`).

### `AdditiveFactCard` (étendu)

Extension de l'entité IKB-A :

- +(existant) `eNumber`, `canonicalName`, `aliases`, `role`, `riskLevel`, `source` (OFF)
- `ciqual`: `CiqualAttributes?` — attributs Ciqual quand disponibles (`IKB-B-FR-007`)

**Invariants inchangés** : clé primaire E-number ; aliases = recherche (pas de synonyme métier) ; attributs Ciqual = contenu général (`IKB-B-FR-009`).

### `KbCache` (nouveau)

Version rafraîchie **persistée localement** (offline) pour les démarrages suivants.

- `baseVersion`: `String`
- `additives`: `List<AdditiveFactCard>`
- `allergens`: `List<AllergenFactCard>`
- `refreshedAt`: `Long` — timestamp (ms)
- `sources`: `List<String>` — sources consultées (OFF, Ciqual)

**Invariants** : écriture atomique (`.tmp` → rename) ; lisible offline ; relecture → index in-memory.

### `KbBaseline` (nouveau — concept)

Version **embarquée** dans l'APK (assets IKB-A) = filet de sécurité ultime. Modélisé par l'impl `EmbeddedReferenceKb` existante (IKB-A). Aucun changement de structure : sert de source quand cache absent/corrompu + réseau absent.

### `KbRefreshOutcome` (nouveau)

Résultat du rafraîchissement au démarrage.

- `status`: `SUCCESS` | `PARTIAL` | `OFFLINE_FALLBACK`
- `baseVersion`: `String?` — version obtenue (null si offline fallback)
- `sourcesConsulted`: `List<String>`
- `sourcesAvailable`: `List<String>` — sources réellement exploitées
- `refreshedAt`: `Long`
- `failureReason`: `String?` — raison d'échec éventuelle (réseau, parse, incohérence)
- `rejectedEntries`: `Int` — nombre d'entrées amont rejetées (incohérentes)

**Invariants** : `OFFLINE_FALLBACK` ⇒ `baseVersion` hérité du cache/baseline ; aucune invention ; `rejectedEntries` tracé (`IKB-B-FR-011`).

## Relationships (IKB-B)

```text
Au démarrage :
  KbCacheStore ──(read)──▶ KbCache? ──(index)──▶ RefreshableReferenceKb
                                │ (absent/corrompu)
                                ▼
                          KbBaseline (assets) ──▶ RefreshableReferenceKb

  KbRefreshCoordinator ──(fetch)──▶ KbRefreshGateway (OFF + Ciqual)
                                  ──(validate)──▶ KbRefreshOutcome
                                  ──(persist)──▶ KbCacheStore (atomic write)
                                  ──(swap index)──▶ RefreshableReferenceKb

RefreshableReferenceKb : ReferenceKb  (lookup sur version courante)
```

## Validation Rules (mapped to FRs) — IKB-B

| Règle | FR |
|-------|----|
| Refresh asynchrone non bloquant au démarrage | `IKB-B-FR-001`/`002` |
| Repli offline : cache → baseline | `IKB-B-FR-003`/`005` |
| Persistance locale du cache | `IKB-B-FR-004` |
| Couverture exhaustive OFF | `IKB-B-FR-006` |
| Attributs Ciqual optionnels (repli silencieux si absents) | `IKB-B-FR-007` |
| Traçabilité source/version (OFF + Ciqual) | `IKB-B-FR-008` |
| Attributs Ciqual = contenu général, aucune extension d'équivalence | `IKB-B-FR-009` |
| Cache corrompu → erreur domaine + repli baseline | `IKB-B-FR-010` |
| Entrées amont incohérentes rejetées/tracées | `IKB-B-FR-011` |

## State Transitions (IKB-B)

```text
[STARTUP] → read cache
   ├── cache valid   → index = cache ; kick refresh (async)
   └── cache invalid → index = baseline ; kick refresh (async)

refresh → SUCCESS    → persist cache ; swap index → refreshed
        → PARTIAL    → persist cache (sources disponibles) ; swap index
        → OFFLINE    → keep current index (cache or baseline) ; trace failure
```

