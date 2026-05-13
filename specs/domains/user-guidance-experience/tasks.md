# Tasks : Feature G — OCR direct, accueil sans chip ni ligne « prêt à scanner »

**Input** : `specs/domains/user-guidance-experience/` (plan.md, spec.md Feature G, research.md, data-model.md, contracts/, quickstart.md)  
**Prerequisites** : plan.md, spec.md  
**Tests** : Constitution ATDD — tests instrumentés / assertions UI mises à jour pour UGE-G-SC-002.

## Format

`- [ ] [TaskID] [P?] [Story?] Description avec chemin de fichier`

---

## Phase 1 : Setup

**Purpose** : cartographier le code à retirer ou à simplifier.

- [X] T001 Inventorier `SegmentConfirmationRequired`, `ingredients_framing_tag_chip`, `ingredientsFramingTagActive`, `CapturePreviewReadyStatusLabel`, `confirm_segment_button` dans `app/src/main/java/com/miamia/camera/` et `app/src/androidTest/java/com/miamia/camera/`

---

## Phase 2 : Fondations (tests / contrats avant merge)

**Purpose** : tests rouges → verts sur la chrome Feature G.

- [X] T002 [US2] Mettre à jour `app/src/androidTest/java/com/miamia/camera/CaptureScreenFeatureFUiTest.kt` : conserver assertions absence `camera_tab_llm_test_button` / « Aperçu caméra actif » ; remplacer l’exigence de texte « Caméra »+« scanner » par assertions UGE-G-SC-002 (`ingredients_framing_tag_chip` count 0 ; chaîne exacte « Caméra prête — vous pouvez scanner » count 0 ; `capture_scan_status_text` absent ou non affiché en `PreviewActive`)

---

## Phase 3 : User Story 1 (US-G1) — Enchaînement direct OCR → analyse (P1)

**Goal** : UGE-G-FR-001, UGE-G-FR-004, UGE-G-SC-001.

**Independent Test** : parcours manuel quickstart G2 ; absence `confirm_segment_button` après capture nominale.

- [X] T003 [US1] Dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`, après OCR `success`/`partial`, appeler le gate avec `implicitValidationFromIngredientsFraming = true` ; supprimer toute émission de `ScanState.SegmentConfirmationRequired` ; enchaîner `confirmSegmentAndAnalyze()` lorsque `submissionAllowed` ; retirer `rejectSegmentConfirmation()` et les champs `ingredientsFramingTagActive` / `setIngredientsFramingTagActive` / reset associé dans `onRetry`
- [X] T004 [US1] Retirer `data class SegmentConfirmationRequired` de `app/src/main/java/com/miamia/camera/ScanState.kt` et adapter les `when` exhaustifs si nécessaire

---

## Phase 4 : User Story 2 (US-G2) — Accueil épuré (P1)

**Goal** : UGE-G-FR-002, UGE-G-FR-003, UGE-G-SC-002.

**Independent Test** : `CaptureScreenFeatureFUiTest` + quickstart G1.

- [X] T005 [US2] Dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`, supprimer le `FilterChip` « Balise ingrédients », retirer la collecte `ingredientsFramingTagActive`, supprimer la branche UI `SegmentConfirmationRequired`, ne pas afficher de ligne de statut sous l’aperçu pour `PreviewActive` (conserver libellés utiles pour états transitoires hors chaîne interdite) ; retirer imports inutilisés (`FilterChip`, `Row` si orphelin)

---

## Phase 5 : Polish & traçabilité

- [X] T006 [P] Vérifier `rg "SegmentConfirmationRequired|ingredients_framing_tag_chip|Balise ingrédients|Caméra prête — vous pouvez scanner" app/src/main/java/com/miamia/camera/` → 0 occurrence résiduelle hors commentaires de migration si autorisés
- [X] T007 Confirmer cohérence avec `specs/domains/user-guidance-experience/contracts/capture-screen-feature-g-direct-scan.md` et scénarios G1–G2 du `specs/domains/user-guidance-experience/quickstart.md`

---

## Dependencies & Execution Order

| Phase | Dépend de | Bloque |
|-------|-----------|--------|
| 1 | — | — |
| 2 | T001 (recommandé) | validation CI tests |
| 3 US-G1 | T002 rédigé (ATDD strict) ou parallèle après T001 | T004 ordre avec T003 |
| 4 US-G2 | T003–T004 (ViewModel + ScanState) | T005 peut suivre T003–T004 |
| 5 | 3 + 4 | — |

**Ordre conseillé** : T001 → T002 → T003 → T004 → T005 → T006 → T007.

---

## Parallel Example

```bash
# Après impl (wrapper Gradle fonctionnel) :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CaptureScreenFeatureFUiTest
```

---

## Implementation Strategy

**MVP** : Phases 3–4 (US-G1 + US-G2).  
**Polish** : Phase 5.

## Notes

- `AnalysisSubmissionGate` reste inchangé : les tests JVM `AnalysisSubmissionGateContractTest` / `AnalysisSubmissionDecisionAcceptanceTest` valident toujours les combinaisons `userConfirmed` / `implicit`.
- Le domaine `ingredient-normalization-validation` peut aligner specs secondaires (segmentation retirée) dans un lot ultérieur.
