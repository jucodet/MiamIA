# Spec Drift Report

Generated: 2026-05-12T17:14:00+02:00
Project: MiamIA
Scope: `specs/domains/` uniquement (specs racine exclues)

## Summary

| Category | Count |
|----------|-------|
| Specs Analyzed | 6 (across 5 domains) |
| Requirements Checked | 66 |
| ✓ Aligned | 41 (62 %) |
| ⚠️ Drifted | 20 (30 %) |
| ✗ Not Implemented | 5 (8 %) |
| 🆕 Unspecced Code | 5 packages (~3 200 lignes) |

---

## Detailed Findings

---

### Spec: user-guidance-experience — photo-capture-llm-result-flow

**Fichier spec** : `specs/domains/user-guidance-experience/spec.md`
**Exigences** : FR-001 → FR-021

#### Aligned ✓

- **FR-001** : Écran de prise de photo = premier écran → `MainActivity.kt:188` `startDest = CameraFlowRoutes.Capture` (quand modèle prêt)
- **FR-002** : Prévisualisation caméra réelle → `CameraPreviewBox.kt` enveloppe un `PreviewView` CameraX réel, `CameraViewModel.attachPreview()` attache le flux
- **FR-003** : Contrôle de mise au point sur la prévisualisation → `CameraScreen.kt:319-330` câble `detectTapGestures` + `viewModel.tapToFocus()` avec `FocusRingIndicator` animé
- **FR-004** : Bouton photo sous la prévisualisation → `CameraScreen.kt:362-370` placé dans la `Column` verticale après le `Box` de prévisualisation
- **FR-005** : Bouton test LLM sous le bouton photo → `CameraScreen.kt:371-380` immédiatement après le bouton capture
- **FR-008** : Réutilisation runner test LLM existant → `CompositionEngineHomeLlmMockRunner` injecté via la factory `CameraViewModel`
- **FR-009** : Désactivation bouton test LLM pendant exécution → `canRunCameraTabLlmTest()` avec guard `AtomicBoolean.compareAndSet` (`CameraViewModel.kt:186-195`)
- **FR-010** : Message d'erreur explicite → `ErrorContent` dans `LlmResultScreen.kt:407-447` affiche icône, titre, catégorie et message
- **FR-011** : Message si caméra indisponible → `ScanState.CameraUnavailable` + message explicite (`CameraScreen.kt:109-129`)
- **FR-012** : Nouvelle capture après fin de cycle → `onRetry()`, `resetStreamingBilan()`, boutons "Nouveau scan" / "Retour"
- **FR-015** : Pas de barre d'onglets → `NavHost` sans scaffold à onglets dans `MainActivity`
- **FR-016** : Retour à l'écran photo après résultat → `popBackStack()` ramène à `CameraFlowRoutes.Capture`
- **FR-019** : Action de récupération en cas d'erreur → Bouton "Retour" sur `LlmResultScreen` + boutons "Réessayer l'analyse", "Voir le texte brut", "Nouveau scan" sur les états d'erreur de `CameraScreen`
- **FR-020** : Zone de lecture adaptée pour transcription longue → `Column.weight(1f).verticalScroll(rememberScrollState())` (`LlmResultScreen.kt:80-86`)
- **FR-021** : Contrôles accessibles sous la transcription → Bouton "Retour" hors zone scrollable (`LlmResultScreen.kt:115-127`)

#### Drifted ⚠️

- **FR-006** : Spec dit « indicateur de chargement sur l'écran de capture, sans afficher l'écran de résultat avant l'état terminal ». Le code navigue **immédiatement** vers `LlmResultScreen` au démarrage de l'analyse (`CameraViewModel.kt:455-456` : `navigateToResultScreen()` appelé avant la réponse LLM). Le loading/streaming s'affiche sur l'écran résultat, pas sur l'écran capture.
  - Location : `CameraViewModel.kt:455-456`
  - Severity : **major** (drift architectural — même écart pour FR-007, FR-013, FR-014)

- **FR-007** : Spec dit « navigation vers écran résultat uniquement lorsque l'analyse se termine avec un résultat exploitable ». Le code émet `navigateToResultScreen()` dès le début de l'analyse, pas après succès.
  - Location : `CameraViewModel.kt:249-256`
  - Severity : **major** (couplé à FR-006)

