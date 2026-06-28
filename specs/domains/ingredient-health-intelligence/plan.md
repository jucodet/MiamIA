# Implementation Plan: ingredient-health-intelligence

**Domain**: `specs/domains/ingredient-health-intelligence` | **Spec**: [spec.md](./spec.md)
**Dernière feature planifiée**: Feature L — personnalisation du prompt de critique santé (2026-06-28)

> Plan cumulatif par feature. Sections historiques conservées pour traçabilité (constitution I).

---

## Feature K — Pastille kcal / 100 g (2026-05-13)

**Branch**: `016-launch-splash-screen` | **Date**: 2026-05-13  
**Input**: Spécification domaine + **clarify** 2026-05-13 (plage d’affichage **Option B** : **1 ≤ N ≤ 1100** kcal/100 g)

## Summary

La **Feature K** expose une pastille d’estimation énergétique (kcal/100 g) en tête du bilan composition (**Ref.** **UGE-A-FR-022**). Le code existant applique `EnergyEstimateValidator` avec une plage **1..950** ; la spec et le **clarify** imposent désormais **1..1100** (huiles et produits très denses). Ce plan : aligner le validateur, les tests JVM, et la documentation d’ingénierie (`research.md`, `data-model.md`, `quickstart.md`) sur **IHI-K-FR-006** / **IHI-K-SC-002**.

## Technical Context

**Language/Version**: Kotlin 2.x, Android (API min projet)  
**Primary Dependencies**: Jetpack Compose, module `composition`, UI résultat sous `camera` (`BilanResultCard`)  
**Storage**: N/A (valeur sur `CompositionBilan` en mémoire)  
**Testing**: JUnit 4, `app/src/test/java/com/miamia/composition/`  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: Aucun objectif nouveau ; parsing inchangé  
**Constraints**: Constitution ATDD ; pas de nombre trompeur hors plage (**US-K2**)  
**Scale/Scope**: `EnergyEstimateValidator.kt`, tests associés, docs domaine

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec + tests bornes |
| II. ATDD | OK — tests `EnergyEstimateValidatorTest` mis à jour |
| III. UX | OK — pastille inchangée, garde-fous renforcés |
| IV. Performance | OK — comparaison entière |
| V. Simplicité | OK — une constante de plage |
| VI. DDD | OK — IHI fournit la valeur ; UGE consomme (**contrat** `composition-energy-read-model.md`) |

**Post-design** : inchangé.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── composition-energy-read-model.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/composition/
├── EnergyEstimateValidator.kt
├── GemmaBilanParser.kt
└── CompositionResultValidator.kt

app/src/test/java/com/miamia/composition/
└── EnergyEstimateValidatorTest.kt

app/src/main/java/com/miamia/camera/
├── BilanResultCard.kt
└── CompositionEnergyUiStrings.kt
```

**Structure Decision** : ajustement local au validateur et aux tests ; UI et parseur inchangés sauf cohérence des commentaires si besoin.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0 — Recherche

Voir [research.md](./research.md) §9 (bornes **1..1100** — décision clarify **Option B**).

## Phase 1 — Design

- [data-model.md](./data-model.md) : `EstimatedEnergyPer100g` — plage **1..1100**.  
- [contracts/composition-energy-read-model.md](./contracts/composition-energy-read-model.md) : rappel borne côté IHI.  
- [quickstart.md](./quickstart.md) : scénario manuel hors plage aligné spec.

## Phase 2 — Livraison (hors scope de ce fichier)

Les tâches exécutables sont dans [tasks.md](./tasks.md) (commande `/speckit-tasks`).

---

## Feature L — Personnalisation du prompt de critique santé (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28  
**Input**: spec.md Feature L + **clarify** 2026-06-28 (5 questions résolues — remplacement en dur, vigilance transversale, critique seule, seuil en nombre d'ingrédients, relecture humaine MVP)

### Summary

La **Feature L** remplace le contenu du prompt de critique santé construit par `HealthCritiquePromptBuilder` : persona expert (nutrition clinique + cancérologie préventive), 5 dimensions de risque par ingrédient, hiérarchie faits établis / incertitudes / hypothèses (réf. CIRC/OMS), populations vulnérables élargies (immunodéprimées, antécédents familiaux cancer) en vigilance transversale, garde-fous éthiques renforcés, et format de sortie strict préservé (4 marqueurs + 3 blocs). Périmètre **critique seule** (bilan composition non modifié). Mécanisme : **remplacement en dur versionné** (pas d'externalisation).

### Technical Context

**Language/Version**: Kotlin 2.x, Android (API min projet)  
**Primary Dependencies**: module `healthcritique` existant (`HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, `HealthCritiqueConfig`)  
**Storage**: N/A (prompt construit en mémoire)  
**Testing**: JUnit 4, `app/src/test/java/com/miamia/healthcritique/` (`HealthCritiquePromptPrudenceTest`, `HealthCritiqueSectionParserTest`)  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: aucun objectif nouveau ; construction de prompt négligeable  
**Constraints**: Constitution ATDD ; non-régression du parseur de sections (`IHI-L-SC-005`) ; conformité Feature C préservée (`IHI-L-FR-014`)  
**Scale/Scope**: `HealthCritiquePromptBuilder.kt`, tests prudence/sections, docs domaine

### Constitution Check (Feature L)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature L + clarify → tests prudence/sections → code |
| II. ATDD | OK — tests builder (persona/dimensions/hiérarchie) + parser (4 marqueurs) avant/avec impl |
| III. UX | OK — format de sortie inchangé, non-régression UI |
| IV. Performance | OK — construction de prompt négligeable |
| V. Simplicité | OK — remplacement en dur, pas d'externalisation (clarify Q1) |
| VI. DDD | OK — Feature L cantonnée à critique (IHI), pas de fuite vers composition (clarify Q3) |

**Post-design** : inchangé.

### Project Structure (Feature L)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature L)
├── research.md (§10 Feature L)
├── data-model.md (entités Feature L)
├── quickstart.md (parcours Feature L)
├── contracts/
│   └── critique-prompt-contract.md
└── tasks.md (section Feature L)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/healthcritique/
├── HealthCritiquePromptBuilder.kt   # remplacement du contenu du prompt
└── HealthCritiqueConfig.kt          # seuil "liste très longue" (LONG_LIST_INGREDIENT_THRESHOLD)

app/src/test/java/com/miamia/healthcritique/
├── HealthCritiquePromptPrudenceTest.kt       # étendu : persona + 5 dimensions + populations vulnérables
└── HealthCritiqueSectionParserTest.kt        # non-régression : 4 marqueurs ordonnés inchangés
```

**Structure Decision** : ajustement local au builder (contenu du prompt) + une constante de seuil ; parseur, engine, UI et flux composition inchangés.

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature L)

Voir [research.md](./research.md) §10 (décisions clarify 2026-06-28 : mécanisme en dur, vigilance transversale, périmètre critique, seuil en nombre d'ingrédients, validation MVP).

### Phase 1 — Design (Feature L)

- [data-model.md](./data-model.md) : entités Feature L — `HealthCritiquePrompt`, `RiskDimension`, `EvidenceTier`, `VulnerablePopulation`, `CritiqueSectionMarker`.
- [contracts/critique-prompt-contract.md](./contracts/critique-prompt-contract.md) : contrat de contenu du prompt de critique (persona, dimensions, hiérarchie, format de sortie strict).
- [quickstart.md](./quickstart.md) : parcours manuel de relecture (jeu fixe) aligné `IHI-L-SC-008`.

### Phase 2 — Livraison (Feature L)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature L).
