# Tasks: ingredient-knowledge — Feature IKB-A (KB référence additifs/allergènes offline + injection contexte)

**Input**: Design documents from `/specs/domains/ingredient-knowledge/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Dans ce projet, les tests d'acceptation/parcours (ATDD) sont **OBLIGATOIRES**. Au minimum une tâche de test d'acceptation par user story, alignée sur les scénarios Given/When/Then du `spec.md`. Les tests DOIVENT échouer avant l'implémentation (ATDD).

**Organization**: Tasks grouped by user story (US-IKB-A1 P1, US-IKB-A2 P1, US-IKB-A3 P2) pour implémentation/test indépendants.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Exécutable en parallèle (fichiers différents, pas de dépendance sur tâche incomplète)
- **[Story]**: User story rattachée (US1=IKB-A1, US2=IKB-A2, US3=IKB-A3)
- Chemins exacts inclus dans chaque description

## Path Conventions

- **Mobile Android (monolithe)** : code dans `app/src/main/java/com/miamia/ingredientknowledge/`, tests JVM dans `app/src/test/java/com/miamia/ingredientknowledge/`, assets dans `app/src/main/assets/ingredientkb/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialisation du package et des dépendances.

- [x] T001 Create package `com.miamia.ingredientknowledge` (dossier `app/src/main/java/com/miamia/ingredientknowledge/`) et dossier assets `app/src/main/assets/ingredientkb/` per implementation plan
- [x] T002 Add `kotlinx-serialization-json` dependency + plugin `org.jetbrains.kotlin.plugin.serialization` in `app/build.gradle.kts` (see research.md §7)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modèles de domaine partagés + interface de frontière. MUST être complet avant toute user story.

**⚠️ CRITICAL**: Aucune user story ne peut démarrer avant la fin de cette phase.

- [x] T003 [P] Create `RiskLevel` enum (FAIBLE/MODERE/ELEVE) in `app/src/main/java/com/miamia/ingredientknowledge/RiskLevel.kt`
- [x] T004 [P] Create `KbSource` (origin, baseVersion, sourceRef) in `app/src/main/java/com/miamia/ingredientknowledge/KbSource.kt`
- [x] T005 [P] Create `AdditiveFactCard` (eNumber clé primaire, canonicalName, aliases, role, riskLevel, source) in `app/src/main/java/com/miamia/ingredientknowledge/AdditiveFactCard.kt`
- [x] T006 [P] Create `AllergenFactCard` (id, regulatoryName, aliases, source) in `app/src/main/java/com/miamia/ingredientknowledge/AllergenFactCard.kt`
- [x] T007 [P] Create `IngredientDesignation` (rawText, normalized) in `app/src/main/java/com/miamia/ingredientknowledge/IngredientDesignation.kt`
- [x] T008 [P] Create `MechanicalNormalizer` (casse→minuscules, espaces→collapse, accents→ASCII) in `app/src/main/java/com/miamia/ingredientknowledge/MechanicalNormalizer.kt`
- [x] T009 [P] Create `LookupOutcome` (matchedAdditives, matchedAllergens, unmatchedDesignations, baseVersion) in `app/src/main/java/com/miamia/ingredientknowledge/LookupOutcome.kt`
- [x] T010 Create `ReferenceKb` interface (lookup, baseVersion) per contract `contracts/ingredient-kb-lookup-contract.md` in `app/src/main/java/com/miamia/ingredientknowledge/ReferenceKb.kt`

**Checkpoint**: Modèles partagés + interface de frontière prêts — les user stories peuvent démarrer.

---

## Phase 3: User Story 1 — Lookup offline additif/allergène (Priority: P1) 🎯 MVP

**Goal**: À partir d'une liste d'ingrédients, retourner les fiches référence (additifs par E-number/alias, allergènes) depuis une base embarquée, hors-ligne, avec repli silencieux pour les substances non référencées. (US-IKB-A1)

