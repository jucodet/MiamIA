# Research — Zone défilante texte capturé (019)

## 1. Identification des écrans concernés (code actuel)

**Decision** : L’incrément cible en priorité `CameraScreen.kt` pour les branches `when (state)` où un texte long et des boutons coexistent sans séparation scroll / pied fixe.

**Rationale** :

- `ScanState.Success` affiche `transcriptText` et la liste `items` en `Text` successifs sans `verticalScroll` ni `weight` : un texte OCR très long pousse les boutons hors écran.
- `ScanState.BilanReady` enveloppe `BilanResultCard` + bouton « Nouveau scan » dans un `Column` **entièrement** `verticalScroll` : le bouton défile avec le contenu, ce qui contrevient à CR-FR-007.
- `ScanState.SegmentConfirmationRequired` utilise déjà `heightIn(max = 280.dp)` + `verticalScroll` sur la zone texte, mais le `Column` parent n’occupe pas forcément toute la hauteur utile ; à valider avec `fillMaxSize()` + `weight(1f)` sur la zone scroll pour remplir l’espace sous les en-têtes (MediaPipe, welcome) tout en gardant les boutons en bas.

**Alternatives considered** :

- **Dialog plein écran** pour le texte brut — rejeté : hors scope spec (même parcours, géométrie seulement).
- **`LazyColumn`** pour le texte — possible mais plus lourd qu’un `verticalScroll` pour un seul bloc `Text` ; garder `verticalScroll` sauf mesure de perf contraire.

## 2. Pattern de mise en page Compose

**Decision** : `Column(Modifier.fillMaxSize())` avec zone centrale `Modifier.weight(1f, fill = true).verticalScroll(...)` et actions **hors** du scroll, comme `LlmResultScreen.kt` (lignes ~73–127).

**Rationale** : pattern déjà validé dans le dépôt ; satisfait CR-FR-006/007/008 avec peu de risque.

**Alternatives considered** :

- **`SubcomposeLayout` / `ConstraintLayout`** — rejeté sauf si `weight` insuffisant sur un appareil cible (YAGNI).

## 3. Clavier logiciel et `windowInsets`

**Decision** : S’appuyer sur le `Column` racine `CameraScreen` déjà `fillMaxSize` + padding ; si lors des tests instrumentés le clavier masque les boutons sur un champ futur, appliquer `Modifier.imePadding()` ou équivalent sur la zone actions — **hors périmètre immédiat** tant qu’aucun champ éditable n’est focus sur ces états.

**Rationale** : la spec mentionne le clavier comme cas limite ; les états cibles sont aujourd’hui en lecture seule.

**Alternatives considered** : `WindowInsets` systématiques sur tous les états — reporté pour éviter régression visuelle sur états caméra plein écran.

## 4. Tests

**Decision** : Étendre les tests UI existants (`IngredientSegmentConfirmationUiTest`) ou ajouter un test ciblant `ScanState.Success` avec `transcriptText` long + assertion que `new_scan_button` reste dans les bounds visibles (sémantique ou `performScrollTo` absent sur le bouton).

**Rationale** : alignement constitution ATDD ; tags `testTag` déjà utilisés (`new_scan_button`, `segment_preview_scroll`, etc.).

**Alternatives considered** : test manuel uniquement — insuffisant pour gate constitution.
