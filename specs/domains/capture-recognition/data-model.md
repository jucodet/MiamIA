# Data Model - capture-recognition

## Entities

- `ScanSession(sessionId, startedAt, status)`
- `PhotoCapture(captureId, sessionId, capturedAt)`

## Value Objects

- `RawOcrText(value, confidence, languageHint)`

## Aggregate

- `ScanSession` aggregate root.
