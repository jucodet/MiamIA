# Quickstart — ingredient-knowledge (Feature IKB-A)

**Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Vérification manuelle du lookup + injection (jeu fixe)

### Prérequis

- Branche `024-offline-ingredient-kb` active.
- `app/src/main/assets/ingredientkb/{additives.json, allergens.json, kb-version.json}` présents.
- Tests JVM : `./gradlew :app:testDebugUnitTest --tests "com.miamia.ingredientknowledge.*"`.

### Scénario 1 — Lookup offline sur le jeu fixe (US-IKB-A1)

1. Ouvrir `ReferenceIngredientFixtures` (jeu fixe d'ingrédients).
2. Exécuter `InMemoryReferenceKbLookupTest` :
   - désignations d'additifs référencés (E-number + dénomination courante) → **une** fiche canonique par additif ;
   - désignation d'allergène réglementaire → fiche allergène ;
   - désignation non référencée → `unmatchedDesignations`, aucune fiche inventée.
3. Attendu : `IKB-A-SC-001` (fiches canoniques attendues) + `IKB-A-SC-002` (aucune invention).

### Scénario 2 — Constitution du `ReferenceContext` (US-IKB-A2)

1. Exécuter `ReferenceContextBuilderTest` avec un lookup retournant > N fiches.
2. Attendu :
   - seules les N fiches prioritaires (allergènes, puis additifs `ELEVE`, `MODERE`, `FAIBLE`) sont retenues ;
   - `qualification = GENERAL` ;
   - aucune formulation de fait étiquette.
3. Attendu : `IKB-A-SC-003` + `IKB-A-SC-007`.

### Scénario 3 — Charge des assets réels (US-IKB-A1, offline)

1. Exécuter `EmbeddedReferenceKbRobolectricTest` (Robolectric) :
   - charge `additives.json` / `allergens.json` / `kb-version.json` ;
   - `baseVersion()` renvoie la version lue ;
   - lookup exploitable sans réseau.
2. Attendu : `IKB-A-SC-005` (traçabilité source/version) + `IKB-A-SC-006` (offline intégral).

### Scénario 4 — Répétabilité du jeu fixe (US-IKB-A3)

1. Exécuter la suite `ingredientknowledge` 3 fois consécutives.
2. Attendu : résultats identiques (`IKB-A-SC-004`).

## Cas d'erreur à vérifier

- Liste d'ingrédients vide / sans substance référencée → `ReferenceContext` vide, pas de blocage.
- Base absente ou illisible → erreur domaine explicite (`IKB-A-FR-010`), pas de contexte inventé.
- Variante orthographique non couverte par normalisation mécanique → `unmatchedDesignations`, pas de fiche.

## Hors périmètre P1 (ne pas tester ici)

- Valeurs nutritionnelles (Ciqual) / kcal.
- Lookup code-barres OpenFoodFacts.
- Enrichissement réseau avec cache.

---

# Quickstart — Feature IKB-B (Auto-update base additifs + enrichissement OFF/Ciqual)

**Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md) (Feature IKB-B) | **Plan**: [plan.md](./plan.md)

## Vérification manuelle du refresh + offline fallback

### Prérequis

- Branche `025-kb-auto-update-enrich` active.
- Permission `INTERNET` présente (déjà utilisée par `GemmaModelDownloader`).
- Tests JVM : `./gradlew :app:testDebugUnitTest --tests "com.miamia.ingredientknowledge.*"`.

### Scénario 1 — Refresh au démarrage avec réseau (US-IKB-B1)

1. Utiliser un `KbRefreshCoordinator` avec un `FakeKbRefreshGateway` renvoyant un payload valide (taxonomie OFF complète + attributs Ciqual).
2. Appeler `refreshAtStartup()`.
3. Attendu : `KbRefreshOutcome(SUCCESS)` ; cache écrit (`cacheStore.write` appelé) ; index swapped (`onRefreshed` invoqué) ; `baseVersion` reflète la version rafraîchie.
4. Vérifier `IKB-B-SC-001` : lookup suivant utilise la version rafraîchie.

### Scénario 2 — Offline fallback (US-IKB-B2)

1. `FakeKbRefreshGateway` lève une erreur réseau ; cache persisté présent.
2. Appeler `refreshAtStartup()`.
3. Attendu : `KbRefreshOutcome(OFFLINE_FALLBACK)` avec `failureReason` renseigné ; `current()` = cache persisté ; aucune exception propagée ; lookup exploitable.
4. Répéter avec cache absent/corrompu → `current()` = baseline embarquée (`IKB-B-FR-010`).
5. Vérifier `IKB-B-SC-002`.

### Scénario 3 — Non-blocage du refresh (US-IKB-B1)

1. Lancer `refreshAtStartup()` (async, `Dispatchers.IO`) ; pendant ce temps, appeler `lookup(...)` sur `RefreshableReferenceKb`.
2. Attendu : `lookup` répond immédiatement sur la version courante (cache/baseline), sans attendre la fin du refresh.
3. Vérifier `IKB-B-SC-006` (0 % blocage UI).

### Scénario 4 — Couverture exhaustive + attributs Ciqual (US-IKB-B3)

1. `FakeKbRefreshGateway` renvoie la taxonomie OFF complète + attributs Ciqual pour un sous-ensemble.
2. Attendu : tous les E-numbers présents dans la base enrichie (`IKB-B-SC-003`) ; attributs Ciqual présents et traçables (`SC-004`) ; attributs absents omis sans invention (`SC-005`).
3. Vérifier qu'un `ReferenceContext` construit depuis la base enrichie reste `qualification = GENERAL` (`IKB-B-SC-007`).

### Scénario 5 — Entrées amont incohérentes (edge case)

1. `FakeKbRefreshGateway` renvoie un E-number dupliqué + un `risk_level` invalide.
2. Attendu : entrées rejetées/tracées, `rejectedEntries > 0` ; pas d'invention ; pas de blocage (`IKB-B-FR-011`).

## Hors périmètre IKB-B (ne pas tester ici)

- Lookup code-barres produit OpenFoodFacts (feature ultérieure).
- Refresh périodique en arrière-plan (WorkManager) — la spec exige « à chaque démarrage » uniquement.
