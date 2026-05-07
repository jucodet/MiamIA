# UI Contract - Capture ecran + chargement LLM + navigation resultat

## Purpose

Contracter le comportement observable du flux **photo-capture-llm-result-flow** : ordre des controles, chargement sur l'ecran capture, navigation conditionnelle vers l'ecran resultat, reutilisation du test LLM.

## Preconditions generales

- Spec : `specs/domains/user-guidance-experience/spec.md` (FR-001 à FR-016, clarifications 2026-05-06).
- **Shell** : pas de barre d'onglets principale (FR-015) ; premier écran = capture (FR-001, SC-007).

## Command inputs

### `TapPhotoCapture`

- **Effect**: lance le flux de capture existant (FR-004).
- **Guards**: ignore ou no-op si `llmProcessing = in_progress` (alignement research Decision 8).

### `TapLlmTest`

- **Effect**: declenche `HomeLlmMockRunner` (ou equivalent injecte) — FR-008.
- **Preconditions**: aucun run `running` ; bouton enabled (FR-009).

### `LeaveCaptureScreen`

- **Effect**: utilisateur quitte l'écran de capture (retour arrière, autre destination du graphe, ou sortie du scope capture) — le shell principal n'utilise pas d'onglets (FR-015).
- **Postcondition**: `userStillOnCaptureScreen = false` pour le run en cours si `llmProcessing = in_progress` ; à la fin du traitement, **aucune** navigation automatique vers l'écran résultat (FR-014).

## Observable states - ecran capture

| Element | Regle |
|---------|--------|
| Ordre vertical | Previsualisation (ou message indispo) puis bouton photo puis bouton test LLM (FR-001, FR-004, FR-005) ; écran présenté au lancement sans onglets (FR-001, FR-015) |
| Loader | Visible sur l'ecran capture (overlay autorise) pendant `in_progress` ; ecran resultat non montre avant fin (FR-006, FR-013) |
| Bouton test | `enabled = false` pendant run test ou analyse photo en cours (FR-009) |
| Bouton photo | `enabled = false` pendant `in_progress` (research Decision 8) |

## Observable states - navigation resultat

| Condition | Navigation |
|-----------|------------|
| `state` terminal success / failure **et** utilisatrice restee sur capture jusqu'a la fin du chargement | Navigation vers ecran resultat avec payload (FR-007, FR-010) |
| Abandon pendant chargement | Pas de navigation automatique a la fin du traitement (FR-014) |
| Retour depuis écran résultat | Retour vers la capture comme contexte d'accueil, sans réintroduction d'onglets (FR-016) |

## Ecran resultat - payload minimal

- `bodyText`: texte principal (succes) ou message d'erreur utilisateur (echec).
- `isError`: booleen.
- `errorCategory`: si `isError`, une des valeurs `timeout` | `runtime-unavailable` | `non-analysable-response` (alignement contrat homepage existant).

## Cartographie acceptance (extraits)

- US2 scenario 1, 2, 3, 5 -> sections Command inputs + Observable states.
- US3 scenario 3, 4 -> test LLM + abandon.
- SC-003 -> loader visible < 1 s apres debut traitement (mesure produit, hors contrat implementation).

## Dependances inter-domaines

- Contenu semantique du texte LLM : domaines composition / sante ; cet ecran affiche seulement le payload fourni.
