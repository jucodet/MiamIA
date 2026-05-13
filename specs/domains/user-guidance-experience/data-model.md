# Data Model: llm-download-onboarding

## Entités

### LlmModelReadinessState (sealed class)

État du flux onboarding observé par le ViewModel.

| État | Description |
|------|-------------|
| `Checking` | Vérification en cours de la présence du modèle local |
| `Offline` | Pas de connexion réseau ; modèle absent |
| `ConfirmationRequired(networkType)` | Modèle absent, réseau disponible ; attente confirmation |
| `Downloading(progress)` | Téléchargement en cours |
| `Ready` | Modèle disponible ; navigation vers capture autorisée |
| `Error(message, canRetry)` | Échec (réseau, espace disque, etc.) |
| `Declined` | Utilisatrice a refusé le téléchargement |

### DownloadProgress (data class)

| Champ | Type | Description |
|-------|------|-------------|
| `percent` | `Int` | 0–100, pourcentage d'avancement |
| `downloadedBytes` | `Long` | Octets déjà téléchargés |
| `totalBytes` | `Long` | Taille totale (-1 si inconnue) |

### NetworkType (enum)

| Valeur | Description |
|--------|-------------|
| `WIFI` | Connexion Wi-Fi détectée |
| `MOBILE_DATA` | Connexion données mobiles (4G/5G) |
| `OFFLINE` | Aucune connexion réseau disponible |

## Transitions d'état

```text
[App Launch]
    │
    ▼
 Checking
    │
    ├── modèle présent ──────────► Ready ──► (navigation capture)
    │
    ├── offline ─────────────────► Offline
    │                                │
    │                                └── réseau revient ──► ConfirmationRequired
    │
    └── réseau disponible ───────► ConfirmationRequired(networkType)
                                     │
                                     ├── "Confirmer" ──► Downloading(progress)
                                     │                      │
                                     │                      ├── succès ──► Ready
                                     │                      └── échec ──► Error
                                     │                                      │
                                     │                                      └── "Réessayer" ──► Downloading
                                     │
                                     └── "Plus tard" ──► Declined
```

## Invariants

- Un seul téléchargement actif à la fois (pas de concurrence).
- `Ready` ne peut être atteint que si le fichier modèle existe et a une taille > 0.
- La transition `Checking → Ready` est immédiate si le modèle est déjà sur disque.
- L'état `Declined` est terminal pour la session ; relancer l'app repropose la confirmation.

---

## Addendum Feature D — Suppression du message d'accueil sur l'écran capture (2026-05-13)

### Entités / Value Objects

- **Aucune nouvelle entité** ni value object introduit par Feature D.
- `WelcomeMessageUiState` (sealed class `Hidden | Displayed(text)`) reste défini dans le package `welcome/` (legacy conservé). Il n'est plus consommé par l'UI de l'écran capture mais peut continuer d'être collecté côté `CameraViewModel.welcomeUiState` sans projection visuelle.

### Transitions d'état

- Aucune transition modifiée. `WelcomeMessageUiState.Displayed` peut toujours être émis par la policy ; il ne produit simplement plus d'effet visuel.

### Invariants ajoutés

- **INV-D-1** : Aucun composable rendu sur l'écran capture (= écran d'accueil) ne MUST référencer ou consommer `WelcomeMessageUiState.Displayed` pour produire un rendu textuel à l'utilisateur.

### Read models présentation (UI, incrément Feature D)

- **CaptureScreenChrome** (conceptuel) : décrit la chrome non-camera de l'écran capture comme un ensemble réduit à `{ MediaPipeStatusIndicator, CaptureActionBar, status text }`. La bannière welcome n'en fait **plus** partie. Référence : [`contracts/capture-screen-no-welcome-banner.md`](./contracts/capture-screen-no-welcome-banner.md).
