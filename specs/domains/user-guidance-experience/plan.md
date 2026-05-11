# Implementation Plan: llm-download-onboarding

**Branch**: `018-llm-download-onboarding` | **Date**: 2026-05-11 | **Spec**: [`spec-llm-download-onboarding.md`](./spec-llm-download-onboarding.md)
**Input**: Spécification domaine `user-guidance-experience` — onboarding téléchargement modèle LLM première utilisation.

**Note**: Phases 0–1 documentées ci-dessous ; `tasks.md` est produit par `/speckit-tasks` (Phase 2 livrable).

## Summary

Implémenter un flux d'onboarding au premier lancement détectant l'absence du modèle Gemma local, présentant un écran plein de confirmation (avec détection Wi-Fi/4G), puis un écran d'attente avec barre de progression, phrases humoristiques rotatives (réutilisation des composants streaming existants), et fouet mixeur animé. Le `GemmaModelDownloader` existant est enrichi d'un callback de progression. La navigation s'intègre en amont du flux capture existant via un état `LlmModelReadiness` observable.

## Technical Context

**Language/Version**: Kotlin (JVM cible Android), Gradle Kotlin DSL
**Primary Dependencies**: Jetpack Compose, ViewModel, StateFlow, ConnectivityManager, coroutines
**Storage**: SharedPreferences (flag confirmation), fichier local `filesDir/gemma/` (modèle)
**Testing**: JUnit 4 (tests unitaires JVM `app/src/test/`), AndroidJUnit4 (tests instrumentés `app/src/androidTest/`) ; pattern ATDD via `*AcceptanceTest.kt`
**Target Platform**: Android (API min 26, compile/target 34)
**Project Type**: Application mobile monolithique (`app/`)
**Performance Goals**: Téléchargement non bloquant ; progression mise à jour toutes les ~500ms ; transition vers écran capture < 3s après fin téléchargement
**Constraints**: Pas de téléchargements concurrents ; réutilisation des composants UI existants (fouet, phrases) ; écran plein pour onboarding (pas de modal)
**Scale/Scope**: 3 écrans (offline, confirmation, attente) + enrichissement `GemmaModelDownloader` + ViewModel dédié + intégration navigation `MainActivity`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| **I. Qualité / traçabilité** | Spec clarifiée → scénarios acceptance → code ; incrément testable par US. |
| **II. ATDD** | Scénarios Given/When/Then définis pour US1, US2, US3 ; tests d'acceptation avant code. |
| **III. UX** | Écran plein informatif, feedback progression, phrases engageantes, animation ; états erreur/offline soignés. |
| **IV. Performance** | Barre progression temps réel ; transition < 3s ; pas de téléchargement bloquant l'UI thread. |
| **V. Simplicité** | Réutilisation `AnimatedWhisk` + `WAITING_PHRASES` existants ; enrichissement minimal du downloader ; pas de nouveau module. |
| **VI. DDD** | Domaine `user-guidance-experience` ; consomme `GemmaModelDownloader` du contexte `local-llm-runtime` via interface ; pas de fuite de frontière. |

**Post-design (Phase 1)** : aucune violation ; frontières préservées.

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md                             # This file
├── spec-llm-download-onboarding.md     # Spec
├── research.md                         # Phase 0
├── data-model.md                       # Phase 1
├── quickstart.md                       # Phase 1
├── contracts/                          # Phase 1
└── tasks.md                            # /speckit-tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/foodgpt/onboarding/
├── ModelDownloadOnboardingScreen.kt    # Écran plein confirmation (Compose)
├── ModelDownloadWaitingScreen.kt       # Écran attente + progression (Compose)
├── NetworkOfflineScreen.kt             # Écran "Connexion requise" (Compose)
├── ModelDownloadViewModel.kt           # ViewModel : readiness, progression, navigation
├── LlmModelReadinessState.kt          # Sealed class états onboarding
└── NetworkTypeDetector.kt              # Détection Wi-Fi/mobile/offline

app/src/main/java/com/foodgpt/gemma4local/
└── GemmaModelDownloader.kt             # Enrichi : callback onProgress(percent, bytes)

app/src/main/java/com/foodgpt/result/
└── LlmResultScreen.kt                 # Existant (WAITING_PHRASES, AnimatedWhisk extraits)

app/src/main/java/com/foodgpt/ui/shared/
├── WaitingPhrases.kt                   # Phrases humoristiques partagées (extrait)
└── AnimatedWhisk.kt                    # Composable fouet animé partagé (extrait)

app/src/test/java/com/foodgpt/onboarding/
├── ModelDownloadViewModelTest.kt
└── NetworkTypeDetectorTest.kt

app/src/androidTest/java/com/foodgpt/onboarding/
└── ModelDownloadOnboardingAcceptanceTest.kt
```

**Structure Decision**: Nouveau package `com.foodgpt.onboarding` dans le module `app` ; extraction des composants partagés (fouet, phrases) dans `com.foodgpt.ui.shared` pour réutilisation entre streaming et onboarding.

## Phase 0 — Recherche

**Statut**: terminé — voir [`research.md`](./research.md).

## Phase 1 — Design & contrats

**Statut**: terminé.

- [`data-model.md`](./data-model.md) — entités `LlmModelReadinessState`, `DownloadProgress`, `NetworkType`.
- [`contracts/onboarding-navigation-contract.md`](./contracts/onboarding-navigation-contract.md) — contrat d'intégration avec `MainActivity` et flux navigation.
- [`quickstart.md`](./quickstart.md) — validation manuelle (premier lancement, offline, progression, erreur).

**Agent context**: `.cursor/rules/specify-rules.mdc` mis à jour vers ce plan.

## Phase 2 — Tâches d'implémentation

Hors fichier : exécuter **`/speckit-tasks`** pour générer ou mettre à jour `tasks.md`.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier pour ce plan.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
