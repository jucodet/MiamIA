# Contrat: Intégration navigation onboarding

## Contexte

L'écran d'onboarding de téléchargement du modèle s'insère comme point d'entrée conditionnel dans la navigation principale (`MainActivity`).

## Contrat d'intégration

### Pré-condition d'affichage

Le flux onboarding s'active **uniquement** quand :
- `GemmaModelDownloader.resolveLocalModel() == null`
- ET l'application vient de démarrer (pas de retour depuis un écran interne)

### Routes de navigation

| Route | Écran | Condition d'affichage |
|-------|-------|----------------------|
| `onboarding_offline` | `NetworkOfflineScreen` | Réseau indisponible + modèle absent |
| `onboarding_confirm` | `ModelDownloadOnboardingScreen` | Réseau OK + modèle absent |
| `onboarding_downloading` | `ModelDownloadWaitingScreen` | Téléchargement en cours |
| `camera_flow_capture` | `CameraScreen` (existant) | Modèle prêt |

### Transition terminale

Quand le téléchargement réussit (`LlmModelReadinessState.Ready`) :
- Naviguer vers `CameraFlowRoutes.Capture`
- Effacer le backstack onboarding (pas de retour possible vers les écrans onboarding)
- `popUpTo(onboarding_confirm) { inclusive = true }`

### ViewModel partagé

`ModelDownloadViewModel` est scopé à l'activité (pas au `NavBackStackEntry`) car il survit aux transitions entre les 3 écrans onboarding.

### Interface avec GemmaModelDownloader

```kotlin
suspend fun downloadModelWithProgress(
    onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
): File
```

Le ViewModel appelle cette méthode dans un `viewModelScope.launch(Dispatchers.IO)` et met à jour `_state: MutableStateFlow<LlmModelReadinessState>` sur chaque callback.

### Interface avec NetworkTypeDetector

```kotlin
fun detectCurrentNetworkType(context: Context): NetworkType
```

Appelé une seule fois à l'initialisation du ViewModel pour router vers `Offline` ou `ConfirmationRequired`.
