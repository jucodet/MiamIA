# Sync Apply Report

**Applied** : 2026-05-13 (mode interactif : lot `proposals.json` historique **non appliqué** ; correctifs drift Feature G **appliqués** sur les specs domaine)

## Décision interactive

- **`proposals.json` (P1–P16)** : statut `approved` présent mais cibles **obsolètes** (chemins `photo-capture-llm-result-flow`, FRs pré-consolidation). **Aucune** modification de spec n'a été faite à partir de ce fichier pour éviter la corruption documentaire.
- **Correctifs approuvés implicitement** (alignement dernier `drift-report` + Feature G) : **appliqués** ci-dessous.

## Changes Made

### Specs / contrats mis à jour

| Fichier | Type | Résumé |
|---------|------|--------|
| `specs/domains/capture-recognition/spec.md` | Modified | Scope aval sans chip UI ; note CR-FR-006/007 vs parcours nominal UGE-G ; hypothèses incrément 019 ; `Last Modified` |
| `specs/domains/ingredient-normalization-validation/contracts/session-capture-intent-for-implicit-validation.md` | Rewritten | Suppression obligation `SegmentConfirmationRequired` ; orchestration implicite Feature G |
| `specs/domains/ingredient-normalization-validation/spec.md` | Modified | FR-010, US2b, SC-005, clarifications session FR-010, hypothèses ; statut doc |
| `specs/domains/ingredient-normalization-validation/tasks.md` | Modified | Note historique T014–T020 (déjà présente si doublon évité) |

### Sauvegarde

| Fichier |
|---------|
| `.specify/sync/backups/2026-05-13/capture-recognition_spec.md` (snapshot pré-edit partiel) |

### Tâches d'implémentation générées

- `.specify/sync/align-tasks.md` — nettoyage `CameraUiState` ; tests instrumentés chip / confirmation

### Non appliqué

| Source | Raison |
|--------|--------|
| `proposals.json` P1–P16 | Cibles et identifiants FR non alignés sur la consolidation domaine actuelle |

## Next Steps

1. Relire les trois artefacts modifiés dans une PR « sync doc ».
2. Exécuter les tâches de `align-tasks.md` lorsque le wrapper Gradle est disponible.
3. Régénérer `proposals.json` via **`speckit.sync.propose`** si un nouveau cycle formel est souhaité.