- **FR-013** : Spec dit « test LLM suit même règle de chargement que FR-006 ». `runCameraTabLlmMockTest()` navigue aussi immédiatement vers le résultat (`CameraViewModel.kt:208-212`). Les deux parcours sont **cohérents entre eux** mais dérivent identiquement de la spec.
  - Location : `CameraViewModel.kt:208-212`
  - Severity : **major** (même drift architectural)

- **FR-014** : Le flag `_captureRouteActive` existe et est tracé via `DisposableEffect` (`CameraScreen.kt:68-71`), mais la navigation vers le résultat se fait **avant** l'analyse. L'utilisatrice n'est jamais « sur l'écran capture pendant le chargement » — elle est déjà sur le résultat. La garde est rendue inopérante par le flux de navigation immédiate.
  - Location : `CameraScreen.kt:68-71`, `CameraViewModel.kt:455`
  - Severity : **major** (conséquence du drift FR-006)

- **FR-017** : La navigation vers `LlmResultScreen` se fait quand `streamingBilan` est en état `Streaming()` (contenu vide initial). L'état `Idle` affiche un header d'attente (fouet + phrases) — l'écran n'est jamais vide visuellement, mais il est affiché **avant** qu'un contenu exploitable soit prêt.
  - Location : `LlmResultScreen.kt:88-90`
  - Severity : **moderate**

- **FR-018** : `buildLlmResultFallbackPayload()` existe (`CameraViewModel.kt:268-283`) et construit un repli avec le texte OCR, mais cette méthode **n'est jamais appelée** dans le flux `LlmResultScreen`. Le fallback OCR n'est accessible que depuis l'écran capture (boutons "Voir le texte brut" dans `GemmaUnavailable` et `CompositionLimit`).
  - Location : `CameraViewModel.kt:268-283`
  - Severity : **moderate**

#### Not Implemented ✗

_Aucune exigence totalement absente_ — les dérives identifiées sont des implémentations partielles ou architecturalement différentes.

---

### Spec: user-guidance-experience — llm-download-onboarding

**Fichier spec** : `specs/domains/user-guidance-experience/spec-llm-download-onboarding.md`
**Exigences** : FR-001 → FR-016

#### Aligned ✓

- **FR-001** : Détection absence modèle LLM → `ModelDownloadViewModel.checkModelPresence()` vérifie au `init`
- **FR-003** : Détection type connexion réseau → `NetworkTypeDetector.kt` distingue WIFI / MOBILE_DATA / OFFLINE via `ConnectivityManager`
- **FR-004** : Blocage du téléchargement sans confirmation → state machine `ConfirmationRequired` → `confirmDownload()` → `startDownload()`
- **FR-005** : Redirection vers écran d'attente après confirmation → Navigation `OnboardingRoutes.Downloading` (`MainActivity.kt:217-220`)
- **FR-006** : Titre permanent "Téléchargement du modèle de langage en cours..." → `ModelDownloadWaitingScreen.kt:67-69`
- **FR-007** : Phrases humoristiques en rotation → `WAITING_PHRASES.shuffled()`, rotation 5 s, `AnimatedContent` fade
- **FR-009** : Redirection auto vers écran principal après succès → `Ready` → `CameraFlowRoutes.Capture` avec `popUpTo(0)` (`MainActivity.kt:202-208`)
- **FR-010** : Message d'erreur avec action de récupération → `Error(canRetry = true)` + bouton "Réessayer"
- **FR-012** : Pas de téléchargements concurrents → `AtomicBoolean.compareAndSet` (`ModelDownloadViewModel.kt:74-75`)
- **FR-014** (SHOULD) : Vérification espace disque → `hasEnoughDiskSpace()` vérifie 3 Go libres via `StatFs` (`ModelDownloadViewModel.kt:111-119`)
- **FR-015** : Barre de progression avec pourcentage → `AnimatedMarmite` affiche `${(animatedProgress * 100).toInt()}%` avec testTag `download_progress_percent`
- **FR-016** : Écran "Connexion requise" hors-ligne → `NetworkOfflineScreen.kt` avec titre, explication, bouton "Réessayer"

#### Drifted ⚠️

- **FR-002** : Spec demande écran plein avec boutons "Confirmer / **Plus tard**". Le code affiche "**Refuser et quitter**" au lieu de "Plus tard".
  - Location : `ModelDownloadOnboardingScreen.kt:119`
  - Severity : **moderate**

