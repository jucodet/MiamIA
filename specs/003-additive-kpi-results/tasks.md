---

description: "Task list template for feature implementation"
---

# Tasks: Écran KPI de risque additifs

**Input**: Design documents from `/home/ju/dev/projects/FoodGpt/specs/003-additive-kpi-results/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/additive-kpi-ui-contract.md, quickstart.md

**Tests**: ATDD obligatoire (template projet) — au minimum un fichier de test par user story, aligné sur les scénarios Given/When/Then du `spec.md`.

**Note**: `check-prerequisites.ps1` peut pointer vers une autre feature si `.specify/feature.json` n’est pas aligné ; pour cette livraison, utiliser explicitement `specs/003-additive-kpi-results/` (variable `SPECIFY_FEATURE_DIRECTORY` si besoin).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile (ce dépôt)** : `app/src/main/java/com/foodgpt/`, `app/src/test/java/com/foodgpt/`, `app/src/androidTest/java/com/foodgpt/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Créer le module domaine `additives` et les types partagés par toutes les user stories.

- [x] T001 Créer `AdditiveRiskLevel.kt` et `AdditiveLineConfidence.kt` dans `app/src/main/java/com/foodgpt/additives/` selon `specs/003-additive-kpi-results/data-model.md`
- [x] T002 [P] Créer `AdditiveRiskItem.kt` dans `app/src/main/java/com/foodgpt/additives/AdditiveRiskItem.kt` selon `specs/003-additive-kpi-results/data-model.md`
- [x] T003 [P] Créer `RiskSummaryKpi.kt` et `AnalysisDisplayResult.kt` dans `app/src/main/java/com/foodgpt/additives/` selon `specs/003-additive-kpi-results/data-model.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Pipeline parse → `AnalysisDisplayResult`, compatibilité bilan existant, prompts modèle. Aucune user story UI ne démarre avant cette phase.

**⚠️ CRITICAL**: `GemmaBilanParser` lit tout le bloc après `###ANALYSE` ; si un marqueur additifs est ajouté après l’analyse, il faut isoler le texte d’analyse pour ne pas polluer `compositionAnalysis`.

- [x] T004 Implémenter `AdditiveKpiParser.kt` dans `app/src/main/java/com/foodgpt/additives/AdditiveKpiParser.kt` (motif `NIVEAU|nom|justification`, repli prudent, normalisation dédoublonnage, tri HIGH→MEDIUM→LOW→UNKNOWN, flags `AdditiveLineConfidence`) selon `specs/003-additive-kpi-results/research.md` et FR-001/FR-003/FR-005
- [x] T005 Implémenter `BuildAdditiveKpiDisplay.kt` dans `app/src/main/java/com/foodgpt/additives/BuildAdditiveKpiDisplay.kt` avec signature et invariants `RiskSummaryKpi` (SC-003) décrits dans `specs/003-additive-kpi-results/contracts/additive-kpi-ui-contract.md`
- [x] T006 Mettre à jour `app/src/main/java/com/foodgpt/composition/GemmaBilanParser.kt` pour exclure du champ `compositionAnalysis` tout bloc délimité après `###ANALYSE` (ex. `###ADDITIFS_RISQUE`) si le prompt introduit ce marqueur, et étendre ou ajuster `app/src/test/java/com/foodgpt/composition/GemmaBilanParserTest.kt` pour couvrir le cas avec et sans marqueur additifs
- [x] T007 [P] Étendre le prompt composition pour le bloc structuré additifs dans `app/src/main/java/com/foodgpt/composition/LiteRtGemmaEngine.kt` selon `specs/003-additive-kpi-results/research.md`
- [x] T008 [P] Aligner le prompt composition côté client local dans `app/src/main/java/com/foodgpt/gemma4local/AndroidGemma4LocalGateway.kt` avec les mêmes exigences de format que T007

**Checkpoint**: `BuildAdditiveKpiDisplay` testable en unitaire avec fixtures texte ; bilan `CompositionBilan` inchangé pour les sorties sans marqueur additifs.

---

## Phase 3: User Story 1 - Classement lisible par niveau de risque (Priority: P1) 🎯 MVP

**Goal**: Pastilles couleur + libellé accessibilité + ordre criticité + état vide explicite sur l’écran bilan.

**Independent Test**: Depuis une réponse LLM (fixtures) avec plusieurs niveaux, vérifier pastilles et ordre rouge → orange → vert ; cas sans additifs → état vide (spec US1).

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Écrire ces tests en premier (ATDD), échec attendu avant implémentation UI branchée.**

