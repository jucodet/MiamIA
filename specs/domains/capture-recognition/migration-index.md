# Migration Index - capture-recognition

## Source -> Target

- `001-scan-ingredients/spec.md` -> `spec.md` (`Purpose`, `Scope`, `Functional Requirements`) [validated]
- `005-camera-start-temp-scan/spec.md` -> `spec.md` (`Scope`, `Functional Requirements`) [validated]
- `007-live-camera-preview-capture/spec.md` -> `spec.md` (`Functional Requirements`, `Invariants`) [validated]
- `008-capture-photo-texte-ocr/spec.md` -> `spec.md` (`Functional Requirements`) [validated]
- `012-home-layout-mediapipe-status/spec.md` -> `spec.md` (`Scope`: readiness status) [validated]
- `015-analyse-ocr-llm/spec.md` -> `spec.md` (`Cross-domain preconditions`) [validated]

## Conflict Decisions

- Owner decision: la redirection post-OCR est geree par `user-guidance-experience`; `capture-recognition` expose uniquement les preconditions OCR.
