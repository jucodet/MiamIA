# Tasks: Zone défilante texte capturé (019-captured-text-scroll)

**Input** : artefacts dans `specs/domains/capture-recognition/`  
**Prerequisites** : `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests** : ATDD obligatoire (template projet) — au moins un test instrumenté par user story, rédigé en échec attendu avant implémentation ciblée.

**Organization** : phases par user story (`spec.md` : US1 P1, US2 P2).

## Format: `[ID] [P?] [Story] Description`

- **[P]** : parallélisable (fichiers distincts, pas de dépendance sur tâche non terminée du même fichier bloquant)
- **[Story]** : `US1`, `US2` — uniquement phases user story
- Chemins fichiers explicites

---

## Phase 1: Setup (préparation)

**Purpose** : alignement sur le contrat et le plan avant code.

- [X] T001 Lire `specs/domains/capture-recognition/contracts/ui-raw-transcript-review-surface.md`, `research.md` et la section incrément de `specs/domains/capture-recognition/spec.md` (CR-FR-006 à CR-FR-008)

---

## Phase 2: Foundational (prérequis bloquant)

**Purpose** : composable de mise en page partagé pour toutes les user stories — sans quoi les branches `ScanState` ne peuvent pas appliquer CR-FR-006/007 de façon homogène.

**⚠️ CRITICAL** : aucune tâche US1/US2 avant T002 terminée.

- [X] T002 Ajouter dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` un composable privé (nom suggéré : `ScrollableReviewWithPrimaryActions`) qui applique le pattern documenté dans `research.md` : `Column(Modifier.fillMaxSize())` ou équivalent borné, zone centrale `Modifier.weight(1f, fill = true).verticalScroll(rememberScrollState())` pour le contenu, et slot actions **hors** scroll ; référence de style `app/src/main/java/com/miamia/result/LlmResultScreen.kt`

**Checkpoint** : helper disponible — démarrer US1 (tests + branchements).

---

## Phase 3: User Story 1 — Parcours texte long (Priority: P1) 🎯 MVP

**Goal** : texte OCR / segment très long entièrement consultable par scroll interne ; boutons d’action principaux toujours visibles en bas (CR-FR-006, CR-FR-007, SC-CR-001).

**Independent Test** : test instrumenté + scénario A / C du `quickstart.md`.

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE** : écrire en premier, échec attendu avant branchement complet des états.

- [X] T003 [US1] Ajouter `app/src/androidTest/java/com/miamia/camera/CapturedTextScrollUiTest.kt` avec `createAndroidComposeRule<ComponentActivity>()`, `CameraViewModel` via `ApplicationProvider`, `debugOverrideScanStateForTests(ScanState.Success(transcriptText = <chaîne multi-lignes très longue>, items = listOf("ligne")))` puis `setContent { CameraScreen(...) }` (lambdas factices acceptables) ; assert zone scroll via `testTag` (ex. `captured_review_scroll`) et `onNodeWithTag("new_scan_button").assertIsDisplayed()` sans scroll global sur le bouton

### Implementation for User Story 1

