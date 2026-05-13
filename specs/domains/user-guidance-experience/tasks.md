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

- **Android app**: `app/src/main/java/com/miamia/`
- **Unit tests**: `app/src/test/java/com/miamia/`
- **Instrumented tests**: `app/src/androidTest/java/com/miamia/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Création des packages et extraction des composants partagés existants.

- [x] T001 Create onboarding package directory at app/src/main/java/com/miamia/onboarding/
- [x] T002 [P] Create shared UI package directory at app/src/main/java/com/miamia/ui/shared/
- [x] T003 [P] Extract `WAITING_PHRASES` list from app/src/main/java/com/miamia/result/LlmResultScreen.kt into app/src/main/java/com/miamia/ui/shared/WaitingPhrases.kt
- [x] T004 [P] Extract `AnimatedWhisk` composable from app/src/main/java/com/miamia/result/LlmResultScreen.kt into app/src/main/java/com/miamia/ui/shared/AnimatedWhisk.kt
- [x] T005 Update imports in app/src/main/java/com/miamia/result/LlmResultScreen.kt to use extracted shared components from com.miamia.ui.shared

**Checkpoint**: Packages créés ; `LlmResultScreen` fonctionne toujours avec les composants extraits.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modèles d'état, détection réseau, enrichissement du downloader — bloquent TOUTES les user stories.

**⚠️ CRITICAL**: Aucune user story ne peut démarrer avant cette phase.

- [x] T006 [P] Create `NetworkType` enum (WIFI, MOBILE_DATA, OFFLINE) in app/src/main/java/com/miamia/onboarding/NetworkTypeDetector.kt
- [x] T007 [P] Create `DownloadProgress` data class (percent, downloadedBytes, totalBytes) in app/src/main/java/com/miamia/onboarding/LlmModelReadinessState.kt
- [x] T008 Create `LlmModelReadinessState` sealed class (Checking, Offline, ConfirmationRequired, Downloading, Ready, Error, Declined) in app/src/main/java/com/miamia/onboarding/LlmModelReadinessState.kt
- [x] T009 Implement `NetworkTypeDetector.detectCurrentNetworkType(context)` using ConnectivityManager.getNetworkCapabilities() in app/src/main/java/com/miamia/onboarding/NetworkTypeDetector.kt
- [x] T010 Enrich `GemmaModelDownloader` with `downloadModelWithProgress(onProgress: (Int, Long, Long) -> Unit): File` method in app/src/main/java/com/miamia/gemma4local/GemmaModelDownloader.kt
- [x] T011 Create `ModelDownloadViewModel` skeleton (init checks model presence, exposes StateFlow<LlmModelReadinessState>) in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T012 [P] Create unit test for NetworkTypeDetector in app/src/test/java/com/miamia/onboarding/NetworkTypeDetectorTest.kt
- [x] T013 [P] Create unit test for ModelDownloadViewModel state transitions in app/src/test/java/com/miamia/onboarding/ModelDownloadViewModelTest.kt

**Checkpoint**: Fondation prête — les modèles d'état, le ViewModel, et le downloader enrichi sont fonctionnels et testés.

---

## Phase 3: User Story 1 — Confirmer le téléchargement du modèle (Priority: P1) 🎯 MVP

**Goal**: Au premier lancement sans modèle, afficher un écran plein de confirmation informant de la taille du téléchargement et du type de connexion détecté (Wi-Fi ou 4G). Permettre de décliner ou confirmer.

**Independent Test**: Simuler un premier lancement sans modèle local ; vérifier l'affichage de l'écran de confirmation, la détection du type de connexion, le blocage du téléchargement tant que la confirmation n'est pas donnée, et le comportement "Plus tard".

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T014 [P] [US1] Acceptance test: premier lancement affiche écran confirmation avec type réseau et taille modèle dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadOnboardingAcceptanceTest.kt
- [x] T015 [P] [US1] Acceptance test: "Plus tard" affiche état explicatif sans déclencher téléchargement dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadOnboardingAcceptanceTest.kt
- [x] T016 [P] [US1] Acceptance test: écran "Connexion requise" affiché quand offline sans modèle, avec bouton Réessayer dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadOnboardingAcceptanceTest.kt

### Implementation for User Story 1

- [x] T017 [US1] Implement `ModelDownloadOnboardingScreen` composable (titre, explication taille, info réseau détecté, boutons Confirmer/Plus tard) in app/src/main/java/com/miamia/onboarding/ModelDownloadOnboardingScreen.kt
- [x] T018 [P] [US1] Implement `NetworkOfflineScreen` composable (message "Connexion requise", explication, bouton Réessayer) in app/src/main/java/com/miamia/onboarding/NetworkOfflineScreen.kt
- [x] T019 [US1] Add ViewModel actions: `confirmDownload()`, `declineDownload()`, `retryNetworkCheck()` in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T020 [US1] Define onboarding navigation routes (onboarding_offline, onboarding_confirm, onboarding_downloading) and integrate as conditional startDestination in app/src/main/java/com/miamia/MainActivity.kt
- [x] T021 [US1] Wire navigation: ConfirmationRequired → ModelDownloadOnboardingScreen, Offline → NetworkOfflineScreen, Ready → CameraFlowRoutes.Capture (popUpTo inclusive) in app/src/main/java/com/miamia/MainActivity.kt

**Checkpoint**: US1 complète — un premier lancement sans modèle affiche la confirmation ou l'écran offline ; "Confirmer" déclenche la transition vers downloading ; "Plus tard" affiche l'état Declined.

---

## Phase 4: User Story 2 — Patienter avec feedback pendant le téléchargement (Priority: P1)

**Goal**: Après confirmation, afficher un écran d'attente engageant avec titre, barre de progression + pourcentage, phrases humoristiques rotatives toutes les 5s, et fouet animé. Redirection automatique vers l'écran capture à la fin.

**Independent Test**: Déclencher un téléchargement ; vérifier que l'écran d'attente affiche le titre permanent, la progression, la rotation des phrases, le fouet animé, et redirige à la fin.

### Tests for User Story 2 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T022 [P] [US2] Acceptance test: écran attente affiche titre "Téléchargement du modèle de langage en cours...", barre de progression, et fouet animé dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadWaitingAcceptanceTest.kt
- [x] T023 [P] [US2] Acceptance test: phrases rotatives changent toutes les ~5s dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadWaitingAcceptanceTest.kt
- [x] T024 [P] [US2] Acceptance test: redirection auto vers capture après succès + message erreur actionnable si échec dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadWaitingAcceptanceTest.kt

### Implementation for User Story 2

- [x] T025 [US2] Implement `ModelDownloadWaitingScreen` composable (titre, barre de progression LinearProgressIndicator, pourcentage texte, AnimatedWhisk, phrases rotatives AnimatedContent) in app/src/main/java/com/miamia/onboarding/ModelDownloadWaitingScreen.kt
- [x] T026 [US2] Implement ViewModel `startDownload()` method calling `downloadModelWithProgress`, updating state Downloading(progress) on each callback, transitioning to Ready or Error in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T027 [US2] Add error UI state within ModelDownloadWaitingScreen (message explicite + bouton Réessayer) in app/src/main/java/com/miamia/onboarding/ModelDownloadWaitingScreen.kt
- [x] T028 [US2] Wire navigation: Downloading → ModelDownloadWaitingScreen, Ready → popUpTo + navigate CameraFlowRoutes.Capture in app/src/main/java/com/miamia/MainActivity.kt
- [x] T029 [US2] Add ViewModel `retryDownload()` action (Error → Downloading transition) in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt

**Checkpoint**: US2 complète — après confirmation, l'écran d'attente s'affiche avec progression réelle ; la fin du téléchargement redirige vers la caméra ; les erreurs sont gérées.

---

## Phase 5: User Story 3 — Reprendre un téléchargement interrompu (Priority: P2)

**Goal**: En V1, si un téléchargement est interrompu (fichier `.downloading` présent), proposer de le reprendre au prochain lancement (le téléchargement redémarre depuis le début en V1 — la reprise partielle est planifiée pour V2).

**Independent Test**: Interrompre un téléchargement, relancer l'app ; vérifier que l'écran de confirmation propose de reprendre (non "premier téléchargement") et que cliquer "Confirmer" relance le processus.

### Tests for User Story 3 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests EN PREMIER, vérifier qu'ils ÉCHOUENT avant implémentation (ATDD)**

- [x] T030 [P] [US3] Acceptance test: relancement après interruption affiche confirmation avec wording "Reprendre le téléchargement" (et non "Télécharger") dans app/src/androidTest/java/com/miamia/onboarding/ModelDownloadResumeAcceptanceTest.kt

### Implementation for User Story 3

- [x] T031 [US3] Detect partial download file (`.downloading` temp file presence) in ModelDownloadViewModel init and expose `isResumable: Boolean` in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T032 [US3] Adapt `ModelDownloadOnboardingScreen` wording: "Reprendre le téléchargement" when isResumable is true in app/src/main/java/com/miamia/onboarding/ModelDownloadOnboardingScreen.kt
- [x] T033 [US3] Clean up stale `.downloading` file before starting new download in app/src/main/java/com/miamia/gemma4local/GemmaModelDownloader.kt

**Checkpoint**: US3 complète — relancement post-interruption détecte le fichier partiel et propose une reprise.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Robustesse, vérification espace disque, concurrence, passage du modèle existant.

- [x] T034 [P] Add disk space check before download (FR-014 SHOULD): warn if insufficient space in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T035 [P] Ensure no concurrent downloads (FR-012 MUST): mutex/flag preventing double startDownload() in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
- [x] T036 [P] Skip onboarding when model already present (edge case): verify Ready state bypasses onboarding screens in app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt
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

---

# Tasks: Feature D — Suppression du message d'accueil sur l'écran capture

**Input**: Design documents from `specs/domains/user-guidance-experience/` (Feature D)
**Prerequisites**: `plan.md` (Feature D), `spec.md` (Feature D), `research.md` (addendum D-1..D-5), `data-model.md` (addendum Feature D), `contracts/capture-screen-no-welcome-banner.md`, `quickstart.md` (addendum Feature D)

**Tests**: Les tests d'acceptation (ATDD) sont **OBLIGATOIRES** (Constitution v0.2.0, Principe II). Chaque user story embarque au moins un test Compose UI d'instrumentation aligné sur ses scénarios Given/When/Then avant toute suppression de code de rendu.

**Organization**: Tâches regroupées par user story. US-D1 (P1) porte la suppression effective de la bannière + assertions d'absence ; US-D2 (P2) couvre la non-régression sur les flux capture/test LLM.

## Phase 1: Setup (Shared Infrastructure) — Feature D

**Purpose**: Vérifier l'écosystème de tests UI Compose et la cohérence des test tags amont (héritages de `capture-recognition`). Aucun nouveau module ni nouvelle dépendance.

- [ ] T101 Vérifier que la suite existante d'instrumentation `com.miamia.camera.*` reste verte (sanity check à exécuter sur poste avec Android SDK configuré) : `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.miamia.camera` — note d'exécution : différé hors environnement sandbox.

---

## Phase 2: Foundational (Blocking Prerequisites) — Feature D

**Purpose**: Garde-fous transverses aux user stories Feature D (audit code amont à supprimer / reconfigurer).

**⚠️ CRITICAL**: Aucun travail US-D1/US-D2 ne démarre tant que Phase 2 n'est pas verte.

- [ ] T102 [P] Confirmer dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` la localisation exacte du bloc à retirer : import `com.miamia.welcome.WelcomeMessageUiState` (autour de la ligne 49), `val welcomeState by viewModel.welcomeUiState.collectAsState()` (autour de la ligne 67), `if (welcomeState is WelcomeMessageUiState.Displayed) { Text(...) }` (autour des lignes 87-93). Ne pas modifier ; uniquement consigner les lignes pour T106.
- [ ] T103 [P] Confirmer la présence des tests AndroidTest à reconfigurer : `app/src/androidTest/java/com/miamia/welcome/US1WelcomeAfterLoginFlowTest.kt`, `app/src/androidTest/java/com/miamia/welcome/US2PositiveToneWelcomeTest.kt`. Lire leur structure actuelle (asserts attendus) pour planifier la reconfiguration en assertions d'absence (T108, T109).

