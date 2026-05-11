# Tasks: photo-capture-llm-result-flow

**Input**: Design documents from `specs/domains/user-guidance-experience/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md), [research.md](./research.md)

**Tests**: ATDD obligatoire (constitution FoodGPT) — au minimum une tâche test par user story, en échec avant implémentation ciblée.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallélisable (fichiers distincts, pas de dépendance sur une tâche incomplète du même lot)
- **[USn]**: rattachement à la user story du [spec.md](./spec.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: dépendances et repères pour la navigation Compose et la traçabilité doc.

- [x] T001 Ajouter la dépendance `androidx.navigation:navigation-compose` (version alignée Compose 1.6.x / doc AndroidX) dans `e:\Dev\projects\FoodGpt\app\build.gradle.kts`
- [x] T002 [P] Ajouter une entrée de traçabilité `photo-capture-llm-result-flow` → fichiers contrat/plan dans `e:\Dev\projects\FoodGpt\specs\domains\user-guidance-experience\traceability.csv` (ou créer la ligne si le fichier est vide)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: routes, écran résultat minimal, NavHost local au flux caméra, état UX partagé (loader, abandon FR-014).

**⚠️ CRITICAL**: Aucune user story complète avant cette phase.

- [x] T003 Créer les constantes / routes de navigation du flux capture → résultat (ex. sealed interface ou `object`) dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\navigation\CameraFlowRoutes.kt`
- [x] T004 [P] Créer le composable `LlmResultScreen` (affichage texte multi-ligne + état erreur) dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\result\LlmResultScreen.kt`
- [x] T005 Intégrer un `NavHost` imbriqué (ou équivalent) pour l’onglet Caméra : graphe `capture` ↔ `llmResult` dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt`, en conservant les onglets existants
- [x] T006 Étendre `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt` avec : état `llmProcessing` (idle / in_progress / terminal), flag « utilisatrice encore sur l’écran capture », `StateFlow` ou canal pour commandes de navigation vers résultat, désactivation logique photo + test pendant `in_progress` (research Decision 8)
- [x] T007 Brancher le cycle de vie (ex. `LifecycleEventObserver` ou callback depuis `CameraScreen`) pour basculer le flag « encore sur capture » et respecter FR-014 dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt` et/ou `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt`

**Checkpoint**: navigation vers `LlmResultScreen` possible avec payload de test ; abandon détectable.

---

## Phase 3: User Story 1 - Disposer de l'écran de capture avec les bons contrôles (Priority: P1) 🎯 MVP

**Goal**: ordre vertical prévisualisation (ou message) → bouton photo → bouton test LLM ; focus si déjà prévu.

**Independent Test**: ouvrir l’onglet Caméra et vérifier l’ordre des contrôles (spec US1).

### Tests for User Story 1 (MANDATORY) ⚠️

- [x] T008 [P] [US1] Test Compose ou sémantique : ordre des éléments (preview / placeholder, bouton capture, bouton test) dans `e:\Dev\projects\FoodGpt\app\src\androidTest\java\com\foodgpt\camera\CameraCaptureLayoutUiTest.kt` (créer le fichier)

### Implementation for User Story 1

- [x] T009 [US1] Restructurer la `Column` principale de `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt` pour placer le bouton de prise de photo sous la zone de prévisualisation et le bouton test LLM directement sous le bouton photo (FR-004, FR-005)
- [x] T010 [US1] Garantir le message explicite caméra indisponible dans la zone preview (FR-011) sans casser l’ordre des boutons dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt`

**Checkpoint**: US1 vérifiable seule (layout + message indispo).

---

## Phase 4: User Story 2 - Suivre l'analyse LLM puis consulter le résultat (Priority: P1)

**Goal**: loader sur écran capture jusqu’à fin ; navigation vers écran résultat si restée sur capture ; pas de nav auto si abandon (FR-006, FR-007, FR-014).

**Independent Test**: capture → loader → résultat ; capture → retour pendant loader → pas de résultat auto (spec US2).

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T011 [P] [US2] Tests unitaires ViewModel : `in_progress` expose overlay ; terminal + foreground → événement nav ; terminal + abandon → pas d’événement nav dans `e:\Dev\projects\FoodGpt\app\src\test\java\com\foodgpt\camera\CameraLlmFlowViewModelTest.kt` (créer le fichier)

