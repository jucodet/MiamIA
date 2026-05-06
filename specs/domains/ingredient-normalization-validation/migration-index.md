# Migration Index - ingredient-normalization-validation

## Source -> Target

- `001-scan-ingredients/spec.md` -> `spec.md` (`Scope`: structuration ingredients) [validated]
- `006-identify-photo-ingredients/spec.md` -> `spec.md` (`Scope`, `Functional Requirements`) [validated]
- `013-isoler-liste-ingredients/spec.md` -> `spec.md` (`Functional Requirements`, `Invariants`) [validated]
- `014-capture-liste-ingredients/spec.md` -> `spec.md` (`Functional Requirements`: ~~priorite ancre `ingredients:`~~ **abroge**, voir `spec.md` ingredient-phrase-segment) [superseded]
- `015-analyse-ocr-llm/spec.md` -> `spec.md` (`Invariants`: coherence liste affichee/analysee) [validated]

## Conflict Decisions

- **2026-05-06**: la regle unique `ingredient-phrase-segment` remplace toute priorite a l ancienne ancre canonique `ingredients:` / premier saut de ligne systematique. Ancre = premiere occurrence **en debut de ligne** (apres espaces) parmi `Ingrédient(s)` / `Ingredient(s)` ; borne = ponctuation de phrase puis fin de ligne puis fin de texte (voir `spec.md`, `contracts/ingredient-segment-boundary-contract.md`). **Implementation** (Android): `IngredientAnchorNormalizer`, `IngredientSegmentBoundaryResolver`, `IngredientSegmentPreparationService`, `IngredientExtractionPipeline`, `CameraViewModel` — valide apres `gradlew test --tests *IngredientSegment*`.
