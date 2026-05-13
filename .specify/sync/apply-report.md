# Sync Apply Report

Applied: 2026-05-12T17:52:00+02:00
Based on: proposals.json from 2026-05-12T17:29:00+02:00

## Changes Made

### Specs Updated

| Spec | Requirement(s) | Change Type | Proposal |
|------|----------------|-------------|----------|
| `user-guidance-experience/spec.md` | FR-006 | Modified (backfill streaming) | P1 |
| `user-guidance-experience/spec.md` | FR-007 | Modified (backfill streaming) | P1 |
| `user-guidance-experience/spec.md` | FR-013 | Modified (backfill streaming) | P1 |
| `user-guidance-experience/spec.md` | FR-014 | Modified (backfill écran résultat) | P1 |
| `user-guidance-experience/spec.md` | FR-017 | Modified (backfill état d'attente) | P2a |
| `user-guidance-experience/spec-llm-download-onboarding.md` | FR-002 | Modified (backfill "Refuser et quitter") | P3 |
| `user-guidance-experience/spec-llm-download-onboarding.md` | FR-008 | Modified (backfill AnimatedMarmite) | P4 |
| `user-guidance-experience/spec-llm-download-onboarding.md` | FR-011 | Modified (backfill finishAffinity) | P3 |
| `ingredient-health-intelligence/spec.md` | FR-008 | Modified (backfill 180s timeout) | P7 |
| `ingredient-health-intelligence/spec.md` | SC-002 | Modified (30s → 180s) | P7 |
| `ingredient-health-intelligence/spec.md` | SC-006 | Modified (30s → 180s) | P7 |
| `ingredient-normalization-validation/spec.md` | FR-009 | Modified (backfill traçabilité mémoire) | P12 |
| `capture-recognition/spec.md` | CR-FR-005 | Annotated (aligné, note architecturale) | P13 |

### New Specs Created

| Spec | Domain | Source | Proposal |
|------|--------|--------|----------|
| `ingredient-health-intelligence/spec-composition-health-critique.md` | ingredient-health-intelligence | healthcritique/ + composition/ | P14 |
| `local-llm-runtime/spec.md` | local-llm-runtime (nouveau domaine) | gemma4local/ | P15 |
| `user-guidance-experience/spec-welcome-home.md` | user-guidance-experience | welcome/ + home/ | P16 |

### Implementation Tasks Generated

7 tâches dans `.specify/sync/align-tasks.md` :

| Task | Proposal | Effort | Phase |
|------|----------|--------|-------|
| Restaurer diacritiques MOCK_INGREDIENTS_INPUT | P6 | small | 2 |
| Assertion non-altération trim | P8a | small | 2 |
| Enrichir HomeLlmMockOutcome avec inputUsed | P8b | small | 2 |
| Ajouter @Tag("manual") au test mock | P8c | small | 2 |
| Supprimer enum mortes LINE_END / NO_NEWLINE_TO_EOF | P9 | small | 2 |
| Ancre absente → ScanState.Empty | P11 | small | 2 |
| Connecter fallback OCR à LlmResultScreen | P2b | medium | 3 |
| Implémenter reprise HTTP Range | P5 | medium | 3 |
| Refactorer regex ancre + supprimer duplication | P10 | medium | 3 |

### Not Applied

| Proposal | Reason |
|----------|--------|
| — | Toutes les 16 propositions ont été approuvées et appliquées |

### Backups

Backups des specs originales dans :
`.specify/sync/backups/2026-05-12/`

| Fichier backup | Spec originale |
|----------------|----------------|
| `user-guidance-experience_spec.md` | `specs/domains/user-guidance-experience/spec.md` |
| `user-guidance-experience_spec-llm-download-onboarding.md` | `specs/domains/user-guidance-experience/spec-llm-download-onboarding.md` |
| `ingredient-health-intelligence_spec.md` | `specs/domains/ingredient-health-intelligence/spec.md` |
| `ingredient-normalization-validation_spec.md` | `specs/domains/ingredient-normalization-validation/spec.md` |
| `capture-recognition_spec.md` | `specs/domains/capture-recognition/spec.md` |

## Next Steps

1. **Relire les specs modifiées** pour valider la formulation des backfills
2. **Commit** : `git add specs/ .specify/ && git commit -m "sync: apply drift resolutions (7 backfills, 3 new specs, 9 align tasks)"`
3. **Phase 2 — Corrections simples** : exécuter les 6 tâches d'alignement `small` dans `align-tasks.md`
4. **Phase 3 — Modifications modérées** : exécuter les 3 tâches d'alignement `medium`
5. **Phase 4 — Backfill specs** : compléter les 3 placeholders via `/speckit-sync-backfill`
