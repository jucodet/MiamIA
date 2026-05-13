# Tasks: ingredient-phrase-segment + FR-010 (auto-analyze-ingredients-tag)

**Input**: Design documents from `specs/domains/ingredient-normalization-validation/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Tests**: ATDD **obligatoire** (constitution MiamIA) — au moins une tâche de test par user story, alignée sur les scénarios Given/When/Then du `spec.md`.

**Organization**: Phases par priorité métier : fondations → **US1 (P1)** → **US2b (P1)** → **US2 (P2)** → **US3 (P3)** → polish.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallélisable (fichiers distincts, pas de dépendance sur une tâche incomplète du même lot)
- **[Story]**: US1, US2, US2b, US3 — obligatoire sur les phases user story
- Chemins fichiers explicites (module `app/`)

---

## Phase 1: Setup

**Purpose**: Cadrage branche, lecture des artefacts domaine avant toucher au code.

- [x] T001 Vérifier que la branche active correspond au périmètre spec (`021-auto-analyze-ingredients-tag` ou équivalent) et que [plan.md](./plan.md) référence bien [spec.md](./spec.md)
- [x] T002 [P] Confirmer la présence des contrats `specs/domains/ingredient-normalization-validation/contracts/boundary-resolver-contract.md` et `specs/domains/ingredient-normalization-validation/contracts/session-capture-intent-for-implicit-validation.md`
- [x] T003 [P] Relire [data-model.md](./data-model.md) (transitions capture → analyse, intention « balise ingrédients »)

---

## Phase 2: Foundational (bloquant)

**Purpose**: Invariants partagés par toutes les user stories — isolation segment + gate soumission.

**Checkpoint**: aucune story ne démarre avant T006 vert (tests JVM fondations au vert).

- [ ] T004 Exécuter `./gradlew :app:testDebugUnitTest` avec SDK Android configuré (`ANDROID_HOME` ou `sdk.dir` dans `local.properties`) et corriger tout échec bloquant sur `app/src/test/java/com/miamia/analysis/ingredientsegment/`
- [x] T005 [P] Vérifier non-régression `IngredientSegmentPreparationContractTest.kt` et `IngredientSegmentFallbackAcceptanceTest.kt` sous `app/src/test/java/com/miamia/analysis/ingredientsegment/`
- [x] T006 Vérifier que `AnalysisSubmissionGate.kt` et `IngredientSegmentModels.kt` exposent bien le contrat FR-007 / FR-010 / FR-011 (paramètre `implicitValidationFromIngredientsFraming`, champ `implicitValidationFromIngredientsFraming` sur `AnalysisSubmissionDecision`)

---

## Phase 3: User Story 1 — Proposer une liste d’ingrédients (Priority: P1) 🎯 MVP

**Goal**: Délimitation déterministe du segment (FR-002 à FR-006, FR-003 : `.` + espace/newline ou fin de texte ; `!` / `?` inconditionnels ; première ancre ; FR/EN).

**Independent Test**: `IngredientSegmentBoundaryResolverTest` + `IngredientSegmentPhraseBoundaryAcceptanceTest` + jeux `app/src/test/java/com/miamia/analysis/ingredientsegment/fixtures/OcrFixtures.kt` — 100 % verts pour les cas US1.

### Tests for User Story 1 (MANDATORY) ⚠️

- [x] T007 [P] [US1] Garantir la couverture BC (points internes, `. `, `.\n`, EOF) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt` alignée sur `contracts/boundary-resolver-contract.md`
- [x] T008 [P] [US1] Vérifier les scénarios US1 §1–§2 via `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPhraseBoundaryAcceptanceTest.kt`
- [x] T009 [P] [US1] Vérifier la perf de non-régression dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPerformanceTest.kt`

### Implementation for User Story 1

- [x] T010 [US1] Maintenir `resolveEnd()` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolver.kt` conforme FR-003 (voir [research.md](./research.md) décisions 017)
- [x] T011 [US1] Maintenir `IngredientSegmentPreparationService.kt` et `IngredientAnchorNormalizer.kt` cohérents avec la sortie du resolver (`app/src/main/java/com/miamia/analysis/ingredientsegment/`)

**Checkpoint**: US1 isolable — proposition automatique stable sans dépendre de la validation UI.

---

## Phase 4: User Story 2b — Parcours accéléré balise ingrédients (Priority: P1)

**Goal**: FR-010 / SC-005 — OCR réussi + balise active + segment exploitable → enchaînement analyse **sans** `SegmentConfirmationRequired` ; traçabilité décision (FR-009).

**Independent Test**: gate + orchestration : balise active → pas d’état confirmation ; balise inactive → flux inchangé (FR-011).

### Tests for User Story 2b (MANDATORY) ⚠️

- [x] T012 [P] [US2b] Compléter / maintenir les cas implicites dans `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGateContractTest.kt` (segment valide + framing ; label seul bloqué)
- [x] T013 [P] [US2b] Étendre `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionDecisionAcceptanceTest.kt` pour le chemin `implicitValidationFromIngredientsFraming = true`
- [x] T014 [P] [US2b] Ajouter ou enrichir un test instrumenté / UI vérifiant l’absence d’écran `SegmentConfirmationRequired` lorsque le chip `ingredients_framing_tag_chip` est sélectionné — `app/src/androidTest/java/com/miamia/camera/ingredientsegment/IngredientSegmentConfirmationUiTest.kt` (ou nouveau fichier co-localisé)

