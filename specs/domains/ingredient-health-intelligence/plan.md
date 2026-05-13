# Implementation Plan — Anti-hallucination & ancrage analyse LLM

**Branch**: `020-forbid-llm-hallucination` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature C (sessions clarify 2026-05-13) + invariants domaine `ingredient-health-intelligence`

## Summary

Renforcer le respect des exigences **IHI-C-FR-001** à **IHI-C-FR-007** dans le code Android existant : ancrage **strict** (sous-chaînes littérales + politique d’équivalence v1 minimale), politique **tout ou rien** sur le succès « fait produit », séparation **contenu général** / **ce produit** (**IHI-C-FR-004**), juxtaposition **additive-risk-insights** uniquement sous **IHI-C-FR-007**, et traçabilité **relecture humaine** au MVP (**IHI-C-FR-006**).  
L’approche technique s’appuie sur une couche d’**ancrage déterministe** post-LLM (composition déjà partiellement couverte par `CompositionResultValidator` ; critique santé et blocs génériques à aligner), des **tests d’acceptation** (ATDD) sur scénarios documentés, et des **contrats** documentés avec `additive-risk-insights` sans fuite de modèle.

## Technical Context

**Language/Version**: Kotlin 2.x (Android), Gradle Android  
**Primary Dependencies**: Jetpack Compose, coroutines, LiteRT / MediaPipe Gemma (`gemma4local`, `composition`, `healthcritique`, `additives`)  
**Storage**: Room / snapshots existants (`LastHealthAnalysisStore`, états `ScanState`) — pas de nouveau magasin obligatoire pour le MVP ancrage  
**Testing**: `androidTest` + tests unitaires JVM (`test`) ; scénarios manuels documentés pour **IHI-C-FR-006** au MVP  
**Target Platform**: Android (API cible du projet)  
**Project Type**: application mobile monolithique modulaire par packages (`com.miamia.*`)  
**Performance Goals**: conserver les budgets existants (ex. timeout composition **Feature A** 180 s côté test bouchonné ; latence inférence inchangée sauf régression mesurée) ; l’ancrage post-LLM doit rester **O(n)** sur la taille du segment + sortie, exécution typiquement **< 500 ms** sur device milieu de gamme pour le parse/validate  
**Constraints**: hors ligne pour inférence locale ; pas de succès partiellement ancré ; pas d’enrichissement additif sans contrat + attribution UI  
**Scale/Scope**: segment ingrédients typiquement **< 12 k** caractères (aligné prompts existants) ; nombre de lignes bilan / sections critique borné par parseurs actuels

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut | Notes |
|-----------|--------|--------|
| I — Qualité / traçabilité | OK | Spec → critères SC-C-* → tests / checklist revue |
| II — ATDD | OK | Scénarios Given/When/Then dans spec Feature C ; tests à faire échouer puis passer |
| III — UX | OK | États d’échec explicites (`non-analysable-response`) ; pas de troncature trompeuse (interdit par clarify) |
| IV — Performance | OK | Objectif ancrage local ci-dessus ; pas de boucle LLM supplémentaire |
| V — Simplicité | OK | Renforcer validateurs existants avant nouveau framework |
| VI — DDD | OK | ACL vers `additive-risk-insights` via read-model + **IHI-C-FR-007** ; segment validé fourni par amont `ingredient-normalization-validation` |

**Re-check post Phase 1** : contrats dans `contracts/` délimitent les dépendances ; pas de partage de modèles Kotlin cross-domain non justifié.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md              # /speckit.tasks — hors périmètre de cette commande
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── composition/
│   ├── CompositionResultValidator.kt   # ancrage composition vs segment
│   ├── CompositionAnalysisEngine.kt
│   ├── GemmaBilanParser.kt
│   └── CompositionModels.kt
├── healthcritique/
│   ├── HealthCritiqueEngine.kt
│   ├── HealthCritiqueSectionParser.kt
│   ├── HealthCritiquePromptBuilder.kt
│   └── HealthIngredientInputValidator.kt
├── additives/                          # KPI / UI juxtaposition
│   └── BuildAdditiveKpiDisplay.kt
├── camera/
│   └── CameraViewModel.kt                # orchestration bilan + KPI
└── gemma4local/                        # runtime local

app/src/test/java/com/miamia/            # unitaires validateurs / parsers
app/src/androidTest/java/com/miamia/    # parcours critiques si présents
```

**Structure Decision** : implémentation dans les packages **composition**, **healthcritique**, **additives** et points d’orchestration **camera** / **result** ; aucun nouveau module Gradle requis pour le MVP.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier. Table vide.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
