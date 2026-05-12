# Tasks: Aperçu caméra réel et capture par bouton

**Input**: Design documents from `/specs/007-live-camera-preview-capture/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Dans ce projet, les tests d’acceptation/parcours (ATDD) sont obligatoires.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Chaque tâche contient un chemin de fichier explicite

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialisation et alignement du module caméra sur la feature

- [X] T001 Vérifier et ajuster dépendances CameraX/Compose/coroutines dans `app/build.gradle.kts`
- [X] T002 Créer le squelette du composant preview dédié dans `app/src/main/java/com/miamia/camera/CameraPreviewBox.kt`
- [X] T003 [P] Créer le contrôleur de capture dédié dans `app/src/main/java/com/miamia/camera/CameraCaptureController.kt`
- [X] T004 [P] Ajouter la configuration de performance preview/capture dans `app/src/main/java/com/miamia/core/FeatureConfig.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fondations de flux caméra avant toute user story

- [X] T005 Étendre la machine d’états caméra pour inclure preview active/erreurs dédiées dans `app/src/main/java/com/miamia/camera/ScanState.kt`
- [X] T006 [P] Implémenter le modèle de session preview (`LivePreviewSession`) dans `app/src/main/java/com/miamia/camera/LivePreviewSession.kt`
- [X] T007 [P] Implémenter le modèle d’action capture (`CaptureAction`) dans `app/src/main/java/com/miamia/camera/CaptureAction.kt`
- [X] T008 [P] Implémenter le modèle frame capturée (`CapturedFrame`) dans `app/src/main/java/com/miamia/camera/CapturedFrame.kt`
- [X] T009 Implémenter le contrat applicatif preview/capture dans `app/src/main/java/com/miamia/camera/CameraPreviewCaptureContract.kt`
- [X] T010 Implémenter la gestion permission et indisponibilité caméra dans `app/src/main/java/com/miamia/permissions/CameraPermissionHandler.kt`
- [X] T011 Implémenter la stratégie anti double-clic capture dans `app/src/main/java/com/miamia/camera/CameraCaptureController.kt`
- [X] T012 Implémenter la conservation temporaire image capturée dans `app/src/main/java/com/miamia/scan/TemporaryImageManager.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Voir l'objectif en direct avant de capturer (Priority: P1) 🎯 MVP

**Goal**: Afficher un vrai flux caméra en direct dans un encart dédié.

**Independent Test**: Ouvrir l’écran avec permission accordée et vérifier un flux visuel continu réel.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T013 [P] [US1] Écrire test instrumenté ouverture écran avec preview active dans `app/src/androidTest/java/com/miamia/camera/US1LivePreviewVisibleTest.kt`
- [X] T014 [P] [US1] Écrire test instrumenté variation visuelle de preview au mouvement dans `app/src/androidTest/java/com/miamia/camera/US1PreviewReflectsMovementTest.kt`
- [X] T015 [P] [US1] Écrire test unitaire interdisant placeholder visuel en mode preview actif dans `app/src/test/java/com/miamia/camera/US1NoPlaceholderWhenPreviewActiveTest.kt`

### Implementation for User Story 1

- [X] T016 [P] [US1] Implémenter le rendu `PreviewView` dans l’encart dédié dans `app/src/main/java/com/miamia/camera/CameraPreviewBox.kt`
- [X] T017 [US1] Intégrer `CameraPreviewBox` dans l’écran caméra principal dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T018 [US1] Lier lifecycle caméra et transition vers état preview actif dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T019 [US1] Ajouter message explicite de statut preview (initialisation/actif) dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: User Story 1 pleinement testable et démontrable

---

## Phase 4: User Story 2 - Déclencher la capture uniquement par action explicite (Priority: P1)

**Goal**: Capturer une image uniquement via bouton explicite après cadrage.

**Independent Test**: Depuis un preview actif, cliquer capture une fois et vérifier une unique image issue du flux.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T020 [P] [US2] Écrire test instrumenté capture sur clic bouton dans `app/src/androidTest/java/com/miamia/camera/US2CaptureOnButtonClickTest.kt`
- [X] T021 [P] [US2] Écrire test instrumenté absence de capture sans clic dans `app/src/androidTest/java/com/miamia/camera/US2NoCaptureWithoutClickTest.kt`
- [X] T022 [P] [US2] Écrire test unitaire anti double-clic (une capture à la fois) dans `app/src/test/java/com/miamia/camera/US2SingleCapturePerActionTest.kt`
- [X] T023 [P] [US2] Écrire test de contrat `CaptureFrameCommand`/`CaptureFrameResult` dans `app/src/test/java/com/miamia/camera/US2CaptureContractTest.kt`

### Implementation for User Story 2

- [X] T024 [P] [US2] Implémenter le bouton capture explicite et son état d’activation dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T025 [US2] Implémenter le déclenchement `takePicture()` dans `app/src/main/java/com/miamia/camera/CameraCaptureController.kt`
- [X] T026 [US2] Mapper action utilisateur -> `CaptureAction` et résultat `CapturedFrame` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T027 [US2] Implémenter feedback visuel immédiat post-capture dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: User Stories 1 et 2 fonctionnent indépendamment

---

## Phase 5: User Story 3 - Gérer l'absence de permission ou l'indisponibilité de la caméra (Priority: P2)

**Goal**: Afficher des erreurs explicites et proposer un réessai sans faux aperçu.

**Independent Test**: Refuser permission ou simuler caméra indisponible, puis vérifier message clair et action de reprise.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T028 [P] [US3] Écrire test instrumenté permission refusée et message explicite dans `app/src/androidTest/java/com/miamia/camera/US3PermissionDeniedStateTest.kt`
- [X] T029 [P] [US3] Écrire test instrumenté caméra indisponible sans faux preview dans `app/src/androidTest/java/com/miamia/camera/US3CameraUnavailableStateTest.kt`
- [X] T030 [P] [US3] Écrire test unitaire de contrat `CameraErrorState` dans `app/src/test/java/com/miamia/camera/US3CameraErrorContractTest.kt`

### Implementation for User Story 3

- [X] T031 [P] [US3] Implémenter l’état erreur permission avec action réessai dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T032 [US3] Implémenter l’état erreur caméra indisponible dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T033 [US3] Implémenter affichage UI des erreurs explicites sans contenu factice dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T034 [US3] Implémenter redemande permission/relance preview dans `app/src/main/java/com/miamia/permissions/CameraPermissionHandler.kt`

**Checkpoint**: Toutes les user stories sont testables indépendamment

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finitions, performance, documentation et robustesse

- [X] T035 [P] Mesurer et journaliser temps d’apparition preview (<3s) dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T036 [P] Mesurer feedback capture perçu (<300ms) dans `app/src/main/java/com/miamia/camera/CameraCaptureController.kt`
- [X] T037 [P] Ajouter test instrumenté responsive (petit/grand écran) dans `app/src/androidTest/java/com/miamia/camera/ResponsivePreviewLayoutTest.kt`
- [X] T038 [P] Ajouter test instrumenté reprise après rotation/background dans `app/src/androidTest/java/com/miamia/camera/PreviewLifecycleResumeTest.kt`
- [X] T039 Mettre à jour la documentation d’exécution et tests dans `specs/007-live-camera-preview-capture/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: démarre immédiatement
- **Phase 2 (Foundational)**: dépend de la phase 1 et bloque les stories
- **Phase 3 (US1)**: dépend de la phase 2
- **Phase 4 (US2)**: dépend de la phase 2 (et s’appuie sur US1 pour le flux visuel)
- **Phase 5 (US3)**: dépend de la phase 2
- **Phase 6 (Polish)**: dépend des stories complétées