**Independent Test**: Exécuter `InMemoryReferenceKbLookupTest` — additif référencé par E-number et par alias → même fiche canonique ; allergène réglementaire → fiche ; substance non référencée → `unmatchedDesignations`, aucune fiche, pas de blocage. Aucun réseau requis.

### Tests for User Story 1 (MANDATORY) ⚠️

> ATDD : écrire ces tests FIRST, vérifier qu'ils ÉCHOUENT avant implémentation.

- [x] T011 [P] [US1] Acceptance test: additif référencé par E-number **et** par dénomination courante → **une** fiche canonique (clé E-number) in `app/src/test/java/com/miamia/ingredientknowledge/InMemoryReferenceKbLookupTest.kt` (aligné US-IKB-A1 scénario 1 + edge case E-number/alias)
- [x] T012 [P] [US1] Acceptance test: substance non référencée → `unmatchedDesignations`, aucune fiche inventée, aucun blocage in `app/src/test/java/com/miamia/ingredientknowledge/InMemoryReferenceKbLookupTest.kt` (aligné US-IKB-A1 scénario 3 + IKB-A-FR-003/007)

### Implementation for User Story 1

- [x] T013 [P] [US1] Create `InMemoryReferenceKb` (implémentation `ReferenceKb` alimentée par fixtures, JVM pur) in `app/src/main/java/com/miamia/ingredientknowledge/InMemoryReferenceKb.kt`
- [x] T014 [P] [US1] Create serialized DTOs (`AdditiveDto`, `AllergenDto`, `KbVersionDto`) for JSON assets in `app/src/main/java/com/miamia/ingredientknowledge/dto/KbDtos.kt`
- [x] T015 [P] [US1] Create asset fixtures `additives.json`, `allergens.json` (14 allergènes UE), `kb-version.json` in `app/src/main/assets/ingredientkb/`
- [x] T016 [US1] Implement `IngredientKbLookup` (matching sous-chaîne littérale + normalisations mécaniques via `MechanicalNormalizer`, déduplication par E-number) in `app/src/main/java/com/miamia/ingredientknowledge/IngredientKbLookup.kt` (depends on T008, T010, T013)
- [x] T017 [US1] Implement `EmbeddedReferenceKb` (parse assets via kotlinx.serialization, build index `Map<E-number, AdditiveFactCard>` + `Map<alias normalisé, E-number>`, expose `baseVersion`) in `app/src/main/java/com/miamia/ingredientknowledge/EmbeddedReferenceKb.kt` (depends on T014, T015)
- [x] T018 [US1] Add error handling: base absente ou illisible → erreur domaine explicite, aucun contexte inventé (`IKB-A-FR-010`) in `app/src/main/java/com/miamia/ingredientknowledge/EmbeddedReferenceKb.kt`
- [x] T019 [P] [US1] Robolectric test: charge les assets réels, `baseVersion()` lue, lookup exploitable hors-ligne in `app/src/test/java/com/miamia/ingredientknowledge/EmbeddedReferenceKbRobolectricTest.kt` (aligné IKB-A-SC-005/006)

**Checkpoint**: User Story 1 fonctionnelle et testable indépendamment (lookup offline + repli silencieux).

---

## Phase 4: User Story 2 — `ReferenceContext` borné et qualifié général (Priority: P1)

**Goal**: Constituer un `ReferenceContext` borné (plafond N), priorisé (allergènes puis additifs à risque élevé), qualifié explicitement « contenu général », sans aucun fait étiquette. (US-IKB-A2)

**Independent Test**: Exécuter `ReferenceContextBuilderTest` — lookup > N fiches → seules les N fiches prioritaires retenues ; `qualification = GENERAL` ; aucune formulation de fait étiquette.

### Tests for User Story 2 (MANDATORY) ⚠️

