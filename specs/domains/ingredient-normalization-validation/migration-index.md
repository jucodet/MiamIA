# Migration Index - ingredient-normalization-validation

## Source -> Target

- `001-scan-ingredients/spec.md` -> `spec.md` (`Scope`: structuration ingredients) [validated]
- `006-identify-photo-ingredients/spec.md` -> `spec.md` (`Scope`, `Functional Requirements`) [validated]
- `013-isoler-liste-ingredients/spec.md` -> `spec.md` (`Functional Requirements`, `Invariants`) [validated]
- `014-capture-liste-ingredients/spec.md` -> `spec.md` (`Functional Requirements`: priorite ancre `ingredients:`) [validated]
- `015-analyse-ocr-llm/spec.md` -> `spec.md` (`Invariants`: coherence liste affichee/analysee) [validated]

## Conflict Decisions

- Owner decision: la regle d'ancrage "premiere ancre valide `ingredients:`" prevaut sur le fallback "premier mot ingredients".
