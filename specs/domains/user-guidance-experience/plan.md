# Implementation Plan: Homepage LLM Mock Trigger

**Branch**: `016-home-llm-button` | **Date**: 2026-05-06 | **Spec**: `specs/domains/user-guidance-experience/spec.md`  
**Input**: Feature specification from `/specs/domains/user-guidance-experience/spec.md`

## Summary

Ajouter un point d'entree UX sur la homepage qui declenche le test bouchonne du pipeline LLM local au clic, affiche un etat d'execution clair, puis rend la reponse LLM (ou une erreur explicite) sans passer par les parcours camera/OCR. La conception preserve la frontiere DDD: `user-guidance-experience` orchestre uniquement le declenchement et le rendu, tandis que la validite metier de la reponse reste dans le domaine d'analyse.

## Technical Context

**Language/Version**: Kotlin 2.x + Android (minSdk 26, targetSdk 34, Java 17)  
**Primary Dependencies**: Jetpack Compose Material3, AndroidX Lifecycle/ViewModel, Kotlin Coroutines, modules app existants pour orchestration d'analyse locale  
**Storage**: N/A (pas de nouvelle persistance requise pour ce scope UX)  
**Testing**: JUnit4 + tests UI Compose + tests ViewModel  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: affichage de l'etat "en cours" en moins de 200 ms apres clic; rendu de la reponse en moins de 30 s dans les cas reussis  
**Constraints**: offline-first local; pas de declenchements concurrents; message d'erreur explicite obligatoire  
**Scale/Scope**: homepage + ecran/resultat associe au test LLM; une execution active a la fois

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Qualite produit et code**: PASS - exigences testables, traçabilite spec vers artefacts de design.
- **II. ATDD d'abord**: PASS - scenarios Given/When/Then dans la spec, quickstart de tests ajoute.
- **III. UX moderne et optimale**: PASS - etats explicites `idle/running/success/failure`, erreurs lisibles.
- **IV. Performance exigence produit**: PASS - objectifs mesurables definis (etat <200ms, reponse <30s).
- **V. Simplicite et evolutivite**: PASS - orchestration UI minimale, aucune fuite de logique metier.
- **VI. Frontieres DDD**: PASS - domaine UX consomme un service d'analyse sans redefinir "reponse exploitable".

**Post-Design Re-check**: PASS - modeles UX et contrat d'interface restent limites a `user-guidance-experience`.

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── homepage-llm-mock-ui-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/foodgpt/
├── home/
│   ├── HomeScreen.kt
│   └── HomeViewModel.kt
├── analysis/
│   └── AnalysisInputBuilder.kt
├── composition/
│   └── LiteRtGemmaEngine.kt
└── ui/
    └── components/

app/src/test/java/com/foodgpt/home/
└── HomeLlmMockTriggerTest.kt

app/src/androidTest/java/com/foodgpt/home/
└── HomeScreenLlmMockUiTest.kt
```

**Structure Decision**: conserver l'implementation dans le flux homepage (`home/`) avec adaptation minimale vers le service d'analyse existant; les tests unitaires et UI couvrent le declenchement, les etats et le rendu resultat.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
