# Quickstart - ingredient-normalization-validation (017 + 021)

## Goal

- **017** : Valider manuellement que la logique de fin de capture du segment ingrédients traite correctement le `.` contextuel : un point suivi d'un espace ou d'un retour à la ligne termine la capture, un point interne (code additif, abréviation) ne la termine pas.
- **021 (FR-010)** : Valider le parcours **balise ingrédients** — enchaînement vers l'analyse **sans** écran de validation du segment, et la non-régression des autres parcours (FR-011).

## Preconditions

- Build Android fonctionnel (`app`).
- Spec : `specs/domains/ingredient-normalization-validation/spec.md`.
- Contrats : `contracts/boundary-resolver-contract.md`, `contracts/session-capture-intent-for-implicit-validation.md` (021).
- Tests JVM passent : `./gradlew :app:testDebugUnitTest`.

## Manual Validation Flow

### A. Point interne — pas de coupure

1. Depuis l'écran de capture, prendre en photo (ou fournir via mock) une étiquette contenant un texte de type :
   `Ingrédients: eau, colorant E.621, sucre, sel`
2. Vérifier que la proposition de segment contient **tout** le texte après l'ancre, y compris `E.621` — la capture ne s'arrête **pas** au `.` de `E.621`.

### B. Point + espace — fin de capture

1. Fournir un texte de type :
   `Ingrédients: eau, sucre, sel. Traces possibles de gluten.`
2. Vérifier que la proposition de segment se termine à `sel.` (premier `. ` après l'ancre), pas à `gluten.`.

### C. Point + retour à la ligne — fin de capture

1. Fournir un texte de type :
   ```
   Ingrédients: eau, sucre.
   Traces possibles de gluten.
   ```
2. Vérifier que la proposition se termine à `sucre.` (le `.\n` est reconnu).

### D. Point en fin de texte — fin de capture

1. Fournir un texte de type :
   `Ingrédients: eau, sel.`
2. Vérifier que la proposition se termine à `sel.` (le `.` en fin de texte est reconnu).

### E. Abréviation suivie de virgule — pas de coupure

1. Fournir un texte de type :
   `Ingredients: vit.B12, iron, zinc`
2. Vérifier que la proposition contient `vit.B12, iron, zinc` sans coupure prématurée.

### F. Ponctuation ! et ? — terminateurs inconditionnels

1. Fournir un texte de type :
   `Ingrédients: eau, sel!`
2. Vérifier que la proposition se termine à `sel!`.

### G. Parcours FR-010 — balise ingrédients, enchaînement sans écran de validation

1. Activer en build de test (ou via mock coordinator) le signal **« balise / mode ingrédients »** sur la session.
2. Fournir un OCR `success` ou `partial` avec transcript contenant une ancre valide et un segment **non vide** (ex. `Ingrédients: eau, sucre.`).
3. Vérifier que l'application **n'affiche pas** l'écran de confirmation de segment et enchaîne vers l'analyse (état streaming / écran résultat selon build).
4. Désactiver le signal : même transcript → l'écran de confirmation **doit** réapparaître (SC-003 / FR-011).

### H. FR-010 + segment vide / label seul

1. Balise active + transcript ne permettant qu'un label seul (`Ingrédients:` sans liste) ou segment bloqué par `AnalysisSubmissionGate`.
2. Vérifier **aucune** analyse silencieuse ; message ou état d'erreur aligné FR-008.

### I. OCR intégral vers LLM (FR-012 / SC-006) — 2026-05-13

1. Préparer un transcript multi-paragraphes (ex. nutrition + liste ingrédients) **sans** ancre « Ingrédients » sur la première partie.
2. Vérifier que l’écran de confirmation (si affiché) montre **l’intégralité** du transcript, et qu’après confirmation l’inférence reçoit exactement ce transcript (logs debug ou point d’arrêt sur `CompositionAnalysisEngine.analyze`).
3. Avec balise ingrédients + implicite FR-010 : vérifier enchaînement sans écran et même propriété d’intégralité.

## Suggested Automated Checks

- Tests unitaires JVM : `IngredientSegmentBoundaryResolverTest` (cas BC-01 à BC-07 du contrat).
- Tests d'acceptation : `IngredientSegmentPhraseBoundaryAcceptanceTest` (scénarios US1 §1 et §2 de la spec).
- Test de non-régression : `IngredientSegmentPerformanceTest` (pas de régression latence).
- **021** : `AnalysisSubmissionGateContractTest` + test UI / instrumenté ciblant SC-005 (absence d'écran confirmation quand signal actif).
- **2026-05-13** : `AnalysisSubmissionGateContractTest` (transcript complet, `!anchorFound` + contenu non vide) ; contrat `contracts/llm-input-full-ocr-contract.md`.

## Expected Outcomes

- Conformité à **SC-001** (100 % des propositions respectent FR-002 à FR-006, y compris au moins un cas de point interne).
- Aucune régression sur les tests existants (ancre, fin de ligne, fin de texte, multiples ancres).
- **021** : **SC-005** (100 % enchaînement sans écran sur chemin FR-010) et **SC-003** (parcours sans balise inchangé).
- **2026-05-13** : **SC-006** — entrée LLM = transcript OCR sessionnel complet (hors trim documenté).
