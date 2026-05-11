# Research - ingredient-normalization-validation (017-ocr-dot-end-capture)

## Decision 1: Le point comme terminateur contextuel (`.` + espace/newline)

- **Decision**: Modifier `IngredientSegmentBoundaryResolver.resolveEnd()` pour que `.` ne soit terminateur que s'il est suivi d'un espace `' '` ou d'un retour à la ligne `'\n'` (ou s'il est le dernier caractère de la ligne/texte). Les `!` et `?` restent des terminateurs inconditionnels.
- **Rationale**: Les étiquettes alimentaires contiennent fréquemment des points internes non terminaux : codes additifs (E.621, E.330), abréviations (vit.B12, conc.min), numéros de lot (L.12345). Traiter le `.` inconditionnellement coupe la liste prématurément sur ces cas réels. La condition contextuelle « suivi d'espace ou newline » distingue de façon fiable le point de fin de phrase (toujours suivi d'un espace ou d'un changement de paragraphe dans un texte OCR correctement reconnu) du point interne (collé au caractère suivant).
- **Alternatives considered**:
  - Regex négative lookbehind pour exclure les codes E.xxx → rejeté (trop spécifique, ne couvre pas toutes les formes).
  - Ignorer le point entièrement et ne garder que `!` / `?` → rejeté (le point de fin de phrase est le cas le plus fréquent sur les étiquettes françaises).
  - Exiger un double-espace ou double-newline → rejeté (trop restrictif, casse les cas valides).

## Decision 2: Pas de modification du modèle de données

- **Decision**: L'enum `IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR` reste inchangé. La sémantique « terminateur de phrase trouvé » couvre aussi bien le `.` contextuel que `!` ou `?`.
- **Rationale**: Ajouter un sous-type (`DOT_CONTEXTUAL` vs `UNCONDITIONAL_PUNCT`) n'apporte pas de valeur observable par l'utilisatrice ni par les consommateurs aval. Le contrat externe ne change pas.
- **Alternatives considered**:
  - Ajouter `DOT_SPACE_TERMINATOR` comme nouvelle valeur d'enum → rejeté (complexité de modèle sans bénéfice UX ou test).

## Decision 3: Le point en fin absolue de texte

- **Decision**: Un `.` en dernière position du texte (i.e. `text[text.length - 1] == '.'`) est considéré comme terminateur valide même sans espace/newline après, puisqu'il n'y a plus de caractère à examiner. Le texte se termine naturellement là.
- **Rationale**: Cas courant d'OCR où le texte est exactement « Ingrédients: eau, sel. » sans newline final. Le `.` terminal marque bien la fin de phrase.
- **Alternatives considered**:
  - Toujours exiger un caractère suivant → rejeté (casse le cas courant du point final).

## Decision 4: Fixtures de test enrichies

- **Decision**: Ajouter dans `OcrFixtures` au minimum les cas suivants :
  - `DOT_INTERNAL_ADDITIVE` : « Ingrédients: eau, colorant E.621, sucre, sel » (pas de terminaison au `.` interne).
  - `DOT_INTERNAL_ABBREVIATION` : « Ingredients: vit.B12, iron, zinc\nNext » (pas de terminaison au `.` interne, fin de ligne).
  - `DOT_SPACE_END` : « Ingrédients: eau, sucre. Traces de lait. » (terminaison au premier `. ` après ancre).
  - `DOT_NEWLINE_END` : « Ingrédients: eau, sucre.\nTraces de lait. » (terminaison au `.\n`).
  - `DOT_EOF_END` : « Ingrédients: eau, sucre. » (terminaison au `.` final, rien après).
- **Rationale**: Couvrir SC-001 (au moins un cas de point interne non suivi d'espace) et les variantes de terminaison contextuelle.
- **Alternatives considered**:
  - Ne tester que les cas existants → rejeté (SC-001 exige explicitement un cas de point interne).

## Decision 5: Pas d'impact sur les couches aval

- **Decision**: `IngredientSegmentPreparationService`, `AnalysisSubmissionGate`, `IngredientAnchorNormalizer`, et les écrans UI ne sont pas modifiés.
- **Rationale**: Le changement est encapsulé dans `IngredientSegmentBoundaryResolver.resolveEnd()`. Le contrat de sortie (`Resolution` avec `endIndexExclusive` et `boundaryEndReason`) reste identique. Les tests aval existants continuent de passer.
- **Alternatives considered**:
  - Refactorer aussi `IngredientAnchorNormalizer` pour une regex combinée ancre + fin → rejeté (mélange des responsabilités, complexité accrue).
