# Implementation Plan: Identification des ingrédients par photo

**Branch**: `006-identify-photo-ingredients` | **Date**: 2026-04-24 | **Spec**: `specs/006-identify-photo-ingredients/spec.md`  
**Input**: Feature specification from `specs/006-identify-photo-ingredients/spec.md`

## Summary

Construire une fonctionnalité Android Kotlin responsive qui extrait une liste d'ingrédients depuis une photo, en
traitement 100% local. La reconnaissance doit privilégier les capacités Google AI Edge Gallery présentes sur l'appareil,
avec un fallback OCR local minimaliste sans dépendance cloud et avec un nombre réduit de librairies embarquées.

## Technical Context

**Language/Version**: Kotlin (Android, API 26+)  
**Primary Dependencies**: AndroidX CameraX, AndroidX Compose Material3, Kotlin Coroutines, ML Kit Text Recognition v2 (offline), Google AI Edge Gallery runtime (si disponible sur device)  
**Storage**: Cache privé applicatif (photo temporaire); Room SQLite uniquement pour métadonnées de tentative et résultat validé  
**Testing**: JUnit5, tests Android instrumentés, tests UI Compose (parcours capture > extraction > correction > validation)  
**Target Platform**: Smartphones Android (portrait + écrans variés)  
**Project Type**: application mobile Android offline-first  
**Performance Goals**: affichage résultat < 8s p95 pour photo nette; rendu UI interactif < 100ms pour actions de correction  
**Constraints**: traitement on-device uniquement, suppression auto de la photo après traitement, dépendances minimales et standalone, aucun backend tiers  
**Scale/Scope**: MVP mono-utilisateur; une photo analysée par session; multilingue auto-détecté

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit/code**: PASS - traçabilité spec -> design -> tests respectée; gestion explicite des erreurs OCR.
- **ATDD d'abord**: PASS - user stories avec scénarios Given/When/Then déjà présents et indépendamment testables.
- **UX moderne et optimale**: PASS - correction manuelle, feedback d'état, responsive Android (téléphones compacts à grands écrans).
- **Performance**: PASS - objectifs mesurables définis (p95 extraction, latence interactions).
- **Simplicité/évolutivité**: PASS - stack locale minimale, fallback explicite sans sur-architecture.

**Post-Design Re-check**: PASS - artefacts Phase 1 (`research.md`, `data-model.md`, `contracts/`, `quickstart.md`) maintiennent
la conformité ATDD/UX/performance et la contrainte de dépendances réduites.

## Project Structure

### Documentation (this feature)

```text
specs/006-identify-photo-ingredients/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ingredient-photo-recognition-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/miamia/
    │   ├── camera/
    │   │   ├── CameraScreen.kt
    │   │   └── CameraViewModel.kt
    │   ├── recognition/
    │   │   ├── DeviceAiCapabilityDetector.kt
    │   │   ├── AiEdgeGalleryRecognizer.kt
    │   │   ├── LocalOcrFallbackRecognizer.kt
    │   │   └── IngredientExtractionPipeline.kt
    │   ├── ingredients/
    │   │   ├── IngredientEditorScreen.kt
    │   │   ├── IngredientListState.kt
    │   │   └── IngredientValidationUseCase.kt
    │   └── data/
    │       ├── db/
    │       └── repository/
    └── androidTest/
        ├── recognition/
        └── ingredients/
```

**Structure Decision**: architecture mobile modulaire légère, séparation claire capture/reconnaissance/édition/validation
afin de garder un cœur OCR remplaçable (AI Edge Gallery prioritaire, fallback offline local) avec dépendances minimales.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
