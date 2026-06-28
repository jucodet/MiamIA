# Tasks: Inference Backend Badge

**Input**: Design documents from `specs/domains/local-llm-runtime/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/backend-execution-contract.md, quickstart.md

**Tests**: Dans ce projet, les tests d'acceptation/parcours (ATDD) sont **OBLIGATOIRES**. Au minimum une tâche de test par user story, alignée sur les scénarios Given/When/Then du `spec.md`.

**Organization**: Tasks grouped by user story (US1/US2/US3) pour implémentation et test indépendants.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallélisable (fichiers différents, pas de dépendance sur tâche incomplète)
- **[Story]**: user story ciblée (US1, US2, US3) — obligatoire en phase user story
- Chemins de fichiers exacts inclus dans chaque description

## Path Conventions

Projet mobile Android monomodule : sources `app/src/main/java/com/miamia/...`, tests `app/src/test/java/com/miamia/...`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Aucun setup projet requis (module Android existant). Phase vide — la création de l'enum partagé est en Phase 2 (Foundational) car bloquante pour toutes les user stories.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infrastructure partagée (enum `BackendExecution` + mapping) que TOUTE user story consomme. **CRITICAL** : aucune user story ne peut démarrer avant la fin de cette phase.

- [x] T001 Create `BackendExecution` enum (NPU/GPU/CPU/INDETERMINATE) + `label` + `from(com.google.ai.edge.litertlm.Backend)` mapping in `app/src/main/java/com/miamia/gemma4local/model/BackendExecution.kt`
- [x] T002 [P] Contract test for `BackendExecution.from` mapping (NPU/GPU/CPU + inconnu → INDETERMINATE) in `app/src/test/java/com/miamia/gemma4local/BackendExecutionMappingTest.kt`

**Checkpoint**: Fondation prête — l'enum et son mapping sont validés ; la propagation runtime→UI peut commencer.

---

## Phase 3: User Story 1 - Présence correcte de la pastille par backend (Priority: P1) 🎯 MVP

**Goal**: Afficher, à côté de la durée d'inférence, une pastille libellée selon le backend réellement utilisé (NPU/GPU/CPU), cohérente avec la durée (même exécution).
**Independent Test**: Exécuter une inférence réussie et vérifier que la pastille `inference_backend_badge` est affichée à côté de `inference_time_label` avec le libellé du backend réellement utilisé.

### Tests for User Story 1 (MANDATORY) ⚠️ ATDD — écrire FAIRE ÉCHOUER d'abord

- [x] T003 [P] [US1] Acceptance UI test `InferenceBackendBadgeUiTest` : rend `BilanResultCard` avec `inferenceTimeMs > 0` pour NPU/GPU/CPU, vérifie présence `inference_backend_badge` + libellé correct à côté de `inference_time_label` in `app/src/test/java/com/miamia/camera/InferenceBackendBadgeUiTest.kt`

### Implementation for User Story 1

- [x] T004 [US1] Add `val backend: BackendExecution = BackendExecution.INDETERMINATE` to `AnalyzeCompositionResult.BilanSuccess` in `app/src/main/java/com/miamia/composition/CompositionModels.kt`
- [x] T005 [US1] Capture + propagate backend in `LiteRtGemmaEngine` (`retainedBackend` field, set in `tryLoadAndInfer` success, reset in `disposeRetainedLocked`, thread into `runInferenceOnEngine` → `BilanSuccess.backend`, use `retainedBackend` on warm path) in `app/src/main/java/com/miamia/composition/LiteRtGemmaEngine.kt`
- [x] T006 [P] [US1] Add `val backend: BackendExecution = BackendExecution.INDETERMINATE` to `StreamingBilanState.Complete` and `ScanState.BilanReady` in `app/src/main/java/com/miamia/camera/StreamingBilanState.kt` and `app/src/main/java/com/miamia/camera/ScanState.kt`
- [x] T007 [US1] Propagate `outcome.backend` / `v.backend` into `StreamingBilanState.Complete` and `ScanState.BilanReady` in `CameraViewModel.runCompositionStage` in `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [x] T008 [US1] Add `backend: BackendExecution = BackendExecution.INDETERMINATE` param to `BilanResultCard` + render pastille (`Surface` arrondie + icône + libellé `backend.label` + `testTag("inference_backend_badge")`) à gauche du `inference_time_label` dans le `Row` de `InferenceTimeBadge` in `app/src/main/java/com/miamia/camera/BilanResultCard.kt`
- [x] T009 [US1] Pass `backend = bilanState.backend` / `state.backend` to `BilanResultCard` in `app/src/main/java/com/miamia/camera/CameraScreen.kt` and `app/src/main/java/com/miamia/result/LlmResultScreen.kt`

