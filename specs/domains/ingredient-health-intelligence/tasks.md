# Tasks : Anti-hallucination & ancrage LLM (Feature C)

**Input** : `specs/domains/ingredient-health-intelligence/` — `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`  
**Branche** : `020-forbid-llm-hallucination`

**Cartographie user stories → libellés tâches**

| Libellé tâche | Spec |
|----------------|------|
| **US1** | Feature C — **US-C1** (P1) Confiance : pas d’ingrédient inventé (+ scénario KPI **IHI-C-FR-007**) |
| **US2** | Feature C — **US-C2** (P2) Refus explicite / tout ou rien |
| **US3** | Feature C — **US-C3** (P3) Séparer général du particulier |

**Tests** : ATDD obligatoire (constitution) — au moins une tâche de test **par** user story ; écrire les tests en premier (rouge puis vert).

## Format : `[ID] [P?] [Story] Description`

---

## Phase 1 : Setup (alignement livrable)

**Purpose** : figer le périmètre et les contrats avant code.

- [x] T001 Confirmer le périmètre implémentation Feature C (exigences **IHI-C-FR-001** à **IHI-C-FR-007**) dans `specs/domains/ingredient-health-intelligence/spec.md`
- [x] T002 [P] Vérifier la traçabilité spec ↔ contrats dans `specs/domains/ingredient-health-intelligence/contracts/grounding-policy-v1.md`
- [x] T003 [P] Vérifier la traçabilité spec ↔ contrats dans `specs/domains/ingredient-health-intelligence/contracts/additive-insights-juxtaposition.md`

---

## Phase 2 : Fondations (bloquant pour toutes les stories)

**Purpose** : normalisation mécanique v1 + point d’extension unique pour l’ancrage textuel.

**⚠️ Aucune story US1–US3 ne démarre avant la fin de cette phase.**

- [x] T004 Implémenter normalisation mécanique v1 et prédicats d’ancrage sous-chaîne dans `app/src/main/java/com/miamia/composition/SegmentAnchoringV1.kt` (nouveau fichier, aligné `research.md`)
- [x] T005 Refactoriser `app/src/main/java/com/miamia/composition/CompositionResultValidator.kt` pour utiliser `app/src/main/java/com/miamia/composition/SegmentAnchoringV1.kt` sur toutes les comparaisons segment / ligne bilan

**Checkpoint** : fondations prêtes — les phases US1–US3 peuvent démarrer (idéalement US1 en premier pour le MVP).

---

## Phase 3 : User Story 1 — Confiance & KPI additifs (Priority: P1)

**Goal** : Aucun ingrédient « inventé » dans le bilan composition ; liste produit ⊆ segment (+ politique v1) ; juxtaposition additifs conforme **IHI-C-FR-007**.

**Independent Test** : `quickstart.md` scénarios 1–3 + tests unitaires nouveaux au vert sans parcours santé.

### Tests pour User Story 1 (MANDATORY)

- [x] T006 [P] [US1] Tests ancrage strict `CompositionResultValidator` / segment dans `app/src/test/java/com/miamia/composition/CompositionResultValidatorGroundingTest.kt` (nouveau fichier)
- [x] T007 [P] [US1] Tests ancrage littéral des tokens KPI vs segment dans `app/src/test/java/com/miamia/additives/AdditiveKpiGroundingTest.kt` (nouveau fichier)

### Implémentation User Story 1

