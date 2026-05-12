# Tasks: Message de bienvenue souriant

**Input**: Design documents from `/specs/010-message-bienvenue-sourire/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Les tests d'acceptation/parcours (ATDD) sont obligatoires et chaque user story inclut au minimum un test qui echoue avant implementation.

**Organization**: Taches groupees par user story pour permettre une implementation et une validation independantes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: peut etre execute en parallele (fichiers differents, pas de dependance directe)
- **[Story]**: etiquette de user story (`[US1]`, `[US2]`, `[US3]`)
- Chaque tache inclut un chemin de fichier exact

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: cadrer les points d'integration et la base de contenus pour les messages de bienvenue.

- [X] T001 Documenter le point de declenchement apres connexion dans `specs/010-message-bienvenue-sourire/contracts/welcome-message-display-contract.md`
- [X] T002 [P] Ajouter le package `welcome` et les stubs `WelcomeMessageProvider.kt`, `WelcomeMessageSelector.kt`, `WelcomeMessagePolicy.kt`, `WelcomeMessageUiState.kt` dans `app/src/main/java/com/miamia/welcome/`
- [X] T003 [P] Ajouter une bibliotheque initiale de messages valides en francais dans `app/src/main/assets/welcome/messages_fr.json`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: etablir les contrats et points partages obligatoires avant toute user story.

- [X] T004 Creer les modeles de domaine `WelcomeMessage`, `WelcomeCatalog`, `WelcomeDisplayEvent` dans `app/src/main/java/com/miamia/welcome/WelcomeModels.kt`
- [X] T005 Implementer le chargement + filtrage langue/actif dans `app/src/main/java/com/miamia/welcome/WelcomeMessageProvider.kt`
- [X] T006 [P] Implementer la politique de selection aleatoire avec repetition autorisee dans `app/src/main/java/com/miamia/welcome/WelcomeMessageSelector.kt`
- [X] T007 [P] Ecrire les tests unitaires du provider et du selector dans `app/src/test/java/com/miamia/welcome/WelcomeMessageProviderTest.kt` et `app/src/test/java/com/miamia/welcome/WelcomeMessageSelectorTest.kt`

**Checkpoint**: la fondation est prete, les user stories peuvent commencer.

---

## Phase 3: User Story 1 - Recevoir un message qui change (Priority: P1) 🎯 MVP

**Goal**: afficher un message apres chaque connexion reussie avec variation aleatoire possible.

**Independent Test**: effectuer plusieurs connexions consecutives et verifier qu'un message valide apparait a chaque fois, meme si repetition autorisee.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T008 [P] [US1] Ajouter le test d'acceptation du flux connexion -> accueil -> message dans `app/src/androidTest/java/com/miamia/welcome/US1WelcomeAfterLoginFlowTest.kt`
- [X] T009 [P] [US1] Ajouter le test contrat `LoginSucceeded` -> `DisplaySuccess`/`DisplaySkipped` dans `app/src/test/java/com/miamia/welcome/US1WelcomeDisplayContractTest.kt`

### Implementation for User Story 1

- [X] T010 [US1] Implementer l'orchestrateur `WelcomeMessagePolicy` (selection sur login success) dans `app/src/main/java/com/miamia/welcome/WelcomeMessagePolicy.kt`
- [X] T011 [US1] Integrer l'appel a la policy dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` au moment de l'etat post-connexion reussie
- [X] T012 [US1] Exposer l'etat d'affichage de bienvenue via `WelcomeMessageUiState` dans `app/src/main/java/com/miamia/welcome/WelcomeMessageUiState.kt`
- [X] T013 [US1] Afficher le message dans l'ecran d'accueil en mettant a jour `app/src/main/java/com/miamia/camera/CameraScreen.kt`

**Checkpoint**: US1 est demonstrable seule (message visible apres connexion reussie).

---

## Phase 4: User Story 2 - Obtenir un message positif (Priority: P2)

**Goal**: garantir que seuls des messages positifs et chaleureux sont affiches.

**Independent Test**: verifier qu'un echantillon de messages affiches respecte le ton attendu et qu'aucun message non valide n'est diffuse.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T014 [P] [US2] Ajouter un test d'acceptation sur le ton des messages affiches dans `app/src/androidTest/java/com/miamia/welcome/US2PositiveToneWelcomeTest.kt`
- [X] T015 [P] [US2] Ajouter un test de contrat de validation de bibliotheque pre-validee dans `app/src/test/java/com/miamia/welcome/US2WelcomeCatalogValidationContractTest.kt`

### Implementation for User Story 2

