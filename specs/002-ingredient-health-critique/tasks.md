# Tasks: Critique santé d’une liste d’ingrédients (prompt LLM)

**Input**: Design documents from `specs/002-ingredient-health-critique/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: ATDD **obligatoire** (template projet) — au minimum une tâche de test par user story, alignée sur les scénarios Given/When/Then du `spec.md`.

**Organization**: Phases par user story (P1 → P3) après fondations partagées.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: exécutable en parallèle (fichiers différents, pas de dépendance sur une tâche incomplète du même lot)
- **[Story]**: `[US1]`, `[US2]`, `[US3]` pour les phases user story uniquement

## Phase 1: Setup (structure partagée)

**Purpose**: initialiser le module `healthcritique` décrit dans `specs/002-ingredient-health-critique/plan.md`.

- [x] T001 Create `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueModels.kt` with `PopulationKey` enum (`ENFANTS`, `FEMMES_ENCEINTES`, `ADULTES`, `PERSONNES_AGEES`) and sealed types / DTOs matching states in `specs/002-ingredient-health-critique/contracts/health-critique-llm-contract.md` (`critique_ready`, `inference_error`, `input_invalid`)

---

## Phase 2: Foundational (prérequis bloquants)

**Purpose**: validation entrée, parsing des sections, prompt système, exécution LLM locale — **aucune user story ne démarre avant la fin de cette phase**.

**Checkpoint**: `HealthCritiqueEngine` peut être invoqué depuis les couches UI avec contrat stable.

- [x] T002 [P] Implement `HealthIngredientInputValidator.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthIngredientInputValidator.kt` (empty / too-short rules + messages FR clairs — seuil documenté, aligné FR-005 et `specs/002-ingredient-health-critique/research.md`)
- [x] T003 [P] Implement `HealthCritiqueSectionParser.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueSectionParser.kt` (markers `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`, remplissage `parseWarnings` si marqueur absent)
- [x] T004 Implement `HealthCritiquePromptBuilder.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthCritiquePromptBuilder.kt` (FR-002, FR-003 : quatre populations, structure 4 sections, consignes de prudence de base, disclaimer non médical)
- [x] T005 Implement `HealthCritiqueLlmRunner.kt` as interface plus `LiteRtHealthCritiqueRunner.kt` in `app/src/main/java/com/foodgpt/healthcritique/` (appel LiteRT-LM sur worker IO ; réutiliser patterns de `app/src/main/java/com/foodgpt/composition/LiteRtGemmaEngine.kt` et résolution modèle comme le flux Gemma existant)
- [x] T006 Implement `HealthCritiqueEngine.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueEngine.kt` (orchestration : validate → build prompts → runner → parse → `critique_ready` / `inference_error` / `input_invalid`)

---

## Phase 3: User Story 1 — Critique santé par population (Priority: P1) MVP

**Goal**: liste d’ingrédients → analyse structurée en 4 sections ; refus entrée vide.

**Independent Test**: saisir une liste valide, lancer l’analyse, vérifier 4 sections ; liste vide → message clair sans appel LLM (scénarios US1 du `spec.md`).

### Tests for User Story 1 (MANDATORY)

> Rédiger ces tests en premier ; ils peuvent échouer jusqu’à complétion de T009–T012.

- [x] T007 [P] [US1] Add `HealthIngredientInputValidatorTest.kt` in `app/src/test/java/com/foodgpt/healthcritique/HealthIngredientInputValidatorTest.kt` (Given liste vide / trop courte / valide — Then `input_invalid` ou acceptation)
- [x] T008 [P] [US1] Add `HealthCritiqueSectionParserTest.kt` in `app/src/test/java/com/foodgpt/healthcritique/HealthCritiqueSectionParserTest.kt` (Given texte LLM fixture — Then quatre sections parsées ou `parseWarnings` attendus)
- [x] T009 [US1] Add `HealthCritiqueEngineTest.kt` in `app/src/test/java/com/foodgpt/healthcritique/HealthCritiqueEngineTest.kt` (Given fake `HealthCritiqueLlmRunner` — When analyse — Then `critique_ready` avec sections ; Given runner erreur — Then `inference_error`)

### Implementation for User Story 1

- [x] T010 [US1] Durcir `LiteRtHealthCritiqueRunner.kt` et `HealthCritiqueEngine.kt` contre le Gemma réel (chemins modèle, `gemma_not_found` / timeout, messages utilisateur alignés `specs/002-ingredient-health-critique/contracts/health-critique-llm-contract.md`)
- [x] T011 [US1] Add `HealthCritiqueViewModel.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueViewModel.kt` (états Compose : saisie, `Analyzing`, résultat 4 blocs, erreurs entrée / inférence)
- [x] T012 [US1] Add `HealthCritiqueScreen.kt` in `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueScreen.kt` (Material3 : `TextField`, bouton analyser, sections scrollables, disclaimer visible)
- [x] T013 [US1] Wire navigation / affichage depuis `app/src/main/java/com/foodgpt/MainActivity.kt` (ou écran parent existant) pour rendre `HealthCritiqueScreen` accessible sans casser le flux caméra existant

**Checkpoint**: US1 démontrable seul (MVP).

---

## Phase 4: User Story 2 — Réponse prudente et non alarmiste (Priority: P2)

**Goal**: prompt et sortie orientés faits / incertitudes ; pas de diagnostic ; prudence grossesse (scénarios US2).

**Independent Test**: contenu du prompt + échantillon de réponse (fixture ou test manuel assisté) reflète les consignes US2.

### Tests for User Story 2 (MANDATORY)

- [x] T014 [P] [US2] Add `HealthCritiquePromptPrudenceTest.kt` in `app/src/test/java/com/foodgpt/healthcritique/HealthCritiquePromptPrudenceTest.kt` (Given `HealthCritiquePromptBuilder` — Then chaînes obligatoires : distinction faits/incertitudes, interdiction diagnostic, consultation pro si grossesse / risque)

### Implementation for User Story 2

- [x] T015 [US2] Enrichir `app/src/main/java/com/foodgpt/healthcritique/HealthCritiquePromptBuilder.kt` avec formulations US2 (additifs ambigus, demande de nuances, edge cases `spec.md` section Edge Cases)
- [x] T016 [US2] Afficher `parseWarnings` et mentions d’ambiguïté dans `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueScreen.kt` lorsque le parseur signale des sections manquantes ou du texte hors marqueurs

**Checkpoint**: US1 + US2 testables indépendamment (US2 sans casser US1).

---

## Phase 5: User Story 3 — Copier et dernière analyse (Priority: P3)

**Goal**: copie réponse (et optionnellement prompt) ; persistance minimaliste dernière analyse (FR-006, SC-003).

**Independent Test**: copier depuis l’UI ; relancer l’app → dernière analyse visible (scénarios US3).

### Tests for User Story 3 (MANDATORY)

- [x] T017 [P] [US3] Add `HealthCritiquePersistenceAndroidTest.kt` in `app/src/androidTest/java/com/foodgpt/healthcritique/HealthCritiquePersistenceAndroidTest.kt` (Given analyse sauvegardée — When redémarrage processus simulé / recréation `Context` — Then snapshot relu avec ingrédients + horodatage + texte résultat)

### Implementation for User Story 3

- [x] T018 [US3] Implement `LastHealthAnalysisStore.kt` in `app/src/main/java/com/foodgpt/healthcritique/LastHealthAnalysisStore.kt` (SharedPreferences ou fichier privé — modèle `LastHealthAnalysisSnapshot` dans `specs/002-ingredient-health-critique/data-model.md`)
- [x] T019 [US3] Brancher `LastHealthAnalysisStore` dans `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueViewModel.kt` (sauvegarde après `critique_ready`, restauration au démarrage)
- [x] T020 [P] [US3] Add `HealthCritiqueClipboardAndroidTest.kt` in `app/src/androidTest/java/com/foodgpt/healthcritique/HealthCritiqueClipboardAndroidTest.kt` (Given résultat affiché — When action copier — Then presse-papiers non vide — SC-003)
- [x] T021 [US3] Ajouter boutons « Copier la réponse » et « Copier le prompt » (si FR-002 export utilisateur) dans `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueScreen.kt` via `ClipboardManager` / API Compose appropriée

**Checkpoint**: les trois user stories sont livrables et testables séparément.

---

## Phase 6: Polish & cross-cutting

**Purpose**: documentation d’exécution, cohérence perf, alignement quickstart.

- [x] T022 [P] Mettre à jour `specs/002-ingredient-health-critique/quickstart.md` avec le chemin d’accès réel à l’écran et les commandes Gradle utilisées en CI locale
- [x] T023 Vérifier timeout / annulation inférence dans `app/src/main/java/com/foodgpt/healthcritique/LiteRtHealthCritiqueRunner.kt` (alignement objectif inférieur à 30 s du `plan.md`) et ajuster `HealthCritiqueViewModel.kt` si nécessaire

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → **Phase 2** → **Phases 3–5 (US1 → US2 → US3)** → **Phase 6**
- Aucune user story avant fin Phase 2 (`T007`–`T009` nécessitent `T001`–`T006` pour compiler ; **échec attendu** des tests ATDD US1 jusqu’à durcissement device `T010`).

### User Story Dependencies

- **US1** : après Phase 2 ; aucune dépendance à US2/US3.
- **US2** : après US1 (s’appuie sur écran + moteur existants) ; tests et prompt uniquement.
- **US3** : après US1 (besoin du flux résultat) ; peut commencer en parallèle de US2 une fois T011–T012 livrés si équipe sépare store vs prudence.

### Within Each User Story

- Tests listés **avant** implémentation (ATDD) pour US1–US3.
- `HealthCritiqueEngine` avant `ViewModel` avant `HealthCritiqueScreen`.

### Parallel Opportunities

- T002 ∥ T003 ; T007 ∥ T008 ; T014 ∥ (après T004) ; T017 ∥ T020 une fois T018 prêt ; T022 en fin de chaîne.

---

## Parallel Example: User Story 1

```text
# Tests ATDD US1 en parallèle (après modèles + validator + parser) :
T007 HealthIngredientInputValidatorTest.kt
T008 HealthCritiqueSectionParserTest.kt
# Puis T009 (engine + fake), puis impl T010–T013 séquentiel.
```

---

## Parallel Example: User Story 3

```text
T017 HealthCritiquePersistenceAndroidTest.kt  ∥  T020 HealthCritiqueClipboardAndroidTest.kt
# Après T018 LastHealthAnalysisStore.kt et branchement T019.
```

---

## Implementation Strategy

### MVP (User Story 1 uniquement)

1. Terminer Phase 1–2 (T001–T006).  
2. Écrire T007–T009 (échec attendu).  
3. Implémenter T010–T013 jusqu’au vert des tests US1.  
4. **Stop** : démo / validation `quickstart.md` partiel.

### Livraison incrémentale

1. MVP (US1) → valider SC-001 partiellement (sections parsées).  
2. Ajouter US2 (T014–T016) → renforcer prudence / confiance.  
3. Ajouter US3 (T017–T021) → FR-006 + SC-003.  
4. Polish T022–T023.

### Comptage des tâches

| Zone | Tâches |
|------|--------|
| Setup + Foundational | T001–T006 (6) |
| US1 | T007–T013 (7) |
| US2 | T014–T016 (3) |
| US3 | T017–T021 (5) |
| Polish | T022–T023 (2) |
| **Total** | **23** |

---

## Notes

- Réutiliser autant que possible `LiteRtGemmaEngine.kt` / clients Gemma existants plutôt que dupliquer la gestion native.  
- Ne pas envoyer le texte hors appareil pour ce flux (alignement `research.md` + contrat).  
- Si `SPECIFY_FEATURE_DIRECTORY` n’est pas défini, `check-prerequisites` suit `.specify/feature.json` : pour régénérer les tâches sur 002, exporter `SPECIFY_FEATURE_DIRECTORY=specs/002-ingredient-health-critique`.
