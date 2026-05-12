# Implementation Plan: Critique santé d’une liste d’ingrédients (prompt LLM)

**Branch**: `002-ingredient-health-critique` | **Date**: 2026-05-04 | **Spec**: `specs/002-ingredient-health-critique/spec.md`  
**Input**: Feature specification + clarifications (`## Clarifications` session 2026-05-04)

**Note**: Si `setup-plan.ps1` échoue (ex. branche Git non `NNN-feature`), régénérer ce fichier avec `SPECIFY_FEATURE_DIRECTORY=specs/002-ingredient-health-critique` sur une branche conforme, ou éditer ce plan directement.

## Summary

Après un **scan** avec **segment ingrédients validé**, l’utilisateur peut lancer une **critique d’impact santé** (quatre populations), avec prompt et inférence **Gemma locale** (LiteRT-LM, alignement **009**). L’**entrée** transmise au LLM MUST être **strictement identique** au **segment validé** (FR-001, **SC-005**) ; sur l’onglet critique santé, la liste est **affichée en lecture seule** — toute correction repasse par le **flux scan / revalidation** (clarification 2026-05-04). L’app affiche la réponse structurée (marqueurs `###…`), permet la **copie**, et **persiste** au minimum la dernière analyse (FR-006). Les cas sans segment validé ou liste trop courte sont **refusés** avec message clair (FR-005).

## Technical Context

**Language/Version**: Kotlin 2.x + Android (minSdk 26, targetSdk 34, Java 17)  
**Primary Dependencies**: Jetpack Compose Material3, Coroutines, **LiteRT-LM** (`com.google.ai.edge.litertlm:litertlm-android`), modèle Gemma `.litertlm` ; état **`ScanState.BilanReady`** / `CameraViewModel` comme source du **texte segment validé**  
**Storage**: `SharedPreferences` (ou fichier privé) pour snapshot « dernière analyse » incluant le **contenu du segment** analysé (FR-006)  
**Testing**: JUnit4, tests instrumentés ; tests d’alignement **texte LLM == segment validé** (SC-005) ; Compose pour lecture seule + copie (SC-003)  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: Réponse ou erreur explicite dans une fenêtre **inférieure à 30 s** perçue après chargement du modèle (alignement spec 009)  
**Constraints**: Pas d’envoi réseau du texte pour ce flux ; **pas d’édition** du segment sur l’écran critique santé ; identité **byte-for-byte** (ou normalisation documentée unique) entre affichage, payload LLM et persistance pour SC-005  
**Scale/Scope**: Onglet « Critique santé » dans `MainActivity` ; un moteur `HealthCritiqueEngine` + UI liée au **dernier bilan / segment validé** du parcours caméra

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit et code**: PASS — spec 002 + `research.md`, `data-model.md`, `contracts/`, `quickstart.md`.
- **ATDD d’abord**: PASS — scénarios spec + contrat `health-critique-llm-contract.md` (états + préconditions segment).
- **UX moderne et optimale**: PASS — lecture seule, messages sans segment, disclaimer, copie, accessibilité.
- **Performance exigence produit**: PASS — objectif temporal + SC-001 à **SC-005** mesurables.
- **Simplicité et évolutivité**: PASS — package `healthcritique` + pont minimal depuis `camera` / `MainActivity`.

**Post-Design Re-check**: PASS — entrée scan + lecture seule documentées dans `research.md` et contrat.

## Project Structure

### Documentation (this feature)

```text
specs/002-ingredient-health-critique/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── health-critique-llm-contract.md
└── tasks.md                    # /speckit.tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── camera/
│   ├── CameraViewModel.kt       # exposer / synchroniser le segment validé vers l’onglet santé
│   ├── CameraScreen.kt         # (optionnel) signal pour dernier segment
│   └── ScanState.kt
├── composition/                 # bilan 009 — source « vérité » segment après validation
├── healthcritique/
│   ├── HealthCritiqueEngine.kt
│   ├── HealthCritiquePromptBuilder.kt
│   ├── HealthCritiqueSectionParser.kt
│   ├── HealthCritiqueViewModel.kt   # reçoit segment en lecture seule ; pas de mutation locale liste
│   ├── HealthCritiqueScreen.kt      # OutlinedTextField readOnly ou Text + testTags
│   ├── LastHealthAnalysisStore.kt
│   └── LiteRtHealthCritiqueRunner.kt
├── MainActivity.kt              # onglets Caméra / Critique santé ; passage segment ViewModel ↔ caméra
app/src/test/java/com/miamia/healthcritique/
└── …                            # tests SC-005, parseur, validation « no segment »
```

**Structure Decision**: **`healthcritique`** reste le module domaine/UI de la critique ; le **couplage** avec le scan se fait au niveau **`MainActivity` / `CameraViewModel`** (segment validé comme seule source de `ingredientText` pour l’analyse).

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