**Checkpoint**: Cible code + cibles tests confirmées ⇒ US-D1 et US-D2 peuvent démarrer.

---

## Phase 3: User Story US-D1 — Écran d'accueil sans bannière d'accueil (Priority: P1) 🎯 MVP

**Goal**: Aucune bannière de message d'accueil rendue sur l'écran capture, dans tous les `ScanState` (UGE-D-FR-001, UGE-D-FR-002).

**Independent Test**: lancer `NoWelcomeBannerOnCaptureUiTest` couvrant ≥ 3 `ScanState` représentatifs (`PreviewActive`, `CameraUnavailable`, `Error`) et vérifier `onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)` dans chacun.

### Tests for User Story US-D1 (MANDATORY) ⚠️

> **NOTE**: ATDD — ces tests DOIVENT être écrits **et observés comme rouges** avant la suppression du bloc de rendu dans `CameraScreen.kt`.

- [ ] T104 [P] [US1] Créer `app/src/androidTest/java/com/miamia/camera/NoWelcomeBannerOnCaptureUiTest.kt` avec un test `no_welcome_banner_in_live_preview` qui force `ScanState.PreviewActive` via `debugOverrideScanStateForTests`, puis assert `composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)`. Vérifier l'échec avant T106.
- [ ] T105 [P] [US1] Étendre le même fichier `NoWelcomeBannerOnCaptureUiTest.kt` avec deux tests supplémentaires : `no_welcome_banner_in_camera_unavailable` (force `ScanState.CameraUnavailable("...")`) et `no_welcome_banner_in_error_state` (force `ScanState.Error("...")`). Chacun assert `assertCountEquals(0)` sur `welcome_message_banner`. Vérifier l'échec avant T106.

