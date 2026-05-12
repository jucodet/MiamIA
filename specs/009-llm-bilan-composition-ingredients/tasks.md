# Tasks: Bilan ingrédients et composition (Gemma local)

**Input**: Design documents depuis `specs/009-llm-bilan-composition-ingredients/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Les tests d’acceptation / parcours (ATDD) sont **obligatoires** pour ce projet — au minimum **une** tâche de test par user story, alignée sur les scénarios Given/When/Then du `spec.md`.

**Organization**: Phases par user story (P1 → P3) après fondations partagées.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Peut s’exécuter en parallèle (fichiers distincts, pas de dépendance sur une tâche incomplète du même lot)
- **[Story]**: US1, US2, US3 — obligatoire sur les phases user story
- Chemins **réels** du dépôt Android `app/`

---

## Phase 1: Setup (infrastructure partagée)

**Purpose**: Conventions modèle Gemma + socle package `composition`.

- [x] T001 Créer `app/src/main/java/com/miamia/composition/GemmaModelPaths.kt` avec constantes (nom asset / chemin canonique du fichier modèle Gemma attendu sur l’appareil)
- [x] T002 [P] Ajouter `app/src/main/assets/gemma/README.md` expliquant où placer le fichier modèle pour builds locaux et tests manuels (`quickstart.md`)
- [x] T003 [P] Vérifier / annoter la dépendance LiteRT `com.google.ai.edge.litert:litert` dans `app/build.gradle.kts` pour la feature 009 (commentaire ou version alignée plan)

---

## Phase 2: Foundational (prérequis bloquants)

**Purpose**: Modèles Kotlin, localisateur Gemma, moteur d’analyse injectable, extension des états UI — **aucune** user story ne démarre avant cette phase.

**⚠️ CRITICAL**: Les phases US1–US3 dépendent de la complétion de cette phase.

- [x] T004 Créer `app/src/main/java/com/miamia/composition/CompositionModels.kt` (`CompositionBilan`, codes erreur `gemma_not_found` / `gemma_load_failed`, résultats `AnalyzeCompositionResult` alignés `data-model.md`)
- [x] T005 Implémenter `app/src/main/java/com/miamia/composition/GemmaModelLocator.kt` (résolution chemin depuis `Context` + statut `ready` / `not_found` / `load_failed`)
- [x] T006 Créer `app/src/main/java/com/miamia/composition/CompositionAnalysisEngine.kt` (interface `suspend fun analyze(rawText: String, …): AnalyzeCompositionResult`) et squelette `app/src/main/java/com/miamia/composition/LiteRtGemmaEngine.kt`
- [x] T007 Étendre `app/src/main/java/com/miamia/camera/ScanState.kt` avec états flux bilan (ex. `CompositionAnalyzing`, `BilanReady`, `GemmaUnavailable`, `CompositionLimit`) sans supprimer les états caméra/OCR existants
- [x] T008 Mettre à jour les branches `when` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` pour couvrir exhaustivement les nouveaux `ScanState` (placeholders UX acceptables jusqu’à US1)
- [x] T009 Vérifier / adapter `app/src/main/java/com/miamia/MainActivity.kt` si la factory `CameraViewModel` doit recevoir `CompositionAnalysisEngine` + `GemmaModelLocator` (signatures constructeur)

**Checkpoint**: compilation du module `app` avec nouveaux types et états (même UI incomplète).

---

## Phase 3: User Story 1 — Voir le bilan à la place du texte brut (Priority: P1) 🎯 MVP

**Goal**: Après texte OCR exploitable, inférence Gemma locale produit liste + analyse ; écran affiche le **bilan** comme contenu principal ; texte brut secondaire (repli / action).

**Independent Test**: Texte capturé représentatif → écran avec sections liste + analyse + disclaimer ; transcript non dominant (spec US1, SC-002).

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE**: Écrire ces tests en premier ; ils **échouent** jusqu’à l’implémentation (ATDD).

