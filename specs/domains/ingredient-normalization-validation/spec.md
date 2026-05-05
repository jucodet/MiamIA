# Domain Spec - ingredient-normalization-validation

## Purpose

Transformer un `RawOcrText` en `ValidatedIngredientSegment` fiable, verifiable par l'utilisateur et exploitable par les domaines d'analyse.

## Scope

- Detection d'ancre ingredients.
- Isolation de segment.
- Correction et validation utilisateur.
- Gestion des cas sans ancre exploitable.
- Strategie d'ancrage prioritaire sur la forme `ingredients:`.

## Invariants

- Le segment valide est immuable pour une analyse donnee.
- Les regles d'isolation sont deterministes et tracees.
- L'analyse aval ne demarre jamais sans confirmation explicite du segment isole.

## Functional Requirements

- INV-FR-001: le domaine MUST prioriser une ancre de type `ingredients:` (casse/espaces/accents tolerees) pour demarrer l'isolation.
- INV-FR-002: le domaine MUST appliquer une borne de fin deterministe (premier retour ligne, sinon fin de texte).
- INV-FR-003: le domaine MUST afficher le segment retenu avant analyse et exiger une confirmation utilisateur explicite.
- INV-FR-004: le domaine MUST bloquer l'analyse et proposer reprise/correction si ancre absente, segment vide, ou segment inexploitable.
- INV-FR-005: le domaine MUST garantir la tracabilite entre `RawOcrText`, segment isole, segment valide.

## Cross-domain Notes

- Le traitement OCR brut reste dans `capture-recognition`.
- L'exploitation sante/composition du segment valide est proprietaire de `ingredient-health-intelligence`.

## Source Mapping

- `specs/001-scan-ingredients/spec.md` (structuration/correction)
- `specs/006-identify-photo-ingredients/spec.md`
- `specs/013-isoler-liste-ingredients/spec.md`
- `specs/014-capture-liste-ingredients/spec.md`
- `specs/015-analyse-ocr-llm/spec.md` (coherence liste affichee/analysee)