- **FR-008** : Spec demande « fouet mixeur animé (même animation que l'écran de streaming analyse) ». Le code utilise `AnimatedMarmite` (marmite se remplissant) pendant le téléchargement, pas `AnimatedWhisk`. L'animation est plus informative (progression intégrée) mais ne correspond pas à la spec.
  - Location : `ModelDownloadWaitingScreen.kt:98-103`
  - Severity : **minor** (amélioration fonctionnelle mais écart de spec)

- **FR-011** : Spec exige « afficher un état explicatif de l'impossibilité d'utiliser l'application sans le modèle, avec possibilité de relancer plus tard ». Le code appelle `finishAffinity()` (fermeture immédiate). L'état `LlmModelReadinessState.Declined` existe dans le ViewModel mais n'est **jamais exploité** dans l'UI.
  - Location : `MainActivity.kt:241`
  - Severity : **moderate**

- **FR-013** (SHOULD) : L'UI affiche "Reprendre le téléchargement" quand `isResumable` est vrai, mais le downloader **supprime le fichier partiel** et recommence à zéro (`GemmaModelDownloader.kt:39-41` : `if (temp.exists()) temp.delete()`). Pas de requête HTTP `Range` pour reprise partielle.
  - Location : `GemmaModelDownloader.kt:39-41`
  - Severity : **moderate** (fausse reprise — l'UI ment à l'utilisatrice)

---

### Spec: ingredient-normalization-validation — ingredient-phrase-segment

**Fichier spec** : `specs/domains/ingredient-normalization-validation/spec.md`
**Exigences** : FR-001 → FR-009

#### Aligned ✓

- **FR-003** : Fin au `.` suivi d'espace/newline, `.` interne ne termine pas, `!`/`?` inconditionnels → `IngredientSegmentBoundaryResolver.kt:16-39` avec couverture tests complète (BC-01 à BC-07)
- **FR-005** : Si aucun terminateur → fin du texte → `BoundaryResolver` retourne `text.length` avec `TEXT_END`
- **FR-006** : Seule la première occurrence d'ancre → `Regex.find()` retourne le premier match. Testé avec fixture `FR_THEN_EN_ANCHOR`
- **FR-007** : Confirmation explicite avant analyse → Flux complet `SegmentConfirmationRequired` → boutons "Confirmer et analyser" / "Reprendre la photo" dans `CameraScreen.kt:260-281`, gate `AnalysisSubmissionGate.evaluate()` vérifie `userConfirmed`

#### Drifted ⚠️

- **FR-001/FR-004** : Comportement correct (règle unique, saut de ligne seul ne termine plus), mais les enum mortes `IngredientSegmentBoundaryEndReason.LINE_END` et `IngredientSegmentFallbackMode.NO_NEWLINE_TO_EOF` existent toujours dans `IngredientSegmentModels.kt`.
  - Location : `IngredientSegmentModels.kt:12,18`
  - Severity : **minor** (code mort, pas de comportement incorrect)

- **FR-002** : (a) La regex d'ancre exige `(?m)(^\s*)` — l'ancre doit être en début de ligne, la spec ne pose pas cette restriction. (b) La regex est **dupliquée** dans `IngredientExtractionPipeline.kt:8-9` au lieu de réutiliser `IngredientAnchorNormalizer`, créant un risque de divergence.
  - Location : `IngredientAnchorNormalizer.kt:10`, `IngredientExtractionPipeline.kt:8-9`
  - Severity : **moderate**

- **FR-008** : La partie confirmation est alignée (gate bloque sur `USER_REJECTED`). **Mais** quand l'ancre est absente, le code passe en `ScanState.Success` avec le texte brut, sans message d'erreur explicite ni voie de reprise. La spec exige un message compréhensible et une reprise.
  - Location : `CameraViewModel.kt:367-375`
  - Severity : **moderate**

- **FR-009** : Le `scanId` lie les étapes en mémoire, mais seul `ValidatedIngredientEntity` est persisté (Room). `OcrRawText` n'est jamais instancié, `IngredientSegmentExtraction` n'est pas persistée. Après une session, impossible de reconstituer la chaîne texte brut → proposition → segment validé.
  - Location : `ValidatedIngredientRepository.kt:9-18`, `IngredientSegmentModels.kt:3-7`
  - Severity : **moderate** (traçabilité incomplète en persistance)

---

### Spec: ingredient-health-intelligence — test-llm-mock-ingredients

**Fichier spec** : `specs/domains/ingredient-health-intelligence/spec.md`
**Exigences** : FR-001 → FR-011

#### Aligned ✓

- **FR-001** : Test bouchonné sans dépendance caméra/OCR → `CompositionEngineHomeLlmMockRunner` invoque directement `CompositionAnalysisEngine`
- **FR-004** : Résultat clairement succès ou échec → `HomeLlmMockOutcome.Success` / `Failure` sealed class
- **FR-006** : Exécution répétable → constante `const val`, runner sans état
- **FR-007** : Réponse exploitable = non vide + analysable → `BilanSuccess` via `GemmaBilanParser.parse()`, réponse vide classée `INVALID_RESPONSE` dans `Gemma4LocalClient.kt:44-56`
- **FR-009** : 3 catégories d'échec exactes → `TIMEOUT`, `RUNTIME_UNAVAILABLE`, `NON_ANALYSABLE_RESPONSE`

#### Drifted ⚠️

- **FR-002** : La chaîne mockée dans le code ne correspond **pas** à la spec — tous les accents/diacritiques sont supprimés.
  - **Spec** : `farine de BLÉ`, `farine complète`, `lécithines`, `LAIT écrémé`, `arômes`
  - **Code** : `farine de BLE`, `farine complete`, `lecithines`, `LAIT ecreme`, `aromes`
  - Location : `HomeLlmMockRunner.kt:63-67`
  - Severity : **major**

- **FR-003** : `AnalysisInputBuilder.buildSegmentPayload()` applique un `.trim()` — pas d'altération en pratique pour la constante actuelle, mais aucune assertion ne le vérifie. La spec exige "sans altération".
  - Location : `AnalysisInputBuilder.kt:4-6`
  - Severity : **minor**

- **FR-005** : Pas de `TestTraceRecord` — traçabilité implicite. `HomeLlmMockOutcome` ne contient pas de référence à l'input. Le `requestId` existe dans `AnalyseTextuelleRequest` mais n'est pas exposé dans le résultat final.
  - Location : `HomeLlmMockRunner.kt`
  - Severity : **moderate**

- **FR-008** : Timeout spec = **30 secondes**. Code = `Gemma4LocalConfig.DEFAULT_TIMEOUT_MS` = **180 000 ms (3 minutes)**.
  - Location : `HomeLlmMockRunner.kt:9` → `Gemma4LocalConfig.kt:4`
  - Severity : **major** (6× le timeout spécifié)

- **FR-010** : Égalité stricte caractère par caractère impossible car la chaîne mockée elle-même ne correspond pas à la spec (accents manquants). Aucune assertion de comparaison n'existe non plus.
  - Location : `HomeLlmMockRunner.kt:63-67`
  - Severity : **major** (violation dérivée de FR-002)

- **FR-011** : Le test `HomeLlmMockTriggerTest.kt` est un `@Test` JUnit standard sans annotation `@Ignore`, `@Tag("manual")` ou filtre d'exclusion. Rien ne l'exclut des suites automatiques, contrairement à la spec (« hors validation régulière »).
  - Location : `HomeLlmMockTriggerTest.kt:9-12`
  - Severity : **minor**

---

### Spec: additive-risk-insights

**Fichier spec** : `specs/domains/additive-risk-insights/spec.md`
**Exigences** : ARI-FR-001 → ARI-FR-004

#### Aligned ✓

- **ARI-FR-001** : Liste ordonnée d'additifs avec niveau de risque normalisé et justification → `AdditiveKpiParser.kt` parse, `AdditiveRiskLevel` normalise (HIGH > MEDIUM > LOW avec `sortKey`), `AdditiveJustificationFormatter` formate. Tri par `sortKey` puis nom alphabétique.
- **ARI-FR-002** : KPI globaux cohérents → `RiskSummaryKpi` avec `totalCount`, `highCount`, `mediumCount`, `lowCount`, `unknownCount`, `globalLevel`. `BuildAdditiveKpiDisplay` assemble l'affichage. UI `AdditiveKpiSummaryBar` affiche Total + répartition couleurs.
- **ARI-FR-003** : Signalement des lignes incomplètes/incohérentes → `AdditiveKpiParser` détecte lignes sans justification (`NEEDS_CONFIRMATION`) et incohérences niveau/justification (`INCOHERENT`). Warnings remontés dans `AdditiveParseOutcome.warnings`. UI affiche badges visuels.
- **ARI-FR-004** (SHOULD) : Niveau de confiance par item → `AdditiveLineConfidence` avec 4 niveaux (`OK`, `NEEDS_CONFIRMATION`, `INCOHERENT`, `DUPLICATE_MERGED`). Calculé par item dans le parser, affiché par item dans l'UI.

---

### Spec: capture-recognition

**Fichier spec** : `specs/domains/capture-recognition/spec.md`
**Exigences** : CR-FR-001 → CR-FR-005

#### Aligned ✓

- **CR-FR-001** : Aperçu caméra réel et capture par action explicite → `CameraPreviewBox` crée un `PreviewView` CameraX réel, `CameraCaptureController.bind()` attache le flux caméra arrière. Guard `canCapturePhoto()` vérifie que le preview est actif avant capture.
- **CR-FR-002** : OCR on-device, pas d'envoi distant → `LocalOcrFallbackRecognizer` utilise ML Kit `TextRecognition` (on-device). `RecognitionEngineSelector` force le fallback local systématiquement. `AiEdgeGalleryRecognizer` est un stub non utilisé en production.
- **CR-FR-003** : États utilisateurs explicites → `ScanState` sealed class exhaustif : readiness (`CameraReady`, `PreviewInitializing`, `PreviewActive`), analyse (`Capturing`, `Analyzing`, `CompositionAnalyzing`), succès (`Success`, `BilanReady`, `SegmentConfirmationRequired`), échec (`Error`, `Empty`, `GemmaUnavailable`, `CompositionLimit`, `PermissionDenied`, `CameraUnavailable`)
- **CR-FR-004** : Nettoyage artefacts temporaires → `IngredientRecognitionCoordinator.runRecognition()` supprime le fichier temporaire (`file.delete()` ligne 34). `CameraCaptureController.unbind()` appelé dans le `finally` du bloc de capture.

#### Drifted ⚠️

- **CR-FR-005** : `ScanFailureClassifier` est un classifieur **passif** — il classifie (`empty`, `blur`, `incomplete`, `low-contrast`) mais ne bloque pas directement la progression. Le blocage réel se fait dans `CameraViewModel` via la vérification `result.outcome`. Pour les cas intermédiaires (`blur`, `incomplete`), le classifieur ne bloque pas — il génère un message d'erreur et tombe dans le branch `Error` qui permet un retry.
  - Location : `ScanFailureClassifier.kt:3-9`, `CameraViewModel.kt:398-399`
  - Severity : **minor**

---

## Unspecced Code 🆕

| Feature | Location | Files | Lines | Suggested Spec |
|---------|----------|-------|-------|----------------|
| Welcome messages & tone rules | `com.miamia.welcome/` | 7 | 169 | `user-guidance-experience` |
| Health critique engine (analyse santé par population) | `com.miamia.healthcritique/` | 13 | 1 030 | `ingredient-health-intelligence` |
| Composition analysis engine (bilan + parser Gemma) | `com.miamia.composition/` | 9 | 728 | `ingredient-health-intelligence` |
| Local LLM runtime (Gemma gateway, client, downloader) | `com.miamia.gemma4local/` | 15 | 902 | `local-llm-runtime` (nouveau domaine) |
| Home screen (layout, MediaPipe status, spacing) | `com.miamia.home/` | 10 | 410 | `user-guidance-experience` |

**Total code non spécifié** : ~3 239 lignes dans 54 fichiers sources

---

## Inter-Spec Conflicts

1. **Drift architectural FR-006/007/013/014** : Les 4 FRs de `photo-capture-llm-result-flow` partagent un même écart fondamental — le flux navigue immédiatement vers l'écran résultat pour y afficher le streaming progressif, alors que la spec exige un chargement sur l'écran capture puis navigation à l'état terminal. Ce choix est cohérent en soi (streaming riche) mais diverge de la spec.

2. **Timeout incohérent** : `ingredient-health-intelligence` spec exige 30 s (FR-008) mais le code utilise `Gemma4LocalConfig.DEFAULT_TIMEOUT_MS = 180_000` partagé avec `CompositionAnalysisEngine`.

3. **Chaîne mockée vs réalité OCR** : La spec `ingredient-health-intelligence` FR-002 définit une chaîne avec accents (`BLÉ`, `complète`, `arômes`) tandis que le code utilise une version ASCII.

4. **Fausse reprise de téléchargement** : L'UI d'onboarding affiche "Reprendre le téléchargement" mais le `GemmaModelDownloader` supprime le fichier partiel et recommence à zéro.

5. **Home screen résiduel** : Le package `home/` contient un `HomeScreen.kt` et un `HomeViewModel.kt` (84 + 99 lignes) qui semblent être un vestige de l'ancienne architecture à onglets.

---

## Recommendations

### Priorité 1 — Major

1. **Décider du flux loading/streaming (FR-006/007/013/014)** — Soit mettre à jour la spec pour refléter le choix d'implémentation actuel (streaming progressif sur l'écran résultat), soit refactorer le flux pour afficher le loading sur l'écran capture conformément à la spec. Le choix actuel offre une meilleure UX mais diverge de la spec.

