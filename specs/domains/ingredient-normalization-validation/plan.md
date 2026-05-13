# Implementation Plan: auto-analyze-ingredients-tag (FR-010)

**Branch**: `021-auto-analyze-ingredients-tag` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Spec domaine `ingredient-normalization-validation` — enchaînement analyse sans écran de validation lorsque la session porte la balise « ingrédients », OCR réussi et proposition exploitable (FR-010 / FR-011, US2b, SC-005 ; SC-004 hors FR-010).

**Note**: Phase 2 (découpage tâches) = `/speckit.tasks` — non produit par cette commande.

## Summary

Après OCR réussi, si la session de capture est explicitement associée à la **balise (mode) ingrédients** et que l’isolation produit un segment exploitable (mêmes garde-fous que pour une validation manuelle via `AnalysisSubmissionGate`), l’application doit **sauter** l’état UI `SegmentConfirmationRequired` et enchaîner vers la même pipeline d’analyse composition que `confirmSegmentAndAnalyze()` (segment traité comme **validé implicite**, traçabilité FR-009). Les autres parcours conservent la confirmation explicite (FR-007, FR-011).

Approche technique retenue (voir [research.md](./research.md)) : signal d’intention **distinct** du texte OCR (éviter de déduire la balise de la seule présence d’une ancre), extension contrôlée de `AnalysisSubmissionGate`, et branchement unique dans `CameraViewModel` après `segmentPreparationService.prepare`.

## Technical Context

**Language/Version**: Kotlin 2.x (Android), Gradle (AGP aligné repo)  
**Primary Dependencies**: Jetpack Compose, Navigation Compose, coroutines / `StateFlow`, couche existante `com.miamia.analysis.ingredientsegment.*`, `CameraViewModel`, MediaPipe / pipeline OCR existant (`scanCoordinator`)  
**Storage**: N/A pour la règle FR-010 elle-même ; traçabilité session inchangée (FR-009, mémoire / `scanId`)  
**Testing**: JVM unit (`:app:testDebugUnitTest`), instrumentation existante `IngredientSegmentConfirmationUiTest` / parcours capture ; nouveaux tests ciblant la branche FR-010  
**Target Platform**: Android (module `app`)  
**Project Type**: application mobile monolithique (`app/`)  
**Performance Goals**: Pas de nouveau critère chiffré côté FR-010 ; **SC-004** ne s’applique pas au parcours FR-010 (clarification 2026-05-13). Conserver le feedback utilisateur pendant l’analyse (streaming bilan existant) pour respecter Constitution III (UX / états de chargement).  
**Constraints**: Frontière DDD — ne pas faire « fuiter » la logique métier d’isolation dans le module OCR ; le signal « balise ingrédients » peut être fourni par **capture-recognition** / UI mais la décision « validation implicite autorisée » reste dans le domaine normalization-validation (`AnalysisSubmissionGate` + orchestration `CameraViewModel`).  
**Scale/Scope**: Un flux de navigation + une extension de gate ; tests d’acceptation et au moins un test UI ou instrumenté pour SC-005.

## Constitution Check

*GATE: exécuté avant Phase 0 ; revérifié après Phase 1.*

| Principe | Verdict | Notes |
|----------|---------|--------|
| I Qualité / traçabilité | **OK** | Spec + contrat + tests ATDD listés en Phase 1. |
| II ATDD | **OK** | US2b et FR-010 / FR-011 fournissent des scénarios Given/When/Then vérifiables ; ajout de tests gate + ViewModel / UI ciblés. |
| III UX | **OK** | Pas d’écran de validation intermédiaire ≠ absence de feedback : réutiliser navigation vers résultat / états `CompositionAnalyzing` existants. |
| IV Performance | **OK** | Aucune exigence perf supplémentaire dans la spec pour ce flux ; mesures existantes composition inchangées. |
| V Simplicité | **OK** | Préférer paramètre explicite sur `evaluate` ou méthode surchargée plutôt que dupliquer toute la logique composition. |
| VI Frontières DDD | **OK** | Signal capture depuis amont en ACL ; décision « segment validé implicite » dans `AnalysisSubmissionGate` / orchestrateur app. |

**Re-check post-Phase 1** : pas de violation introduite ; dépendance contractuelle avec `capture-recognition` documentée dans [contracts/session-capture-intent-for-implicit-validation.md](./contracts/session-capture-intent-for-implicit-validation.md).

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-normalization-validation/
├── plan.md              # Ce fichier
├── research.md          # Phase 0 (décisions 021 ajoutées)
├── data-model.md        # Phase 1 — états + intention de session
├── quickstart.md        # Phase 1 — validation manuelle FR-010
├── contracts/
│   ├── boundary-resolver-contract.md
│   ├── ingredient-segment-boundary-contract.md
│   └── session-capture-intent-for-implicit-validation.md  # nouveau
└── tasks.md             # /speckit.tasks (hors périmètre)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── camera/
│   ├── CameraViewModel.kt          # branchement post-OCR : skip SegmentConfirmationRequired si FR-010
│   ├── CameraScreen.kt             # optionnel : transmettre l’intention « balise ingrédients »
│   └── ScanState.kt
├── analysis/ingredientsegment/
│   ├── AnalysisSubmissionGate.kt   # autoriser soumission sans tap utilisateur si intention + garde-fous
│   └── ...
└── recognition/                    # ou composition du coordinator : port du signal session si nécessaire

app/src/test/java/com/miamia/analysis/ingredientsegment/
├── AnalysisSubmissionGateContractTest.kt   # étendre
└── ...

app/src/androidTest/java/com/miamia/camera/ingredientsegment/
└── IngredientSegmentConfirmationUiTest.kt  # ou nouveau test parcours accéléré
```

**Structure Decision** : implémentation concentrée dans `CameraViewModel` (orchestration) + `AnalysisSubmissionGate` (règle métier « quand une soumission sans confirmation UI est permise »). Signal « balise ingrédients » propagé depuis la couche capture / UI sans dupliquer la résolution d’ancre.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

*(Pas de lignes dans le tableau — aucune complexité supplémentaire à excuser.)*

## Phase 2 (rappel)

Le découpage en tâches ordonnées est produit par **`/speckit.tasks`**, pas par `/speckit.plan`.