- [x] T020 [P] [US2] Acceptance test: `ReferenceContextBuilder` applique le plafond N et la priorisation allergènes→`ELEVE`→`MODERE`→`FAIBLE`, `qualification = GENERAL`, aucune formulation de fait étiquette in `app/src/test/java/com/miamia/ingredientknowledge/ReferenceContextBuilderTest.kt` (aligné US-IKB-A2 scénarios 1 + IKB-A-FR-011/004/005)
- [x] T021 [P] [US2] Contract test: `ReferenceContext` read-model respecte les garde-fous du `contracts/reference-context-read-model.md` (qualification GENERAL, ensemble borné, pas d'alias hors normalisation) in `app/src/test/java/com/miamia/ingredientknowledge/ReferenceContextContractTest.kt`

### Implementation for User Story 2

- [x] T022 [P] [US2] Create `ReferenceContext` + `ReferenceContextEntry` (kind, key, display, riskLevel?, role?, qualification=GENERAL, baseVersion) in `app/src/main/java/com/miamia/ingredientknowledge/ReferenceContext.kt`
- [x] T023 [US2] Implement `ReferenceContextBuilder` (plafond N défaut 12 configurable, priorisation allergènes→`ELEVE`→`MODERE`→`FAIBLE`, omission silencieuse au-delà du plafond) in `app/src/main/java/com/miamia/ingredientknowledge/ReferenceContextBuilder.kt` (depends on T009, T022)

**Checkpoint**: User Stories 1 **et** 2 fonctionnent indépendamment (lookup + contexte borné général).

---

## Phase 5: User Story 3 — Jeu fixe d'ingrédients de référence (Priority: P2)

**Goal**: Fournir un jeu fixe d'ingrédients exécutable isolément (hors capture/OCR/runtime LLM) vérifiant lookup + contexte, avec répétabilité. (US-IKB-A3)

**Independent Test**: Exécuter `ReferenceIngredientFixturesTest` 3× — résultats identiques ; fiches attendues exactes ; contexte conforme.

### Tests for User Story 3 (MANDATORY) ⚠️

- [x] T024 [P] [US3] Acceptance test: le jeu fixe produit exactement les fiches attendues (référencées) et aucune pour les non référencées ; contexte + qualification « général » conformes ; répétabilité ≥ 3 exécutions in `app/src/test/java/com/miamia/ingredientknowledge/ReferenceIngredientFixturesTest.kt` (aligné US-IKB-A3 scénarios 1-3 + IKB-A-SC-004)

### Implementation for User Story 3

- [x] T025 [P] [US3] Create `ReferenceIngredientFixtures` (jeu fixe d'ingrédients + fiches attendues) in `app/src/test/java/com/miamia/ingredientknowledge/fixtures/ReferenceIngredientFixtures.kt`
- [x] T026 [US3] Wire end-to-end fixture verification (lookup `InMemoryReferenceKb` → `ReferenceContextBuilder` → assertions sur le jeu fixe) in `app/src/test/java/com/miamia/ingredientknowledge/fixtures/ReferenceIngredientFixturesTest.kt` (depends on T024, T025)

**Checkpoint**: Toutes les user stories sont indépendamment fonctionnelles et reproductibles.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Améliorations transverses + validation quickstart.

- [x] T027 [P] Unit test: `MechanicalNormalizer` (casse/espaces/accents ; variantes non couvertes → pas de match) in `app/src/test/java/com/miamia/ingredientknowledge/MechanicalNormalizerTest.kt`
- [x] T028 Run `quickstart.md` validation (scénarios 1-4) — vérifier lookup, contexte borné, assets réels, répétabilité per `specs/domains/ingredient-knowledge/quickstart.md`
- [x] T029 [P] Performance sanity check: lookup p95 < 20 ms pour ≤ 50 désignations (micro-benchmark JVM sur `InMemoryReferenceKb`) in `app/src/test/java/com/miamia/ingredientknowledge/IngredientKbLookupPerfTest.kt` (aligné plan.md Performance Goals, principe IV)
- [x] T030 Update `specs/domains/domain-map.md` ownership note si nécessaire (rattachement `ingredient-knowledge` à l'équipe Intelligence) — vérifier cohérence avec la frontière DDD

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: pas de dépendance — démarrage immédiat.
- **Foundational (Phase 2)**: dépend de Setup — **BLOQUE** toutes les user stories.
- **User Stories (Phase 3+)**: dépendent de Foundational.
  - US1 (Phase 3) puis US2 (Phase 4) : US2 consomme le `LookupOutcome` de US1 (dépendance interne T023→T009, et le test US2 peut réutiliser `InMemoryReferenceKb`).
  - US3 (Phase 5) : consomme US1 + US2 (lookup + builder) — indépendamment testable via fixtures.
- **Polish (Phase 6)**: dépend de toutes les user stories retenues.

### User Story Dependencies

- **US1 (P1)**: démarre après Foundational — pas de dépendance vers une autre story.
- **US2 (P1)**: démarre après Foundational ; réutilise `LookupOutcome`/`InMemoryReferenceKb` (US1) mais reste testable isolément avec un lookup bouchonné.
- **US3 (P2)**: démarre après US1 + US2 — vérifie de bout en bout sur le jeu fixe.

### Within Each User Story

- Tests d'acceptation écrits et ÉCHOUANT avant implémentation (ATDD).
- Modèles avant services.
- Services avant intégration.
- Story complète avant la priorité suivante.

### Parallel Opportunities

- Toutes les tâches `[P]` de Setup/Foundational en parallèle (fichiers distincts).
- T011/T012 (tests US1) en parallèle.
- T013, T014, T015 (impl US1) en parallèle (fichiers distincts).
- T020/T021 (tests US2) en parallèle.
- T024/T025 (US3) en parallèle.
- T027/T029/T030 (Polish) en parallèle.

---

## Parallel Example: User Story 1

```bash
# Lancer les tests US1 ensemble (ATDD) :
Task: "Acceptance test additif E-number+alias → fiche canonique (T011)"
Task: "Acceptance test substance non référencée → repli silencieux (T012)"

# Lancer les modèles/impl US1 ensemble (fichiers distincts) :
Task: "InMemoryReferenceKb (T013)"
Task: "KbDtos (T014)"
Task: "asset fixtures additives.json/allergens.json/kb-version.json (T015)"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T002)
2. Complete Phase 2: Foundational (T003-T010) — CRITIQUE, bloque tout
3. Complete Phase 3: User Story 1 (T011-T019)
4. **STOP and VALIDATE**: `InMemoryReferenceKbLookupTest` + `EmbeddedReferenceKbRobolectricTest` passent ; lookup offline + repli silencieux démontrés.
5. Demo si prêt.

### Incremental Delivery

1. Setup + Foundational → fondation prête.
2. + US1 → lookup offline validé (MVP).
3. + US2 → `ReferenceContext` borné + qualifié général, prêt à injection LLM.
4. + US3 → jeu fixe reproductible (qualité continue).
5. Polish → validation quickstart + perf.

### Parallel Team Strategy

Avec plusieurs dev : Setup + Foundational en commun, puis :
- Dev A : US1 (lookup + assets)
- Dev B : US2 (contexte builder) — peut démarrer en parallèle avec un lookup bouchonné
- US3 après fusion US1+US2.

---

## Notes

- `[P]` = fichiers différents, pas de dépendance.
- `[Story]` rattachement à US-IKB-A1/A2/A3 pour traçabilité.
- Chaque user story indépendamment complétable et testable.
- Vérifier que les tests échouent avant l'implémentation (ATDD).
- Committer après chaque tâche ou groupe logique.
- Respecter Feature C : `ReferenceContext` = contenu général uniquement, aucun fait étiquette, aucune extension de `EquivalencePolicy` v1 stricte.
