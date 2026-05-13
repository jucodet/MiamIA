# Contrat — politique d’ancrage textuel v1 (`EquivalencePolicy`)

## Portée

- S’applique aux sorties classées **succès** pour les affirmations **fait produit** issues du flux LLM (composition + passages critique liés à **ce produit**).
- Ne remplace pas la validation amont du `ValidatedIngredientSegment` (`ingredient-normalization-validation`).

## Règles v1 (clarify 2026-05-13)

| Règle | Description |
|-------|-------------|
| G1 | Une affirmation « fait produit » MUST être étayée par une sous-chaîne du segment **ou** par une règle explicitement listée dans `mechanicalNormalizations`. |
| G2 | Aucune règle de synonyme implicite ; toute règle hors liste mécanique MUST être versionnée dans `EquivalencePolicy.synonymRules` (vide en v1). |
| G3 | **Tout ou rien** : ancrage partiel ⇒ `AnchoringOutcome.REJECTED_NON_ANALYSABLE` (**IHI-C-FR-003**). |

## Tests de conformité minimaux

- Jeu **positif** : segment fixe + bilan attendu ancré à 100 %.
- Jeu **négatif** : segment sans ingrédient X + tentative de bilan avec X ⇒ rejet.
