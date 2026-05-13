# Implementation Plan : Feature F — libellés capture, fin test LLM, retrait « Aperçu caméra actif »

**Branch** : `021-auto-analyze-ingredients-tag` (branche renvoyée par `setup-plan.ps1` ; créer une branche dédiée si la stratégie release l’exige) | **Date** : 2026-05-13 | **Spec** : [spec.md — Feature F](./spec.md#feature-f--libellé-caméra-explicite-suppression-test-llm-retrait-aperçu-caméra-actif-)
**Input** : `specs/domains/user-guidance-experience/spec.md` (Feature F)

## Summary

Rendre l’état « prêt à scanner » lisible sans le mot ambigu « Disponible » seul, retirer tout bouton et parcours « Test LLM » de l’écran d’accueil (= capture), et supprimer la ligne de statut « Aperçu caméra actif ». Changements concentrés sur `CameraScreen.kt`, `CameraViewModel.kt`, `MediaPipeStatusViewState.kt`, `MainActivity.kt` (câblage runner), tests AndroidTest sous `com.miamia.camera`, et documentation des contrats. Alignement documentaire requis avec le domaine voisin `capture-recognition` (`contracts/capture-action-bar.md`, test tags) dans la même livraison ou immédiatement après (UGE-F-FR-004).

## Technical Context

**Language / Version** : Kotlin (Android), Jetpack Compose (Material 3), API min du projet inchangée.  
**Primary Dependencies** : Compose UI, `ScanState` / flux existant post-capture, `HomeLlmMockRunner` (retrait du chemin UI uniquement).  
**Storage** : N/A.  
**Testing** : AndroidTest Compose (`CameraCaptureLayoutUiTest`, `CameraUnavailableLlmButtonUiTest` à réécrire ou renommer), tests unitaires éventuels sur mapping de libellés ; pas de nouveau framework.  
**Target Platform** : Android téléphone, portrait principal.  
**Project Type** : mobile-app (`app`).  
**Performance Goals** : aucune régression sur temps d’affichage de l’écran capture ; pas de recomposition supplémentaire inutile.  
**Constraints** : respect UGE-F-FR-001..003 et SC-D-003 (tests capture cohérents après retrait LLM) ; frontière DDD : logique mock LLM peut rester dans `home/` pour tests internes si isolée, mais aucune exposition UI (UGE-F-FR-002).  
**Scale/Scope** : ~4 fichiers Kotlin principaux + 2 fichiers AndroidTest + contrats / quickstart / `capture-recognition` en suivi contractuel.

## Constitution Check

*GATE : doit passer avant Phase 0 ; revérifier après Phase 1.*

| Principe (constitution v0.2.0) | Évaluation | Justification |
|---|---|---|
| I. Qualité produit / code | ✅ PASS | Spec Feature F traçable → tests mis à jour → code ; pas de régression silencieuse sur la capture principale. |
| II. ATDD | ✅ PASS | Scénarios Given/When/Then dans spec ; tests layout existants adaptés (assert absence bouton LLM, nouveaux libellés) ou nouveaux tests ciblés. |
| III. UX | ✅ PASS | Objectif explicite : micro-copies claires, suppression bruit redondant et action de démo. |
| IV. Performance | ✅ PASS | Suppression d’éléments UI ; pas de charge ajoutée. |
| V. Simplicité | ✅ PASS | Suppression de code mort UI et de branches ViewModel liées au bouton ; pas de nouvelle couche. |
| VI. Frontières DDD | ✅ PASS | Orchestration UX dans `user-guidance-experience` ; contrat technique des tags et bande d’action reste owner `capture-recognition` — mise à jour croisée documentée (UGE-F-FR-004). |

Aucune violation ⇒ pas d’entrée obligatoire dans Complexity Tracking.

## Project Structure

### Documentation (ce plan)

```text
specs/domains/user-guidance-experience/
├── plan.md              # Ce fichier
├── research.md          # Phase 0 — addendum Feature F
├── data-model.md        # Phase 1 — addendum Feature F
├── quickstart.md        # Phase 1 — addendum scénarios + commandes tests
├── contracts/
│   ├── capture-screen-feature-f-status-copy.md   # Phase 1 — libellés + absence bouton LLM
│   └── homepage-llm-mock-ui-contract.md            # marqué obsolète côté UI capture (Feature F)
└── tasks.md             # Phase 2 (/speckit.tasks — non créé ici)
```

### Source Code (extrait ciblé)

```text
app/src/main/java/com/miamia/
├── camera/
│   ├── CameraScreen.kt          # bande d’action, libellés ScanState, suppression bouton Test LLM
│   └── CameraViewModel.kt      # suppression runCameraTabLlmMockTest + deps runner si inutilisées
├── home/
│   └── MediaPipeStatusViewState.kt   # remplacer label "Disponible" par libellé explicite (UGE-F-FR-001)
└── MainActivity.kt             # arrêt injection runner pour onglet capture si applicable

app/src/androidTest/java/com/miamia/camera/
├── CameraCaptureLayoutUiTest.kt       # retirer assertions sur camera_tab_llm_test_button ; conserver non-recouvrement preview / bouton principal
└── CameraUnavailableLlmButtonUiTest.kt # renommer / réorienter (ex. bande d’action sans second bouton) ou fusionner avec layout test
```

**Structure Decision** : point d’entrée unique écran capture = package `camera/` ; libellé MediaPipe partagé via `home/MediaPipeStatusViewState.kt` (déjà consommé par l’écran capture).

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (none) | — | — |

## Phase 0 — Outline & Research

Consolidé dans `research.md`, section **Addendum Feature F**. Décisions clés :

1. **Libellé « Disponible »** : aujourd’hui dans `MediaPipeStatusViewState.kt` ; remplacer par une formulation explicite liée à la détection d’étiquette (ex. « Détection prête » ou « MediaPipe prêt ») — à valider avec coproduit pour éviter jargon ; interdit : chaîne d’un seul mot générique « Disponible ».
2. **Statut `PreviewActive`** : remplacer « Aperçu caméra actif » par la même famille de libellé que US-F1 (caméra prête à scanner) **ou** masquer la ligne si redondance visuelle totale — défaut retenu : **une ligne explicite** satisfaisant UGE-F-FR-001 et UGE-F-FR-003 (plus de chaîne exacte « Aperçu caméra actif »).
3. **Test LLM** : retirer `OutlinedButton` + `onRunLlmTest` + `camera_tab_llm_test_button` ; supprimer `runCameraTabLlmMockTest` et simplifier constructeur `CameraViewModel` / factory `MainActivity` si le runner n’est plus utilisé que pour ce bouton.
4. **Contrat `homepage-llm-mock-ui-contract.md`** : marqué **hors produit** pour l’UI capture ; le runner peut subsister pour tests JVM/Android hors écran capture si nécessaire.

## Phase 1 — Design & Contracts

Livrables :

- `data-model.md` — addendum : mapping `ScanState` → libellé utilisateur (sans « Aperçu caméra actif ») ; ligne MediaPipe explicite.
- `contracts/capture-screen-feature-f-status-copy.md` — chaînes interdites / attendues, absence tag `camera_tab_llm_test_button`, cohérence avec tests.
- `quickstart.md` — addendum Feature F : parcours manuel + commandes Gradle ciblées.
- **Ref domaine voisin** : PR d’implémentation MUST inclure ou référencer une mise à jour de `specs/domains/capture-recognition/contracts/capture-action-bar.md` (suppression second bouton, statuts textuels).

Fichier d’agent (`.cursor/rules/specify-rules.mdc`) : pointe déjà vers ce `plan.md` — aucun changement de chemin requis.

## Post-Design Constitution Re-check

| Principe | Statut |
|---|---|
| I — Qualité | ✅ |
| II — ATDD | ✅ tests adaptés listés en Phase 0 |
| III — UX | ✅ |
| IV — Perf | ✅ |
| V — Simplicité | ✅ |
| VI — DDD | ✅ ref `capture-recognition` explicite |

Aucune nouvelle violation.
