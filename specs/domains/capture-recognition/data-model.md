# Data Model - capture-recognition

## Entities

- `ScanSession(sessionId, startedAt, status)`
- `PhotoCapture(captureId, sessionId, capturedAt)`

## Value Objects

- `RawOcrText(value, confidence, languageHint)`

## Aggregate

- `ScanSession` aggregate root.

## Presentation read models (UI, incrément 019)

- **OcrReviewLayoutState** (conceptuel) : regroupe la chaîne affichée (`RawOcrText` ou extrait segment / transcript brut) et la liste d’actions proposées ; la vue MUST respecter le contrat [`contracts/ui-raw-transcript-review-surface.md`](./contracts/ui-raw-transcript-review-surface.md) (zone scroll + pied fixe). Aucun nouveau type backend obligatoire : projection depuis `ScanState` existant.
