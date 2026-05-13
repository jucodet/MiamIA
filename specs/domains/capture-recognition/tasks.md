---

description: "Tasks — Bouton capture sous l’aperçu + libellé « Y a quoi là-dedans ? »"
---

# Tasks: Bouton capture sous l’aperçu et libellé « Y a quoi là-dedans ? » (incrément 020)

**Input**: Design documents from `specs/domains/capture-recognition/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/capture-action-bar.md`, `quickstart.md`

**Tests**: Les tests d’acceptation/parcours (ATDD) sont **OBLIGATOIRES** (Constitution v0.2.0, Principe II). Chaque user story embarque au moins un test Compose UI aligné sur ses scénarios Given/When/Then avant toute implémentation.

**Organization**: Tâches regroupées par user story pour permettre une implémentation et un test indépendants de chaque story (US1 placement, US2 libellé). Les deux stories sont P1 et touchent le même fichier (`CameraScreen.kt`) ; elles restent **séquençables** (US1 → US2) ou **livrables ensemble** dans une même PR — leur indépendance est assurée par leurs tests respectifs.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: peut être exécuté en parallèle (fichier distinct, aucune dépendance bloquante).
- **[Story]**: rattachement à une user story (`US1`, `US2`) ; absente pour Setup / Foundational / Polish.
- Chaque description inclut le **chemin exact** des fichiers concernés.

## Path Conventions

- **Mobile (Android, Compose)** : `app/src/main/java/com/miamia/...` (code production) et `app/src/androidTest/java/com/miamia/...` (tests d’instrumentation Compose).
- **Specs / contracts** : `specs/domains/capture-recognition/...`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Vérifier que l’écosystème de build/test est fonctionnel avant d’écrire toute nouvelle ligne — incrément UI minimal, aucun nouveau module ni nouvelle dépendance.

- [~] T001 Vérifier que le build debug courant compile sans régression (`./gradlew :app:assembleDebug`) et qu’un test d’instrumentation déjà existant (ex. `com.miamia.camera.CameraCaptureLayoutUiTest`) s’exécute sur émulateur/appareil cible (`./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraCaptureLayoutUiTest`). **Différé** : exécuter sur poste avec Android SDK configuré (`ANDROID_HOME`) et émulateur/appareil connecté ; non exécutable dans l’environnement courant (sandbox WSL sans SDK complet).

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infrastructure transverse aux deux user stories ; **bloque** US1 et US2.

**⚠️ CRITICAL**: Aucun travail US1/US2 ne démarre tant que Phase 2 n’est pas verte.

- [X] T002 [P] Confirmer dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` la présence et la stabilité des test tags utilisés par les contrats : `photo_preview_box` (région aperçu, état live), `photo_preview_placeholder` (région aperçu, état `CameraUnavailable`), `capture_photo_button`, `camera_tab_llm_test_button`. Ne pas renommer ; aucune ligne de production n’est modifiée à ce stade.
- [X] T003 [P] Introduire une constante de libellé top-level `private const val CapturePrimaryActionLabel = "Y a quoi là-dedans ?"` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` (ligne 53). **Note d’exécution** : finalement référencée directement dans `CaptureActionBar` (cf. T006/T011 fusionnés) — la constante est utilisée, pas simplement déclarée.

**Checkpoint**: Test tags vérifiés stables + constante de libellé disponible ⇒ US1 et US2 peuvent démarrer.

---

## Phase 3: User Story 1 — Aperçu caméra non recouvert (Priority: P1) 🎯 MVP

**Goal**: Restaurer la visibilité intégrale du flux vidéo de l’aperçu caméra — aucun bouton d’action persistant ne MUST recouvrir, même partiellement, la `PreviewRegion` (CR-FR-009, CR-FR-010). Bande d’action explicitement placée **en dessous** de l’aperçu.

**Independent Test**: lancer `CameraCaptureLayoutUiTest` étendu ; vérifier que `top(capture_photo_button) ≥ bottom(photo_preview_box)` et que le test échoue tant que la mise en page actuelle persiste (placement perçu comme recouvrant) puis passe après refactor.

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE**: ATDD — ces tests DOIVENT être écrits **et observés comme rouges** avant la moindre modification de la mise en page Compose.

