# Modèle de données — Critique santé (002)

## Entités (logique métier)

### IngredientList

| Champ | Type | Règles |
|-------|------|--------|
| `rawText` | `String` | Saisie utilisateur ; trim ; non accepté si blanc (FR-005). |
| `normalizedText` | `String?` | Optionnel ; normalisation légère (espaces, casse) pour affichage ou clé de cache. |

### AnalysisRequest

| Champ | Type | Règles |
|-------|------|--------|
| `id` | `String` (UUID) | Identifiant unique par lancement. |
| `createdAt` | `Instant` / `long` (epoch ms) | Horodatage local. |
| `ingredientList` | référence `IngredientList` | Entrée figée au moment du run. |
| `finalSystemPrompt` | `String` | Instructions système + contraintes prudence (FR-002, FR-003). |
| `userPayload` | `String` | Corps utilisateur (liste d’ingrédients + contexte minimal). |
| `populations` | fixe | Quatre populations ordonnées : enfants, femmes enceintes, adultes, personnes âgées (FR-002). |

### AnalysisResult

| Champ | Type | Règles |
|-------|------|--------|
| `requestId` | `String` | Lien vers `AnalysisRequest.id`. |
| `rawLlmText` | `String` | Texte brut retour modèle. |
| `sections` | `Map<PopulationKey, String>` | Dérivé du parseur : une entrée par population si détection réussie. |
| `parseWarnings` | `List<String>` | Sections manquantes, marqueurs absents, ambiguïtés (edge cases spec). |
| `disclaimerAcknowledged` | `Boolean` | UI a affiché l’avertissement non médical (aligné Assumptions). |

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
| `ingredientRaw` | `String` |
| `resultRaw` | `String` |
| `requestId` | `String` (optionnel, traçabilité) |

Une seule ligne logique écrasée à chaque analyse réussie (ou politique produit : conserver aussi la dernière erreur — à trancher en implémentation, défaut : **dernière analyse aboutie**).

## Transitions d’état (UI / orchestration)

```text
Idle → ValidatingInput → (InvalidInput → Idle avec message)
Idle → PromptReady → Inferring → ResultReady | InferenceError
ResultReady → Idle (nouvelle saisie)
```

- **InvalidInput**: liste vide ou trop courte (FR-005).
- **InferenceError**: timeout, modèle absent, erreur LiteRT (réutiliser patterns message 009 si même moteur).

## Validation (rappel spec)

- Réponse structurée **4 sections** (FR-003, SC-001).
- Pas de diagnostic médical ; formulations prudentes (User Story 2).
