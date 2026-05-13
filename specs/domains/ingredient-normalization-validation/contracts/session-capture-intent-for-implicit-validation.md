# Contract — Intention de session « balise ingrédients » (FR-010)

## Purpose

Définir le signal minimal **amont** requis pour activer la validation implicite du segment (FR-010), sans confondre avec la détection d’**ancre** dans le texte OCR (FR-002).

## Obligations (downstream `ingredient-normalization-validation`)

1. **Source du signal** : le booléen (ou équivalent enum) « session avec balise / mode ingrédients » MUST provenir d’une **décision produit explicite** avant ou au moment de la capture (UI ou coordinator), pas d’une inférence sur le seul `transcriptText`.
2. **Couplage** : le consommateur métier (`CameraViewModel` + `AnalysisSubmissionGate`) MUST recevoir ce signal en même temps qu’un `scanId` et le transcript utilisé pour `IngredientSegmentPreparationService.prepare`.
3. **Indépendance** : si le signal est **faux**, le comportement MUST rester aligné sur FR-007 (affichage `SegmentConfirmationRequired` lorsque le segment est exploitable et qu’une analyse est demandée via le parcours actuel).

## Garde-fous (inchangés côté segment)

- Ancre absente ou segment vide / label seul : **pas** d’analyse automatique (FR-008), que le signal soit vrai ou faux.
- OCR échec / vide : pas d’application de FR-010 (alignement edge cases spec).

## Traçabilité

- Les logs ou métadonnées de décision SHOULD distinguer `implicit_fr010` vs `user_confirmed_ui` pour la conformité FR-009 (audit session).

## Références

- Spec : `spec.md` — FR-010, FR-011, US2b, SC-005.
- Plan : `plan.md` — branche `021-auto-analyze-ingredients-tag`.
