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

---

## Addendum Feature E — Catalogue phrases loaders (2026-05-13)

### E-Decision 1 : Conserver une liste Kotlin unique dans `WaitingPhrases.kt`

- **Decision** : Ajouter les dix chaînes à la constante `WAITING_PHRASES` existante ; pas de fichier JSON ni de CMS distant pour cette livraison.
- **Rationale** : Aligné sur l’architecture actuelle (Decision 5 historique « extraction vers `com.miamia.ui.shared` ») ; coût minimal ; pas d’exigence spec de externaliser le contenu.
- **Alternatives considered** :
  - **Fichier `res/values/strings.xml`** : rejeté — rotation et shuffle utilisent déjà une `List<String>` en code ; migration complète hors scope.
  - **Téléchargement distant des punchlines** : rejeté — hors scope et contradictoire avec l’hypothèse « pas de changement de mécanisme ».

### E-Decision 2 : Stratégie de test ATDD

- **Decision** : Test JVM sur `WAITING_PHRASES` : taille ≥ 21, ensemble de taille 21 (unicité), et chacune des dix phrases de l’annexe spec présente (égalité de chaîne après trim, ou inclusion explicite des 10 literals attendus).
- **Rationale** : Rapide, déterministe, pas besoin d’émulateur pour valider le contrat de données ; les AndroidTest existants couvrent la rotation UI.
- **Alternatives considered** :
  - **Screenshot / Compose only** : rejeté — ne prouve pas la présence des 10 nouvelles entrées dans le pool sans parcourir toutes les rotations.

### E-Decision 3 : Trace SC-E-002 (revue éditoriale)

- **Decision** : Une ligne dans `specs/domains/user-guidance-experience/migration-index.md` ou commentaire de PR listant les 10 textes livrés suffit comme « trace documentaire » pour la revue produit en équipe réduite.
- **Rationale** : SC-E-002 demande une trace légère, pas un workflow outillé.
- **Alternatives considered** : tableau de validation externe (rejeté : friction inutile pour du copywriting).

---

## Addendum Feature F — Libellés capture, suppression test LLM (2026-05-13)

### F-Decision 1 : Remplacer « Disponible » (MediaPipe) par un libellé explicite

- **Decision** : Modifier `MediaPipeStatusViewState` (label actuellement `"Disponible"`) pour une formulation en français qui indique clairement l’état utile pour l’utilisatrice (ex. « Détection d’étiquette prête » ou « Indicateur technique prêt » — libellé final validé en revue copy ≤ 80 caractères, **sans** le seul mot « Disponible »).
- **Rationale** : Aligné sur UGE-F-FR-001 et US-F1 ; la chaîne est aujourd’hui la source directe de l’ambiguïté signalée en recette.
- **Alternatives considered** :
  - **Masquer complètement la ligne MediaPipe en état nominal** : rejeté — l’indicateur technique reste une exigence historique de readiness ; seul le libellé change.
  - **Icône seule sans texte** : rejeté — accessibilité et critère de compréhension sans aide (UGE-F-SC-001).

### F-Decision 2 : Statut `PreviewActive` sans « Aperçu caméra actif »

- **Decision** : Dans `CameraScreen.kt`, remplacer la branche `ScanState.PreviewActive -> "Aperçu caméra actif"` par une phrase unique satisfaisant UGE-F-FR-001 (ex. « Caméra prête — vous pouvez scanner ») pour éviter deux lignes redondantes si MediaPipe affiche déjà une intention proche ; si double empreinte textuelle, fusionner en **une** ligne de statut sous la bande d’action.
- **Rationale** : US-F3 interdit la chaîne exacte « Aperçu caméra actif » ; US-F1 exige un libellé explicite pour l’état prêt — une seule ligne claire réduit le bruit.
- **Alternatives considered** :
  - **Supprimer toute ligne de statut en `PreviewActive`** : acceptable si l’équipe valide que la prévisualisation vidéo suffit ; défaut retenu = **conserver une ligne** explicite (meilleure couverture UGE-F-SC-001).

### F-Decision 3 : Retrait bouton Test LLM et chemin ViewModel

- **Decision** : Supprimer le composable du second bouton, les paramètres `onRunLlmTest`, la méthode `runCameraTabLlmMockTest()`, et retirer l’injection `HomeLlmMockRunner` de la factory `CameraViewModel` / `MainActivity` **si** aucun autre usage runtime ne subsiste pour l’écran capture. Conserver les classes `home/HomeLlmMockRunner.kt` temporairement si encore référencées par des tests hors UI — sinon nettoyage dans la même PR.
- **Rationale** : UGE-F-FR-002 ; réduction surface de test et de navigation involontaire.
- **Alternatives considered** :
  - **Feature flag pour cacher le bouton** : rejeté — exigence produit = suppression, pas masquage conditionnel.
  - **Déplacer le bouton dans un menu debug** : hors scope Feature F.