- [x] T010 [P] [US1] Tests unitaires `GemmaModelLocator` (fichier absent → `not_found`, fichier présent → chemin résolu) dans `app/src/test/java/com/miamia/composition/GemmaModelLocatorTest.kt`
- [x] T011 [P] [US1] Tests unitaires parseur / mapping sortie LLM vers `CompositionBilan` (sections liste + analyse) dans `app/src/test/java/com/miamia/composition/GemmaBilanParserTest.kt`
- [x] T012 [P] [US1] Tests de mapping logique vers contrat `bilan_ready` dans `app/src/test/java/com/miamia/composition/GemmaCompositionContractMappingTest.kt` (aligné `specs/009-llm-bilan-composition-ingredients/contracts/gemma-composition-bilan-contract.md`)

### Implementation for User Story 1

- [x] T013 [P] [US1] Implémenter `app/src/main/java/com/miamia/composition/GemmaBilanParser.kt` (extraction `ingredientLines` + `compositionAnalysis` depuis sortie modèle versionnée)
- [x] T014 [US1] Compléter `app/src/main/java/com/miamia/composition/LiteRtGemmaEngine.kt` (prompt système + utilisateur, appel LiteRT, parsing via `GemmaBilanParser`, timeout paramétrable)
- [x] T015 [US1] Orchestrer dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` : après succès OCR → `CompositionAnalyzing` → `BilanReady` ou `GemmaUnavailable` / `CompositionLimit` (aucun envoi réseau)
- [x] T016 [US1] Remplir UI `BilanReady` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : sections **liste** + **analyse** + **disclaimer** FR-010 ; texte brut accessible en mode secondaire (repli / bouton)
- [x] T017 [US1] Brancher injections dans `app/src/main/java/com/miamia/MainActivity.kt` : instancier `GemmaModelLocator`, `LiteRtGemmaEngine` (ou impl par défaut), passer à `CameraViewModel.factory(...)`

**Checkpoint**: US1 démontrable seul avec modèle Gemma présent sur device / émulateur.

---

## Phase 4: User Story 2 — Transparence et limites (Priority: P2)

**Goal**: Texte non fiable → message limite ; pas de liste fictive ; formulations prudentes ; disclaimer visible (spec US2, FR-006 / FR-007).

**Independent Test**: Entrée OCR ambiguë → `CompositionLimit` + message clair ; pas d’ingrédients inventés.

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T018 [P] [US2] Tests unitaires texte ambigu / vide structuré → `CompositionLimit` sans `BilanReady` dans `app/src/test/java/com/miamia/composition/CompositionLimitTest.kt`

### Implementation for User Story 2

- [x] T019 [US2] Ajouter validation post-inférence (rejet bilan vide « complet », heuristiques prudence) dans `app/src/main/java/com/miamia/composition/CompositionResultValidator.kt` ou extension de `LiteRtGemmaEngine.kt`
- [x] T020 [US2] Afficher `CompositionLimit` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` avec copie utilisateur conforme FR-007 (pas de liste plausible fictive)

**Checkpoint**: US1 + US2 testables indépendamment (US2 vérifiable avec mocks moteur).

---

## Phase 5: User Story 3 — Attente et récupération (Priority: P3)

**Goal**: Indicateur pendant Gemma ; erreur explicite si modèle absent ; réessai / repli texte brut ; pas de faux bilan (spec US3, FR-008 / FR-009 / FR-012, directive « Gemma introuvable »).

**Independent Test**: Simuler `not_found` / timeout → message + réessai ; jamais `BilanReady` sans succès local.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T021 [P] [US3] Tests unitaires `CameraViewModel` : moteur faux qui renvoie `gemma_not_found` ou timeout → état `GemmaUnavailable`, pas `BilanReady` dans `app/src/test/java/com/miamia/camera/CameraViewModelGemmaErrorTest.kt` (nécessite injectable `CompositionAnalysisEngine` dans `CameraViewModel` ou test double du coordinateur)

### Implementation for User Story 3

