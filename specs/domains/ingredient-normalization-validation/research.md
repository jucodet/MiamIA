# Research - ingredient-normalization-validation (017-ocr-dot-end-capture)

## Decision 1: Le point comme terminateur contextuel (`.` + espace/newline)

- **Decision**: Modifier `IngredientSegmentBoundaryResolver.resolveEnd()` pour que `.` ne soit terminateur que s'il est suivi d'un espace `' '` ou d'un retour à la ligne `'\n'` (ou s'il est le dernier caractère de la ligne/texte). Les `!` et `?` restent des terminateurs inconditionnels.
- **Rationale**: Les étiquettes alimentaires contiennent fréquemment des points internes non terminaux : codes additifs (E.621, E.330), abréviations (vit.B12, conc.min), numéros de lot (L.12345). Traiter le `.` inconditionnellement coupe la liste prématurément sur ces cas réels. La condition contextuelle « suivi d'espace ou newline » distingue de façon fiable le point de fin de phrase (toujours suivi d'un espace ou d'un changement de paragraphe dans un texte OCR correctement reconnu) du point interne (collé au caractère suivant).
- **Alternatives considered**:
  - Regex négative lookbehind pour exclure les codes E.xxx → rejeté (trop spécifique, ne couvre pas toutes les formes).
  - Ignorer le point entièrement et ne garder que `!` / `?` → rejeté (le point de fin de phrase est le cas le plus fréquent sur les étiquettes françaises).
  - Exiger un double-espace ou double-newline → rejeté (trop restrictif, casse les cas valides).

## Decision 2: Pas de modification du modèle de données

- **Decision**: L'enum `IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR` reste inchangé. La sémantique « terminateur de phrase trouvé » couvre aussi bien le `.` contextuel que `!` ou `?`.
- **Rationale**: Ajouter un sous-type (`DOT_CONTEXTUAL` vs `UNCONDITIONAL_PUNCT`) n'apporte pas de valeur observable par l'utilisatrice ni par les consommateurs aval. Le contrat externe ne change pas.
- **Alternatives considered**:
  - Ajouter `DOT_SPACE_TERMINATOR` comme nouvelle valeur d'enum → rejeté (complexité de modèle sans bénéfice UX ou test).

## Decision 3: Le point en fin absolue de texte

- **Decision**: Un `.` en dernière position du texte (i.e. `text[text.length - 1] == '.'`) est considéré comme terminateur valide même sans espace/newline après, puisqu'il n'y a plus de caractère à examiner. Le texte se termine naturellement là.
- **Rationale**: Cas courant d'OCR où le texte est exactement « Ingrédients: eau, sel. » sans newline final. Le `.` terminal marque bien la fin de phrase.
- **Alternatives considered**:
  - Toujours exiger un caractère suivant → rejeté (casse le cas courant du point final).

## Decision 4: Fixtures de test enrichies

- **Decision**: Ajouter dans `OcrFixtures` au minimum les cas suivants :
  - `DOT_INTERNAL_ADDITIVE` : « Ingrédients: eau, colorant E.621, sucre, sel » (pas de terminaison au `.` interne).
  - `DOT_INTERNAL_ABBREVIATION` : « Ingredients: vit.B12, iron, zinc\nNext » (pas de terminaison au `.` interne, fin de ligne).
  - `DOT_SPACE_END` : « Ingrédients: eau, sucre. Traces de lait. » (terminaison au premier `. ` après ancre).
  - `DOT_NEWLINE_END` : « Ingrédients: eau, sucre.\nTraces de lait. » (terminaison au `.\n`).
  - `DOT_EOF_END` : « Ingrédients: eau, sucre. » (terminaison au `.` final, rien après).
