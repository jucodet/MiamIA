# Implementation Plan: Suppression du message d'accueil sur l'écran capture (Feature D)

**Branch**: `021-auto-analyze-ingredients-tag` (branche active ; voir note Plan vs Branche ci-dessous) | **Date**: 2026-05-13 | **Spec**: [spec.md — Feature D](./spec.md#feature-d--suppression-du-message-daccueil-sur-lécran-capture)
**Input**: Feature specification from `specs/domains/user-guidance-experience/spec.md` (Feature D)

> Note Plan vs Branche : la branche courante a été créée pour un autre incrément. L'évolution Feature D est strictement UI, indépendante des autres flux et peut être livrée sur cette branche ou via une branche dédiée selon la stratégie de release.

## Summary

Retirer la bannière de message d'accueil rendue actuellement par `CameraScreen.kt` au-dessus de l'aperçu caméra. Le rendu se fait via un `Text(welcomeState.text, testTag = "welcome_message_banner")` consommant `CameraViewModel.welcomeUiState`. La feature consiste à supprimer **uniquement la consommation UI** (et l'import associé) sans toucher au package `welcome/` (catalogue, sélecteur, policy), pour conserver un diff minimal et préserver la possibilité d'un retour rapide ou d'une réutilisation contextuelle ultérieure. Une tâche de nettoyage facultative est planifiée comme suivi hors livraison.

## Technical Context

**Language/Version**: Kotlin (Android), Jetpack Compose (Material 3) — versions de l'application existante.  
**Primary Dependencies**: Compose UI (`Column`, `Text`, `Modifier.testTag`), `StateFlow.collectAsState` (à retirer côté UI capture pour `welcomeUiState`).  
**Storage**: N/A (changement UI pur, aucune donnée persistée concernée).  
**Testing**: AndroidTest Compose UI (existant pour la capture) + tests unitaires existants pour le package `welcome/` (sélecteur, policy, catalogue) → à laisser inchangés.  
**Target Platform**: Android (téléphone, principalement portrait).  
**Project Type**: mobile-app (module Android `app`).  
**Performance Goals**: aucun objectif spécifique au-delà de « aucune régression » ; gain marginal (un `collectAsState` en moins côté écran capture).  
**Constraints**:
- aucune chaîne issue du catalogue `welcome/` ne MUST apparaître sur l'écran capture (UGE-D-FR-001) ;
- aucun nœud porteur du test tag `welcome_message_banner` ne MUST exister dans l'arbre Compose de l'écran capture (UGE-D-FR-002) ;
- conformité avec capture-recognition (CR-FR-009..011) préservée (gain d'espace ≥ 1 ligne `bodyLarge`, SC-D-002, bénéficie à la `PreviewRegion`).  
**Scale/Scope**: 1 écran (`CameraScreen.kt`), 1 bloc `if (welcomeState is WelcomeMessageUiState.Displayed) { ... }` à retirer (lignes 87-93 dans la version courante), 1 import et 1 collecte `welcomeState` côté UI. Aucun nouveau composable. Aucune entité.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe (constitution v0.2.0) | Évaluation | Justification |
|---|---|---|
| I. Qualité produit/code (non négociable) | ✅ PASS | Traçabilité spec (UGE-D-FR-001..005 + SC-D-001..004) → tests acceptation (assertion d'absence) → code (1 bloc supprimé). Aucune régression UX/perf attendue ; la suite de tests existante capture/llm-onboarding reste valide. |
| II. ATDD d'abord | ✅ PASS | Nouveaux scénarios Given/When/Then écrits avant code, tests AndroidTest **d'absence** (`assertCountEquals(0)` sur le test tag) à ajouter avant la suppression du rendu. |
| III. UX moderne et optimale | ✅ PASS | Décision produit explicite ; gain d'espace pour la `PreviewRegion`. |
| IV. Performance comme exigence produit | ✅ PASS | Léger gain (un flow consommé en moins). Aucun nouveau calcul. |
| V. Simplicité, lisibilité, évolutivité | ✅ PASS | Diff minimal ; pas de sur-architecture. Le code `welcome/` est conservé tel quel (suivi possible). |
| VI. Frontières DDD et autonomie des domaines | ⚠️ PASS (avec note) | L'évolution est interne à `user-guidance-experience` (responsable des messages d'accueil). Le code de rendu se trouve actuellement dans `CameraScreen.kt` (co-occupé avec `capture-recognition`) — cette co-habitation préexiste et n'est pas créée par cette livraison. Aucun contrat inter-domaines n'est rompu ; aucun signal cross-context n'est introduit. Voir « Suivi DDD » ci-dessous. |

Aucune violation ⇒ pas d'entrée dans Complexity Tracking.

### Suivi DDD (hors scope livraison)

- `CameraScreen.kt` mélange aujourd'hui des éléments de plusieurs bounded contexts (`capture-recognition` : aperçu + bande d'action ; `user-guidance-experience` : `MediaPipeStatusIndicator`, bannière welcome ; `local-llm-runtime` indirect via états `GemmaUnavailable`). Cette co-habitation est antérieure à la Feature D et reste hors scope. Un suivi est recommandé pour clarifier les frontières (extraction d'un `HomeShell` côté UGE, ou réduction de la responsabilité de `CameraScreen` à `capture-recognition`).

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md              # Ce fichier
├── research.md          # Phase 0 — addendum Feature D
├── data-model.md        # Phase 1 — addendum Feature D (aucune nouvelle entité)
├── quickstart.md        # Phase 1 — addendum Feature D
├── contracts/
│   └── capture-screen-no-welcome-banner.md   # Phase 1 — nouveau contrat UI
└── tasks.md             # Phase 2 (généré par /speckit.tasks — non créé ici)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/java/com/miamia/
│   │   ├── camera/
│   │   │   └── CameraScreen.kt           # cible principale (retrait du bloc bannière + import + collectAsState welcomeState)
│   │   └── welcome/                       # PRÉSERVÉ tel quel (catalogue, selector, policy)
│   └── androidTest/java/com/miamia/
│       ├── camera/
│       │   └── (nouveau) NoWelcomeBannerOnCaptureUiTest.kt
│       └── welcome/
│           ├── US1WelcomeAfterLoginFlowTest.kt    # à reconfigurer ou retirer (UGE-D-FR-004)
│           └── US2PositiveToneWelcomeTest.kt      # à reconfigurer ou retirer (UGE-D-FR-004)
```

**Structure Decision**:
- Cible unique = `app/src/main/java/com/miamia/camera/CameraScreen.kt` (retrait d'un `if`-block, un import, un `collectAsState`).
- Pas de modification du `CameraViewModel` (laisse `welcomeUiState` exposé, simplement non consommé côté UI capture). Suivi possible : nettoyage ultérieur.
- Nouveau test d'acceptation `NoWelcomeBannerOnCaptureUiTest.kt` qui assert `onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)` dans plusieurs `ScanState`.
- Tests existants `welcome/`  AndroidTest (US1WelcomeAfterLoginFlowTest, US2PositiveToneWelcomeTest) à reconfigurer en tests d'absence ou à retirer (UGE-D-FR-004).

## Complexity Tracking

> Aucune violation à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (none) | — | — |

## Phase 0 — Outline & Research

Voir `research.md` (addendum Feature D). Sujets résolus :
1. **Stratégie de retrait** : suppression du rendu UI uniquement, conservation du package `welcome/` comme legacy non-consommé.
2. **Sort du flow `welcomeUiState`** : exposition conservée côté ViewModel (zéro régression sur les tests unitaires existants) ; consommation supprimée côté `CameraScreen`.
3. **Sort des tests `welcome/` AndroidTest** : reconfiguration en assertions d'absence (cf. UGE-D-FR-004) vs retrait pur — décision = reconfiguration pour conserver la couverture « pas de message d'accueil au lancement ».
4. **Test d'acceptation principal** : nouveau test Compose UI dédié `NoWelcomeBannerOnCaptureUiTest` couvrant tous les `ScanState` représentatifs.
5. **Impact perf / espace** : gain estimé ≥ 24 dp (1 ligne `bodyLarge`) sur la hauteur de `PreviewRegion` (cohérent avec SC-D-002).

## Phase 1 — Design & Contracts

Voir :
- `data-model.md` — addendum Feature D : **aucun nouvel agrégat ni value object**. Note explicite : `WelcomeMessageUiState` reste défini dans le package `welcome/` mais n'est plus projeté sur l'UI capture.
- `contracts/capture-screen-no-welcome-banner.md` — contrat UI : interdiction de rendu de tout nœud lié à un message d'accueil sur l'écran capture, dans tous les `ScanState`. Test tags requis stables.
- `quickstart.md` — addendum Feature D : scénarios manuels et commandes Gradle pour les tests instrumentés.

Mise à jour du fichier d'agent Spec Kit (`.cursor/rules/specify-rules.mdc`) : référence du plan pointée vers le présent fichier.

## Post-Design Constitution Re-check

| Principe | Statut |
|---|---|
| I — Qualité | ✅ inchangé |
| II — ATDD | ✅ contrats + tests prévus avant code |
| III — UX | ✅ aligné |
| IV — Perf | ✅ inchangé (léger gain) |
| V — Simplicité | ✅ diff minimal |
| VI — DDD | ✅ pas de débordement nouveau ; suivi noté |

Aucune nouvelle violation.
