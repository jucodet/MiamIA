# Domain Plan - capture-recognition

## Objectives

1. Stabiliser les etats de scan et de capture.
2. Normaliser la sortie OCR brute vers un contrat ACL.

## Dependencies

- Downstream: `ingredient-normalization-validation` via ACL.
- Platform: `traceability-storage`.

## Acceptance

- Chaque session de scan expose un identifiant stable.
- Sortie OCR brute disponible avec metadonnees minimales.
