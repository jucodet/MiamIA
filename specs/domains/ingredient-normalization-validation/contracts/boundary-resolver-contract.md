# Contract - IngredientSegmentBoundaryResolver.resolveEnd (FR-003 révisé)

## Purpose

Contracter le comportement observable de la résolution de borne de fin du segment ingrédients, après la révision FR-003 (017-ocr-dot-end-capture).

## Preconditions

- `text` : texte OCR brut non vide.
- `anchorIndex` : index valide (0 ≤ anchorIndex < text.length) pointant sur le début de l'ancre détectée par `IngredientAnchorNormalizer`.

## Règles de terminaison (priorité descendante)

### 1. Terminateur de phrase (SENTENCE_TERMINATOR)

Scanner le texte depuis `anchorIndex` jusqu'à la fin de ligne (premier `'\n'` ou fin de texte) :

| Caractère | Condition pour être terminateur | Inclus dans le segment |
|-----------|-------------------------------|----------------------|
| `.` | Suivi d'un espace (`' '`), d'un `'\n'`, ou en dernière position du texte | Oui (endIndexExclusive = index + 1) |
| `!` | Aucune condition supplémentaire | Oui (endIndexExclusive = index + 1) |
| `?` | Aucune condition supplémentaire | Oui (endIndexExclusive = index + 1) |

Le premier caractère satisfaisant ces conditions détermine `endIndexExclusive`.

### 2. Fin de ligne (LINE_END)

Si aucun terminateur de phrase n'est trouvé avant le `'\n'` :
- `endIndexExclusive` = index du `'\n'`
- `boundaryEndReason` = `LINE_END`

### 3. Fin de texte (TEXT_END)

Si ni terminateur de phrase ni `'\n'` ne sont trouvés :
- `endIndexExclusive` = `text.length`
- `boundaryEndReason` = `TEXT_END`

## Cas de test contractuels

| ID | Input (après ancre) | Attendu | Reason |
|---|---|---|---|
| BC-01 | `": eau, sel. Traces"` | `": eau, sel."` | `. ` = SENTENCE_TERMINATOR |
| BC-02 | `": eau, E.621, sel\nTraces"` | `": eau, E.621, sel"` | `E.6` = point interne → LINE_END |
| BC-03 | `": vit.B12, fer"` | `": vit.B12, fer"` | point interne + TEXT_END |
| BC-04 | `": eau, sel."` (fin de texte) | `": eau, sel."` | `.` en fin de texte = SENTENCE_TERMINATOR |
| BC-05 | `": eau, sel!\nTraces"` | `": eau, sel!"` | `!` = SENTENCE_TERMINATOR (inconditionnel) |
| BC-06 | `": eau, sel?\nTraces"` | `": eau, sel?"` | `?` = SENTENCE_TERMINATOR (inconditionnel) |
| BC-07 | `": eau, sel.\nTraces"` | `": eau, sel."` | `.\n` = SENTENCE_TERMINATOR |

## Garanties

- Le contrat de sortie `Resolution(endIndexExclusive, boundaryEndReason)` est inchangé.
- Les consommateurs aval (`IngredientSegmentPreparationService`, `AnalysisSubmissionGate`) ne nécessitent pas de modification.
- Un `.` non suivi d'un espace ou d'un retour à la ligne (et qui n'est pas en fin de texte) ne produit **jamais** `SENTENCE_TERMINATOR`.
