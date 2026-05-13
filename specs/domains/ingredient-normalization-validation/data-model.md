# Data Model - ingredient-normalization-validation (017 + 021)

## Entities existantes — pas de modification structurelle

Ce changement ne crée pas de nouvelles entités ni de nouveaux champs. Les structures existantes dans `IngredientSegmentModels.kt` sont suffisantes.

### 0) OcrRawText

- **Description**: texte brut d'une capture d'étiquette OCR.
- **Fields**:
  - `scanId` (string, unique)
  - `content` (string)
  - `capturedAtEpochMs` (long)
- **Impact 017**: aucun.

### 1) IngredientSegmentExtraction

- **Description**: résultat de l'extraction du segment ingrédients.
- **Fields**:
  - `scanId` (string)
  - `anchorFound` (boolean)
  - `anchorIndex` (int, nullable)
  - `endIndex` (int, nullable)
  - `segmentText` (string, nullable)
  - `fallbackMode` (enum: `NONE`, `ANCHOR_MISSING_BLOCKED`, `NO_NEWLINE_TO_EOF`)
  - `boundaryEndReason` (enum: `NONE`, `SENTENCE_TERMINATOR`, `LINE_END`, `TEXT_END`)
- **Impact 017**: la valeur `SENTENCE_TERMINATOR` inclut désormais uniquement les `.` suivis d'un espace/newline (ou en fin de texte), et les `!` / `?` inconditionnels. Sémantique externe inchangée.

### 2) IngredientSegmentBoundaryResolver.Resolution

- **Description**: résultat intermédiaire du calcul de borne de fin.
- **Fields**:
  - `endIndexExclusive` (int)
  - `boundaryEndReason` (enum `IngredientSegmentBoundaryEndReason`)
- **Impact 017**: le contrat « `endIndexExclusive` pointe après le terminateur » est préservé. Le `.` interne ne produit plus de `Resolution` prématurée.

## Invariant révisé (FR-003)

Le `.` est un terminateur de phrase **si et seulement si** :
1. Il est suivi d'un espace (`' '`) ou d'un retour à la ligne (`'\n'`), **ou**
2. Il est le dernier caractère du texte disponible (pas de caractère après).

Les `!` et `?` restent des terminateurs inconditionnels (pas de condition contextuelle).

## State transitions

### Isolation (inchangé, 017)

Le flux `prepare()` → `resolveEnd()` → `AnalysisSubmissionGate.evaluate()` pour la **géométrie** du segment reste identique (`IngredientSegmentFallbackMode`, `SubmissionBlockedReason` pour ancre / segment vide inchangés).

### Orchestration capture → analyse (021 FR-010 + 2026-05-13 FR-012)

Nouvelle dimension : **intention de capture** (signal balise « ingrédients ») + **transcript OCR complet** comme entrée unique du LLM (plus de dépendance à `anchorFound` pour autoriser la soumission).

```text
OCR success/partial → transcriptText (SSOT)
       │
       ├─► AnalysisSubmissionGate.evaluate(..., fullOcrTranscript = transcriptText)
       │         ├─ transcript vide / label seul → Error (FR-008)
       │         ├─ USER_REJECTED → SegmentConfirmationRequired (aperçu = transcript intégral)
       │         └─ submissionAllowed
       │                 ├─► [FR-010] implicitValidation → confirmSegmentAndAnalyze() → CompositionAnalyzing
       │                 └─► (sinon implicite) confirmation utilisateur → CompositionAnalyzing
       │
       └─► runCompositionStage(rawText = transcript OCR intégral)
```

**Note** : `IngredientSegmentExtraction` reste produit pour traçabilité / vues auxiliaires ; il **ne bloque plus** seul le chemin LLM lorsque le transcript complet est exploitable.

### Objet décisionnel (extension logique)

- **`AnalysisSubmissionDecision`** : conserver `submissionAllowed`, `blockedReason`, `segmentPreview` ; documenter en implémentation comment distinguer **confirmation explicite** vs **validation implicite FR-010** (champ booléen dédié ou convention de log — à figer au moment du code sans changer les invariants « segment non vide »).

## Nouveau concept (021) — intention de session capture

| Concept | Description | Owner technique suggéré |
|---------|-------------|-------------------------|
| **IngredientsFramingTag** (nom logique) | Indique que l’utilisatrice a choisi le mode / balise « ingrédients » avant la capture, distinct du contenu OCR. | Propagé depuis UI ou `scanCoordinator` / résultat reconnaissance vers `CameraViewModel` ; référencé dans contrat [session-capture-intent-for-implicit-validation.md](./contracts/session-capture-intent-for-implicit-validation.md). |

Aucun nouveau champ persistant obligatoire pour FR-010 (FR-009 : mémoire de session suffisante MVP).
