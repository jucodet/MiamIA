# Modèle de données — Critique santé (002)

## Entités (logique métier)

### IngredientList (entrée affichée + payload)

| Champ | Type | Règles |
|-------|------|--------|
| `validatedSegmentText` | `String` | Texte **exact** du segment ingrédients **validé** dans le flux scan (FR-001, SC-005). Trim unique si et seulement si la même règle est appliquée au moment de la validation scan (documenter une seule normalisation). |
| `sourceScanId` | `String?` | Optionnel ; identifiant de session / scan pour traçabilité (FR-006). |
| `isEditableInHealthUi` | `Boolean` | **Toujours `false`** sur l’écran critique santé (clarification 2026-05-04). |

### AnalysisRequest

| Champ | Type | Règles |
|-------|------|--------|
| `id` | `String` (UUID) | Identifiant unique par lancement. |
| `createdAt` | `Instant` / `long` (epoch ms) | Horodatage local. |
| `ingredientList` | référence `IngredientList` | **Uniquement** rempli à partir du segment validé du scan. |
| `finalSystemPrompt` | `String` | Instructions système + contraintes prudence (FR-002, FR-003). |
| `userPayload` | `String` | Corps utilisateur passé au LLM : MUST reprendre `validatedSegmentText` (même contenu que l’affichage lecture seule). |
| `populations` | fixe | Quatre populations ordonnées (FR-002). |

### AnalysisResult

| Champ | Type | Règles |
|-------|------|--------|
| `requestId` | `String` | Lien vers `AnalysisRequest.id`. |
| `rawLlmText` | `String` | Texte brut retour modèle. |
| `sections` | `Map<PopulationKey, String>` | Dérivé du parseur. |
| `parseWarnings` | `List<String>` | Sections manquantes, marqueurs absents, ambiguïtés. |
| `disclaimerAcknowledged` | `Boolean` | UI a affiché l’avertissement non médical. |

### PopulationKey (enum)

- `ENFANTS`
- `FEMMES_ENCEINTES`
- `ADULTES`
- `PERSONNES_AGEES`

Ordre d’affichage UI = ordre ci-dessus.

## Persistance minimale (FR-006)

### LastHealthAnalysisSnapshot (DTO stockage)

| Champ | Type |
|-------|------|
| `savedAt` | `Instant` / epoch ms |
| `ingredientRaw` | `String` | Copie du **segment validé** au moment de l’analyse (alignement SC-005 / historique). |
| `resultRaw` | `String` |
| `systemPromptSnapshot` | `String` (optionnel) |
| `requestId` | `String` (optionnel) |

## Transitions d’état (UI / orchestration)

```text
SansSegmentValide → (tentative analyse → message « effectuer un scan » / segment requis)
SegmentValideAffiché (lecture seule) → ValidatingInput → Inferring → ResultReady | InferenceError
ResultReady → SegmentValideAffiché (nouveau scan remplace le segment source)
```

- **InvalidInput**: segment vide, trop court selon règles FR-005, ou **absence de segment validé** alors que l’utilisateur lance l’analyse.
- **InferenceError**: timeout, modèle absent, erreur LiteRT.

## Validation (rappel spec)

- Réponse structurée **4 sections** (FR-003, SC-001).
- **SC-005**: payload LLM = `validatedSegmentText` (contrôle test ou assertion en CI).
- Pas de diagnostic médical (User Story 2).
