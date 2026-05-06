# Data Model - user-guidance-experience (photo-capture-llm-result-flow)

## Entities

### 0) AppNavigationShell

- **Description**: enveloppe de l'application au démarrage et pendant l'usage — **sans** barre d'onglets principale (FR-015).
- **Fields**:
  - `rootDestination` (enum: `capture` uniquement pour la racine produit v1 de ce périmètre)
  - `tabsVisible` (boolean) — **false** pour la structure principale (pas d'onglets multi-sections)
  - `coldStartComplete` (boolean) — UI principale affichée (pour mesurer SC-007 côté produit)
- **Validation rules**:
  - Au premier frame « prêt » après lancement, `rootDestination = capture` (FR-001)
  - `tabsVisible` ne doit pas représenter un `TabRow` (ou équivalent) entre Accueil / Caméra / Santé comme navigation primaire

### 1) CaptureScreenUiSession

- **Description**: session UX sur l'ecran de prise de photo (previsualisation, boutons, chargement).
- **Fields**:
  - `sessionId` (string, unique)
  - `cameraPreview` (enum: `available` | `unavailable`)
  - `focusIndicator` (optional, implementation-specific reference)
  - `photoButtonEnabled` (boolean)
  - `llmTestButtonEnabled` (boolean)
  - `llmProcessing` (enum: `idle` | `in_progress` | `terminal_success` | `terminal_failure`)
  - `loadingOverlayVisible` (boolean) — true lorsque `llmProcessing = in_progress` apres une entree valide (photo ou test)
  - `userStillOnCaptureScreen` (boolean) — false si l'utilisatrice a quitte l'ecran (navigation / retour) pendant `in_progress`
- **Validation rules**:
  - Si `llmProcessing = in_progress`, alors `llmTestButtonEnabled = false` (FR-009) et `photoButtonEnabled = false` (research Decision 8)
  - Si `cameraPreview = unavailable`, la zone preview affiche le message FR-011 ; les boutons suivent la spec (test LLM reste pertinent)

### 2) LlmPipelineRun

- **Description**: execution LLM observable depuis l'ecran capture (photo ou bouton test).
- **Fields**:
  - `runId` (string, unique)
  - `source` (enum: `photo_capture` | `llm_test_button`)
  - `startedAt` (datetime)
  - `state` (enum: `idle` | `running` | `success` | `failure`)
  - `responseText` (string, optional)
  - `errorCategory` (enum optional: `timeout` | `runtime-unavailable` | `non-analysable-response`)
  - `errorMessage` (string, optional)
  - `navigationToResultAllowed` (boolean) — true seulement si l'utilisatrice est restee sur l'ecran capture jusqu'a l'etat terminal (inverse de FR-014 quand abandon)
- **Validation rules**:
  - `responseText` obligatoire si `state = success` et `navigationToResultAllowed = true`
  - `errorCategory` et `errorMessage` obligatoires si `state = failure` et `navigationToResultAllowed = true`
  - Une seule execution `running` a un instant donne pour une meme session capture

### 3) LlmResultScreenPayload

- **Description**: donnees affichees sur l'ecran resultat dedie.
- **Fields**:
  - `runId` (string, reference)
  - `bodyText` (string)
  - `isError` (boolean)
  - `errorCategory` (optional, meme enum que `LlmPipelineRun` si `isError`)
- **Validation rules**:
  - `bodyText` non vide sauf cas "sortie vide" gere par message explicite (edge case spec)

### 4) HomepageLlmTestRun (legacy / meme agregat conceptuel)

- **Description**: execution historique declenchee depuis la homepage ; champs alignes sur `LlmPipelineRun` avec `source = llm_test_button` pour coherence.
- **Note**: conserver les regles existantes pour les ecrans homepage tant que la migration n'unifie pas les ViewModels.

## Value Objects

- **LlmDisplayPayload**:
  - `title` (string, optional)
  - `body` (string)
  - `isMultiline` (boolean)
- **LlmFailureFeedback**:
  - `category` (`timeout` | `runtime-unavailable` | `non-analysable-response`)
  - `userMessage` (string non vide)

## Relationships

- `CaptureScreenUiSession` porte l'etat des boutons et du loader ; declenche `LlmPipelineRun`.
- `LlmPipelineRun` (termine, `navigationToResultAllowed = true`) -> alimente `LlmResultScreenPayload` puis navigation.

## State Transitions

- `idle -> in_progress`: apres capture photo valide ou clic test LLM accepte.
- `in_progress -> terminal_*`: reponse LLM ou echec ; si `userStillOnCaptureScreen`, `navigationToResultAllowed = true` et navigation vers ecran resultat ; sinon pas de navigation automatique.
- Apres affichage resultat (ou abandon), retour possible vers capture : `terminal_* -> idle` sur nouvelle session ou reinitialisation explicite (FR-012).

## Aggregate Boundary

- **Aggregate root conceptuel**: `CaptureScreenUiSession` + `LlmPipelineRun` lies par `sessionId` / `runId`.
- **Invariant principal**: pas d'analyse concurrente ; pas de navigation resultat si abandon pendant `in_progress` (FR-014).
