# Data Model - ingredient-health-intelligence

## Entities

- `HealthAnalysisReport`
  - `reportId`
  - `validatedSegment`
  - `compositionSummary`
  - `populationCritiques`
  - `additiveRiskFacts`
  - `generatedAt`

## Value Objects

- `ValidatedIngredientSegment`
- `AdditiveRiskFact`
- `PopulationCritique`

## Aggregates

- Aggregate root: `HealthAnalysisReport`
  - Owns consistency between segment source and generated facts.

## Invariants

- `validatedSegment` non vide.
- `validatedSegment` immuable dans le cycle de vie du rapport.