### Implementation for User Story US-D1

- [ ] T106 [US1] Dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : (a) retirer l'import `import com.miamia.welcome.WelcomeMessageUiState`, (b) retirer la ligne `val welcomeState by viewModel.welcomeUiState.collectAsState()`, (c) retirer le bloc `if (welcomeState is WelcomeMessageUiState.Displayed) { Text(...) }` (y compris le `testTag("welcome_message_banner")`). Aucun autre changement (préserver `MediaPipeStatusIndicator`, `when (state) { ... }`, et toute la structure capture-recognition).
- [ ] T107 [US1] Faire passer T104 et T105 ; vérifier qu'aucune chaîne de référence à `welcome_message_banner` ne subsiste dans `app/src/main/java/com/miamia/camera/` (`rg "welcome_message_banner|WelcomeMessageUiState" app/src/main/java/com/miamia/camera/` ⇒ 0 occurrence). Re-exécuter `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.NoWelcomeBannerOnCaptureUiTest`.
- [ ] T108 [US1] Reconfigurer `app/src/androidTest/java/com/miamia/welcome/US1WelcomeAfterLoginFlowTest.kt` en test d'**absence** : après un login simulé (parcours existant), assert `composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)`. Le test conserve sa valeur de couverture mais asserte l'attendu actuel (UGE-D-FR-001/004).
- [ ] T109 [US1] Reconfigurer `app/src/androidTest/java/com/miamia/welcome/US2PositiveToneWelcomeTest.kt` en test d'**absence** identique (assert que même si un message « ton positif » est sélectionné en interne par la policy, aucun nœud `welcome_message_banner` n'est rendu sur l'écran capture).

