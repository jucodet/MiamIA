# Drift Resolution Proposals

Generated: 2026-05-12T17:29:00+02:00
Based on: drift-report from 2026-05-12T17:14:00+02:00

## Summary

| Resolution Type | Count |
|-----------------|-------|
| Backfill (Code → Spec) | 7 |
| Align (Spec → Code) | 6 |
| New Specs | 3 |
| Remove from Spec | 0 |
| Human Decision | 0 (all resolved interactively) |

---

## Proposals

---

### Proposal 1: photo-capture-llm-result-flow / FR-006, FR-007, FR-013, FR-014

**Direction**: BACKFILL ✅ Approved

**Current State**:
- Spec says: "Indicateur de chargement sur l'écran de capture ; navigation vers l'écran résultat uniquement après état terminal."
- Code does: "Navigation immédiate vers LlmResultScreen au démarrage de l'analyse. Streaming progressif (produit, ingrédients, synthèse, impacts) sur l'écran résultat."

**Proposed Resolution**:

Mettre à jour les 4 FRs dans `specs/domains/user-guidance-experience/spec.md` :

- **FR-006 (nouveau)** : Le système MUST, après capture photo et lancement de l'analyse LLM, naviguer vers l'écran de résultat dédié et y afficher un indicateur de chargement (fouet animé, phrases humoristiques rotatives) suivi du streaming progressif des sections d'analyse (produit identifié, ingrédients, synthèse, impacts santé) au fur et à mesure de leur disponibilité.
- **FR-007 (nouveau)** : Le système MUST, lorsque l'analyse LLM se termine avec succès et que l'utilisatrice est sur l'écran de résultat, afficher le bilan complet (carte résultat avec toutes les sections). En cas d'échec, afficher un état d'erreur actionnable sur le même écran.
- **FR-013 (nouveau)** : Le système MUST, pour le parcours déclenché par le bouton test LLM, appliquer le même flux que FR-006 : navigation immédiate vers l'écran de résultat avec streaming progressif.
- **FR-014 (nouveau)** : Le système MUST, si l'utilisatrice quitte l'écran de résultat pendant le streaming (retour arrière), ne pas la ramener automatiquement vers cet écran lorsque le traitement se termine. Le streaming continue en arrière-plan mais aucune navigation automatique n'est imposée.

**Rationale**: Le streaming progressif sur l'écran résultat est une meilleure UX — l'utilisatrice voit les résultats apparaître en temps réel au lieu d'attendre sur un écran statique. Ce choix d'implémentation est intentionnel et testé.

**Confidence**: HIGH

---

### Proposal 2: photo-capture-llm-result-flow / FR-017, FR-018

**Direction**: BACKFILL FR-017 + ALIGN FR-018 ✅ Approved

**Current State**:
- FR-017 spec: "Interdire l'affichage d'un écran résultat vide."
- FR-017 code: L'état `Streaming()` initial affiche un header d'attente (fouet + phrases).
- FR-018 spec: "Afficher un état de repli utile avec le texte OCR."
- FR-018 code: `buildLlmResultFallbackPayload()` existe mais n'est jamais appelé depuis `LlmResultScreen`.

**Proposed Resolution**:

- **FR-017 (backfill)** : Le système MUST, sur l'écran de résultat, afficher un état d'attente visuel (animation + phrases rotatives) lorsque le streaming d'analyse est en cours et qu'aucune section n'est encore disponible. Cet état d'attente constitue un contenu non vide au sens de cette exigence.

- **FR-018 (align)** : Connecter `buildLlmResultFallbackPayload()` au flux `LlmResultScreen`. Quand `StreamingBilanState.Error` est affiché, inclure le texte OCR brut comme contenu de repli (si disponible) afin que l'utilisatrice puisse au minimum consulter le texte reconnu.

  - **Fichiers à modifier** : `LlmResultScreen.kt` (ErrorContent), `CameraViewModel.kt` (exposer rawTranscript au streaming state)

**Rationale**: FR-017 — le header d'attente est un feedback, pas un écran vide. FR-018 — le code de fallback existe, il suffit de le brancher.

**Confidence**: HIGH

---

### Proposal 3: llm-download-onboarding / FR-002, FR-011

**Direction**: BACKFILL ✅ Approved

