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

- **Decision**: Extraire `AnimatedWhisk` et `WAITING_PHRASES` depuis `LlmResultScreen.kt` vers `com.miamia.ui.shared` pour réutilisation par l'écran onboarding.
- **Rationale**: Évite la duplication de code entre deux écrans qui utilisent exactement les mêmes composants visuels.
- **Alternatives considered**:
  - Copier/dupliquer les composants dans le package onboarding (rejeté : violation DRY explicite et maintenance double).
  - Les laisser dans `result` et importer cross-package (rejeté : couplage direction inhabituelle).

---

## Addendum Feature D — Suppression du message d'accueil sur l'écran capture (2026-05-13)

### D-Decision 1 : Stratégie de retrait — UI-only, code `welcome/` conservé

- **Decision** : Supprimer **uniquement** la consommation UI du `welcomeUiState` dans `CameraScreen.kt` (import, `collectAsState`, bloc `if (welcomeState is WelcomeMessageUiState.Displayed) { Text(...) }`). Conserver le package `app/src/main/java/com/miamia/welcome/` (catalogue, sélecteur, policy) tel quel.
- **Rationale** :
  - Diff minimal ; risque très faible ; rétractation explicite mais réversible.
  - Préserve la couverture de tests unitaires existante (`WelcomeMessageSelectorTest`, `WelcomeMessageProviderTest`, etc.) sans modification.
  - Permet une réintroduction ultérieure ailleurs (par exemple écran résultat ou écran onboarding) si la décision produit change.
- **Alternatives considered** :
  - **Supprimer aussi le package `welcome/` + le flow `welcomeUiState` du ViewModel** : rejeté pour cette livraison — augmenterait inutilement la surface du diff (rétraction de code, tests à supprimer en cascade) et empêcherait un rollback rapide. Proposé comme suivi (post-livraison).
  - **Masquer la bannière via une feature flag** : rejeté — l'exigence est explicite et permanente, pas conditionnelle ; introduire un flag créerait de la dette inutile.

### D-Decision 2 : Sort du flow `CameraViewModel.welcomeUiState`

- **Decision** : Conserver l'exposition `welcomeUiState: StateFlow<WelcomeMessageUiState>` côté `CameraViewModel`. Simplement, l'UI capture ne le consomme plus. La mise à jour `onLoginSucceeded(...)` (`CameraViewModel:178`) reste fonctionnelle.
- **Rationale** :
  - Minimise le risque de casser des tests/ usages indirects.
  - Permet à un autre consommateur (futur écran, instrumentation) de réutiliser le flow.
- **Alternatives considered** :
  - Supprimer le `MutableStateFlow` + le getter exposé : rejeté pour cette livraison ; suivi possible (cleanup ultérieur).

### D-Decision 3 : Sort des tests AndroidTest `welcome/`

- **Decision** : Reconfigurer `US1WelcomeAfterLoginFlowTest` et `US2PositiveToneWelcomeTest` en tests d'**absence** : après un login simulé / un état nominal, vérifier qu'aucun nœud porteur du test tag `welcome_message_banner` n'existe dans l'arbre Compose de l'écran capture. `US3EmptyCatalogNoMessageTest` peut être conservé tel quel (asserte déjà l'absence).
- **Rationale** :
  - Préserve la valeur de couverture (« pas de message à l'ouverture ») tout en alignant les attentes avec la nouvelle exigence.
  - Évite la suppression brute, qui réduirait la couverture sans bénéfice.
- **Alternatives considered** :
  - **Suppression pure des tests** : rejeté — perte de couverture explicite « pas de message » que les nouveaux tests doivent justement attester.
  - **Conserver les tests inchangés** : rejeté — ils deviendraient rouges et bloqueraient le merge ; ce qui est attendu (ATDD) mais sans correctif consécutif s'ils ne sont pas reconfigurés ils restent rouges.

### D-Decision 4 : Test d'acceptation principal

- **Decision** : Ajouter un nouveau test d'instrumentation `NoWelcomeBannerOnCaptureUiTest.kt` dans `app/src/androidTest/java/com/miamia/camera/` qui couvre **plusieurs `ScanState`** représentatifs (`PreviewActive`, `CameraUnavailable`, `PermissionDenied`, `Error`) via `debugOverrideScanStateForTests`, et assert dans chacun `composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)`.
- **Rationale** :
  - Garantit UGE-D-FR-001/002 sur l'éventail réel d'états.
  - Découplé des tests `welcome/` existants (séparation responsabilités UGE vs sélection welcome).
- **Alternatives considered** :
  - **Étendre `CameraCaptureLayoutUiTest`** (capture-recognition) : rejeté — frontière DDD : l'absence de bannière relève de `user-guidance-experience`, pas du domaine de capture.

### D-Decision 5 : Impact espace / performance

- **Decision** : Pas d'objectif perf ajouté. Mesure du gain d'espace = hauteur effective d'une `Text(style = bodyLarge)` (≈ 24 dp typo + padding implicite Column), garantie ≥ 1 ligne `bodyLarge` (SC-D-002).
- **Rationale** : Bénéfice direct sur la `PreviewRegion` adaptative (`weight(1f)` capture-recognition), qui peut consommer le gain.
- **Alternatives considered** : n/a (mesure produit, pas d'option de mise en œuvre).

### D-Decisions agrégées

| ID | Décision | Statut |
|---|---|---|
| D-1 | Retrait UI-only, package `welcome/` conservé | ✅ |
| D-2 | `welcomeUiState` conservé côté ViewModel | ✅ |
| D-3 | Tests `welcome/` AndroidTest reconfigurés en assertions d'absence | ✅ |
| D-4 | Nouveau test acceptation `NoWelcomeBannerOnCaptureUiTest` multi-états | ✅ |
| D-5 | Gain d'espace ≥ 1 ligne `bodyLarge` | ✅ |

Aucune entrée NEEDS CLARIFICATION résiduelle pour Feature D.
