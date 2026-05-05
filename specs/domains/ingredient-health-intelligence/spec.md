# Domain Spec - ingredient-health-intelligence

## Purpose

Produire une decision sante fiable a partir d'un `ValidatedIngredientSegment`.

## Scope

- Bilan composition.
- Critique sante par population.
- Invariant de verite source entre segment valide, payload LLM et resultat persiste.
- Redirection post-scan vers analyse et affichage conjoint "liste source + bilan".
- Gestion d'erreurs runtime analyse avec repli explicite.

## Out of Scope

- Capture camera et OCR.
- Isolation initiale du segment.
- Presentation KPI detaillee.

## Functional Requirements

- IHI-FR-001: le domaine MUST refuser toute analyse sans `ValidatedIngredientSegment`.
- IHI-FR-002: l'entree d'analyse MUST etre strictement identique au segment valide.
- IHI-FR-003: la sortie MUST exposer un `HealthAnalysisReport` structure.
- IHI-FR-004: le domaine MUST publier des `AdditiveRiskFacts` pour consommation aval.
- IHI-FR-005: le domaine MUST structurer les analyses sante par populations cibles et expliciter incertitudes/prudence (pas de diagnostic).
- IHI-FR-006: le domaine MUST maintenir l'honnetete resultat en cas d'echec runtime (message explicite, pas de faux bilan complet).
- IHI-FR-007: le domaine MUST conserver au minimum la derniere analyse avec reference exacte au segment source analyse.

## Invariants

- Un segment valide actif par execution d'analyse.
- Le hash de l'entree analysee est trace avec le rapport.
- Le texte affiche comme "liste analysee" correspond exactement a la charge utile d'analyse.

## Source Mapping

- `specs/002-ingredient-health-critique/spec.md`
- `specs/009-llm-bilan-composition-ingredients/spec.md`
- `specs/015-analyse-ocr-llm/spec.md`
- `specs/011-api-gemma4-telephone/spec.md` (contraintes runtime local pour appels LLM)
