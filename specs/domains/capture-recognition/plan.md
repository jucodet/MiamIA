# Implementation Plan: Bouton capture sous l’aperçu et libellé « Y a quoi là-dedans ? »

**Branch**: `021-auto-analyze-ingredients-tag` (branche active ; voir note Plan vs Branche ci-dessous) | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md#feature-increment--bouton-capture-sous-laperçu-et-libellé--y-a-quoi-là-dedans--)
**Input**: Feature specification from `specs/domains/capture-recognition/spec.md`

> Note Plan vs Branche : la branche courante a été créée pour une autre évolution. Cet incrément reste cohérent (même domaine, mêmes invariants) et peut être livré sur cette branche ou via une branche dédiée selon la stratégie de release. Aucun impact sur la frontière de domaine.

## Summary

Recadrer l’écran capture pour que l’aperçu vidéo reste intégralement visible et que les boutons d’action (capture principale + actions secondaires existantes) soient présentés sous l’aperçu dans une bande d’action dédiée, avec une séparation visuelle non ambiguë. Renommer l’action principale de capture en « Y a quoi là-dedans ? » (1-pour-1 avec l’ancien « Prendre la photo »). Aucun changement de parcours, d’API ni de logique métier ; uniquement mise en page Compose + libellé.

## Technical Context

**Language/Version**: Kotlin (Android), Jetpack Compose (Material 3) — versions de l’application existante.
**Primary Dependencies**: Compose UI (`Column`, `Box`, `Modifier.height`, `Arrangement.spacedBy`), Material 3 (`Button`, `OutlinedButton`, `Text`), CameraX (`CameraPreviewBox`), test tags Compose (existants).
**Storage**: N/A (changement UI pur, aucun nouveau persistant).
**Testing**: AndroidTest Compose UI (déjà utilisé : `app/src/androidTest/java/com/miamia/camera/*`). Pas de nouveau framework de test introduit.
**Target Platform**: Android (téléphone, orientation principale portrait ; paysage couvert par cas limites).
**Project Type**: mobile-app (module Android `app`).
**Performance Goals**: aucune régression sur l’ouverture de l’écran (≤ délai actuel observable à l’œil) ; pas de surcharge de recomposition (la mise en page reste un `Column` linéaire au lieu d’un `Box` empilé).
**Constraints**: 
- aucun bouton ne MUST recouvrir, même partiellement, la zone vidéo (CR-FR-009/010) ;
- libellé exact « Y a quoi là-dedans ? » (CR-FR-011) ;
- comportement de capture inchangé (CR-FR-001) ;
- conformité avec CR-FR-007 (bande d’action visible sans scroll global).
**Scale/Scope**: 1 écran (CameraScreen.kt), 2 emplacements de bouton « Prendre la photo » (états `CameraUnavailable` + états live preview), 0 nouveau composable structurel obligatoire (extraction d’un sous-composable `CaptureActionBar` recommandée mais pas imposée).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe (constitution v0.2.0) | Évaluation | Justification |
|---|---|---|
| I. Qualité produit/code (non négociable) | ✅ PASS | Traçabilité spec (CR-FR-009..011 + SC-CR-003..005) → tests acceptation → code. Aucune régression UX/perf attendue. |
| II. ATDD d’abord | ✅ PASS | Nouveaux scénarios Given/When/Then prévus (US1 non-recouvrement, US2 libellé) avec tests AndroidTest avant code. |
| III. UX moderne et optimale | ✅ PASS | C’est le motif central : restaurer la visibilité du flux vidéo + libellé conversationnel. |
| IV. Performance comme exigence produit | ✅ PASS | Aucun ajout de calcul ; simplification potentielle (suppression d’un éventuel empilement). Pas de cible perf spécifique au-delà du « aucune régression ». |
| V. Simplicité, lisibilité, évolutivité | ✅ PASS | Patch UI minimal ; option d’extraire `CaptureActionBar` pour clarifier — pas de sur-architecture. |
| VI. Frontières DDD et autonomie des domaines | ✅ PASS | Concerne uniquement `capture-recognition` (UI de capture). Pas de fuite vers `ingredient-normalization-validation`, `user-guidance-experience`, ou autre domaine. Aucune ACL touchée. |

Aucune violation ⇒ pas d’entrée dans Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/domains/capture-recognition/
├── plan.md              # Ce fichier
├── research.md          # Phase 0 — décisions de mise en page + renommage
├── data-model.md        # Phase 1 — aucun nouveau modèle métier (N/A justifié)
├── quickstart.md        # Phase 1 — déjà existant ; ajout vérifications manuelles
├── contracts/
│   └── capture-action-bar.md   # Phase 1 — contrat UI capture-screen (placement + libellé)
└── tasks.md             # Phase 2 (généré par /speckit.tasks — non créé ici)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/java/com/miamia/camera/
│   │   ├── CameraScreen.kt                 # cible principale (placements + libellé)
│   │   └── (sous-composable optionnel) CaptureActionBar.kt
│   └── androidTest/java/com/miamia/camera/
│       ├── CameraCaptureLayoutUiTest.kt    # à étendre : non-recouvrement aperçu
│       ├── CameraUnavailableLlmButtonUiTest.kt  # libellé bouton (état Unavailable)
│       └── (nouveau) CaptureActionLabelUiTest.kt   # libellé bouton (états live)
```

**Structure Decision**: 
- Cible unique = `app/src/main/java/com/miamia/camera/CameraScreen.kt` (deux occurrences à recadrer + renommer).
- Extraction d’un composable privé `CaptureActionBar` recommandée (lisibilité + réutilisation entre l’état `CameraUnavailable` et les états live preview), mais non bloquante.
- Tests d’acceptation Compose UI ajoutés/étendus sous `app/src/androidTest/java/com/miamia/camera/`.

## Complexity Tracking

> Aucune violation à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (none) | — | — |

## Phase 0 — Outline & Research

Voir `research.md`. Sujets résolus :
1. Décision de mise en page : `Column` linéaire `Preview Box → CaptureActionBar → status text` (vs empilement `Box`).
2. Espacement explicite (≥ `HomeSpacingRules.standardFixedSpacing` × 1) entre l’aperçu et la bande d’action, pour garantir une séparation visuelle non ambiguë.
3. Libellé exact « Y a quoi là-dedans ? » utilisé tel quel (pas de string resource introduite par cette évolution pour limiter le diff ; un `R.string.capture_action_primary` peut être créé en suivi).
4. Test tags conservés (`capture_photo_button`, `photo_preview_box`) pour ne pas casser les tests existants.

## Phase 1 — Design & Contracts

Voir :
- `data-model.md` — aucun nouveau modèle métier (incrément purement UI).
- `contracts/capture-action-bar.md` — contrat UI : placement (sous l’aperçu, hors zone vidéo), libellé exact, comportement de clic inchangé, test tags requis.
- `quickstart.md` — vérifications manuelles + commandes de tests Compose UI à exécuter.

Mise à jour du fichier d’agent Spec Kit (`.cursor/rules/specify-rules.mdc`) : référence du plan pointée vers le présent fichier.

## Post-Design Constitution Re-check

| Principe | Statut |
|---|---|
| I — Qualité | ✅ inchangé |
| II — ATDD | ✅ contrats + tests prévus avant code |
| III — UX | ✅ aligné |
| IV — Perf | ✅ inchangé |
| V — Simplicité | ✅ patch minimal |
| VI — DDD | ✅ pas de débordement de domaine |

Aucune nouvelle violation.
