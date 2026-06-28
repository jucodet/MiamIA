# Tasks: ingredient-health-intelligence

**Domain**: `specs/domains/ingredient-health-intelligence/`
**Dernière feature**: Feature O — critique santé intégrée à l'écran principal des résultats (2026-06-28)

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

---

## Feature N — Critique ciblée par profil utilisateur (2026-06-28)

**Input**: `specs/domains/ingredient-health-intelligence/` (plan.md Feature N, spec.md Feature N, research.md §12, data-model.md Feature N, `contracts/critique-profil-contract.md`)
**Prerequisites**: spec Feature N + clarify 2026-06-28 (5 décisions) intégré en spec

**Tests**: ATDD — au moins une tâche de test par user story (US-N1, US-N2, US-N3, US-N4).

## Format

`- [ ] Tnnn [P?] [USNn?] Description avec chemin fichier`

## Dependency graph (Feature N)

```text
T301 → T302 [US-N1] ──┐
       T303 [P] [US-N2]┤→ T305 → T306 [US-N2] → T307 [US-N3] → T308 [US-N3] → T309 [US-N4] → T310 [US-N4] → T311 [P]
       T304 [P] [US-N2]┘
```

## Phase 1: Setup (Feature N)

**Purpose**: confirmer l'alignement spec/clarify/contract ↔ code critique existant.

- [X] T301 Lire `specs/domains/ingredient-health-intelligence/spec.md` (Feature N, **IHI-N-FR-001**..**016**), `contracts/critique-profil-contract.md`, et `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` + `HealthCritiqueSectionParser.kt` + `HealthCritiqueModels.kt` pour confirmer : format 4-marqueurs courant à superséder, absence de concept profil, restitution UI 4-sections à remplacer.

---

## Phase 2: Foundational (Feature N)

**Purpose**: introduire le contrat profil (enum + provider) avant l'adaptation du builder/parseur.

- [X] T302 [US-N1] Créer `app/src/main/java/com/miamia/healthcritique/UserProfile.kt` : enum `UserProfile` (5 valeurs `FEMME_ENCEINTE`, `ENFANT`, `PERSONNE_AGEE`, `ADULTE`, `SPORTIF`) avec `val label: String` (libellés français : « Femme enceinte », « Enfant », « Agé », « Adulte », « Sportif »), `val marker: String` (`###FEMME_ENCEINTE`, `###ENFANT`, `###PERSONNE_AGEE`, `###ADULTE`, `###SPORTIF`), et `val DEFAULT: UserProfile = ADULTE` (companion). Réf. **IHI-N-FR-001**/**006**.
- [X] T303 [P] [US-N2] Créer `app/src/main/java/com/miamia/healthcritique/UserProfileProvider.kt` : interface `UserProfileProvider { fun current(): UserProfile }` + `class DefaultUserProfileProvider : UserProfileProvider` retournant `UserProfile.DEFAULT` (settable en mémoire via `var override: UserProfile? = null` pour tests). Réf. **IHI-N-FR-001**/**012**.

**Checkpoint**: contrat profil disponible pour builder/engine/viewmodel ; `DEFAULT = ADULTE` (fallback).

---

## Phase 3: User Story N1 — Profil renseigné / contrat profil (P1) 🎯 MVP

**Goal**: le profil utilisateur est modélisé (5 profils + défaut « Adulte ») et consommable via `UserProfileProvider`.

**Independent test**: tests JVM sur `UserProfile` (labels/marqueurs/`DEFAULT`) + `DefaultUserProfileProvider` (fallback `ADULTE`).

### Tests for US-N1 (MANDATORY) ⚠️

- [X] T304 [P] [US-N1] Créer `app/src/test/java/com/miamia/healthcritique/UserProfileTest.kt` : assertions sur les 5 `UserProfile` (label français exact + marker `###...` exact), `UserProfile.DEFAULT == ADULTE` ; `DefaultUserProfileProvider().current() == ADULTE` ; setter `override = FEMME_ENCEINTE` → `current() == FEMME_ENCEINTE`. Test doit passer après T302/T303 (ATDD vert).

### Implementation for US-N1

- *(couvert par T302 + T303)*

**Checkpoint**: US-N1 vert (contrat profil + fallback « Adulte » testés).

---

## Phase 4: User Story N2 — Prompt adapté au profil + rappel « Évalué pour vous » (P1)

**Goal**: `HealthCritiquePromptBuilder.buildSystemInstruction(profile)` exige uniquement le marqueur du profil sélectionné + rappel « Évalué pour vous : <label> », et préserve l'héritage Feature L (persona, dimensions, garde-fous, disclaimer, seuil liste longue).

**Independent test**: tests JVM sur `buildSystemInstruction(profile)` — marqueur unique + rappel + absence des autres marqueurs + blocs prudence/cartes/liste + héritage Feature L.

