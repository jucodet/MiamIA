# Domain Spec - additive-risk-insights

## Purpose

Produire des KPI additifs/risques lisibles a partir de faits publies par le core.

## Scope

- Parsing contrat publie.
- Projection KPI et resume risque.
- Justifications courtes et coherent mapping couleurs.
- Gestion des donnees partielles/incoherentes (a confirmer).

## Invariants

- Chaque KPI doit referencer un fait source.
- Les labels de risque sont normalises.
- Le tri de criticite est stable (rouge > orange > vert).

## Functional Requirements

- ARI-FR-001: le domaine MUST produire une liste ordonnee d'additifs avec niveau de risque normalise et justification concise.
- ARI-FR-002: le domaine MUST calculer des KPI globaux coherents avec les elements detailles (totaux + repartition couleurs).
- ARI-FR-003: le domaine MUST signaler les lignes incompletes/incoherentes sans masquer l'incertitude.
- ARI-FR-004: le domaine SHOULD fournir un niveau de confiance ou statut de validation par item pour la projection UI.

## Cross-domain Notes

- Les recommandations globales et alternatives plus saines restent owner de `ingredient-health-intelligence`.
- L'orchestration d'affichage ecran est owner de `user-guidance-experience`.

## Source Mapping

- `specs/003-additive-kpi-results/spec.md`
- `specs/004-llm-summary-recommendations/spec.md` (liaison resume/recommandations en amont du detail KPI)
