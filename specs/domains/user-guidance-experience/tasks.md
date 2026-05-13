# Tasks : Feature F — libellés capture, fin test LLM, retrait « Aperçu caméra actif »

**Input** : `specs/domains/user-guidance-experience/` (plan.md, spec.md Feature F, research.md, data-model.md, contracts/, quickstart.md)  
**Prerequisites** : plan.md, spec.md  
**Tests** : ATDD obligatoire (constitution) — tests AndroidTest / JVM mis à jour ou ajoutés avant/après implémentation selon le flux.

## Format

`- [ ] [TaskID] [P?] [Story?] Description avec chemin de fichier`

---

## Phase 1 : Setup

**Purpose** : cadrage périmètre code existant (aucune infra nouvelle).

- [X] T001 Inventorier les références à `camera_tab_llm_test_button`, `runCameraTabLlmMockTest`, `canRunCameraTabLlmTest`, `homeLlmRunner` dans `app/src/main/java/com/miamia/camera/`, `app/src/main/java/com/miamia/MainActivity.kt`, `app/src/test/java/com/miamia/camera/`, `app/src/androidTest/java/com/miamia/camera/`

---

## Phase 2 : Fondations (tests ATDD — à faire échouer puis passer)

**Purpose** : garde-fous Compose / JVM avant retouches production.

- [X] T002 [US1] Étendre `app/src/androidTest/java/com/miamia/camera/CameraCaptureLayoutUiTest.kt` : supprimer toute dépendance à `camera_tab_llm_test_button` ; conserver assertions non-recouvrement `photo_preview_box` vs `capture_photo_button` (≥ 16 dp)
- [X] T003 [US2] Réécrire `app/src/androidTest/java/com/miamia/camera/CameraUnavailableLlmButtonUiTest.kt` : retirer assertions sur le bouton LLM ; conserver message indisponible + non-recouvrement placeholder vs `capture_photo_button` + libellé « Y a quoi là-dedans ? »
- [X] T004 [P] [US1] Ajouter `app/src/androidTest/java/com/miamia/camera/CaptureScreenFeatureFUiTest.kt` : forcer `ScanState.PreviewActive` via `CameraViewModel.debugOverrideScanStateForTests`, assert `onAllNodesWithTag("camera_tab_llm_test_button").assertCountEquals(0)`, assert aucun texte « Aperçu caméra actif », assert présence d’un libellé explicite (contient « Caméra » et « scanner »)
- [X] T005 [US2] Supprimer ou remplacer `app/src/test/java/com/miamia/camera/CameraLlmTestButtonTest.kt` (tests obsolètes `canRunCameraTabLlmTest` / runner injecté)

**Checkpoint** : `./gradlew :app:testDebugUnitTest` et cible AndroidTest capture passent après implémentation.

---

## Phase 3 : User Story 1 (US-F1 + US-F3) — Libellés explicites, fin « Aperçu caméra actif » (P1)

**Goal** : UGE-F-FR-001, UGE-F-FR-003 ; statut `PreviewActive` + MediaPipe sans ambiguïté.

**Independent Test** : `CaptureScreenFeatureFUiTest` + inspection manuelle quickstart F1.

- [X] T006 [US1] Remplacer le libellé « Disponible » dans `app/src/main/java/com/miamia/home/MediaPipeStatusViewState.kt` (`MediaPipeStatusViewState.available()`) par une formulation explicite (≠ seul mot « Disponible »)
- [X] T007 [US1] Dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`, remplacer la branche `ScanState.PreviewActive -> "Aperçu caméra actif"` par un libellé explicite aligné spec (ex. « Caméra prête — vous pouvez scanner ») ; ajouter `Modifier.testTag("capture_scan_status_text")` sur le `Text` de statut si utile aux tests

---

## Phase 4 : User Story 2 (US-F2) — Suppression Test LLM (P1)

**Goal** : UGE-F-FR-002 ; plus de bouton, plus de méthode ViewModel, factory sans runner côté capture.

**Independent Test** : AndroidTest sans nœud `camera_tab_llm_test_button` ; capture photo inchangée.

- [X] T008 [US2] Simplifier `CaptureActionBar` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : retirer `OutlinedButton` « Test LLM », paramètres `onRunLlmTest` / `canRunLlmTest` ; mettre à jour les deux appels (`CameraUnavailable` + branche preview)
- [X] T009 [US2] Retirer `homeLlmRunner`, `cameraTabLlmTestInFlight`, `canRunCameraTabLlmTest()`, `runCameraTabLlmMockTest()` et imports associés dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` ; ajuster `canCapturePhoto()` (ne plus bloquer sur l’état mock LLM)
- [X] T010 [US2] Retirer la création de `CompositionEngineHomeLlmMockRunner` et l’argument `homeLlmRunner` dans `app/src/main/java/com/miamia/MainActivity.kt` ; appeler `CameraViewModel.factory(application, coordinator, compositionEngine)` sans 4e paramètre
- [X] T011 [US2] Mettre à jour `CameraViewModel.factory` dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (signature sans `HomeLlmMockRunner`)

---

## Phase 5 : Polish & domaine voisin

- [X] T012 [P] Vérifier qu’aucune occurrence résiduelle de `camera_tab_llm_test_button` ou « Aperçu caméra actif » dans `app/src/main/` et `app/src/androidTest/` (grep)
- [X] T013 Valider manuellement les scénarios F1 du `specs/domains/user-guidance-experience/quickstart.md`

---

## Dependencies & Execution Order

| Phase | Dépend de | Bloque |
|-------|-----------|--------|
| 1 Setup | — | — |
| 2 Tests | T001 (recommandé) | Phases 3–4 jusqu’à mise à jour code |
| 3 US-F1/US-F3 | Phase 2 tests rédigés (T002–T004) | — |
| 4 US-F2 | T008–T011 (même fichiers que partie UI) | exécuter après ou avec T006–T007 pour limiter conflits : **ordre conseillé T006→T007→T008→T009→T010→T011** |
| 5 Polish | 3 + 4 | — |

**Parallèle** : T004 [P] peut être rédigé en parallèle de T002–T003 si fichiers distincts ; T006 et imports `MainActivity` séquentiels après refactoring ViewModel.

### Ordre d’implémentation conseillé (fichiers partagés)

1. T006, T007 (libellés)  
2. T008–T011 (retrait LLM + factory)  
3. T002–T005 (tests)  
4. T012–T013  

*(Les tests T002–T005 peuvent être écrits en premier en ATDD strict : rouges jusqu’à T008–T011 faits.)*

---

## Parallel Example (US-F2)

```bash
# Après impl, une seule cible pour valider la suite capture :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CameraCaptureLayoutUiTest,com.miamia.camera.CameraUnavailableLlmButtonUiTest,com.miamia.camera.CaptureScreenFeatureFUiTest
```

---

## Implementation Strategy

**MVP** : Phases 1–4 (US-F1 + US-F3 + US-F2) — livrable utilisateur complet Feature F.  
**Polish** : Phase 5.

## Notes

- Le package `app/src/main/java/com/miamia/home/HomeLlmMockRunner.kt` reste utilisé par `HomeViewModel` ; ne pas supprimer le fichier tant que `HomeScreen` / tests `home/` en dépendent.
- Contrat domaine voisin déjà révisé : `specs/domains/capture-recognition/contracts/capture-action-bar.md`.