- [x] T022 [US3] Finaliser copies erreur « Gemma introuvable » / charge / timeout dans `app/src/main/java/com/miamia/composition/CompositionMessages.kt` (nouveau) ou extension `app/src/main/java/com/miamia/ingredients/ScanFailureMessageBuilder.kt`
- [x] T023 [US3] Boutons **Réessayer** et **Voir le texte brut** (repli) pour `GemmaUnavailable` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [x] T024 [US3] Appliquer `withTimeout` / annulation cohérente dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` pour respect SC-003 (délai max analyse composition)

**Checkpoint**: Parcours erreur Gemma conforme `SC-006` et contrat `gemma_error`.

---

## Phase 6: Polish & cross-cutting

**Purpose**: Finitions transverses.

- [x] T025 [P] Passer revue performances (chargement lazy modèle, libération ressources) dans `app/src/main/java/com/miamia/composition/LiteRtGemmaEngine.kt` et `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (aligné `research.md`)
- [x] T026 Exécuter validations manuelles `specs/009-llm-bilan-composition-ingredients/quickstart.md` et noter écarts dans `specs/009-llm-bilan-composition-ingredients/quickstart.md` (section résultat)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → aucune dépendance externe.
- **Phase 2** → dépend de Phase 1 ; **bloque** toutes les user stories.
- **Phases 3–5** → dépendent de Phase 2 ; US2 / US3 peuvent démarrer après T009 si les composants de base existent (itération recommandée : **US1 d’abord**).
- **Phase 6** → après les stories souhaitées pour release.

### User Story Dependencies

- **US1 (P1)**: Dépend de Phase 2 ; aucune dépendance sur US2/US3 pour le cœur « bilan affiché ».
- **US2 (P2)**: Renforce validateurs / UI limite ; idéalement après squelette `LiteRtGemmaEngine` (T014).
- **US3 (P3)**: Dépend de l’injectabilité testable du moteur (T017 / refactors `CameraViewModel`) pour T021.

### Within Each User Story

- Tests (T010–T012, T018, T021) **avant** ou en lockstep avec implémentation — **échec rouge** attendu avant vert (ATDD).
- Modèles / locator avant moteur ; moteur avant orchestration ViewModel ; ViewModel avant finitions UI erreur.

### Parallel Opportunities

- **Phase 1**: T002, T003 en parallèle après T001.
- **US1 tests**: T010, T011, T012 en parallèle (fichiers de test distincts).
- **US1 impl**: T013 en parallèle de préparations si T014 n’a pas encore besoin du parseur fini — sinon T013 puis T014.
- **US2 / US3 tests**: T018 et T021 en parallèle si équipes séparées (après fondations).

---

## Parallel Example: User Story 1

```bash
# Lancer en parallèle les tests ATDD US1 (après Phase 2) :
# - app/src/test/java/com/miamia/composition/GemmaModelLocatorTest.kt
# - app/src/test/java/com/miamia/composition/GemmaBilanParserTest.kt
# - app/src/test/java/com/miamia/composition/GemmaCompositionContractMappingTest.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 uniquement)

1. Phases 1–2 (structure + états + squelettes).
2. Phase 3 (US1) : tests T010–T012 puis impl T013–T017.
3. **STOP** — valider manuellement `quickstart.md` scénario principal avec modèle Gemma présent.

### Livraison incrémentale

1. Ajouter Phase 4 (US2) — limites / `CompositionLimit`.
2. Ajouter Phase 5 (US3) — erreurs Gemma, timeout, repli.
3. Phase 6 — polish perf + doc.

### Stratégie équipe parallèle

Après Phase 2 : développeur A sur US1 (T010–T017), développeur B sur tests US2 (T018) + validateur (T019), développeur C sur injectabilité + tests US3 (T021–T024).

---

## Notes

- Total tâches : **26** (T001–T026).
- Par story : **US1** — 3 tests + 5 impl = 8 tâches (T010–T017) ; **US2** — 1 test + 2 impl = 3 (T018–T020) ; **US3** — 1 test + 3 impl = 4 (T021–T024) ; setup 3 ; foundational 6 ; polish 2.
- Les tâches `[P]` supposent **aucune** édition concurrente sur le **même** fichier ; ajuster si conflit Git sur `CameraScreen.kt` / `CameraViewModel.kt`.
- Pas de bilan simulé si Gemma absent (FR-012) — vérifier à la revue sur chaque PR touchant `LiteRtGemmaEngine.kt` / `CameraViewModel.kt`.