### User Story Dependencies

- **US1 (P1)**: indépendante après fondations
- **US2 (P1)**: indépendante après fondations, mais validée avec preview opérationnel
- **US3 (P2)**: indépendante après fondations

### Within Each User Story

- Tests ATDD d’abord (tests échouent avant implémentation)
- Contrats et modèles d’état avant orchestration UI
- Orchestration avant finalisation écran
- Validation indépendante de story avant progression

### Parallel Opportunities

- Setup: `T003`, `T004`
- Foundational: `T006`, `T007`, `T008`
- US1: `T013`, `T014`, `T015`, `T016`
- US2: `T020`, `T021`, `T022`, `T023`, `T024`
- US3: `T028`, `T029`, `T030`, `T031`
- Polish: `T035`, `T036`, `T037`, `T038`

---

## Parallel Example: User Story 2

```bash
Task: "Écrire test instrumenté capture sur clic bouton dans app/src/androidTest/java/com/miamia/camera/US2CaptureOnButtonClickTest.kt"
Task: "Écrire test instrumenté absence de capture sans clic dans app/src/androidTest/java/com/miamia/camera/US2NoCaptureWithoutClickTest.kt"
Task: "Écrire test unitaire anti double-clic dans app/src/test/java/com/miamia/camera/US2SingleCapturePerActionTest.kt"
Task: "Implémenter le bouton capture explicite et son état d’activation dans app/src/main/java/com/miamia/camera/CameraScreen.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Terminer Setup + Foundational
2. Livrer US1 (preview réel) avec ses tests ATDD
3. Valider indépendamment avant extension capture

### Incremental Delivery

1. Setup + fondations
2. Ajouter US1 puis valider
3. Ajouter US2 puis valider
4. Ajouter US3 puis valider
5. Finaliser polish/performance

### Parallel Team Strategy

1. Équipe sur Setup/Fondations
2. Ensuite distribution:
   - Dev A: US1 (preview réel)
   - Dev B: US2 (capture explicite)
   - Dev C: US3 (gestion erreurs permissions/indispo)
3. Intégration finale + validation SC-001 à SC-004
