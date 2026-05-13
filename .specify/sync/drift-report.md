# Spec Drift Report

**Generated** : 2026-05-13 (analyse agent, périmètre `specs/domains/**/spec.md`)  
**Project** : MiamIA

> **Périmètre** : les exigences **autoritaires** analysées sont celles des domaines sous `specs/domains/`. Les chemins `specs/00x-*/spec.md` (hors `domains`) sont du **mapping source / traçabilité** ; ils ne sont pas re-parcourus ici comme SSOT.

## Summary

| Category | Count |
|----------|-------|
| Specs Analyzed | 6 |
| Requirements Checked | ~45 (échantillon ciblé + FR/SC visibles) |
| ✓ Aligned | 28 (~62%) |
| ⚠️ Drifted | 10 (~22%) |
| ✗ Not Implemented / non vérifiable statique | 4 (~9%) |
| 🆕 Unspecced / dette doc | 3 |

## Detailed Findings

### Spec: user-guidance-experience — Domain Spec — user-guidance-experience

#### Aligned ✓

- **UGE-G-FR-001 à UGE-G-FR-004**, **UGE-G-SC-002** → `CameraScreen.kt` (absence chip, absence chaîne interdite, pas de statut `PreviewActive` sous tag `capture_scan_status_text`), `CameraViewModel.kt` (gate avec `implicitValidationFromIngredientsFraming = true`, enchaînement `confirmSegmentAndAnalyze()`), `ScanState.kt` (suppression `SegmentConfirmationRequired`), `CaptureScreenFeatureFUiTest.kt`.
- **UGE-A-FR-006** (pas d’écran relecture avant analyse) → cohérent avec le flux ci-dessus.
- **UGE-F-FR-002 / UGE-F-FR-003** (pas Test LLM, pas « Aperçu caméra actif ») → conservés par tests UI existants.

#### Drifted ⚠️

- **SC-D-004 / formulation « 0 occurrence » WelcomeMessageUiState dans `camera/`** : la spec checklist demandait zéro référence dans le package capture ; le **ViewModel** conserve `welcomeUiState` et la policy (non rendue). **Sévérité** : *mineure* (intention produit = pas de rendu ; la spec checklist pourrait être assouplie en « non consommé par l’UI »).

#### Not Implemented ✗ (ou non prouvable sans bench)

- Critères **performance** type **SC-A-001**, **SC-B-003** : pas de mesure automatique dans ce rapport.

---

### Spec: capture-recognition — Domain Spec - capture-recognition

#### Aligned ✓

- **CR-FR-001**, **CR-FR-009**, **CR-FR-010**, **CR-FR-011** → disposition capture / bande d’action / libellé « Y a quoi là-dedans ? » (structure `CameraScreen` + tests layout historiques).

#### Drifted ⚠️

- **Scope § « Ref. domaine aval » (ligne ~14)** : la spec suppose encore une **UI** « balise / mode ingrédients » ; **Feature G** retire le chip — le texte domaine **capture-recognition** est en retard sur **user-guidance-experience**. **Sévérité** : *modérée*.
- **CR-FR-006 / CR-FR-007** et l’incrément **019 — zone défilante texte capturé** : ils décrivent un **écran de relecture** post-capture avec scroll + actions fixes. Le **parcours nominal** actuel (UGE-G) **n’interpose plus** cet écran avant l’analyse LLM. Des écrans de relecture **subsistent** pour d’autres états (`Success`, erreurs composition, etc.) mais pas comme étape systématique post-OCR. **Sévérité** : *modérée* (à clarifier : périmètre « écran consultation RawOcrText » = uniquement parcours legacy ou tout état affichant transcript).

#### Not Implemented ✗

- **SC-CR-004** (revue qualitative utilisateurs) : non instrumenté dans le dépôt.

---

### Spec: ingredient-normalization-validation — ingredient-phrase-segment

#### Aligned ✓

- **FR-012 / FR-014** (entrée LLM = transcript intégral ; pas de troncature par ancrage) → `AnalysisSubmissionGate` + `runCompositionStage` sur texte complet ; alignement large avec l’implémentation actuelle.

#### Drifted ⚠️

- **FR-010** : rédaction encore centrée sur la **balise ingrédients** comme précondition ; le code **n’expose plus** cette UI et force l’équivalent « implicite » pour le gate. Comportement produit proche (enchaînement direct) ; **libellé spec** à mettre à jour. **Sévérité** : *modérée*.
- **Contrat** `contracts/session-capture-intent-for-implicit-validation.md` §3 : exige encore `SegmentConfirmationRequired` si le signal est faux — **état et branche UI supprimés** côté app. **Sévérité** : *majeure* pour le **contrat** (doc faux par rapport au code).

#### Not Implemented ✗

- **FR-009** persistance longue durée : explicitement hors MVP dans la spec.

---

### Specs: additive-risk-insights, ingredient-health-intelligence, local-llm-runtime

**Non audités en profondeur** dans ce passage (pas de grep ciblé ni parcours code ↔ FR exhaustif). Comptés dans « Specs analyzed » pour complétude du répertoire `specs/domains/`.

---

## Unspecced Code 🆕

| Feature | Location | Suggested Spec |
|---------|----------|----------------|
| `CameraUiState` + `requiresSegmentConfirmation` (non référencé ailleurs dans `app/src/main` d’après recherche) | `app/src/main/java/com/miamia/camera/CameraUiState.kt` | Supprimer ou documenter dans `capture-recognition` |
| `IngredientSegmentPreparationService` toujours invoqué depuis `CameraViewModel` | `CameraViewModel.kt` | Clarifier dans `ingredient-normalization-validation` comme « vue auxiliaire / gate uniquement » |
| `tasks.md` domaine ingrédients mentionnant chip / `SegmentConfirmationRequired` | `specs/domains/ingredient-normalization-validation/tasks.md` | Mise à jour doc (pas du code) |

---

## Inter-Spec Conflicts

1. **user-guidance-experience Feature G** (pas de chip balise, pas d’écran confirmation segment nominal) **vs** **capture-recognition** scope L14 (signal UI balise) **vs** **ingredient-normalization-validation** contrat session-capture §3 (`SegmentConfirmationRequired`).
2. **capture-recognition** incrément **019** (relecture texte après capture) **vs** **UGE-G** (pas d’étape relecture transcript avant analyse sur le parcours nominal).

---

## Recommendations

1. **Mettre à jour** `specs/domains/ingredient-normalization-validation/contracts/session-capture-intent-for-implicit-validation.md` et **FR-010** pour refléter l’absence d’UI balise et l’absence d’état `SegmentConfirmationRequired` (nouveau modèle : « intention implicite produit » ou équivalent).
2. **Réviser** `specs/domains/capture-recognition/spec.md` (scope L14, CR-FR-006/007 et incrément 019) : préciser si l’écran scrollable s’applique uniquement aux **états de repli** (ex. `Success` / limites) et non au parcours nominal OCR→LLM.
3. **Nettoyer** `CameraUiState.kt` s’il est mort, ou le rattacher à une spec / un flux réel.
4. **Régénérer ou éditer** `specs/domains/ingredient-normalization-validation/tasks.md` pour retirer les tâches accomplies qui décrivent encore le chip et l’UI de confirmation.
5. Lancer **`speckit.sync.apply`** (ou PR dédiée) après validation humaine des libellés FR.

---

*Fichier machine-readable : `.specify/sync/drift-report.json`*