**Checkpoint**: T104, T105 (nouveau test), T108, T109 (tests legacy reconfigurés) passent au vert sur émulateur. Le bloc de rendu welcome a disparu de `CameraScreen.kt`. La couverture de couverture « pas de message d'accueil » est préservée et alignée avec l'exigence.

---

## Phase 4: User Story US-D2 — Aucune régression sur les autres parcours (Priority: P2)

**Goal**: Le retrait du message d'accueil ne MUST modifier ni le comportement de la caméra, ni les boutons de capture/test LLM, ni l'indicateur MediaPipe, ni la navigation vers le résultat (UGE-D-FR-003).

**Independent Test**: relancer la suite d'instrumentation `com.miamia.camera.*` (capture-recognition) et vérifier que `CameraCaptureLayoutUiTest`, `CameraUnavailableLlmButtonUiTest`, `CaptureActionLabelUiTest` passent à l'identique (SC-D-003).

### Tests for User Story US-D2 (MANDATORY) ⚠️

- [ ] T110 [P] [US2] Vérification statique : `rg "WelcomeMessageUiState|welcome_message_banner" app/src/main/java/com/miamia/camera/` ⇒ 0 occurrence (alignement SC-D-004). À automatiser optionnellement via un test unitaire de présence/absence (lecture de fichier ressource) ou laissé en vérification manuelle CI.
- [ ] T111 [P] [US2] Relancer la suite d'instrumentation existante de capture-recognition pour vérifier la non-régression : `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.miamia.camera`. Exigence : `CameraCaptureLayoutUiTest`, `CameraUnavailableLlmButtonUiTest`, `CaptureActionLabelUiTest` doivent tous passer au vert sans modification (SC-D-003).

