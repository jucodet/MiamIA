# Quickstart: Feature 008 (Capture photo -> texte affiché)

## Prérequis

- Android Studio avec SDK Android 34
- Appareil ou émulateur Android API 26+
- Permission caméra disponible

## Lancer l'application

1. Ouvrir le projet `MiamIA`.
2. Compiler et lancer l'app en debug.
3. Ouvrir l'écran de capture.

## Vérification manuelle principale (ATDD)

1. **Given** l'écran affiche l'action "Prendre la photo"  
   **When** l'utilisateur appuie sur le bouton  
   **Then** une capture réelle est lancée et un état "traitement en cours" apparaît.

2. **Given** une image contenant du texte lisible  
   **When** le traitement local se termine  
   **Then** le texte extrait est affiché en moins de 10 secondes.

3. **Given** une image sans texte exploitable  
   **When** l'analyse se termine  
   **Then** un message explicite "aucun texte détecté" (ou équivalent) est affiché.

4. **Given** une erreur locale de lecture  
   **When** le flux échoue  
   **Then** un message compréhensible + option de relance est affiché.

5. **Given** le téléphone est hors-ligne  
   **When** l'utilisateur lance une capture  
   **Then** la lecture de texte fonctionne (ou échoue localement) sans dépendre du réseau.

## Points de contrôle techniques

- Vérifier qu'aucun appel réseau n'est requis pour la lecture de texte.
- Vérifier qu'aucun texte factice n'est injecté dans les résultats.
- Vérifier suppression/rotation du fichier image temporaire selon le flux défini.

## Résultat de validation (2026-04-24)

- Build unitaire exécuté: `:app:testDebugUnitTest` -> **SUCCESS**
- Vérification statique: pas d'erreur linter sur les fichiers modifiés de la feature.
