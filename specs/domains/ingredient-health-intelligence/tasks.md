# Tasks: ingredient-health-intelligence

**Domain**: `specs/domains/ingredient-health-intelligence/`
**Dernière feature**: Feature L — personnalisation du prompt de critique santé (2026-06-28)

> Tâches cumulatives par feature. Section historique Feature K conservée pour traçabilité (constitution I).

---

## Feature K — Plage kcal/100 g (1..1100)

**Input**: `specs/domains/ingredient-health-intelligence/` (plan.md, spec.md Feature K, research.md §9)  
**Prerequisites**: plan.md, spec.md — clarify **Option B** intégré en spec (**IHI-K-FR-006**)

**Tests**: ATDD — au moins une tâche de test par user story Feature K (US-K1, US-K2).

## Format

`- [ ] Tnnn [P?] [USKn?] Description avec chemin fichier`

## Dependency graph (ordre des user stories)

- **US-K1** (affichage nominal) et **US-K2** (hors bornes) partagent le même validateur : livrer d’abord le changement de plage + tests, puis vérif doc.

```text
T001 → T002 [US-K1] ─┐
       T003 [US-K2] ─┴→ T004 → T005
```

## Phase 1: Setup

**Purpose**: aucune infra nouvelle ; vérifier l’alignement spec/code.

- [x] T001 Lire `specs/domains/ingredient-health-intelligence/spec.md` (Feature K, **IHI-K-FR-006**) et `app/src/main/java/com/miamia/composition/EnergyEstimateValidator.kt` pour confirmer l’écart **950 → 1100**.

---

## Phase 2: Foundational (validateur)

**Purpose**: une seule source de vérité pour les bornes d’affichage.

- [x] T002 Mettre à jour `VALID_KCAL_PER_100G_RANGE` et le commentaire fichier dans `app/src/main/java/com/miamia/composition/EnergyEstimateValidator.kt` (**1..1100**).

**Checkpoint**: `clampOrNull` / `sanitizeBilan` reflètent la spec.

---

## Phase 3: User Story K — Pastille énergie (P1 / P2)

### US-K1 — Voir l’estimation après succès (P1)

**Goal**: les valeurs **951–1100** kcal/100 g deviennent affichables après parse si le modèle les émet.

**Independent test**: tests JVM sur `parseKcalFromEnergyBlock` / `clampOrNull` avec **1000** et **1100** non nuls.

- [x] T003 [US-K1] Étendre `app/src/test/java/com/miamia/composition/EnergyEstimateValidatorTest.kt` : cas **951**, **1000**, **1100** acceptés ; conserver **420** / **380** label.

### US-K2 — Absence de chiffre plutôt qu’invention (P2)

**Goal**: **0**, **1101**, valeurs non numériques → `null` ; `sanitizeBilan` efface les aberrations (ex. 12_000).

**Independent test**: assertions `assertNull` sur hors plage + `sanitizeBilan` inchangé pour 12_000.

- [x] T004 [US-K2] Dans `EnergyEstimateValidatorTest.kt`, remplacer le cas **951** hors plage par **1101** (et garder **0** null) ; vérifier `sanitizeBilan` avec valeur hors **1..1100**.

---

## Phase 4: Polish & documentation domaine

- [x] T005 [P] Vérifier qu’aucune doc domaine ne cite **1..950** pour Feature K (grep sous `specs/domains/ingredient-health-intelligence/`) ; `research.md`, `data-model.md`, `quickstart.md`, `contracts/composition-energy-read-model.md`, `plan.md` déjà alignés par le plan — corriger tout résidu.

---

## Parallel execution

- Après **T002** : **T003** et préparation **T004** sur le même fichier de test — exécuter **T003** puis **T004** (même fichier, séquentiel).

## Implementation strategy

- Livraison minimale : constante + tests + cohérence doc ; pas de changement UI (`BilanResultCard` / `CompositionEnergyUiStrings`) sauf copie obsolète.

## Suggested MVP scope

- **T001–T004** (validateur + tests) = incrément complet pour **clarify Option B**.

---

## Feature L — Personnalisation du prompt de critique santé (2026-06-28)

**Input**: `specs/domains/ingredient-health-intelligence/` (plan.md Feature L, spec.md Feature L, research.md §10, data-model.md Feature L, `contracts/critique-prompt-contract.md`)  
**Prerequisites**: spec Feature L + clarify 2026-06-28 (5 décisions) intégré en spec

**Tests**: ATDD — au moins une tâche de test par user story (US-L1, US-L2, US-L3).

## Format

`- [ ] Tnnn [P?] [USLn?] Description avec chemin fichier`

## Dependency graph (Feature L)

