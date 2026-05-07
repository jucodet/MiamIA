# Tasks: ingredient-phrase-segment

**Input**: Design documents from `specs/domains/ingredient-normalization-validation/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: ATDD obligatoire (constitution) — au minimum une tâche de test par user story, rédigée avant l’implémentation ciblée.

**Organization**: Phases par user story (P1 → P3), puis polish transversal.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: peut s’exécuter en parallèle (fichiers différents, pas de dépendance sur une tâche incomplète du même lot bloquant)
- **[Story]**: `[US1]`, `[US2]`, `[US3]` pour les phases user story uniquement

---

## Phase 1: Setup (documentation & jeux de test)

**Purpose**: préparer la traçabilité domaine et les fixtures alignées sur `quickstart.md`.

- [x] T001 Mettre à jour l’entrée de migration (règle unique vs ancienne ancre `ingredients:`) dans `specs/domains/ingredient-normalization-validation/migration-index.md`
- [x] T002 [P] Étendre les chaînes de test nominaux FR/EN (singulier+pluriel, multi-ancres, sans ancre) dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/fixtures/OcrFixtures.kt` selon `specs/domains/ingredient-normalization-validation/quickstart.md`

---

## Phase 2: Foundational (prérequis modèle)

**Purpose**: étendre le modèle de résultat pour distinguer la **raison de borne** (phrase / ligne / fin de texte) sans casser les appels existants.

**⚠️ CRITICAL**: Aucune story ne peut être considérée “terminée” tant que l’extraction ne reflète pas FR-003–FR-005.

- [x] T003 Étendre `IngredientSegmentFallbackMode` et/ou ajouter un champ de borne (ex. `boundaryEndReason`) dans `IngredientSegmentExtraction` dans `app/src/main/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentModels.kt` pour refléter `sentence_terminator | line_end | text_end` du contrat `specs/domains/ingredient-normalization-validation/contracts/ingredient-segment-boundary-contract.md`

**Checkpoint**: compilation des tests existants possible après adaptation des constructeurs `IngredientSegmentExtraction` dans les tests.

---

## Phase 3: User Story 1 — Proposer une liste d’ingrédients (Priority: P1) 🎯 MVP

**Goal**: première ancre FR/EN reconnue, borne fin phrase → fin de ligne → fin de texte, première occurrence seulement (FR-002–FR-006).

**Independent Test**: jeux `OcrFixtures` + assertions sur `IngredientSegmentPreparationService.prepare()` comme dans `spec.md` US1.

### Tests for User Story 1 (MANDATORY) ⚠️

> Rédiger en premier, faire échouer avant l’implémentation (ATDD).

- [x] T004 [P] [US1] Ajouter les scénarios US1 (ponctuation finale, fin de ligne, monoligne, double ancre, ancre EN) dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPhraseBoundaryAcceptanceTest.kt`
- [x] T005 [P] [US1] Renforcer les tests unitaires de `IngredientSegmentBoundaryResolver` dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt` (appels directs au resolver, pas seulement via le service)
- [x] T006 [P] [US1] Mettre à jour les attentes du contrat de préparation dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPreparationContractTest.kt`
- [x] T007 [P] [US1] Mettre à jour les cas nominaux dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentExtractionAcceptanceTest.kt`

### Implementation for User Story 1

- [x] T008 [US1] Remplacer la logique d’ancre (supprimer la priorité `ingredients:` / normalisation obsolète) par la détection **première phrase-ancre** FR/EN dans `app/src/main/java/com/foodgpt/analysis/ingredientsegment/IngredientAnchorNormalizer.kt`
- [x] T009 [US1] Implémenter la hiérarchie de fin FR-003–FR-005 dans `app/src/main/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentBoundaryResolver.kt`
- [x] T010 [US1] Brancher `prepare()` sur la nouvelle détection d’ancre + résolution de borne dans `app/src/main/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPreparationService.kt`
- [x] T011 [US1] Corriger les régressions dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentFallbackAcceptanceTest.kt`, `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPreparationServiceTest.kt` et `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPerformanceTest.kt`

**Checkpoint**: US1 verte en isolation (`prepare` + tests associés).

---

## Phase 4: User Story 2 — Confirmer ou corriger avant analyse (Priority: P2)

**Goal**: segment “label seul” ou non confirmé → pas d’analyse aval (FR-007–FR-008).

**Independent Test**: `AnalysisSubmissionGate.evaluate()` avec `userConfirmed` true/false et segments limites.

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T012 [P] [US2] Couvrir le rejet “étiquette seule” pour `Ingredient` / `Ingredients` / `Ingrédient(s)` dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/AnalysisSubmissionGateContractTest.kt`
- [x] T013 [P] [US2] Vérifier le blocage sans confirmation explicite dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/AnalysisSubmissionDecisionAcceptanceTest.kt`

### Implementation for User Story 2

- [x] T014 [US2] Étendre `ingredientsLabelOnlyRegex` et la normalisation de contrôle dans `app/src/main/java/com/foodgpt/analysis/ingredientsegment/AnalysisSubmissionGate.kt`
- [x] T015 [US2] Vérifier que les appels `submissionGate.evaluate(..., userConfirmed = …)` dans `app/src/main/java/com/foodgpt/camera/CameraViewModel.kt` respectent toujours FR-007 (aperçu vs confirmation)

**Checkpoint**: gate cohérent avec les ancres FR/EN et la confirmation utilisateur.

---

## Phase 5: User Story 3 — Absence de liste exploitable (Priority: P3)

**Goal**: ancre absente ou segment vide → état bloqué explicite, pas de liste factice (FR-008).

**Independent Test**: `OcrFixtures.NO_ANCHOR` + segment vide après trim.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T016 [P] [US3] Ajouter/renforcer le cas sans ancre dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentFallbackAcceptanceTest.kt`
- [x] T017 [P] [US3] Couvrir `ANCHOR_MISSING_BLOCKED` / segment vide dans `app/src/test/java/com/foodgpt/analysis/ingredientsegment/IngredientSegmentPreparationServiceTest.kt`