### Implementation for User Story US-D2

- [ ] T112 [US2] Aucune implémentation distincte — la non-régression est obtenue par la **non-modification** des éléments hors scope (T106 ne touche pas la chrome de capture-recognition ni `MediaPipeStatusIndicator`). Cette tâche consigne explicitement qu'aucun fichier hors `CameraScreen.kt` (et hors tests `welcome/` reconfigurés) ne MUST être modifié dans cette livraison.

**Checkpoint**: T111 vert ⇒ aucune régression sur les flux capture/test LLM. SC-D-003 + SC-D-004 atteints.

---

## Phase 5: Polish & Cross-Cutting Concerns — Feature D

**Purpose**: Traçabilité, migration-index (rétractation explicite de la Feature 010 / Feature C), suivi de nettoyage.

- [ ] T113 [P] Mettre à jour `specs/domains/user-guidance-experience/traceability.csv` en ajoutant les entrées Feature D : `D,spec.md,spec.md,Functional Requirements (UGE-D-FR-001..005),mapped`, `D,spec.md,plan.md,Constitution Check,mapped`, `D,spec.md,contracts/capture-screen-no-welcome-banner.md,Obligations,mapped`, `D,spec.md,research.md,D-Decision 1..5,mapped`, `D,SC-D-001,quickstart.md,Scenario D1,planned`, `D,SC-D-002,quickstart.md,Scenario D1,planned`, `D,SC-D-003,quickstart.md,Régression,planned`, `D,SC-D-004,research.md,D-Decision 1,planned`.
- [ ] T114 [P] Mettre à jour `specs/domains/user-guidance-experience/migration-index.md` avec une entrée **de rétractation** : « Feature D rétracte la portion d'exigence d'affichage de la bannière welcome de la Feature 010 / Feature C — la logique de sélection `welcome/` reste valide en tant que politique mais ne MUST plus être projetée sur l'UI capture ». Statut : `validated` côté specs/code (exécution tests instrumentés différée).
- [ ] T115 Exécuter manuellement les scénarios D1, D2, D3, D4 de `specs/domains/user-guidance-experience/quickstart.md` sur émulateur portrait + appareil compact ; capturer une copie d'écran post-implémentation comparable à la copie d'écran d'origine (référence : chrome épurée, gain d'espace visible sur la `PreviewRegion`).
- [ ] T116 [P] Note de suivi (hors scope cette PR) : créer un ticket pour le nettoyage complet du package `app/src/main/java/com/miamia/welcome/` et du flow `CameraViewModel.welcomeUiState` (décisions D-Decision 1 et D-Decision 2 documentent l'orientation). Référencer la Feature D comme prérequis de couverture.

---

## Dependencies & Execution Order — Feature D

### Phase Dependencies

- **Setup (Phase 1)** : aucune dépendance, démarre immédiatement (sanity check facultatif si l'environnement n'a pas le SDK Android).
- **Foundational (Phase 2)** : dépend de Phase 1 ; **bloque** US-D1 et US-D2 (cibles code/test à confirmer avant édition).
- **US-D1 (Phase 3)** : dépend de Phase 2 ; **indépendant** d'US-D2 (les deux opèrent dans le même fichier de production, US-D1 modifie, US-D2 vérifie la non-régression).
- **US-D2 (Phase 4)** : dépend de Phase 2 + idéalement de l'achèvement de T106 (pour mesurer la non-régression sur le delta réel). T110/T111 peuvent toutefois être lancés en parallèle de T108/T109.
- **Polish (Phase 5)** : dépend de l'achèvement des deux user stories (T113/T114 référencent l'état post-merge ; T116 peut être planifié dès la fin de US-D1).

