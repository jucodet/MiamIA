# Contract — UI surface « bande d’action de capture » (capture-recognition)

**Type** : contrat comportemental UI (application Android Compose).  
**Consommateurs** : équipe mobile (écran `CameraScreen`) ; aucun consommateur réseau.  
**Incrément** : 020 — bouton capture sous l’aperçu + libellé « Y a quoi là-dedans ? ». **Révision 2026-05-13 (cross-ref `user-guidance-experience` Feature F)** : plus d’action secondaire « Test LLM » en produit ; plus de chaîne exacte « Aperçu caméra actif » ; libellés d’état « prêt » explicites (voir contrat domaine UGE).

## Obligations

1. **Région aperçu (PreviewRegion)** : la zone vidéo (`CameraPreviewBox` ou placeholder d’indisponibilité de même empreinte) MUST être rendue à l’intérieur d’un conteneur dédié — repéré par le test tag `photo_preview_box` — dont aucun enfant persistant ne MUST être un bouton d’action utilisateur.
2. **Région actions (CaptureActionBarRegion)** : tous les boutons d’action liés à la capture (action principale + actions secondaires persistantes éventuelles) MUST être placés **en dehors** du conteneur `photo_preview_box`, dans une bande **immédiatement en dessous** de l’aperçu, alignée sur la largeur utile de l’écran.
3. **Non-recouvrement** : à tout instant et dans tout état (`PreviewActive`, `PreviewInitializing`, `Capturing`, `Analyzing`, `CameraUnavailable`), la bounding box rendue de tout bouton d’action MUST avoir un `top` ≥ `bottom` de la bounding box rendue de `photo_preview_box`. Les overlays internes à la `PreviewRegion` qui ne sont pas des actions utilisateur (spinner de capture, anneau de focus, indicateur d’analyse) ne sont **pas** concernés par cette obligation.
4. **Libellé action principale** : l’élément porteur du test tag `capture_photo_button` MUST afficher exactement le texte `« Y a quoi là-dedans ? »` (chaîne littérale, casse et ponctuation comprises ; espace insécable optionnel avant `?` selon plateforme, sans modifier la signification).
5. **Comportement clic** : le clic sur le bouton porteur du test tag `capture_photo_button` MUST conserver le comportement existant (déclenchement d’une capture explicite via `CameraViewModel.capturePhoto(...)`). Aucun changement de séquence d’événements n’est introduit par ce contrat.
6. **Actions secondaires** : depuis la livraison **Feature F** (`user-guidance-experience`), aucun bouton d’action secondaire (diagnostic / test LLM) ne MUST être présent sur la bande d’action capture. La `CaptureActionBarRegion` contient au minimum l’action principale (`capture_photo_button`).
7. **Statuts sous la bande d’action** : un libellé textuel d’état MAY être affiché sous la bande d’action (hors `PreviewRegion`). Il MUST rester compréhensible sans jargon ambigu (interdit : le seul mot « Disponible » comme unique information « prêt » ; interdit : la chaîne exacte « Aperçu caméra actif »). Les formulations transitoires (« Démarrage de l’aperçu caméra… », « Capture en cours… », « Traitement de l’image… ») restent autorisées si encore pertinentes.

## Non-obligations

- Choix d’un composant Compose précis (`Column`, `Row`, `Surface`, `Scaffold.bottomBar`, etc.) : libre tant que les obligations 1–7 sont respectées.
- Internationalisation du libellé via `strings.xml` : non obligatoire dans cet incrément ; un suivi est recommandé pour exposer `R.string.capture_action_primary`.
- Style visuel précis du bouton (couleur, élévation) : conforme au design Material 3 existant ; non prescrit par ce contrat.

## Test tags requis (stables)

- `photo_preview_box` (existant — région aperçu)
- `capture_photo_button` (existant — bouton action principale, libellé « Y a quoi là-dedans ? »)

> Le tag `camera_tab_llm_test_button` était requis pour l’incrément 020 ; il est **retiré** avec la Feature F `user-guidance-experience` (plus de bouton Test LLM en produit).

## Traçabilité spec

- `spec.md` — CR-FR-009, CR-FR-010, CR-FR-011 ; scénarios User Story 1 et 2 (incrément 020) ; SC-CR-003, SC-CR-004, SC-CR-005.
- `research.md` — R-001, R-002, R-003, R-004, R-005.
- `user-guidance-experience/spec.md` — Feature F (UGE-F-FR-001..004) ; `user-guidance-experience/contracts/capture-screen-feature-f-status-copy.md`.
