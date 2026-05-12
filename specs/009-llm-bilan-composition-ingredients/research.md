# Research: Bilan composition avec Gemma local

## Décision 1: Runtime LLM = Gemma via LiteRT (on-device)

- **Decision**: Exécuter Gemma **localement** avec le runtime **LiteRT** (`com.google.ai.edge.litert:litert`), déjà déclaré dans le module `app`, pour transformer le **texte capturé** en structure **bilan** (liste d’ingrédients + analyse composition).
- **Rationale**: Alignement avec la directive produit/plan (« grâce à Gemma »), avec FR-011 (pas d’envoi texte hors appareil), cohérence stack MiamIA (LiteRT déjà intégré), une seule pile à maintenir pour inférence edge.
- **Alternatives considered**:
  - **MediaPipe Tasks GenAI / LLM Inference** avec Gemma : viable ; rejetée comme primaire pour MVP pour éviter une deuxième stack si LiteRT suffit — réouverture possible si contraintes modèle/format imposent MediaPipe.
  - **Service cloud LLM**: exclu (contradictoire avec spec + clarifications).

## Décision 2: Empaquetage modèle Gemma et détection « non trouvé »

- **Decision**: Livrer ou référencer le fichier modèle Gemma (ex. sous `assets/` ou répertoire files après première copie) avec un **chemin canonique** connu de l’app ; `GemmaModelLocator` vérifie **existence + lisibilité** avant init LiteRT. Tout échec (fichier absent, 0 octets, nom non conforme, init runtime) mappe vers un **code d’erreur** stable (ex. `gemma_not_found` / `gemma_load_failed`) et un **message utilisateur** du type « Gemma introuvable sur cet appareil » (libellé final en UX).
- **Rationale**: Satisfait explicitement la demande « message d’erreur si Gemma n’est pas trouvé » et FR-012 (pas de bilan simulé).
- **Alternatives considered**:
  - **Téléchargement à la demande** depuis CDN : rejeté pour le critère « non trouvé » simple et pour réduire surface réseau ; pourrait être une **phase 2** si taille APK prohibitive.
  - **Plusieurs variantes de modèle** (2B / 7B) : MVP = une variante documentée ; sélection automatique reportée.

## Décision 3: Sortie structurée vs prompt-only

- **Decision**: Utiliser un **prompt système + utilisateur** versionné qui contraint la sortie (sections LISTE / ANALYSE, ton factuel, interdiction d’inventer des ingrédients) puis **parser** la réponse en champs UI (liste lignes + paragraphes analyse) ; validation minimale (non vide, pas de marqueurs d’échec internes) avant affichage `BilanSuccess`.
- **Rationale**: Contrôle testable, correspond aux sections SC-002 ; permet tests unitaires sur parseur + golden files sans GPU en CI lorsque mocks injectés.
- **Alternatives considered**:
  - **JSON strict forcé dans le prompt** : préférable long terme ; risque parse JSON fragile sur petit modèle — itération 2.
  - **RAG base locale** : hors scope MVP sauf si allégations réglementaires exigent sources attachées (spec FR-006 / politique éditoriale).

## Décision 4: Intégration flux existant caméra / OCR

- **Decision**: Conserver OCR ML Kit dans `LocalOcrFallbackRecognizer` / coordinateur existant ; **après** `outcome` texte exploitable, appeler `GemmaCompositionAnalyzer` sur la **transcription agrégée** ; mettre à jour `ScanState` (ou équivalent) pour distinguer **analyse Gemma en cours** vs **bilan prêt** vs **erreur Gemma**.
- **Rationale**: Respecte l’assumption spec « texte déjà disponible » ; limite régression capture.
- **Alternatives considered**:
  - **Remplacer l’écran texte par uniquement Gemma sans étape intermédiaire** : rejeté (OCR déjà la source de vérité textuelle).

## Décision 5: Performance et mémoire

- **Decision**: Charger Gemma **à la demande** (lazy) au premier besoin post-capture, avec **timeout** configurable aligné SC-003 ; libérer ressources LiteRT en fin de flux ou `onCleared` ViewModel si singleton non souhaité.
- **Rationale**: Constitution « performance comme exigence » ; évite pression mémoire permanente.
- **Alternatives considered**:
  - **Modèle résident en mémoire dès cold start** : rejeté MVP (temps démarrage + RAM).