2. **Aligner la chaîne mockée (FR-002 ingredient-health-intelligence)** — Restaurer les diacritiques dans `MOCK_INGREDIENTS_INPUT` : `BLÉ`, `complète`, `écrémé`, `lécithines`, `à`, `arômes`.

3. **Corriger le timeout du test bouchonné (FR-008)** — Introduire `MOCK_TEST_TIMEOUT_MS = 30_000` dédié au test mock pour respecter FR-008 sans impacter les autres usages de `Gemma4LocalConfig`.

### Priorité 2 — Moderate

4. **Connecter le fallback OCR à l'écran résultat (FR-018)** — `buildLlmResultFallbackPayload()` existe mais n'est jamais appelé depuis `LlmResultScreen`. Le texte OCR devrait être accessible en cas d'erreur.

5. **Revoir le comportement "Refuser le téléchargement" (FR-011 onboarding)** — Exploiter `LlmModelReadinessState.Declined` qui existe déjà au lieu de `finishAffinity()`. Changer le label en "Plus tard" et afficher un état explicatif.

6. **Corriger la fausse reprise de téléchargement (FR-013 onboarding)** — Soit implémenter la reprise réelle (requête HTTP `Range`), soit retirer l'affichage "Reprendre" de l'UI pour ne pas mentir à l'utilisatrice.