**Current State**:
- Spec says: "Boutons 'Confirmer / Plus tard'" + "afficher un état explicatif avec possibilité de relancer plus tard"
- Code does: Bouton "Refuser et quitter" → `finishAffinity()`

**Proposed Resolution**:

Mettre à jour dans `specs/domains/user-guidance-experience/spec-llm-download-onboarding.md` :

- **FR-002 (nouveau)** : Le système MUST afficher un écran plein dédié informant de la taille du téléchargement, du type de connexion détecté, et proposant les actions "Confirmer" et "Refuser et quitter".
- **FR-011 (nouveau)** : Le système MUST, lorsque l'utilisatrice décline le téléchargement, fermer l'application proprement. Le modèle est obligatoire pour le fonctionnement de l'application ; aucun état dégradé sans modèle n'est supporté.

**Rationale**: Le modèle LLM est obligatoire. Garder l'utilisatrice dans l'app sans modèle n'apporte aucune valeur. "Refuser et quitter" est le comportement le plus honnête.

**Confidence**: HIGH

---

### Proposal 4: llm-download-onboarding / FR-008

**Direction**: BACKFILL ✅ Approved

**Current State**:
- Spec says: "fouet mixeur animé (même animation que l'écran de streaming analyse)"
- Code does: `AnimatedMarmite` avec progression intégrée visuellement

**Proposed Resolution**:

- **FR-008 (nouveau)** : Le système MUST afficher une animation de marmite se remplissant (`AnimatedMarmite`) intégrant visuellement la progression du téléchargement, comme indicateur de processus actif. Cette animation est distincte du fouet animé de l'écran de streaming d'analyse, car elle intègre la progression connue du téléchargement.

**Rationale**: Le fouet convient au streaming (durée indéterminée). La marmite avec remplissage progressif est plus informative pour un téléchargement dont la progression est connue. Amélioration UX intentionnelle.

**Confidence**: HIGH

---

### Proposal 5: llm-download-onboarding / FR-013

**Direction**: ALIGN ✅ Approved

**Current State**:
- Spec says: "Le système SHOULD supporter la reprise du téléchargement après interruption."
- Code does: L'UI affiche "Reprendre" mais `GemmaModelDownloader` supprime le fichier partiel et recommence à zéro.

**Proposed Resolution**:

Implémenter la reprise réelle du téléchargement dans `GemmaModelDownloader.kt` :

1. Ne pas supprimer le fichier `.downloading` existant
2. Ajouter un header HTTP `Range: bytes=<file_size>-` à la requête
3. Ouvrir le fichier en mode append au lieu d'écraser
4. Gérer le cas où le serveur ne supporte pas `Range` (recommencer à zéro avec log)
5. Mettre à jour la progression initiale à partir de la taille du fichier partiel

- **Fichiers à modifier** : `GemmaModelDownloader.kt`, `ModelDownloadViewModel.kt` (initialiser la progression partielle)

**Rationale**: L'UI promet la reprise — le code doit honorer cette promesse. Un fichier de 2.6 Go sur mobile justifie pleinement la reprise.

**Confidence**: HIGH

---

### Proposal 6: ingredient-health-intelligence / FR-002, FR-010

**Direction**: ALIGN ✅ Approved

**Current State**:
- Spec says: Chaîne mockée avec diacritiques (BLÉ, complète, lécithines, écrémé, arômes)
- Code does: Version ASCII sans accents (BLE, complete, lecithines, ecreme, aromes)

**Proposed Resolution**:

Modifier `HomeLlmMockRunner.kt:63-67` :

```kotlin
const val MOCK_INGREDIENTS_INPUT: String =
    "Ingredients. Sucre, farine de BLÉ 33 %, farine complète de BLÉ 15 %, huile de palme, " +
        "huile de colza, amidon de BLÉ, sirop de glucose, poudres à lever (carbonates d'ammonium, " +
        "carbonates de sodium), émulsifiant (lécithines de SOJA), sel, LAIT écrémé en poudre, " +
        "LAIT entier en poudre, arômes."
```

**Rationale**: La spec est la source de vérité pour la chaîne de test. Les diacritiques sont réalistes pour une étiquette alimentaire française. Tester sans accents ne valide pas le cas d'usage réel.

**Confidence**: HIGH

---

