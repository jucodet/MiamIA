# Tasks: Utilisation API Gemma4 telephone

**Input**: Design documents from `/specs/011-api-gemma4-telephone/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/gemma4-local-api-contract.md, quickstart.md

**Tests**: Les tests d'acceptation/parcours (ATDD) sont obligatoires et doivent echouer avant implementation.

**Organization**: Taches groupees par user story pour permettre implementation et validation independantes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: execution en parallele possible (fichiers differents, pas de dependance directe)
- **[Story]**: etiquette user story (`[US1]`, `[US2]`, `[US3]`)
- Chaque tache inclut un chemin de fichier explicite

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialiser l'ossature technique pour l'integration Gemma4 locale.

- [X] T001 Creer le package d'integration locale dans app/src/main/java/com/miamia/gemma4local/
- [X] T002 Creer les squelettes de tests contract/integration dans app/src/test/java/com/miamia/gemma4local/ et app/src/androidTest/java/com/miamia/gemma4local/
- [X] T003 [P] Ajouter les constantes de configuration/timeouts Gemma4 locale dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalConfig.kt

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Composants transverses obligatoires avant toute user story.

- [X] T004 Implementer la verification de disponibilite API locale dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalAvailabilityChecker.kt
- [X] T005 [P] Implementer le mapping requete interne -> contrat API locale dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalRequestMapper.kt
- [X] T006 [P] Implementer le mapping d'erreurs API locale -> erreurs metier dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalErrorMapper.kt
- [X] T007 Implementer la journalisation technique sans contenu utilisateur dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalMetricsLogger.kt
- [X] T008 Creer le client d'appel API locale texte dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalClient.kt
- [X] T009 Integrer l'abstraction Gemma4 locale dans le flux d'analyse existant dans app/src/main/java/com/miamia/llm/

**Checkpoint**: Fondation prete; les user stories peuvent etre implementees independamment.

---

## Phase 3: User Story 1 - Basculer vers le modele local du telephone (Priority: P1) 🎯 MVP

**Goal**: Executer les analyses texte via API locale Gemma4 sans modele embarque volumineux.

**Independent Test**: Depuis le flux d'analyse existant, une requete texte retourne un resultat `SUCCESS` via API locale sans telechargement de modele.

### Tests for User Story 1 (MANDATORY) ⚠️

- [X] T010 [P] [US1] Ecrire le test d'acceptation Given/When/Then du succes nominal dans app/src/androidTest/java/com/miamia/gemma4local/Us1LocalApiSuccessAcceptanceTest.kt
- [X] T011 [P] [US1] Ecrire le test de contrat `analyzeText` (input/output success) dans app/src/test/java/com/miamia/gemma4local/Gemma4LocalApiContractSuccessTest.kt

### Implementation for User Story 1

- [X] T012 [P] [US1] Implementer le modele `AnalyseTextuelleRequest` dans app/src/main/java/com/miamia/gemma4local/model/AnalyseTextuelleRequest.kt
- [X] T013 [P] [US1] Implementer le modele `AnalyseTextuelleResult` dans app/src/main/java/com/miamia/gemma4local/model/AnalyseTextuelleResult.kt
- [X] T014 [US1] Brancher `Gemma4LocalClient` sur le flux texte principal dans app/src/main/java/com/miamia/analysis/
- [X] T015 [US1] Supprimer la dependance au modele embarque Gemma4 pour le flux texte dans app/src/main/java/com/miamia/llm/
- [X] T016 [US1] Ajouter la validation `inputText` non vide/normalise dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalRequestMapper.kt

**Checkpoint**: US1 livre une analyse texte fonctionnelle via API locale.

---

## Phase 4: User Story 2 - Informer clairement en cas d'indisponibilite de l'API locale (Priority: P2)

**Goal**: Afficher un echec explicite et actionnable quand l'API locale n'est pas disponible, sans fallback.

**Independent Test**: API locale desactivee -> message d'erreur explicite en moins de 2 secondes, sans fallback local/distant.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T017 [P] [US2] Ecrire le test d'acceptation Given/When/Then de l'indisponibilite API locale dans app/src/androidTest/java/com/miamia/gemma4local/Us2ApiUnavailableAcceptanceTest.kt
- [X] T018 [P] [US2] Ecrire le test de contrat `analyzeText` (failure + errorType + userMessage) dans app/src/test/java/com/miamia/gemma4local/Gemma4LocalApiContractFailureTest.kt

### Implementation for User Story 2

- [X] T019 [US2] Implementer le resultat d'echec `API_UNAVAILABLE` sans fallback dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalClient.kt
- [X] T020 [US2] Connecter le mapping d'erreurs vers messages utilisateur dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalErrorMapper.kt
- [X] T021 [US2] Afficher l'etat d'erreur explicite dans l'UI du flux analyse dans app/src/main/java/com/miamia/analysis/
- [X] T022 [US2] Verifier l'absence de fallback moteur dans l'orchestration analyse dans app/src/main/java/com/miamia/llm/

**Checkpoint**: US2 gere proprement les indisponibilites API locale.

---

## Phase 5: User Story 3 - Preserver une experience d'analyse fluide (Priority: P3)

**Goal**: Respecter les objectifs de latence par classe d'appareil et garantir observabilite non sensible.

**Independent Test**: Campagne de requetes textuelles valide p95 < 3s (recents), p95 < 6s (minimum compatibles), et logs sans contenu utilisateur.

### Tests for User Story 3 (MANDATORY) ⚠️

- [X] T023 [P] [US3] Ecrire le test d'integration de mesure latence p95 par classe d'appareil dans app/src/androidTest/java/com/miamia/gemma4local/Us3LatencySloIntegrationTest.kt
- [X] T024 [P] [US3] Ecrire le test de non-conservation du contenu utilisateur dans les metriques dans app/src/test/java/com/miamia/gemma4local/Us3PrivacyMetricsTest.kt

### Implementation for User Story 3

- [X] T025 [US3] Implementer l'entite `ApiCallMetric` et `deviceClass` dans app/src/main/java/com/miamia/gemma4local/model/ApiCallMetric.kt
- [X] T026 [US3] Journaliser `requestId/outcome/latency/errorType/deviceClass` sans contenu brut dans app/src/main/java/com/miamia/gemma4local/Gemma4LocalMetricsLogger.kt
- [X] T027 [US3] Ajouter la classification appareil recent/minimum compatible dans app/src/main/java/com/miamia/gemma4local/DeviceClassResolver.kt
- [X] T028 [US3] Integrer la collecte des latences p95 au pipeline d'analyse dans app/src/main/java/com/miamia/analysis/

**Checkpoint**: US3 valide performances cible et confidentialite des traces.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finaliser qualite, documentation et verification globale.

- [X] T029 [P] Mettre a jour la documentation d'integration locale dans specs/011-api-gemma4-telephone/quickstart.md
- [X] T030 Executer la verification complete quickstart et consigner les resultats dans specs/011-api-gemma4-telephone/quickstart.md
- [X] T031 [P] Nettoyer/refactoriser les composants gemma4local partages dans app/src/main/java/com/miamia/gemma4local/

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: demarrage immediat
- **Phase 2 (Foundational)**: depend de Phase 1; bloque toutes les user stories
- **Phases 3-5 (US1-US3)**: dependent de la Phase 2; execution possible en parallele selon capacite equipe
- **Phase 6 (Polish)**: depend des stories selectionnees comme scope de livraison

### User Story Dependencies

- **US1 (P1)**: demarre apres fondations; independante
- **US2 (P2)**: demarre apres fondations; utilise composants US1/fondation mais reste testable seule
- **US3 (P3)**: demarre apres fondations; s'appuie sur instrumentation commune

### Within Each User Story

- Ecrire tests d'acceptation/contrat d'abord (doivent echouer)
- Implementer modeles et mapping
- Integrer services/client
- Finaliser UI/orchestration
- Valider independamment la story

### Parallel Opportunities

- T003, T005, T006, T010, T011, T017, T018, T023, T024, T029, T031
- Les stories US1/US2/US3 peuvent etre menees en parallele apres Phase 2

---

## Parallel Example: User Story 1

```bash
# Lancer les tests ATDD US1 en parallele
Task: "T010 [US1] .../Us1LocalApiSuccessAcceptanceTest.kt"
Task: "T011 [US1] .../Gemma4LocalApiContractSuccessTest.kt"

# Lancer les modeles US1 en parallele
Task: "T012 [US1] .../AnalyseTextuelleRequest.kt"
Task: "T013 [US1] .../AnalyseTextuelleResult.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Terminer Phase 1
2. Terminer Phase 2 (blocant)
3. Livrer Phase 3 (US1)
4. Valider US1 seule (test acceptance + contrat)
5. Demo/livraison MVP

### Incremental Delivery

1. Setup + Foundation
2. Ajouter US1 -> valider -> livrer
3. Ajouter US2 -> valider -> livrer
4. Ajouter US3 -> valider -> livrer

### Parallel Team Strategy

1. Equipe complete Setup + Foundation
2. Ensuite repartition:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Integration finale et polish
