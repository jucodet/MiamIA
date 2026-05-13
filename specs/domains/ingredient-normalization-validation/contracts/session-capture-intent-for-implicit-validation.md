# Contract — Intention d'enchaînement direct post-OCR (FR-010, alignement Feature G)

## Purpose

Définir comment le domaine **`ingredient-normalization-validation`** interprète le **signal d'intention** d'analyser sur le **transcript OCR complet** sans étape de relecture / validation segment, **sans** confondre avec la détection d'**ancre** dans le texte (FR-002) pour les **vues auxiliaires** uniquement.

## Contexte produit (2026-05-13)

- **`user-guidance-experience` Feature G** : retrait du chip UI « balise ingrédients » (UGE-G-FR-002) ; retrait du parcours nominal vers un écran type `SegmentConfirmationRequired` (UGE-G-FR-004).
- L'orchestrateur capture (`CameraViewModel`) satisfait le gate avec `implicitValidationFromIngredientsFraming = true` pour la **décision pré-analyse** sur transcript complet lorsque le produit active le **chemin direct** documenté en UGE-G.

## Obligations

1. **Signal amont** : l'intention « enchaînement direct sur transcript admissible » est portée par la **politique produit / orchestration** (UGE-G + `CameraViewModel` + `AnalysisSubmissionGate`), **sans** exiger un contrôle UI « balise » distinct sur l'écran capture.
2. **Couplage** : le consommateur (`CameraViewModel` + `AnalysisSubmissionGate`) MUST recevoir le `scanId`, le transcript utilisé pour l'évaluation, et les paramètres du gate en cohérence avec FR-012. `IngredientSegmentPreparationService.prepare` MAY être invoqué pour **proposition auxiliaire** / traçabilité (FR-014), **sans** constituer l'entrée LLM.
3. **Parcours nominal** : lorsque le transcript complet satisfait les garde-fous du gate (non vide, non « label ingrédients seul », etc.), le système MUST enchaîner vers l'analyse LLM **sans** étape utilisateur dont la fonction première est la relecture du transcript avant analyse.
4. **FR-007 / FR-011** : restent applicables à tout **traitement aval hors FR-012** qui imposerait encore une liste isolée comme entrée obligatoire confirmée par l'utilisatrice (parcours non couverts par l'analyse LLM sur OCR intégral).

## Garde-fous

- OCR vide / label seul / inexploitable : pas d'analyse (FR-008), indépendamment du chemin nominal.
- Les règles FR-001–FR-006 ne filtrent pas l'entrée LLM (FR-014).

## Traçabilité

- Les logs ou métadonnées SHOULD distinguer `implicit_gate_preview` vs toute future réintroduction d'une confirmation UI explicite distincte.

## Références

- `spec.md` — FR-010, FR-011, FR-012, US2b (libellés historiques « balise » = intention directe lorsque Feature G s'applique).
- `specs/domains/user-guidance-experience/spec.md` — Feature G (UGE-G-FR-001–004).
