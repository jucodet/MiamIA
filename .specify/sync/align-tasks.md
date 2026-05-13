# Drift Alignment Tasks

Generated: 2026-05-12T17:52:00+02:00
Based on: proposals.json approved 2026-05-12

> Ces tâches décrivent les changements de code nécessaires pour aligner l'implémentation sur les specs.
> Elles sont ordonnées par phase (2 = corrections simples, 3 = modifications modérées).

---

## Phase 2 — Corrections simples

---

### Task P6: Restaurer les diacritiques dans MOCK_INGREDIENTS_INPUT

**Proposal**: P6 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-health-intelligence` / FR-002, FR-010
**Current Code**: `MOCK_INGREDIENTS_INPUT` utilise des caractères ASCII sans accents (`BLE`, `complete`, `lecithines`, `ecreme`, `aromes`)
**Required Change**: Restaurer les diacritiques UTF-8 pour correspondre à la chaîne mockée de la spec
**Files to Modify**:
- `app/src/main/java/com/miamia/home/HomeLlmMockRunner.kt` (lignes 63-67)
**Estimated Effort**: small

### Acceptance Criteria
- [ ] `MOCK_INGREDIENTS_INPUT` contient les diacritiques : `BLÉ`, `complète`, `lécithines`, `écrémé`, `arômes`
- [ ] La chaîne est strictement identique caractère par caractère à celle définie dans la spec FR-002
- [ ] Les tests unitaires existants passent avec la chaîne corrigée
- [ ] Un test vérifie l'égalité stricte entre la constante et la charge envoyée (FR-010)

---

### Task P8a: Ajouter assertion de non-altération trim

**Proposal**: P8 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-health-intelligence` / FR-003
**Current Code**: `AnalysisInputBuilder.buildSegmentPayload()` applique `trim()` sans assertion de non-altération
**Required Change**: Ajouter un test assertant `payload == MOCK_INGREDIENTS_INPUT.trim()`
**Files to Modify**:
- `app/src/test/java/com/miamia/home/HomeLlmMockTriggerTest.kt`
**Estimated Effort**: small

### Acceptance Criteria
- [ ] Un test vérifie que `AnalysisInputBuilder.buildSegmentPayload(MOCK_INGREDIENTS_INPUT)` == `MOCK_INGREDIENTS_INPUT.trim()`
- [ ] Le test échoue si `buildSegmentPayload` altère le contenu (pas seulement le whitespace)

---

### Task P8b: Enrichir HomeLlmMockOutcome avec inputUsed

