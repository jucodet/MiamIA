# Quickstart: llm-download-onboarding

## Validation manuelle

### Scénario 1 — Premier lancement avec Wi-Fi

1. Désinstaller l'application (ou effacer les données).
2. S'assurer que l'appareil est connecté en Wi-Fi.
3. Lancer l'application.
4. **Vérifier** : l'écran plein de confirmation s'affiche avec le message "Connexion Wi-Fi détectée", la taille approximative du modèle, et les boutons "Confirmer" / "Plus tard".
5. Appuyer sur "Confirmer".
6. **Vérifier** : l'écran d'attente s'affiche avec :
   - Titre "Téléchargement du modèle de langage en cours..."
   - Barre de progression qui avance
   - Pourcentage affiché
   - Fouet mixeur animé
   - Phrases humoristiques qui changent toutes les ~5s
7. Attendre la fin du téléchargement.
8. **Vérifier** : redirection automatique vers l'écran capture (caméra).

### Scénario 2 — Premier lancement en données mobiles

1. Désinstaller l'application, passer en données mobiles (désactiver Wi-Fi).
2. Lancer l'application.
3. **Vérifier** : l'écran de confirmation affiche un avertissement explicite sur la consommation de données mobiles.
4. Appuyer sur "Confirmer".
5. **Vérifier** : téléchargement et écran d'attente fonctionnent normalement.

### Scénario 3 — Premier lancement hors-ligne

1. Désinstaller l'application, activer le mode avion.
2. Lancer l'application.
3. **Vérifier** : l'écran "Connexion requise" s'affiche avec explication et bouton "Réessayer".
4. Réactiver le réseau.
5. Appuyer sur "Réessayer".
6. **Vérifier** : transition vers l'écran de confirmation.

### Scénario 4 — Refus du téléchargement

1. Arriver sur l'écran de confirmation.
2. Appuyer sur "Plus tard".
3. **Vérifier** : un état explicatif indique que l'application ne peut pas fonctionner sans le modèle, avec possibilité de relancer le téléchargement.

### Scénario 5 — Erreur réseau pendant téléchargement

1. Démarrer le téléchargement.
2. Activer le mode avion pendant le téléchargement.
3. **Vérifier** : un message d'erreur clair s'affiche avec un bouton "Réessayer".
4. Réactiver le réseau et appuyer sur "Réessayer".
5. **Vérifier** : le téléchargement redémarre (V1 : depuis le début).

### Scénario 6 — Modèle déjà présent (relancement normal)

1. Lancer l'application avec le modèle déjà téléchargé.
2. **Vérifier** : aucun écran onboarding ne s'affiche ; accès direct à l'écran capture.

---

## Addendum Feature D — Vérifier l'absence du message d'accueil sur l'écran capture

### Scénario D1 — Premier rendu de l'écran capture (US-D1, P1)

1. Lancer l'application (modèle présent, caméra accordée).
2. **Attendu** :
   - L'écran capture s'affiche directement.
   - Aucune bannière texte ne précède l'aperçu caméra. La chrome se réduit à : `MediaPipeStatusIndicator` (haut) → `photo_preview_box` (zone caméra adaptative) → `CaptureActionBar` (au minimum « Y a quoi là-dedans ? ») → statut textuel explicite (Feature F : plus de « Aperçu caméra actif » ni « Disponible » seul ; pas de bouton « Test LLM »).

### Scénario D2 — État `CameraUnavailable` (US-D1, P1)

1. Forcer un état `CameraUnavailable` (révoquer la permission caméra ou simuler via `debugOverrideScanStateForTests`).
2. **Attendu** :
   - Le placeholder `photo_preview_placeholder` s'affiche normalement.
   - Aucune bannière d'accueil n'apparaît au-dessus de `Caméra indisponible: …`.

### Scénario D3 — Retour depuis un écran secondaire (US-D1, P1)