7. **Traiter l'ancre absente comme erreur (FR-008 ingredient-normalization)** — Remplacer `ScanState.Success` par un état d'erreur explicite avec message et voie de reprise quand l'ancre est absente.

8. **Assouplir la regex d'ancre (FR-002 ingredient-normalization)** — Retirer la contrainte début de ligne `(?m)(^\s*)` et extraire la regex dupliquée de `IngredientExtractionPipeline` pour réutiliser `IngredientAnchorNormalizer`.

9. **Compléter la traçabilité persistée (FR-009 ingredient-normalization)** — Persister `OcrRawText` et `IngredientSegmentExtraction` en plus du segment validé.

### Priorité 3 — Minor

10. **Corriger l'animation onboarding (FR-008 onboarding)** — `AnimatedMarmite` ≠ `AnimatedWhisk`. Mettre à jour la spec ou l'animation pour aligner les deux.

11. **Exclure le test mock des suites automatiques (FR-011 ingredient-health-intelligence)** — Ajouter `@Tag("manual")` ou `@Ignore` à `HomeLlmMockTriggerTest`.

12. **Nettoyer le code mort** — Supprimer `LINE_END`, `NO_NEWLINE_TO_EOF` (FR-004 abrogé).

13. **Ajouter la traçabilité au test mock (FR-005 ingredient-health-intelligence)** — Enrichir `HomeLlmMockOutcome` avec l'input source et le requestId.

### Priorité stratégique

14. **Backfill specs manquantes** — `healthcritique/` (1 030 lignes), `composition/` (728 lignes) et `gemma4local/` (902 lignes) représentent le cœur fonctionnel sans spec domaine. Priorité : `ingredient-health-intelligence` (pour couvrir healthcritique + composition), puis `local-llm-runtime` (pour gemma4local).

15. **Unifier ou retirer `home/`** — Le package est un vestige à nettoyer ou migrer vers `user-guidance-experience`.