### Tests for US-N2 (MANDATORY) ⚠️

- [X] T305 [US-N2] Créer `app/src/test/java/com/miamia/healthcritique/HealthCritiqueProfilePromptTest.kt` : pour `FEMME_ENCEINTE` et `SPORTIF`, assertions `contains` sur le rappel « Évalué pour vous : Femme enceinte » / « Évalué pour vous : Sportif », le marqueur du profil (`###FEMME_ENCEINTE` / `###SPORTIF`), et `!contains` sur les **autres** marqueurs (`###ENFANT`, `###ADULTE`, etc.) ; assertions sur les blocs « Niveau de prudence », « Impact », « Fait établi », « Nuance », « Cible particulièrement », « Liste complète des ingrédients analysés » ; héritage Feature L (persona « nutrition clinique »/« cancérologie préventive », « cancérogène »/« mutagène »/« neurotoxique »/« métabolique »/« inflammatoire », « diagnostic »/« prescription », `HealthCritiquePromptBuilder.DISCLAIMER`). Test doit échouer avant l'implémentation (ATDD).

### Implementation for US-N2

- [X] T306 [US-N2] Modifier `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt` : ajouter `fun buildSystemInstruction(profile: UserProfile): String` qui (a) conserve le persona/dimensions/hiérarchie/garde-fous/disclaimer Feature L, (b) exige le rappel « Évalué pour vous : ${profile.label} » en tête, (c) exige **uniquement** le marqueur `${profile.marker}` (plus de 4-marqueurs), (d) exige les blocs Niveau de prudence (Faible/Modéré/Élevé + texte court), cartes d'ingrédients à vigilance (• nom | code | type + Impact/Fait établi/Nuance/Cible particulièrement), liste complète des ingrédients analysés (- nom : RAS/Modéré/Élevé), (e) conserve la gestion liste très longue (`LONG_LIST_INGREDIENT_THRESHOLD`) et langue illisible. Conserver `buildUserMessage` inchangé. Réf. **IHI-N-FR-002**/**003**/**004**/**006**/**007**/**008**/**011**.

**Checkpoint**: US-N2 vert (prompt profil unique + rappel + blocs + héritage Feature L).

---

## Phase 5: User Story N3 — Niveau de prudence parsé + rejet 4-marqueurs (P1)

**Goal**: le parseur extrait le Niveau de prudence, les cartes d'ingrédients et la liste compacte depuis une sortie profil unique ; il rejette une sortie 4-marqueurs legacy.

**Independent test**: tests JVM sur `HealthCritiqueSectionParser` — extraction (marqueur unique, prudence, cartes, liste compacte) + rejet 4-marqueurs.

### Tests for US-N3 (MANDATORY) ⚠️

- [X] T307 [US-N3] Étendre `app/src/test/java/com/miamia/healthcritique/HealthCritiqueSectionParserTest.kt` : (a) sur une sortie profil unique (rappel « Évalué pour vous : Adulte » + `###ADULTE` + `Niveau de prudence : Modéré — …` + une carte `• Nitrite de sodium | E250 | Conservateur — Additif` avec sous-lignes + `Liste complète…` avec `- Farine : RAS`), vérifier `prudenceLevel == MODERE`, `riskCards.size == 1` (nom « Nitrite de sodium », code « E250 », type « Conservateur — Additif », champs non vides), `fullIngredientList` contient « Farine »/`RAS` ; (b) sur une sortie 4-marqueurs (`###ENFANTS`…`###PERSONNES_AGEES`), vérifier le rejet (warning + `isRejectedLegacy4Markers == true`). Test doit échouer avant l'implémentation (ATDD).

### Implementation for US-N3

- [X] T308 [US-N3] Réécrire `app/src/main/java/com/miamia/healthcritique/HealthCritiqueSectionParser.kt` : remplacer le parse 4-sections par un parse **profil unique** — détecter le marqueur canonique présent (parmi les 5 `UserProfile.marker`), extraire `Niveau de prudence : <palier> — <texte>` (regex), les cartes (blocs `• <nom> | <code> | <type>` + sous-lignes `Impact :`/`Fait établi :`/`Nuance :`/`Cible particulièrement :`), et la liste compacte (`- <nom> : <RAS|Modéré|Élevé>`) ; retourner un `ProfileCritiqueResult` (voir T309) ; si **plus d'un** marqueur de profil est présent (sortie 4-marqueurs legacy), marquer `isRejectedLegacy4Markers = true` + warning (→ `non-analysable-response` côté engine). Réf. **IHI-N-FR-013**/**IHI-N-SC-009**.

**Checkpoint**: US-N3 vert (parse profil unique + rejet 4-marqueurs).

