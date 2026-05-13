# Tâches d'alignement code (post `speckit-sync-apply`)

Généré suite au refus du lot historique `proposals.json` (P1–P16) et à l'application **documentaire** du correctif Feature G / drift 2026-05-13.

## Task: Align code — retirer `CameraUiState` mort

**Spec Requirement** : propreté frontière capture / dette signalée drift-report (unspecced).

**Current Code** : `app/src/main/java/com/miamia/camera/CameraUiState.kt` avec `requiresSegmentConfirmation` non référencé dans `app/src/main`.

**Required Change** : supprimer le fichier **ou** documenter un usage réel ; en l'absence de références, suppression recommandée + nettoyage imports.

**Files to Modify** : `app/src/main/java/com/miamia/camera/CameraUiState.kt` ; recherche globale `CameraUiState`.

**Estimated Effort** : small

### Acceptance Criteria

- [ ] `rg CameraUiState app/src/main` → 0 occurrence **ou** usage documenté dans spec domaine capture.
- [ ] Compilation `:app:compileDebugKotlin` OK (lorsque le wrapper Gradle est rétabli).

---

## Task: Align tests — `IngredientSegmentConfirmationUiTest` / références chip

**Spec Requirement** : UGE-G-SC-002 ; contrat session-capture mis à jour.

**Current Code** : tests / tâches historiques mentionnent `ingredients_framing_tag_chip` et `confirm_segment_button` sur parcours nominal.

**Required Change** : adapter ou supprimer les tests devenus obsolètes ; faire pointer la couverture UI vers `CaptureScreenFeatureFUiTest` (Feature G) ou équivalent.

**Files to Modify** : `app/src/androidTest/java/com/miamia/camera/ingredientsegment/IngredientSegmentConfirmationUiTest.kt` ; éventuellement tests JVM gate.

**Estimated Effort** : small

### Acceptance Criteria

- [ ] Aucun test instrumenté ne **requiert** le chip retiré pour valider le parcours nominal.
- [ ] Suite AndroidTest capture verte sur la cible choisie.
