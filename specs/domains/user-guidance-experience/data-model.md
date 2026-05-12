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
