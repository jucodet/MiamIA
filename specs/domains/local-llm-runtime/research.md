# Research — Inference Backend Badge

**Feature**: `026-inference-backend-badge` · **Date**: 2026-06-28

## R0.1 — Comment connaître le backend réellement utilisé par LiteRT-LM ?

**Decision**: Capturer le backend au point exact de succès dans la boucle `runInferenceLoop` / `runLitertLm`, à partir de l'instance `com.google.ai.edge.litertlm.Backend` itérée (déjà typée `Backend.NPU` / `Backend.GPU` / `Backend.CPU`). Le runtime décide du backend effectif (premier qui réussit dans l'ordre NPU→GPU→CPU), la présentation ne l'infère jamais.

**Rationale**: `LiteRtGemmaEngine` et `HybridGemma4LocalGateway` itèrent déjà `priorizedBackends()` / `litertLmBackendChain()` et disposent de la fonction `backendName(backend)` qui mappe vers "NPU"/"GPU"/"CPU". Il suffit de propager cette information vers le haut au moment du succès plutôt que de ne faire que la logger (`Log.i backend_success $name`).

**Alternatives considered**:
- Interroger l'`Engine` après coup pour deviner le backend → rejeté : aucune API LiteRT-LM stable exposant le backend retenu après `initialize()` ; risque d'erreur.
- Mémoriser un backend "préféré" côté UI → rejeté : violerait l'invariant spec (la pastille reflète le backend **réellement** utilisé, pas le préféré/disponible).

## R0.2 — Comment transporter le backend jusqu'à l'UI sans casser la chaîne existante ?

**Decision**: Ajouter un champ `backend: BackendExecution = BackendExecution.INDETERMINATE` (valeur par défaut non-cassante) sur `AnalyzeCompositionResult.BilanSuccess`, `StreamingBilanState.Complete`, `ScanState.BilanReady`, et un paramètre `backend: BackendExecution = BackendExecution.INDETERMINATE` sur `BilanResultCard`. Propagation stricte runtime → ViewModel → UI.

**Rationale**: Approche additive (champs avec défaut) → aucun casser les appelants existants ni les tests hérités. L'invariant de cohérence durée ↔ backend est garanti car les deux sont peuplisés dans le même bloc `BilanSuccess → Complete/BilanReady` du `CameraViewModel.runCompositionStage`.

**Alternatives considered**:
- Wrapper global `InferenceOutcome(text, backend, latency)` → rejeté : refactor large, multple call sites, risque de régression hors scope.
- Stocker le backend dans `CompositionBilan` → rejeté : `CompositionBilan` est un modèle métier du domaine `ingredient-health-intelligence`/composition ; y injecter une donnée runtime technique violerait la frontière DDD.

## R0.3 — Comment gérer le cas "backend chaud" retenu (retainedEngine) ?

**Decision**: Suivre le backend du moteur retenu via un champ privé `retainedBackend: BackendExecution?` mis à jour en parallèle de `retainedEngine` / `retainedModelAbsolutePath` dans `LiteRtGemmaEngine`. Quand l'inférence réussit sur le moteur chaud, on utilise `retainedBackend` ; quand elle réussit sur un nouveau backend, on enregistre ce backend. En cas d'échec du moteur chaud (dispose), on remet `retainedBackend` à null.

**Rationale**: Le moteur chaud est réutilisé sans re-parcourir la chaîne ; il faut donc connaître son backend d'origine pour le reporter. Symétrique exact de `retainedModelAbsolutePath`.

**Alternatives considered**:
- Forcer le re-parcours de la chaîne à chaque appel pour connaître le backend → rejeté : détruirait l'optimisation du moteur retenu (régression perf, principe IV).
- Ne pas reporter le backend en mode moteur chaud (INDETERMINATE) → rejeté : pastille erronée/incomplète sur les inférences suivantes (violation spec SC-001).

## R0.4 — Quelle distinctité visuelle par backend ?

**Decision**: Pastille = `Surface` arrondie avec icône Material + couleur + libellé court :
- **NPU** → icône `Icons.Filled.Memory` (puce), couleur `MiamIAColors.Primary` (sage/teal), libellé "NPU".
- **GPU** → icône `Icons.Filled.DeveloperMode` (ou `Icons.Filled.Bolt`), couleur `MiamIAColors.SectionIngredients` (bleu muted), libellé "GPU".
- **CPU** → icône `Icons.Filled.DeveloperBoard` (fallback `Icons.Filled.Memory`), couleur `MiamIAColors.OnSurfaceVariant` (neutre), libellé "CPU".
- **INDETERMINATE** → icône `Icons.Filled.HelpOutline`, couleur neutre, libellé "—" (pas de backend trompeur).

La pastille est placée immédiatement à gauche du libellé "Inférence : Xs", dans le même `Row` aligné à fin.

**Rationale**: Icônes Material déjà importées dans le projet ; palette `MiamIAColors` respecte le contraste WCAG AA sur fond blanc. Distinctité triple (icône + couleur + libellé) → identification sans lecture (SC-002). Thème sombre géré via `MaterialTheme.colorScheme.*` pour les fonds/contours.

**Alternatives considered**:
- Texte seul ("NPU"/"GPU"/"CPU") → rejeté : spec US2 exige distinctité visuelle sans lecture.
- Emoji → rejeté : rendu variable selon OEM, contraste non maîtrisé.

## R0.5 — Périmètre critique santé ?

**Decision**: Hors scope MVP. La critique santé (`LiteRtHealthCritiqueRunner` → `HybridGemma4LocalGateway.inferStreaming`) n'affiche pas actuellement de durée d'inférence dans `InlineCritiqueSection` ; la spec cible "à côté de l'info durée d'inférence" qui n'existe que côté composition (`BilanResultCard.InferenceTimeBadge`).

**Rationale**: Respect de YAGNI/Scope (principe V). L'enum `BackendExecution` et le mapping sont réutilisables si la critique expose plus tard sa propre durée ; pas de travail supplémentaire now.

**Alternatives considered**:
- Étendre `inferStreaming` pour retourner backend + ajouter durée critique → rejeté : hors spec, scope creep, risque de régression sur la critique.

## R0.6 — Tests d'acceptation

**Decision**:
- `BackendExecutionMappingTest` (unitaire, JVM) : valide le mapping `litertlm.Backend.NPU/GPU/CPU` → `BackendExecution` et le fallback `INDETERMINATE` pour un backend inconnu.
- `InferenceBackendBadgeUiTest` (Compose, Robolectric) : rend `BilanResultCard` avec `inferenceTimeMs > 0` et chacun des backends, vérifie le libellé + testTag (`inference_backend_badge`) + présence pour NPU/GPU/CPU, et l'état neutre pour `INDETERMINATE` ; vérifie qu'aucune pastille backend trompeuse n'apparaît quand `inferenceTimeMs == 0L` (cas échoué avant exécution — `BilanResultCard` n'affiche déjà plus `InferenceTimeBadge`).

**Rationale**: Couvre US1 (présence correcte par backend), US2 (distinctité — libellés distincts), US3 (état non trompeur). Cohérent avec la constitution ATDD.
