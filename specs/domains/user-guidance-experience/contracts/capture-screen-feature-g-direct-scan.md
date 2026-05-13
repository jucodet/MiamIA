# Contrat UI — Feature G : capture directe, chrome épurée

**Domaine** : `user-guidance-experience`  
**Écran** : capture (accueil) — `CameraScreen`  
**Références spec** : US-G1, US-G2 ; UGE-G-FR-001 à UGE-G-FR-004 ; UGE-G-SC-001, UGE-G-SC-002

## Obligations MUST

1. **Pas de chip balise** : l’arbre Compose de l’écran capture en état prêt (`PreviewActive` ou équivalent « prêt à photographier ») MUST NOT contenir de nœud avec `testTag("ingredients_framing_tag_chip")` ni de libellé « Balise ingrédients » (ou équivalent sémantique « cadrage ingrédients seuls »).
2. **Chaîne interdite** : MUST NOT afficher la chaîne exacte `Caméra prête — vous pouvez scanner` (casse et tiret comme indiqué).
3. **Pas de ligne statut prêt obligatoire** : pour `PreviewActive`, MUST NOT exiger un `Text` sous la prévisualisation dont le rôle est d’inviter à scanner ; les états transitoires (`PreviewInitializing`, `Capturing`, `Analyzing`) MAY afficher des libellés utilitaires distincts, sans violer (1) ni (2).
4. **Pas d’écran confirmation segment nominal** : après OCR `success`/`partial` avec transcript admissible par le gate, le parcours MUST NOT présenter l’UI historique de confirmation transcript (boutons type « Confirmer et analyser » / « Reprendre la photo » issus de l’état `SegmentConfirmationRequired`).

## Tags de test (instrumentation)

| Tag | Attendu Feature G |
|-----|-------------------|
| `ingredients_framing_tag_chip` | 0 nœud |
| `capture_scan_status_text` | Absent ou non affiché en `PreviewActive` ; si présent pour transitoires, texte ≠ chaîne interdite |
| `confirm_segment_button` | 0 nœud sur parcours nominal (état retiré) |

## Relation inter-domaines

- Le **transcript complet** et les garde-fous de soumission restent documentés / testés sous `ingredient-normalization-validation` (`AnalysisSubmissionGate`). Le présent contrat ne duplique pas les règles transcript ; il fixe uniquement la **chrome** et la **navigation** visibles utilisatrice.
