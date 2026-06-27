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
