# Implementation Plan: ingredient-knowledge — Feature IKB-A (KB référence additifs/allergènes offline + injection contexte)

**Branch**: `024-offline-ingredient-kb` | **Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/domains/ingredient-knowledge/spec.md` + clarify session 2026-06-27

## Summary

La **Feature IKB-A** fournit une **base référence embarquée** (additifs E-numbers issus de la taxonomie OpenFoodFacts + 14 allergènes réglementaires UE), un **lookup offline** depuis une liste d'ingrédients (sous-chaîne littérale + normalisations mécaniques casse/espaces/accents), et un **`ReferenceContext`** borné et priorisé (allergènes puis additifs à risque élevé) publié comme **contenu général** à destination des flux LLM composition + critique du domaine `ingredient-health-intelligence`. Le domaine ne produit aucune analyse produit et n'étend pas la `EquivalencePolicy` v1 stricte du core.

## Technical Context

**Language/Version**: Kotlin 2.x, Android (minSdk 26, targetSdk 34)
**Primary Dependencies**: `kotlinx.serialization` (parsing JSON taxonomie OFF/allergènes), Android `assets` (base embarquée), JUnit 4 + Robolectric (tests lisant les assets)
**Storage**: Android `assets` en lecture seule (`app/src/main/assets/ingredientkb/`) + index in-memory au démarrage ; **pas de Room au P1** (base statique, versionnée via `kb-version.json`)
**Testing**: JUnit 4 JVM pur (implémentation in-memory + fixtures, style `GemmaBilanParserTest`) ; Robolectric uniquement pour les tests chargeant les assets réels
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique, nouveau package `com.miamia.ingredientknowledge`
**Performance Goals**: lookup p95 < 20 ms pour ≤ 50 désignations ; base embarquée < 5 Mo ; `ReferenceContext` injecté ≤ plafond N fiches (valeur par défaut 12, configurable)
**Constraints**: offline intégral (aucune dépendance réseau au P1) ; respect Feature C — contexte qualifié « contenu général » ; aucune extension de `EquivalencePolicy` v1 stricte ; `MAX_INPUT_CHARS = 12 000` côté LLM consommateur ( borne indirecte via plafond N fiches)
**Scale/Scope**: ~600 E-numbers (extrait taxonomie OFF) + 14 allergènes UE ; 1 jeu fixe d'ingrédients de référence ; ~8–10 nouvelles classes + 4–5 classes de test

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec → jeu fixe (ATDD) → code ; traçabilité source/version par fiche (`IKB-A-FR-009`) |
| II. ATDD | OK — user stories avec Given/When/Then ; jeu fixe exécutable isolément (US-IKB-A3) |
| III. UX | N/A — pas d'UI directe ; le domaine publie un read-model consommé par les flux LLM (pas de parcours utilisateur propre) |
| IV. Performance | OK — objectifs mesurables : lookup p95 < 20 ms, base < 5 Mo, plafond N fiches |
| V. Simplicité | OK — interface `ReferenceKb` + impl in-memory ; pas de Room au P1 (base statique) |
| VI. Frontières DDD | OK — nouveau domaine `ingredient-knowledge` ; contrat *Published Language* avec `ingredient-health-intelligence` ; couche anti-corruption côté core (le core consomme un `ReferenceContext` projeté, pas le modèle interne) |

**Post-design** : inchangé (aucune violation à justifier).

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-knowledge/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── reference-context-read-model.md
│   └── ingredient-kb-lookup-contract.md
└── tasks.md   # /speckit-tasks (hors scope /speckit-plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/ingredientknowledge/
├── AdditiveFactCard.kt
├── AllergenFactCard.kt
├── IngredientDesignation.kt
├── ReferenceContext.kt
├── LookupOutcome.kt
├── KbSource.kt                  # source + version de base
├── RiskLevel.kt                 # enum FAIBLE / MODERE / ELEVE
├── MechanicalNormalizer.kt      # casse / espaces / accents
├── ReferenceKb.kt               # interface (anti-corruption)
├── IngredientKbLookup.kt        # sous-chaîne littérale + normalisation
├── ReferenceContextBuilder.kt   # plafond + priorisation allergènes→risque élevé
├── EmbeddedReferenceKb.kt       # impl Android assets (kotlinx.serialization)
└── InMemoryReferenceKb.kt       # impl test/fixture (JVM pur)

app/src/main/assets/ingredientkb/
├── additives.json               # extrait taxonomie OFF
├── allergens.json               # 14 allergènes réglementaires UE
└── kb-version.json              # version de base (source + version)

app/src/test/java/com/miamia/ingredientknowledge/
├── fixtures/
│   └── ReferenceIngredientFixtures.kt   # jeu fixe d'ingrédients (US-IKB-A3)
├── InMemoryReferenceKbLookupTest.kt     # lookup + repli silencieux
├── ReferenceContextBuilderTest.kt       # cap + priorisation
├── MechanicalNormalizerTest.kt
└── EmbeddedReferenceKbRobolectricTest.kt # charge les assets réels (Robolectric)
```

