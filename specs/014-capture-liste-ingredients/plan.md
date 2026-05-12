# Implementation Plan: Ancrage ingredients avec espace avant deux-points

**Branch**: `015-espace-ingredients-deuxpoints` | **Date**: 2026-04-28 | **Spec**: `specs/014-capture-liste-ingredients/spec.md`  
**Input**: Feature specification from `specs/014-capture-liste-ingredients/spec.md`

## Summary

Fiabiliser le debut de capture de la liste ingredients en priorisant une ancre de type `ingredients:` (incluant la variante avec espace `ingredients :`), pour eviter les phrases introductives et alimenter le bilan composition avec un segment pertinent.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (Jetpack Compose)  
**Primary Dependencies**: Android SDK, pipeline OCR existant, pipeline reconnaissance ingredients existant, modules composition/analysis existants  
**Storage**: N/A (pas de nouveau stockage requis)  
**Testing**: JUnit4, tests unitaires Kotlin, tests instrumentes Android existants  
**Target Platform**: Android smartphone  
**Project Type**: mobile-app  
**Performance Goals**: extraction d ancre sans regression perceptible du temps d analyse; p95 de preparation segment maintenu sous 200 ms  
**Constraints**: comportement deterministic, compatibilite stricte avec le flux `009-llm-bilan-composition-ingredients`, traitement local uniquement  
**Scale/Scope**: un seul flux OCR -> selection segment ingredients -> analyse composition; couverture des variantes de ponctuation/casse/espaces

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualite produit et code**: PASS - changement borne, traçable et testable.
- **ATDD d abord**: PASS - scenarios Given/When/Then explicites dans la spec.
- **UX moderne et optimale**: PASS - reduction des erreurs d interpretation, feedback de blocage conserve.
- **Performance comme exigence produit**: PASS - objectif de non-regression + seuil p95 maintenu.
- **Simplicite et evolutivite**: PASS - regles d ancrage minimales et deterministic.

**Post-Design Re-check**: PASS - artefacts de recherche et design alignes avec la constitution.

## Project Structure

### Documentation (this feature)

```text
specs/014-capture-liste-ingredients/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ingredients-anchor-selection-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/miamia/
    │   ├── recognition/
    │   │   ├── IngredientExtractionPipeline.kt
    │   │   └── ... (recognition flow)
    │   ├── analysis/ingredientsegment/
    │   │   └── ... (segment preparation flow)
    │   └── camera/
    │       └── CameraViewModel.kt
    ├── test/java/com/miamia/
    │   └── recognition/
    └── androidTest/java/com/miamia/
        └── camera/
```

**Structure Decision**: Extension de la logique d ancrage existante dans le flux reconnaissance/segment, avec tests unitaires cibles sur la priorisation `ingredients:` et `ingredients :`.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