---

## Phase 6: User Story N4 — Restitution UI (jauge + cartes filtrées + « Voir tous ») (P1)

**Goal**: l'écran de résultat affiche le rappel « Évalué pour vous », la jauge de Niveau de prudence, les cartes d'ingrédients à vigilance (filtrées), et le bouton « Voir tous les ingrédients analysés » (liste compacte) ; fallback « profil par défaut » signalé.

**Independent test**: parcours quickstart Feature N étapes 1–6 + tests engine/viewmodel (profile transmis, fallback ADULTE).

### Tests for US-N4 (MANDATORY) ⚠️

- [X] T309 [US-N4] Étendre `app/src/test/java/com/miamia/healthcritique/HealthCritiqueEngineTest.kt` (ou nouveau test) : avec un `FakeHealthCritiqueLlmRunner` retournant une sortie profil unique (rappel + `###ADULTE` + prudence + cartes + liste), `engine.analyze(ingredientText, profile = ADULTE)` retourne `CritiqueReady` avec `profileCritique` (prudence/cards/liste remplis) ; avec une sortie 4-marqueurs, `engine.analyze` retourne `InferenceError` (non-analysable). Test doit échouer avant l'implémentation (ATDD).

### Implementation for US-N4

- [X] T310 [US-N4] Câbler le profil dans `app/src/main/java/com/miamia/healthcritique/HealthCritiqueModels.kt` + `HealthCritiqueEngine.kt` + `HealthCritiqueViewModel.kt` + `HealthCritiqueResultScreen.kt` :
  - `HealthCritiqueModels.kt` : ajouter `PrudenceLevel` (FAIBLE/MODERE/ELEVE + label), `IngredientRiskCard`, `FullIngredientStatutEntry` (+ enum `IngredientVigilanceStatut RAS/MODERE/ELEVE`), `ProfileCritiqueResult` ; étendre `HealthCritiqueResult.CritiqueReady` avec `profileCritique: ProfileCritiqueResult` (ou remplacer `sections`).
  - `HealthCritiqueEngine.kt` : `analyze(ingredientText, profile: UserProfile, …)` — `promptBuilder.buildSystemInstruction(profile)` ; sur succès, construire `ProfileCritiqueResult` (rappel + `isDefaultProfile = (profile == DEFAULT && provider signale défaut)` — MVP : `isDefaultProfile = (profile == UserProfile.DEFAULT)`), et rejeter (InferenceError `INFERENCE_FAILED`) si `isRejectedLegacy4Markers`. Conserver l'ancrage E-numbers (`HealthCritiqueAnchoring`).
  - `HealthCritiqueViewModel.kt` : accepter un `UserProfileProvider` (factory) ; `analyze()` lit `provider.current()` et le passe à `engine.analyze` ; exposer `profile`/`isDefaultProfile` dans `HealthCritiqueScreenState`.
  - `HealthCritiqueResultScreen.kt` : remplacer le rendu 4-sections par — rappel « Évalué pour vous : <label> » (+ badge « profil par défaut » si applicable), jauge 3 paliers (Faible/Modéré/Élevé) + texte court, cartes `IngredientRiskCard` (accordéon repliable, test tag `health_result_card_<nom>`), bouton « Voir tous les ingrédients analysés » (test tag `health_result_show_all`) déploiant la liste compacte (nom + statut). Conserver disclaimer + boutons Copier/Retour.
  Réf. **IHI-N-FR-009**/**010**/**011**/**012** + **IHI-N-SC-004**/**005**/**006**/**008**.

**Checkpoint**: US-N4 vert (restitution profil unique + jauge + cartes filtrées + « Voir tous » + fallback signalé).

---

## Phase 7: Polish & documentation domaine (Feature N)

- [X] T311 [P] Vérifier la cohérence doc domaine (grep sous `specs/domains/ingredient-health-intelligence/`) : `plan.md` Feature N, `research.md` §12, `data-model.md` Feature N, `contracts/critique-profil-contract.md`, `quickstart.md` Feature N alignés ; exécuter le parcours quickstart Feature N (tests JVM `UserProfileTest` + `HealthCritiqueProfilePromptTest` + `HealthCritiqueSectionParserTest` étendu + `HealthCritiqueEngineTest` étendu). Confirmer qu'aucune doc ne cite le format 4-marqueurs comme actif pour le flux critique (supersession tracée).

---

## Parallel execution (Feature N)

