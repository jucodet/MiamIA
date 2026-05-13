# Tasks: Feature K — plage kcal/100 g (1..1100)

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