- [X] T004 [US1] Branch `is ScanState.Success` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : placer titre + transcript + items dans la zone scroll du helper T002 ; bouton « Nouveau scan » (`testTag` `new_scan_button`) dans la zone actions fixe ; ajouter `Modifier.testTag("captured_review_scroll")` sur le conteneur scroll
- [X] T005 [US1] Branch `is ScanState.BilanReady` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : supprimer `verticalScroll` sur le `Column` englobant toute la carte ; envelopper `BilanResultCard` dans la zone scroll du helper T002 ; conserver le bouton « Nouveau scan » dans la zone actions fixe
- [X] T006 [US1] Branch `is ScanState.SegmentConfirmationRequired` dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` : faire occuper à la zone texte la hauteur restante via `weight(1f)` sur le conteneur scroll (retirer ou ajuster `heightIn(max = 280.dp)` si contradictoire avec remplissage flexible) ; conserver `testTag` `segment_preview_scroll` / `segment_preview_text` ; boutons `confirm_segment_button` et `reject_segment_button` hors scroll

**Checkpoint** : US1 testable seul ; T003 passe.

---

## Phase 4: User Story 2 — Parcours texte court (Priority: P2)

**Goal** : texte court lisible sans scroll forcé inutile ; boutons visibles (CR-FR-008, SC-CR-002).

**Independent Test** : test instrumenté texte court + revue visuelle rapide.

### Tests for User Story 2 (MANDATORY) ⚠️

- [X] T007 [P] [US2] Étendre `app/src/androidTest/java/com/miamia/camera/CapturedTextScrollUiTest.kt` : `ScanState.Success` avec `transcriptText` court (1–2 lignes) ; `onNodeWithTag("new_scan_button").assertIsDisplayed()` ; assert contenu `transcriptText` visible (`assertTextContains` ou équivalent)

### Implementation for User Story 2

- [X] T008 [US2] Ajuster contraintes du helper / branches dans `app/src/main/java/com/miamia/camera/CameraScreen.kt` pour éviter troncature du texte court et hauteur fantôme disproportionnée (CR-FR-008) ; valider `SegmentConfirmationRequired` avec `segmentPreview` court

**Checkpoint** : US1 + US2 indépendamment verts.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose** : non-régression, validation manuelle, messages longs hors parcours principal.

- [ ] T009 Exécuter les scénarios A–C de `specs/domains/capture-recognition/quickstart.md` sur émulateur ou appareil et corriger les écarts éventuels dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`
- [X] T010 [P] Si `ScanState.GemmaUnavailable` ou `ScanState.CompositionLimit` affiche un `Text` potentiellement très long au-dessus des boutons dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`, appliquer le même schéma scroll / pied fixe pour éviter régression UX symétrique

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → **Phase 2** → **Phases 3–4** (US1 puis ou en série avec US2 après fondation) → **Phase 5**

### User Story Dependencies

- **US1 (P1)** : après T002 ; bloque la démo MVP.
- **US2 (P2)** : après T002 ; idéalement après T004–T006 pour réutiliser tags et helper stabilisé (T007–T008 peuvent suivre T006).

### Within US1

- T003 (test) avant ou en lockstep avec T004 : ajouter les `testTag` nécessaires au plus tard dans T004 pour que T003 compile et échoue puis passe.
- T004 → T005 → T006 ordre recommandé (même fichier, éviter conflits git).

### Parallel Opportunities

- **T007 [P] [US2]** : peut être rédigé en parallèle de T005–T006 **si** les `testTag` US1 sont déjà posés dans T004 (fichier de test distinct du cœur des grosses modifs `BilanReady`).
- **T010 [P]** : fichier même `CameraScreen.kt` — séquentiel après T006–T008 pour limiter les conflits de merge.

---

## Parallel Example: User Story 1

```bash
# Après T002 : implémenter T004 puis faire passer T003, ou itérer T003 ↔ T004 jusqu’au vert.
# Pas de lancement massif parallèle sur le même fichier CameraScreen.kt.
```

---

## Implementation Strategy

### MVP First (User Story 1 seule)

1. T001 → T002  
2. T003 + T004–T006 jusqu’au vert de T003  
3. **STOP** : démo MVP (texte long + boutons visibles)

### Incremental Delivery

1. Ajouter US2 (T007–T008)  
2. Polish T009–T010

### Parallel Team Strategy

- Dev A : T002 + T004–T006  
- Dev B : préparer T007 en attente des tags (après T004)

---

## Notes

- `debugOverrideScanStateForTests` : `app/src/main/java/com/miamia/camera/CameraViewModel.kt`  
- Ne pas étendre le périmètre aux écrans hors route capture sauf T010 justifié par régression UX.
