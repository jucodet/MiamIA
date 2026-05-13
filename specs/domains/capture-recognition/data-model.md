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

## Presentation read models (UI, incrément 020 — capture action bar)

- **CaptureScreenLayoutContract** (conceptuel) : décrit la mise en page de l’écran capture en deux régions disjointes — `PreviewRegion` (aperçu vidéo réel ou placeholder d’indisponibilité) et `CaptureActionBarRegion` (bande d’action sous l’aperçu). La vue MUST respecter le contrat [`contracts/capture-action-bar.md`](./contracts/capture-action-bar.md) (non-recouvrement + libellé exact). Aucun nouveau type backend obligatoire : aucune nouvelle donnée n’est nécessaire — uniquement la position et le libellé du composant.

> Note (incrément 020) : aucun nouvel agrégat ni value object introduit. L’évolution est strictement UI/Compose ; les invariants du domaine (`RawOcrText`, `ScanSession`, `PhotoCapture`) sont préservés à l’identique.
