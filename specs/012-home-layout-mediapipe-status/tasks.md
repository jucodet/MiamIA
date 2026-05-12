# Tasks: Disposition accueil MediaPipe

**Input**: Design documents from `/specs/012-home-layout-mediapipe-status/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Dans ce projet, les tests d'acceptation/parcours (ATDD) sont **OBLIGATOIRES**.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Cadrage technique et préparation des validations UI de la feature.

- [X] T001 Aligner le plan de test de la feature dans `specs/012-home-layout-mediapipe-status/quickstart.md`
- [X] T002 Creer le squelette de test instrumente d'accueil dans `app/src/androidTest/java/com/miamia/home/HomeLayoutOrderAcceptanceTest.kt`
- [X] T003 [P] Creer le squelette de test unitaire de mapping d'etat voyant dans `app/src/test/java/com/miamia/home/MediaPipeStatusViewStateMapperTest.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Mettre en place les briques communes bloquantes avant les user stories.

**CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 Creer le modele d'etat de voyant d'accueil dans `app/src/main/java/com/miamia/home/MediaPipeStatusViewState.kt`
- [X] T005 [P] Creer la configuration d'ordre des sections d'accueil dans `app/src/main/java/com/miamia/home/HomeLayoutSections.kt`
- [X] T006 [P] Creer la regle d'espacement standard fixe dans `app/src/main/java/com/miamia/home/HomeSpacingRules.kt`
- [X] T007 Implementer un mapper de priorite inter-specs pour l'accueil dans `app/src/main/java/com/miamia/home/HomeSpecPriorityResolver.kt`
- [X] T008 Integrer ces artefacts dans l'etat ecran d'accueil principal dans `app/src/main/java/com/miamia/MainActivity.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Voir l'etat MediaPipe en tete (Priority: P1) 🎯 MVP

**Goal**: Afficher en premier un voyant MediaPipe a 3 etats avec couleur + libelle.

**Independent Test**: Ouvrir l'accueil et verifier que le premier element visible est le voyant, avec transitions `Verification...` vers `Disponible`/`Indisponible`.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T009 [P] [US1] Ajouter le test instrumente des etats du voyant dans `app/src/androidTest/java/com/miamia/home/HomeMediaPipeIndicatorStateTest.kt`
- [X] T010 [P] [US1] Ajouter le test unitaire de mapping etat->libelle/couleur dans `app/src/test/java/com/miamia/home/MediaPipeStatusViewStateMapperTest.kt`

### Implementation for User Story 1

- [X] T011 [US1] Implementer le composant Compose du voyant en tete avec libelle dans `app/src/main/java/com/miamia/home/MediaPipeStatusIndicator.kt`
- [X] T012 [US1] Brancher la detection MediaPipe a l'etat `CHECKING/AVAILABLE/UNAVAILABLE` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [X] T013 [US1] Integrer le composant voyant en premiere position de l'accueil dans `app/src/main/java/com/miamia/MainActivity.kt`
- [X] T014 [US1] Ajouter la logique de fallback visuel si detection differree dans `app/src/main/java/com/miamia/home/MediaPipeStatusViewState.kt`

**Checkpoint**: User Story 1 doit etre fonctionnelle et testable independamment.

---

## Phase 4: User Story 2 - Conserver un ordre vertical explicite (Priority: P2)

**Goal**: Garantir l'ordre vertical de reference (voyant, bienvenue, vue photo, bouton) en mode portrait.

**Independent Test**: Afficher l'accueil en portrait (avec message long et police grande) et verifier l'ordre strict des 4 blocs.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T015 [P] [US2] Ajouter le test instrumente d'ordre vertical strict de l'accueil dans `app/src/androidTest/java/com/miamia/home/HomeLayoutOrderAcceptanceTest.kt`
- [X] T016 [P] [US2] Ajouter le test instrumente de non-regression message long en bloc 2 dans `app/src/androidTest/java/com/miamia/home/HomeWelcomeBlockOrderTest.kt`

### Implementation for User Story 2

- [X] T017 [US2] Implementer l'orchestrateur d'ordre de sections en portrait dans `app/src/main/java/com/miamia/home/HomeLayoutOrderBuilder.kt`
- [X] T018 [US2] Recomposer l'ecran d'accueil avec l'ordre de reference 012 dans `app/src/main/java/com/miamia/MainActivity.kt`
- [X] T019 [US2] Forcer le scope portrait pour ce layout d'accueil dans `app/src/main/java/com/miamia/home/HomeLayoutSections.kt`
- [X] T020 [US2] Ajouter la regle de precedence documentaire 012 sur l'ordre UI dans `app/src/main/java/com/miamia/home/HomeSpecPriorityResolver.kt`

**Checkpoint**: User Stories 1 et 2 doivent fonctionner independamment.

---

## Phase 5: User Story 3 - Lancer rapidement la prise de photo (Priority: P3)

**Goal**: Positionner le bouton capture juste sous l'encart photo avec espacement standard fixe, sans changer la logique metier capture.

**Independent Test**: Verifier que le bouton est immediatement sous la vue photo avec espacement constant, et que l'action capture reste fonctionnelle.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T021 [P] [US3] Ajouter le test instrumente de position bouton sous encart photo dans `app/src/androidTest/java/com/miamia/home/HomeCaptureButtonPlacementTest.kt`
- [X] T022 [P] [US3] Ajouter le test instrumente de constance d'espacement en portrait dans `app/src/androidTest/java/com/miamia/home/HomeCaptureSpacingConsistencyTest.kt`

### Implementation for User Story 3

- [X] T023 [US3] Implementer le conteneur vertical vue photo + bouton avec espacement standard fixe dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T024 [US3] Verifier et adapter le wiring du bouton vers l'action capture existante dans `app/src/main/java/com/miamia/camera/CaptureAction.kt`
- [X] T025 [US3] Ajouter la validation runtime de structure stable si preview indisponible dans `app/src/main/java/com/miamia/home/HomeLayoutOrderBuilder.kt`

**Checkpoint**: Toutes les user stories doivent etre independamment fonctionnelles.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finaliser, verifier et documenter les impacts transverses.

- [X] T026 [P] Mettre a jour la documentation de contrat d'ecran d'accueil dans `specs/012-home-layout-mediapipe-status/contracts/home-layout-order-contract.md`
- [X] T027 Executer et consigner la validation quickstart dans `specs/012-home-layout-mediapipe-status/quickstart.md`
- [X] T028 [P] Ajouter un test de non-regression inter-specs 007/010/012 dans `app/src/androidTest/java/com/miamia/home/HomeInterSpecConsistencyTest.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: demarrage immediat
- **Phase 2 (Foundational)**: depend de Phase 1 et bloque toutes les user stories
- **Phase 3 (US1)**: depend de Phase 2
- **Phase 4 (US2)**: depend de Phase 2 (et de US1 pour reutiliser le composant voyant)
- **Phase 5 (US3)**: depend de Phase 2 (et de US2 pour ordre/layout final)
- **Phase 6 (Polish)**: depend des US finalisees