1. Depuis l'écran capture, naviguer vers l'écran résultat (ou un écran secondaire).
2. Revenir en arrière vers l'écran capture.
3. **Attendu** : aucune bannière d'accueil n'apparaît au retour. La chrome reste identique.

### Scénario D4 — Non-régression sur l'action principale (US-D2, P2)

1. Sur l'écran capture, activer « Y a quoi là-dedans ? ».
2. **Attendu** : démarrage du flux capture inchangé (cohérence avec `capture-recognition` CR-FR-001..011 et `user-guidance-experience` UGE-A-FR-001..016, sous réserve Feature F : plus de bouton Test LLM).

### Scénario F1 — Libellés et absence Test LLM (Feature F, P1)

1. Lancer l'app (modèle présent, caméra accordée), arriver sur l'écran capture en `PreviewActive`.
2. **Attendu** :
   - Aucun bouton « Test LLM » ; aucun nœud avec tag `camera_tab_llm_test_button`.
   - Aucune occurrence du texte exact « Aperçu caméra actif ».
   - Les libellés d'état visibles ne se réduisent pas au seul mot « Disponible » pour indiquer « prêt à scanner ».
3. (Optionnel) Parcourir un état transitoire (`PreviewInitializing`) : texte lisible, sans chaîne interdite.

### Scénario G1 — Accueil épuré (Feature G, P1, US-G2)

1. Même contexte qu’en F1 (`PreviewActive`).
2. **Attendu** :
   - Aucun nœud avec tag `ingredients_framing_tag_chip` ; aucun chip « Balise ingrédients ».
   - Aucune occurrence de la chaîne exacte « Caméra prête — vous pouvez scanner ».
   - Pas de ligne de statut textuelle obligatoire sous l’aperçu pour signifier « prêt » (la vidéo live et le bouton « Y a quoi là-dedans ? » suffisent).

### Scénario G2 — Parcours nominal sans relecture transcript (Feature G, P1, US-G1)

1. Capturer une étiquette avec texte OCR exploitable (non vide, pas seulement le mot « Ingrédients : »).
2. **Attendu** : transition vers chargement / analyse ou écran résultat **sans** feuille intermédiaire « Vérifier le texte reconnu » / boutons « Confirmer et analyser ».

### Commandes de tests (locales)

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.NoWelcomeBannerOnCaptureUiTest

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.welcome.US1WelcomeAfterLoginFlowTest

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.welcome.US2PositiveToneWelcomeTest
```

### Régression (Feature D)

- Après Feature F : seul le bouton « Y a quoi là-dedans ? » reste exigé sur la bande d'action ; l'indicateur MediaPipe et la navigation post-capture restent fonctionnels.
- Le flow `CameraViewModel.welcomeUiState` reste exposé mais non consommé ; les tests unitaires `welcome/` (sélecteur, policy, catalogue) restent valides à l'identique.

---

## Addendum Feature E — Phrases loaders (catalogue élargi)

### Scénario E1 — Téléchargement modèle (rotation)

1. Réinstaller l'app ou supprimer le modèle pour forcer l'écran d'attente téléchargement.
2. Lancer le téléchargement et observer les phrases sous la marmite (`download_waiting_phrase`).
3. **Attendu** : au fil des rotations (~5 s), apparaissent parfois les **nouvelles** formulations (ton alimentaire / étiquette, sans contenu offensant). Le pool paraît plus varié qu'avant livraison.

### Scénario E2 — Loader streaming résultat

1. Lancer une capture menant à l'écran résultat avec streaming (ou tout parcours qui affiche le loader streaming).
2. Pendant l'état d'attente initial (`streaming_waiting_phrase`), observer les rotations.
3. **Attendu** : les mêmes nouvelles phrases peuvent apparaître (catalogue partagé), affichage lisible sans troncature anormale sur téléphone portrait.

### Commandes (locales)

```bash
./gradlew :app:testDebugUnitTest --tests "com.miamia.ui.shared.WaitingPhrasesCatalogFeatureETest"

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.onboarding.ModelDownloadWaitingAcceptanceTest
```