- **T302** (`UserProfile.kt`) et **T303** (`UserProfileProvider.kt`) éditent des fichiers distincts → parallèle [P].
- **T304** (`UserProfileTest.kt`, fichier distinct) → parallèle avec T305 (`HealthCritiqueProfilePromptTest.kt`).
- **T305** et **T307** éditent des fichiers de test distincts → parallèle [P].
- **T306** édite `HealthCritiquePromptBuilder.kt` ; **T308** édite `HealthCritiqueSectionParser.kt` (fichiers distincts) mais T308 dépend conceptuellement du format défini en T306 → séquentiel (T306 puis T308).
- **T309** (test engine) puis **T310** (models+engine+viewmodel+screen) → séquentiel.
- **T311** après T310.

## Implementation strategy (Feature N)

- MVP = **T301–T306** (contrat profil + prompt profil unique + rappel) ; US-N1 + US-N2 démontrables et testables indépendamment (tests JVM).
- Incrémental : US-N3 (T307–T308, parseur) puis US-N4 (T309–T310, restitution) s'ajoutent sans casser US-N1/N2.
- Non-régression critique : Feature C (ancrage) préservé ; flux composition et `additive-risk-insights` non modifiés (les « alertes » sont les KPI existants juxtaposés via **IHI-C-FR-007**).
- Supersession : le format 4-marqueurs Feature L est **retiré** (parseur le rejette) — `IHI-L-FR-009`/`IHI-L-SC-004` supersédés (traçabilité en spec).

## Suggested MVP scope (Feature N)

- **T301–T306** = US-N1 + US-N2 complètes (contrat profil + prompt ciblé + rappel « Évalué pour vous »).

---

## Feature O — Critique santé intégrée à l'écran principal des résultats (2026-06-28)

**Input**: `specs/domains/ingredient-health-intelligence/` (plan.md Feature O, spec.md Feature O, research.md §13, data-model.md Feature O, `contracts/critique-inline-restitution-contract.md`)
**Prerequisites**: spec Feature O + clarify 2026-06-28 (2 décisions : déclenchement automatique + suppression écrans/route séparés) intégré en spec ; Feature N implémentée (restitution profil + cartes + jauge).

**Tests**: ATDD — tests instrumentés adaptés + parcours quickstart Feature O (auto-trigger, restitution inline, états erreur/chargement, suppression navigation, non-régression composition).

## Format

`- [ ] Tnnn [P?] [USOn?] Description avec chemin fichier`

## Dependency graph (Feature O)

```text
T401 → T402 [US-O1] → T403 [US-O2] → T404 [US-O2] → T405 [US-O3] → T406 [US-O4] → T407 [P] → T408 [P]
```

## Phase 1: Setup (Feature O)

**Purpose**: confirmer l'alignement spec/contract ↔ code existant (Feature M à superséder + Feature N à réutiliser).

- [X] T401 Lire `specs/domains/ingredient-health-intelligence/spec.md` (Feature O, **IHI-O-FR-001**..**014**), `contracts/critique-inline-restitution-contract.md`, et `app/src/main/java/com/miamia/result/LlmResultScreen.kt` + `app/src/main/java/com/miamia/MainActivity.kt` (section `NavHost`) + `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt` + `app/src/main/java/com/miamia/healthcritique/HealthCritiqueResultScreen.kt` + `HealthCritiqueViewModel.kt` pour confirmer : bouton `llm_result_critique_sante` + routes `HealthCritiqueEntry`/`HealthCritiqueResult` + écrans séparés à supprimer ; composables de restitution (`CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem`, `FullIngredientListToggle`) à extraire ; états ViewModel (`ui`, `streamingText`, `isLoading`, `result`, `analyze()`).

---

## Phase 2: Foundational (Feature O)

**Purpose**: préparer la section critique inline (extraction des composables de restitution) avant le câblage auto-trigger.

- [X] T402 [US-O1] Extraire les composables de restitution de `app/src/main/java/com/miamia/healthcritique/HealthCritiqueResultScreen.kt` (`CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem`, `FieldLine`, `FullIngredientListToggle`, et les actions « Copier la réponse » / « Copier le prompt » via `HealthCritiqueClipboard`) vers un composable partagé `InlineCritiqueSection` (ex. `app/src/main/java/com/miamia/healthcritique/InlineCritiqueSection.kt` ou module `result`) consommant `HealthCritiqueViewModel` (collecte `ui` + `streamingText`) — restitution 100 % inline, aucune navigation (`IHI-O-FR-002`/`005`/`006`).

**Checkpoint**: composable `InlineCritiqueSection` réutilisable, rendant les états `en cours` / `erreur` / `prête` (rappel + jauge + cartes filtrées + « Voir tous » + disclaimers + actions copier).

---

## Phase 3: User Story O1 — Voir la critique santé sur l'écran principal (P1) 🎯 MVP

**Goal**: la section « Critique santé » s'affiche inline sur `LlmResultScreen`, en continuité sous le bilan composition / pastille kcal / KPI additifs, sans bouton ni navigation.