- [X] T004 [P] [US1] Étendre `app/src/androidTest/java/com/miamia/camera/CameraCaptureLayoutUiTest.kt` avec un test `preview_andCaptureBar_doNotOverlap_inLivePreview` qui (a) atteint un état live preview, (b) récupère `getUnclippedBoundsInRoot()` pour `photo_preview_box`, `capture_photo_button` et `camera_tab_llm_test_button`, (c) assert strictement `bottom(photo_preview_box) ≤ top(capture_photo_button)` ET `bottom(photo_preview_box) ≤ top(camera_tab_llm_test_button)`, **renforcé par un seuil de séparation visuelle ≥ 16 dp** (couvre la perception « bouton recouvre l’aperçu » même si Compose place techniquement les enfants en stack vertical). Exécution effective : ATDD à valider côté poste avec émulateur.
- [X] T005 [P] [US1] Ajouter dans `app/src/androidTest/java/com/miamia/camera/CameraUnavailableLlmButtonUiTest.kt` un test `cameraUnavailable_capturePlaceholder_andCaptureBar_doNotOverlap` qui force `ScanState.CameraUnavailable("...")` via `debugOverrideScanStateForTests`, puis assert `bottom(photo_preview_placeholder) ≤ top(capture_photo_button)` avec seuil ≥ 16 dp. Exécution effective : ATDD à valider côté poste avec émulateur.

### Implementation for User Story 1

- [X] T006 [US1] Dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` (états live preview), `CaptureActionBar` **extrait** (déclaré lignes 418–450, invoqué ligne 387) avec `Spacer(Modifier.height(8.dp))` interne. Préserve `Arrangement.spacedBy(HomeSpacingRules.standardFixedSpacing)` parent (= 12 dp) ⇒ gap total ≥ 20 dp entre `photo_preview_box.bottom` et `capture_photo_button.top`. Test tags conservés.
- [X] T007 [US1] Dans la branche `ScanState.CameraUnavailable` (ligne 133), `CaptureActionBar` réutilisé sous la `Box(testTag = "photo_preview_placeholder")`. Le bouton « Réessayer » reste sous la bande d’action (ligne 139), hors de la `Box` placeholder. Gap total ≥ 20 dp (12 dp parent + 8 dp Spacer interne).
- [~] T008 [US1] **Différé** : exécution effective des tests sur émulateur/appareil. Vérification statique ✅ — le code respecte les invariants (extraction `CaptureActionBar`, gap ≥ 16 dp par construction, test tags inchangés). Commande à lancer côté poste : `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraCaptureLayoutUiTest`.

**Checkpoint**: T004 + T005 passent au vert ; `top(button) ≥ bottom(preview)` dans les deux états (live + Unavailable). Story 1 démontrable et testable indépendamment de US2.

---

## Phase 4: User Story 2 — Libellé « Y a quoi là-dedans ? » (Priority: P1)

**Goal**: L’action principale de capture porte exactement le libellé « Y a quoi là-dedans ? » dans tous les états affichant le bouton, en remplacement 1-pour-1 de « Prendre la photo » (CR-FR-011).

**Independent Test**: lancer `CaptureActionLabelUiTest` (nouveau) et `CameraUnavailableLlmButtonUiTest` (étendu) ; vérifier que le node `capture_photo_button` affiche exactement la chaîne `Y a quoi là-dedans ?` dans l’état `PreviewActive` et dans l’état `CameraUnavailable`.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T009 [P] [US2] Créé : `app/src/androidTest/java/com/miamia/camera/CaptureActionLabelUiTest.kt` — test `capture_photo_button_displays_y_a_quoi_la_dedans_in_live_preview` force `ScanState.PreviewActive` via `debugOverrideScanStateForTests` puis assert `hasText("Y a quoi là-dedans ?")` sur `capture_photo_button`.
- [X] T010 [P] [US2] Étendu : `CameraUnavailableLlmButtonUiTest.kt` — test `cameraUnavailable_capturePhotoButton_displaysYAQuoiLaDedansLabel` ajouté (assert libellé sur l’état `CameraUnavailable`).

### Implementation for User Story 2

- [X] T011 [US2] L’extraction `CaptureActionBar` (T006) ayant centralisé le bouton, le remplacement s’est fait à un seul endroit : `Text(CapturePrimaryActionLabel)` ligne 437 dans `CameraScreen.kt`. Comportement `onClick` inchangé.
- [X] T012 [US2] Vérifié : `rg "Prendre la photo" app/src/main/` ⇒ aucun résultat (chaîne disparue du code de production). « Reprendre la photo » (état `SegmentConfirmationRequired`) reste — libellé distinct, hors scope.

**Checkpoint**: T009 + T010 passent au vert ; les test tags restent inchangés ; la chaîne « Prendre la photo » a disparu de `app/src/main/`.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Traçabilité, validation manuelle, et préparation d’un suivi i18n.

- [X] T013 [P] `specs/domains/capture-recognition/traceability.csv` mis à jour : 9 nouvelles entrées (CR-FR-009..011, Constitution Check, Contrat, Research, Tasks, SC-CR-003..005).
- [X] T014 [P] `specs/domains/capture-recognition/migration-index.md` mis à jour : entrée incrément 020 (statut `validated` pour la couche specs/code ; exécution tests instrumentés différée hors environnement).
- [~] T015 **À exécuter par l’utilisateur** : scénarios D, E et F de `quickstart.md` sur émulateur portrait + capture d’écran post-implémentation. Non exécutable depuis cet environnement (pas d’émulateur).
- [X] T016 [P] Note de suivi documentée dans `research.md` (R-002) et explicitement laissée hors scope : ticket à créer pour extraire `CapturePrimaryActionLabel` vers `app/src/main/res/values/strings.xml` (`R.string.capture_action_primary`). Diff minimal préservé.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** : aucune dépendance, démarre immédiatement.
- **Foundational (Phase 2)** : dépend de Phase 1. **Bloque** US1 et US2.
- **US1 (Phase 3)** : dépend de Phase 2 ; **indépendant** d’US2 (touche le placement, pas le libellé).
- **US2 (Phase 4)** : dépend de Phase 2 ; **indépendant** d’US1 (touche le libellé, pas le placement). Si US1 a déjà extrait `CaptureActionBar` (T006), le changement de US2 (T011) se fait en un seul point ; sinon il se fait à deux endroits — pas de blocage.
- **Polish (Phase 5)** : dépend des deux user stories terminées (T013/T014/T015 référencent l’état post-merge ; T016 peut être planifié dès la fin de US2).

### User Story Dependencies

- **US1 (P1)** : démarre après Phase 2 ; pas de dépendance vers US2.
- **US2 (P1)** : démarre après Phase 2 ; pas de dépendance vers US1. Couplage doux : si exécuté **après** US1 sur la même branche, T011 modifie une seule occurrence dans `CaptureActionBar` ; sinon il modifie deux occurrences inline.

### Within Each User Story

- Tests d’acceptation MUST être rouges avant implémentation (ATDD, Principe II).
- Aucune nouvelle entité/modèle ⇒ pas d’ordre modèle→service.
- Refactor de structure (T006) **avant** l’ajustement final du placement (T007/T008).
- Implémentation centrale (T011) **après** les tests de libellé (T009/T010).

### Parallel Opportunities

- T002 ‖ T003 (Phase 2) — fichiers / activités distincts (vérification vs édition d’une constante).
- T004 ‖ T005 (Phase 3 / Tests US1) — deux fichiers de test distincts.
- T009 ‖ T010 (Phase 4 / Tests US2) — deux fichiers de test distincts.
- T013 ‖ T014 ‖ T016 (Phase 5) — fichiers et nature distincts (traceability, migration-index, ticket de suivi).
- US1 et US2 peuvent être traitées en parallèle par deux développeurs **si** US1 livre T006 (extraction `CaptureActionBar`) avant que US2 entre dans T011 — sinon US2 fait l’édition à deux endroits dans `CameraScreen.kt`.

---

## Parallel Example: User Story 1 (tests ATDD)

```bash
# Lancer les deux tests rouges en parallèle :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraCaptureLayoutUiTest &

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraUnavailableLlmButtonUiTest &

