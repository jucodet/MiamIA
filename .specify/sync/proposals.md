# Drift Resolution Proposals

**Generated** : 2026-05-13  
**Based on** : `.specify/sync/drift-report.json` (2026-05-13T12:00:00Z)  
**Note** : une partie des dérives **capture-recognition / ingredient / contrat session-capture** a déjà été traitée par **`speckit-sync-apply`** (2026-05-13). Les propositions **PR-VERIFY-*** invitent à **relancer `speckit.sync.analyze`** puis clôturer ou rouvrir des items.

## Summary

| Resolution Type | Count |
|-----------------|-------|
| Backfill (Code → Spec) | 2 |
| Align (Spec → Code) | 1 |
| Human Decision | 4 |
| Verify / Close (post–sync-apply) | 3 |
| Defer (spec housekeeping) | 1 |
| Remove (méta sync) | 1 |

---

## Proposals

### Proposal PR-001 : `user-guidance-experience` / SC-D-004

**Direction** : **BACKFILL**

**Current State** :
- **Spec dit** (`spec.md`) : `rg … WelcomeMessageUiState … app/.../camera/` ⇒ 0 occurrence.
- **Code fait** : `CameraViewModel` expose `welcomeUiState` (types `WelcomeMessageUiState` dans la signature du ViewModel).

**Proposed Resolution** :

- Remplacer **SC-D-004** (et toute formulation équivalente dans les checklists) par l’alignement déjà présent dans `contracts/capture-screen-no-welcome-banner.md` §3 : **interdiction de consommation UI** sur l’écran capture ; **autorisation** du flow `CameraViewModel.welcomeUiState` tant qu’aucun composable capture ne projette `Displayed` en texte visible.
- Critère de vérif : `rg welcome_message_banner app/.../camera/` ⇒ 0 **et** absence de `collectAsState` / branche rendue pour welcome sur `CameraScreen`.

**Rationale** : le contrat domaine et `research.md` (Feature D) sont la source de vérité ; le critère « 0 occurrence du nom de type » est trop strict et crée une fausse dérive.

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-002 : `capture-recognition` / Scope + CR-FR-006–007 (post–sync-apply)

**Direction** : **VERIFY** *(documentation déjà mise à jour — valider puis fermer)*

**Current State** : le drift-report mentionnait encore l’ancienne ligne scope « UI balise » et l’ambiguïté 019 vs nominal UGE-G.

**Proposed Resolution** :

- Relire `specs/domains/capture-recognition/spec.md` après `speckit-sync-apply` ; si cohérent avec le code, **regénérer `drift-report`** et marquer cette entrée comme résolue.

**Rationale** : éviter de redoubler les edits ; valider l’état actuel.

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-003 : `ingredient-normalization-validation` / FR-010 + contrat session (post–sync-apply)

**Direction** : **VERIFY**

**Proposed Resolution** : confirmer que `spec.md` (FR-010, US2b) et `contracts/session-capture-intent-for-implicit-validation.md` reflètent l’implémentation (`implicitValidationFromIngredientsFraming`, pas de `SegmentConfirmationRequired` nominal).

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-004 : `ingredient-normalization-validation` / `IngredientSegmentPreparationService` dans le flux capture

**Direction** : **HUMAN_DECISION**

**Current State** :
- **Spec / intention produit** : segmentation ne doit pas filtrer l’entrée LLM (FR-012, FR-014).
- **Code fait** : `CameraViewModel` appelle encore `prepare()` pour alimenter le gate / traçabilité.

**Options** :
- **A** — Conserver `prepare()` uniquement pour vues auxiliaires / logs ; documenter explicitement dans `plan.md` ou `research.md` du domaine ingrédients.
- **B** — Simplifier le gate pour ne plus dépendre de `IngredientSegmentExtraction` sur le chemin nominal (refactor plus large).

**Rationale** : les deux sont valides ; impact effort différent.

**Confidence** : MEDIUM

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-005 : Unspecced — `CameraUiState.kt`

**Direction** : **ALIGN** (Spec → Code : nettoyage)

**Proposed Resolution** : supprimer `app/src/main/java/com/miamia/camera/CameraUiState.kt` s’il n’existe aucune référence dans `app/src/main` ; sinon documenter une seule référence légitime dans `capture-recognition` ou UGE.

**Rationale** : fichier orphelin = dette ; pas de FR domaine qui l’exige.

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-006 : `user-guidance-experience` / critères performance (SC-A-001, SC-B-003, …)

**Direction** : **HUMAN_DECISION**

**Proposed Resolution** :

- **Option A** : marquer ces SC comme **« validation manuelle / bench périodique »** (hors CI statique).
- **Option B** : ajouter des tests instrumentés de perf (coût élevé).

**Rationale** : le drift signalait « non vérifiable en statique » — choix produit / QA.

**Confidence** : MEDIUM

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-007 : `capture-recognition` / SC-CR-004

**Direction** : **HUMAN_DECISION** *(ou BACKFILL léger)*

**Proposed Resolution** : conserver le SC comme critère **qualitatif** explicite ; ajouter une note « hors CI automatisé sauf campagne utilisateur ».

**Confidence** : LOW

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-008 : `ingredient-normalization-validation` / FR-009 persistance

**Direction** : **DEFER** *(backfill statut dans la spec)*

**Proposed Resolution** : confirmer dans `spec.md` que la persistance Room reste **hors MVP** et que l’absence d’implémentation n’est pas une dérive bloquante.

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-009 : Doc — `ingredient-normalization-validation/tasks.md` (historique chip)

**Direction** : **BACKFILL** *(housekeeping)*

**Proposed Resolution** : archiver les phases US2/US2b obsolètes dans une sous-section « Historique pré–Feature G » ou régénérer `tasks.md` depuis le `spec.md` actuel (Speckit tasks).

**Rationale** : les cases `[x]` avec texte faux pour l’état courant induisent en erreur.

**Confidence** : MEDIUM

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-010 : Conflit inter-specs (drift-report `conflicts[]`)

**Direction** : **VERIFY**

**Proposed Resolution** : après relecture des specs mises à jour (UGE-G, capture, ingrédient), **relancer `speckit.sync.analyze`** ; si `conflicts` est vide, clôturer.

**Confidence** : MEDIUM

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-011 : Domaines non audités (`additive-risk-insights`, `ingredient-health-intelligence`, `local-llm-runtime`)

**Direction** : **NEW_SPEC** *(optionnel)* / **HUMAN**

**Proposed Resolution** : planifier un cycle **`speckit.sync.analyze`** dédié par domaine ; pas de proposition de texte FR dans ce lot sans analyse ciblée.

**Confidence** : LOW

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

### Proposal PR-012 : Nettoyage `proposals.json` historique (P1–P16)

**Direction** : **REMOVE** *(métadonnées sync)*

**Proposed Resolution** : remplacer l’ancien fichier par ce lot **PR-001…** une fois approuvé ; conserver une copie `proposals.archive-2026-05-12.json` si besoin de traçabilité.

**Rationale** : éviter une nouvelle application accidentelle de propositions obsolètes.

**Confidence** : HIGH

**Action** :
- [ ] Approve
- [ ] Reject
- [ ] Modify

---

## Mode interactif (`--interactive`)

Pour chaque proposition, le flux recommandé est :

`[A]pprove / [R]eject / [M]odify / [S]kip / [Q]uit`

Puis mettre à jour `approved: true` dans `.specify/sync/proposals.json` pour les IDs acceptés avant **`speckit-sync-apply`**.
