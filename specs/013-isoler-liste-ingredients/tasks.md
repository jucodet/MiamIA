# Tasks: Isolation de la liste ingredients avant analyse

**Input**: Design documents from `/specs/013-isoler-liste-ingredients/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Les tests d acceptance/parcours (ATDD) sont obligatoires et precendent l implementation.

**Organization**: Taches groupees par user story pour garantir une livraison independante et testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Peut etre executee en parallele (fichiers differents, pas de dependance directe)
- **[Story]**: User story ciblee (`[US1]`, `[US2]`, `[US3]`)
- Chaque tache inclut un chemin de fichier explicite

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparer le squelette technique de la feature et la structure de tests.

- [X] T001 Creer la structure de tests de la feature dans `app/src/test/java/com/miamia/analysis/ingredientsegment/`
- [X] T002 [P] Creer la structure de tests instrumentes UI dans `app/src/androidTest/java/com/miamia/camera/ingredientsegment/`
- [X] T003 [P] Ajouter les fixtures OCR de reference dans `app/src/test/java/com/miamia/analysis/ingredientsegment/fixtures/OcrFixtures.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Mettre en place les composants transverses requis par toutes les user stories.

**⚠️ CRITICAL**: Aucune user story ne commence avant completion de cette phase.

- [X] T004 Definir les modeles de domaine `OcrRawText`, `IngredientSegmentExtraction`, `AnalysisSubmissionDecision` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentModels.kt`
- [X] T005 [P] Implementer la normalisation de l ancre ingredients (casse/accents) dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientAnchorNormalizer.kt`
- [X] T006 [P] Implementer le parseur de bornes (premiere occurrence, newline ou EOF) dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolver.kt`
- [X] T007 Implementer le service de preparation conforme au contrat dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationService.kt`
- [X] T008 Implementer la passerelle de soumission (`submissionAllowed`/`blockedReason`) dans `app/src/main/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGate.kt`

**Checkpoint**: Fondations pretes - les user stories peuvent commencer.

---

## Phase 3: User Story 1 - Isoler automatiquement la ligne ingredients (Priority: P1) 🎯 MVP

**Goal**: Extraire uniquement le segment ingredients pertinent avant analyse.

**Independent Test**: Avec un OCR multi-lignes contenant bruit + ingredients, verifier que seul le segment ancre est retenu pour l analyse.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T009 [P] [US1] Ecrire le test d acceptance Given/When/Then extraction nominale dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentExtractionAcceptanceTest.kt`
- [X] T010 [P] [US1] Ecrire le test de contrat `IngredientSegmentPreparationOutput` dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationContractTest.kt`

### Implementation for User Story 1

- [X] T011 [US1] Integrer `IngredientSegmentPreparationService` dans le flux OCR->analyse du viewmodel dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T012 [US1] Exclure le texte avant ancre et apres borne dans la construction de payload d analyse dans `app/src/main/java/com/miamia/analysis/AnalysisInputBuilder.kt`
- [X] T013 [US1] Ajouter la couverture des occurrences multiples (premiere occurrence) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationServiceTest.kt`

**Checkpoint**: US1 livrable et testable independamment.

---

## Phase 4: User Story 2 - Comportement previsible en cas de texte incomplet (Priority: P2)

**Goal**: Bloquer proprement l analyse quand l ancre est absente ou le segment est inexploitable.

**Independent Test**: Fournir un OCR sans `ingredients` et verifier blocage + proposition de recapture/edition.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T014 [P] [US2] Ecrire le test d acceptance blocage absence ancre dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentFallbackAcceptanceTest.kt`
- [X] T015 [P] [US2] Ecrire le test de contrat `AnalysisSubmissionGateOutput` (blocked reasons) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGateContractTest.kt`

### Implementation for User Story 2

- [X] T016 [US2] Implementer la strategie `ANCHOR_MISSING_BLOCKED` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGate.kt`
- [X] T017 [US2] Ajouter le traitement UI de blocage (recapture/edition) dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T018 [US2] Ajouter le cas sans retour a la ligne (borne EOF) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`

**Checkpoint**: US2 livrable et testable independamment.

---

## Phase 5: User Story 3 - Rendre l extraction verifiable par l utilisateur (Priority: P3)

**Goal**: Afficher le segment extrait avant analyse et exiger une confirmation explicite.

**Independent Test**: Apres extraction, verifier que le segment est visible et que l envoi reste bloque sans confirmation.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T019 [P] [US3] Ecrire le test UI compose de previsualisation + confirmation dans `app/src/androidTest/java/com/miamia/camera/ingredientsegment/IngredientSegmentConfirmationUiTest.kt`
- [X] T020 [P] [US3] Ecrire le test d acceptance envoi conditionne a `userConfirmed` dans `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionDecisionAcceptanceTest.kt`

### Implementation for User Story 3

- [X] T021 [US3] Ajouter l etat UI de previsualisation/confirmation dans `app/src/main/java/com/miamia/camera/CameraUiState.kt`
- [X] T022 [US3] Ajouter la section de confirmation segment dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T023 [US3] Connecter la confirmation utilisateur a la passerelle d envoi dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`

**Checkpoint**: US3 livrable et testable independamment.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finaliser qualite, performance, documentation, et verification de bout en bout.

- [X] T024 [P] Documenter les regles de preparation dans `specs/013-isoler-liste-ingredients/quickstart.md`
- [X] T025 Ajouter les logs metier minimaux (fallback, confirmation, blocage) dans `app/src/main/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGate.kt`
- [X] T026 Mesurer et verifier l objectif p95 (<200 ms) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPerformanceTest.kt`
- [ ] T027 Executer la validation complete des parcours quickstart dans `specs/013-isoler-liste-ingredients/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: demarre immediatement
- **Phase 2 (Foundational)**: depend de Phase 1 et bloque toutes les user stories
- **Phases 3-5 (US1-US3)**: dependent de Phase 2; execution possible en parallele selon capacite equipe
- **Phase 6 (Polish)**: depend de la completion des stories cibles

### User Story Dependencies

- **US1 (P1)**: commence apres fondations, sans dependance a US2/US3
- **US2 (P2)**: commence apres fondations, s appuie sur la passerelle de soumission
- **US3 (P3)**: commence apres fondations, depend du segment extrait disponible

### Within Each User Story

- Tests ATDD d abord (doivent echouer avant implementation)
- Implementation metier ensuite
- Integration UI/flux en dernier
- Validation independante de la story avant passage a la suivante

### Parallel Opportunities

- Phase 1: `T002`, `T003` paralleles
- Phase 2: `T005`, `T006` paralleles
- US1: `T009`, `T010` paralleles
- US2: `T014`, `T015` paralleles
- US3: `T019`, `T020` paralleles

---

## Parallel Example: User Story 1

```bash
Task: "T009 [US1] Acceptance extraction nominale dans app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentExtractionAcceptanceTest.kt"
Task: "T010 [US1] Contract preparation output dans app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationContractTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completer Setup + Foundational
2. Completer US1
3. Valider US1 independamment
4. Demo MVP

### Incremental Delivery

1. Ajouter US2 apres MVP pour robustesse des cas incomplets
2. Ajouter US3 pour transparence et confirmation utilisateur
3. Finaliser avec phase Polish (perf + docs + validation quickstart)

### Parallel Team Strategy

1. Equipe complete Setup + Foundational
2. Puis repartition:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Integration et validation finale commune en Phase 6
