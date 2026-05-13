# Contrat UI — Libellés écran capture (Feature F)

**Domaine owner UX** : `user-guidance-experience`  
**Domaine owner technique tags / PreviewRegion** : `capture-recognition` — voir [`../../capture-recognition/contracts/capture-action-bar.md`](../../capture-recognition/contracts/capture-action-bar.md) (révision 2026-05-13).

## Objectif

Matérialiser UGE-F-FR-001 à UGE-F-FR-003 : libellés explicites, absence de bouton Test LLM, absence de la chaîne « Aperçu caméra actif ».

## Obligations produit (observables)

1. **État prêt à capturer** : lorsque la prévisualisation est active et que l’utilisatrice peut lancer une capture, le texte d’état visible (MediaPipe et/ou ligne sous la bande d’action) MUST communiquer explicitement l’intention « caméra / scan » ; interdit : le seul mot « Disponible » comme unique libellé informatif de cet état.
2. **Chaîne interdite** : la séquence exacte de caractères `Aperçu caméra actif` (graphème tel qu’en production avant Feature F) ne MUST apparaître dans aucun `Text` de l’écran capture.
3. **Bouton Test LLM** : aucun bouton, lien ou menu visible ne MUST porter le libellé « Test LLM » ni équivalent de démonstration LLM sur cet écran.
4. **Test tags** : le tag `camera_tab_llm_test_button` ne MUST plus être utilisé en production (aligné sur le contrat `capture-action-bar`).

## Vérification (ATDD)

- Tests Compose : `onNodeWithTag("camera_tab_llm_test_button")` → **absent** (ou test supprimé).
- Assertion texte : `onNode(...).assertTextContains("Aperçu caméra actif")` → **interdit** sur l’écran capture.
- Couverture géométrique : conserver les tests non-recouvrement `photo_preview_box` vs `capture_photo_button` après retrait du second bouton.

## Traçabilité

- `spec.md` — Feature F (US-F1 à US-F3, UGE-F-FR-001..003, UGE-F-SC-001..003).
