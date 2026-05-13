# Implementation Plan: captured-text-scroll-layout

**Branch**: `019-captured-text-scroll` | **Date**: 2026-05-13 | **Spec**: [`spec.md`](./spec.md) (CR-FR-006 à CR-FR-008, incrément zone défilante)
**Input**: Spécification domaine `capture-recognition` — texte capturé / `RawOcrText` consultable dans une zone défilante, actions principales toujours visibles en bas.

**Note** : Phases 0–1 ci-dessous. `tasks.md` est produit par `/speckit-tasks` (phase 2).

## Summary

Corriger la mise en page des écrans de la route capture qui affichent un texte OCR ou segment long avec des boutons en dessous : appliquer un **schéma deux zones** (contenu texte dans une zone à hauteur flexible avec **défilement vertical interne**, bandeau d’**actions fixes** sous la zone) au lieu d’un `Column` entièrement défilant ou sans contrainte de hauteur. Priorité aux états où le problème est avéré aujourd’hui (`ScanState.Success` « texte brut », `ScanState.BilanReady` avec carte longue + bouton) et alignement / renforcement de `ScanState.SegmentConfirmationRequired` pour consommer correctement la hauteur disponible sous la bannière MediaPipe / welcome.

## Technical Context

**Language/Version** : Kotlin, JVM cible Android (aligné `app/` existant)  
**Primary Dependencies** : Jetpack Compose (Material3), `CameraViewModel` / `ScanState`  
**Storage** : N/A pour cet incrément (pas de nouveau persistant)  
**Testing** : AndroidJUnit4 (`app/src/androidTest/`), tests UI existants `IngredientSegmentConfirmationUiTest` ; compléments ciblés sur tags Compose existants / nouveaux si besoin  
**Target Platform** : Android (API min/projet existants)  
**Project Type** : application mobile monolithique (`app/`)  
**Performance Goals** : défilement fluide 60 fps visé sur texte long ; pas de recomposition excessive (décisions dans `research.md`)  
**Constraints** : respect CR-FR-005 (texte vide) ; pas de fuite de règles métier vers `ingredient-normalization-validation` ; changement limité à la couche présentation capture  
**Scale/Scope** : 1 fichier principal `CameraScreen.kt` (+ éventuel extrait composable si factorisation justifiée) ; 1–2 tests UI ou smoke manuel documenté dans `quickstart.md`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| **I. Qualité / traçabilité** | Spec domaine + scénarios Given/When/Then ; plan relie exigences à fichiers et tests. |
| **II. ATDD** | Scénarios P1/P2 dans `spec.md` ; tests UI à mettre à jour ou ajouter avant merge. |
| **III. UX** | Conforme : actions visibles, texte long consultable, cas clavier / paysage mentionnés en spec. |
| **IV. Performance** | Objectif fluidité scroll ; pas de traitement OCR supplémentaire. |
| **V. Simplicité** | Réutiliser le pattern déjà présent dans `LlmResultScreen` (`weight(1f)` + `verticalScroll`). |
| **VI. DDD** | Reste dans `capture-recognition` ; pas de changement de contrat ACL vers la normalisation. |

**Post-design** : aucune violation ; pas de section Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/domains/capture-recognition/
├── plan.md              # Ce fichier
├── spec.md
├── research.md          # Phase 0
├── data-model.md        # Phase 1 (complété)
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1 — contrat UI comportemental
├── checklists/
└── tasks.md             # /speckit-tasks (hors périmètre de cette commande)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/camera/
├── CameraScreen.kt              # États ScanState : layout scroll + pied d’actions
├── CameraViewModel.kt           # Lecture seule si aucun changement d’état requis
└── ScanState.kt                 # Référence types (pas de changement attendu)

app/src/androidTest/java/com/miamia/camera/ingredientsegment/
└── IngredientSegmentConfirmationUiTest.kt   # À étendre ou parcours Success brut
```

**Structure Decision** : monolithe `app/` ; toute la logique de cet incrément est **UI Compose** dans le module caméra existant, en cohérence avec `LlmResultScreen.kt` (référence de pattern pied de page fixe).

## Complexity Tracking

> Non applicable — aucune violation constitutionnelle.
