# Quickstart — Inference Backend Badge

**Feature**: `026-inference-backend-badge` · **Date**: 2026-06-28

## Vérification rapide (scénarios d'acceptation)

### US1 — Présence correcte par backend

1. Lancer une analyse d'ingrédients (capture → OCR → inférence composition).
2. Sur l'écran de résultat (`LlmResultScreen` ou `CameraScreen`), localiser le libellé `Inférence : Xs` (testTag `inference_time_label`).
3. Vérifier la présence d'une pastille `inference_backend_badge` immédiatement à gauche, libellée selon le backend réellement utilisé (NPU / GPU / CPU).

### US2 — Distinctité visuelle

1. Comparer les pastilles des trois backends (test UI ou forçage via tests) : icône + couleur + libellé différents pour NPU, GPU, CPU.
2. Vérifier qu'un utilisateur identifie le backend au visuel seul, sans lire le libellé.

### US3 — Cas non trompeur

1. Cas inférence échouée avant exécution : `BilanResultCard` ne rend pas `InferenceTimeBadge` (durée = 0) → aucune pastille backend affichée.
2. Cas backend non reporté (`INDETERMINATE`) : pastille neutre libellée "—", icône `HelpOutline`, couleur neutre — pas de backend trompeur.

## Lancer les tests

```bash
./gradlew :app:testDebugUnitTest --tests "com.miamia.gemma4local.BackendExecutionMappingTest"
./gradlew :app:testDebugUnitTest --tests "com.miamia.camera.InferenceBackendBadgeUiTest"
```

## Points d'extension futurs

- Exposer le backend côté critique santé si une durée d'inférence y est ajoutée (réutiliser `BackendExecution`).
- Persister `backend` dans `ApiCallMetric` pour observabilité long-terme (champ déjà optionnel possible).
