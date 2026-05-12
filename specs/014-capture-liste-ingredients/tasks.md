# Tasks: Ancrage ingredients avec espace avant deux-points

**Input**: Design documents from `/specs/014-capture-liste-ingredients/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Dans ce projet, les tests d acceptance/parcours (ATDD) sont obligatoires.

**Organization**: Taches groupees par user story pour permettre une implementation et validation independantes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Peut etre execute en parallele (fichiers differents, sans dependance)
- **[Story]**: Label story (`[US1]`, `[US2]`, `[US3]`)
- Chaque tache reference un chemin de fichier explicite

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparer l espace de tests et les points d extension du pipeline OCR.

- [X] T001 Creer les fichiers de tests d ancrage ingredients dans `app/src/test/java/com/miamia/recognition/IngredientAnchorDetectionTest.kt`
- [X] T002 [P] Creer le fichier de tests de contrat d ancrage dans `app/src/test/java/com/miamia/recognition/IngredientAnchorContractTest.kt`
- [X] T003 [P] Ajouter les fixtures OCR de cas intro/liste dans `app/src/test/java/com/miamia/recognition/IngredientAnchorFixtures.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Mettre en place les regles centrales de detection et selection d ancre.

**⚠️ CRITICAL**: Cette phase bloque toutes les user stories.

- [X] T004 Implementer la detection canonique `ingredients\\s*:` dans `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt`
- [X] T005 [P] Implementer la regle de selection `FIRST_CANONICAL_MATCH` dans `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt`
- [X] T006 [P] Ajouter la normalisation casse/espaces pour ancre dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientAnchorNormalizer.kt`
- [X] T007 Implementer la structure de sortie ancre (`candidates`, `selectedStartIndex`, `anchorFound`) dans `app/src/main/java/com/miamia/recognition/RecognitionContracts.kt`
- [X] T008 Ajouter le blocage explicite sans ancre canonique dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`

**Checkpoint**: Fondations prêtes - stories executables.

---

## Phase 3: User Story 1 - Capturer la vraie liste ingredients (Priority: P1) 🎯 MVP

**Goal**: Eviter les phrases introductives et demarrer sur la vraie liste ingredients.

**Independent Test**: Un texte avec phrase introductive + ligne `ingredients:` doit etre ancre sur la ligne de liste.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T009 [P] [US1] Ecrire le test d acceptance intro vs vraie liste dans `app/src/test/java/com/miamia/recognition/IngredientAnchorDetectionTest.kt`
- [X] T010 [P] [US1] Ecrire le test de contrat `IngredientAnchorDetectionOutput` dans `app/src/test/java/com/miamia/recognition/IngredientAnchorContractTest.kt`

### Implementation for User Story 1

- [X] T011 [US1] Mettre a jour la segmentation pour ignorer phrase intro dans `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt`
- [X] T012 [US1] Connecter la nouvelle ancre au flux d analyse dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T013 [US1] Verifier la compatibilite avec bilan 009 dans `app/src/main/java/com/miamia/composition/CompositionAnalysisEngine.kt`

**Checkpoint**: US1 fonctionnelle et testable independamment.

---

## Phase 4: User Story 2 - Gerer variantes de format (Priority: P2)

**Goal**: Supporter `ingredients:` et `ingredients :` avec variations de casse.

**Independent Test**: Les formes `Ingredients :`, `INGREDIENTS:`, `ingredients :` doivent etre detectees comme ancres valides.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T014 [P] [US2] Ecrire les tests de variantes casse/espaces dans `app/src/test/java/com/miamia/recognition/IngredientAnchorDetectionTest.kt`
- [X] T015 [P] [US2] Ecrire le test de priorite ancre canonique vs mot simple dans `app/src/test/java/com/miamia/recognition/IngredientAnchorContractTest.kt`

### Implementation for User Story 2

- [X] T016 [US2] Ajouter la prise en charge des espaces avant deux-points dans `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt`
- [X] T017 [US2] Ajouter la normalisation robuste autour du separateur dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationService.kt`
- [X] T018 [US2] Ajuster le message utilisateur si ancre non canonique detectee dans `app/src/main/java/com/miamia/ingredients/ScanFailureMessageBuilder.kt`

**Checkpoint**: US2 testable independamment.

---

## Phase 5: User Story 3 - Bloquer analyse si liste introuvable (Priority: P3)

**Goal**: Empêcher les analyses trompeuses sans ancre canonique.

**Independent Test**: Sans `ingredients:`/`ingredients :`, l analyse est bloquee avec action de recapture/edition.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T019 [P] [US3] Ecrire le test d acceptance de blocage sans ancre canonique dans `app/src/test/java/com/miamia/recognition/IngredientAnchorDetectionTest.kt`
- [X] T020 [P] [US3] Ecrire le test instrumente de message de blocage dans `app/src/androidTest/java/com/miamia/camera/US3CameraErrorContractTest.kt`

### Implementation for User Story 3

- [X] T021 [US3] Ajouter la decision `NO_CANONICAL_ANCHOR` au contrat reconnaissance dans `app/src/main/java/com/miamia/recognition/RecognitionContracts.kt`
- [X] T022 [US3] Appliquer le blocage explicite et actions de recuperation dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T023 [US3] Afficher un etat d erreur coherent dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: US3 testable independamment.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finaliser documentation, qualite globale et validation bout-en-bout.

- [X] T024 [P] Mettre a jour les cas de verification dans `specs/014-capture-liste-ingredients/quickstart.md`
- [X] T025 Verifier la coherence contrat/spec/implementation dans `specs/014-capture-liste-ingredients/contracts/ingredients-anchor-selection-contract.md`
- [ ] T026 Executer la validation complete des parcours quickstart dans `specs/014-capture-liste-ingredients/quickstart.md`
- [X] T027 [P] Ajouter un test de non-regression performance sur extraction d ancre dans `app/src/test/java/com/miamia/recognition/IngredientAnchorPerformanceTest.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: demarre immediatement
- **Phase 2 (Foundational)**: depend de Phase 1 et bloque les stories
- **Phases 3-5 (US1-US3)**: dependent de Phase 2; execution possible en parallele selon capacite
- **Phase 6 (Polish)**: depend des stories ciblees

### User Story Dependencies

- **US1 (P1)**: debut apres fondations, livrable MVP
- **US2 (P2)**: debut apres fondations, etend robustesse format
- **US3 (P3)**: debut apres fondations, renforce fiabilite en echec

### Within Each User Story

- Tests d acceptance/contrat d abord (ATDD)
- Implementation metier ensuite
- Integration UI/flux ensuite
- Validation independante de story avant progression

### Parallel Opportunities

- Phase 1: `T002`, `T003`
- Phase 2: `T005`, `T006`
- US1: `T009`, `T010`
- US2: `T014`, `T015`
- US3: `T019`, `T020`
- Phase 6: `T024`, `T027`

---

## Parallel Example: User Story 2

```bash
Task: "T014 [US2] Tests variantes casse/espaces dans app/src/test/java/com/miamia/recognition/IngredientAnchorDetectionTest.kt"
Task: "T015 [US2] Test priorite ancre canonique dans app/src/test/java/com/miamia/recognition/IngredientAnchorContractTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completer Setup + Foundational
2. Completer US1
3. Valider US1 independamment
4. Demonstration MVP

### Incremental Delivery

1. Ajouter US2 (robustesse format)
2. Ajouter US3 (blocage explicite sans ancre)
3. Finaliser polish (quickstart + non-regressions)

### Parallel Team Strategy

1. Equipe complete Setup + Foundational
2. Repartition:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Convergence sur phase Polish
