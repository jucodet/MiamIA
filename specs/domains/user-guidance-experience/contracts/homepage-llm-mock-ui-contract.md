# UI Contract - Homepage LLM Mock Trigger

## Purpose

Definir le contrat d'interaction observable pour le declenchement du test bouchonne LLM depuis la homepage et l'affichage du resultat.

## Command Input

### `RunHomepageLlmMockTest`

- **Source**: clic utilisateur sur le bouton homepage dedie.
- **Preconditions**:
  - aucune execution en etat `running`
- **Expected behavior**:
  - transition immediate vers etat `running`
  - declenchement d'une tentative d'analyse

## Observable Output Model

### `HomepageLlmMockViewState`

- `state`: `idle` | `running` | `success` | `failure`
- `responseText`: string optionnelle (renseignee uniquement en `success`)
- `errorCategory`: `timeout` | `runtime-unavailable` | `non-analysable-response` (uniquement en `failure`)
- `errorMessage`: string optionnelle (uniquement en `failure`)
- `canRun`: boolean (false si `running`, true sinon)

## Acceptance Mapping

- FR-001/FR-002: commande disponible et declenchement au clic.
- FR-003: `canRun=false` pendant `running`, pas de run concurrent.
- FR-004/FR-005: rendu visible de la reponse + etats explicites.
- FR-006: rendu erreur comprehensible en echec.
- FR-007: relance possible quand `state != running`.

## Error Semantics

- `timeout`: aucune reponse exploitable avant 30 secondes.
- `runtime-unavailable`: moteur local indisponible.
- `non-analysable-response`: reponse vide ou invalide selon le domaine d'analyse.
