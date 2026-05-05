# Domain Spec - user-guidance-experience

## Purpose

Orchestrer l'experience utilisateur transversale sans polluer les modeles metier.

## Scope

- Message de bienvenue.
- Bloc readiness/home.
- Transitions et redirections de flux.
- Affichage des etats de progression et erreurs actionnables.

## Invariants

- Les transitions respectent les preconditions publiees par les BC metier.
- La UI ne mutile pas les objets metier publies.
- L'utilisateur peut toujours revenir vers un parcours de reprise apres erreur.

## Functional Requirements

- UGE-FR-001: le domaine MUST afficher l'accueil selon l'ordre de reference (statut readiness, message bienvenue, preview, bouton capture) en mode portrait.
- UGE-FR-002: le domaine MUST appliquer une redirection automatique OCR->analyse quand les preconditions metier sont satisfaites.
- UGE-FR-003: le domaine MUST afficher des messages clairs et actions de reprise en cas d'echec capture/OCR/analyse.
- UGE-FR-004: le domaine MUST maintenir la lisibilite de la source analysee et du resultat sur l'ecran d'analyse.
- UGE-FR-005: le domaine MUST garantir que les variations de message de bienvenue restent positives et non bloquantes.

## Cross-domain Notes

- La regle de validite OCR appartient a `capture-recognition`.
- La regle d'isolation/validation du segment ingredients appartient a `ingredient-normalization-validation`.
- Les regles de contenu du bilan appartiennent a `ingredient-health-intelligence`.

## Source Mapping

- `specs/010-message-bienvenue-sourire/spec.md`
- `specs/012-home-layout-mediapipe-status/spec.md`
- `specs/015-analyse-ocr-llm/spec.md`
- `specs/005-camera-start-temp-scan/spec.md` (etats UX scan)