**Independent test**: parcours quickstart étapes 1–3 — bilan `Complete` + segment validé → section critique visible inline sur `LlmResultScreen` ; aucun bouton `llm_result_critique_sante` ; aucune route `HealthCritiqueEntry`/`HealthCritiqueResult`.

### Implementation for US-O1

- [X] T403 [US-O1] Intégrer `InlineCritiqueSection(viewModel = healthCritiqueViewModel, modifier = ...)` dans `app/src/main/java/com/miamia/result/LlmResultScreen.kt`, en continuité sous `BilanResultCard` (état `Complete`), en ordonnancement : bilan → pastille kcal → KPI additifs juxtaposés → section critique inline (`IHI-O-FR-002`/`012`). Supprimer le bouton « Critique santé » (callback `onCritiqueSante`, test tag `llm_result_critique_sante`, `critiqueEnabled`) — `IHI-O-FR-003` (supersede `IHI-M-FR-002`). Injecter `HealthCritiqueViewModel` en paramètre de `LlmResultScreen` (à la place de `onCritiqueSante`).

**Checkpoint**: US-O1 vert — section critique inline rendue (état `prête` si ViewModel déjà peuplé) ; bouton supprimé.

---

## Phase 4: User Story O2 — Déclenchement automatique (P1)

**Goal**: la critique se lance automatiquement au `Complete` + segment validé disponible, sans action utilisateur.

**Independent test**: parcours quickstart étapes 1–2 + 9 — `analyze()` déclenché automatiquement ; état `en cours` puis `prête` inline ; idempotence (un `Complete` → une inférence).

### Implementation for US-O2

- [X] T404 [US-O2] Ajouter le déclenchement automatique dans `app/src/main/java/com/miamia/result/LlmResultScreen.kt` via `LaunchedEffect(streamingBilan, validatedSegment)` : si `streamingBilan is StreamingBilanState.Complete` **et** `validatedSegment` non null/non vide → `healthCritiqueViewModel.analyze()` (`IHI-O-FR-001`). Garder l'idempotence via l'état du ViewModel (pas de double `analyze()` pour un même `Complete` — `IHI-O-FR-013`). Ne pas déclencher si `Error` ou segment vide (`IHI-O-FR-010`).
- [X] T405 [US-O2] Câbler l'injection du `HealthCritiqueViewModel` et la synchronisation du segment dans `app/src/main/java/com/miamia/MainActivity.kt` : passer `healthCritiqueViewModel` à `LlmResultScreen` dans `composable(CameraFlowRoutes.LlmResult)` ; conserver le `LaunchedEffect` existant `lastValidatedSegmentForHealth → setValidatedSegmentFromScan(...)` (`IHI-O-FR-008`). Retirer le `LaunchedEffect(healthCritiqueViewModel)` qui collectait `navigateToResult` (plus de navigation vers `HealthCritiqueResult`).

**Checkpoint**: US-O2 vert — auto-trigger au `Complete` + segment ; streaming inline ; idempotent.

---

## Phase 5: User Story O3 — États d'erreur et de chargement inline (P2)

**Goal**: les états `en cours` / `erreur` sont rendus inline sans casser le bilan composition.

**Independent test**: parcours quickstart étapes 5–7 — erreur d'inférence rendue inline (bilan intact au-dessus) ; bilan `Error` ou segment vide → critique non déclenchée.

### Implementation for US-O3

- [X] T406 [US-O3] Vérifier dans `app/src/main/java/com/miamia/healthcritique/InlineCritiqueSection.kt` (extrait en T402) que les états `isLoading` + `streamingText` (en cours), `InferenceError` / `InputInvalid` (erreur) sont rendus inline ; que l'erreur critique n'affecte pas le bilan composition affiché au-dessus dans `LlmResultScreen.kt` (`IHI-O-FR-006`). Compléter si besoin (ex. masquer la section critique si pas de segment / bilan `Error`).

**Checkpoint**: US-O3 vert — états inline robustes ; bilan composition intact en cas d'erreur critique.

---

## Phase 6: User Story O4 — Suppression de la navigation séparée (P1)

**Goal**: retrait des écrans/route séparés ; pile de navigation simplifiée ; retour direct au scan.

**Independent test**: parcours quickstart étapes 3 + 8 — aucune route `HealthCritiqueEntry`/`HealthCritiqueResult` ; `HealthCritiqueScreen` / `HealthCritiqueResultScreen` supprimés ; « Retour » ramène au scan.

### Implementation for US-O4

