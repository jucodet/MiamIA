# Implementation Plan: ingredient-health-intelligence — Feature K (bornes kcal/100 g)

**Branch**: `016-launch-splash-screen` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md)  
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
