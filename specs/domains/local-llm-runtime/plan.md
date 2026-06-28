# Implementation Plan: Inference Backend Badge

**Branch**: `026-inference-backend-badge` | **Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/domains/local-llm-runtime/spec.md`

## Summary

Exposer le backend d'exécution réellement utilisé pour l'inférence Gemma locale (NPU / GPU / CPU) depuis le runtime `LiteRtGemmaEngine` (composition) jusqu'à l'UI, et l'afficher sous forme d'une pastille (icône + couleur + libellé) à côté de la durée d'inférence déjà rendue par `BilanResultCard.InferenceTimeBadge`. Un état `INDETERMINATE` couvre les cas échoués / backend non reporté sans affichage trompeur.

## Technical Context

**Language/Version**: Kotlin (Android, JDK 21 via Gradle AGP) · Jetpack Compose Material3
**Primary Dependencies**: `com.google.ai.edge.litertlm` (Backend / Engine / Conversation), Jetpack Compose, Material3, kotlinx.coroutines
**Storage**: Aucune persistance nouvelle (métrique volatile transportée dans l'état UI existant)
**Testing**: JUnit4 + Robolectric pour tests unitaires/contrat ; tests UI Compose (`createComposeRule`) pour la pastille
**Target Platform**: Android (minSdk existant du module `app`)
**Project Type**: Mobile-app Android (module `app/src/main/java/com/miamia/...`)
**Performance Goals**: Aucun surcoût d'inférence (capture backend déjà connue dans la boucle existante, simple propagation d'une enum)
**Constraints**: Pas de régression UX/perf ; pastille lisible thèmes clair/sombre ; cohérence backend ↔ durée (même exécution)
**Scale/Scope**: 1 écran de résultat concerné (`LlmResultScreen` / `CameraScreen` via `BilanResultCard`), 1 runtime modifié (`LiteRtGemmaEngine`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut | Justification |
|----------|--------|---------------|
| I. Qualité produit et code | ✅ | Traçabilité spec → tests d'acceptation → code ; pas de régression (durée inchangée, ajout seul). |
| II. ATDD d'abord | ✅ | Scénarios Given/When/Then présents dans spec.md (US1/US2/US3) ; tests d'acceptation à écrire avant/pendant l'impl. |
| III. UX moderne et optimale | ✅ | Pastille distincte par backend, états vides/erreurs soignés (`INDETERMINATE`), double thème. |
| IV. Performance comme exigence | ✅ | Aucune mesure d'inférence ajoutée ; capture backend O(1) dans la boucle existante. Pas de régression. |
| V. Simplicité, lisibilité | ✅ | Une enum `BackendExecution` partagée, propagation via champs existants (`BilanSuccess`, `Complete`, `BilanReady`). Pas de couche supplémentaire. |
| VI. Frontières DDD | ✅ | Donnée backend propriété de `local-llm-runtime` (`gemma4local/model`) ; présentation consomme un read-model publié (`BackendBadge`). Pas de fuite de modèle entre domaines. |

**Verdict gate**: PASS — aucune violation injustifiée.

## Project Structure

### Documentation (this feature)

```text
specs/domains/local-llm-runtime/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── backend-execution-contract.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── gemma4local/model/
│   └── BackendExecution.kt          # NEW — enum NPU/GPU/CPU/INDETERMINATE + mapping from litertlm.Backend
├── composition/
│   ├── CompositionModels.kt         # EDIT — BilanSuccess += backend: BackendExecution
│   └── LiteRtGemmaEngine.kt         # EDIT — capture backend réellement utilisé (retained + par essai)
├── camera/
│   ├── StreamingBilanState.kt       # EDIT — Complete += backend
│   ├── ScanState.kt                 # EDIT — BilanReady += backend
│   ├── CameraViewModel.kt           # EDIT — propage backend dans Complete/BilanReady
│   ├── CameraScreen.kt              # EDIT — passe backend à BilanResultCard
│   └── BilanResultCard.kt           # EDIT — InferenceTimeBadge affiche la pastille backend
└── result/
    └── LlmResultScreen.kt           # EDIT — passe backend à BilanResultCard

app/src/test/java/com/miamia/
├── gemma4local/
│   └── BackendExecutionMappingTest.kt       # NEW — contrat mapping litertlm.Backend → BackendExecution
└── camera/
    └── InferenceBackendBadgeUiTest.kt       # NEW — acceptance UI pastille (US1/US2/US3)
```

**Structure Decision**: Périmètre mobile-app Android existant — modification ciblée des fichiers de la chaîne composition→UI, sans nouveau module. La nouvelle enum vit dans `gemma4local/model` (domaine `local-llm-runtime`), frontière DDD respectée.

## Complexity Tracking

> Aucune violation de Constitution à justifier — tableau laissé vide.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
