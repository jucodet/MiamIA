# Data Model — Inference Backend Badge

**Feature**: `026-inference-backend-badge` · **Date**: 2026-06-28

## Entities

### BackendExecution (NEW)

Enum Kotlin propriété du domaine `local-llm-runtime`, package `com.miamia.gemma4local.model`.

```kotlin
package com.miamia.gemma4local.model

enum class BackendExecution {
    NPU,
    GPU,
    CPU,
    INDETERMINATE;

    val label: String
        get() = when (this) {
            NPU -> "NPU"
            GPU -> "GPU"
            CPU -> "CPU"
            INDETERMINATE -> "—"
        }
}
```

- **Invariants**: fermé (4 valeurs). `INDETERMINATE` couvre backend non reporté / inconnu / inférence échouée avant exécution.
- **Mapping entrant** (depuis `com.google.ai.edge.litertlm.Backend`):
  - `Backend.NPU` → `NPU`
  - `Backend.GPU` → `GPU`
  - `Backend.CPU` → `CPU`
  - tout autre / null / non reconnu → `INDETERMINATE`
- **Relation**: attaché à un résultat d'inférence (transporté via `AnalyzeCompositionResult.BilanSuccess` → `StreamingBilanState.Complete` / `ScanState.BilanReady` → `BilanResultCard`).

### BackendBadge (présentation, dérivé)

Représentation UI dérivée de `BackendExecution` (non persistée, calculée dans `BilanResultCard`) :

| BackendExecution | Icône Material | Couleur | Libellé | testTag |
|------------------|----------------|---------|---------|---------|
| NPU              | `Icons.Filled.Memory`          | `MiamIAColors.Primary`            | "NPU" | `inference_backend_badge` |
| GPU              | `Icons.Filled.DeveloperMode`   | `MiamIAColors.SectionIngredients` | "GPU" | `inference_backend_badge` |
| CPU              | `Icons.Filled.DeveloperBoard`  | `MiamIAColors.OnSurfaceVariant`   | "CPU" | `inference_backend_badge` |
| INDETERMINATE    | `Icons.Filled.HelpOutline`     | neutre (`onSurfaceVariant`)        | "—"   | `inference_backend_badge` |

## Modifications de modèles existants (additif, défaut non-cassant)

- `AnalyzeCompositionResult.BilanSuccess` : `+ val backend: BackendExecution = BackendExecution.INDETERMINATE`
- `StreamingBilanState.Complete` : `+ val backend: BackendExecution = BackendExecution.INDETERMINATE`
- `ScanState.BilanReady` : `+ val backend: BackendExecution = BackendExecution.INDETERMINATE`
- `BilanResultCard(...)` : `+ val backend: BackendExecution = BackendExecution.INDETERMINATE`

## State transitions (LiteRtGemmaEngine)

- `retainedBackend: BackendExecution?` (privé) suit `retainedEngine` :
  - succès sur moteur chaud → `retainedBackend` (non-null) propagé dans `BilanSuccess.backend`.
  - succès sur nouveau backend → backend de l'essai propagé, `retainedBackend` mis à jour.
  - échec moteur chaud → `disposeRetainedLocked()` remet `retainedBackend = null`.
  - timeout/échec total → `BilanSuccess` non produit (erreur) ; aucun backend trompeur.

## Validation rules (depuis spec)

- Une et une seule pastille par résultat nominal réussi (FR-007).
- `INDETERMINATE` pour backend inconnu / échec pré-exécution (FR-005, FR-008).
- Cohérence durée ↔ backend peuplis dans le même bloc (FR-003).
