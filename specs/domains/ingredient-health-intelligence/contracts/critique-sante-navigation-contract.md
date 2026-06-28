# Contrat — navigation vers la critique santé (Feature M)

> **SUPERSÉDÉ par `critique-inline-restitution-contract.md` (Feature O, 2026-06-28)** : la critique santé est désormais rendue **inline sur `LlmResultScreen`** avec déclenchement automatique ; la route `HealthCritiqueEntry`, le bouton « Critique santé » et l'écran d'entrée `HealthCritiqueScreen` sont **supprimés**. Ce contrat est conservé pour traçabilité (constitution I) ; ne pas utiliser pour l'implémentation courante.

## Portée

- Câblage de navigation rendant `HealthCritiqueScreen` accessible en production.
- Ne modifie **pas** `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, le flux composition, ni la chaîne `analyze()` → `navigateToResult` → `HealthCritiqueResult` (`IHI-M-FR-006`/`007`).

## Route

| Élément | Valeur | Réf spec |
|---------|--------|----------|
| Constante | `CameraFlowRoutes.HealthCritiqueEntry` (ex. `"health_critique_entry"`) | `IHI-M-FR-001` |
| Cible | `HealthCritiqueScreen(healthCritiqueViewModel)` | `IHI-M-FR-004` |
| Enregistrement | `NavHost` de `MainActivity` | `IHI-M-FR-001` |

## Point d'entrée UI (`LlmResultScreen`)

| Élément | Description | Réf spec |
|---------|-------------|----------|
| Composant | Bouton « Critique santé » | `IHI-M-FR-002` |
| Visibilité | État terminal `Complete` ou `Error` du `streamingBilan` | `IHI-M-FR-002` |
| Activation | `lastValidatedSegmentForHealth` non null / non vide | `IHI-M-FR-003` |
| Callback | `onCritiqueSante: () -> Unit` → `cameraNavController.navigate(HealthCritiqueEntry)` | `IHI-M-FR-002` |
| Test tag | `llm_result_critique_sante` (recommandé) | — |

## Synchronisation du segment (réutilisée, non modifiée)

- `cameraViewModel.lastValidatedSegmentForHealth` → `healthCritiqueViewModel.setValidatedSegmentFromScan(segment)` (déjà câblé dans `MainActivity`).
- `HealthCritiqueScreen` affiche la liste en lecture seule synchronisée.

## Chaîne critique santé (inchangée)

```text
HealthCritiqueScreen (bouton « Analyser »)
  → viewModel.analyze()
  → navigateToResult (SharedFlow)
  → cameraNavController.navigate(HealthCritiqueResult)
  → HealthCritiqueResultScreen (sections parsées ENFANTS / FEMMES_ENCEINTES / ADULTES / PERSONNES_AGEES)
```

## Tests de conformité minimaux

- **Navigation** : depuis `LlmResultScreen` (bilan `Complete` + segment disponible), le bouton « Critique santé » est activé et navigue vers `HealthCritiqueScreen`.
- **Désactivation** : sans segment validé, le bouton est désactivé/masqué.
- **Bout-en-bout** : « Analyser » depuis `HealthCritiqueScreen` → `HealthCritiqueResultScreen` affiche les sections (comportement pré-existant, non modifié).
- **Non-régression** : flux composition, moteur, prompt, parseur inchangés.
