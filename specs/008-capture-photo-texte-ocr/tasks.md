# Tasks: Capture photo et affichage du texte reconnu

**Input**: Design documents from `specs/008-capture-photo-texte-ocr/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/photo-text-capture-contract.md`, `quickstart.md`

**Tests**: Les tests d’acceptation/parcours (ATDD) sont obligatoires pour chaque user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Préparer l’environnement de test et la base de mesure de performance du flux capture->texte.

- [X] T001 Configurer le package de tests instrumentés de la feature dans `app/src/androidTest/java/com/miamia/camera/`
- [X] T002 [P] Créer un jeu d’assets de test (image lisible, image sans texte, image floue) dans `app/src/androidTest/assets/ocr/`
- [X] T003 [P] Ajouter un helper de mesure de latence (capture->résultat) dans `app/src/androidTest/java/com/miamia/recognition/RecognitionTimingHelper.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Poser les fondations techniques communes qui bloquent toutes les user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Ajouter/adapter le contrat d’état du flux `idle/capturing/processing/success/empty/error` dans `app/src/main/java/com/miamia/camera/ScanState.kt`
- [X] T005 [P] Introduire le modèle résultat unifié (`success|empty|error`, message, text) dans `app/src/main/java/com/miamia/recognition/RecognitionContracts.kt`
- [X] T006 [P] Centraliser la règle “OCR on-device only / aucun envoi réseau” dans `app/src/main/java/com/miamia/recognition/RecognitionEngineSelector.kt`
- [X] T007 Implémenter la politique de cycle de vie des images temporaires (création, nettoyage après résultat) dans `app/src/main/java/com/miamia/scan/TemporaryImageManager.kt`
- [X] T008 Connecter le flux orchestré capture->analyse->résultat dans `app/src/main/java/com/miamia/scan/ScanSessionCoordinator.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Capturer puis voir le texte de l’image (Priority: P1) 🎯 MVP

**Goal**: Déclencher une capture réelle au clic “Prendre la photo”, analyser localement et afficher le texte reconnu avec état de progression.

**Independent Test**: Depuis l’écran de capture, un clic unique lance une capture réelle puis affiche un texte extrait lisible en moins de 10s sur image lisible.

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation (ATDD)**

- [X] T009 [P] [US1] Écrire le test d’acceptation “tap capture -> processing -> success avec displayText non vide” dans `app/src/androidTest/java/com/miamia/camera/CameraCaptureToTextSuccessTest.kt`
- [X] T010 [P] [US1] Écrire le test de contrat “TakePhotoTapped -> state success conforme au contrat” dans `app/src/androidTest/java/com/miamia/recognition/PhotoTextContractSuccessTest.kt`
- [X] T011 [P] [US1] Écrire le test de performance “résultat affiché < 10s” dans `app/src/androidTest/java/com/miamia/recognition/PhotoTextLatencyTest.kt`

### Implementation for User Story 1

- [X] T012 [P] [US1] Implémenter le déclenchement explicite de capture sur action utilisateur dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T013 [P] [US1] Mettre à jour le ViewModel pour enchaîner `capturing -> processing -> success` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T014 [US1] Brancher la capture réelle CameraX au contrôleur de session dans `app/src/main/java/com/miamia/camera/CameraCaptureController.kt`
- [X] T015 [US1] Implémenter l’extraction locale du texte et la production de `displayText` dans `app/src/main/java/com/miamia/recognition/LocalOcrFallbackRecognizer.kt`
- [X] T016 [US1] Mapper le résultat OCR vers le format d’affichage utilisateur dans `app/src/main/java/com/miamia/ingredients/ExtractedIngredientMapper.kt`
- [X] T017 [US1] Afficher l’état “traitement en cours” et le résultat texte dans `app/src/main/java/com/miamia/ingredients/IngredientEditorScreen.kt`

**Checkpoint**: User Story 1 fully functional and testable independently

---

## Phase 4: User Story 2 - Absence de texte ou échec de reconnaissance (Priority: P2)

**Goal**: En cas d’absence de texte ou d’erreur locale, afficher un message clair et proposer une relance simple, sans contenu factice.

