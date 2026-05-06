# Implementation Plan: Règle unique segment ingrédients

**Branch**: `016-ingredient-phrase-segment` | **Date**: 2026-05-06 | **Spec**: `specs/domains/ingredient-normalization-validation/spec.md`  
**Input**: Feature specification from `specs/domains/ingredient-normalization-validation/spec.md`

## Summary

Unifier l’isolation du segment ingrédients à partir du texte OCR avec une règle déterministe: première ancre reconnue (`Ingrédient`, `Ingrédients`, `Ingredient`, `Ingredients`), borne de fin à la fin de phrase si ponctuation terminale standard, sinon fin de ligne, sinon fin du texte disponible. Le domaine doit garantir la traçabilité `RawOcrText -> proposition -> segment validé` et bloquer toute analyse aval sans confirmation explicite.

## Technical Context

**Language/Version**: Kotlin 2.x (Android)  
**Primary Dependencies**: pipeline OCR local existant, services `analysis/ingredientsegment`, couche `ingredients` (validation UI)  
**Storage**: persistance locale existante des sessions/segments validés (pas de nouveau stockage requis)  
**Testing**: JUnit4 (unitaires), Android instrumentés existants pour flux scan/validation  
**Target Platform**: Android (app mobile)  
**Project Type**: Mobile app domain module  
**Performance Goals**: isolation déterministe en mémoire et affichage de la proposition en < 2 s après réception du texte OCR dans 95% des cas usuels  
**Constraints**: offline-only pour OCR, aucune invention de contenu, première occurrence uniquement, règles canoniques de borne  
**Scale/Scope**: un domaine (`ingredient-normalization-validation`), un contrat d’isolation, impacts ciblés sur préparation/validation de segment

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Qualité produit et code**: PASS — règles explicites, testables, sans ambiguïté majeure.
- **II. ATDD d’abord**: PASS — scénarios Given/When/Then et critères mesurables présents dans la spec.
- **III. UX moderne et optimale**: PASS — échecs explicites + reprise prévue en cas d’ancre absente/inexploitable.
- **IV. Performance exigence produit**: PASS — objectif de réactivité défini pour l’isolation.
- **V. Simplicité et évolutivité contrôlée**: PASS — remplacement d’anciennes règles par une règle unique.
- **VI. Frontières DDD**: PASS — isolation dans `ingredient-normalization-validation`, OCR brut conservé en amont dans `capture-recognition`.

**Post-Design Re-check**: PASS — artefacts de conception (research/data-model/contracts/quickstart) respectent les frontières DDD et la testabilité.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-normalization-validation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ingredient-segment-boundary-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/foodgpt/
├── analysis/ingredientsegment/
│   ├── IngredientAnchorNormalizer.kt
│   ├── IngredientSegmentBoundaryResolver.kt
│   ├── IngredientSegmentPreparationService.kt
│   └── IngredientSegmentModels.kt
├── ingredients/
│   ├── IngredientValidationUseCase.kt
│   └── IngredientListState.kt
└── recognition/
    └── RecognitionContracts.kt

app/src/test/java/com/foodgpt/analysis/ingredientsegment/
├── IngredientSegmentBoundaryResolverTest.kt
├── IngredientSegmentExtractionAcceptanceTest.kt
└── IngredientSegmentFallbackAcceptanceTest.kt
```

**Structure Decision**: conserver l’architecture Android existante, implémenter la règle dans `analysis/ingredientsegment`, et couvrir par tests unitaires/acceptance déjà en place dans le même module domaine.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