- [X] T016 [US2] Ajouter la validation de ton/format du catalogue dans `app/src/main/java/com/miamia/welcome/WelcomeMessageProvider.kt`
- [X] T017 [US2] Creer le set de regles de ton et de filtrage dans `app/src/main/java/com/miamia/welcome/WelcomeToneRules.kt`
- [X] T018 [US2] Mettre a jour la bibliotheque de contenus valides dans `app/src/main/assets/welcome/messages_fr.json`

**Checkpoint**: US2 est testable independamment (qualite du ton garantie).

---

## Phase 5: User Story 3 - Continuer meme en cas de limites (Priority: P3)

**Goal**: maintenir un comportement stable quand la bibliotheque est reduite ou vide (sans fallback).

**Independent Test**: valider qu'avec catalogue vide, aucun message n'est affiche et aucune erreur bloquante n'apparait.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T019 [P] [US3] Ajouter le test d'acceptation du cas catalogue vide dans `app/src/androidTest/java/com/miamia/welcome/US3EmptyCatalogNoMessageTest.kt`
- [X] T020 [P] [US3] Ajouter le test de non-regression "pas d'erreur bloquante sans message" dans `app/src/test/java/com/miamia/welcome/US3NoCrashWhenCatalogEmptyTest.kt`

### Implementation for User Story 3

- [X] T021 [US3] Implementer l'etat `NOT_DISPLAYED_EMPTY_CATALOG` dans `app/src/main/java/com/miamia/welcome/WelcomeMessagePolicy.kt`
- [X] T022 [US3] Adapter le rendu UI sans message/fallback dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T023 [US3] Logger l'evenement d'affichage saute dans `app/src/main/java/com/miamia/welcome/WelcomeDisplayLogger.kt`

**Checkpoint**: US3 est autonome et robuste sur les cas limites.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: finaliser la qualite globale, la doc et la verification de bout en bout.

- [X] T024 [P] Ajouter la documentation d'exploitation/edition des messages dans `specs/010-message-bienvenue-sourire/quickstart.md`
- [ ] T025 Executer et consigner la verification complete quickstart dans `specs/010-message-bienvenue-sourire/research.md`
- [X] T026 [P] Nettoyer les noms/terminologie welcome sur l'ensemble des fichiers dans `app/src/main/java/com/miamia/welcome/`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): demarre immediatement.
- Phase 2 (Foundational): depend de Phase 1, bloque toutes les user stories.
- Phases US1/US2/US3: demarrent apres Phase 2; execution possible en parallele si equipe suffisante.
- Phase 6 (Polish): depend de la completion des stories visees.

### User Story Dependencies

- **US1 (P1)**: demarre apres Foundational, sans dependance US2/US3.
- **US2 (P2)**: demarre apres Foundational, s'appuie sur provider/selecteur deja en place.
- **US3 (P3)**: demarre apres Foundational, complete les comportements de bord sans casser US1/US2.

### Within Each User Story

- Tests d'acceptation/contrat d'abord (doivent echouer avant implementation).
- Logique metier ensuite (`welcome` package).
- Integration ViewModel/UI ensuite.
- Validation finale de la story avant passage a la suivante.

### Parallel Opportunities

- `T002` et `T003` en parallele.
- `T006` et `T007` en parallele apres `T004`/`T005`.
- Dans chaque story, les taches de tests marquees `[P]` peuvent tourner ensemble.
- `US2` et `US3` peuvent etre travaillees en parallele une fois `US1` stabilisee si capacite equipe.

---

## Parallel Example: User Story 1

```bash
# Lancer les tests US1 en parallele:
Task: "T008 app/src/androidTest/java/com/miamia/welcome/US1WelcomeAfterLoginFlowTest.kt"
Task: "T009 app/src/test/java/com/miamia/welcome/US1WelcomeDisplayContractTest.kt"

# Puis avancer implementation metier/UI:
Task: "T010 app/src/main/java/com/miamia/welcome/WelcomeMessagePolicy.kt"
Task: "T012 app/src/main/java/com/miamia/welcome/WelcomeMessageUiState.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completer Phase 1 + Phase 2.
2. Completer US1 (Phase 3).
3. Stopper et valider US1 seule avant extension.

### Incremental Delivery

1. Livrer US1 (valeur immediate: message apres connexion).
2. Ajouter US2 (qualite du ton).
3. Ajouter US3 (robustesse catalogue vide).
4. Finaliser avec Polish.

### Parallel Team Strategy

1. Equipe complete Setup + Foundational.
2. Ensuite:
   - Dev A: US1 integration flux connexion/UI.
   - Dev B: US2 validation contenu/ton.
   - Dev C: US3 cas limites et stabilite.

---

## Notes

- Tous les items respectent le format checklist requis (`- [ ] Txxx ...`).
- Les taches `[USx]` sont limitees aux phases de user stories.
- Chaque user story est independamment testable et livrable.
