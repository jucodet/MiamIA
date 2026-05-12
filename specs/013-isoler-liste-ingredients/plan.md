# Implementation Plan: Isolation de la liste d ingredients OCR

**Branch**: `013-isoler-liste-ingredients` | **Date**: 2026-04-28 | **Spec**: `specs/013-isoler-liste-ingredients/spec.md`  
**Input**: Feature specification from `specs/013-isoler-liste-ingredients/spec.md`

## Summary

Isoler la portion ingredients du texte OCR avant analyse pour reduire le bruit contextuel et ameliorer la pertinence des resultats. La delimitation est ancree sur la premiere occurrence de `ingredients` et se termine au premier retour a la ligne (ou fin du texte si pas de retour), avec confirmation utilisateur avant envoi et strategie de blocage quand l ancre est absente.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (Jetpack Compose)  
**Primary Dependencies**: Android SDK, Jetpack Compose, composants OCR/scan existants, module d analyse local  
**Storage**: stockage local existant (resultat OCR et donnees de scan)  
**Testing**: JUnit4, tests unitaires Kotlin, tests UI Compose, tests instrumentes Android  
**Target Platform**: Android smartphone (parcours scan ingredients)  
**Project Type**: mobile-app  
**Performance Goals**: preparation du segment ingredients en moins de 200 ms p95 apres reception du texte OCR; aucune regression du temps global de scan documente  
**Constraints**: comportement deterministic, hors-ligne pour l etape de preparation, pas de changement du flux metier principal de `001-scan-ingredients`  
**Scale/Scope**: 1 flux OCR->preparation->analyse sur l ecran de scan; cas limites sur ancre/retour ligne/occurrences multiples

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualite produit et code**: PASS - changement borne, testable et trace spec -> tests -> code.
- **ATDD d abord**: PASS - scenarios Given/When/Then couvrent nominal et cas de repli.
- **UX moderne et optimale**: PASS - confirmation explicite avant analyse + messages clairs sur blocage.
- **Performance comme exigence produit**: PASS - objectif mesurable sur preparation du segment.
- **Simplicite et evolutivite**: PASS - extraction lineaire, sans sur-architecture.

**Post-Design Re-check**: PASS - artefacts Phase 0/1 complets, sans contradiction avec la constitution.

## Project Structure

### Documentation (this feature)

```text
specs/013-isoler-liste-ingredients/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ingredient-segment-prep-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/miamia/
    │   ├── camera/
    │   │   ├── CameraViewModel.kt
    │   │   └── CameraScreen.kt
    │   ├── ocr/
    │   │   └── ... (OCR text pipeline)
    │   └── analysis/
    │       └── ... (analysis input pipeline)
    ├── test/java/com/miamia/
    │   ├── camera/
    │   └── analysis/
    └── androidTest/java/com/miamia/
        └── camera/
```

**Structure Decision**: Extension de l app Android existante en ajoutant une etape de preparation fonctionnelle entre OCR et analyse, avec validation unitaire des regles de decoupage et verification UI de confirmation utilisateur.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
