# Data Model — ingredient-health-intelligence

## Entities

- `HealthAnalysisReport` (agrégat existant)
  - `reportId`
  - `validatedSegment` : `ValidatedIngredientSegment`
  - `compositionSummary` : bilan structuré post-parse LLM
  - `populationCritiques` : sections critique par population
  - `additiveRiskFacts` : projection vers read-model additifs (référence, pas texte étiquette)
  - `generatedAt`
  - `anchoringOutcome` *(étendu)* : `AnchoringOutcome` — résultat du contrôle tout-ou-rien sur la partie « fait produit »

- `EquivalencePolicy` *(nouveau concept métier — versionné)*
  - `policyVersion`
  - `mechanicalNormalizations` : liste close (ex. `LOWERCASE`, `COLLAPSE_WHITESPACE`) — **seule** extension v1 autorisée par défaut
  - `synonymRules` : vide en v1 ; entrées futures explicites + version

- `GroundedProductClaim` *(vue logique pour audit)*
  - `claimText` : fragment de sortie présenté comme fait produit
  - `sourceSpan` : référence à sous-chaîne du `ValidatedIngredientSegment` **ou** clé de règle `EquivalencePolicy`

- `AttributedAdditiveInsights` *(projection UI / read-model)*
  - `attributionLabel` : texte fixe identifiant le domaine `additive-risk-insights`
  - `linkedAdditiveTokens` : liste de mentions **littérales** présentes dans le segment et couvertes par les faits affichés
  - `payload` : données structurées KPI (détail dans domaine additifs)

## Value Objects

- `ValidatedIngredientSegment` — immuable, seule source de vérité textuelle pour faits étiquette (**IHI-C-FR-001**).
- `AdditiveRiskFact` — déjà utilisé côté présentation ; ne doit pas être confondu avec libellé étiquette.
- `PopulationCritique` — sections ; chaque assertion « ce produit » doit être ancrable (**IHI-C-FR-004** b).
- `AnchoringOutcome` : `FULLY_GROUNDED` | `REJECTED_NON_ANALYSABLE` (mappe métier vers `non-analysable-response` ou équivalent).

## Aggregates

- **Root** : `HealthAnalysisReport`
  - Cohérence : aucun état **succès** si `anchoringOutcome != FULLY_GROUNDED` pour le bloc « fait produit » LLM analysé.
  - `additiveRiskFacts` peut coexister avec succès **uniquement** si contrat **IHI-C-FR-007** respecté.

## Invariants

- `validatedSegment` non vide pour une analyse lancée.
- `validatedSegment` immuable pendant le cycle de rapport.
- **IHI-C-FR-003** : pas de `FULLY_GROUNDED` si une `GroundedProductClaim` requise manque d’`sourceSpan` admissible.
- **IHI-C-FR-007** : chaque entrée `AttributedAdditiveInsights.linkedAdditiveTokens` ⊆ sous-chaînes littérales du segment (post **IHI-C-FR-005**).

## Validation rules (implémentation)

- Normaliser segment + candidats de claims avec **uniquement** `mechanicalNormalizations` v1 avant test de sous-chaîne.
- Ratio de rejet : tendre vers **0** ligne « fait produit » non ancrée en succès (stricter que le seuil historique 50 % si la spec prime).