- [X] T407 [US-O4] Supprimer les routes et entrées `NavHost` dans `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt` (retirer `HealthCritiqueEntry` et `HealthCritiqueResult`) et dans `app/src/main/java/com/miamia/MainActivity.kt` (retirer `composable(HealthCritiqueEntry)` et `composable(HealthCritiqueResult)`) (`IHI-O-FR-004`/`005`, supersede `IHI-M-FR-001`/`006`). Supprimer les fichiers `app/src/main/java/com/miamia/healthcritique/HealthCritiqueScreen.kt` et `app/src/main/java/com/miamia/healthcritique/HealthCritiqueResultScreen.kt` (leurs composables de restitution ont été extraits en T402) (`IHI-O-FR-004`/`005`). Retirer l'import/usage de `navigateToResult` dans `HealthCritiqueViewModel.kt` si devenu mort (ou marquer no-op). S'assurer que `onBack` / `popBackStack` depuis `LlmResultScreen` ramène au scan sans écran intermédiaire (`IHI-O-FR-014`).

**Checkpoint**: US-O4 vert — navigation séparée supprimée ; retour direct au scan.

---

## Phase 7: Polish & documentation domaine (Feature O)

- [X] T408 [P] Vérifier la cohérence doc domaine (grep sous `specs/domains/ingredient-health-intelligence/`) : `plan.md` Feature O, `research.md` §13, `data-model.md` Feature O, `contracts/critique-inline-restitution-contract.md`, `quickstart.md` Feature O alignés ; `contracts/critique-sante-navigation-contract.md` marqué **SUPERSÉDÉ** ; exécuter le parcours quickstart Feature O. Adapter les tests instrumentés existants (`LlmResultScreenUiTest.kt`, `HealthCritiqueReadOnlySegmentAndroidTest.kt`, `HealthCritiquePersistenceAndroidTest.kt`) au nouveau câblage inline (auto-trigger, suppression navigation) ; confirmer qu'aucune référence active à `HealthCritiqueScreen` / `HealthCritiqueEntry` / `HealthCritiqueResult` / `onCritiqueSante` ne subsiste dans le code de production.

---

## Parallel execution (Feature O)

- **T402** (extraction `InlineCritiqueSection`) puis **T403** (`LlmResultScreen`) → séquentiel (T403 dépend du composable extrait).
- **T404** + **T405** éditent `LlmResultScreen.kt` / `MainActivity.kt` (fichiers distincts) mais T405 dépend de l'injection introduite par T403/T404 → séquentiel.
- **T406** (vérification/complétion `InlineCritiqueSection`) après T402.
- **T407** (suppression routes/écrans) après T403 (le bouton doit déjà être retiré) ; édite `CameraFlowRoutes.kt` + `MainActivity.kt` + suppressions fichiers → séquentiel.
- **T408** (polish/tests/doc) après T407 → parallèle [P] sur doc vs tests instrumentés.

## Implementation strategy (Feature O)

- MVP = **T401–T405** (extraction + section inline + auto-trigger + injection) ; US-O1 + US-O2 démontrables et testables indépendamment (parcours quickstart étapes 1–2 + 9).
- Incrémental : US-O3 (T406, états inline robustes) puis US-O4 (T407, suppression navigation) s'ajoutent sans casser US-O1/O2.
- Non-régression critique : `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser`, flux composition, KPI additifs juxtaposés (`IHI-C-FR-007`) **inchangés** (`IHI-O-FR-007` / `IHI-O-SC-005`).
- Supersession : Feature M (`IHI-M-FR-001`..`008`, `IHI-M-SC-001`..`005`) **retirée** — traçabilité en spec/plan/contract.

## Suggested MVP scope (Feature O)

- **T401–T405** = US-O1 + US-O2 complètes (section critique inline sur `LlmResultScreen` + déclenchement automatique).

---

# Feature P — Compte rendu restructuré (4 sections) + critique concise/visuelle par profil (2026-06-28)

**Spec**: spec.md Feature P | **Plan**: plan.md Feature P | **Contract**: contracts/report-layout-contract.md

## Phase 8: User Story P1 — Compte rendu en 4 sections ordonnées fixes (P1)

**Goal**: `LlmResultScreen` / `BilanResultCard` expose exactement 4 sections ordonnées (Produit identifié → Synthèse → Verdict par ingrédient → Critique santé), stables跨-états.

**Independent test**: parcours quickstart étapes 1–2 + 9 — 4 sections dans l'ordre ; états neutres produit/verdict ; stabilité跨-états.

### Implementation for US-P1

