# Contract — UI surface « bande d’action de capture » (capture-recognition)

**Type** : contrat comportemental UI (application Android Compose).  
**Consommateurs** : équipe mobile (écran `CameraScreen`) ; aucun consommateur réseau.  
**Incrément** : 020 — bouton capture sous l’aperçu + libellé « Y a quoi là-dedans ? ».

## Obligations

1. **Région aperçu (PreviewRegion)** : la zone vidéo (`CameraPreviewBox` ou placeholder d’indisponibilité de même empreinte) MUST être rendue à l’intérieur d’un conteneur dédié — repéré par le test tag `photo_preview_box` — dont aucun enfant persistant ne MUST être un bouton d’action utilisateur.
2. **Région actions (CaptureActionBarRegion)** : tous les boutons d’action liés à la capture (action principale + actions secondaires persistantes éventuelles) MUST être placés **en dehors** du conteneur `photo_preview_box`, dans une bande **immédiatement en dessous** de l’aperçu, alignée sur la largeur utile de l’écran.
3. **Non-recouvrement** : à tout instant et dans tout état (`PreviewActive`, `PreviewInitializing`, `Capturing`, `Analyzing`, `CameraUnavailable`), la bounding box rendue de tout bouton d’action MUST avoir un `top` ≥ `bottom` de la bounding box rendue de `photo_preview_box`. Les overlays internes à la `PreviewRegion` qui ne sont pas des actions utilisateur (spinner de capture, anneau de focus, indicateur d’analyse) ne sont **pas** concernés par cette obligation.
4. **Libellé action principale** : l’élément porteur du test tag `capture_photo_button` MUST afficher exactement le texte `« Y a quoi là-dedans ? »` (chaîne littérale, casse et ponctuation comprises ; espace insécable optionnel avant `?` selon plateforme, sans modifier la signification).
5. **Comportement clic** : le clic sur le bouton porteur du test tag `capture_photo_button` MUST conserver le comportement existant (déclenchement d’une capture explicite via `CameraViewModel.capturePhoto(...)`). Aucun changement de séquence d’événements n’est introduit par ce contrat.
6. **Actions secondaires** : si une ou plusieurs actions secondaires (ex. action de diagnostic LLM `camera_tab_llm_test_button`) sont présentes, elles MUST partager la même `CaptureActionBarRegion` et respecter l’obligation de non-recouvrement (point 3).
7. **Statuts d’aperçu** : le libellé textuel d’état (« Aperçu caméra actif », « Démarrage de l’aperçu caméra… », « Capture en cours… », « Traitement de l’image… ») MUST rester sous la bande d’action ou au moins en dehors de la `PreviewRegion`.

## Non-obligations

- Choix d’un composant Compose précis (`Column`, `Row`, `Surface`, `Scaffold.bottomBar`, etc.) : libre tant que les obligations 1–7 sont respectées.
- Internationalisation du libellé via `strings.xml` : non obligatoire dans cet incrément ; un suivi est recommandé pour exposer `R.string.capture_action_primary`.
- Style visuel précis du bouton (couleur, élévation) : conforme au design Material 3 existant ; non prescrit par ce contrat.

## Test tags requis (stables)

- `photo_preview_box` (existant — région aperçu)
- `capture_photo_button` (existant — bouton action principale, libellé « Y a quoi là-dedans ? »)
- `camera_tab_llm_test_button` (existant — action secondaire ; placement sous la même bande)

## Traçabilité spec

- `spec.md` — CR-FR-009, CR-FR-010, CR-FR-011 ; scénarios User Story 1 et 2 (incrément 020) ; SC-CR-003, SC-CR-004, SC-CR-005.
- `research.md` — R-001, R-002, R-003, R-004, R-005.
