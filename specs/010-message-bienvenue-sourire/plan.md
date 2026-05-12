# Implementation Plan: Message de bienvenue souriant

**Branch**: `010-message-bienvenue-sourire` | **Date**: 2026-04-27 | **Spec**: `specs/010-message-bienvenue-sourire/spec.md`  
**Input**: Feature specification from `specs/010-message-bienvenue-sourire/spec.md`

## Summary

Ajouter un affichage de message de bienvenue apres connexion reussie, choisi aleatoirement depuis une bibliotheque fixe de messages pre-valides en francais, avec repetition immediate autorisee. Si la bibliotheque est vide, aucun message n'est affiche et le parcours utilisateur reste fonctionnel.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (Compose)  
**Primary Dependencies**: Jetpack Compose, ViewModel, Coroutines  
**Storage**: N/A (lecture d'une bibliotheque de messages predefinie, sans persistance obligatoire)  
**Testing**: JUnit4 + tests UI Compose + tests instrumentes Android  
**Target Platform**: Application mobile Android  
**Project Type**: Mobile app  
**Performance Goals**: message visible en moins de 2 secondes apres affichage ecran d'accueil (SC-001)  
**Constraints**: affichage uniquement apres connexion reussie; messages en francais; aucun fallback si bibliotheque vide  
**Scale/Scope**: un affichage par connexion reussie; v1 centree sur utilisateur connecte

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualite produit et code**: PASS - exigences tracees spec -> plan -> artefacts, impacts limites et testables.
- **ATDD d'abord**: PASS - scenarios Given/When/Then deja presents et exploitables en tests de parcours.
- **UX moderne et optimale**: PASS - parcours sans friction, comportement explicite en cas de bibliotheque vide.
- **Performance exigence produit**: PASS - objectif de delai d'affichage measurable (SC-001).
- **Simplicite et evolutivite**: PASS - solution locale simple (bibliotheque fixe + selection aleatoire), sans sur-architecture.

**Post-Design Re-check**: PASS - `research.md`, `data-model.md`, `contracts/` et `quickstart.md` couvrent les decisions et preservent les gates.

## Project Structure

### Documentation (this feature)

```text
specs/010-message-bienvenue-sourire/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── welcome-message-display-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/main/java/com/miamia/
    ├── camera/ or auth/flow module (existing login success trigger)
    ├── ingredients/ (existing UI/state patterns to align)
    └── welcome/ (new package suggested)
        ├── WelcomeMessageProvider.kt
        ├── WelcomeMessageSelector.kt
        ├── WelcomeMessagePolicy.kt
        └── WelcomeMessageUiState.kt
```

**Structure Decision**: etendre l'application Android existante avec un sous-module `welcome` dedie a la selection/qualification du message afin de garder une separation claire entre logique d'authentification et logique UX de bienvenue.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
