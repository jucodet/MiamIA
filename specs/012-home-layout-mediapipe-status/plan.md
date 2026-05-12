# Implementation Plan: Disposition accueil MediaPipe

**Branch**: `012-home-layout-mediapipe-status` | **Date**: 2026-04-28 | **Spec**: `specs/012-home-layout-mediapipe-status/spec.md`  
**Input**: Feature specification from `specs/012-home-layout-mediapipe-status/spec.md`

## Summary

Structurer l'ecran d'accueil avec un ordre vertical explicite et stable: voyant d'etat MediaPipe en tete (avec libelle), message de bienvenue complet, encart de vue photo, puis bouton de prise de photo juste en dessous avec espacement standard fixe. Le perimetre est limite au mode portrait et la spec 012 devient la reference d'ordre UI pour les specs d'accueil liees.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (Jetpack Compose)  
**Primary Dependencies**: Android SDK, Jetpack Compose, ViewModel, modules existants `welcome`, `camera`, `gemma4local`  
**Storage**: N/A (aucune persistence additionnelle requise pour cette feature UI)  
**Testing**: JUnit4 (unit), tests UI Compose, tests instrumentes Android  
**Target Platform**: Application Android (smartphones), mode portrait  
**Project Type**: mobile-app  
**Performance Goals**: affichage initial stable de l'ordre des blocs en moins de 1 seconde apres rendu ecran; etat de voyant coherent des la fin de verification  
**Constraints**: pas de changement de logique metier capture/welcome; respect strict de l'ordre UI 012; mode paysage hors perimetre  
**Scale/Scope**: un seul ecran (accueil) + harmonisation contractuelle avec specs 007 et 010

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualite produit et code**: PASS - changement borne, traçable et compatible avec composants existants.
- **ATDD d'abord**: PASS - scenarios Given/When/Then deja explicites dans la spec, couvrant etat nominal et edges.
- **UX moderne et optimale**: PASS - ordre visuel clair, feedback d'etat immediat, action primaire proche de la vue photo.
- **Performance exigence produit**: PASS - objectifs d'affichage et de coherence etat etablis et verifiables.
- **Simplicite et evolutivite**: PASS - reajustement UI sans sur-architecture, priorite inter-spec documentee.

**Post-Design Re-check**: PASS - `research.md`, `data-model.md`, `contracts/`, et `quickstart.md` preservent les principes de qualite, testabilite et simplicite.

## Project Structure

### Documentation (this feature)

```text
specs/012-home-layout-mediapipe-status/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── home-layout-order-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/miamia/
    │   ├── MainActivity.kt
    │   ├── camera/
    │   │   ├── CameraScreen.kt
    │   │   └── CameraViewModel.kt
    │   ├── welcome/
    │   │   ├── WelcomeMessageUiState.kt
    │   │   └── WelcomeMessageProvider.kt
    │   └── gemma4local/
    │       └── Gemma4LocalAvailabilityChecker.kt
    ├── androidTest/java/com/miamia/
    │   ├── camera/
    │   └── welcome/
    └── test/java/com/miamia/
        ├── camera/
        └── welcome/
```

**Structure Decision**: Extension de l'application Android existante, en priorisant des ajustements sur l'ecran d'accueil Compose et des tests UI/instrumentes pour verifier ordre et stabilite visuelle sans modifier les flux metier camera et welcome.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
