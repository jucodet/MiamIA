# Implementation Plan: ocr-dot-end-capture

**Branch**: `017-ocr-dot-end-capture` | **Date**: 2026-05-11 | **Spec**: [`spec.md`](./spec.md)
**Input**: Spécification domaine `ingredient-normalization-validation` — le `.` n'est fin de capture que s'il est suivi d'un espace ou d'un retour à la ligne ; `!` et `?` restent inconditionnels.

**Note**: Phases 0–1 documentées ci-dessous ; `tasks.md` est produit par `/speckit-tasks` (Phase 2 livrable).

## Summary

Modifier la logique de détection de fin de segment ingrédients dans `IngredientSegmentBoundaryResolver` pour que le caractère `.` ne soit reconnu comme terminateur de capture **que** lorsqu'il est suivi d'un espace (`' '`) ou d'un retour à la ligne (`'\n'`) (**FR-003 révisé**). Les points internes (codes additifs « E.621 », abréviations « vit.B12 ») ne coupent plus la liste. Les caractères `!` et `?` restent des terminateurs inconditionnels. Enrichir les fixtures OCR et les tests d'acceptation pour couvrir les cas de points internes (**SC-001**). Aucun changement de navigation, de modèle de données amont, ni de frontière DDD.

## Technical Context

**Language/Version**: Kotlin (JVM cible Android), Gradle Kotlin DSL
**Primary Dependencies**: Jetpack Compose, CameraX, ViewModel, coroutines ; module unique `app`
**Storage**: N/A pour ce changement (pas d'impact base Room ni `ValidatedIngredientRepository`)
**Testing**: JUnit 4 (tests unitaires JVM `app/src/test/`), AndroidJUnit4 (tests instrumentés `app/src/androidTest/`) ; pattern ATDD via `*AcceptanceTest.kt`
**Target Platform**: Android (API min 26, compile/target 34)
**Project Type**: application mobile monolithique (`app/`)
**Performance Goals**: **SC-001** (100 % des propositions respectent FR-002 à FR-006) ; pas de régression de latence sur `resolveEnd`
**Constraints**: changement chirurgical dans `IngredientSegmentBoundaryResolver.resolveEnd()` ; aucun impact sur `IngredientAnchorNormalizer`, `AnalysisSubmissionGate`, ni sur les écrans UI
**Scale/Scope**: 1 fichier source modifié, 1 fichier fixtures enrichi, 2–3 fichiers tests ajoutés/modifiés

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| **I. Qualité / traçabilité** | Spec domaine → tests d'acceptation → code ; incrément testable unitairement. |
| **II. ATDD** | Scénarios US1 §1–§2 dans `spec.md` ; tests acceptance ajoutés avant code (red→green). |
| **III. UX** | Pas d'impact UI directe ; la proposition affichée sera plus longue/correcte pour les étiquettes avec codes additifs. |
| **IV. Performance** | Pas de boucle supplémentaire significative ; test de non-régression existant (`IngredientSegmentPerformanceTest`). |
| **V. Simplicité** | Ajout d'un `if` contextuel dans un `when` existant — complexité minimale. |
| **VI. DDD** | Domaine `ingredient-normalization-validation` ; pas de fuite vers d'autres contextes. |

**Post-design (Phase 1)** : aucune violation nouvelle ; frontières préservées.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-normalization-validation/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1
└── tasks.md             # /speckit-tasks (non créé par ce plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/foodgpt/analysis/ingredientsegment/
├── IngredientSegmentBoundaryResolver.kt   # Modifié : logique '.' + espace/newline
├── IngredientSegmentModels.kt             # Inchangé (enum existant suffit)
├── IngredientSegmentPreparationService.kt # Inchangé (orchestration)
├── IngredientAnchorNormalizer.kt          # Inchangé
└── AnalysisSubmissionGate.kt              # Inchangé

app/src/test/java/com/foodgpt/analysis/ingredientsegment/
├── fixtures/OcrFixtures.kt                               # Enrichi : cas points internes
├── IngredientSegmentBoundaryResolverTest.kt               # Enrichi : tests '.' contextuel
├── IngredientSegmentPhraseBoundaryAcceptanceTest.kt       # Enrichi : scénario US1 §2
└── (autres tests existants inchangés)
```

**Structure Decision**: Module unique `app` ; changement limité au package `analysis.ingredientsegment`.

## Phase 0 — Recherche

**Statut**: terminé — voir [`research.md`](./research.md). Aucun `NEEDS CLARIFICATION` restant.

## Phase 1 — Design & contrats

**Statut**: terminé.

- [`data-model.md`](./data-model.md) — aucune nouvelle entité ; documentation de l'invariant contextuel sur `SENTENCE_TERMINATOR`.
- [`contracts/boundary-resolver-contract.md`](./contracts/boundary-resolver-contract.md) — contrat de `resolveEnd` révisé.
- [`quickstart.md`](./quickstart.md) — validation manuelle (point interne, point + espace, `!`, `?`).

**Agent context**: `.cursor/rules/specify-rules.mdc` mis à jour vers `specs/domains/ingredient-normalization-validation/plan.md`.

## Phase 2 — Tâches d'implémentation

Hors fichier : exécuter **`/speckit-tasks`** pour générer ou mettre à jour `tasks.md`.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier pour ce plan.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