```text
T101 → T102 → T103 [US-L1] ──┐
              T104 [US-L2] ──┤→ T106 → T107 [P]
              T105 [P] [US-L3]┘
```

## Phase 1: Setup (Feature L)

**Purpose**: confirmer l'alignement spec/clarify/contract ↔ code existant.

- [X] T101 Lire `specs/domains/ingredient-health-intelligence/spec.md` (Feature L, **IHI-L-FR-001**..**017**), `contracts/critique-prompt-contract.md`, et `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` pour confirmer l'écart de contenu (persona + dimensions + populations vulnérables absents de l'instruction système courante).

---

## Phase 2: Foundational (Feature L)

**Purpose**: introduire le seuil « liste très longue » en nombre d'ingrédients avant le corps du prompt.

- [X] T102 Ajouter la constante `LONG_LIST_INGREDIENT_THRESHOLD: Int = 20` (seuil en nombre d'ingrédients — clarify Q4) avec commentaire référençant **IHI-L-FR-012**, dans `app/src/main/java/com/miamia/healthcritique/HealthCritiqueConfig.kt`.

**Checkpoint**: constante disponible pour le builder et les tests.

---

## Phase 3: User Story L1 — Cadre expert + dimensions de risque (P1) 🎯 MVP

**Goal**: le prompt construit contient le persona expert (nutrition clinique + cancérologie préventive) et exige l'évaluation des 5 dimensions de risque par ingrédient avec hiérarchie faits/incertitudes/hypothèses (réf. CIRC/OMS).

**Independent test**: tests JVM sur `buildSystemInstruction()` — présence du persona, des 5 dimensions, des 3 tiers de preuve, et référence CIRC/OMS.

### Tests for US-L1 (MANDATORY) ⚠️

- [X] T103 [US-L1] Étendre `app/src/test/java/com/miamia/healthcritique/HealthCritiquePromptPrudenceTest.kt` : assertions `contains` sur le persona (« nutrition clinique », « cancérologie préventive »), les 5 dimensions de risque (« cancérogène », « mutagène », « neurotoxique », « métabolique », « inflammatoire »), les 3 tiers (« faits établis », « incertitudes », « hypothèses »), et « CIRC »/« OMS » ; test doit échouer avant l'implémentation (ATDD).

### Implementation for US-L1

- [X] T104 [US-L1] Réécrire `buildSystemInstruction()` dans `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` : ajouter le persona expert, l'analyse ingrédient par ingrédient (correction OCR sans invention — cohérent **IHI-C-FR-001**), les 5 dimensions de risque, la hiérarchie faits établis (CIRC/OMS)/incertitudes/hypothèses, et la contextualisation dose/exposition (interdire « toujours toxique »/« poison »).

**Checkpoint**: US-L1 vert (tests persona + dimensions + hiérarchie passent).

---

## Phase 4: User Story L2 — Populations vulnérables + garde-fous éthiques (P1)

**Goal**: le prompt contient les 4 populations vulnérables (dont immunodéprimées, antécédents familiaux cancer) en vigilance transversale, et les garde-fous éthiques (pas de diagnostic/prescription, orientation professionnel de santé), plus le disclaimer et les cas particuliers.

**Independent test**: tests JVM sur `buildSystemInstruction()` — présence des 4 populations vulnérables et des garde-fous éthiques.

### Tests for US-L2 (MANDATORY) ⚠️

- [X] T105 [US-L2] Étendre `app/src/test/java/com/miamia/healthcritique/HealthCritiquePromptPrudenceTest.kt` (après T103, même fichier) : assertions sur les populations vulnérables (« immunodéprimées », « antécédents familiaux »), les garde-fous (« diagnostic », « prescription », « professionnel »), le disclaimer (réutiliser `HealthCritiquePromptBuilder.DISCLAIMER`), le signalement d'opacité (« arômes »), et la gestion longue liste / langue illisible ; test doit échouer avant l'implémentation (ATDD).

### Implementation for US-L2

- [X] T106 [US-L2] Compléter `buildSystemInstruction()` dans `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` : ajouter le disclaimer, le signalement d'opacité (termes ambigus), les garde-fous éthiques (refus diagnostic/prescription + orientation professionnel de santé), les 4 populations vulnérables en **vigilance transversale intégrée** (clarify Q2 — sans section ni préambule), et les cas particuliers (liste ≥ `LONG_LIST_INGREDIENT_THRESHOLD` → synthèse en tête de section 2 ; langue/illisible → conserver marqueurs + demander précisions).

**Checkpoint**: US-L2 vert (tests populations vulnérables + garde-fous passent).

---

## Phase 5: User Story L3 — Format de sortie strict préservé (P1)

**Goal**: le prompt préserve le format de sortie strict (4 marqueurs ordonnés, aucun texte avant `###ENFANTS`, 3 blocs obligatoires par section) ; non-régression du parseur de sections.

**Independent test**: tests JVM — `HealthCritiqueSectionParserTest` (non-régression 4 marqueurs ordonnés) + assertions format strict dans le prudence test.

### Tests for US-L3 (MANDATORY) ⚠️

- [X] T107 [P] [US-L3] Étendre `app/src/test/java/com/miamia/healthcritique/HealthCritiqueSectionParserTest.kt` : confirmer la non-régression (4 sections reconnues dans l'ordre `###ENFANTS`→`###FEMMES_ENCEINTES`→`###ADULTES`→`###PERSONNES_AGEES`, warning sur marqueur absent) ; ajouter au prudence test les assertions « aucun texte avant ###ENFANTS » et présence des 3 blocs (« Points de vigilance », « Analyse par ingrédient », « Niveau de prudence »). Fichier distinct de T103/T105 → [P].

### Implementation for US-L3

- [X] T108 [US-L3] Finaliser `buildSystemInstruction()` dans `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` : conserver/exposer explicitement le format de sortie strict (4 marqueurs ordonnés, aucun texte avant `###ENFANTS`, 3 blocs obligatoires : Points de vigilance / Analyse par ingrédient & Nuances / Niveau de prudence Faible-Modéré-Élevé). Vérifier `HealthCritiqueSectionParser` inchangé (aucune modification du parseur — `IHI-L-SC-005`).

**Checkpoint**: US-L3 vert (tests format strict + non-régression parser passent).

---

## Phase 6: Polish & documentation domaine (Feature L)

- [X] T109 [P] Vérifier la cohérence doc domaine (grep sous `specs/domains/ingredient-health-intelligence/`) : `plan.md`, `research.md` §10, `data-model.md`, `contracts/critique-prompt-contract.md`, `quickstart.md` alignés Feature L ; exécuter le parcours quickstart Feature L (tests JVM `HealthCritiquePromptPrudenceTest` + `HealthCritiqueSectionParserTest`).

---

## Parallel execution (Feature L)

- **T103** et **T105** éditent le même fichier `HealthCritiquePromptPrudenceTest.kt` → séquentiel (T103 puis T105).
- **T107** édite `HealthCritiqueSectionParserTest.kt` (fichier distinct) → parallèle avec T103/T105.
- **T104**, **T106**, **T108** éditent tous `HealthCritiquePromptBuilder.kt` → séquentiel (dans l'ordre des phases).
- **T109** après T108.

## Implementation strategy (Feature L)

- MVP = **T101–T104** (persona + dimensions + hiérarchie) ; incrément démontrable et testable indépendamment (US-L1).
- Incrémental : US-L2 (T105–T106) puis US-L3 (T107–T108) s'ajoutent sans casser US-L1.
- Non-régression critique : `HealthCritiqueSectionParser` et le flux composition ne sont **pas** modifiés (`IHI-L-SC-005`, `IHI-L-FR-017`).

## Suggested MVP scope (Feature L)

- **T101–T104** = US-L1 complète (persona expert + 5 dimensions de risque + hiérarchie des preuves).

---

## Feature M — Accès UI à la critique santé (câblage de navigation) (2026-06-28)

**Input**: `specs/domains/ingredient-health-intelligence/` (plan.md Feature M, spec.md Feature M, research.md §11, data-model.md Feature M, `contracts/critique-sante-navigation-contract.md`)
**Prerequisites**: spec Feature M (aucun NEEDS CLARIFICATION)

**Tests**: parcours quickstart + tests instrumentés existing (`HealthCritiqueReadOnlySegmentAndroidTest`). Pas de nouveau test unitaire requis (câblage navigation pur).

## Format

`- [ ] Tnnn [P?] [USMn?] Description avec chemin fichier`

## Dependency graph (Feature M)

```text
T201 → T202 → T203 [US-M1] → T204 [US-M1] → T205 [US-M2] → T206 [P]
```

## Phase 1: Setup (Feature M)

**Purpose**: confirmer l'alignement spec/contract ↔ code de navigation existant.

- [X] T201 Lire `specs/domains/ingredient-health-intelligence/spec.md` (Feature M, **IHI-M-FR-001**..**008**), `contracts/critique-sante-navigation-contract.md`, `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt`, `app/src/main/java/com/miamia/result/LlmResultScreen.kt`, et la section `NavHost` de `app/src/main/java/com/miamia/MainActivity.kt` pour confirmer : route `HealthCritiqueEntry` absente, `HealthCritiqueScreen` non monté en production, `LlmResultScreen` sans bouton « Critique santé ».

---

## Phase 2: Foundational (Feature M)

**Purpose**: introduire la constante de route avant le câblage UI.

- [X] T202 Ajouter la constante `const val HealthCritiqueEntry = "health_critique_entry"` dans `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt` (objet `CameraFlowRoutes`), avec commentaire référençant **IHI-M-FR-001**.

**Checkpoint**: route disponible pour `MainActivity` et `LlmResultScreen`.

---

## Phase 3: User Story M1 — Atteindre la critique santé depuis le résultat composition (P1) 🎯 MVP

**Goal**: un bouton « Critique santé » dans `LlmResultScreen` navigue vers `HealthCritiqueScreen` (route `HealthCritiqueEntry`), activé seulement si un segment validé est disponible.

**Independent test**: parcours quickstart étapes 1–3 — depuis le résultat composition, le bouton est présent, activé avec segment, navigue vers `HealthCritiqueScreen` (liste lecture seule).

### Implementation for US-M1

- [X] T203 [US-M1] Ajouter un paramètre `onCritiqueSante: () -> Unit` à `LlmResultScreen` dans `app/src/main/java/com/miamia/result/LlmResultScreen.kt` ; collecter `viewModel.lastValidatedSegmentForHealth` (StateFlow) via `collectAsState()` ; ajouter un bouton « Critique santé » (test tag `llm_result_critique_sante`) visible à l'état terminal (`Complete`/`Error`), **activé** uniquement si le segment validé est non null/non vide (`IHI-M-FR-002`/`003`), placé au-dessus du bouton « Retour » existant. Le bouton appelle `onCritiqueSante`.

- [X] T204 [US-M1] Câbler la navigation dans `app/src/main/java/com/miamia/MainActivity.kt` : (a) ajouter `composable(CameraFlowRoutes.HealthCritiqueEntry) { HealthCritiqueScreen(healthCritiqueViewModel) }` dans le `NavHost` (`IHI-M-FR-001`/`004`) ; (b) dans `composable(CameraFlowRoutes.LlmResult)`, passer `onCritiqueSante = { cameraNavController.navigate(CameraFlowRoutes.HealthCritiqueEntry) }` à `LlmResultScreen`. Ne pas modifier la route `HealthCritiqueResult` ni le flux `analyze()` (`IHI-M-FR-006`).

**Checkpoint**: US-M1 vert — bouton présent + navigation vers `HealthCritiqueScreen` (liste synchronisée lecture seule).

---

## Phase 4: User Story M2 — Lancer la critique et voir les sections (P1)

**Goal**: confirmer bout-en-bout que « Analyser » depuis `HealthCritiqueScreen` atteint `HealthCritiqueResultScreen` et affiche les sections par population (chaîne pré-existante, désormais atteignable).

**Independent test**: parcours quickstart étapes 4–5 — « Analyser » → `HealthCritiqueResultScreen` affiche ENFANTS / FEMMES ENCEINTES / ADULTES / PERSONNES AGEES.

### Implementation for US-M2

- [X] T205 [US-M2] Vérifier (lecture seule, pas de code à modifier) que la chaîne `viewModel.analyze()` → `navigateToResult` → `cameraNavController.navigate(HealthCritiqueResult)` → `HealthCritiqueResultScreen` fonctionne désormais bout-en-bout depuis `HealthCritiqueScreen` monté en production (T204). Si une régression est détectée sur `HealthCritiqueViewModel.analyze()` ou `navigateToResult`, la corriger sans étendre le scope (la chaîne est pré-existante et devait être intacte — `IHI-M-FR-006`).

**Checkpoint**: US-M2 vert — sections par population affichées après « Analyser ».

---

## Phase 5: Polish & documentation domaine (Feature M)

- [X] T206 [P] Vérifier la cohérence doc domaine (grep sous `specs/domains/ingredient-health-intelligence/`) : `plan.md` Feature M, `research.md` §11, `data-model.md` Feature M, `contracts/critique-sante-navigation-contract.md`, `quickstart.md` Feature M alignés ; exécuter le parcours quickstart Feature M (étapes 1–7).

---

## Parallel execution (Feature M)

- **T203** (`LlmResultScreen.kt`) et **T204** (`MainActivity.kt`) éditent des fichiers distincts mais T204 dépend du nouveau paramètre `onCritiqueSante` introduit par T203 → séquentiel (T203 puis T204).
- **T205** est une vérification (pas d'édit) → après T204.
- **T206** après T205.

## Implementation strategy (Feature M)

- MVP = **T201–T204** (route + bouton + câblage NavHost) ; US-M1 complète et démontrable.
- US-M2 (T205) est une vérification de non-régression de la chaîne pré-existante.
- Non-régression critique : `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, flux composition **non modifiés** (`IHI-M-FR-007`).

## Suggested MVP scope (Feature M)

- **T201–T204** = US-M1 complète (accès UI à la critique santé depuis le résultat composition).
