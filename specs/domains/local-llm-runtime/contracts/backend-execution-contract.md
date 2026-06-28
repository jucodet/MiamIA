# Contract — Backend Execution (read-model publié)

**Feature**: `026-inference-backend-badge` · **Date**: 2026-06-28

## Propriétaire

Domaine `local-llm-runtime` (bounded context `LocalLlmRuntimeContext`).

## Nature

Contrat interne **publishé** (Open Host Service) : le runtime local publie le backend d'exécution constaté vers la couche présentation transversale (UI du résultat d'analyse). La présentation ne calcule ni ne devine le backend ; elle consomme ce read-model.

## Contrat (Kotlin)

```kotlin
package com.miamia.gemma4local.model

enum class BackendExecution { NPU, GPU, CPU, INDETERMINATE }
```

## Flux de propagation

```text
LiteRtGemmaEngine (constate le backend litertlm.Backend qui réussit)
   └─ BackendExecution.from(backend)                       // mapping propriétaire runtime
       └─ AnalyzeCompositionResult.BilanSuccess.backend    // contrat runtime ↔ composition/viewmodel
           └─ CameraViewModel.runCompositionStage
               ├─ StreamingBilanState.Complete.backend     // contrat viewmodel ↔ UI (LlmResultScreen)
               └─ ScanState.BilanReady.backend             // contrat viewmodel ↔ UI (CameraScreen)
                   └─ BilanResultCard(backend = ...)       // présentation → BackendBadge (pastille)
```

## Garanties contractuelles

- **Non-cassant**: tout champ/paramètre `backend` a une valeur par défaut `BackendExecution.INDETERMINATE` → les appelants/existing tests ignorent le backend par défaut.
- **Fermeture**: `BackendExecution` est scellé à 4 valeurs ; toute valeur hors NPU/GPU/CPU est `INDETERMINATE`.
- **Cohérence**: `backend` et `inferenceTimeMs` sont peuplis dans le même bloc `BilanSuccess → Complete/BilanReady` (même exécution d'inférence).
- **Pas de fuite**: `BackendExecution` vit dans `gemma4local.model` (runtime) ; aucune dépendance inverse depuis les domaines métier (`composition` importe déjà `gemma4local` pour le gateway — conforme au pattern Conformist de la domain-map).

## Tests de contrat requis

- `BackendExecutionMappingTest` : mapping `litertlm.Backend` → `BackendExecution` (NPU/GPU/CPU + inconnu → INDETERMINATE).
- `InferenceBackendBadgeUiTest` : rendu pastille par backend + état non trompeur.