### Implementation for User Story 2

- [x] T012 [US2] Afficher un recouvrement de chargement (loader + texte optionnel) sur `CameraScreen` lorsque `llmProcessing == in_progress` dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt`
- [x] T013 [US2] À la fin du pipeline photo + LLM existant, mapper succès/échec vers payload `LlmResultScreen` et déclencher `navigate` uniquement si le flag « encore sur capture » est vrai dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt` et `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt`
- [x] T014 [US2] Passer le texte résultat / erreur via arguments Navigation sûrs (échapper aux longues chaînes si besoin : `SavedStateHandle` ViewModel destination) dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\result\LlmResultScreen.kt` et le graphe dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt`

**Checkpoint**: parcours photo conforme aux clarifications 2026-05-06.

---

## Phase 5: User Story 3 - Lancer le test LLM depuis le même écran (Priority: P2)

**Goal**: bouton test sous photo déclenche `HomeLlmMockRunner` ; même loader + navigation ; pas de concurrence (FR-008, FR-009, FR-013).

**Independent Test**: tap test depuis onglet Caméra → même comportement que spec US3.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T015 [P] [US3] Test unitaire : second tap sur test ignoré pendant `running` dans `e:\Dev\projects\FoodGpt\app\src\test\java\com\foodgpt\camera\CameraLlmTestButtonTest.kt` (créer le fichier)

### Implementation for User Story 3

- [x] T016 [US3] Injecter / fournir `HomeLlmMockRunner` au `CameraViewModel` via factory dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt` et `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt`
- [x] T017 [US3] Relier le bouton test LLM à la coroutine `runner.run()` avec les mêmes états et navigation que le parcours photo dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt` et `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt`
- [x] T018 [US3] Désactiver visuellement le bouton test pendant `in_progress` dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt`

**Checkpoint**: US3 indépendant une fois US2 en place.

---

## Phase 6: User Story 4 - Comprendre les indisponibilités (Priority: P3)

**Goal**: message clair si caméra indisponible ; test LLM encore accessible si pertinent (US4).

**Independent Test**: simuler indisponibilité caméra ; vérifier message + bouton test.

### Tests for User Story 4 (MANDATORY) ⚠️

- [x] T019 [P] [US4] Test UI ou unitaire : état `unavailable` affiche le message FR-011 et conserve le bouton test visible dans `e:\Dev\projects\FoodGpt\app\src\test\java\com\foodgpt\camera\CameraUnavailableUiStateTest.kt` ou `androidTest` équivalent

### Implementation for User Story 4

- [x] T020 [US4] Affiner les libellés / `contentDescription` accessibilité pour l’état indisponible et le CTA test dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt`

**Checkpoint**: US4 complète les edge cases caméra.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: validation rapide, perf perçue, documentation d’implémentation locale.