- [X] T501 [US-P1] Rendre `ProductSection` **inconditionnel** dans `app/src/main/java/com/miamia/camera/BilanResultCard.kt` : toujours rendre la section ; si `bilan.identifiedProduct` est blank/null, afficher un état neutre « Produit non identifié » (texte `onSurfaceVariant`) au lieu de skipper la section (`IHI-P-FR-001`, edge case « Produit non identifié »).
- [X] T502 [US-P1] Rendre `HealthImpactSection` **inconditionnelle** dans `app/src/main/java/com/miamia/camera/BilanResultCard.kt` : toujours rendre la section ; si `bilan.healthImpacts` est vide, afficher un état neutre « Aucun ingrédient à vigilance identifié » (texte `onSurfaceVariant`) au lieu de skipper (`IHI-P-FR-001`, edge case « Aucun ingrédient à vigilance »).
- [X] T503 [US-P1] Réordonner `BilanResultCard` pour exposer les 3 sections internes dans l'ordre **Produit identifié → Synthèse → Verdict par ingrédient** (en supprimant l'appel `IngredientsSection` — cf. US-P2) dans `app/src/main/java/com/miamia/camera/BilanResultCard.kt` ; conserver `BilanHeader`, `DisclaimerSection`, `InferenceTimeBadge`, `RawTranscriptToggle` comme méta hors-sections (`IHI-P-FR-001` / `IHI-P-SC-001`).

**Checkpoint**: US-P1 vert — 3 sections internes (Produit, Synthèse, Verdict) inconditionnelles et ordonnées ; la 4ᵉ (Critique) est rendue par `InlineCritiqueSection` en continuité (Feature O).

## Phase 9: User Story P2 — Suppression de la liste brute des ingrédients identifiés (P1)

**Goal**: plus d'affichage à plat du `ValidatedIngredientSegment` (section Complete + carte streaming) ; ancrage Feature C préservé (segment reste entrée d'analyse).

**Independent test**: parcours quickstart étape 3 — `bilan_ingredients_section` et `streaming_ingredients_card` absents ; verdict + bouton « Voir tous les ingrédients analysés » restent disponibles.

### Implementation for US-P2