### Implementation for User Story 2b

- [x] T015 [US2b] Orchestrer le saut de confirmation dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (`capturePhoto` → `confirmSegmentAndAnalyze()` lorsque `previewDecision.submissionAllowed && previewDecision.implicitValidationFromIngredientsFraming`)
- [x] T016 [US2b] Exposer l’intention « balise ingrédients » via `ingredientsFramingTagActive` / `setIngredientsFramingTagActive` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [x] T017 [US2b] Brancher le `FilterChip` « Balise ingrédients » (`testTag` `ingredients_framing_tag_chip`) dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: US2b livrable indépendamment une fois US1 + Foundational verts.

---

## Phase 5: User Story 2 — Confirmer ou corriger avant analyse (Priority: P2)

**Goal**: FR-007 hors FR-010 — écran `SegmentConfirmationRequired`, confirmation explicite, pas d’analyse sans validation utilisateur (SC-003).

**Independent Test**: `IngredientSegmentConfirmationUiTest` + flux manuel : sans balise → écran confirmation obligatoire.

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T018 [P] [US2] Maintenir `app/src/androidTest/java/com/miamia/camera/ingredientsegment/IngredientSegmentConfirmationUiTest.kt` (bouton `confirm_segment_button`, parcours sans balise)
- [x] T019 [P] [US2] Vérifier `app/src/test/java/com/miamia/camera/CameraLlmFlowViewModelTest.kt` pour les chemins composition après validation explicite

### Implementation for User Story 2

- [x] T020 [US2] Conserver l’UI confirmation dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` (branche `ScanState.SegmentConfirmationRequired`) et `confirmSegmentAndAnalyze()` / `rejectSegmentConfirmation()` dans `CameraViewModel.kt`
- [x] T021 [US2] S’assurer que `AnalysisSubmissionGate.evaluate(..., userConfirmed = true)` reste le seul chemin après tap « Confirmer et analyser » (`CameraViewModel.kt`)

**Checkpoint**: US2 vérifiable sans activer la balise ingrédients.

---

## Phase 6: User Story 3 — Absence de liste exploitable (Priority: P3)

**Goal**: FR-008 — pas d’analyse sur segment vide / sans ancre ; messages et reprise.

**Independent Test**: textes sans ancre ou proposition vide → erreur contrôlée, pas de liste fictive.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T022 [P] [US3] Maintenir `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentFallbackAcceptanceTest.kt` (ancre absente)
- [x] T023 [P] [US3] Vérifier les messages d’échec OCR / segment dans `app/src/test/java/com/miamia/ingredients/` (ex. `ScanFailureMessageBuilder` si touché par le flux)

### Implementation for User Story 3

- [x] T024 [US3] Vérifier que `CameraViewModel.capturePhoto` mappe toujours les cas sans ancre / segment vide vers `ScanState.Error` ou états appropriés sans lancer la composition (`app/src/main/java/com/miamia/camera/CameraViewModel.kt`)

**Checkpoint**: US3 sans régression après changements US2b.

---

## Phase 7: Polish & cross-cutting

**Purpose**: Suite complète, doc domaine amont, quickstart.

- [ ] T025 Exécuter `./gradlew :app:testDebugUnitTest` puis `./gradlew :app:connectedDebugAndroidTest` si émulateur disponible (couverture US2 / US2b UI)
- [ ] T026 [P] Valider manuellement les sections A–H de `specs/domains/ingredient-normalization-validation/quickstart.md`
- [x] T027 [P] Si le signal « balise » est porté uniquement par l’UI locale, ajouter une phrase de référence croisée dans `specs/domains/capture-recognition/spec.md` (ACL / intention de session) sans dupliquer les FR métier
- [x] T028 Mettre à jour `specs/domains/ingredient-normalization-validation/traceability.csv` (si présent) pour lier FR-010 / tâches T012–T017

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → immédiate
- **Phase 2** → après Phase 1 ; **bloque** US1–US3
- **Phase 3 (US1)** → après Phase 2
- **Phase 4 (US2b)** → après Phase 2 ; **recommandé** après Phase 3 (segment correct avant auto-analyse)
- **Phase 5 (US2)** → après Phase 2 ; peut suivre Phase 4 (mêmes fichiers `CameraViewModel` / `CameraScreen` — séquence T015–T021 pour limiter les conflits)
- **Phase 6 (US3)** → après Phase 2 ; peut être parallèle à US2 une fois US1 vert
- **Phase 7** → après les stories cibles livrées

### User Story Dependencies

| Story | Dépend de |
|-------|-----------|
| US1 | Foundational |
| US2b | Foundational, US1 (logique segment) |
| US2 | Foundational, US1 ; cohérence avec US2b sur `CameraViewModel` / `CameraScreen` |
| US3 | Foundational, US1 |

### Within each story

- Tests (T007–T009, T012–T014, …) **avant** ou en lockstep avec les changements produits (ATDD).
- Ne pas fusionner US2 et US2b dans une même PR sans tests T012–T014 + T018 verts.

### Parallel Opportunities

- T002, T003 en parallèle
- T005 + préparation T007–T009 (lecture) en parallèle après T004 connu vert
- T007, T008, T009 en parallèle
- T012, T013, T014 en parallèle (attention : T014 instrumenté peut être long seul)
- T022, T023 en parallèle
- T026, T027 en parallèle

---

## Parallel Example: User Story 2b

```bash
# Tests ATDD FR-010 en parallèle (fichiers de test distincts) :
Task: "T012 AnalysisSubmissionGateContractTest implicit framing"
Task: "T013 AnalysisSubmissionDecisionAcceptanceTest implicit path"
Task: "T014 IngredientSegmentConfirmationUiTest balise active"
```

---

## Implementation Strategy

### MVP minimal (US1 seule)

1. Phases 1–2 puis Phase 3 jusqu’au **Checkpoint** US1.  
2. `./gradlew :app:testDebugUnitTest` — arrêt si échec.

### Livraison FR-010 (valeur produit balise)

1. US1 vert → Phase 4 (US2b) complète (T012–T017).  
2. Valider SC-005 (pas d’écran intermédiaire) + SC-003 (sans balise inchangé).

### Incrémental complet

1. Setup + Foundational  
2. US1 → US2b → US2 → US3  
3. Polish (T025–T028)

---

## Task counts (aperçu)

| Phase | Story | Tâches (ids) |
|-------|-------|----------------|
| 1 Setup | — | T001–T003 (3) |
| 2 Foundational | — | T004–T006 (3) |
| 3 US1 | US1 | T007–T011 (5) |
| 4 US2b | US2b | T012–T017 (6) |
| 5 US2 | US2 | T018–T021 (4) |
| 6 US3 | US3 | T022–T024 (3) |
| 7 Polish | — | T025–T028 (4) |
| **Total** | | **28 tâches** |

---

## Phase 7: OCR intégral → LLM (FR-012 / FR-014) — 2026-05-13

**Goal**: Entrée du modèle de composition = transcript OCR sessionnel intégral ; plus de refus basé sur `anchorFound` seul ; garde-fou sur transcript vide / label seul.

**Independent Test**: `AnalysisSubmissionGateContractTest` + flux `CameraViewModel` (confirmation affiche le transcript complet).

- [x] T024 [US1] Étendre `AnalysisSubmissionGate.evaluate(..., fullOcrTranscript)` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGate.kt`.
- [x] T025 [P] [US1] Mettre à jour les tests gate dans `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionGateContractTest.kt` et `AnalysisSubmissionDecisionAcceptanceTest.kt` (cas sans ancre + transcript non vide).
- [x] T026 [US1] Orchestration `CameraViewModel.capturePhoto` et `confirmSegmentAndAnalyze` : transcript complet vers le gate et `runCompositionStage` ; suppression du court-circuit `Success` sur `!anchorFound` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`.
- [x] T027 [P] [US1] Libellé confirmation « Vérifier le texte reconnu » dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`.
- [x] T028 [P] Artefacts spec : `specs/domains/ingredient-normalization-validation/plan.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/llm-input-full-ocr-contract.md`.

