# Contract: Ingredient Segment Boundary

## Purpose

Définir le contrat fonctionnel entre le texte OCR fourni au domaine et le résultat d'isolation de la liste d'ingrédients.

## Input

```json
{
  "sessionId": "string",
  "rawText": "string"
}
```

## Output

```json
{
  "sessionId": "string",
  "status": "proposal_ready | blocked_no_anchor | blocked_empty_segment",
  "anchor": {
    "token": "Ingrédient | Ingrédients | Ingredient | Ingredients",
    "startIndex": 0,
    "lineIndex": 0
  },
  "boundary": {
    "startIndex": 0,
    "endIndex": 42,
    "endReason": "sentence_terminator | line_end | text_end"
  },
  "proposalText": "string",
  "message": "string"
}
```

## Rules

1. L'ancre retenue est la première occurrence reconnue dans l'ordre de lecture.
2. La borne de fin suit la hiérarchie:
   - `sentence_terminator` (`.`, `!`, `?`) si présent après ancre dans la même phrase,
   - sinon `line_end`,
   - sinon `text_end`.
3. Si aucune ancre n'est trouvée: `status=blocked_no_anchor`, `proposalText` vide.
4. Si ancre trouvée mais segment vide/invalide: `status=blocked_empty_segment`.
5. Aucune analyse aval n'est autorisée tant qu'un segment validé n'est pas confirmé.

## Error Semantics

- `blocked_no_anchor`: aucune ancre reconnue.
- `blocked_empty_segment`: extraction impossible ou segment vide.

## Backward Compatibility

- Ce contrat remplace les formulations historiques basées sur une ancre prioritaire `ingredients:` et une fin systématique au premier saut de ligne.
