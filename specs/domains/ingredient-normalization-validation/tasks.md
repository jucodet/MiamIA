# Tasks: ocr-dot-end-capture

**Input**: Design documents from `specs/domains/ingredient-normalization-validation/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Dans ce projet, les tests d'acceptation/parcours (ATDD) sont **OBLIGATOIRES**.
Chaque user story inclut au minimum un test d'acceptation aligné sur les scénarios Given/When/Then du `spec.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Enrichir les fixtures OCR partagées pour les nouvelles variantes de points internes et contextuels (research Decision 4)

- [x] T001 [P] Ajouter les fixtures `DOT_INTERNAL_ADDITIVE`, `DOT_INTERNAL_ABBREVIATION`, `DOT_SPACE_END`, `DOT_NEWLINE_END`, `DOT_EOF_END` dans `app/src/test/java/com/miamia/analysis/ingredientsegment/fixtures/OcrFixtures.kt`

**Checkpoint**: Fixtures disponibles, aucun test ne les consomme encore

---

## Phase 2: User Story 1 - Proposer une liste d'ingrédients avec fin de capture contextuelle (Priority: P1) 🎯 MVP

**Goal**: Le `.` ne termine la capture que s'il est suivi d'un espace ou d'un retour à la ligne (ou en fin de texte). Les points internes (codes additifs, abréviations) ne coupent plus la liste. `!` et `?` restent inconditionnels.

**Independent Test**: Exécuter `IngredientSegmentBoundaryResolverTest` et `IngredientSegmentPhraseBoundaryAcceptanceTest` — tous les cas (existants + nouveaux) passent à 100 %.

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation (ATDD)**

- [x] T002 [P] [US1] Ajouter le test contrat BC-01 (`DOT_SPACE_END` → SENTENCE_TERMINATOR au `. `) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`
- [x] T003 [P] [US1] Ajouter le test contrat BC-02 (`DOT_INTERNAL_ADDITIVE` → point interne ignoré, LINE_END ou TEXT_END) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`
- [x] T004 [P] [US1] Ajouter le test contrat BC-03 (`DOT_INTERNAL_ABBREVIATION` → point interne ignoré, TEXT_END) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`
- [x] T005 [P] [US1] Ajouter le test contrat BC-04 (`DOT_EOF_END` → `.` en fin de texte = SENTENCE_TERMINATOR) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`
- [x] T006 [P] [US1] Ajouter le test contrat BC-07 (`DOT_NEWLINE_END` → `.\n` = SENTENCE_TERMINATOR) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolverTest.kt`
- [x] T007 [P] [US1] Ajouter le test d'acceptation US1§2 (point interne ne coupe pas la capture) via `IngredientSegmentPreparationService` dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPhraseBoundaryAcceptanceTest.kt`
- [x] T008 [P] [US1] Ajouter le test d'acceptation US1§1 révisé (`. ` termine la capture) via `IngredientSegmentPreparationService` dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPhraseBoundaryAcceptanceTest.kt`

### Implementation for User Story 1

- [x] T009 [US1] Modifier `resolveEnd()` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolver.kt` : le `.` n'est terminateur que si suivi d'un espace, d'un `\n`, ou en dernière position du texte ; `!` et `?` restent inconditionnels (FR-003 révisé, research Decision 1 + Decision 3)
- [x] T010 [US1] Vérifier que les tests existants dans `IngredientSegmentBoundaryResolverTest.kt` passent toujours (non-régression : `resolver ends at sentence terminator before newline` doit conserver son comportement car le `.` dans la fixture `FR_WITH_SENTENCE_END` est suivi d'un espace)
- [x] T011 [US1] Vérifier que les tests existants dans `IngredientSegmentPhraseBoundaryAcceptanceTest.kt` passent toujours (4 tests existants inchangés)
- [x] T012 [US1] Vérifier que `IngredientSegmentPerformanceTest` ne montre pas de régression de latence dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPerformanceTest.kt`

**Checkpoint**: US1 complet — tous les tests (nouveaux + existants) passent, le `.` contextuel fonctionne

---

## Phase 3: User Story 2 - Confirmer ou corriger avant analyse (Priority: P2)

**Goal**: Vérifier que le flux de confirmation/correction existant fonctionne correctement avec les segments plus longs produits par la nouvelle logique de fin de capture.

**Independent Test**: Exécuter `AnalysisSubmissionGateContractTest` et `AnalysisSubmissionDecisionAcceptanceTest` — tous passent.

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T013 [P] [US2] Ajouter un test d'acceptation vérifiant que `AnalysisSubmissionGate.evaluate()` accepte un segment contenant un point interne (ex. `DOT_INTERNAL_ADDITIVE`) après confirmation utilisateur dans `app/src/test/java/com/miamia/analysis/ingredientsegment/AnalysisSubmissionDecisionAcceptanceTest.kt`

### Vérification for User Story 2

