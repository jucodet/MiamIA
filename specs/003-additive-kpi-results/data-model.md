# Modèle de données — KPI additifs (003)

## Énumérations

### AdditiveRiskLevel (affichage + tri)

| Valeur | Couleur MVP | Tri (décroissant) |
|--------|-------------|-------------------|
| `HIGH` | Rouge | 0 |
| `MEDIUM` | Orange | 1 |
| `LOW` | Vert | 2 |
| `UNKNOWN` | Neutre / gris | 3 (après les autres, ou regroupé « à confirmer ») |

### AdditiveLineConfidence

- `OK` — niveau et justification présents et cohérents selon règles simples.
- `NEEDS_CONFIRMATION` — niveau ou justification manquant (réponse LLM partielle).
- `INCOHERENT` — contradiction détectée (ex. niveau LOW avec justification « cancérogène probable » — heuristique configurable).
- `DUPLICATE_MERGED` — ligne fusionnée avec une précédente (optionnellement exposé à l’UI).

## Entités

### AdditiveRiskItem

| Champ | Type | Règles |
|-------|------|--------|
| `canonicalName` | `String` | Nom affiché ; normalisé pour égalité. |
| `displayName` | `String` | Peut conserver casse d’origine pour lisibilité. |
| `level` | `AdditiveRiskLevel` | Dérivé parseur / défaut `UNKNOWN`. |
| `justification` | `String` | Court ; tronqué côté présentation si au-delà du seuil. |
| `confidence` | `AdditiveLineConfidence` | FR-005 / edge cases. |

### RiskSummaryKPI

| Champ | Type | Règles |
|-------|------|--------|
| `totalCount` | `Int` | Égal au nombre de lignes après dédoublonnage. |
| `highCount` | `Int` | Compte `level == HIGH`. |
| `mediumCount` | `Int` | Compte `level == MEDIUM`. |
| `lowCount` | `Int` | Compte `level == LOW`. |
| `unknownCount` | `Int` | Compte `UNKNOWN` + éventuellement `NEEDS_CONFIRMATION` selon règle produit. |
| `globalLevel` | `AdditiveRiskLevel?` | Optionnel (P3) : pire niveau observé parmi les items `OK`, sinon `null` si tout `UNKNOWN`. |

**Invariant SC-003**: `totalCount == highCount + mediumCount + lowCount + unknownCount` (avec convention unique pour ranger `NEEDS_CONFIRMATION` dans `unknownCount`).

### AnalysisDisplayResult

| Champ | Type | Règles |
|-------|------|--------|
| `sourceRawLlmText` | `String` | Texte modèle complet (traçabilité, debug). |
| `itemsOrdered` | `List<AdditiveRiskItem>` | Tri FR-003 : HIGH → MEDIUM → LOW → UNKNOWN. |
| `summary` | `RiskSummaryKPI` | Dérivé de `itemsOrdered`. |
| `parseErrors` | `List<String>` | Messages utilisateur ou diagnostics non bloquants. |
| `isEmptyState` | `Boolean` | Vrai si aucun additif exploitable après parsing. |

## Transitions

```text
CompositionBilan + rawModelText
    → run AdditiveKpiPipeline.parse()
    → AnalysisDisplayResult
    → UI (liste + KPI)
```

## Validation

- Si `isEmptyState` : afficher l’état vide spec US1 scénario 3.
- Si `parseErrors` non vide : bannière d’avertissement sans masquer la liste partielle si items présents.
