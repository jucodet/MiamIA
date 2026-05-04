# Contract: Affichage KPI additifs (post-LLM)

## Objectif

Décrire le contrat entre la couche **présentation** (`CameraScreen` / composants Compose) et la couche **domaine** (`AnalysisDisplayResult`) pour la feature **003**, après obtention d’une réponse LLM de composition.

## Entrée (ViewModel / use-case)

- **`bilan`**: `CompositionBilan` existant (`ingredientLines`, `compositionAnalysis`, `disclaimer`).
- **`rawLlmText`**: texte brut complet retourné par Gemma (pour parseur évolutif).
- **Commande logique**: `BuildAdditiveKpiDisplay(bilan, rawLlmText) -> AnalysisDisplayResult`

## Sortie UI (obligations)

### Liste (FR-002, FR-003)

- Une **ligne** par `AdditiveRiskItem` dans `itemsOrdered` (ordre déjà appliqué côté domaine).
- Chaque ligne MUST afficher :
  - pastille / indicateur de couleur + **libellé** de niveau (accessibilité) ;
  - `displayName` (ou `canonicalName` si pas de variante) ;
  - `justification` tronquée si nécessaire, avec possibilité d’**expansion** inline (FR-006 : pas de navigation profonde — `click` ou `Dropdown` sur la même ligne autorisé).

### KPI globaux (FR-004, US3)

- Bloc « synthèse » MUST montrer : `totalCount`, `highCount`, `mediumCount`, `lowCount`, `unknownCount` (libellés FR).
- Les compteurs MUST être **strictement dérivés** de `itemsOrdered` (SC-003).

### États vides et erreurs (FR-005, US1 scénario 3)

- Si `isEmptyState` : message explicite du type « Aucun additif identifié dans cette analyse » + lien secondaire vers texte brut si disponible.
- Si `confidence == NEEDS_CONFIRMATION` ou `INCOHERENT` : **badge** ou texte secondaire sur la ligne (« à confirmer », « incohérence »).

### Tests d’acceptation (mapping)

| Scénario spec | Vérification contrat |
|---------------|----------------------|
| US1.1 pastilles | Chaque item a indicateur couleur + libellé |
| US1.2 ordre | `itemsOrdered` respecte HIGH puis MEDIUM puis LOW puis UNKNOWN |
| US1.3 vide | `isEmptyState` → UI état vide |
| US2 justification | Texte non vide sauf `UNKNOWN` + règle produit |
| US3 KPI | `summary` aligné avec liste |

## Non-objectifs (MVP)

- Pas d’appel réseau pour enrichir les additifs.
- Pas de certification réglementaire des classifications (rappel disclaimer composition existant + non-diagnostic).
