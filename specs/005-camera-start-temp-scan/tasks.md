# Tasks: Démarrage caméra et scan temporaire

**Input**: Design documents from `/specs/005-camera-start-temp-scan/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Les tests d’acceptation/parcours (ATDD) sont obligatoires et écrits avant implémentation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialisation Android/Kotlin et outillage minimal pour un flux local-only.

- [X] T001 Créer la structure du module Android dans `app/src/main/java/com/miamia/`
- [X] T002 Configurer les dépendances minimales (CameraX, Room, Coroutines, AI Edge runtime) dans `app/build.gradle.kts`
- [X] T003 [P] Configurer permissions et manifest caméra dans `app/src/main/AndroidManifest.xml`
- [X] T004 [P] Ajouter la configuration de base des tests unitaires et instrumentés dans `app/build.gradle.kts`
- [X] T005 [P] Créer le squelette de packages `camera`, `scan`, `data`, `permissions` dans `app/src/main/java/com/miamia/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fondations communes bloquantes pour toutes les user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 Implémenter le modèle `ScanSessionEntity` Room dans `app/src/main/java/com/miamia/data/db/ScanSessionEntity.kt`
- [X] T007 [P] Implémenter `AppDatabase` + DAO de session dans `app/src/main/java/com/miamia/data/db/AppDatabase.kt`
- [X] T008 Implémenter `ScanSessionRepository` pour persistance métadonnées dans `app/src/main/java/com/miamia/data/repository/ScanSessionRepository.kt`
- [X] T009 [P] Implémenter `TemporaryImageManager` (cache privé + suppression idempotente) dans `app/src/main/java/com/miamia/scan/TemporaryImageManager.kt`
- [X] T010 [P] Implémenter le contrat d’analyse locale `LocalOcrAnalyzer` basé sur AI Edge dans `app/src/main/java/com/miamia/scan/LocalOcrAnalyzer.kt`
- [X] T011 Implémenter `ScanSessionCoordinator` (orchestration capture/analyse/persistance/cleanup) dans `app/src/main/java/com/miamia/scan/ScanSessionCoordinator.kt`
- [X] T012 [P] Définir la machine d’états `ScanState` dans `app/src/main/java/com/miamia/camera/ScanState.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Arriver directement en mode caméra au lancement (Priority: P1) 🎯 MVP

**Goal**: Afficher immédiatement la caméra avec bouton Scan au démarrage, y compris gestion permission.

**Independent Test**: Relancer l’app et vérifier écran caméra initial + bouton scan + comportement permission.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T013 [P] [US1] Ajouter test UI lancement caméra initiale dans `app/src/androidTest/camera/LaunchCameraScreenTest.kt`
- [X] T014 [P] [US1] Ajouter test UI parcours permission refusée/autorisée dans `app/src/androidTest/camera/CameraPermissionFlowTest.kt`

### Implementation for User Story 1

- [X] T015 [US1] Implémenter `CameraPermissionHandler` dans `app/src/main/java/com/miamia/permissions/CameraPermissionHandler.kt`
- [X] T016 [US1] Implémenter `CameraViewModel` état initial `camera_ready` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T017 [US1] Implémenter l’écran `CameraScreen` (preview + bouton Scan) dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T018 [US1] Connecter route d’entrée app vers `CameraScreen` dans `app/src/main/java/com/miamia/MainActivity.kt`

**Checkpoint**: US1 est démontrable indépendamment (entrée caméra + scan button + permissions)

---

## Phase 4: User Story 2 - Lancer l’analyse via une photo temporaire (Priority: P2)

**Goal**: Capturer une image temporaire, analyser localement, puis supprimer systématiquement le média.

**Independent Test**: Appui scan -> analyse locale démarre -> aucune persistance image durable en succès/erreur.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T019 [P] [US2] Ajouter test d’intégration du pipeline local scan->analyse dans `app/src/androidTest/scan/LocalScanPipelineTest.kt`
- [X] T020 [P] [US2] Ajouter test d’intégration suppression image temporaire succès/erreur dans `app/src/androidTest/scan/TempImageCleanupTest.kt`
- [X] T021 [P] [US2] Ajouter test unitaire repository métadonnées sans image brute dans `app/src/test/java/com/miamia/data/ScanSessionRepositoryTest.kt`

### Implementation for User Story 2

- [X] T022 [US2] Implémenter capture photo temporaire CameraX dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T023 [US2] Implémenter appel `StartScanRequest` vers `LocalOcrAnalyzer` dans `app/src/main/java/com/miamia/scan/ScanSessionCoordinator.kt`
- [X] T024 [US2] Persister `ScanSession` (statuts, durée, fingerprint) dans `app/src/main/java/com/miamia/data/repository/ScanSessionRepository.kt`
- [X] T025 [US2] Implémenter suppression en `finally` de `TemporaryScanImage` dans `app/src/main/java/com/miamia/scan/TemporaryImageManager.kt`
- [X] T026 [US2] Ajouter garde anti-concurrence sur clic multiple Scan dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`

