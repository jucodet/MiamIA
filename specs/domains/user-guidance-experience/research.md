# Research: llm-download-onboarding

## Decision 1: Mécanisme de callback de progression dans GemmaModelDownloader

- **Decision**: Enrichir `GemmaModelDownloader.downloadModel()` avec un paramètre `onProgress: ((Int, Long, Long) -> Unit)?` (percent, downloaded, total) appelé à chaque lecture de buffer.
- **Rationale**: Le downloader existant calcule déjà `percent` et `downloaded/totalBytes` en interne (lignes 50-66) ; exposer un callback est minimal et non-breaking.
- **Alternatives considered**:
  - SharedFlow interne au Downloader (rejeté : couplage inutile, le ViewModel contrôle déjà le scope).
  - WorkManager avec ProgressData (rejeté : over-engineering pour un téléchargement foreground avec UI active).

## Decision 2: Détection du type de réseau

- **Decision**: Utiliser `ConnectivityManager.getNetworkCapabilities()` pour distinguer Wi-Fi, mobile, et offline.
- **Rationale**: API standard Android (API 23+) ; pas de dépendance supplémentaire ; compatible API min 26 du projet.
- **Alternatives considered**:
  - `NetworkInfo.getType()` deprecated (rejeté : API obsolète depuis API 29).
  - Bibliothèque tierce ReactiveNetwork (rejeté : dépendance externe non justifiée).

## Decision 3: Navigation onboarding vs flux capture existant

- **Decision**: Intercaler l'écran onboarding comme `startDestination` conditionnelle dans le `NavHost` de `MainActivity` quand le modèle est absent. Après téléchargement réussi, naviguer vers `CameraFlowRoutes.Capture` en remplaçant le backstack.
- **Rationale**: Navigation Compose supporte nativement `startDestination` dynamique ; pas de refactoring du flux existant.
- **Alternatives considered**:
  - Activity séparée pour l'onboarding (rejeté : complexité de communication inter-activity inutile).
  - Dialog/BottomSheet overlay (rejeté : clarification spec exige un écran plein dédié).

## Decision 4: Reprise de téléchargement (FR-013 SHOULD)

- **Decision**: V1 sans reprise HTTP Range ; en cas d'interruption, le fichier `.downloading` est supprimé et le téléchargement recommence. La reprise sera ajoutée en V2 via header `Range`.
- **Rationale**: Le downloader existant supprime déjà le fichier temp en `finally`. Ajouter Range nécessite de persister l'offset + vérifier que le serveur HuggingFace supporte `Accept-Ranges` — complexité reportée.
- **Alternatives considered**:
  - Implémentation Range immédiate (rejeté : effort disproportionné pour une feature SHOULD de V1).
  - DownloadManager système (rejeté : perte de contrôle sur la progression UI et l'emplacement fichier).

## Decision 5: Extraction composants partagés (fouet + phrases)

- **Decision**: Extraire `AnimatedWhisk` et `WAITING_PHRASES` depuis `LlmResultScreen.kt` vers `com.foodgpt.ui.shared` pour réutilisation par l'écran onboarding.
- **Rationale**: Évite la duplication de code entre deux écrans qui utilisent exactement les mêmes composants visuels.
- **Alternatives considered**:
  - Copier/dupliquer les composants dans le package onboarding (rejeté : violation DRY explicite et maintenance double).
  - Les laisser dans `result` et importer cross-package (rejeté : couplage direction inhabituelle).