### User Story Dependencies

- **US-D1 (P1)** : démarre après Phase 2 ; pas de dépendance vers US-D2. Touche `CameraScreen.kt` + 1 nouveau test + 2 tests legacy reconfigurés.
- **US-D2 (P2)** : démarre après Phase 2 ; **dépendance douce** vers T106 (US-D1) pour observer la suppression effective. Aucune modification de code de production.

### Within Each User Story

- Tests d'acceptation MUST être rouges avant implémentation (T104/T105 avant T106 ; T108/T109 avant que les tests existants ne soient adaptés).
- Aucune nouvelle entité / modèle ⇒ pas d'ordre modèle→service.
- Suppression du bloc de rendu (T106) **après** T104/T105 (ATDD).
- Reconfiguration tests legacy (T108/T109) peut être faite en parallèle de T106 — séquentiellement ils sont indépendants (fichiers différents).

### Parallel Opportunities — Feature D

- T102 ‖ T103 (Phase 2) — fichiers / activités distincts (vérification production vs vérification tests).
- T104 ‖ T105 (Phase 3 / Tests US-D1) — même fichier mais tests indépendants ; peuvent être édités séquentiellement dans un même commit.
- T108 ‖ T109 (Phase 3 / Reconfiguration legacy) — deux fichiers de test distincts.
- T110 ‖ T111 (Phase 4) — vérification statique ‖ exécution suite instrumentation existante.
- T113 ‖ T114 ‖ T116 (Phase 5) — fichiers et nature distincts (traceability, migration-index, ticket de suivi).

---

## Parallel Example: User Story US-D1 (tests ATDD)

```bash
# Lancer les tests rouges en parallèle :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.NoWelcomeBannerOnCaptureUiTest &

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.welcome.US1WelcomeAfterLoginFlowTest &

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.welcome.US2PositiveToneWelcomeTest &

wait
```

## Parallel Example: User Story US-D2 (non-régression)

```bash
# Vérification statique en local :
rg "WelcomeMessageUiState|welcome_message_banner" app/src/main/java/com/miamia/camera/

# Suite d'instrumentation capture-recognition (non-régression) :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.package=com.miamia.camera
```

---

## Implementation Strategy — Feature D

### MVP First (User Story US-D1 Only)

1. Phase 1 (T101) — sanity build/tests.
2. Phase 2 (T102, T103) — audit cibles code + tests legacy.
3. Phase 3 (T104 → T109) — suppression effective + reconfigurations.
4. **STOP et valider** : MVP utilisable (l'écran d'accueil est épuré, conforme à la décision produit), US-D2 reste à vérifier comme polish.

### Incremental Delivery

1. Setup + Foundational ✅ → audit prêt.
2. US-D1 ✅ → bannière supprimée ; tests d'absence verts (nouveau + legacy reconfigurés) — démontrable seul.
3. US-D2 ✅ → suite capture-recognition verte sans modification de code de production — démontrable seul (ou cumulé avec US-D1).
4. Polish ✅ → traçabilité + migration-index (rétractation) + validation manuelle + suivi cleanup planifié.

### Parallel Team Strategy

À deux développeurs :

1. Dev A : Phase 2 + US-D1 (T104 → T107 séquence atomique sur `CameraScreen.kt`).
2. Dev B : T108 + T109 (reconfiguration tests legacy `welcome/`) dès Phase 2 verte, en parallèle de Dev A.
3. Polish (T113–T116) partageable.

---

## Notes — Feature D

- `[P]` = fichiers / activités disjointes, exécutables en parallèle.
- `[Story]` = traçabilité utilisateur (US-D1 = `US1` ici, US-D2 = `US2`) ; absent pour Setup / Foundational / Polish.
- ATDD : T104, T105, T108, T109 **doivent être rouges** avant T106 (suppression effective du bloc bannière).
- À éviter : suppression du package `welcome/` dans cette livraison (décision D-Decision 1, suivi T116) ; suppression du flow `CameraViewModel.welcomeUiState` dans cette livraison (D-Decision 2, suivi T116) ; modification de la chrome non-bannière (MediaPipe, CaptureActionBar) — strict diff minimum sur `CameraScreen.kt`.
