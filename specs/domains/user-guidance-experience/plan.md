# Implementation Plan: User guidance & experience — Feature G (OCR direct, accueil épuré)

**Branch**: `016-full-ocr-llm` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md) (Feature G, révisions Feature F)
**Input**: Spécification domaine `specs/domains/user-guidance-experience/spec.md`

## Summary

La **Feature G** impose : (1) enchaînement **OCR → analyse LLM** sans écran intermédiaire de relecture du transcript ; (2) écran capture **sans** chip « balise ingrédients » et **sans** la chaîne exacte « Caméra prête — vous pouvez scanner » ni ligne d’invitation obligatoire sous l’aperçu pour l’état prêt ; (3) retrait du parcours nominal vers `SegmentConfirmationRequired`. L’approche technique : `CameraViewModel` passe toujours une validation implicite au `AnalysisSubmissionGate` pour les prévisualisations gate (équivalent produit « analyse directe sur transcript complet »), supprime l’état `SegmentConfirmationRequired` du modèle d’état, et simplifie `CameraScreen` (chrome capture). Le domaine **`ingredient-normalization-validation`** reste la référence pour retirer progressivement la segmentation hors chemin nominal (déjà alignée sur transcript complet côté gate).

## Technical Context

**Language/Version**: Kotlin 2.x, Android (API min 26 cible projet)  
**Primary Dependencies**: Jetpack Compose, CameraX, coroutines, modules internes `camera`, `analysis.ingredientsegment`, `composition`  
**Storage**: N/A (états en mémoire ViewModel)  
**Testing**: JUnit 4/5, tests JVM `app/src/test`, AndroidTest `app/src/androidTest`  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: Pas de régression sur latence capture → navigation résultat (inchangé hors suppression d’étapes UI)  
**Constraints**: Constitution ATDD ; frontières DDD — comportement UX documenté ici, garde-fous transcript dans domaine ingrédients  
**Scale/Scope**: Écran capture + `CameraViewModel` + `ScanState` ; tests UI associés

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature G + contrats + tâches + tests UI |
| II. ATDD | OK — mise à jour `CaptureScreenFeatureFUiTest` / assertions absence chip & chaîne interdite |
| III. UX | OK — réduction friction (scan direct) |
| IV. Performance | OK — pas d’objectif nouveau ; pas de travail lourd ajouté |
| V. Simplicité | OK — suppression branches UI et état mort |
| VI. DDD | OK — UX et navigation dans UGE ; gate/transcript déjà partagés avec `ingredient-normalization-validation` via contrat implicite |

**Post-design** : inchangé.

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/camera/
├── CameraScreen.kt
├── CameraViewModel.kt
├── ScanState.kt
app/src/androidTest/java/com/miamia/camera/
├── CaptureScreenFeatureFUiTest.kt
app/src/main/java/com/miamia/analysis/ingredientsegment/
├── AnalysisSubmissionGate.kt   # inchangé comportement contractuel ; appelant force implicit=true
```

**Structure Decision** : modifications localisées au module capture et tests instrumentés associés ; pas de nouveau module.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