**Checkpoint**: US2 est testable seule (analyse locale + non-persistance photo)

---

## Phase 5: User Story 3 - Comprendre l’état du scan pendant l’analyse (Priority: P3)

**Goal**: Rendre les états utilisateur explicites sur tout le cycle de scan.

**Independent Test**: Vérifier transitions `camera_ready -> capturing -> analyzing -> success/error` et action retry.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T027 [P] [US3] Ajouter test UI transitions d’états pendant scan dans `app/src/androidTest/scan/ScanFeedbackStateTest.kt`
- [X] T028 [P] [US3] Ajouter test UI affichage message d’erreur + retry dans `app/src/androidTest/scan/ScanErrorRetryTest.kt`

### Implementation for User Story 3

- [X] T029 [US3] Implémenter mapping des états orchestrateur -> `ScanState` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T030 [US3] Implémenter composants UI d’état (loader/succès/erreur) dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T031 [US3] Implémenter action `Retry` et retour `camera_ready` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`

**Checkpoint**: US3 ajoute une UX complète et lisible sans casser US1/US2

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Stabilisation finale, performance, documentation.

- [X] T032 [P] Ajouter métriques locales de perf startup/scan dans `app/src/main/java/com/miamia/scan/ScanSessionCoordinator.kt`
- [X] T033 Vérifier conformité contrat `contracts/local-scan-analysis-contract.md` et ajuster implémentation correspondante
- [X] T034 [P] Mettre à jour documentation d’exécution dans `specs/005-camera-start-temp-scan/quickstart.md`
- [ ] T035 Exécuter la validation complète quickstart sur appareil et consigner résultats dans `specs/005-camera-start-temp-scan/research.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: démarre immédiatement
- **Foundational (Phase 2)**: dépend de Setup et bloque les user stories
- **User Stories (Phase 3-5)**: démarrent après Phase 2, par priorité P1 -> P2 -> P3
- **Polish (Phase 6)**: après user stories terminées

### User Story Dependencies

- **US1 (P1)**: indépendante après fondations
- **US2 (P2)**: dépend de US1 pour UI caméra opérationnelle
- **US3 (P3)**: dépend de US2 pour refléter les états du pipeline réel

### Parallel Opportunities

- Setup: `T003`, `T004`, `T005`
- Foundational: `T007`, `T009`, `T010`, `T012`
- US1 tests: `T013`, `T014`
- US2 tests: `T019`, `T020`, `T021`
- US3 tests: `T027`, `T028`
- Polish: `T032`, `T034`

---

## Parallel Example: User Story 2

```bash
Task: "T019 [US2] LocalScanPipelineTest dans app/src/androidTest/scan/LocalScanPipelineTest.kt"
Task: "T020 [US2] TempImageCleanupTest dans app/src/androidTest/scan/TempImageCleanupTest.kt"
Task: "T021 [US2] ScanSessionRepositoryTest dans app/src/test/java/com/miamia/data/ScanSessionRepositoryTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Terminer Phases 1 et 2
2. Implémenter US1
3. Valider démarrage caméra + permission
4. Démo MVP

### Incremental Delivery

1. Ajouter US2 (pipeline local + cleanup image)
2. Ajouter US3 (feedback états)
3. Finaliser polish/perf/quickstart

### Format Validation

Tous les items suivent le format requis: `- [ ] Txxx [P?] [US?] Description avec chemin de fichier`.

