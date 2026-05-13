# Contract — UI surface « écran capture sans bannière d'accueil » (user-guidance-experience)

**Type** : contrat comportemental UI (application Android Compose).  
**Consommateurs** : équipe mobile (écran `CameraScreen`) ; aucun consommateur réseau.  
**Incrément** : Feature D — Suppression du message d'accueil sur l'écran capture.

## Obligations

1. **Aucune bannière d'accueil rendue** : à tout instant et dans tout `ScanState` (`PermissionDenied`, `CameraUnavailable`, `CameraReady`, `PreviewInitializing`, `PreviewActive`, `Capturing`, `Analyzing`, `CompositionAnalyzing`, `BilanReady`, `GemmaUnavailable`, `CompositionLimit`, `Success`, `SegmentConfirmationRequired`, `Empty`, `Error`), l'arbre Compose de l'écran capture (= écran d'accueil) MUST NOT contenir de composable affichant un message issu du catalogue `welcome/`.
2. **Aucun nœud `welcome_message_banner`** : `composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)` MUST passer pour tout état du `CameraScreen`.
3. **Pas de consommation UI de `WelcomeMessageUiState`** : aucun composable rendu sur l'écran capture ne MUST collecter ni projeter `WelcomeMessageUiState.Displayed` en élément visible. La présence du flow côté `CameraViewModel.welcomeUiState` reste autorisée tant qu'il n'est pas consommé pour rendre du texte à l'écran.
4. **Préservation des autres éléments de chrome** : la chrome non-camera de l'écran capture MUST conserver `MediaPipeStatusIndicator` (en haut, hors zone caméra) et la `CaptureActionBar` (sous la zone caméra, contrat dédié `capture-recognition/contracts/capture-action-bar.md`). Le statut textuel (« Aperçu caméra actif », etc.) MUST rester sous la bande d'action.
5. **Gain d'espace** : le retrait MUST libérer l'espace vertical précédemment occupé par la bannière (≥ 1 ligne `bodyLarge`), au bénéfice de la `PreviewRegion` (adaptative via `weight(1f)`).

## Non-obligations

- Suppression du package `app/src/main/java/com/miamia/welcome/` : **non requise** par ce contrat. Suivi possible (nettoyage post-livraison).
- Suppression du flow `CameraViewModel.welcomeUiState` : **non requise**. Suivi possible.
- Modification des tests unitaires `welcome/` (sélecteur, policy, catalogue) : **non requise** ; ils restent valides en tant que tests de logique.
- Choix d'un composant Compose particulier pour la chrome restante : libre tant que les obligations 1–5 sont respectées.

## Test tags

- **Stables (conservés)** : `photo_preview_box`, `photo_preview_placeholder`, `capture_photo_button`, `camera_tab_llm_test_button` (contrats `capture-recognition`).
- **Absent (obligation contractuelle)** : `welcome_message_banner` MUST NOT exister dans l'arbre Compose de l'écran capture.

## Tests d'acceptation associés

- **Nouveau** : `app/src/androidTest/java/com/miamia/camera/NoWelcomeBannerOnCaptureUiTest.kt` couvrant ≥ 3 `ScanState` (live preview + `CameraUnavailable` + état d'erreur représentatif) avec `assertCountEquals(0)` sur `welcome_message_banner`.
- **Reconfigurés (UGE-D-FR-004)** : `app/src/androidTest/java/com/miamia/welcome/US1WelcomeAfterLoginFlowTest.kt` et `app/src/androidTest/java/com/miamia/welcome/US2PositiveToneWelcomeTest.kt` deviennent des tests d'**absence** (assert qu'aucun nœud `welcome_message_banner` n'apparaît même après un login simulé).

## Traçabilité spec

- `spec.md` — Feature D, UGE-D-FR-001..005 ; user stories US-D1 (P1), US-D2 (P2) ; SC-D-001..004.
- `research.md` — D-Decision 1..5.
- `data-model.md` — Addendum Feature D, INV-D-1.

## Dépendances inter-domaines

- **`capture-recognition`** : le contrat `capture-action-bar.md` reste applicable à l'identique. Le gain d'espace bénéficie à la `PreviewRegion` adaptative (`weight(1f)` + `heightIn(220..480 dp)`). Aucun changement contractuel.
- **`local-llm-runtime`** : indirect via `ScanState.GemmaUnavailable` ; aucun changement.
