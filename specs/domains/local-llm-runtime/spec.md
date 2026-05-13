# Domain Spec - local-llm-runtime

> **Status**: Placeholder — à compléter via `/speckit-sync-backfill`
> **Created**: 2026-05-12 (sync-apply P15)
> **Source packages**: `app/src/main/java/com/miamia/gemma4local/` (15 fichiers, ~902 lignes)

## Purpose

Gérer le cycle de vie complet du modèle LLM local (Gemma) : disponibilité, chargement, exécution d'inférence, gestion des erreurs runtime, métriques et observabilité.

## Scope

- Disponibilité et chargement du modèle Gemma local (`Gemma4LocalAvailabilityChecker`, `HybridGemma4LocalGateway`)
- Exécution d'inférence locale (`Gemma4LocalClient`, requête → réponse streaming)
- Gestion des erreurs runtime (modèle absent, échec chargement, timeout) via `Gemma4LocalErrorMapper`
- Métriques et observabilité (latence, classe d'appareil via `DeviceClassResolver`)
- Import et téléchargement du modèle (`GemmaModelDownloader`, `GemmaModelImportManager`)
- Configuration (`Gemma4LocalConfig`)

## Invariants

- *(à définir lors du backfill complet)*

## Functional Requirements

- *(à extraire du code via `/speckit-sync-backfill`)*

## Cross-domain Notes

- `user-guidance-experience` orchestre l'onboarding de téléchargement du modèle
- `ingredient-health-intelligence` consomme le gateway pour l'analyse de composition
- `capture-recognition` n'a pas de dépendance directe sur ce domaine

## Source Mapping

- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalClient.kt`
- `app/src/main/java/com/miamia/gemma4local/HybridGemma4LocalGateway.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalAvailabilityChecker.kt`
- `app/src/main/java/com/miamia/gemma4local/GemmaModelDownloader.kt`
- `app/src/main/java/com/miamia/gemma4local/GemmaModelImportManager.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalErrorMapper.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalConfig.kt`
- `app/src/main/java/com/miamia/gemma4local/DeviceClassResolver.kt`
