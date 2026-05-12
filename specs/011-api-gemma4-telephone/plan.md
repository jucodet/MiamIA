# Implementation Plan: Utilisation API Gemma4 telephone

**Branch**: `010-message-bienvenue-sourire` | **Date**: 2026-04-27 | **Spec**: `specs/011-api-gemma4-telephone/spec.md`  
**Input**: Feature specification from `specs/011-api-gemma4-telephone/spec.md`

## Summary

Remplacer l'usage d'un modele Gemma4 embarque volumineux par l'appel a l'API locale du telephone pour les analyses textuelles v1. La solution vise une execution sans telechargement de modele, sans fallback moteur, avec journalisation technique non sensible, et des objectifs de latence differencies selon la classe d'appareil.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (Compose)  
**Primary Dependencies**: Android SDK, Jetpack Compose, ViewModel, Coroutines  
**Storage**: N/A (pas de persistance du contenu analyse; metriques techniques uniquement)  
**Testing**: JUnit4, tests unitaires, tests UI Compose, tests instrumentes Android  
**Target Platform**: Application mobile Android (appareils compatibles Gemma4 local)  
**Project Type**: mobile-app  
**Performance Goals**: p95 < 3s sur appareils recents; p95 < 6s sur appareils minimum compatibles; message d'erreur < 2s  
**Constraints**: pas de consentement explicite additionnel; pas de fallback local/distant; v1 texte uniquement; aucune conservation du contenu analyse  
**Scale/Scope**: migration du flux d'analyse existant vers API locale pour les requetes textuelles, sans extension a image/audio en v1

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualite produit et code**: PASS - exigences spec tracees en artefacts de plan, avec objectifs verifiables et risques identifies.
- **ATDD d'abord**: PASS - scenarios Given/When/Then couvrent succes, indisponibilite API et contraintes de parcours.
- **UX moderne et optimale**: PASS - retours utilisateurs explicites sur echec, sans blocage global de l'interface.
- **Performance exigence produit**: PASS - objectifs p95 et delai d'affichage d'erreur sont mesurables.
- **Simplicite et evolutivite**: PASS - pas de fallback v1, perimetre texte seul, surface de changement controlee.

**Post-Design Re-check**: PASS - `research.md`, `data-model.md`, `contracts/` et `quickstart.md` maintiennent les exigences de qualite, testabilite et performance.

## Project Structure

### Documentation (this feature)

```text
specs/011-api-gemma4-telephone/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── gemma4-local-api-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/main/java/com/miamia/
    ├── analysis/                  # flux d'analyse existant
    ├── llm/                       # integration modele et orchestration
    └── gemma4local/               # nouveau module d'acces API locale
        ├── Gemma4LocalClient.kt
        ├── Gemma4LocalAvailabilityChecker.kt
        ├── Gemma4LocalErrorMapper.kt
        ├── Gemma4LocalMetricsLogger.kt
        └── Gemma4LocalRequestMapper.kt
```

**Structure Decision**: extension de l'application Android existante via un module `gemma4local` dedie, afin d'isoler l'integration API locale, de limiter l'impact sur le flux d'analyse existant, et de faciliter les tests unitaires/integration.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
