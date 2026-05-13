# Implementation Plan: OCR intégral vers LLM (FR-012 / FR-014)

**Branch**: `016-full-ocr-llm` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Évolution spec — suppression de la segmentation comme entrée obligatoire du modèle de langage ; entrée = texte OCR sessionnel intégral.

## Summary

Le pipeline capture → reconnaissance → **analyse composition (LLM)** doit transmettre au moteur d’inférence l’**intégralité** du transcript OCR (`joinToString` des lignes normalisées), sans utiliser le segment délimité par ancrage (FR-001–FR-006) comme payload d’analyse. Les règles d’ancrage restent disponibles pour des **vues auxiliaires** futures ; le **garde-fou de soumission** (`AnalysisSubmissionGate`) s’appuie désormais sur le **transcript complet** (non vide, non « label seul ») plutôt que sur la présence d’ancre. `CameraViewModel` enchaîne `runCompositionStage` avec ce transcript ; `confirmSegmentAndAnalyze` re-prépare l’extraction sur le transcript complet et réévalue le gate en `userConfirmed = true`.

## Technical Context

**Language/Version**: Kotlin 2.x (Android), Gradle  
**Primary Dependencies**: Jetpack Compose, CameraX, coroutines, tests JUnit4 + Robolectric  
**Storage**: N/A (état session ViewModel, `StateFlow`)  
**Testing**: `app/src/test/...`, `./gradlew :app:testDebugUnitTest`  
**Target Platform**: Android (API niveau projet)  
**Project Type**: application mobile monolithique (`app/`)  
**Performance Goals**: pas de régression sur la latence post-OCR avant inférence (pas de traitement supplémentaire O(n) au-delà du trim / regex existants sur le transcript déjà construit)  
**Constraints**: respect FR-008 (OCR vide / échec → pas d’analyse) ; alignement `CompositionResultValidator` sur texte source élargi (ancrage des lignes dans le transcript complet)  
**Scale/Scope**: `AnalysisSubmissionGate`, `CameraViewModel`, tests contrat gate ; copie micro des contrats domaine

## Constitution Check

| Principe | Statut |
|----------|--------|
| I Qualité / traçabilité | OK — spec + tests gate + flux caméra |
| II ATDD | OK — tests contrat gate mis à jour + scénario implicite sans ancre |
| III UX | OK — écran de confirmation affiche le même `segmentPreview` désormais alimenté par le transcript intégral |
| IV Performance | OK — pas de duplication OCR |
| V Simplicité | OK — garde-fou centralisé dans le gate |
| VI DDD | OK — périmètre `ingredient-normalization-validation` ; ref aval `ingredient-health-intelligence` noté en assumption spec |

**Gate**: aucune violation justifiant la table Complexity.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-normalization-validation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
├── spec.md
└── tasks.md          # généré par /speckit-tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── analysis/ingredientsegment/AnalysisSubmissionGate.kt
├── camera/CameraViewModel.kt
└── ...

app/src/test/java/com/miamia/analysis/ingredientsegment/
├── AnalysisSubmissionGateContractTest.kt
└── AnalysisSubmissionDecisionAcceptanceTest.kt
```

**Structure Decision**: module Android unique `app` ; logique métier segment/gate sous `analysis.ingredientsegment` ; orchestration scan sous `camera`.

## Complexity Tracking

*(non applicable)*
