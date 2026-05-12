# Contract: Gemma4 Local API Integration

## Purpose

Definir le contrat fonctionnel entre l'application MiamIA et la couche d'integration de l'API locale Gemma4 pour les requetes textuelles v1.

## Input Contract

- **Operation**: `analyzeText`
- **Required input**:
  - `requestId`: string unique
  - `inputText`: string non vide
  - `sourceScreen`: string optionnel
- **Preconditions**:
  - verification de disponibilite API locale executee avant appel d'analyse.
  - aucune etape de consentement explicite additionnelle.

## Output Contract

- **Success response**:
  - `status = SUCCESS`
  - `requestId` inchange
  - `outputText` renseigne
  - `completedAt` renseigne
- **Failure response**:
  - `status = FAILED`
  - `requestId` inchange
  - `errorType` dans `{API_UNAVAILABLE, NETWORK_LOCAL, INVALID_RESPONSE, TIMEOUT, UNKNOWN}`
  - `userMessage` explicite et actionnable
  - `completedAt` renseigne

## Behavioral Rules

- Aucun fallback vers un autre moteur local ou distant en cas d'echec.
- En cas d'indisponibilite API locale, l'operation retourne immediatement un echec explicite.
- Le flux applicatif reste utilisable hors fonctionnalite d'analyse.
- Le perimetre v1 est limite aux requetes textuelles.

## Observability Contract

- Enregistrer pour chaque appel:
  - `requestId`, `outcome`, `latencyMs`, `errorType`, `deviceClass`, `recordedAt`
- Interdiction de journaliser le contenu utilisateur (`inputText`, `outputText`).

## Performance Contract

- Appareils recents: au moins 95% des analyses aboutissent en moins de 3 secondes.
- Appareils minimum compatibles: au moins 95% des analyses aboutissent en moins de 6 secondes.
- 100% des erreurs d'appel affichent un message utilisateur en moins de 2 secondes apres detection.