- [x] T014 [US2] Vérifier que les tests existants `AnalysisSubmissionGateContractTest` et `AnalysisSubmissionDecisionAcceptanceTest` passent toujours (non-régression gate aval) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/`

**Checkpoint**: US2 vérifié — le flux confirmation/gate accepte les segments avec points internes

---

## Phase 4: User Story 3 - Comprendre l'absence de liste exploitable (Priority: P3)

**Goal**: Vérifier que le comportement en l'absence d'ancre ou avec segment vide n'est pas affecté par le changement FR-003.

**Independent Test**: Exécuter `IngredientSegmentFallbackAcceptanceTest` — tous passent.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T015 [US3] Vérifier que `IngredientSegmentFallbackAcceptanceTest` passe toujours (ancre absente → `ANCHOR_MISSING_BLOCKED`) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentFallbackAcceptanceTest.kt`

### Vérification for User Story 3

- [x] T016 [US3] Vérifier que `IngredientSegmentPreparationContractTest` passe toujours (contrat de sortie préservé) dans `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientSegmentPreparationContractTest.kt`

**Checkpoint**: US3 vérifié — aucune régression sur le comportement fallback/blocage

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validation finale, documentation, non-régression globale

- [x] T017 Exécuter la suite complète `./gradlew :app:testDebugUnitTest` et confirmer 0 échec
- [x] T018 [P] Mettre à jour le commentaire KDoc de `resolveEnd()` dans `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentBoundaryResolver.kt` pour refléter FR-003 révisé
- [x] T019 [P] Exécuter la validation manuelle quickstart (sections A–F) documentée dans `specs/domains/ingredient-normalization-validation/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Pas de dépendance — fixtures seules
- **US1 (Phase 2)**: Dépend de Phase 1 (fixtures) — tests ATDD d'abord (T002–T008), puis implémentation (T009), puis vérification non-régression (T010–T012)
- **US2 (Phase 3)**: Dépend de Phase 2 (US1 terminé, resolver modifié)
- **US3 (Phase 4)**: Dépend de Phase 2 (US1 terminé, resolver modifié)
- **Polish (Phase 5)**: Dépend de toutes les phases précédentes

### User Story Dependencies

- **User Story 1 (P1)**: Seule story avec du code de production modifié — bloque les vérifications US2/US3
- **User Story 2 (P2)**: Peut démarrer après US1 — vérifie la compatibilité gate aval
- **User Story 3 (P3)**: Peut démarrer après US1 — vérifie la compatibilité fallback ; **parallélisable avec US2**

### Within User Story 1

- T002–T008 (tests ATDD) MUST être écrits et échouer AVANT T009 (implémentation)
- T009 (implémentation) fait passer les tests T002–T008
- T010–T012 (non-régression) après T009

### Parallel Opportunities

- T002–T008 : tous [P], fichiers différents ou sections indépendantes du même fichier
- T013 [P] et T015 peuvent être écrits en parallèle avec T002–T008
- US2 (Phase 3) et US3 (Phase 4) peuvent être vérifiés en parallèle après US1
- T017, T018, T019 : T018 et T019 sont [P] entre eux

---

## Parallel Example: User Story 1

```bash
# Étape 1 — écrire tous les tests en parallèle (ATDD, doivent FAIL) :
Task: "T002 [P] [US1] Test contrat BC-01 (dot+space → SENTENCE_TERMINATOR)"
Task: "T003 [P] [US1] Test contrat BC-02 (dot interne additif → ignoré)"
Task: "T004 [P] [US1] Test contrat BC-03 (dot interne abréviation → ignoré)"
Task: "T005 [P] [US1] Test contrat BC-04 (dot EOF → SENTENCE_TERMINATOR)"
Task: "T006 [P] [US1] Test contrat BC-07 (dot+newline → SENTENCE_TERMINATOR)"
Task: "T007 [P] [US1] Acceptance US1§2 (point interne ne coupe pas)"
Task: "T008 [P] [US1] Acceptance US1§1 révisé (dot+space termine)"

# Étape 2 — implémenter (séquentiel, 1 fichier) :
Task: "T009 [US1] Modifier resolveEnd() dans IngredientSegmentBoundaryResolver.kt"

# Étape 3 — vérifier non-régression :
Task: "T010 [US1] Tests existants BoundaryResolverTest"
Task: "T011 [US1] Tests existants PhraseBoundaryAcceptanceTest"
Task: "T012 [US1] PerformanceTest non-régression"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Fixtures (T001)
2. Complete Phase 2: Tests ATDD US1 (T002–T008) → FAIL
3. Implémenter T009 → tests passent
4. Vérifier non-régression (T010–T012)
5. **STOP and VALIDATE**: `./gradlew :app:testDebugUnitTest`

### Incremental Delivery

1. Phase 1 (fixtures) → Setup prêt
2. Phase 2 (US1) → MVP livrable, `.` contextuel fonctionne
3. Phase 3 (US2) → Gate aval vérifiée
4. Phase 4 (US3) → Fallback vérifié
5. Phase 5 (polish) → Suite complète, KDoc, quickstart validé

---

## Notes

- Changement chirurgical : **1 fichier source modifié** (`IngredientSegmentBoundaryResolver.kt`)
- **5 fixtures ajoutées**, **7 tests ajoutés/enrichis**, vérification non-régression sur **6 fichiers test existants**
- US2 et US3 sont des vérifications de non-régression (code existant déjà implémenté)
- Aucun changement UI, aucun changement de modèle de données, aucune migration
