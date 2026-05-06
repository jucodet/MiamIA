# Quickstart: ingredient-phrase-segment

## But

Valider rapidement que l'isolation du segment ingrédients applique la règle canonique:
première ancre reconnue (`Ingrédient`, `Ingrédients`, `Ingredient`, `Ingredients`) puis borne de fin (`fin de phrase` -> `fin de ligne` -> `fin du texte`).

## Préconditions

- Application Android compilable.
- Parcours OCR local disponible pour produire un texte brut.
- Jeux de tests unitaires activables (`app/src/test/...`).

## Scénarios de vérification rapide

1. **Ancre FR + point final**
   - Entrée: `Ingrédients: sucre, sel. Traces possibles...`
   - Attendu: segment jusqu'au point final de la phrase ancre.

2. **Ancre EN sans ponctuation finale mais avec saut de ligne**
   - Entrée:
     - `Ingredients sugar, salt`
     - `May contain nuts`
   - Attendu: segment limité à la première ligne.

3. **Ancre EN monoligne sans ponctuation**
   - Entrée: `Ingredient sugar, salt and flour`
   - Attendu: segment limité à la fin du texte.

4. **Occurrences multiples**
   - Entrée: `Ingrédients: eau. Ingredients: sugar.`
   - Attendu: seule la première occurrence est retenue.

5. **Sans ancre reconnue**
   - Entrée: `Composition: eau, sel`
   - Attendu: état bloqué explicite, pas de segment validé automatique.

## Validation domaine

- Vérifier que l'analyse aval est bloquée sans confirmation explicite du segment.
- Vérifier la traçabilité `rawText -> proposal -> validatedSegment`.

## Commandes utiles (indicatives)

- Exécuter les tests de segment:
  - `./gradlew test --tests "*IngredientSegment*"`
- Exécuter les tests de reconnaissance liés à l'ancre:
  - `./gradlew test --tests "*IngredientAnchor*"`