- **Rationale**: Couvrir SC-001 (au moins un cas de point interne non suivi d'espace) et les variantes de terminaison contextuelle.
- **Alternatives considered**:
  - Ne tester que les cas existants → rejeté (SC-001 exige explicitement un cas de point interne).

## Decision 5: Pas d'impact sur les couches aval

- **Decision**: `IngredientSegmentPreparationService`, `AnalysisSubmissionGate`, `IngredientAnchorNormalizer`, et les écrans UI ne sont pas modifiés **pour le périmètre 017 seul** (borne `.` contextuel).
- **Rationale**: Le changement 017 est encapsulé dans `IngredientSegmentBoundaryResolver.resolveEnd()`. Le contrat de sortie (`Resolution` avec `endIndexExclusive` et `boundaryEndReason`) reste identique. Les tests aval existants continuent de passer.
- **Alternatives considered**:
  - Refactorer aussi `IngredientAnchorNormalizer` pour une regex combinée ancre + fin → rejeté (mélange des responsabilités, complexité accrue).

---

## Évolution 021 — auto-analyze-ingredients-tag (FR-010)

### Decision 6: Signal « balise ingrédients » indépendant du texte OCR

- **Decision**: Introduire (ou réutiliser si déjà présent côté produit) un indicateur de session **explicite** — par ex. `ingredientsFramingTagActive: Boolean` ou enum `CaptureFramingIntent` — porté depuis l’UI / coordinator de capture jusqu’à `CameraViewModel`, **sans** inférer FR-010 uniquement à partir de la présence d’une ancre dans le transcript.
- **Rationale**: La spec (US2 scénario 3, FR-011) distingue parcours avec balise et parcours étiquette entière ; se baser sur le seul texte violerait le périmètre et ouvrirait l’analyse automatique sur des scans non intentionnels.
- **Alternatives considered**:
  - Déduire le parcours accéléré si `anchorFound` → rejeté (trop large, conflit avec FR-007).

### Decision 7: Extension contrôlée de `AnalysisSubmissionGate`

- **Decision**: Étendre `evaluate(...)` (ou ajouter une surcharge documentée) pour accepter une condition du type **« validation implicite autorisée »** lorsque le signal balise est actif **et** que les garde-fous existants (ancre, segment non vide, pas label-seul) passent ; dans ce cas retourner `submissionAllowed = true` avec un marqueur traçable (`userConfirmed` / flag dédié dans `AnalysisSubmissionDecision` selon choix d’implémentation minimal).
- **Rationale**: Aujourd’hui `userConfirmed = false` entraîne systématiquement `USER_REJECTED` ; FR-010 exige un chemin équivalent fonctionnel à la confirmation sans écran intermédiaire. Centraliser la règle dans le gate évite de court-circuiter les validations « segment vide ».
- **Alternatives considered**:
  - Appeler `evaluate(..., userConfirmed = true)` depuis le ViewModel sans changer le gate → rejeté (perte de sémantique et de traçabilité dans les logs / décisions).

### Decision 8: Réutiliser la pipeline `confirmSegmentAndAnalyze`

- **Decision**: Après décision « soumission autorisée » en mode implicite, invoquer la même séquence que `confirmSegmentAndAnalyze()` (passage à `CompositionAnalyzing`, `runCompositionStage`, navigation résultat) **sans** émettre `ScanState.SegmentConfirmationRequired`.
- **Rationale**: Une seule voie d’analyse composition limite les divergences et satisfait US2b (« texte analysé = proposition automatique »).
- **Alternatives considered**:
  - Dupliquer l’appel `engine.analyze` dans `capturePhoto` → rejeté (maintenance, risque de dérive).

### Decision 9: Tests et non-régression SC-003 / SC-005

- **Decision**: Étendre `AnalysisSubmissionGateContractTest` (et tests ViewModel si présents) avec cas : balise active + segment valide → autorisé ; balise inactive + `userConfirmed=false` → toujours bloqué ; balise active + segment vide → bloqué (FR-008). Ajouter au minimum un test instrumenté ou robolectric ciblant l’absence d’UI de confirmation sur le chemin FR-010 lorsque le signal est activé en test.
- **Rationale**: SC-005 exige 100 % d’enchaînement sans écran ; SC-003 exige que les autres parcours restent bloqués sans confirmation.
- **Alternatives considered**:
  - Seuls tests unitaires gate sans UI → insuffisant pour garantir absence d’écran ; combiner les deux niveaux.

---

## Évolution 2026-05-13 — OCR intégral → LLM (FR-012, FR-014)

### Decision 10: Garde-fou sur le transcript OCR complet

- **Decision**: Étendre `AnalysisSubmissionGate.evaluate(..., fullOcrTranscript: String)` pour que la vacuité et le cas « label ingrédients seul » soient évalués sur `fullOcrTranscript.trim()`, et **retirer** le refus systématique lorsque `!extraction.anchorFound`. Le champ `segmentPreview` de `AnalysisSubmissionDecision` transporte le texte montré avant confirmation ; il est aligné sur le transcript complet (trim) pour cohérence avec l’entrée LLM.
- **Rationale**: La spec (FR-008, FR-012) distingue absence d’ancre (autorisée pour LLM si le transcript est non vide) et transcript vide / non exploitable.
- **Alternatives considered**:
  - Deuxième gate dédié « LlmInputGate » → rejeté (duplication des règles USER_REJECTED / implicite).

### Decision 11: Orchestration caméra

- **Decision**: Dans `CameraViewModel.capturePhoto`, supprimer le court-circuit `Success` sur `!anchorFound` ; toujours appeler le gate avec le transcript ; en cas `USER_REJECTED`, afficher `SegmentConfirmationRequired` avec `segmentPreview = decision.segmentPreview` (transcript intégral). `confirmSegmentAndAnalyze` appelle `prepare(scanId, lastRawTranscript!!)` et `runCompositionStage(engine, texteComplet, …)`.
- **Rationale**: Un seul SSOT (`lastRawTranscript`) pour OCR sessionnel ; alignement SC-006.
- **Alternatives considered**:
  - Conserver deux payloads (segment + brut) dans l’état Compose → rejeté (risque de dérive entre ce qui est affiché et ce qui est inféré).

### Decision 12: Post-validation composition

- **Decision**: Conserver `CompositionResultValidator.validateAgainstSource(bilan, segmentText, …)` en passant le **même** texte que celui envoyé au LLM (transcript complet). L’ancrage v1 des lignes ingrédients reste valide sur une chaîne plus large.
- **Rationale**: Pas de nouvelle variante de validateur ; les ingrédients extraits doivent rester ancrés dans le texte effectivement analysé.
- **Alternatives considered**:
  - Désactiver l’ancrage quand le texte dépasse N caractères → rejeté (affaiblissement qualité).
