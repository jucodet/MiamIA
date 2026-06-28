# Contrat — restitution inline de la critique santé (Feature O)

> **Supersede** : `critique-sante-navigation-contract.md` (Feature M). Le câblage par bouton + écrans séparés est retiré ; ce contrat décrit la restitution **100 % inline** sur l'écran principal des résultats et le **déclenchement automatique**.

## Portée

- Rendre la critique santé par profil (Feature N) directement sur `LlmResultScreen` (écran principal des résultats), en continuité sous le bilan composition / pastille kcal / KPI additifs juxtaposés.
- Déclenchement **automatique** (aucune action utilisateur, aucune navigation).
- Ne modifie **pas** `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser`, ni le flux composition.

## Éléments supprimés (supersede Feature M)

| Élément | Statut | Réf |
|---------|--------|-----|
| `CameraFlowRoutes.HealthCritiqueEntry` (constante + route) | **Supprimé** | `IHI-O-FR-004` (supersede `IHI-M-FR-001`) |
| `CameraFlowRoutes.HealthCritiqueResult` (route) | **Supprimé** | `IHI-O-FR-005` |
| `HealthCritiqueScreen.kt` (écran d'entrée) | **Supprimé** | `IHI-O-FR-004` (supersede `IHI-M-FR-004`) |
| `HealthCritiqueResultScreen.kt` (écran de restitution séparé) | **Supprimé** | `IHI-O-FR-005` |
| Bouton « Critique santé » (`onCritiqueSante`, test tag `llm_result_critique_sante`) | **Supprimé** | `IHI-O-FR-003` (supersede `IHI-M-FR-002`) |
| `composable(HealthCritiqueEntry)` / `composable(HealthCritiqueResult)` dans `NavHost` | **Supprimés** | `IHI-O-FR-004` / `IHI-O-FR-005` |

## Déclenchement automatique (`LlmResultScreen`)

| Élément | Description | Réf spec |
|---------|-------------|----------|
| Condition | `streamingBilan == StreamingBilanState.Complete` **et** `lastValidatedSegmentForHealth` non vide | `IHI-O-FR-001` |
| Mécanisme | `LaunchedEffect(streamingBilan, validatedSegment)` → `healthCritiqueViewModel.analyze()` | `IHI-O-FR-001` |
| Idempotence | gardé par l'état du `HealthCritiqueViewModel` (un seul `analyze()` par `Complete`) | `IHI-O-FR-013` |
| Non-déclenchement | bilan `Error` ou segment vide au `Complete` → section critique masquée / état neutre | `IHI-O-FR-010` |
| Annulation | au retour (`onBack` / `popBackStack`), annulation propre du job d'inférence (pas de fuite) | `IHI-O-FR-014` |

## Restitution inline (section critique de `LlmResultScreen`)

| Élément | Description | Réf spec |
|---------|-------------|----------|
| Placement | en continuité sous le bilan composition / pastille kcal / KPI additifs juxtaposés | `IHI-O-FR-002` / `IHI-O-FR-012` |
| Composables | extraits de `HealthCritiqueResultScreen` : rappel « Évalué pour vous », avertissements, `PrudenceGauge`, `IngredientRiskCardItem` (filtrés Modéré/Élevé), `FullIngredientListToggle`, disclaimers | `IHI-O-FR-005` (cohérent Feature N `IHI-N-FR-006`..`011`) |
| État `en cours` | loading + `streamingText` rendus inline (inférence synchrone `sendMessage` ; `onStreamPartial` rappelé une fois en fin d'inférence) | `IHI-O-FR-006` |
| État `erreur` | `InferenceError` / `InputInvalid` rendus inline (message), sans casser le bilan composition au-dessus | `IHI-O-FR-006` |
| État `prête` | `CritiqueReady` rendu inline (rappel + jauge + cartes + liste complète) | `IHI-O-FR-002` |
| Profil | consommation `UserProfileProvider` (Feature N/I) + rappel + fallback « Adulte » + signal « profil par défaut » | `IHI-O-FR-008` |
| ~~Actions copier~~ | **Retirées** : « Copier la réponse » + « Copier le prompt » supprimées de la section critique inline | ~~`IHI-O-FR-009`~~ (supersédé) |
| Persistance | `LastHealthAnalysisStore` conservé (rotation/process death), sans écran séparé | `IHI-O-FR-011` |

## Chaîne critique santé (cible Feature O)

```text
LlmResultScreen (bilan Complete + segment validé)
  → LaunchedEffect → healthCritiqueViewModel.analyze()
  → états ui/streamingText collectés inline (en cours → prête / erreur)
  → restitution inline (rappel + jauge + cartes + liste complète)
  (aucune navigation, aucune route séparée)
```

## Tests de conformité minimaux

- **Auto-trigger** : `LlmResultScreen` avec `streamingBilan = Complete` + segment non vide → `analyze()` déclenché automatiquement ; section critique visible inline sans action/navigation.
- **Non-déclenchement** : `streamingBilan = Error` ou segment vide → critique non déclenchée ; section critique absente / neutre.
- **États inline** : `isLoading` + `streamingText` rendus ; `InferenceError`/`InputInvalid` rendus inline sans casser le bilan ; `CritiqueReady` rendu (rappel + jauge + cartes + liste).
- **Suppression navigation** : aucune route `HealthCritiqueEntry` / `HealthCritiqueResult` dans le `NavHost` ; aucun bouton `llm_result_critique_sante` ; `HealthCritiqueScreen` / `HealthCritiqueResultScreen` absents.
- **Idempotence** : un `Complete` → au plus une inférence critique.
- **Retour** : `onBack` ramène au scan ; le job d'inférence est annulé proprement.
- **Non-régression** : flux composition, `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser` inchangés ; KPI additifs juxtaposés toujours rendus (`IHI-C-FR-007`).