**Structure Decision** : nouveau package `ingredientknowledge` (borne context autonome). L'interface `ReferenceKb` sert d'**anti-corruption layer** : les flux LLM consommateurs (`composition`, `healthcritique`) ne dépendent que du contrat `ReferenceContext` (read-model), jamais du modèle interne ni d'Android assets. L'impl in-memory (`InMemoryReferenceKb`) rend les tests JVM pur exécutables sans Robolectric (style `GemmaBilanParserTest`), conformément à US-IKB-A3.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0 — Recherche

Voir [research.md](./research.md) : format de bundling (JSON assets), parsing (kotlinx.serialization), modèle d'index (E-number clé primaire + alias), matching (sous-chaîne + normalisations mécaniques), plafond d'injection et priorisation, conformité Feature C.

## Phase 1 — Design

- [data-model.md](./data-model.md) : `AdditiveFactCard`, `AllergenFactCard`, `IngredientDesignation`, `ReferenceContext`, `LookupOutcome`, `RiskLevel`, `KbSource`.
- [contracts/reference-context-read-model.md](./contracts/reference-context-read-model.md) : *Published Language* consommé par `ingredient-health-intelligence` (composition + critique).
- [contracts/ingredient-kb-lookup-contract.md](./contracts/ingredient-kb-lookup-contract.md) : interface `ReferenceKb` (frontière de domaine).
- [quickstart.md](./quickstart.md) : scénario manuel de vérification du lookup + injection sur le jeu fixe.

## Phase 2 — Livraison (hors scope de ce fichier)

Les tâches exécutables sont dans [tasks.md](./tasks.md) (commande `/speckit-tasks`).

---

# Implementation Plan: ingredient-knowledge — Feature IKB-B (Auto-update base additifs + enrichissement OFF/Ciqual)

**Branch**: `025-kb-auto-update-enrich` | **Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md) (Feature IKB-B)
**Input**: Feature specification Feature IKB-B + clarify session 2026-06-28

## Summary

