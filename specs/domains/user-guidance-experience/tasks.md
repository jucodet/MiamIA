# Tasks: llm-download-onboarding

**Input**: Design documents from `/specs/domains/user-guidance-experience/`
**Prerequisites**: plan.md, spec-llm-download-onboarding.md, research.md, data-model.md, contracts/

**Tests**: Les tests d'acceptation/parcours (ATDD) sont **OBLIGATOIRES** — au minimum un par user story.

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android app**: `app/src/main/java/com/foodgpt/`
- **Unit tests**: `app/src/test/java/com/foodgpt/`
- **Instrumented tests**: `app/src/androidTest/java/com/foodgpt/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Création des packages et extraction des composants partagés existants.

- [x] T001 Create onboarding package directory at app/src/main/java/com/foodgpt/onboarding/
- [x] T002 [P] Create shared UI package directory at app/src/main/java/com/foodgpt/ui/shared/
- [x] T003 [P] Extract `WAITING_PHRASES` list from app/src/main/java/com/foodgpt/result/LlmResultScreen.kt into app/src/main/java/com/foodgpt/ui/shared/WaitingPhrases.kt
- [x] T004 [P] Extract `AnimatedWhisk` composable from app/src/main/java/com/foodgpt/result/LlmResultScreen.kt into app/src/main/java/com/foodgpt/ui/shared/AnimatedWhisk.kt
- [x] T005 Update imports in app/src/main/java/com/foodgpt/result/LlmResultScreen.kt to use extracted shared components from com.foodgpt.ui.shared

**Checkpoint**: Packages créés ; `LlmResultScreen` fonctionne toujours avec les composants extraits.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modèles d'état, détection réseau, enrichissement du downloader — bloquent TOUTES les user stories.

**⚠️ CRITICAL**: Aucune user story ne peut démarrer avant cette phase.

- [x] T006 [P] Create `NetworkType` enum (WIFI, MOBILE_DATA, OFFLINE) in app/src/main/java/com/foodgpt/onboarding/NetworkTypeDetector.kt
- [x] T007 [P] Create `DownloadProgress` data class (percent, downloadedBytes, totalBytes) in app/src/main/java/com/foodgpt/onboarding/LlmModelReadinessState.kt
- [x] T008 Create `LlmModelReadinessState` sealed class (Checking, Offline, ConfirmationRequired, Downloading, Ready, Error, Declined) in app/src/main/java/com/foodgpt/onboarding/LlmModelReadinessState.kt
- [x] T009 Implement `NetworkTypeDetector.detectCurrentNetworkType(context)` using ConnectivityManager.getNetworkCapabilities() in app/src/main/java/com/foodgpt/onboarding/NetworkTypeDetector.kt
- [x] T010 Enrich `GemmaModelDownloader` with `downloadModelWithProgress(onProgress: (Int, Long, Long) -> Unit): File` method in app/src/main/java/com/foodgpt/gemma4local/GemmaModelDownloader.kt
- [x] T011 Create `ModelDownloadViewModel` skeleton (init checks model presence, exposes StateFlow<LlmModelReadinessState>) in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T012 [P] Create unit test for NetworkTypeDetector in app/src/test/java/com/foodgpt/onboarding/NetworkTypeDetectorTest.kt
- [x] T013 [P] Create unit test for ModelDownloadViewModel state transitions in app/src/test/java/com/foodgpt/onboarding/ModelDownloadViewModelTest.kt

**Checkpoint**: Fondation prête — les modèles d'état, le ViewModel, et le downloader enrichi sont fonctionnels et testés.

---

## Phase 3: User Story 1 — Confirmer le téléchargement du modèle (Priority: P1) 🎯 MVP

**Goal**: Au premier lancement sans modèle, afficher un écran plein de confirmation informant de la taille du téléchargement et du type de connexion détecté (Wi-Fi ou 4G). Permettre de décliner ou confirmer.

**Independent Test**: Simuler un premier lancement sans modèle local ; vérifier l'affichage de l'écran de confirmation, la détection du type de connexion, le blocage du téléchargement tant que la confirmation n'est pas donnée, et le comportement "Plus tard".

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T014 [P] [US1] Acceptance test: premier lancement affiche écran confirmation avec type réseau et taille modèle dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadOnboardingAcceptanceTest.kt
- [x] T015 [P] [US1] Acceptance test: "Plus tard" affiche état explicatif sans déclencher téléchargement dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadOnboardingAcceptanceTest.kt
- [x] T016 [P] [US1] Acceptance test: écran "Connexion requise" affiché quand offline sans modèle, avec bouton Réessayer dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadOnboardingAcceptanceTest.kt

### Implementation for User Story 1

- [x] T017 [US1] Implement `ModelDownloadOnboardingScreen` composable (titre, explication taille, info réseau détecté, boutons Confirmer/Plus tard) in app/src/main/java/com/foodgpt/onboarding/ModelDownloadOnboardingScreen.kt
- [x] T018 [P] [US1] Implement `NetworkOfflineScreen` composable (message "Connexion requise", explication, bouton Réessayer) in app/src/main/java/com/foodgpt/onboarding/NetworkOfflineScreen.kt
- [x] T019 [US1] Add ViewModel actions: `confirmDownload()`, `declineDownload()`, `retryNetworkCheck()` in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T020 [US1] Define onboarding navigation routes (onboarding_offline, onboarding_confirm, onboarding_downloading) and integrate as conditional startDestination in app/src/main/java/com/foodgpt/MainActivity.kt
- [x] T021 [US1] Wire navigation: ConfirmationRequired → ModelDownloadOnboardingScreen, Offline → NetworkOfflineScreen, Ready → CameraFlowRoutes.Capture (popUpTo inclusive) in app/src/main/java/com/foodgpt/MainActivity.kt

**Checkpoint**: US1 complète — un premier lancement sans modèle affiche la confirmation ou l'écran offline ; "Confirmer" déclenche la transition vers downloading ; "Plus tard" affiche l'état Declined.

---

## Phase 4: User Story 2 — Patienter avec feedback pendant le téléchargement (Priority: P1)

**Goal**: Après confirmation, afficher un écran d'attente engageant avec titre, barre de progression + pourcentage, phrases humoristiques rotatives toutes les 5s, et fouet animé. Redirection automatique vers l'écran capture à la fin.

**Independent Test**: Déclencher un téléchargement ; vérifier que l'écran d'attente affiche le titre permanent, la progression, la rotation des phrases, le fouet animé, et redirige à la fin.

### Tests for User Story 2 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T022 [P] [US2] Acceptance test: écran attente affiche titre "Téléchargement du modèle de langage en cours...", barre de progression, et fouet animé dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadWaitingAcceptanceTest.kt
- [x] T023 [P] [US2] Acceptance test: phrases rotatives changent toutes les ~5s dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadWaitingAcceptanceTest.kt
- [x] T024 [P] [US2] Acceptance test: redirection auto vers capture après succès + message erreur actionnable si échec dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadWaitingAcceptanceTest.kt

### Implementation for User Story 2

- [x] T025 [US2] Implement `ModelDownloadWaitingScreen` composable (titre, barre de progression LinearProgressIndicator, pourcentage texte, AnimatedWhisk, phrases rotatives AnimatedContent) in app/src/main/java/com/foodgpt/onboarding/ModelDownloadWaitingScreen.kt
- [x] T026 [US2] Implement ViewModel `startDownload()` method calling `downloadModelWithProgress`, updating state Downloading(progress) on each callback, transitioning to Ready or Error in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T027 [US2] Add error UI state within ModelDownloadWaitingScreen (message explicite + bouton Réessayer) in app/src/main/java/com/foodgpt/onboarding/ModelDownloadWaitingScreen.kt
- [x] T028 [US2] Wire navigation: Downloading → ModelDownloadWaitingScreen, Ready → popUpTo + navigate CameraFlowRoutes.Capture in app/src/main/java/com/foodgpt/MainActivity.kt
- [x] T029 [US2] Add ViewModel `retryDownload()` action (Error → Downloading transition) in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt

**Checkpoint**: US2 complète — après confirmation, l'écran d'attente s'affiche avec progression réelle ; la fin du téléchargement redirige vers la caméra ; les erreurs sont gérées.

---

## Phase 5: User Story 3 — Reprendre un téléchargement interrompu (Priority: P2)

**Goal**: En V1, si un téléchargement est interrompu (fichier `.downloading` présent), proposer de le reprendre au prochain lancement (le téléchargement redémarre depuis le début en V1 — la reprise partielle est planifiée pour V2).

**Independent Test**: Interrompre un téléchargement, relancer l'app ; vérifier que l'écran de confirmation propose de reprendre (non "premier téléchargement") et que cliquer "Confirmer" relance le processus.

### Tests for User Story 3 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T030 [P] [US3] Acceptance test: relancement après interruption affiche confirmation avec wording "Reprendre le téléchargement" (et non "Télécharger") dans app/src/androidTest/java/com/foodgpt/onboarding/ModelDownloadResumeAcceptanceTest.kt

### Implementation for User Story 3

- [x] T031 [US3] Detect partial download file (`.downloading` temp file presence) in ModelDownloadViewModel init and expose `isResumable: Boolean` in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T032 [US3] Adapt `ModelDownloadOnboardingScreen` wording: "Reprendre le téléchargement" when isResumable is true in app/src/main/java/com/foodgpt/onboarding/ModelDownloadOnboardingScreen.kt
- [x] T033 [US3] Clean up stale `.downloading` file before starting new download in app/src/main/java/com/foodgpt/gemma4local/GemmaModelDownloader.kt

**Checkpoint**: US3 complète — relancement post-interruption détecte le fichier partiel et propose une reprise.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Robustesse, vérification espace disque, concurrence, passage du modèle existant.

- [x] T034 [P] Add disk space check before download (FR-014 SHOULD): warn if insufficient space in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T035 [P] Ensure no concurrent downloads (FR-012 MUST): mutex/flag preventing double startDownload() in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [x] T036 [P] Skip onboarding when model already present (edge case): verify Ready state bypasses onboarding screens in app/src/main/java/com/foodgpt/onboarding/ModelDownloadViewModel.kt
- [ ] T037 Run quickstart.md validation scenarios manually (6 scénarios) and document results
- [x] T038 Code cleanup: remove dead code, verify no unused imports across onboarding package

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion (T001–T005) — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2) — delivers confirmation flow
- **User Story 2 (Phase 4)**: Depends on Foundational (Phase 2) + US1 T019 (confirmDownload triggers transition to Downloading)
- **User Story 3 (Phase 5)**: Depends on Phase 2 + US1 screen existing (T017)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2 — No dependencies on other stories
- **US2 (P1)**: Can start after Phase 2 — Uses `confirmDownload()` from US1 but its implementation is in Phase 2 ViewModel skeleton
- **US3 (P2)**: Can start after Phase 2 — Adapts confirmation screen text from US1

### Within Each User Story

- Acceptance tests MUST be written and FAIL before implementation (ATDD)
- State models/ViewModel logic before UI composables
- UI composables before navigation wiring
- Story complete before moving to next priority

### Parallel Opportunities

- T003, T004 (extraction) can run in parallel
- T006, T007, T012, T013 (models + tests) can run in parallel within Phase 2
- T014, T015, T016 (US1 acceptance tests) can run in parallel
- T017, T018 (confirmation + offline screens) can run in parallel
- T022, T023, T024 (US2 acceptance tests) can run in parallel
- T034, T035, T036 (polish tasks) can run in parallel

---

## Parallel Example: User Story 1

```bash
# ATDD: Write all acceptance tests first (must FAIL):
Task T014: "Acceptance test premier lancement confirmation"
Task T015: "Acceptance test Plus tard"
Task T016: "Acceptance test offline Réessayer"