### Proposal 7: ingredient-health-intelligence / FR-008

**Direction**: BACKFILL ✅ Approved

**Current State**:
- Spec says: "Échec automatique si aucune réponse dans 30 secondes."
- Code does: Timeout = 180 000 ms (3 minutes)

**Proposed Resolution**:

Mettre à jour FR-008 dans `specs/domains/ingredient-health-intelligence/spec.md` :

- **FR-008 (nouveau)** : Le système MUST échouer automatiquement le test si aucune réponse exploitable n'est obtenue dans une fenêtre de **180 secondes** (timeout strict). Ce délai tient compte du cold start du modèle Gemma local et des variations de performance selon la classe d'appareil.

Mettre à jour SC-002 et SC-006 en conséquence (remplacer 30 s par 180 s).

**Rationale**: Le modèle Gemma local peut légitimement mettre plus de 30 s sur un appareil modeste (cold start + chargement du modèle + décodage). 30 s est irréaliste en conditions réelles.

**Confidence**: HIGH

---

### Proposal 8: ingredient-health-intelligence / FR-003, FR-005, FR-011

**Direction**: ALIGN ✅ Approved

**Current State**:
- FR-003: `trim()` appliqué sans assertion de non-altération
- FR-005: Pas de `TestTraceRecord`, traçabilité implicite
- FR-011: Test inclus dans les suites automatiques sans exclusion

**Proposed Resolution**:

1. **FR-003** : Ajouter dans `HomeLlmMockTriggerTest.kt` un test :
   ```kotlin
   @Test fun mockPayloadPreservesInput() {
       val payload = AnalysisInputBuilder.buildSegmentPayload(MOCK_INGREDIENTS_INPUT)
       assertEquals(MOCK_INGREDIENTS_INPUT.trim(), payload)
   }
   ```

2. **FR-005** : Enrichir `HomeLlmMockOutcome` :
   ```kotlin
   sealed class HomeLlmMockOutcome {
       abstract val inputUsed: String
       data class Success(val responseText: String, override val inputUsed: String) : HomeLlmMockOutcome()
       data class Failure(val category: HomeLlmFailureCategory, val message: String, override val inputUsed: String) : HomeLlmMockOutcome()
   }
   ```

3. **FR-011** : Ajouter `@Tag("manual")` à `HomeLlmMockTriggerTest` et configurer Gradle pour exclure ce tag des suites automatiques.

- **Fichiers à modifier** : `HomeLlmMockTriggerTest.kt`, `HomeViewModel.kt`, `HomeLlmMockRunner.kt`

**Rationale**: Corrections simples, la spec a raison sur ces trois points.

**Confidence**: HIGH

---

### Proposal 9: ingredient-normalization-validation / FR-001, FR-004

**Direction**: ALIGN ✅ Approved

**Current State**:
- Enum mortes `LINE_END` et `NO_NEWLINE_TO_EOF` dans `IngredientSegmentModels.kt`

**Proposed Resolution**:

Supprimer les deux valeurs d'enum :
- `IngredientSegmentBoundaryEndReason.LINE_END`
- `IngredientSegmentFallbackMode.NO_NEWLINE_TO_EOF`

Vérifier qu'aucun test ne les référence (les supprimer le cas échéant).

- **Fichiers à modifier** : `IngredientSegmentModels.kt`, éventuellement des tests

**Rationale**: Code mort issu de l'ancienne règle FR-004 abrogée. Aucun appelant en production.

**Confidence**: HIGH

---

### Proposal 10: ingredient-normalization-validation / FR-002

**Direction**: ALIGN ✅ Approved

**Current State**:
- Regex exige début de ligne `(?m)(^\s*)`
- Regex dupliquée dans `IngredientExtractionPipeline.kt`

**Proposed Resolution**:

1. **Retirer la contrainte début de ligne** dans `IngredientAnchorNormalizer.kt` :
   ```kotlin
   private val anchorRegex =
       Regex("""(?i)\b(ingr[ée]dients?|ingredients?)\b""")
   ```

2. **Supprimer la regex dupliquée** dans `IngredientExtractionPipeline.kt` et réutiliser `IngredientAnchorNormalizer.findFirstPhraseAnchorIndex()`.

3. **Mettre à jour les tests** pour couvrir le cas d'ancre en milieu de ligne.

