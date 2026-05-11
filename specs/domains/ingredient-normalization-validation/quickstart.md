# Quickstart - ocr-dot-end-capture (017)

## Goal

Valider manuellement que la logique de fin de capture du segment ingrédients traite correctement le `.` contextuel : un point suivi d'un espace ou d'un retour à la ligne termine la capture, un point interne (code additif, abréviation) ne la termine pas.

## Preconditions

- Build Android fonctionnel (`app`).
- Spec : `specs/domains/ingredient-normalization-validation/spec.md`.
- Contrat : `contracts/boundary-resolver-contract.md`.
- Tests JVM passent : `./gradlew :app:testDebugUnitTest`.

## Manual Validation Flow

### A. Point interne — pas de coupure

1. Depuis l'écran de capture, prendre en photo (ou fournir via mock) une étiquette contenant un texte de type :
   `Ingrédients: eau, colorant E.621, sucre, sel`
2. Vérifier que la proposition de segment contient **tout** le texte après l'ancre, y compris `E.621` — la capture ne s'arrête **pas** au `.` de `E.621`.

### B. Point + espace — fin de capture

1. Fournir un texte de type :
   `Ingrédients: eau, sucre, sel. Traces possibles de gluten.`
2. Vérifier que la proposition de segment se termine à `sel.` (premier `. ` après l'ancre), pas à `gluten.`.

### C. Point + retour à la ligne — fin de capture

1. Fournir un texte de type :
   ```
   Ingrédients: eau, sucre.
   Traces possibles de gluten.
   ```
2. Vérifier que la proposition se termine à `sucre.` (le `.\n` est reconnu).

### D. Point en fin de texte — fin de capture

1. Fournir un texte de type :
   `Ingrédients: eau, sel.`
2. Vérifier que la proposition se termine à `sel.` (le `.` en fin de texte est reconnu).

### E. Abréviation suivie de virgule — pas de coupure

1. Fournir un texte de type :
   `Ingredients: vit.B12, iron, zinc`
2. Vérifier que la proposition contient `vit.B12, iron, zinc` sans coupure prématurée.

### F. Ponctuation ! et ? — terminateurs inconditionnels

1. Fournir un texte de type :
   `Ingrédients: eau, sel!`
2. Vérifier que la proposition se termine à `sel!`.

## Suggested Automated Checks

- Tests unitaires JVM : `IngredientSegmentBoundaryResolverTest` (cas BC-01 à BC-07 du contrat).
- Tests d'acceptation : `IngredientSegmentPhraseBoundaryAcceptanceTest` (scénarios US1 §1 et §2 de la spec).
- Test de non-régression : `IngredientSegmentPerformanceTest` (pas de régression latence).

## Expected Outcomes

- Conformité à **SC-001** (100 % des propositions respectent FR-002 à FR-006, y compris au moins un cas de point interne).
- Aucune régression sur les tests existants (ancre, fin de ligne, fin de texte, multiples ancres).
