# Tasks: Identification des ingrédients par photo

**Input**: Design documents from `/specs/006-identify-photo-ingredients/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Les tests d'acceptation/parcours (ATDD) sont obligatoires pour chaque user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Chaque tâche inclut un chemin de fichier explicite

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialisation Android/Kotlin et structure de base

- [X] T001 Configurer les dépendances minimales (CameraX, Compose, Coroutines, ML Kit offline) dans `app/build.gradle.kts`
- [X] T002 Créer la structure de packages feature selon le plan dans `app/src/main/java/com/miamia/`
- [X] T003 [P] Configurer permissions caméra et règles manifest dans `app/src/main/AndroidManifest.xml`
- [X] T004 [P] Ajouter constantes feature et seuil OCR 0.70 dans `app/src/main/java/com/miamia/core/FeatureConfig.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fondations partagées bloquantes avant les user stories

- [X] T005 Implémenter la détection de capacité AI Edge device dans `app/src/main/java/com/miamia/recognition/DeviceAiCapabilityDetector.kt`
- [X] T006 [P] Définir interfaces de reconnaissance et résultats contractuels dans `app/src/main/java/com/miamia/recognition/RecognitionContracts.kt`
- [X] T007 Implémenter le fallback OCR local offline ML Kit dans `app/src/main/java/com/miamia/recognition/LocalOcrFallbackRecognizer.kt`
- [X] T008 Implémenter le wrapper AI Edge Gallery (si disponible) dans `app/src/main/java/com/miamia/recognition/AiEdgeGalleryRecognizer.kt`
- [X] T009 Implémenter le sélecteur de moteur priorisant AI Edge puis fallback dans `app/src/main/java/com/miamia/recognition/RecognitionEngineSelector.kt`
- [X] T010 [P] Créer entités Room (`ScanAttemptEntity`, `ValidatedIngredientEntity`) dans `app/src/main/java/com/miamia/data/db/ScanEntities.kt`
- [X] T011 [P] Créer DAO et base locale de session scan dans `app/src/main/java/com/miamia/data/db/AppDatabase.kt`
- [X] T012 Implémenter le manager de photo temporaire avec suppression automatique dans `app/src/main/java/com/miamia/camera/TemporaryImageManager.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Extraire les ingrédients d'une photo (Priority: P1) 🎯 MVP

**Goal**: Extraire et afficher une liste d'ingrédients lisible depuis une photo nette, en local.

**Independent Test**: Soumettre une photo lisible et vérifier une liste ordonnée d'ingrédients, sans appel réseau.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T013 [P] [US1] Écrire test d'acceptation capture->extraction->liste dans `app/src/androidTest/java/com/miamia/ingredients/US1ExtractIngredientsFlowTest.kt`
- [X] T014 [P] [US1] Écrire test de contrat `StartIngredientRecognitionCommand` et `IngredientRecognitionResult` dans `app/src/test/java/com/miamia/recognition/IngredientRecognitionContractTest.kt`
- [X] T015 [P] [US1] Écrire test offline garantissant zéro appel réseau dans `app/src/test/java/com/miamia/recognition/OfflineOnlyRecognitionTest.kt`

### Implementation for User Story 1

- [X] T016 [P] [US1] Implémenter le pipeline parsing ingrédients (ordre, séparateurs, allergènes) dans `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt`
- [X] T017 [US1] Implémenter l'orchestrateur capture->OCR->extraction dans `app/src/main/java/com/miamia/recognition/IngredientRecognitionCoordinator.kt`
- [X] T018 [US1] Implémenter état UI de scan (idle/processing/success/failure) dans `app/src/main/java/com/miamia/ingredients/IngredientListState.kt`
- [X] T019 [US1] Implémenter écran caméra responsive et déclenchement scan dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T020 [US1] Implémenter ViewModel de scan et exposition liste extraite dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T021 [US1] Implémenter mapping résultat OCR -> `ExtractedIngredientItem` dans `app/src/main/java/com/miamia/ingredients/ExtractedIngredientMapper.kt`

**Checkpoint**: User Story 1 fonctionne de manière autonome et testable

---

## Phase 4: User Story 2 - Corriger avant validation (Priority: P2)

**Goal**: Permettre la correction manuelle puis validation fidèle de la liste finale.

**Independent Test**: Modifier un ingrédient mal reconnu et vérifier que la liste validée correspond exactement.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T022 [P] [US2] Écrire test d'acceptation édition + validation utilisateur dans `app/src/androidTest/java/com/miamia/ingredients/US2EditAndValidateFlowTest.kt`
- [X] T023 [P] [US2] Écrire test de contrat `ValidateIngredientListCommand`/`ValidateIngredientListResult` dans `app/src/test/java/com/miamia/ingredients/IngredientValidationContractTest.kt`

### Implementation for User Story 2

- [X] T024 [P] [US2] Implémenter écran d'édition de la liste extraite dans `app/src/main/java/com/miamia/ingredients/IngredientEditorScreen.kt`
- [X] T025 [US2] Implémenter règles de validation des entrées utilisateur dans `app/src/main/java/com/miamia/ingredients/IngredientValidationUseCase.kt`
- [X] T026 [US2] Implémenter persistance de `ValidatedIngredientList` dans `app/src/main/java/com/miamia/data/repository/ValidatedIngredientRepository.kt`
- [X] T027 [US2] Implémenter auto-validation conditionnelle (OCR >= 0.70) dans `app/src/main/java/com/miamia/ingredients/AutoValidationPolicy.kt`
- [X] T028 [US2] Connecter édition/validation au ViewModel dans `app/src/main/java/com/miamia/ingredients/IngredientEditorViewModel.kt`

**Checkpoint**: User Stories 1 et 2 sont indépendamment fonctionnelles

---

## Phase 5: User Story 3 - Gérer les échecs de scan (Priority: P3)

**Goal**: Afficher un échec clair et autoriser une relance manuelle simple.

**Independent Test**: Soumettre une photo floue et vérifier message explicite + option de relance manuelle.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T029 [P] [US3] Écrire test d'acceptation scénario photo floue + relance manuelle dans `app/src/androidTest/java/com/miamia/ingredients/US3FailureAndRetryFlowTest.kt`
- [X] T030 [P] [US3] Écrire test garantissant absence de retry automatique dans `app/src/test/java/com/miamia/recognition/NoAutoRetryPolicyTest.kt`

### Implementation for User Story 3

- [X] T031 [P] [US3] Implémenter classification des échecs (blur, low-contrast, incomplet) dans `app/src/main/java/com/miamia/recognition/ScanFailureClassifier.kt`
- [X] T032 [US3] Implémenter génération des messages utilisateur d'échec explicites dans `app/src/main/java/com/miamia/ingredients/ScanFailureMessageBuilder.kt`
- [X] T033 [US3] Implémenter action de relance manuelle dans `app/src/main/java/com/miamia/ingredients/RetryScanActionHandler.kt`
- [X] T034 [US3] Intégrer l'affichage des erreurs dans l'écran de scan dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: Toutes les user stories sont indépendamment testables et complètes

---

## Final Phase: Polish & Cross-Cutting Concerns

**Purpose**: Finitions transverses, performance, conformité et documentation

- [X] T035 [P] Documenter setup/exécution/ATDD de la feature dans `specs/006-identify-photo-ingredients/quickstart.md`
- [X] T036 Vérifier et optimiser latence p95 extraction (<8s) dans `app/src/main/java/com/miamia/recognition/IngredientRecognitionCoordinator.kt`
- [X] T037 [P] Vérifier suppression systématique de la photo temporaire dans `app/src/test/java/com/miamia/camera/TemporaryImageManagerTest.kt`
- [X] T038 [P] Vérifier responsive UI (petits/grands écrans) dans `app/src/androidTest/java/com/miamia/ingredients/ResponsiveLayoutTest.kt`
- [X] T039 Vérifier dépendances minimales et supprimer libs inutilisées dans `app/build.gradle.kts`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: démarre immédiatement
- **Phase 2 (Foundational)**: dépend de Phase 1 et bloque toutes les user stories
- **Phase 3 (US1)**: dépend de Phase 2
- **Phase 4 (US2)**: dépend de Phase 2 (et réutilise la sortie US1 pour test de bout en bout)
- **Phase 5 (US3)**: dépend de Phase 2
- **Final Phase**: dépend des user stories livrées

### User Story Dependencies

- **US1 (P1)**: indépendante après fondations
- **US2 (P2)**: indépendante après fondations, s'appuie sur le flux d'extraction existant
- **US3 (P3)**: indépendante après fondations, partage l'écran de scan

### Within Each User Story

- Tests ATDD d'abord (doivent échouer avant implémentation)
- Contrats/modèles avant orchestration
- Orchestration avant UI finale
- Story validée indépendamment avant passage à la suivante

### Parallel Opportunities

- Setup: T003, T004 en parallèle
- Fondations: T006, T010, T011 en parallèle
- US1: T013, T014, T015, T016 en parallèle
- US2: T022, T023, T024 en parallèle
- US3: T029, T030, T031 en parallèle
- Polish: T035, T037, T038 en parallèle

---

## Parallel Example: User Story 1

```bash
Task: "Écrire test d'acceptation capture->extraction->liste dans app/src/androidTest/java/com/miamia/ingredients/US1ExtractIngredientsFlowTest.kt"
Task: "Écrire test de contrat StartIngredientRecognitionCommand et IngredientRecognitionResult dans app/src/test/java/com/miamia/recognition/IngredientRecognitionContractTest.kt"
Task: "Implémenter le pipeline parsing ingrédients dans app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Terminer Phase 1 puis Phase 2
2. Livrer US1 complète (tests + implémentation)
3. Vérifier performance/offline/suppression image
4. Démontrer le MVP

### Incremental Delivery

1. Setup + fondations
2. Ajouter US1 puis valider indépendamment
3. Ajouter US2 puis valider indépendamment
4. Ajouter US3 puis valider indépendamment
5. Finaliser polish transverse

### Parallel Team Strategy

1. Toute l'équipe termine Setup + Foundational
2. Puis distribution:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Intégration finale et vérification des critères SC-001 à SC-004