**Proposal**: P8 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-health-intelligence` / FR-005
**Current Code**: `HomeLlmMockOutcome` ne contient pas de référence à l'input utilisé
**Required Change**: Ajouter un champ `inputUsed: String` à `HomeLlmMockOutcome` et le renseigner dans `HomeLlmMockRunner`
**Files to Modify**:
- `app/src/main/java/com/miamia/home/HomeLlmMockRunner.kt` (définition de `HomeLlmMockOutcome`)
- `app/src/main/java/com/miamia/home/HomeViewModel.kt` (propagation si nécessaire)
**Estimated Effort**: small

### Acceptance Criteria
- [ ] `HomeLlmMockOutcome.Success` et `HomeLlmMockOutcome.Failure` exposent `inputUsed`
- [ ] La valeur de `inputUsed` correspond exactement à la chaîne transmise à l'analyse
- [ ] Les appelants existants compilent sans erreur

---

### Task P8c: Ajouter @Tag("manual") au test mock

**Proposal**: P8 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-health-intelligence` / FR-011
**Current Code**: `HomeLlmMockTriggerTest` est un `@Test` standard inclus dans les suites automatiques
**Required Change**: Ajouter `@Tag("manual")` et configurer Gradle pour exclure ce tag
**Files to Modify**:
- `app/src/test/java/com/miamia/home/HomeLlmMockTriggerTest.kt`
- `app/build.gradle.kts` (si configuration d'exclusion nécessaire)
**Estimated Effort**: small

### Acceptance Criteria
- [ ] La classe `HomeLlmMockTriggerTest` porte `@Tag("manual")`
- [ ] Le test n'est pas exécuté par `./gradlew test` sans filtre explicite
- [ ] Le test reste exécutable manuellement via `--tests` ou IDE

---

### Task P9: Supprimer enum mortes LINE_END et NO_NEWLINE_TO_EOF

**Proposal**: P9 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-normalization-validation` / FR-001, FR-004
**Current Code**: `IngredientSegmentBoundaryEndReason.LINE_END` et `IngredientSegmentFallbackMode.NO_NEWLINE_TO_EOF` existent sans appelant
**Required Change**: Supprimer ces deux valeurs d'enum et tout test les référençant
**Files to Modify**:
- `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientSegmentModels.kt` (lignes 12, 18)
- Tests éventuels référençant ces valeurs
**Estimated Effort**: small

### Acceptance Criteria
- [ ] `IngredientSegmentBoundaryEndReason.LINE_END` n'existe plus
- [ ] `IngredientSegmentFallbackMode.NO_NEWLINE_TO_EOF` n'existe plus (ou l'enum entière si plus de valeurs)
- [ ] Le projet compile sans erreur
- [ ] Aucun test ne référence ces valeurs supprimées

---

### Task P11: Ancre absente → ScanState.Empty avec message explicite

**Proposal**: P11 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-normalization-validation` / FR-008
**Current Code**: Ancre absente → `ScanState.Success(transcriptText)` sans message d'erreur explicite
**Required Change**: Remplacer par `ScanState.Empty(message = "Aucune liste d'ingrédients détectée...")` avec voie de reprise
**Files to Modify**:
- `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (lignes 367-375)
**Estimated Effort**: small

### Acceptance Criteria
- [ ] Quand l'ancre "Ingrédients" est absente du texte OCR, `ScanState.Empty` est émis avec un message compréhensible
- [ ] Le message indique que la mention "Ingrédients" n'a pas été trouvée
- [ ] L'UI `CameraScreen` affiche ce message + un bouton "Nouveau scan" (reprise)
- [ ] `ScanState.Success` n'est plus émis pour les cas sans ancre

---

## Phase 3 — Modifications modérées

---

### Task P2b: Connecter fallback OCR à LlmResultScreen

**Proposal**: P2 — ALIGN (Spec → Code)
**Spec Requirement**: `user-guidance-experience` / FR-018
**Current Code**: `buildLlmResultFallbackPayload()` existe dans `CameraViewModel` mais n'est jamais appelé depuis `LlmResultScreen`. Le fallback OCR n'est accessible que depuis `CameraScreen`.
**Required Change**: Quand `StreamingBilanState.Error` est affiché sur `LlmResultScreen`, inclure le texte OCR brut comme contenu de repli (si disponible)
**Files to Modify**:
- `app/src/main/java/com/miamia/result/LlmResultScreen.kt` (composable `ErrorContent`)
- `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (exposer `rawTranscript` au streaming state)
**Estimated Effort**: medium

### Acceptance Criteria
- [ ] Quand l'analyse LLM échoue avec erreur, l'écran résultat affiche le texte OCR brut comme contenu de repli
- [ ] Le texte OCR est présenté dans une section clairement identifiée ("Texte reconnu" ou équivalent)
- [ ] Un bouton d'action "Reprendre la photo" ou "Retour" est disponible
- [ ] Si aucun texte OCR n'est disponible, l'erreur s'affiche sans section de repli

---

### Task P5: Implémenter la vraie reprise de téléchargement (HTTP Range)

**Proposal**: P5 — ALIGN (Spec → Code)
**Spec Requirement**: `user-guidance-experience/llm-download-onboarding` / FR-013
**Current Code**: `GemmaModelDownloader` supprime le fichier `.downloading` partiel et recommence à zéro. L'UI affiche "Reprendre le téléchargement" de façon mensongère.
**Required Change**: Implémenter la reprise réelle via HTTP Range headers
**Files to Modify**:
- `app/src/main/java/com/miamia/gemma4local/GemmaModelDownloader.kt` (logique de téléchargement)
- `app/src/main/java/com/miamia/onboarding/ModelDownloadViewModel.kt` (initialiser la progression partielle)
**Estimated Effort**: medium

### Acceptance Criteria
- [ ] Le fichier `.downloading` existant n'est PAS supprimé au redémarrage
- [ ] Un header HTTP `Range: bytes=<file_size>-` est ajouté à la requête si un fichier partiel existe
- [ ] Le fichier est ouvert en mode append (pas écraser)
- [ ] Si le serveur ne supporte pas `Range` (réponse non-206), le téléchargement recommence à zéro avec log
- [ ] La progression initiale reflète la taille du fichier partiel existant
- [ ] Le test de non-régression vérifie que le fichier final est identique à un téléchargement complet

---

### Task P10: Refactorer regex ancre + supprimer duplication

**Proposal**: P10 — ALIGN (Spec → Code)
**Spec Requirement**: `ingredient-normalization-validation` / FR-002
**Current Code**: (a) Regex exige début de ligne `(?m)(^\s*)` ; (b) Regex dupliquée dans `IngredientExtractionPipeline`
**Required Change**: Retirer la contrainte début de ligne et supprimer la duplication
**Files to Modify**:
- `app/src/main/java/com/miamia/analysis/ingredientsegment/IngredientAnchorNormalizer.kt` (regex, ligne 10)
- `app/src/main/java/com/miamia/recognition/IngredientExtractionPipeline.kt` (supprimer regex dupliquée, réutiliser IngredientAnchorNormalizer)
- `app/src/test/java/com/miamia/analysis/ingredientsegment/IngredientAnchorContractTest.kt` (ajouter cas ancre en milieu de ligne)
**Estimated Effort**: medium

### Acceptance Criteria
- [ ] La regex dans `IngredientAnchorNormalizer` ne contient plus de contrainte `^` (début de ligne)
- [ ] `IngredientExtractionPipeline` réutilise `IngredientAnchorNormalizer.findFirstPhraseAnchorIndex()` au lieu de sa propre regex
- [ ] Un test vérifie la détection d'ancre en milieu de ligne (ex. "Info nutritionnelles Ingrédients: eau, sel")
- [ ] Les tests existants passent (pas de régression sur les cas début de ligne)
- [ ] Valider sur des échantillons OCR réels pour vérifier l'absence de faux positifs