- [x] T009 [P] [US1] Test ordre HIGH→MEDIUM→LOW→UNKNOWN et pastille/domain pour chaque ligne dans `app/src/test/java/com/foodgpt/additives/US1AdditiveRankingAndBadgesContractTest.kt` (fixtures alignées scénarios US1.1–US1.2)
- [x] T010 [P] [US1] Test état vide `isEmptyState` et message cohérent dans `app/src/test/java/com/foodgpt/additives/US1AdditiveEmptyStateContractTest.kt` (scénario US1.3)

### Implementation for User Story 1

- [x] T011 [US1] Exposer le résultat `AnalysisDisplayResult` pour `ScanState.BilanReady` via calcul sur `Dispatchers.Default` dans `app/src/main/java/com/foodgpt/camera/CameraViewModel.kt` en appelant `BuildAdditiveKpiDisplay` avec `bilan` et `rawTranscript` de `app/src/main/java/com/foodgpt/camera/ScanState.kt`
- [x] T012 [P] [US1] Créer composants Compose liste + pastille + libellé niveau + `contentDescription` dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveKpiPanel.kt` et `app/src/main/java/com/foodgpt/additives/ui/AdditiveRiskLine.kt` (contrat liste § « Liste » dans `specs/003-additive-kpi-results/contracts/additive-kpi-ui-contract.md`)
- [x] T013 [US1] Intégrer le panneau KPI sous le bilan existant dans la branche `is ScanState.BilanReady` de `app/src/main/java/com/foodgpt/camera/CameraScreen.kt` en consommant l’état exposé par `CameraViewModel`
- [x] T014 [US1] Afficher badges « à confirmer » / « incohérence » (et fusion doublons si exposé) sur les lignes lorsque `confidence` ≠ `OK` dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveRiskLine.kt` (FR-005, contrat erreurs)

**Checkpoint**: US1 vérifiable seule sur device ou preview avec fixtures ViewModel.

---

## Phase 4: User Story 2 - Justification courte compréhensible (Priority: P2)

**Goal**: Justification visible par ligne, tronquée proprement avec consultation du détail sans navigation profonde (FR-006).

**Independent Test**: Ligne rouge/orange/verte affiche justification courte ; expansion inline révèle le texte complet (spec US2).

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T015 [P] [US2] Test troncature + règle `UNKNOWN` pour texte justification dans `app/src/test/java/com/foodgpt/additives/US2JustificationPresentationContractTest.kt` (scénarios US2.1–US2.2)

### Implementation for User Story 2

- [x] T016 [US2] Appliquer troncature (seuil ~120 caractères, ellipse) côté présentation dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveRiskLine.kt` selon `specs/003-additive-kpi-results/research.md`
- [x] T017 [US2] Ajouter interaction d’expansion / repli inline (même ligne, pas nouvelle destination) pour le détail justification dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveRiskLine.kt` (FR-006)

**Checkpoint**: US2 testable indépendamment avec mocks `AnalysisDisplayResult`.

---

## Phase 5: User Story 3 - KPI globaux (Priority: P3)

**Goal**: Bloc synthèse total + compteurs par couleur strictement alignés sur `itemsOrdered` (SC-003) ; niveau global optionnel.

**Independent Test**: Sur fixture ≥5 items, totaux et compteurs égaux aux lignes affichées ; optionnellement pire niveau cohérent (spec US3).

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T018 [P] [US3] Test invariant SC-003 et correspondance compteurs / liste dans `app/src/test/java/com/foodgpt/additives/US3KpiSummaryMatchesListContractTest.kt` (scénarios US3.1–US3.2)

### Implementation for User Story 3

- [x] T019 [US3] Implémenter le bloc synthèse (`totalCount`, `highCount`, `mediumCount`, `lowCount`, `unknownCount`) avec `Modifier.testTag` sur les compteurs dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveKpiSummaryBar.kt` (nouveau fichier) et l’intégrer depuis `app/src/main/java/com/foodgpt/additives/ui/AdditiveKpiPanel.kt`
- [x] T020 [US3] Afficher `globalLevel` lorsque non nul dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveKpiSummaryBar.kt` selon `specs/003-additive-kpi-results/data-model.md`

**Checkpoint**: US3 vérifiable avec les mêmes fixtures que US1 sans régression d’ordre.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Bannières d’erreur parse, tests instrumentés, perf, validation quickstart.

