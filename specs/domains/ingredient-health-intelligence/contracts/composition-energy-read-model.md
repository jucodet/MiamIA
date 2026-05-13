# Contrat — read-model estimation énergie (IHI → UGE)

**Domaine source** : `ingredient-health-intelligence` (Feature K)  
**Domaine consommateur** : `user-guidance-experience` (**UGE-A-FR-022**)

## Champs exposés à l’UI

| Champ logique | Type v1 | Règle |
|---------------|---------|--------|
| `analysisComplete` | dérivé du flux existant | Pastille affichée sur bilan composition **succès** (même critère que carte bilan aujourd’hui). |
| `estimatedKcalPer100g` | `Int?` | Non nul uniquement si parse + **EnergyEstimateValidator** acceptent la valeur (**1..1100** kcal/100 g, entier). |
| `energyLabelQualifier` | texte UI | MUST inclure « estim » / « indicatif » (ou équivalent) lorsque `estimatedKcalPer100g != null` (**IHI-K-FR-003**). |
| `energyUnavailableCopy` | texte UI | Lorsque `estimatedKcalPer100g == null`, pas de nombre inventé ; libellé du type « estimation indisponible » ou pastille limitée à l’état « analyse terminée » (**US-K2**). |

## Invariants

- L’UI MUST NOT présenter `estimatedKcalPer100g` comme valeur réglementaire tableau nutritionnel (**IHI-K-FR-005**).
- Aucune obligation d’afficher un entier : `null` est un état produit valide.

## Versioning

- **v1** : section `###ENERGIE_ESTIMEE` + entier borné ; évolutions (champ structuré, seconde passe) = bump de contrat + mise à jour `plan.md`.
