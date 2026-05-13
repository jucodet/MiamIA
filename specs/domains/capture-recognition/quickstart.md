# Quickstart — Vérifier la zone défilante texte capturé

## Prérequis

- Build debug installé sur émulateur ou appareil.
- Branche `019-captured-text-scroll` (ou équivalent) avec les changements `CameraScreen` fusionnés.

## Scénario A — Texte brut après « Voir le texte capturé »

1. Parcours jusqu’à un état proposant **« Voir le texte capturé »** ou **« Voir le texte brut »** (ex. `CompositionLimit`, `GemmaUnavailable` puis action).
2. S’assurer que le texte affiché est **long** (étiquette réelle ou jeu de données de test).
3. **Attendu** : le texte défile **dans une zone dédiée** ; le bouton principal (ex. « Nouveau scan ») reste **visible en bas** sans avoir à faire défiler toute la page pour l’atteindre.

## Scénario B — Confirmation de segment

1. Atteindre `SegmentConfirmationRequired` avec un `segmentPreview` long.
2. **Attendu** : scroll uniquement dans la zone de prévisualisation ; « Confirmer et analyser » et « Reprendre la photo » restent accessibles en bas de l’écran utile.

## Scénario C — Bilan prêt (carte longue)

1. Atteindre `BilanReady` avec un bilan volumineux (raw + sections).
2. **Attendu** : « Nouveau scan » reste visible en bas pendant que le contenu du bilan défile au-dessus.

## Régression

- Texte OCR vide : toujours bloqué conformément à CR-FR-005 (pas de régression navigation).

---

## Incrément 020 — Vérifier le bouton capture sous l’aperçu et son libellé

### Scénario D — Aperçu non recouvert (US1, P1)

1. Lancer l’app et atteindre l’écran capture (états live preview : `PreviewActive`).
2. **Attendu** :
   - La zone de prévisualisation vidéo (test tag `photo_preview_box`) est visible jusqu’à ses bords.
   - Aucun bouton ne chevauche, même partiellement, cette zone vidéo (`capture_photo_button` et `camera_tab_llm_test_button` sont en dessous).
   - Une rupture visuelle nette sépare l’aperçu et la bande d’action.

### Scénario E — Libellé « Y a quoi là-dedans ? » (US2, P1)

1. Sur le même écran capture, repérer l’action principale (test tag `capture_photo_button`).
2. **Attendu** :
   - Le libellé affiché est exactement **« Y a quoi là-dedans ? »**.
   - L’ancien libellé « Prendre la photo » ne doit plus apparaître nulle part (états live ni état `CameraUnavailable`).
3. Activer le bouton :
   - **Attendu** : déclenchement immédiat d’une capture (transition `Capturing` ou suivant le flux existant). Aucun changement de comportement.

### Scénario F — État `CameraUnavailable` (US1+US2)

1. Forcer un état `CameraUnavailable` (révoquer la permission caméra, ou simuler via outil de test).
2. **Attendu** :
   - Le placeholder de prévisualisation (test tag `photo_preview_placeholder`) reste visible sans chevauchement.
   - Le bouton `capture_photo_button` affiche **« Y a quoi là-dedans ? »** et est placé sous le placeholder.

### Commandes de tests (locales)

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraCaptureLayoutUiTest

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CaptureActionLabelUiTest

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraUnavailableLlmButtonUiTest
```

### Régression (incrément 020)

- `capturePhoto(...)` doit rester appelé exactement comme avant (aucun changement de signature, aucun changement de flux).
- Les test tags `photo_preview_box`, `capture_photo_button`, `camera_tab_llm_test_button` doivent rester stables (utilisés par d’autres tests existants).
