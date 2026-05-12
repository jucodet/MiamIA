# Implementation Plan: Bilan ingrédients et composition (Gemma local)

**Branch**: `009-llm-bilan-composition-ingredients` | **Date**: 2026-04-24 | **Spec**: `specs/009-llm-bilan-composition-ingredients/spec.md`  
**Input**: Feature specification + directive plan : exécution **locale** du LLM sur l’appareil via **Gemma** ; en l’absence de Gemma (ressource introuvable ou initialisation impossible), afficher un **message d’erreur** explicite (aligné FR-009 / FR-011 / FR-012 et clarifications 2026-04-24).

## Summary

Après obtention du **texte capturé** (flux photo → OCR local existant), enchaîner une étape d’**inférence locale Gemma** (runtime **LiteRT** déjà présent dans le projet) pour : (1) extraire / normaliser la **liste d’ingrédients** avec corrections de typos/OCR prudente, (2) produire une **analyse de composition** à formulation factuelle et prudente. L’**écran de résultat** affiche le **bilan** (liste + analyse + mention informative) **à la place** du texte brut comme contenu principal. Si le **modèle Gemma** n’est **pas trouvé** (fichier absent, chemin invalide, échec chargement) ou ne peut pas s’exécuter localement, l’utilisateur voit un **état d’erreur dédié** (ex. « Gemma introuvable » / message équivalent compréhensible), avec **réessai** et **repli** vers le texte brut si le produit le prévoit — **sans** bilan partiel simulé.

## Technical Context

**Language/Version**: Kotlin **2.3.x** + Android (minSdk 26, targetSdk 34, Java 17)  
**Primary Dependencies**: Jetpack Compose Material3, Kotlin Coroutines, ML Kit Text Recognition (texte capturé), **LiteRT** (`com.google.ai.edge.litert:litert`), **LiteRT-LM** (`com.google.ai.edge.litertlm:litertlm-android`), **Gemma** au format **`.litertlm`** (LiteRT-LM) dans `assets/gemma/`  
**Storage**: Cache privé app pour image temporaire ; **assets ou fichiers privés** pour poids du modèle Gemma (bundle ou copie post-install) ; Room pour persistance session si réutilisation des specs scan existantes  
**Testing**: JUnit4, tests instrumentés AndroidX, tests UI Compose (états `Analyzing` → `BilanSuccess` / `GemmaUnavailable`)  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: Bilan affiché ou erreur explicite dans la fenêtre **< 30 s** perçue (spec SC-003) ; objectif intermédiaire engineering : garder p95 raisonnable sur devices milieu de gamme une fois le modèle chargé (à mesurer).  
**Constraints**: **Aucun** envoi du texte capturé hors appareil pour cette fonctionnalité (FR-011) ; **Gemma obligatoire** pour le chemin « bilan complet » — pas de contournement cloud ; pas de faux bilan si Gemma absent (FR-012) ; OCR inchangé (hors scope spec 009).  
**Scale/Scope**: Un flux post-OCR par capture ; textes étiquettes courants ; modèle Gemma dimensionné au compromis qualité / taille / RAM.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit et code**: PASS — spec + clarifications + artefacts `research.md` / `data-model.md` / `contracts/` / `quickstart.md` traçables.
- **ATDD d’abord**: PASS — scénarios Given/When/Then dans la spec ; contrat UI enrichi pour états Gemma.
- **UX moderne et optimale**: PASS — chargement analyse, erreur Gemma explicite, repli texte brut optionnel produit.
- **Performance exigence produit**: PASS — SC-003 et objectifs perf ci-dessus.
- **Simplicité et évolutivité**: PASS — s’appuie sur modules `recognition` / `camera` / `ingredients` existants ; couche `GemmaCompositionAnalyzer` (nom indicatif) isolée pour tests.

**Post-Design Re-check**: PASS — décisions Gemma + LiteRT documentées dans `research.md` ; pas de NEEDS CLARIFICATION bloquant.

## Project Structure

### Documentation (this feature)

```text
specs/009-llm-bilan-composition-ingredients/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── gemma-composition-bilan-contract.md
└── tasks.md                    # généré par /speckit.tasks (hors périmètre /speckit.plan)
```

### Source Code (repository root)

```text
app/
└── src/main/java/com/miamia/
    ├── camera/
    │   ├── CameraViewModel.kt          # orchestration états scan → extension bilan
    │   └── CameraScreen.kt             # affichage bilan vs texte brut
    ├── recognition/
    │   ├── IngredientRecognitionCoordinator.kt
    │   └── ...                         # point d’entrée texte OCR vers Gemma (nouveau pipeline)
    ├── ingredients/
    │   └── ...                         # builders messages / mapping UI bilan
    └── composition/                    # (nouveau package suggéré)
        ├── GemmaModelLocator.kt        # résolution chemin + « trouvé / non trouvé »
        ├── GemmaCompositionAnalyzer.kt # inférence + parsing sortie structurée
        └── CompositionBilan.kt         # modèles Kotlin résultat
```

**Structure Decision**: Étendre l’app Android existante ; introduire un package dédié **composition** (ou équivalent) pour Gemma / LiteRT afin de ne pas mélanger OCR et LLM dans les mêmes classes ; conserver `CameraViewModel` comme orchestrateur d’états UI testables.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

## Implementation Notes (2026-04-24)

- **Gemma** = seul moteur LLM retenu pour le chemin bilan ; détection **« non trouvé »** = échec explicite `GemmaModelLocator` + message utilisateur (pas de génération distante).
- **LiteRT** déjà dans `app/build.gradle.kts` : réutiliser pour chargement / session d’inférence (détails API en implémentation selon version et format modèle).
- Enrichir `DeviceAiCapabilityDetector` ou créer un détecteur **spécifique Gemma** (présence fichier, lisibilité, init LiteRT) pour alimenter l’UI avant lancement coûteux du scan si souhaité MVP+.
