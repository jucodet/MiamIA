# Data Model - ingredient-normalization-validation (017-ocr-dot-end-capture)

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

Pas de changement dans les transitions d'état (`IngredientSegmentFallbackMode`, `SubmissionBlockedReason`). Le flux `prepare()` → `resolveEnd()` → `AnalysisSubmissionGate.evaluate()` reste identique.
