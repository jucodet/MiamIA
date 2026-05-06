# Tasks: Homepage LLM Mock Trigger

**Input**: Design documents from `/specs/domains/user-guidance-experience/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Les tests ATDD/parcours sont obligatoires pour chaque user story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Poser la structure minimale des tests et du flux homepage pour ce scope.

- [X] T001 Verifier et aligner la structure des packages home dans `app/src/main/java/com/foodgpt/home/`
- [X] T002 [P] Creer le squelette de test unitaire dans `app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt`
- [X] T003 [P] Creer le squelette de test UI dans `app/src/androidTest/java/com/foodgpt/home/HomeScreenLlmMockUiTest.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Mettre en place les bases communes qui bloquent toutes les user stories.

- [X] T004 Definir le modele d'etat `HomepageTestRunState` dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T005 [P] Definir le contrat de declenchement/rendu homepage dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [X] T006 Implementer le garde-fou anti-concurrence (une execution `running`) dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T007 Connecter l'appel au service d'analyse existant via adaptateur de domaine dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T008 Implementer la classification d'erreurs (`timeout`, `runtime-unavailable`, `non-analysable-response`) dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`

**Checkpoint**: Fondations terminees, les user stories peuvent commencer.

---

## Phase 3: User Story 1 - Lancer le test depuis la homepage (Priority: P1) 🎯 MVP

**Goal**: Permettre le clic sur bouton homepage qui declenche le test bouchonne LLM.

**Independent Test**: Depuis la homepage, un clic demarre une execution unique sans camera/OCR.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T009 [P] [US1] Ecrire le test ATDD de declenchement au clic dans `app/src/androidTest/java/com/foodgpt/home/HomeScreenLlmMockUiTest.kt`
- [X] T010 [P] [US1] Ecrire le test unitaire de transition `idle -> running` dans `app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt`

### Implementation for User Story 1

- [X] T011 [US1] Ajouter le bouton de lancement LLM mock sur la homepage dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [X] T012 [US1] Relier le clic au declenchement ViewModel dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [X] T013 [US1] Implementer la commande `RunHomepageLlmMockTest` dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T014 [US1] Bloquer les clics repetes pendant `running` dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`

**Checkpoint**: US1 livrable et testable independamment.

---

## Phase 4: User Story 2 - Voir clairement la reponse LLM (Priority: P2)

**Goal**: Afficher lisiblement la reponse LLM apres execution reussie.

**Independent Test**: Simuler une reponse de succes et verifier son rendu integral.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T015 [P] [US2] Ecrire le test ATDD d'affichage reponse succes dans `app/src/androidTest/java/com/foodgpt/home/HomeScreenLlmMockUiTest.kt`
- [X] T016 [P] [US2] Ecrire le test unitaire de mapping `success -> responseText` dans `app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt`

### Implementation for User Story 2

- [X] T017 [US2] Implementer la zone de resultat (texte multi-lignes) dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [X] T018 [US2] Propager `responseText` depuis le ViewModel vers l'UI dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T019 [US2] Ajouter l'etat visuel explicite (`running/success`) dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`

**Checkpoint**: US2 livrable et testable independamment.

---

## Phase 5: User Story 3 - Comprendre les erreurs de lancement ou d'execution (Priority: P3)

**Goal**: Afficher des erreurs explicites et actionnables en cas d'echec.

**Independent Test**: Simuler chaque categorie d'echec et verifier le message rendu.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T020 [P] [US3] Ecrire le test ATDD d'affichage erreur sur echec dans `app/src/androidTest/java/com/foodgpt/home/HomeScreenLlmMockUiTest.kt`
- [X] T021 [P] [US3] Ecrire le test unitaire du timeout 30s dans `app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt`
- [X] T022 [P] [US3] Ecrire le test unitaire des categories d'erreur dans `app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt`

### Implementation for User Story 3

- [X] T023 [US3] Implementer le rendu de message d'erreur par categorie dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [X] T024 [US3] Implementer le timeout 30s et l'etat `failure(timeout)` dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`
- [X] T025 [US3] Permettre la relance apres echec ou succes (`canRun=true`) dans `app/src/main/java/com/foodgpt/home/HomeViewModel.kt`

**Checkpoint**: US3 livrable et testable independamment.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finaliser la coherence globale et verifier le parcours complet.

- [X] T026 [P] Harmoniser les textes UX (loading, succes, erreur) dans `app/src/main/java/com/foodgpt/home/HomeScreen.kt`
- [ ] T027 Verifier le quickstart de bout en bout et mettre a jour la documentation dans `specs/domains/user-guidance-experience/quickstart.md`
- [ ] T028 Executer la campagne de tests unitaires et UI du scope home dans `app/src/test/java/com/foodgpt/home/` et `app/src/androidTest/java/com/foodgpt/home/`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): demarre immediatement.
- Phase 2 (Foundational): depend de Phase 1 et bloque toutes les user stories.
- Phases 3/4/5 (US1/US2/US3): dependent de Phase 2; execution possible en sequence P1 -> P2 -> P3 (recommande) ou en parallele selon capacite.
- Phase 6 (Polish): depend de la completion des user stories cibles.

### User Story Dependencies

- **US1 (P1)**: aucune dependance sur les autres stories apres fondations.
- **US2 (P2)**: depend de la base de declenchement US1 pour obtenir un resultat a afficher.
- **US3 (P3)**: s'appuie sur le flux US1/US2 pour presenter les erreurs et la relance.

### Parallel Opportunities

- T002 et T003 en parallele.
- T005 en parallele de T004 (puis convergence avant T006/T007).
- Dans chaque story, les taches de test marquees `[P]` sont executables en parallele.
- T020/T021/T022 peuvent tourner simultanement.

---

## Parallel Example: User Story 3

```bash
Task: "T020 [US3] Test ATDD affichage erreur dans app/src/androidTest/java/com/foodgpt/home/HomeScreenLlmMockUiTest.kt"
Task: "T021 [US3] Test timeout 30s dans app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt"
Task: "T022 [US3] Test categories d'erreur dans app/src/test/java/com/foodgpt/home/HomeLlmMockTriggerTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Completer Phases 1 et 2.
2. Livrer Phase 3 (US1) seule.
3. Valider le declenchement homepage independamment.

### Incremental Delivery

1. Ajouter US2 pour affichage resultat.
2. Ajouter US3 pour robustesse erreurs + relance.
3. Finaliser avec Phase 6.

### Format Validation

Toutes les taches respectent le format requis:
- Checkbox markdown `- [ ]`
- ID sequentiel `T001` a `T028`
- Marqueur `[P]` uniquement quand parallelisable
- Label story `[US1]`, `[US2]`, `[US3]` sur les phases user story
- Chemin de fichier explicite dans chaque tache
