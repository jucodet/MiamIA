# Implementation Plan: Critique santé d’une liste d’ingrédients (prompt LLM)

**Branch**: `002-ingredient-health-critique` | **Date**: 2026-05-04 | **Spec**: `specs/002-ingredient-health-critique/spec.md`  
**Input**: Feature specification from `specs/002-ingredient-health-critique/spec.md`

**Note**: Généré par `/speckit.plan`. Le dépôt peut être sur une autre branche Git locale ; le répertoire de spec reste `specs/002-ingredient-health-critique/`.

## Summary

Permettre à l’utilisateur de soumettre une **liste d’ingrédients** en texte libre et d’obtenir une **critique d’impact santé** aussi objective que possible, **structurée en quatre populations** (enfants, femmes enceintes, adultes, personnes âgées), avec **consignes de prudence** (nuancer les incertitudes, pas de diagnostic). L’app **construit le prompt système**, exécute **Gemma en local** (LiteRT-LM, aligné sur le socle spec **009**), **affiche** la réponse, permet la **copie**, et **persiste au minimum la dernière analyse**. Les entrées invalides (vide, trop courte) sont **refusées** avec message clair.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (minSdk 26, targetSdk 34, Java 17)  
**Primary Dependencies**: Jetpack Compose Material3, Coroutines, **LiteRT-LM** (`com.google.ai.edge.litertlm:litertlm-android`), modèle Gemma `.litertlm`  
**Storage**: `SharedPreferences` ou fichier privé léger pour snapshot « dernière analyse » (FR-006) ; pas d’exigence Room en v1  
**Testing**: JUnit4, tests instrumentés AndroidX, tests UI Compose pour états saisie / résultat / copie  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: Réponse ou erreur explicite dans une fenêtre **inférieure à 30 s** perçue sur device milieu de gamme après chargement du modèle (alignement engineering avec spec 009)  
**Constraints**: Formulations prudentes et non médicales ; structure **4 sections** détectable (marqueurs `###…`) ; confidentialité : pas d’envoi réseau du texte pour ce flux (cohérent pratique 009)  
**Scale/Scope**: Un parcours d’analyse ponctuel ; une entrée d’historique minimale ; parseur léger post-inférence

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit et code**: PASS — spec 002 + artefacts `research.md`, `data-model.md`, `contracts/`, `quickstart.md` pour traçabilité implémentation.
- **ATDD d’abord**: PASS — user stories et scénarios Given/When/Then présents dans `spec.md` ; contrat `health-critique-llm-contract.md` pour états testables.
- **UX moderne et optimale**: PASS — messages entrée invalide, chargement, disclaimer, copie (FR-004, US3).
- **Performance exigence produit**: PASS — objectif temporal documenté ci-dessus et critères mesurables dans la spec (SC-001 à SC-004).
- **Simplicité et évolutivité**: PASS — extension du pattern `LiteRtGemmaEngine` / gateway locale plutôt que nouveau runtime ; persistance minimale extensible vers Room si besoin.

**Post-Design Re-check**: PASS — décisions documentées dans `research.md` ; aucun NEEDS CLARIFICATION bloquant.

## Project Structure

### Documentation (this feature)

```text
specs/002-ingredient-health-critique/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── health-critique-llm-contract.md
└── tasks.md                    # généré par /speckit.tasks (hors périmètre /speckit.plan)
```

### Source Code (repository root)

```text
app/
└── src/main/java/com/foodgpt/
    ├── composition/                    # existant — référence LiteRtGemmaEngine
    │   └── LiteRtGemmaEngine.kt        # pattern d’inférence + systemInstruction
    ├── healthcritique/                 # (nouveau package suggéré)
    │   ├── HealthCritiquePromptBuilder.kt
    │   ├── HealthCritiqueSectionParser.kt
    │   ├── HealthCritiqueEngine.kt     # façade AnalyzeIngredientHealthCritique
    │   └── LastHealthAnalysisStore.kt
    └── …                             # écran / ViewModel : point d’entrée UI à placer selon navigation produit
app/src/test/java/com/foodgpt/healthcritique/
└── …                                 # tests parseur + validation entrée
```

**Structure Decision**: Étendre l’app Android existante ; isoler prompt + parsing + persistance légère dans un package **`healthcritique`** pour ne pas mélanger avec le bilan composition (`###LISTE` / `###ANALYSE`) ; réutiliser l’infrastructure LiteRT-LM déjà présente dans `app/build.gradle.kts`.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