- [x] T008 [US1] Appliquer la règle stricte v1 (100 % des lignes ingrédients checkables ancrées, seuil historique 50 % supprimé ou rendu conforme spec) dans `app/src/main/java/com/miamia/composition/CompositionResultValidator.kt`
- [x] T009 [US1] Propager état non analysable cohérent (pas de `BilanReady` si ancrage échoue) dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`
- [x] T010 [US1] Filtrer ou masquer les entrées KPI sans mention littérale dans le segment dans `app/src/main/java/com/miamia/additives/BuildAdditiveKpiDisplay.kt`
- [x] T011 [US1] Renforcer l’attribution explicite domaine additifs (libellé / accessibilité) dans `app/src/main/java/com/miamia/additives/ui/AdditiveKpiPanel.kt`

**Checkpoint** : US1 livrable seul (composition + KPI) avec tests au vert.

---

## Phase 4 : User Story 2 — Refus explicite / tout ou rien (Priority: P2)

**Goal** : Aucun succès avec segments « fait produit » partiellement ancrés ; pas de chiffres/allégations étiquette inventés (**IHI-C-FR-003**).

**Independent Test** : jeux contre-exemples **SC-C-002** / **SC-C-004** en unitaire ; flux UI affiche limite claire.

### Tests pour User Story 2 (MANDATORY)

- [x] T012 [P] [US2] Tests tout ou rien (bilan ou sections partiellement hors segment → échec contrôlé) dans `app/src/test/java/com/miamia/composition/CompositionAnchoringAllOrNothingTest.kt` (nouveau fichier)

### Implémentation User Story 2

- [x] T013 [US2] Garantir l’absence de `BilanSuccess` + état succès UI si `CompositionResultValidator` renvoie limite, dans `app/src/main/java/com/miamia/composition/Gemma4LocalCompositionEngine.kt` et `app/src/main/java/com/miamia/composition/LiteRtGemmaEngine.kt` (cohérence avec `app/src/main/java/com/miamia/camera/CameraViewModel.kt`)
- [x] T014 [US2] Introduire rejet ou état non analysable lorsque le texte critique contient des affirmations produit non ancrées dans `app/src/main/java/com/miamia/healthcritique/HealthCritiqueEngine.kt` (s’appuyer sur `SegmentAnchoringV1.kt` ou wrapper dédié dans le même package `healthcritique/` si nécessaire pour respecter les frontières de package)
- [ ] T015 [US2] Harmoniser les messages utilisateur d’échec ancrage sur l’écran résultat dans `app/src/main/java/com/miamia/result/LlmResultScreen.kt` (cohérent `non-analysable-response` / copy UX)

**Checkpoint** : US1 + US2 testables indépendamment ; pas de succès partiellement ancré.

---

## Phase 5 : User Story 3 — Général vs particulier (Priority: P3)

**Goal** : Blocs éducatifs identifiables ; pont « ce produit » uniquement sur tokens segment (**IHI-C-FR-004**).

**Independent Test** : tests parseur / règles sur sections ; revue manuelle **quickstart** section santé.

### Tests pour User Story 3 (MANDATORY)

- [x] T016 [P] [US3] Tests prompts / sections (séparation général vs « ce produit ») dans `app/src/test/java/com/miamia/healthcritique/HealthCritiqueGroundingTest.kt` (nouveau fichier ou extension de `app/src/test/java/com/miamia/healthcritique/HealthCritiqueSegmentParityTest.kt` si plus pertinent)

### Implémentation User Story 3

- [ ] T017 [US3] Ajuster les instructions LLM pour blocs généraux identifiables dans `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt`
- [ ] T018 [US3] Renforcer la validation ou le marquage des sections « fait produit » dans `app/src/main/java/com/miamia/healthcritique/HealthCritiqueSectionParser.kt`
- [ ] T019 [US3] Couvrir le parcours lecture seule segment + critique dans `app/src/androidTest/java/com/miamia/healthcritique/HealthCritiqueReadOnlySegmentAndroidTest.kt` (étendre les cas existants alignés **US-C3**)

**Checkpoint** : US1–US3 complètes avec critères **SC-C-003** couverts par tests + manuel.

---

## Phase 6 : Finition & transversal

**Purpose** : documentation, checklist, dette connue.

- [x] T020 [P] Mettre à jour la checklist qualité dans `specs/domains/ingredient-health-intelligence/checklists/requirements.md` (cohérence post-implémentation)
- [x] T021 [P] Rafraîchir les scénarios manuels MVP (**IHI-C-FR-006**) dans `specs/domains/ingredient-health-intelligence/quickstart.md`
- [x] T022 Recenser explicitement la dette **Feature B** (placeholder spec) si le code composition/santé diverge : note brève dans `specs/domains/ingredient-health-intelligence/plan.md` ou `research.md`

---

## Dependencies & Execution Order

### Phases

- **Phase 1** : aucune dépendance — démarrage immédiat.
- **Phase 2** : dépend de Phase 1 — **bloque** US1, US2, US3.
- **Phases 3–5** : dépendent de Phase 2. Ordre recommandé **US1 → US2 → US3** (MVP = Phase 3 seule). US2 et US3 peuvent être parallélisées **après** Phase 2 si équipe et si US1 déjà stabilisé (risque de conflits sur `HealthCritiqueEngine.kt` : séquencer si un seul dev).
- **Phase 6** : après les stories cibles livrées.

### User Story Dependencies

- **US1** : aucune dépendance inter-story (après Phase 2).
- **US2** : logiquement après ou en lockstep avec US1 pour éviter les conflits sur moteurs composition partagés ; peut être testée seule une fois `SegmentAnchoringV1.kt` stable.
- **US3** : peut s’appuyer sur les mêmes utilitaires d’ancrage ; indépendante fonctionnellement si les prompts/parseurs sont encapsulés.

### Parallel Opportunities

- **T002** ∥ **T003** (fichiers spec distincts).
- **T006** ∥ **T007** (packages test distincts).
- **T020** ∥ **T021** (documentation).
- Après Phase 2 : **T006**/**T007** en parallèle avant **T008**–**T011**.

### Exemple parallèle (US1 — ATDD)

```bash
# Lancer en parallèle la rédaction des tests rouges US1 :
# - app/src/test/java/com/miamia/composition/CompositionResultValidatorGroundingTest.kt
# - app/src/test/java/com/miamia/additives/AdditiveKpiGroundingTest.kt
```

---

## Implementation Strategy

### MVP (User Story 1 uniquement)

1. Phase 1 → Phase 2 (**T004**–**T005**).
2. Phase 3 complète (**T006**–**T011**).
3. **STOP** : valider indépendamment (tests + `quickstart.md` scénarios 1–3).

### Livraison incrémentale

1. + Phase 4 (US2) — échecs explicites, tout ou rien bout-en-bout.
2. + Phase 5 (US3) — critique santé + général.
3. Phase 6 — polish documentation.

### Stratégie équipe parallèle

- Dev A : Phase 3 (composition + CameraViewModel).
- Dev B : Phase 3 (additives) une fois **T004** gelé.
- Dev C : Phase 5 après merge des utilitaires d’ancrage partagés.

---

## Notes

- Chaque tâche avec **[US*]** référence la table de cartographie en tête de fichier.
- Respecter ATDD : tests **T006**, **T007**, **T012**, **T016** écrits et **rouges** avant implémentation correspondante.
- Commits fréquents par story ou groupe logique.