**Checkpoint**: US1 fonctionnelle — pastille présente et libellée par backend, cohérente avec la durée.

---

## Phase 4: User Story 2 - Distinctité visuelle par backend (Priority: P2)

**Goal**: Chaque backend possède un visuel distinct (icône + couleur) permettant l'identification sans lecture du libellé.
**Independent Test**: Rendre les trois pastilles NPU/GPU/CPU et vérifier qu'icône et couleur diffèrent pour chacune.

### Tests for User Story 2 (MANDATORY) ⚠️ ATDD

- [x] T010 [P] [US2] Extend `InferenceBackendBadgeUiTest` : assert distinctité visuelle par backend (icône `Icons.Filled.Memory`/`DeveloperMode`/`DeveloperBoard` + couleurs `MiamIAColors.Primary`/`SectionIngredients`/`OnSurfaceVariant`) in `app/src/test/java/com/miamia/camera/InferenceBackendBadgeUiTest.kt`

### Implementation for User Story 2

- [x] T011 [US2] Implement per-backend icon + color mapping in the pastille (NPU→`Memory`/`Primary`, GPU→`DeveloperMode`/`SectionIngredients`, CPU→`DeveloperBoard`/`OnSurfaceVariant`) in `app/src/main/java/com/miamia/camera/BilanResultCard.kt`

**Checkpoint**: US1 + US2 fonctionnelles — pastille distinguable au visuel sans lecture.

---

## Phase 5: User Story 3 - Cas non trompeur (échec / backend indéterminé) (Priority: P3)

**Goal**: Aucune pastille backend trompeuse pour les inférences échouées avant exécution ou au backend indéterminé.
**Independent Test**: Rendre `BilanResultCard` avec `inferenceTimeMs == 0` (échec pré-exécution) → pas de pastille ; avec backend `INDETERMINATE` → pastille neutre libellée "—".

### Tests for User Story 3 (MANDATORY) ⚠️ ATDD

- [x] T012 [P] [US3] Extend `InferenceBackendBadgeUiTest` : (a) `inferenceTimeMs == 0L` → `inference_backend_badge` absent ; (b) backend `INDETERMINATE` + `inferenceTimeMs > 0` → pastille neutre libellée "—" (icône `HelpOutline`) in `app/src/test/java/com/miamia/camera/InferenceBackendBadgeUiTest.kt`

### Implementation for User Story 3

- [x] T013 [US3] Handle `INDETERMINATE` rendering (icône `Icons.Filled.HelpOutline`, couleur neutre `onSurfaceVariant`, libellé "—") and keep pastille gated behind `inferenceTimeMs > 0L` in `app/src/main/java/com/miamia/camera/BilanResultCard.kt`

**Checkpoint**: Toutes les user stories sont indépendamment fonctionnelles et non trompeuses.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validation transverse et qualité.

- [ ] T014 Run quickstart.md validation: `./gradlew :app:testDebugUnitTest --tests "com.miamia.gemma4local.BackendExecutionMappingTest"` and `--tests "com.miamia.camera.InferenceBackendBadgeUiTest"`
- [ ] T015 [P] Verify pastille lisibilité/contraste thèmes clair et sombre (icône + couleur + libellé) in `app/src/main/java/com/miamia/camera/BilanResultCard.kt`
- [x] T016 [P] Verify no regression on existing `CameraViewModelGemmaErrorTest` / `CameraLlmFlowViewModelTest` / `Gemma4LocalApiContractSuccessTest` (champ `backend` a défaut non-cassant)

### Correctif path production (post-test utilisateur)

> Le plan initial ciblait `LiteRtGemmaEngine`, mais `MainActivity` câble `Gemma4LocalCompositionEngine` → `Gemma4LocalClient` → `HybridGemma4LocalGateway`. Sans ce correctif, le badge restait `INDETERMINATE` ("—") en production.

