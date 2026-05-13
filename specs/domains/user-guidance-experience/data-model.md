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

---

## Addendum Feature E — Catalogue de phrases d'attente (2026-05-13)

### Entité logique : `WaitingPhraseCatalog` (value object implicite)

| Attribut | Description |
|----------|-------------|
| `phrases` | Liste immuable de chaînes UTF-8, français ; actuellement matérialisée par `WAITING_PHRASES` dans le code. |
| Taille post-livraison | Exactement **21** entrées (11 baseline + 10 ajouts Feature E). |
| Unicité | Aucun doublon exact (comparaison après trim espaces début/fin). |

### Règles de validation (métier)

- Chaque entrée MUST respecter UGE-E-FR-002 et UGE-E-FR-005.
- Les dix ajouts MUST être distincts des onze formulations baseline (UGE-E-FR-003).

### Relations

- Consommée par les écrans d'attente UX (`LlmResultScreen`, `ModelDownloadWaitingScreen`) sans couplage inverse : les écrans ne mutent pas le catalogue.

### Transitions d'état

- Aucune machine d'état nouvelle ; seule la cardinalité du catalogue change.

---

## Addendum Feature F — Statuts textuels écran capture (2026-05-13)

### Read model : `CaptureStatusLine` (conceptuel)

| Attribut | Description |
|----------|-------------|
| `scanStateLabel` | Texte dérivé de `ScanState` affiché sous la bande d’action (hors `PreviewRegion`). Pour `PreviewActive`, MUST être une phrase explicite « prêt à scanner » (UGE-F-FR-001) et MUST NOT être la chaîne exacte « Aperçu caméra actif » (UGE-F-FR-003). |
| `mediaPipeStatusLabel` | Texte court pour l’indicateur MediaPipe en haut d’écran ; MUST NOT se réduire au seul mot « Disponible » comme unique information (UGE-F-FR-001). |

### Invariants ajoutés

- **INV-F-1** : Aucun composable interactif « Test LLM » (tag, sémantique ou libellé équivalent) ne MUST apparaître sur l’écran capture.
- **INV-F-2** : Le tag de test `camera_tab_llm_test_button` ne MUST plus être émis dans l’arbre Compose de production.

### Relations

- Dépend de `ScanState` (défini côté `camera/` / capture) pour le mapping texte ; pas de nouvelle entité persistante.