**Independent Test**: Sur image sans texte ou floue, l’écran présente respectivement `empty` ou `error` avec message explicite et action de retry.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T018 [P] [US2] Écrire le test d’acceptation “image sans texte -> état empty + message explicite” dans `app/src/androidTest/java/com/miamia/recognition/PhotoTextEmptyStateTest.kt`
- [X] T019 [P] [US2] Écrire le test d’acceptation “erreur moteur local -> état error + retry disponible” dans `app/src/androidTest/java/com/miamia/recognition/PhotoTextErrorStateTest.kt`
- [X] T020 [P] [US2] Écrire le test de contrat “aucun texte factice en empty/error” dans `app/src/androidTest/java/com/miamia/recognition/PhotoTextNoFakeContentContractTest.kt`

### Implementation for User Story 2

- [X] T021 [P] [US2] Implémenter la classification des échecs OCR locaux (`empty` vs `error`) dans `app/src/main/java/com/miamia/recognition/ScanFailureClassifier.kt`
- [X] T022 [P] [US2] Générer des messages utilisateur cohérents pour `empty` et `error` dans `app/src/main/java/com/miamia/ingredients/ScanFailureMessageBuilder.kt`
- [X] T023 [US2] Implémenter l’action de relance depuis l’écran résultat dans `app/src/main/java/com/miamia/ingredients/RetryScanActionHandler.kt`
- [X] T024 [US2] Faire refléter les états `empty/error` et l’action retry dans le ViewModel dans `app/src/main/java/com/miamia/ingredients/IngredientEditorViewModel.kt`
- [X] T025 [US2] Supprimer automatiquement l’image temporaire après résultat `success|empty|error` dans `app/src/main/java/com/miamia/scan/TemporaryImageManager.kt`

**Checkpoint**: User Stories 1 and 2 both work independently

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Finitions transverses, robustesse et validation du quickstart.

- [X] T026 [P] Vérifier l’absence d’appel réseau pendant OCR via instrumentation dans `app/src/androidTest/java/com/miamia/recognition/OfflineOnlyRecognitionTest.kt`
- [X] T027 [P] Ajuster microcopies et états UI (loading/empty/error) dans `app/src/main/java/com/miamia/ingredients/IngredientEditorScreen.kt`
- [X] T028 Exécuter la validation complète `quickstart.md` et documenter le résultat dans `specs/008-capture-photo-texte-ocr/quickstart.md`
- [X] T029 Mettre à jour la documentation technique de la feature dans `specs/008-capture-photo-texte-ocr/plan.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Phase 1; blocks all user stories
- **US1 (Phase 3)**: Depends on Phase 2; MVP
- **US2 (Phase 4)**: Depends on Phase 2 and réutilise le flux US1
- **Polish (Phase 5)**: Depends on US1 + US2

### User Story Dependencies

- **US1 (P1)**: Démarrable dès la fin des fondations, livrable seul (MVP)
- **US2 (P2)**: Dépend de l’orchestration existante mais reste testable indépendamment avec cas `empty/error`

### Within Each User Story

- Tests d’acceptation/contrat d’abord (FAIL), puis implémentation, puis validation rapide

### Parallel Opportunities

- Phase 1: T002/T003 parallélisables
- Phase 2: T005/T006/T007 parallélisables
- US1: T009/T010/T011 parallélisables; T012/T013 parallélisables
- US2: T018/T019/T020 parallélisables; T021/T022 parallélisables

---

## Parallel Example: User Story 1

```bash
Task: "T009 [US1] test d'acceptation success dans app/src/androidTest/java/com/miamia/camera/CameraCaptureToTextSuccessTest.kt"
Task: "T010 [US1] test de contrat success dans app/src/androidTest/java/com/miamia/recognition/PhotoTextContractSuccessTest.kt"
Task: "T011 [US1] test de latence dans app/src/androidTest/java/com/miamia/recognition/PhotoTextLatencyTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Compléter Phase 1 + Phase 2
2. Livrer US1 (Phase 3)
3. Valider indépendamment la promesse “tap -> texte affiché < 10s”

### Incremental Delivery

1. Ajouter US2 après stabilisation US1
2. Vérifier qu’US2 n’introduit aucune régression sur US1
3. Terminer par polish et validation quickstart

### Parallel Team Strategy

1. Dev A: orchestration capture/state
2. Dev B: reconnaissance locale/contrats
3. Dev C: UI messages/retry + tests instrumentés