- [x] T017 [US1] Propagate backend from `HybridGemma4LocalGateway.runInferenceLoop` via new `InferenceOutcome(text, backend)` + `analyzeTextStreamingWithBackend`/`analyzeTextWithBackend` in `app/src/main/java/com/miamia/gemma4local/HybridGemma4LocalGateway.kt`
- [x] T018 [US1] Add `backend: BackendExecution = INDETERMINATE` to `AnalyseTextuelleResult` and populate it in `Gemma4LocalClient.analyze` (uses `*WithBackend` variants for `HybridGemma4LocalGateway`) in `app/src/main/java/com/miamia/gemma4local/model/AnalyseTextuelleResult.kt` and `app/src/main/java/com/miamia/gemma4local/Gemma4LocalClient.kt`
- [x] T019 [US1] Pass `localResult.backend` into `BilanSuccess(backend = ...)` in `Gemma4LocalCompositionEngine.analyze` in `app/src/main/java/com/miamia/composition/Gemma4LocalCompositionEngine.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: vide — démarrage immédiat en Phase 2.
- **Foundational (Phase 2)**: bloque toutes les user stories (enum partagée + mapping).
- **User Stories (Phase 3+)**: dépendent de Phase 2 ; séquentielles par priorité (US1 → US2 → US3) car US2/US3 étendent la pastille introduite en US1.
- **Polish (Phase 6)**: dépend de la complétion des user stories.

### User Story Dependencies

- **US1 (P1)**: démarre après Phase 2. Dépend de T001 (enum). MVP autonome.
- **US2 (P2)**: démarre après US1 (étend la pastille US1). Indépendamment testable visuellement.
- **US3 (P3)**: démarre après US1 (étend la pastille US1). Indépendamment testable sur états non nominaux.

### Within Each User Story

- Tests d'acceptation écrits d'abord (ATDD) et devant échouer avant implémentation.
- Modèles (BilanSuccess/Complete/BilanReady) avant services (engine/viewmodel) avant UI.
- Implémentation core avant intégration écrans.
- Story complète avant passage à la priorité suivante.

### Parallel Opportunities

- T001 et T002 (Phase 2) : T002 [P] peut s'écrire en parallèle de T001 (fichiers différents) mais s'exécute après T001 (compile-time).
- T003 (test US1) [P] peut s'écrire en parallèle de T004–T009 (fichiers différents) — échoue jusqu'à implémentation.
- T006 [P] (deux fichiers d'état) parallélisable avec T004 (CompositionModels).
- T010/T012 (tests US2/US3) [P] parallélisables entre eux (même fichier test mais sections distinctes — séquentiellement pour éviter conflit).
- T015/T016 (Polish) [P] parallélisables.

---

## Parallel Example: User Story 1

```bash
# Écrire tous les tests US1 ensemble (ATDD, doivent échouer d'abord) :
Task: "Acceptance UI test InferenceBackendBadgeUiTest in app/src/test/java/com/miamia/camera/InferenceBackendBadgeUiTest.kt"

# Puis modèles US1 en parallèle (fichiers différents) :
Task: "Add backend to BilanSuccess in CompositionModels.kt"
Task: "Add backend to Complete in StreamingBilanState.kt + BilanReady in ScanState.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 2: Foundational (enum + mapping + test contrat).
2. Complete Phase 3: User Story 1 (propagation runtime→viewmodel→UI + pastille libellée).
3. **STOP and VALIDATE**: `InferenceBackendBadgeUiTest` (US1) + `BackendExecutionMappingTest` passent ; inférence réelle affiche la pastille correcte.
4. Demo si prêt.

### Incremental Delivery

1. Foundational → enum + mapping validés.
2. + US1 → pastille présente et libellée (MVP).
3. + US2 → distinctité visuelle icône/couleur.
4. + US3 → états non trompeurs (INDETERMINATE / échec pré-exécution).
5. Chaque story ajoute de la valeur sans casser la précédente (champ `backend` à défaut non-cassant).

### Parallel Team Strategy

Avec plusieurs devs après Phase 2 :
- Dev A : US1 (chaîne complète runtime→UI).
- Dev B : US2 (mapping visuel pastille) après US1.
- Dev C : US3 (états non nominaux) après US1.

---

## Notes

- [P] = fichiers différents, pas de dépendance sur tâche incomplète.
- [Story] = traçabilité vers spec.md (US1/US2/US3).
- Champ `backend` toujours à défaut `BackendExecution.INDETERMINATE` → non-cassant pour appelants/tests existants.
- Commit après chaque tâche ou groupe logique (hooks git optionnels).
- Valider à chaque checkpoint la story indépendamment.
- Éviter : tâches vagues, conflits sur même fichier, dépendances cross-story cassant l'indépendance.