### Implementation for User Story 3

- [x] T018 [US3] Mettre à jour le libellé utilisateur `no-canonical-anchor` pour refléter les ancres `Ingrédient(s)` / `Ingredient(s)` dans `app/src/main/java/com/foodgpt/ingredients/ScanFailureMessageBuilder.kt`

**Checkpoint**: messages et états bloqués alignés sur la spec.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: aligner le reste du pipeline reconnaissance / documentation / non-régression.

- [x] T019 [P] Aligner `detectAnchors` / `extractOrderedItems` sur la même politique d’ancre (ou factoriser vers le service de segment) dans `app/src/main/java/com/foodgpt/recognition/IngredientExtractionPipeline.kt`
- [x] T020 [P] Marquer la spec domaine et le plan comme validés côté implémentation dans `specs/domains/ingredient-normalization-validation/migration-index.md`
- [x] T021 Exécuter la suite ciblée depuis la racine du dépôt `e:\Dev\projects\FoodGpt` : `.\gradlew.bat :app:testDebugUnitTest --tests "*IngredientSegment*" --tests "*IngredientAnchor*"`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → pas de dépendance.
- **Phase 2** → après Phase 1 (fixtures utiles pour T003 mais T003 peut suivre immédiatement T001–T002).
- **Phase 3** → après Phase 2 ; **tests T004–T007 avant T008–T011** (ATDD).
- **Phase 4** → après Phase 3 (s’appuie sur `IngredientSegmentExtraction` stable).
- **Phase 5** → peut chevaucher Phase 4 après T010 si messages indépendants, mais recommandé après Phase 3.
- **Phase 6** → après les stories souhaitées livrées.

### User Story Dependencies

- **US1**: aucune dépendance sur US2/US3 ; **MVP**.
- **US2**: dépend de la forme de `IngredientSegmentExtraction` (US1).
- **US3**: partiellement parallèle à US2 ; message `ScanFailureMessageBuilder` dépend du flux qui émet `no-canonical-anchor`.

### Parallel Opportunities

- **T002, T004–T007, T012–T013, T016–T017, T019–T020** : parallélisables si pas de conflit sur les mêmes fichiers.
- **T008–T010** : séquentiel (ancre → borne → orchestration `prepare`).

---

## Parallel Example: User Story 1

```bash
# Lancer en parallèle la rédaction des tests ATDD US1 :
# - IngredientSegmentPhraseBoundaryAcceptanceTest.kt
# - IngredientSegmentBoundaryResolverTest.kt
# - IngredientSegmentPreparationContractTest.kt
# - IngredientSegmentExtractionAcceptanceTest.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1 + Phase 2  
2. Écrire T004–T007 (échec attendu)  
3. Implémenter T008–T011  
4. Valider US1 seule  

### Incremental Delivery

1. US1 (MVP)  
2. US2 (gate + confirmation)  
3. US3 (messages bloquants)  
4. Phase 6 (pipeline + non-régression)  

---

## Report Summary

| Métrique | Valeur |
|----------|--------|
| **Total tasks** | 21 |
| **US1 tasks** | 8 (4 tests + 4 impl) |
| **US2 tasks** | 4 (2 tests + 2 impl) |
| **US3 tasks** | 3 (2 tests + 1 impl) |
| **Setup + Foundational + Polish** | 6 |
| **MVP scope** | Phase 1–3 (T001–T011) |

**Validation format**: toutes les lignes de tâche suivent `- [ ] Tnnn [P?] [USx?] … chemin/fichier`.