- [X] T504 [US-P2] Supprimer l'appel `IngredientsSection(bilan.ingredientLines)` et la fonction `IngredientsSection` dans `app/src/main/java/com/miamia/camera/BilanResultCard.kt` (test tag `bilan_ingredients_section` retiré — `IHI-P-FR-002` / `IHI-P-SC-002`). Vérifier que `bilan.ingredientLines` n'est plus lu dans `BilanResultCard` (le segment reste utilisé en amont pour l'analyse, ancrage Feature C inchangé).
- [X] T505 [US-P2] Supprimer la carte streaming « Ingrédients identifiés » (bloc `AnimatedVisibility` sur `state.partialIngredients` avec test tag `streaming_ingredients_card`) dans `StreamingContent` de `app/src/main/java/com/miamia/result/LlmResultScreen.kt` (`IHI-P-FR-002` / `IHI-P-SC-002`). Conserver les cartes streaming « Produit identifié », « Synthèse », « Verdict par ingrédient » (alignement 4-sections pendant le streaming).

**Checkpoint**: US-P2 vert — aucune liste brute d'ingrédients à plat sur le compte rendu ; verdict + liste compacte (Feature N) toujours accessibles.

## Phase 10: User Story P-intégration — Synthèse absorbe kcal + KPI additifs (P1)

**Goal**: la section « Synthèse » intègre la pastille kcal (Feature K) et le panneau KPI additifs (`AdditiveKpiPanel`) ; la section « Additifs » autonome disparaît.

**Independent test**: parcours quickstart étape 4 — Synthèse contient pastille kcal + KPI additifs ; `bilan_additives_section` absent.

### Implementation for US-P-intégration

- [X] T506 [US-P-intégration] Fusionner la `CompositionEnergyPastille` et l'`AdditivesSection` dans la section **Synthèse** de `app/src/main/java/com/miamia/camera/BilanResultCard.kt` : étendre `AnalysisSection` (ou introduire une `SyntheseSection`) pour contenir (a) la `CompositionEnergyPastille` en tête, (b) le texte d'analyse `bilan.compositionAnalysis`, (c) le `AdditiveKpiPanel` (si `additiveKpi != null`, attribution `IHI-C-FR-007` préservée via `onRequestShowRaw`). Retirer l'appel autonome `CompositionEnergyPastille(...)` et `AdditivesSection(...)` hors Synthèse ; supprimer la fonction `AdditivesSection` devenue section autonome (le `AdditiveKpiPanel` reste utilisé dans Synthèse) (`IHI-P-FR-003` / `IHI-P-SC-003`). Le `onToggleRaw` reste câblé au `RawTranscriptToggle` (inchangé) et à `AdditiveKpiPanel.onRequestShowRaw`.

**Checkpoint**: US-P-intégration vert — Synthèse agrège kcal + analyse + KPI additifs ; plus de section « Additifs » autonome.

## Phase 11: User Story P3 — Critique santé concise/visuelle, risques profil en tête (P1)

**Goal**: la critique (section 4) met en évidence les risques spécifiques au profil sélectionné via des pastilles visuelles courtes en tête, avant la jauge ; cartes détaillées en repli.

**Independent test**: parcours quickstart étapes 5–7 — pastilles risques profil entre « Évalué pour vous » et la jauge ; ancrage respecté ; cartes repliables.

### Implementation for US-P3

- [X] T507 [US-P3] Ajouter un composable `ProfileRiskHighlights(cards: List<IngredientRiskCard>)` dans `app/src/main/java/com/miamia/healthcritique/InlineCritiqueSection.kt` : rend une ligne/ligne-flux de **pastilles colorées courtes** (une par carte — `nom` + `code` éventuel + marqueur de sévérité visuel dérivé du niveau de vigilance Modéré/Élevé). Si `cards.isEmpty()`, pastille neutre « Aucun risque marqué pour votre profil ». Test tag `health_result_risk_highlights` (`IHI-P-FR-005` / `IHI-P-SC-004`).
- [X] T508 [US-P3] Insérer `ProfileRiskHighlights(critique.riskCards)` dans `CritiqueProfileContent` **entre** le rappel `evaluatedForHeader` (+ signal « profil par défaut ») et la `PrudenceGauge`, avant le bloc narratif/carte (`IHI-P-FR-005`/`007`). S'assurer que les cartes `IngredientRiskCardItem` restent **repliables par défaut** (profondeur non dominante — `IHI-P-FR-006` / `IHI-P-SC-006`, inchangé).

**Checkpoint**: US-P3 vert — risques profil mis en évidence en tête de critique ; profondeur conservée en repli.

## Phase 12: Polish & documentation domaine (Feature P)

- [X] T509 [P] Vérifier la cohérence doc domaine (grep sous `specs/domains/ingredient-health-intelligence/`) : `plan.md` Feature P, `research.md` §14, `data-model.md` Feature P, `contracts/report-layout-contract.md`, `quickstart.md` Feature P alignés avec la spec Feature P (`IHI-P-FR-001`..`012` / `IHI-P-SC-001`..`010`) ; exécuter le parcours quickstart Feature P. Confirmer qu'aucune référence active à `IngredientsSection` / `bilan_ingredients_section` / `streaming_ingredients_card` / `AdditivesSection` (comme section autonome) ne subsiste dans le code de production.
- [X] T510 [P] Adapter les tests instrumentés existants dans `app/src/androidTest/java/com/miamia/result/LlmResultScreenUiTest.kt` (et `app/src/androidTest/java/com/miamia/healthcritique/HealthCritiqueReadOnlySegmentAndroidTest.kt` si besoin) : (a) retirer les assertions sur `bilan_ingredients_section` / `streaming_ingredients_card`, (b) ajouter l'attente des **4 sections** ordonnées (Produit, Synthèse, Verdict, Critique inline), (c) ajouter l'attente de `health_result_risk_highlights` (pastilles risques profil), (d) vérifier l'absence de `bilan_additives_section` (fusion Synthèse). Confirmer la non-régression composition/critique (Feature O/N inchangées).

---

## Parallel execution (Feature P)

- **T501** + **T502** + **T504** + **T505** éditent des sections/fichiers distincts mais convergent vers `BilanResultCard.kt` / `LlmResultScreen.kt` → coordonner séquentiellement sur le même fichier (T501/T502/T504 sur `BilanResultCard.kt` ; T505 sur `LlmResultScreen.kt`).
- **T503** (réordonnancement) après T501/T502/T504 (sections inconditionnelles + IngredientsSection retirée).
- **T506** (fusion Synthèse) après T503 (ordre établi) — édite `BilanResultCard.kt` → séquentiel.
- **T507** + **T508** sur `InlineCritiqueSection.kt` → séquentiel (T508 dépend du composable T507).
- **T509** (doc) + **T510** (tests) → parallèle [P] après T503–T508.

## Implementation strategy (Feature P)

- MVP = **T501–T506** (4 sections ordonnées + suppression liste brute + fusion kcal/KPI dans Synthèse) ; US-P1 + US-P2 + US-P-intégration démontrables et testables indépendamment (parcours quickstart étapes 1–4 + 9).
- Incrémental : US-P3 (T507–T508, critique concise/visuelle) s'ajoute sans casser US-P1/P2/P-intégration.
- Non-régression critique : `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser`, flux composition, `AdditiveKpiPanel` (`additive-risk-insights`), `CompositionEnergyPastille` (Feature K), déclenchement automatique + restitution inline (Feature O) **inchangés** (`IHI-P-FR-008`/`009`/`012`).

## Suggested MVP scope (Feature P)

- **T501–T506** = US-P1 + US-P2 + US-P-intégration complètes (4 sections ordonnées + suppression liste brute + Synthèse agrégée).