### F-Decision 4 : Tests AndroidTest

- **Decision** : Mettre à jour `CameraCaptureLayoutUiTest` pour ne plus référencer `camera_tab_llm_test_button` ; conserver l’assertion géométrique `photo_preview_box` vs `capture_photo_button`. Remplacer ou supprimer `CameraUnavailableLlmButtonUiTest` par un test « bande d’action sans second bouton » + libellés d’erreur inchangés pour `CameraUnavailable`.
- **Rationale** : SC-D-003 / edge cases Feature F ; ATDD — tests rouges avant vert après implémentation.
- **Alternatives considered** :
  - **Conserver le tag `camera_tab_llm_test_button` sur un composant invisible** : rejeté — dette et confusion.

### F-Decision 5 : Contrats inter-domaines

- **Decision** : Ajouter `contracts/capture-screen-feature-f-status-copy.md` dans ce domaine ; mettre à jour `specs/domains/capture-recognition/contracts/capture-action-bar.md` dans la même fenêtre de livraison pour retirer le second bouton et les mentions « Aperçu caméra actif » / test tag LLM.
- **Rationale** : UGE-F-FR-004 ; owner technique des tags = `capture-recognition`.
- **Alternatives considered** : **Dupliquer le contrat uniquement en UGE** : rejeté — risque de divergence ; le domaine technique doit rester SSOT des tags preview/bande d’action.

### F-Decisions agrégées

| ID | Décision | Statut |
|---|---|---|
| F-1 | Libellé MediaPipe explicite (plus « Disponible » seul) | ✅ |
| F-2 | Ligne statut `PreviewActive` sans « Aperçu caméra actif », texte explicite | ✅ |
| F-3 | Retrait UI + API ViewModel `runCameraTabLlmMockTest` + injection si orpheline | ✅ |
| F-4 | Réécriture tests layout / unavailable | ✅ |
| F-5 | Sync contrat `capture-recognition` | ✅ (tracé plan) |

Aucun NEEDS CLARIFICATION résiduel pour Feature F.

---

## Addendum Feature G — OCR direct, accueil épuré (2026-05-13)

### G-Decision 1 : Validation implicite systématique côté orchestration capture

- **Decision** : Dans `CameraViewModel.capturePhoto`, appeler `AnalysisSubmissionGate.evaluate` avec `implicitValidationFromIngredientsFraming = true` pour la décision pré-analyse (sans chip utilisateur), afin que `submissionAllowed` reflète uniquement les garde-fous transcript (vide, label seul) et non une attente de confirmation.
- **Rationale** : Alignement avec UGE-G-FR-001 / UGE-G-FR-004 sans étendre la surface API du gate ; le gate conserve les tests contractuels existants (chemins `userConfirmed` / `implicit` inchangés).
- **Alternatives considered** :
  - **Nouveau paramètre « directAnalyze » sur le gate** : rejeté — surface API plus large pour un besoin déjà couvert par `implicitValidationFromIngredientsFraming`.
  - **Supprimer le gate** : rejeté — garde-fous label-seul / vide restent nécessaires.

### G-Decision 2 : Retrait `SegmentConfirmationRequired` et chrome associée

- **Decision** : Supprimer la variante `SegmentConfirmationRequired` de `ScanState`, retirer la branche Compose correspondante, supprimer `rejectSegmentConfirmation` et le chip « Balise ingrédients » + `ingredientsFramingTagActive` / `setIngredientsFramingTagActive`.
- **Rationale** : UGE-G-FR-002, UGE-G-FR-004 ; modèle d’état minimal.
- **Alternatives considered** :
  - **Conserver l’état pour tests/debug** : rejeté — risque de réactivation accidentelle ; les tests peuvent cibler l’absence d’UI et le flux direct.

### G-Decision 3 : Ligne de statut sous l’aperçu pour `PreviewActive`

- **Decision** : Ne pas afficher de `Text` de statut dédié pour `PreviewActive` (ni chaîne interdite, ni invitation obligatoire). Conserver des libellés optionnels pour les états transitoires (`PreviewInitializing`, `Capturing`, `Analyzing`) hors chaîne interdite.
- **Rationale** : UGE-G-FR-003, US-G2 scénario 3.
- **Alternatives considered** :
  - **Texte alternatif « prêt »** : rejeté — l’exigence est l’absence de ligne imposée à cet emplacement pour l’état prêt.

Aucun NEEDS CLARIFICATION résiduel pour Feature G.