La **Feature IKB-B** ajoute un **rafraîchissement asynchrone au démarrage** de la base référence additifs depuis les sources amont (**taxonomie OpenFoodFacts** en couverture exhaustive + **attributs Ciqual** quand disponibles), avec **repli offline en cascade** : dernière version **persistée localement** (cache `filesDir`) → à défaut **baseline embarquée** (APK). Rafraîchissement **non bloquant** ; conformité **Feature C** inchangée (attributs Ciqual = contenu général, aucune extension d'équivalence).

## Technical Context (IKB-B)

**Language/Version**: Kotlin 2.x, Android (minSdk 26, targetSdk 34)
**Primary Dependencies**: `java.net.HttpURLConnection` (pattern existant `GemmaModelDownloader` — pas de nouvelle dépendance HTTP), `kotlinx.serialization-json` (déjà ajouté en IKB-A), `kotlinx-coroutines` (async refresh sur `Dispatchers.IO`)
**Storage**: Cache persistant offline = fichier JSON versionné dans `Context.filesDir/ingredientkb/` (`additives.cache.json`, `allergens.cache.json`, `kb-version.cache.json`) ; index in-memory rechargé au démarrage. **Pas de Room** (base statique versionnée, cohérent avec IKB-A). Baseline embarquée (assets IKB-A) = filet de sécurité ultime.
**Testing**: JUnit 4 JVM pur (gateway avec fake HTTP fetcher + fake cache store), Robolectric (cache réel `filesDir` + assets baseline), `kotlinx-coroutines-test` (async refresh, repli offline)
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique, extension du package `com.miamia.ingredientknowledge`
**Performance Goals**: refresh non bloquant (l'app reste interactive, `IKB-B-SC-006`) ; lookup p95 < 20 ms conservé (index in-memory) ; cache offline < 10 Mo (taxonomie OFF complète + attributs Ciqual)
**Constraints**: offline-first (repli cache → baseline) ; respect Feature C (contenu général) ; aucune extension de `EquivalencePolicy` v1 stricte ; rafraîchissement non bloquant ; entrées amont incohérentes rejetées/tracées
**Scale/Scope**: taxonomie OFF additive complète (~500–600 E-numbers) + attributs Ciqual par substance quand disponibles ; ~6–8 nouvelles classes + 4–5 classes de test

## Constitution Check (IKB-B)

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — refresh tracé (`KbRefreshOutcome` : sources, version, timestamp, raison d'échec) ; entrées incohérentes rejetées/tracées |
| II. ATDD | OK — US-IKB-B1/B2/B3 avec Given/When/Then ; tests offline fallback + refresh non bloquant |
| III. UX | OK — refresh non bloquant (`IKB-B-FR-002`), pas d'erreur bloquante hors-ligne (`IKB-B-FR-003`) |
| IV. Performance | OK — refresh non bloquant mesurable (`SC-006`), lookup p95 < 20 ms conservé, cache < 10 Mo |
| V. Simplicité | OK — `HttpURLConnection` (pattern existant) + cache fichier JSON (pas de Room) ; anti-corruption via interfaces `KbRefreshGateway` / `KbCacheStore` |
| VI. Frontières DDD | OK — reste dans `ingredient-knowledge` ; `KbRefreshGateway` = frontière vers sources externes (anti-corruption OFF/Ciqual) ; read-model `ReferenceContext` inchangé pour le core |

**Post-design** : inchangé (aucune violation à justifier).

## Project Structure (IKB-B)

### Documentation (this feature)

```text
specs/domains/ingredient-knowledge/
├── plan.md                      # mis à jour (section IKB-B ajoutée)
├── research.md                  # mis à jour (section IKB-B ajoutée)
├── data-model.md                # mis à jour (section IKB-B ajoutée)
├── quickstart.md                # mis à jour (section IKB-B ajoutée)
├── contracts/
│   ├── reference-context-read-model.md   # inchangé
│   ├── ingredient-kb-lookup-contract.md  # inchangé
│   └── kb-refresh-gateway-contract.md    # NOUVEAU — frontière sources amont OFF/Ciqual + cache
└── tasks.md                     # /speckit-tasks (à regénérer pour IKB-B)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/ingredientknowledge/
├── ... (fichiers IKB-A inchangés)
├── CiqualAttributes.kt                 # attributs composition Ciqual optionnels
├── KbRefreshOutcome.kt                 # résultat refresh (succès/partiel/offline-fallback)
├── KbRefreshGateway.kt                 # interface (anti-corruption OFF/Ciqual)
├── OffCiqualRefreshGateway.kt          # impl HttpURLConnection (OFF taxonomy + Ciqual)
├── KbCacheStore.kt                     # interface cache offline
├── FileKbCacheStore.kt                 # impl filesDir JSON (kotlinx.serialization)
├── KbRefreshCoordinator.kt             # orchestrateur : refresh → cache → fallback baseline
└── RefreshableReferenceKb.kt           # ReferenceKb utilisant (cache | baseline) + refresh différé

app/src/test/java/com/miamia/ingredientknowledge/
├── ... (tests IKB-A inchangés)
├── KbRefreshCoordinatorTest.kt         # refresh succès / offline fallback / cache corrompu
├── FakeKbRefreshGatewayTest.kt         # gateway fake (sources partielles, incohérences)
├── FileKbCacheStoreRobolectricTest.kt  # cache réel filesDir + repli baseline
└── RefreshableReferenceKbTest.kt       # lookup sur version courante pendant refresh
```

**Structure Decision** : extension du package `ingredientknowledge`. Trois interfaces d'anti-corruption :
- `KbRefreshGateway` : isole les sources externes (OFF/Ciqual) — testable avec un fake.
- `KbCacheStore` : isole le stockage offline — testable avec un fake (JVM pur) + impl `filesDir` (Robolectric).
- `RefreshableReferenceKb` : implémente `ReferenceKb` en exposant la **version courante disponible** (cache ou baseline) et en déclenchant le refresh de façon **non bloquante** via `KbRefreshCoordinator`.

## Complexity Tracking (IKB-B)

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0 — Recherche (IKB-B)

Voir [research.md](./research.md) §IKB-B : sources amont (URLs taxonomie OFF + Ciqual), format fetch/parse, stratégie de cache (filesDir JSON vs Room), repli offline en cascade, refresh non bloquant, rejet des entrées incohérentes, conformité Feature C.

## Phase 1 — Design (IKB-B)

- [data-model.md](./data-model.md) §IKB-B : `AdditiveFactCard` étendu (attributs Ciqual), `CiqualAttributes`, `KbCache`, `KbRefreshOutcome`, `KbBaseline`.
- [contracts/kb-refresh-gateway-contract.md](./contracts/kb-refresh-gateway-contract.md) : interfaces `KbRefreshGateway` + `KbCacheStore` + orchestrateur `KbRefreshCoordinator`.
- [quickstart.md](./quickstart.md) §IKB-B : scénarios manuels refresh (réseau ok), offline fallback (cache puis baseline), entrée amont incohérente.

## Phase 2 — Livraison (hors scope de ce fichier)

Tâches exécutables dans [tasks.md](./tasks.md) (à regénérer via `/speckit-tasks` pour IKB-B).

