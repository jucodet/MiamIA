# Implementation Plan: Aperçu caméra réel et capture par bouton

**Branch**: `007-live-camera-preview-capture` | **Date**: 2026-04-24 | **Spec**: `specs/007-live-camera-preview-capture/spec.md`  
**Input**: Feature specification from `specs/007-live-camera-preview-capture/spec.md`

## Summary

Remplacer l'aperçu simulé de l'écran caméra par un véritable flux objectif en direct, puis déclencher une capture
uniquement via un bouton explicite. Le flux doit gérer les états permission/indisponibilité sans masquer les erreurs
par du contenu factice.

## Technical Context

**Language/Version**: Kotlin Android (minSdk 26, targetSdk 34)  
**Primary Dependencies**: AndroidX CameraX (camera-core/camera-camera2/camera-lifecycle/camera-view), Jetpack Compose Material3, Kotlin Coroutines  
**Storage**: Cache privé applicatif pour image temporaire; persistance métadonnées scan via Room existant  
**Testing**: JUnit4/JUnit5 local, tests instrumentés Android pour parcours capture  
**Target Platform**: Smartphones Android avec caméra (portrait + responsive écrans variés)  
**Project Type**: application mobile Android offline-first  
**Performance Goals**: affichage aperçu caméra < 3s après ouverture écran; capture perçue instantanée (<300ms feedback UI)  
**Constraints**: pas de placeholder visuel trompeur, capture strictement sur action utilisateur, gestion explicite permissions/erreurs  
**Scale/Scope**: MVP sur un flux mono-caméra (objectif par défaut), une capture à la fois

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit/code**: PASS - comportement observable et testable, suppression du mock trompeur.
- **ATDD d'abord**: PASS - scénarios Given/When/Then dans la spec, tests de parcours prévus.
- **UX moderne**: PASS - flux clair (aperçu réel, bouton capture, messages d'erreur explicites).
- **Performance**: PASS - objectifs mesurables (temps d'apparition preview, feedback capture).
- **Simplicité/évolutivité**: PASS - réutilisation stack CameraX/Compose déjà présente, sans sur-architecture.

**Post-Design Re-check**: PASS - artefacts `research.md`, `data-model.md`, `contracts/` et `quickstart.md`
respectent ATDD, UX explicite, objectifs de performance et simplicité de conception.

## Project Structure

### Documentation (this feature)

```text
specs/007-live-camera-preview-capture/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── camera-preview-capture-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/miamia/
    │   ├── camera/
    │   │   ├── CameraScreen.kt
    │   │   ├── CameraPreviewBox.kt
    │   │   ├── CameraCaptureController.kt
    │   │   ├── CameraViewModel.kt
    │   │   └── ScanState.kt
    │   ├── permissions/
    │   │   └── CameraPermissionHandler.kt
    │   └── scan/
    │       └── TemporaryImageManager.kt
    └── androidTest/java/com/miamia/camera/
```

**Structure Decision**: implémentation concentrée dans le module `camera` avec séparation preview/capture/état UI,
afin de garder un flux lisible et facilement testable en parcours utilisateur.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
