# Data Model: ingredient-phrase-segment

## Entities

### IngredientExtractionSession

- **Description**: session fonctionnelle qui relie le texte OCR d'entrée, la proposition de segment et la validation utilisateur.
- **Fields**:
  - `sessionId` (string, unique)
  - `rawText` (string, required)
  - `status` (`ready_for_proposal` | `proposal_ready` | `blocked_no_anchor` | `blocked_empty_segment` | `validated`)
  - `createdAt` (datetime)
  - `updatedAt` (datetime)
- **Validation Rules**:
  - `rawText` non vide pour tenter l'isolation.
  - `validated` interdit sans proposition non vide.

## Value Objects

### IngredientAnchorMatch

- **Description**: résultat de détection de la première ancre reconnue.
- **Fields**:
  - `token` (`Ingrédient` | `Ingrédients` | `Ingredient` | `Ingredients`)
  - `startIndex` (int, >= 0)
  - `lineIndex` (int, >= 0)
- **Validation Rules**:
  - représente uniquement la première occurrence dans l'ordre de lecture.

### SegmentBoundary

- **Description**: bornes calculées du segment à proposer.
- **Fields**:
  - `startIndex` (int)
  - `endIndex` (int, exclusif)
  - `endReason` (`sentence_terminator` | `line_end` | `text_end`)
- **Validation Rules**:
  - `endIndex > startIndex`
  - `endReason` suit la hiérarchie: ponctuation de phrase > fin de ligne > fin de texte.

### IngredientSegmentProposal

- **Description**: segment extrait avant confirmation.
- **Fields**:
  - `text` (string)
  - `anchor` (`IngredientAnchorMatch`)
  - `boundary` (`SegmentBoundary`)
- **Validation Rules**:
  - `text` non vide.
  - cohérence index avec `rawText`.

### ValidatedIngredientSegment

- **Description**: segment final confirmé (ou corrigé) par l'utilisatrice.
- **Fields**:
  - `text` (string, required)
  - `confirmedByUser` (boolean, true)
  - `sourceProposalHash` (string)
- **Validation Rules**:
  - `confirmedByUser` doit être vrai pour autoriser l'analyse aval.

## Relationships

- `IngredientExtractionSession` 1 -> 0..1 `IngredientSegmentProposal`
- `IngredientExtractionSession` 1 -> 0..1 `ValidatedIngredientSegment`
- `IngredientSegmentProposal` 1 -> 1 `IngredientAnchorMatch`
- `IngredientSegmentProposal` 1 -> 1 `SegmentBoundary`

## State Transitions

- `ready_for_proposal` -> `proposal_ready` (ancre trouvée + segment non vide)
- `ready_for_proposal` -> `blocked_no_anchor` (aucune ancre reconnue)
- `ready_for_proposal` -> `blocked_empty_segment` (bornes invalides ou texte vide)
- `proposal_ready` -> `validated` (confirmation explicite utilisateur)
- `proposal_ready` -> `ready_for_proposal` (reprise/correction et nouvelle tentative)

## Traceability Notes

- Toute session doit permettre de reconstruire `rawText -> proposal(text + boundary + anchor) -> validatedSegment`.
