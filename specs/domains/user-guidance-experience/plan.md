# Implementation Plan: photo-capture-llm-result-flow

**Branch**: `016-ingredient-phrase-segment` (outil) | **Spec branch référencée**: `017-photo-analyse-ecran-resultat` | **Date**: 2026-05-06 | **Spec**: [`spec.md`](./spec.md)

**Input**: Spécification domaine `user-guidance-experience` — accueil = écran capture, sans onglets, loader sur capture, navigation résultat conditionnelle, abandon FR-014, retour capture FR-016.

**Note**: Phases 0–1 documentées ci-dessous ; `tasks.md` est produit par `/speckit-tasks` (Phase 2 livrable).

## Summary

Faire de l’écran de **prise de photo** le **premier écran** et la **racine** de la navigation principale (**FR-001**, **FR-015**, **SC-007**) : prévisualisation (ou message si caméra indisponible), bouton photo, puis bouton test LLM. Après photo ou test, afficher un **loader sur l’écran capture** jusqu’à l’état terminal (**FR-006**, **FR-013**), puis **naviguer vers un écran résultat** seulement si l’utilisatrice est **restée sur la capture** (**FR-007**, **FR-010**, **FR-014**). **Retour** depuis le résultat → **capture**, sans réintroduction d’onglets (**FR-016**). Décisions détaillées dans [`research.md`](./research.md) ; contrat observable dans [`contracts/capture-llm-result-navigation-contract.md`](./contracts/capture-llm-result-navigation-contract.md).

## Technical Context

**Language/Version**: Kotlin (JVM cible Android), Gradle Kotlin DSL  
**Primary Dependencies**: Jetpack Compose, Navigation Compose, CameraX (prévisualisation / capture), ViewModel, coroutines ; gateway LLM local existant (ex. `AndroidGemma4LocalGateway` / mocks selon build)  
**Storage**: N/A pour ce flux UX (état écran / run en mémoire ; persistance hors périmètre sauf réutilisation existante)  
**Testing**: Tests instrumentés Android (`androidTest`), tests unitaires JVM si logique pure ; ATDD aligné constitution (scénarios Given/When/Then de la spec)  
**Target Platform**: Android (API minimale du module `app`)  
**Project Type**: application mobile monolithique (`app/`)  
**Performance Goals**: **SC-003** (loader &lt; 1 s après début traitement), **SC-007** (écran capture sans onglets &lt; 3 s cold start représentatif), latence résultat **SC-004**  
**Constraints**: pas de navigation résultat après abandon pendant chargement (**FR-014**) ; un seul run LLM actif ; boutons photo et test désactivés pendant `in_progress` (research Decision 8)  
**Scale/Scope**: un graphe principal capture → résultat LLM ; pas de barre d’onglets multi-sections (**FR-015**)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| **I. Qualité / traçabilité** | Spec domaine + contrat UI + modèle données ; incrément testable par user story. |
| **II. ATDD** | Scénarios US1–US4 et edge cases dans `spec.md` ; quickstart + contrat pour validation manuelle et cibles de tests auto. |
| **III. UX** | Parcours simple, feedback loader, erreurs explicites (**FR-010**, **FR-011**). |
| **IV. Performance** | **SC-001**–**SC-007** dans la spec. |
| **V. Simplicité** | Navigation pile simple ; pas de sur-couche sans besoin. |
| **VI. DDD** | Domaine `user-guidance-experience` ; sémantique du texte LLM hors périmètre (research Decision 4). |

**Post-design (Phase 1)** : aucune violation nouvelle ; frontières préservées (`data-model.md`, contrat limité à l’observable UI).

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1
└── tasks.md             # /speckit-tasks (non créé par ce plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/foodgpt/
├── MainActivity.kt                    # Point d’entrée Compose ; shell sans TabRow principal
├── camera/                            # CameraScreen, CameraViewModel, capture
├── navigation/                        # Routes / NavHost capture ↔ résultat
├── result/ ou équivalent              # LlmResultScreen, payload d’affichage
├── home/                              # HomeLlmMockRunner (réutilisation FR-008)
├── gemma4local/                       # Gateway / modèles résultat analyse
└── …                                  # Autres packages inchangés hors refactor navigation

app/src/androidTest/java/com/foodgpt/  # Tests parcours / smoke cold start si ajoutés
```

**Structure Decision**: Module unique `app` ; refactor **MainActivity** pour retirer l’onglet comme navigation **primaire** et faire du flux capture (+ sous-route résultat) la **racine**, conformément à research Decisions 10–11.

## Phase 0 — Recherche

**Statut**: terminé — voir [`research.md`](./research.md). Aucun `NEEDS CLARIFICATION` restant pour ce périmètre.

## Phase 1 — Design & contrats

**Statut**: terminé.

- [`data-model.md`](./data-model.md) — entités `AppNavigationShell`, `CaptureScreenUiSession`, `LlmPipelineRun`, `LlmResultScreenPayload`.
- [`contracts/capture-llm-result-navigation-contract.md`](./contracts/capture-llm-result-navigation-contract.md) — commandes observables, états, navigation.
- [`quickstart.md`](./quickstart.md) — validation manuelle (cold start, onglets, FR-016, abandon).

**Agent context**: `.cursor/rules/specify-rules.mdc` pointe déjà vers `specs/domains/user-guidance-experience/plan.md`.

## Phase 2 — Tâches d’implémentation

Hors fichier : exécuter **`/speckit-tasks`** pour générer ou mettre à jour `tasks.md` (refactor navigation, tests ATDD **SC-007** / **FR-015** / **FR-016**, etc.).

## Complexity Tracking

> Aucune violation constitutionnelle à justifier pour ce plan.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