# Then implement UI screens in parallel:
Task T017: "ModelDownloadOnboardingScreen composable"
Task T018: "NetworkOfflineScreen composable"
```

---

## Parallel Example: User Story 2

```bash
# ATDD: Write all acceptance tests first (must FAIL):
Task T022: "Acceptance test titre + barre + fouet"
Task T023: "Acceptance test phrases rotatives"
Task T024: "Acceptance test redirection + erreur"

# Then implement:
Task T025: "ModelDownloadWaitingScreen composable"
Task T026: "ViewModel startDownload + progression"  (sequential - depends on T025 for display)
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2)

1. Complete Phase 1: Setup (extraction composants partagés)
2. Complete Phase 2: Foundational (modèles, ViewModel, downloader enrichi)
3. Complete Phase 3: User Story 1 (confirmation + offline)
4. **STOP and VALIDATE**: Tester US1 isolément (quickstart scénarios 1-4, 6)
5. Complete Phase 4: User Story 2 (écran attente + progression)
6. **STOP and VALIDATE**: Tester US2 isolément (quickstart scénario complet)
7. Deploy/demo: flux complet du premier lancement

### Incremental Delivery

1. Setup + Foundational → Infrastructure prête
2. US1 → Confirmation fonctionnelle, pas de téléchargement réel → Démo
3. US2 → Téléchargement avec feedback → Démo complète (MVP!)
4. US3 → Reprise post-interruption → Robustesse accrue
5. Polish → Espace disque, concurrence, nettoyage

---

## Notes

- [P] tasks = fichiers différents, pas de dépendances
- [Story] label mappe la tâche à sa user story pour traçabilité
- Chaque user story est indépendamment testable et livrable
- Vérifier que les tests échouent AVANT l'implémentation (ATDD)
- Committer après chaque tâche ou groupe logique
- Stopper à chaque checkpoint pour valider la story isolément
