# Contrat — juxtaposition `additive-risk-insights` ↔ analyse LLM succès

**Consommateur** : `ingredient-health-intelligence` (UI / orchestration)  
**Fournisseur** : `additive-risk-insights` (read-model / KPI)

## Obligations (répond à **IHI-C-FR-007**)

1. **Ancrage littéral** : tout additif ou sujet d’enrichissement MUST correspondre à une désignation présente dans le `ValidatedIngredientSegment` après application **uniquement** de la **EquivalencePolicy** v1 (mécanique explicite).
2. **Attribution** : tout bloc d’enrichissement MUST porter un libellé ou identifiant d’attribution résolu vers le domaine additifs (pas « texte étiquette »).
3. **Non-confusion** : la présentation MUST respecter **IHI-C-FR-004** / **SC-C-003** (séparation sémantique et visuelle avec le bilan LLM).

## Interdictions

- Injecter des libellés réglementaires ou quantités **non** présents dans le segment comme s’ils provenaient de l’étiquette.
- Afficher des KPI pour des additifs absents du segment.

## Versioning

- Révisions du contrat : **MINOR** si nouveaux champs optionnels ; **MAJOR** si sémantique d’attribution ou d’ancrage change.
