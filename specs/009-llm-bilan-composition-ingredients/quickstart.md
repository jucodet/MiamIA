# Quickstart: Feature 009 (Bilan Gemma local après texte capturé)

## Prérequis

- Android Studio, SDK 34, appareil ou émulateur **API 26+**
- Fichier modèle **Gemma** attendu par l’app (emplacement documenté au moment de l’implémentation — ex. `assets/`). **Sans ce fichier**, le flux doit afficher l’erreur **« Gemma introuvable »** (ou équivalent).
- Permission caméra pour atteindre le texte capturé (feature 008)

## Lancer l’application

1. Ouvrir le dépôt `MiamIA`.
2. Synchroniser Gradle ; vérifier la dépendance **LiteRT** présente.
3. Placer le modèle Gemma attendu **ou** volontairement l’omettre pour tester l’erreur.
4. Lancer l’app en debug, effectuer une capture avec texte lisible.

## Vérification manuelle principale (ATDD)

1. **Given** le texte OCR est disponible et Gemma est **présent**  
   **When** l’analyse locale se termine  
   **Then** l’écran affiche **liste** + **analyse** comme contenu principal (texte brut non dominant).

2. **Given** Gemma est **absent** (fichier retiré ou jamais fourni)  
   **When** l’utilisateur tente le bilan  
   **Then** un message d’**erreur explicite** (ex. Gemma introuvable) s’affiche, **sans** bilan liste+analyse factice, avec possibilité de **réessai** / repli texte brut si prévu.

3. **Given** l’analyse Gemma est en cours  
   **When** l’utilisateur attend  
   **Then** un indicateur de **progression** est visible (FR-008).

4. **Given** le téléphone est **hors-ligne**  
   **When** le bilan est demandé  
   **Then** le comportement dépend uniquement des **ressources locales** (Gemma + LiteRT), sans appel réseau pour l’étape LLM.

5. **Given** un texte OCR ambigu sans liste exploitable  
   **When** le résultat est présenté  
   **Then** message de **limite** (`composition_limit`) plutôt qu’une liste inventée.

## Points de contrôle techniques

- Vérifier qu’aucune requête réseau n’est nécessaire pour Gemma (spec FR-011).
- Vérifier codes erreur `gemma_not_found` / `gemma_load_failed` dans les logs internes (facultatif) et message utilisateur clair.
- Profiler mémoire / temps première inférence sur un appareil réel milieu de gamme.

## Résultat de validation (implémentation 2026-04-24)

- `:app:testDebugUnitTest` : **SUCCESS** (tests composition + `CameraViewModelGemmaErrorTest`).
- Sans fichier `assets/gemma/gemma_model.litertlm` dans l’APK : parcours **Gemma introuvable** / `GemmaUnavailable` après OCR réussi.
- Avec fichier modèle présent : inférence **LiteRT-LM** (`LiteRtGemmaEngine`) ; toolchain **Kotlin 2.3.21** + **Room 2.8.4** + **KSP 2.3.7** (requis par `litertlm-android` 0.10.x).