### User Story Dependencies

- **US1 (P1)**: aucune dependance metier sur autres stories, commence apres fondations
- **US2 (P2)**: independante fonctionnellement, mais s'appuie sur le voyant de US1
- **US3 (P3)**: independante sur le comportement capture, depend du layout final US2

### Within Each User Story

- Ecrire tests d'acceptation d'abord et les faire echouer
- Implementer composants/modeles ensuite
- Integrer dans l'ecran d'accueil ensuite
- Valider la story independamment avant suite

### Parallel Opportunities

- Setup: T003 en parallele de T001/T002
- Foundational: T005 et T006 en parallele, puis T007/T008
- US1: T009 et T010 en parallele
- US2: T015 et T016 en parallele
- US3: T021 et T022 en parallele
- Polish: T026 et T028 en parallele

---

## Parallel Example: User Story 2

```bash
# Lancer les tests US2 en parallele:
Task: "T015 [US2] test d'ordre vertical dans app/src/androidTest/java/com/miamia/home/HomeLayoutOrderAcceptanceTest.kt"
Task: "T016 [US2] test message long dans app/src/androidTest/java/com/miamia/home/HomeWelcomeBlockOrderTest.kt"

# Puis lancer les implems US2 decouplees:
Task: "T017 [US2] orchestrateur d'ordre dans app/src/main/java/com/miamia/home/HomeLayoutOrderBuilder.kt"
Task: "T020 [US2] resolver priorite dans app/src/main/java/com/miamia/home/HomeSpecPriorityResolver.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completer Phase 1
2. Completer Phase 2
3. Completer US1 (Phase 3)
4. Stop et valider uniquement US1 (voyant en tete + etats)

### Incremental Delivery

1. Livrer US1 (etat MediaPipe en tete)
2. Ajouter US2 (ordre vertical explicite complet)
3. Ajouter US3 (bouton capture sous encart avec espacement fixe)
4. Finaliser avec Phase 6 (non-regressions et quickstart)

### Suggested MVP Scope

- Phase 1 + Phase 2 + Phase 3 (US1)

