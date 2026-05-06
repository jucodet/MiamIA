# Data Model - user-guidance-experience (homepage-llm-mock-trigger)

## Entities

### 1) HomepageLlmTestRun

- **Description**: execution observable du test bouchonne declenchee depuis la homepage.
- **Fields**:
  - `runId` (string, unique)
  - `triggeredAt` (datetime)
  - `state` (enum: `idle` | `running` | `success` | `failure`)
  - `responseText` (string, optional)
  - `errorCategory` (enum optional: `timeout` | `runtime-unavailable` | `non-analysable-response`)
  - `errorMessage` (string, optional)
- **Validation rules**:
  - `responseText` obligatoire si `state = success`
  - `errorCategory` et `errorMessage` obligatoires si `state = failure`
  - Une seule execution `running` autorisee a un instant donne

### 2) HomepageTriggerAction

- **Description**: commande utilisateur issue du clic bouton sur la homepage.
- **Fields**:
  - `actionAt` (datetime)
  - `source` (fixed value: `homepage-button`)
  - `accepted` (boolean)
  - `rejectionReason` (string, optional)
- **Validation rules**:
  - `accepted = false` si une execution est deja `running`

## Value Objects

- **LlmDisplayPayload**:
  - `title` (string)
  - `body` (string non vide)
  - `isMultiline` (boolean)
- **LlmFailureFeedback**:
  - `category` (`timeout` | `runtime-unavailable` | `non-analysable-response`)
  - `userMessage` (string non vide)

## Relationships

- `HomepageTriggerAction` (accepted) -> cree un `HomepageLlmTestRun`.
- `HomepageLlmTestRun` produit soit `LlmDisplayPayload` (succès), soit `LlmFailureFeedback` (échec).

## State Transitions

- `idle -> running`: clic accepte.
- `running -> success`: reponse LLM affichee.
- `running -> failure`: timeout, indisponibilite runtime, ou reponse non analysable.
- `success -> running`: relance manuelle.
- `failure -> running`: relance manuelle.

## Aggregate Boundary

- **Aggregate root**: `HomepageLlmTestRun`.
- **Invariant principal**: aucune execution concurrente `running` sur la homepage.
