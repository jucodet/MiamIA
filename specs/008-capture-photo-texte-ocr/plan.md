# Implementation Plan: Capture photo et affichage du texte reconnu

**Branch**: `008-capture-photo-texte-ocr` | **Date**: 2026-04-24 | **Spec**: `specs/008-capture-photo-texte-ocr/spec.md`  
**Input**: Feature specification from `specs/008-capture-photo-texte-ocr/spec.md`

## Summary

Implémenter un parcours Android local-first où l'utilisateur appuie sur "Prendre la photo", obtient une capture réelle, puis voit le texte extrait de cette photo en moins de 10 secondes dans des conditions normales. Le traitement de lecture de texte est 100% on-device (sans envoi image/réseau requis), avec états UX explicites (chargement, aucun texte détecté, erreur et relance).

## Technical Context

**Language/Version**: Kotlin + Android (minSdk 26, targetSdk 34, Java 17)  
**Primary Dependencies**: CameraX, Jetpack Compose Material3, Kotlin Coroutines, ML Kit Text Recognition (on-device), LiteRT/AI Edge pour capacités locales  
**Storage**: Cache privé app pour image temporaire; Room SQLite pour sessions/résultats utiles  
**Testing**: JUnit4 unitaires, tests instrumentés AndroidX, tests UI Compose (scénarios Given/When/Then)  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: Résultat texte affiché en moins de 10s (conditions normales), feedback UI immédiat à chaque transition d'état  
**Constraints**: OCR sur appareil uniquement, aucun envoi image pour l'extraction, fonctionnement sans réseau pour cette étape, pas de contenu factice  
**Scale/Scope**: MVP mono-utilisateur, un flux capture->texte principal, support de textes courts à longs sur étiquettes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit et code**: PASS - exigences traçables spec -> plan -> artefacts; erreurs et cas limites couverts.
- **ATDD d'abord**: PASS - stories et scénarios Given/When/Then testables déjà définis dans la spec.
- **UX moderne et optimale**: PASS - états loading/erreur/aucun texte, action explicite utilisateur, parcours clair.
- **Performance exigence produit**: PASS - cible mesurable < 10s conservée dans la spec harmonisée.
- **Simplicité et évolutivité**: PASS - pipeline local minimal réutilisant modules existants (`camera`, `recognition`, `ingredients`).

**Post-Design Re-check**: PASS - `research.md`, `data-model.md`, `contracts/` et `quickstart.md` respectent ATDD, offline-first et contraintes de performance.

## Project Structure

### Documentation (this feature)

```text
specs/008-capture-photo-texte-ocr/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── photo-text-capture-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/main/java/com/miamia/
    ├── camera/
    │   ├── CameraScreen.kt
    │   ├── CameraViewModel.kt
    │   ├── CameraCaptureController.kt
    │   └── CameraPreviewBox.kt
    ├── recognition/
    │   ├── IngredientRecognitionCoordinator.kt
    │   ├── RecognitionEngineSelector.kt
    │   └── LocalOcrFallbackRecognizer.kt
    ├── ingredients/
    │   ├── IngredientEditorScreen.kt
    │   └── ScanFailureMessageBuilder.kt
    └── scan/
        ├── TemporaryImageManager.kt
        └── ScanSessionCoordinator.kt
```

**Structure Decision**: Conserver l'architecture Android existante et implémenter la feature 008 en orchestration de composants déjà présents, afin de limiter le risque, réduire la dette et garantir la cohérence UX/perf avec 006/007.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

## Implementation Notes (2026-04-24)

- OCR local branché via ML Kit on-device dans `LocalOcrFallbackRecognizer`.
- Le sélecteur force actuellement le moteur local pour éviter tout résultat factice.
- États `empty` et `error` explicités côté UI avec actions de relance.
- Tests unitaires debug validés (`:app:testDebugUnitTest`).