- [x] T021 [P] Exécuter les scénarios de `e:\Dev\projects\FoodGpt\specs\domains\user-guidance-experience\quickstart.md` et noter les écarts dans `e:\Dev\projects\FoodGpt\specs\domains\user-guidance-experience\migration-index.md` (section validation manuelle) si nécessaire
- [x] T022 Lancer `e:\Dev\projects\FoodGpt\gradlew.bat :app:testDebugUnitTest` et corriger les régressions sur les nouveaux tests camera/result
- [x] T023 [P] Vérifier l’absence de navigation résultat fantôme lors d’un changement d’onglet pendant `in_progress` dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt`

---

## Phase 8: Shell sans onglets (FR-001, FR-015, FR-016) — alignement spec 2026-05-06

- [x] T024 Retirer `TabRow` / onglets Accueil·Caméra·Critique santé ; `NavHost` capture → résultat comme racine unique dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt` (plus de `HomeViewModel` / `HomeScreen` dans le chrome principal ; `HealthCritiqueViewModel` conservé pour le flux segment ← scan)
- [x] T025 [P] Adapter les tests instrumentés caméra (plus de clic sur « Caméra » ; vérifier l’absence des libellés d’onglets) dans `e:\Dev\projects\FoodGpt\app\src\androidTest\java\com\foodgpt\camera\`
- [x] T026 [P] Réparer la compilation `androidTest` health critique : `FakeHealthCritiqueLlmRunner` dédié `androidTest` + assertion lecture seule simplifiée dans `e:\Dev\projects\FoodGpt\app\src\androidTest\java\com\foodgpt\healthcritique\`
- [x] T027 Corriger le fallback résultat vide : mémoriser le payload de navigation sur l’écran résultat (éviter `getAndClear()` à chaque recomposition) et afficher un repli utile basé sur OCR si payload absent dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\MainActivity.kt` et `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraViewModel.kt` + test `CameraLlmFlowViewModelTest`
- [x] T028 Aligner le gating de navigation sur la présence réelle de la route capture (et non sur `ON_START/ON_STOP` activité) via `DisposableEffect` entrée/sortie d’écran dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\camera\CameraScreen.kt`
- [x] T029 Forcer le parcours Gemma sur le runtime GenAI local ML Kit (sans fallback `.litertlm` app-private) dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\gemma4local\HybridGemma4LocalGateway.kt`
- [x] T030 Supprimer le message d’erreur “import .litertlm” du flux caméra et reclasser tout échec de disponibilité en indisponibilité runtime ML Kit local dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\gemma4local\Gemma4LocalClient.kt`, `Gemma4LocalAvailabilityChecker.kt`, `Gemma4LocalMessages.kt`
- [x] T031 Aligner le client local sur le rework traversant: health-check non bloquant et tentative systématique d’appel réel `generateContent` via gateway ML Kit local dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\gemma4local\Gemma4LocalClient.kt`
- [x] T031 Couvrir le cas “transcription longue” sur l’écran résultat : zone texte scrollable contrainte pour préserver l’accès aux contrôles + test UI dédié dans `e:\Dev\projects\FoodGpt\app\src\main\java\com\foodgpt\result\LlmResultScreen.kt` et `e:\Dev\projects\FoodGpt\app\src\androidTest\java\com\foodgpt\result\LlmResultScreenUiTest.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → aucune dépendance.
- **Phase 2** → après Phase 1 (dépendance Navigation).
- **Phases 3–6** → après Phase 2 ; **US1** peut précéder **US2** pour stabiliser le layout avant loader/nav ; **US2** avant **US3** (réutilise mécanisme nav + états).
- **Phase 7** → après les user stories souhaitées.

### User Story Dependencies

| Story | Dépend de |
|-------|-----------|
| US1 | Phase 2 (routes + NavHost minimal pour afficher l’écran capture dans le bon conteneur) |
| US2 | US1 (ordre des boutons), Phase 2 |
| US3 | US2 |
| US4 | US1 (structure écran) |

### Parallel Opportunities

- T001 / T002 en parallèle.
- T004 / T008 / T011 / T015 / T019 : fichiers nouveaux, après leurs prérequis de phase respectifs.
- Tests [P] d’une même story en parallèle une fois les interfaces ViewModel stabilisées.

### Exemple parallèle (après Phase 2, avant impl US1)

```text
T008 [US1] CameraCaptureLayoutUiTest.kt
T011 [US2] CameraLlmFlowViewModelTest.kt  (si signatures ViewModel déjà figées)
```

---

## Implementation Strategy

### MVP (US1 + fondations)

1. Phase 1 + Phase 2
2. Phase 3 (US1) — livrer layout conforme spec
3. Valider manuellement

### Incrément suivant

4. Phase 4 (US2) — loader + résultat + FR-014  
5. Phase 5 (US3) — test LLM sur le même écran  
6. Phase 6 (US4) — polish indisponibilité  
7. Phase 7

---

## Notes

- Les chemins `e:\Dev\projects\FoodGpt\...` sont volontairement absolus pour l’agent d’implémentation ; en revue, utiliser des chemins relatifs au repo si préféré.
- Ancienne liste « Homepage LLM Mock Trigger » : les tâches cochées historiques ne couvrent pas ce flux ; ce fichier remplace la file d’attente pour `photo-capture-llm-result-flow`.
