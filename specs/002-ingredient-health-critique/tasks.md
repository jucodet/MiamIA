# Tasks: Critique santé d’une liste d’ingrédients (prompt LLM)

**Input**: Design documents from `specs/002-ingredient-health-critique/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Contexte**: Une implémentation **healthcritique** existe déjà dans l’app ; ce backlog **réaligne** le produit sur la spec et les clarifications **2026-05-04** (entrée = **segment validé** du scan, **lecture seule**, **SC-005**, `no_validated_segment` dans `specs/002-ingredient-health-critique/contracts/health-critique-llm-contract.md`).

**Tests**: ATDD **obligatoire** — au minimum une tâche de test par user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallélisable (fichiers distincts, pas de dépendance interne au lot)
- **[Story]**: `[US1]`, `[US2]`, `[US3]` uniquement dans les phases user story

## Phase 1: Setup (alignement modèle & contrat)

**Purpose**: refléter le contrat et le data-model à jour dans le code.

- [X] T001 Extend `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueModels.kt` with `InputInvalidReason.NO_VALIDATED_SEGMENT` (et tout champ nécessaire pour mapper `HealthCritiqueResult.InputInvalid` au contrat JSON)

---

## Phase 2: Foundational (validation & moteur)

**Purpose**: refus sans segment ; chaîne transmise au LLM = segment validé uniquement.

- [X] T002 Update `app/src/main/java/com/foodgpt/healthcritique/HealthIngredientInputValidator.kt` to support **absence de segment** (`NO_VALIDATED_SEGMENT`) avec message FR clair (FR-005, contrat)
- [X] T003 Update `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueEngine.kt` so `analyze` **rejette** tout `ingredientText` qui ne correspond pas à la règle produit « segment courant » lorsque cette règle est appliquée (documenter dans KDoc : appelant MUST passer le segment validé ; option : pré-validation explicite avant `validate()` longueur)
- [X] T004 [P] Add unit tests in `app/src/test/java/com/foodgpt/healthcritique/HealthIngredientInputValidatorTest.kt` for `NO_VALIDATED_SEGMENT` (blank / null segment) and keep existing empty / too_short cases

**Checkpoint**: le moteur peut retourner `input_invalid` / `no_validated_segment` sans appeler Gemma.

---

## Phase 3: User Story 1 — Liste scan + lecture seule + 4 sections (P1) MVP

**Goal**: après **BilanReady** (ou équivalent), l’onglet critique santé affiche le **même texte** que `rawTranscript` / segment passé à la composition ; **read-only** ; analyse → 4 sections ; SC-005.

**Independent Test**: scan → segment validé → bilan ; onglet santé : texte **identique** au segment, non éditable ; analyse OK ; sans scan : message `no_validated_segment`.

### Tests for User Story 1 (MANDATORY)

- [X] T005 [P] [US1] Add `HealthCritiqueSegmentParityTest.kt` in `app/src/test/java/com/foodgpt/healthcritique/HealthCritiqueSegmentParityTest.kt` (Given segment fixe — When `HealthCritiqueEngine.analyze` avec fake runner — Then chaîne passée au runner **égale** au segment — SC-005)
- [X] T006 [US1] Add or extend Compose/UI test in `app/src/androidTest/java/com/foodgpt/healthcritique/HealthCritiqueReadOnlySegmentAndroidTest.kt` (champ liste **readOnly** / pas de mutation clavier sur le segment affiché)

### Implementation for User Story 1

- [X] T007 [US1] Expose dans `app/src/main/java/com/foodgpt/camera/CameraViewModel.kt` une API stable (ex. `StateFlow<String?>` `lastValidatedSegmentForHealth` ou lecture depuis `ScanState.BilanReady.rawTranscript`) mise à jour lors de `ScanState.BilanReady` et **réinitialisée** quand le scan repart / segment invalide (alignement `specs/002-ingredient-health-critique/data-model.md`)
- [X] T008 [US1] Wire `app/src/main/java/com/foodgpt/MainActivity.kt` pour transmettre le segment validé au `HealthCritiqueViewModel` (callbacks, `viewModelScope`, ou factory enrichie) sans cycle de dépendances cassé
- [X] T009 [US1] Refactor `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueViewModel.kt` : supprimer la mutation utilisateur type **saisie libre** ; n’accepter que `setValidatedSegmentFromScan(String?)` (ou équivalent) ; `analyze()` utilise **uniquement** ce buffer
- [X] T010 [US1] Update `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueScreen.kt` : affichage **lecture seule** (`readOnly = true` sur `OutlinedTextField` ou `Text` + style) ; désactiver tout chemin qui modifie la liste hors resynchronisation depuis le scan
- [X] T011 [US1] Garantir que `app/src/main/java/com/foodgpt/healthcritique/LiteRtHealthCritiqueRunner.kt` (ou l’appelant) reçoit **exactement** la même `String` que celle affichée (assertion debug optionnelle en `DEBUG`)

**Checkpoint**: US1 conforme FR-001 + clarification lecture seule + SC-005 sur parcours nominal.

---

## Phase 4: User Story 2 — Prudence (P2)

**Goal**: inchangé fonctionnellement ; vérifier que le prompt reflète toujours les exigences US2 une fois le flux scan branché.

### Tests for User Story 2 (MANDATORY)

- [X] T012 [P] [US2] Confirm `app/src/test/java/com/foodgpt/healthcritique/HealthCritiquePromptPrudenceTest.kt` couvre toujours les chaînes obligatoires après toute évolution de `HealthCritiquePromptBuilder.kt`

### Implementation for User Story 2

- [X] T013 [US2] Revoir `app/src/main/java/com/foodgpt/healthcritique/HealthCritiquePromptBuilder.kt` pour mention explicite que la liste provient d’une **capture** (contexte OCR) si pertinent pour le modèle, sans violer l’interdiction de détails d’implémentation inutiles côté spec

---

## Phase 5: User Story 3 — Copie & historique (P3)

**Goal**: historique et copie utilisent la **liste segment** (même contenu SC-005).

### Tests for User Story 3 (MANDATORY)

- [X] T014 [P] [US3] Extend `app/src/androidTest/java/com/foodgpt/healthcritique/HealthCritiquePersistenceAndroidTest.kt` (ou test JVM si possible) pour vérifier que `ingredientRaw` persisté == segment validé passé à l’analyse

### Implementation for User Story 3

- [X] T015 [US3] Aligner `LastHealthAnalysisStore` + sauvegarde dans `HealthCritiqueViewModel.kt` sur le **segment validé** effectivement analysé (FR-006)
- [X] T016 [US3] Vérifier les actions copie dans `HealthCritiqueScreen.kt` / `HealthCritiqueClipboard.kt` restent conformes **SC-003**

---

## Phase 6: Polish & documentation

- [X] T017 [P] Mettre à jour `specs/002-ingredient-health-critique/quickstart.md` si le flux manuel diffère après intégration (chemins UI réels)
- [X] T018 Vérifier cohérence `specs/002-ingredient-health-critique/contracts/health-critique-llm-contract.md` avec les types Kotlin (`InputInvalidReason`) et ajuster les noms d’erreurs côté UI si besoin

---

## Dependencies & Execution Order

- **Phase 1 → 2 → 3** séquentiel pour la chaîne segment → moteur → UI.
- **US2 / US3** peuvent suivre immédiatement après **T011** (US1 cœur) si ressources parallèles.

### Parallel opportunities

- T004 ∥ T005 (après T002–T003) ; T012 ∥ T014 ; T017 en fin.

### MVP

- Terminer **Phase 1–3** (T001–T011 + tests T005–T006) pour une démo « scan → santé read-only → analyse ».

### Comptage

| Zone | Tâches |
|------|--------|
| Phase 1 | 1 |
| Phase 2 | 3 |
| US1 | 7 |
| US2 | 2 |
| US3 | 3 |
| Polish | 2 |
| **Total** | **18** |

---

## Notes

- Pour `check-prerequisites.ps1 -Json`, utiliser une **branche feature** conforme ou `SPECIFY_FEATURE_DIRECTORY=specs/002-ingredient-health-critique` selon votre environnement.
- Si `CameraViewModel` ne conserve pas encore `rawText` après navigation, s’appuyer sur `ScanState.BilanReady.rawTranscript` comme source affichée (déjà présent dans `runCompositionStage`).