**Checkpoint**: SC-006 — `segmentPreview` / entrée LLM alignés sur le transcript trim ; ancre absente n’empêche plus une soumission valide si le transcript est substantiel.

---

## Notes

<<<<<<< HEAD
- **017** : changement principal `IngredientSegmentBoundaryResolver.kt` ; fixtures `OcrFixtures` et tests boundary associés.
- **021 (FR-010)** : `AnalysisSubmissionGate`, `CameraViewModel`, `CameraScreen`, `AnalysisSubmissionDecision` ; exécuter `./gradlew :app:testDebugUnitTest` avec SDK Android (`ANDROID_HOME` ou `local.properties`).
- **2026-05-13 (FR-012)** : paramètre `fullOcrTranscript` sur le gate ; wrapper Gradle requis (`gradle/wrapper/gradle-wrapper.jar` présent) pour lancer les tests en local.
- US2 / US3 (phases 3–4) : vérifications de non-régression autour du gate et du fallback.
=======
- **T004 / T025** : restent **ouverts** tant que le SDK Android n’est pas configuré (`local.properties` / `ANDROID_HOME`) — exécuter Gradle localement pour clore.
- **T026** : validation manuelle quickstart (A–H) — à faire côté équipe / device réel.
- Les IDs **T001–T028** sont séquentiels pour éviter les collisions avec d’anciens plans ; marquer `[x]` au fur et à mesure dans ce fichier.
- Frontière DDD : pas d’inférence « balise » depuis le seul texte OCR — voir [contracts/session-capture-intent-for-implicit-validation.md](./contracts/session-capture-intent-for-implicit-validation.md).
- **SC-004** : ne s’applique pas au parcours FR-010 (clarification spec 2026-05-13).
>>>>>>> f2d806ea7921ea48dd8d92efc6c8fa3783e1ba2c
