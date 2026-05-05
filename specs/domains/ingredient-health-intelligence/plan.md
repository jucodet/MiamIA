# Domain Plan - ingredient-health-intelligence

## Objectives

1. Stabiliser le contrat d'entree `ValidatedIngredientSegment`.
2. Uniformiser la production `HealthAnalysisReport`.
3. Publier un langage aval robuste (`AdditiveRiskFacts`).

## Workstreams

- WS1: Contrat metier d'entree/sortie.
- WS2: Enforcement des invariants de verite source.
- WS3: Couche de publication vers domaines aval.

## Dependencies

- Upstream: `ingredient-normalization-validation` (Customer/Supplier).
- Platform: `local-llm-runtime` (Conformist), `traceability-storage` (Shared Kernel restreint).

## Acceptance

- Entree analysee egale au segment valide dans 100% des cas.
- Rapport structure disponible meme en cas de refus metier.
