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