- [x] T021 [P] Afficher une bannière non bloquante si `parseErrors` non vide dans `app/src/main/java/com/foodgpt/additives/ui/AdditiveKpiPanel.kt` (contrat `data-model.md`)
- [x] T022 [P] Test Compose (liste ordonnée + tags KPI) dans `app/src/androidTest/java/com/foodgpt/additives/AdditiveKpiPanelComposeTest.kt` aligné sur `specs/003-additive-kpi-results/quickstart.md`
- [x] T023 [P] (Optionnel) Micro-benchmark ou test de performance parsing sur `Dispatchers.Default` dans `app/src/test/java/com/foodgpt/additives/AdditiveKpiParserPerformanceTest.kt` pour objectif p95 documenté dans `specs/003-additive-kpi-results/plan.md`
- [ ] T024 Valider manuellement et exécuter `./gradlew :app:testDebugUnitTest` puis `./gradlew :app:connectedDebugAndroidTest` comme décrit dans `specs/003-additive-kpi-results/quickstart.md` *(SDK Android requis sur la machine ; non exécuté dans cet environnement)*

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Aucune dépendance externe au feature.
- **Foundational (Phase 2)**: Dépend de Phase 1 — bloque toutes les user stories.
- **User Stories (Phases 3–5)**: Dépendent de Phase 2. US2 et US3 peuvent être développées en parallèle **après** squelette US1 (T011–T013) si l’équipe segmente, mais l’intégration écran partagé impose ordre séquentiel prudent : US1 → US2 → US3 pour éviter conflits sur les mêmes fichiers Compose.
- **Polish (Phase 6)**: Après les user stories ciblées.

### User Story Dependencies

- **US1 (P1)**: Après Phase 2 — base MVP.
- **US2 (P2)**: Après US1 minimal (lignes affichées) ; modifie surtout `AdditiveRiskLine.kt`.
- **US3 (P3)**: Après US1 ; ajoute `AdditiveKpiSummaryBar.kt` et liaisons dans `AdditiveKpiPanel.kt`.

### Within Each User Story

- Tests ATDD ([P]) avant ou en parallèle du code, mais **rouges** avant merge implémentation.
- ViewModel (`CameraViewModel.kt`) avant branchement UI `CameraScreen.kt` pour US1.
- Parser / `BuildAdditiveKpiDisplay` avant tout test domaine qui les importe.

### Parallel Opportunities

- T002 et T003 en séquence stricte (modèle item avant résumé si même PR) ; T007 et T008 en parallèle (fichiers distincts).
- T009 et T010 en parallèle (tests US1).
- T015 et T018 en parallèle une fois Phase 2 verte (tests US2/US3).
- T021, T022, T023 en parallèle en fin de feature (fichiers différents si barres d’erreur et tests séparés).

---

## Parallel Example: User Story 1

```bash
# Lancer en parallèle les tests ATDD US1 :
Task: "US1AdditiveRankingAndBadgesContractTest.kt"
Task: "US1AdditiveEmptyStateContractTest.kt"
```

---

## Parallel Example: User Story 3

```bash
# Test contract KPI en parallèle d’une revue UI US2 si équipe dispo :
Task: "US3KpiSummaryMatchesListContractTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 + Phase 2 (parse + prompts + `GemmaBilanParser`).
2. Phase 3 (US1) avec tests T009–T010 puis impl T011–T014.
3. **STOP** — valider quickstart scénarios 1–3 et état vide.

### Incremental Delivery

1. MVP US1 → release interne.
2. Ajouter US2 (justification + expansion).
3. Ajouter US3 (barre KPI + globalLevel).
4. Phase 6 polish + CI instrumentée.

### Parallel Team Strategy

- Dev A: Phase 2 parser + `BuildAdditiveKpiDisplay` + tests parseur.
- Dev B: Phase 2 prompts (T007–T008) + `GemmaBilanParser`.
- Après merge Phase 2: Dev A US1 UI/VM, Dev B tests US3 + summary bar en parallèle une fois `AdditiveKpiPanel.kt` stabilisé.

---

## Notes

- Les tâches respectent le format `- [x] Tnnn [P] [USn] Description avec chemin fichier`.
- Chemins Android cohérents avec `specs/003-additive-kpi-results/plan.md`.
- Renommer fichiers Compose si l’équipe préfère un seul fichier `AdditiveKpiPanel.kt` : garder alors une seule tâche de fichier mais conserver les responsabilités listées.
