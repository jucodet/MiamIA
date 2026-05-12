# Implementation Plan: Démarrage caméra et scan temporaire

**Branch**: `005-camera-start-temp-scan` | **Date**: 2026-04-19 | **Spec**: `specs/005-camera-start-temp-scan/spec.md`
**Input**: Feature specification from `specs/005-camera-start-temp-scan/spec.md`

## Summary

Construire un flux mobile Android qui démarre directement sur la caméra, capture une image temporaire au clic sur
"Scan", puis analyse l’image localement sur le smartphone via une brique Google AI Edge, sans upload vers un serveur
tiers. Les métadonnées de session sont persistées en SQLite locale; l’image temporaire est supprimée en fin de cycle.

## Technical Context

**Language/Version**: Kotlin (Android moderne, cible API 26+)  
**Primary Dependencies**: AndroidX CameraX (capture), AndroidX Room (SQLite), Google AI Edge OCR/vision runtime local, Kotlin Coroutines  
**Storage**: SQLite locale via Room pour métadonnées uniquement; fichier image temporaire en cache privé puis suppression  
**Testing**: JUnit5, tests instrumentés Android, tests UI Compose/Espresso pour scénario caméra+scan  
**Target Platform**: Smartphones Android (mode portrait, permissions caméra)  
**Project Type**: application mobile Android offline-first  
**Performance Goals**: affichage caméra initial < 2s; déclenchement analyse < 500ms après clic; résultat OCR < 10s p95  
**Constraints**: 100% local on-device; zéro upload tiers; minimum de librairies; dépendances utilisées de manière standalone  
**Scale/Scope**: MVP mono-utilisateur sur appareil unique; conservation historique métadonnées locale

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit/code**: PASS - exigences explicites (confidentialité image, états UX, suppression temporaire).
- **ATDD d’abord**: PASS - scénarios Given/When/Then dans la spec, tests d’acceptation planifiés avant implémentation.
- **UX moderne**: PASS - parcours direct caméra + feedback explicite (idle/analyzing/success/error).
- **Performance**: PASS - objectifs mesurables définis (startup, latence déclenchement, délai OCR p95).
- **Simplicité/évolutivité**: PASS - stack réduite, traitement local, séparation claire capture/analyse/persistance.

**Post-Design Re-check**: PASS - artefacts Phase 1 (`research.md`, `data-model.md`, `contracts/`, `quickstart.md`)
restent conformes: ATDD testable, UX explicite, contraintes perf mesurables, aucune dépendance cloud tierce.

## Project Structure

### Documentation (this feature)

```text
specs/005-camera-start-temp-scan/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── local-scan-analysis-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/miamia/
│   ├── camera/
│   │   ├── CameraScreen.kt
│   │   ├── CameraViewModel.kt
│   │   └── ScanState.kt
│   ├── scan/
│   │   ├── TemporaryImageManager.kt
│   │   ├── LocalOcrAnalyzer.kt
│   │   └── ScanSessionCoordinator.kt
│   ├── data/
│   │   ├── db/AppDatabase.kt
│   │   ├── db/ScanSessionEntity.kt
│   │   └── repository/ScanSessionRepository.kt
│   └── permissions/
│       └── CameraPermissionHandler.kt
└── src/androidTest/
    ├── camera/
    └── scan/
```

**Structure Decision**: Architecture Android modulaire légère (feature packages), avec séparation stricte UI caméra,
orchestration de session scan, OCR local AI Edge et persistance SQLite des seules métadonnées.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