- **Fichiers à modifier** : `IngredientAnchorNormalizer.kt`, `IngredientExtractionPipeline.kt`, `IngredientAnchorContractTest.kt`

**Rationale**: La spec dit "première occurrence dans l'ordre de lecture du texte" sans restriction de position. La duplication crée un risque de divergence.

**Confidence**: MEDIUM — tester avec des données OCR réelles pour vérifier l'absence de faux positifs.

---

### Proposal 11: ingredient-normalization-validation / FR-008

**Direction**: ALIGN ✅ Approved

**Current State**:
- Ancre absente → `ScanState.Success(transcriptText)` sans message d'erreur

**Proposed Resolution**:

Remplacer dans `CameraViewModel.kt:367-375` :

```kotlin
if (!extraction.anchorFound) {
    lastRawTranscript = transcriptText
    lastItemsPreview = itemLabels
    _scanState.value = ScanState.Empty(
        message = "Aucune liste d'ingrédients détectée dans le texte reconnu. " +
            "Vérifiez que l'étiquette contient la mention « Ingrédients »."
    )
    return@launch
}
```

L'état `ScanState.Empty` affiche déjà un message + bouton "Nouveau scan" dans `CameraScreen`, ce qui fournit la voie de reprise exigée par la spec.

- **Fichiers à modifier** : `CameraViewModel.kt`

**Rationale**: L'utilisatrice voit du texte brut sans comprendre pourquoi l'analyse ne se lance pas. Un message explicite avec reprise est essentiel.

**Confidence**: HIGH

---

### Proposal 12: ingredient-normalization-validation / FR-009

**Direction**: BACKFILL ✅ Approved

**Current State**:
- Seul `ValidatedIngredientEntity` est persisté. `OcrRawText` et `IngredientSegmentExtraction` sont des data classes en mémoire.

**Proposed Resolution**:

Mettre à jour FR-009 dans `specs/domains/ingredient-normalization-validation/spec.md` :

- **FR-009 (nouveau)** : Le système MUST conserver, au minimum en mémoire pendant la session active, la traçabilité entre le texte brut fourni pour l'étiquette (`scanId`), la proposition isolée et le segment validé après confirmation. La persistance au-delà de la session est une évolution souhaitable mais non bloquante pour le MVP.

**Rationale**: La traçabilité en mémoire via `scanId` est suffisante pour le MVP. La persistance complète (Room) ajouterait de la complexité (nouvelles entités, DAO, migration DB) sans valeur immédiate pour l'utilisatrice.

**Confidence**: HIGH

---

### Proposal 13: capture-recognition / CR-FR-005

**Direction**: BACKFILL ✅ Approved (aucune modification)

**Current State**:
- `ScanFailureClassifier` est passif, le blocage est dans `CameraViewModel`

**Proposed Resolution**:

Aucune modification nécessaire. Le comportement est correct — la progression est effectivement refusée pour les cas vides/inexploitables. La spec ne prescrit pas l'architecture interne.

Reclassifier ce drift comme **aligné** dans le prochain rapport.

**Confidence**: HIGH

---

### Proposal 14: NEW_SPEC — healthcritique/ + composition/

**Direction**: NEW_SPEC ✅ Approved

**Feature**: Analyse de composition + critique santé par population
**Location**: `com.miamia.healthcritique/` (1 030 lignes) + `com.miamia.composition/` (728 lignes)
**Total**: 1 758 lignes, 22 fichiers

**Draft Spec**: `specs/domains/ingredient-health-intelligence/spec-composition-health-critique.md`

Spec à générer via `/speckit-sync-backfill` couvrant :

- US1 : Analyse de composition (bilan ingrédients via Gemma local)
- US2 : Critique santé par population (enfants, femmes enceintes, adultes, personnes âgées)
- US3 : Gestion des erreurs et des limites (Gemma indisponible, timeout, réponse non analysable)
- US4 : Persistance et consultation du dernier résultat
- US5 : Copie et partage des résultats

FRs à extraire du code : prompt builder, section parser, input validator, clipboard, last analysis store, composition models, bilan parser, result validator.

**Confidence**: HIGH — code testé et stable, valeur métier critique.

---

### Proposal 15: NEW_SPEC — gemma4local/

