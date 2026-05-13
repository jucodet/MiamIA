# Contrat : entrée LLM = transcript OCR intégral (FR-012)

## Portée

- Toute analyse composition (ou équivalent) invoquée après une reconnaissance **success** / **partial** MUST recevoir la même chaîne que le transcript sessionnel (concaténation des lignes OCR normalisées, séparateur `\n`), après `trim()` éventuel appliqué de façon uniforme (`AnalysisInputBuilder.buildSegmentPayload` si utilisé).
- Les bornes `IngredientSegmentExtraction` (ancrage FR-002–FR-006) MUST NOT réduire ce payload.

## Garde-fou (`AnalysisSubmissionGate`)

- Entrée supplémentaire obligatoire : `fullOcrTranscript` (transcript complet).
- Refus (`submissionAllowed = false`, raison ≠ `USER_REJECTED`) si transcript trim vide ou si le transcript entier correspond au motif « label ingrédients seul » (même regex que segment label-only).
- `!anchorFound` ne MUST NOT à lui seul produire un refus si le transcript est exploitable.

## Affichage confirmation

- `AnalysisSubmissionDecision.segmentPreview` MUST refléter le texte présenté / analysé pour le chemin LLM (transcript intégral trim), afin d’aligner UI et inférence (SC-006).