wait
```

## Parallel Example: User Story 2 (tests ATDD)

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CaptureActionLabelUiTest &

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraUnavailableLlmButtonUiTest &

wait
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 (T001) — sanity build.
2. Phase 2 (T002, T003) — préparation transverse.
3. Phase 3 (T004 → T008) — non-recouvrement aperçu / bande d’action.
4. **STOP et valider** : MVP utilisable (l’aperçu redevient pleinement exploitable, qualité OCR restaurée), même si le libellé est encore « Prendre la photo ».

### Incremental Delivery

1. Setup + Foundational ✅ → infrastructure prête.
2. US1 ✅ → premier livrable (aperçu non recouvert) — démontrable seul.
3. US2 ✅ → libellé conforme — démontrable seul (ou cumulé avec US1).
4. Polish ✅ → traçabilité + validation manuelle + suivi i18n planifié.

### Parallel Team Strategy

À deux développeurs :

1. Dev A : Phase 2 (T002 + T003) puis US1 (T004 → T008).
2. Dev B : Phase 2 terminée ⇒ US2 (T009 + T010, puis T011 dès que T006 est mergé).
3. Polish (T013–T016) partageable entre les deux après T012.

---

## Notes

- `[P]` = fichiers / activités disjointes, exécutables en parallèle.
- `[Story]` = traçabilité utilisateur (US1, US2) ; absent pour Setup / Foundational / Polish.
- ATDD : les tests T004, T005, T009, T010 **doivent être rouges** avant les tâches d’implémentation correspondantes.
- Commit recommandé par tâche ou par groupe logique (test rouge → impl → test vert).
- Stop possible après tout checkpoint pour valider une story indépendamment.
- À éviter : modifications inline de `Text("Prendre la photo")` sans constante T003 (régression de maintenabilité) ; modification des test tags existants (casse des tests aval) ; déplacement d’un bouton **dans** la `Box` de `photo_preview_box` (viole CR-FR-009).