**Direction**: NEW_SPEC ✅ Approved

**Feature**: Runtime LLM local (Gemma gateway, client, downloader)
**Location**: `com.miamia.gemma4local/` (902 lignes, 15 fichiers)

**Draft Spec**: `specs/domains/local-llm-runtime/spec.md`

Spec à générer via `/speckit-sync-backfill` couvrant :

- US1 : Disponibilité et chargement du modèle Gemma local
- US2 : Exécution d'inférence locale (requête → réponse)
- US3 : Gestion des erreurs runtime (modèle absent, échec chargement, timeout)
- US4 : Métriques et observabilité (latence, classe d'appareil)
- US5 : Import et téléchargement du modèle

FRs à extraire : `Gemma4LocalClient`, `HybridGemma4LocalGateway`, `Gemma4LocalAvailabilityChecker`, `GemmaModelDownloader`, `Gemma4LocalErrorMapper`, `DeviceClassResolver`.

**Confidence**: MEDIUM — infrastructure technique, priorité moindre que la logique métier.

---

### Proposal 16: NEW_SPEC — welcome/ + home/

**Direction**: BACKFILL dans user-guidance-experience ✅ Approved

**Feature**: Messages de bienvenue + écran d'accueil
**Location**: `com.miamia.welcome/` (169 lignes) + `com.miamia.home/` (410 lignes)

**Proposed Resolution**:

Ajouter des user stories dans `specs/domains/user-guidance-experience/spec.md` :

- **US Welcome** : Message de bienvenue aléatoire au lancement (catalogue, ton positif, gestion catalogue vide)
- **US Home Legacy** : Évaluer et nettoyer le code `home/` — si l'écran de capture est l'accueil (FR-001), le `HomeScreen` est un vestige à retirer ou migrer.

Spec à générer via `/speckit-sync-backfill` pour le welcome, et via une tâche de nettoyage pour le home.

**Confidence**: MEDIUM

---

## Action Plan

### Phase 1 — Backfills de spec (aucun changement de code)

| # | Action | Fichier |
|---|--------|---------|
| P1 | Réécrire FR-006/007/013/014 pour streaming progressif | `spec.md` (user-guidance-experience) |
| P2a | Réécrire FR-017 pour état d'attente streaming | `spec.md` |
| P3 | Réécrire FR-002/011 pour "Refuser et quitter" | `spec-llm-download-onboarding.md` |
| P4 | Réécrire FR-008 pour AnimatedMarmite | `spec-llm-download-onboarding.md` |
| P7 | Réécrire FR-008 timeout → 180 s | `spec.md` (ingredient-health-intelligence) |
| P12 | Réécrire FR-009 traçabilité mémoire | `spec.md` (ingredient-normalization) |
| P13 | Reclassifier CR-FR-005 comme aligné | `drift-report.json` |

### Phase 2 — Alignements de code (changements mineurs)

| # | Action | Fichier(s) |
|---|--------|------------|
| P6 | Restaurer diacritiques MOCK_INGREDIENTS_INPUT | `HomeLlmMockRunner.kt` |
| P8 | Assertion trim + inputUsed + @Tag manual | `HomeLlmMockTriggerTest.kt`, `HomeViewModel.kt` |
| P9 | Supprimer enum mortes | `IngredientSegmentModels.kt` |
| P11 | Ancre absente → ScanState.Empty | `CameraViewModel.kt` |

### Phase 3 — Alignements de code (changements modérés)

| # | Action | Fichier(s) |
|---|--------|------------|
| P2b | Connecter fallback OCR à LlmResultScreen | `LlmResultScreen.kt`, `CameraViewModel.kt` |
| P5 | Implémenter vraie reprise HTTP Range | `GemmaModelDownloader.kt` |
| P10 | Refactorer regex ancre + supprimer duplication | `IngredientAnchorNormalizer.kt`, `IngredientExtractionPipeline.kt` |

### Phase 4 — Nouvelles specs (backfill depuis code)

| # | Action | Domaine |
|---|--------|---------|
| P14 | Backfill spec composition + health critique | `ingredient-health-intelligence` |
| P15 | Créer domaine + spec local-llm-runtime | `local-llm-runtime` |
| P16 | Backfill welcome + nettoyage home | `user-guidance-experience` |
