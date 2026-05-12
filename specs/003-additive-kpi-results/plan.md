# Implementation Plan: Écran KPI de risque additifs

**Branch**: `003-additive-kpi-results` | **Date**: 2026-05-04 | **Spec**: `specs/003-additive-kpi-results/spec.md`  
**Input**: Feature specification from `specs/003-additive-kpi-results/spec.md`

**Note**: La branche Git locale peut différer ; le répertoire de spec est `specs/003-additive-kpi-results/`. Utiliser `SPECIFY_FEATURE_DIRECTORY=specs/003-additive-kpi-results` pour les scripts Spec Kit si `.specify/feature.json` pointe ailleurs.

## Summary

Après une **réponse LLM** de composition (liste + analyse), enrichir l’**écran résultat** avec un **classement d’additifs** : pastille **verte / orange / rouge** (vigilance faible → élevée), **justification courte** par ligne, **tri par criticité** (rouge puis orange puis vert), **KPI globaux** (totaux et compteurs par niveau), et gestion explicite des **cas incomplets** (à confirmer), **doublons** et **incohérences**. Le parsing s’appuie sur un **format structuré** documenté dans `research.md`, avec repli prudent si le modèle ne respecte pas le gabarit. L’intégration se fait dans la continuité du flux **`ScanState.BilanReady`** / `CameraScreen` (Material3, accessibilité).

## Technical Context

**Language/Version**: Kotlin + Android (minSdk 26, targetSdk 34, Java 17) — aligné `app/build.gradle.kts`  
**Primary Dependencies**: Jetpack Compose Material3, Coroutines, pipeline existant `composition` (`GemmaBilanParser`, `CompositionBilan`, `LiteRtGemmaEngine` / prompts)  
**Storage**: N/A pour le MVP (données dérivées en mémoire à partir du texte LLM + bilan)  
**Testing**: JUnit4, tests Compose / instrumentés selon patterns existants dans `app/src/test` et `app/src/androidTest`  
**Target Platform**: Smartphones Android  
**Project Type**: Application mobile Android  
**Performance Goals**: Parsing + agrégation KPI **p95 inférieur à 50 ms** pour listes d’additifs typiques (objectif engineering, à valider par mesure)  
**Constraints**: Pas de diagnostic médical ; couleur **non seule** indicateur (libellé + `contentDescription`) ; KPI **strictement** alignés sur la liste affichée (SC-003)  
**Scale/Scope**: Un panneau résultat dans le parcours bilan ; pas d’API cloud obligatoire en MVP

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Qualité produit et code**: PASS — spec 003 + artefacts `research.md`, `data-model.md`, `contracts/`, `quickstart.md`.
- **ATDD d’abord**: PASS — user stories et scénarios Given/When/Then dans `spec.md` ; contrat UI dans `contracts/additive-kpi-ui-contract.md`.
- **UX moderne et optimale**: PASS — états vides, badges d’incertitude, expansion inline justification (FR-006), accessibilité couleur.
- **Performance exigence produit**: PASS — objectif parsing mesurable + critères de succès spec (SC-002 lecture rapide).
- **Simplicité et évolutivité**: PASS — couche parseur + modèle d’affichage découplés de l’UI ; extension future (2ᵉ LLM, base E-additives) sans casser le contrat.

**Post-Design Re-check**: PASS — décisions dans `research.md` ; pas de NEEDS CLARIFICATION bloquant.

## Project Structure

### Documentation (this feature)

```text
specs/003-additive-kpi-results/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── additive-kpi-ui-contract.md
└── tasks.md                    # généré par /speckit.tasks (hors /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/
├── composition/
│   ├── GemmaBilanParser.kt           # référence parseur existant ; évolution prompt ailleurs
│   └── LiteRtGemmaEngine.kt         # extension possible du prompt système (bloc structuré additifs)
├── additives/                       # (nouveau package suggéré)
│   ├── AdditiveRiskLevel.kt
│   ├── AdditiveRiskItem.kt
│   ├── RiskSummaryKpi.kt
│   ├── AnalysisDisplayResult.kt
│   ├── AdditiveKpiParser.kt        # raw LLM + CompositionBilan → AnalysisDisplayResult
│   └── AdditiveKpiSummaryBuilder.kt
├── camera/
│   ├── CameraScreen.kt              # intégration UI KPI dans BilanReady
│   └── CameraViewModel.kt          # optionnel : exposition état enrichi
app/src/test/java/com/miamia/additives/
└── …                               # tests parseur + ordre + KPI
```

**Structure Decision**: Introduire un package **`additives`** (domaine + parsing) pour ne pas alourdir `GemmaBilanParser` avec la logique KPI ; conserver `CameraScreen` comme hôte UI du résultat scan, avec composants Compose dédiés réutilisables.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
