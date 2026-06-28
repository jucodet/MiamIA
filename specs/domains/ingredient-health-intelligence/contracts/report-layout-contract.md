# Report Layout Contract — Compte rendu 4 sections (Feature P)

**Domain**: `ingredient-health-intelligence`
**Created**: 2026-06-28
**Spec ref**: Feature P — `IHI-P-FR-001` à `IHI-P-FR-012` / `IHI-P-SC-001` à `IHI-P-SC-010`
**Supersède**: aucun (préserve Feature N `critique-profil-contract.md` + Feature O `critique-inline-restitution-contract.md`).

## Purpose

Contrat de **mise en page** de l'écran de compte rendu (`LlmResultScreen` + `BilanResultCard` + `InlineCritiqueSection`) : structure **4 sections ordonnées fixes**, suppression de la liste brute d'ingrédients, intégration kcal/KPI dans la Synthèse, et critique santé concise/visuelle avec risques profil en tête.

## Section model — 4 sections ordonnées fixes

| # | Section | Owner composable | Contenu |
|---|---------|------------------|---------|
| 1 | **Produit identifié** | `BilanResultCard.ProductSection` | Produit identifié + confiance, ou état neutre « Produit non identifié ». **Inconditionnelle**. |
| 2 | **Synthèse** | `BilanResultCard.AnalysisSection` (étendu) | Pastille kcal (Feature K) **en tête** + texte d'analyse + `AdditiveKpiPanel` (KPI `additive-risk-insights`, attribution `IHI-C-FR-007`). **Inconditionnelle**. |
| 3 | **Verdict par ingrédient** | `BilanResultCard.HealthImpactSection` | Impacts santé ingrédient par ingrédient, ou état neutre « Aucun ingrédient à vigilance identifié ». **Inconditionnelle**. |
| 4 | **Critique santé** | `InlineCritiqueSection` (Feature O, inline) | Rappel « Évalué pour vous : <profil> » + `ProfileRiskHighlights` + `PrudenceGauge` + cartes repliables + `FullIngredientListToggle`. Rendue inline en continuité (Feature O inchangée). |

**Invariants de mise en page** :

- **Exactement 4 sections** sur le compte rendu `Complete` (`IHI-P-FR-001` / `IHI-P-SC-001`).
- **Ordre stable** sur tous les états du compte rendu (composition en cours, critique en cours, erreur critique, profil par défaut, liste longue, produit non identifié) (`IHI-P-FR-011` / `IHI-P-SC-008`).
- **Aucune section hors cadre** : la section « Additifs » autonome est supprimée (fusionnée dans Synthèse) ; la section « Ingrédients identifiés » est supprimée.

## Suppressions (UI)

- `RawIngredientListDisplay` :
  - `BilanResultCard.IngredientsSection` (test tag `bilan_ingredients_section`) — **supprimé**.
  - `LlmResultScreen.StreamingContent` carte « Ingrédients identifiés » (test tag `streaming_ingredients_card`) — **supprimée**.
  - Le `ValidatedIngredientSegment` reste **entrée d'analyse** (ancrage Feature C `IHI-C-FR-005`) — seul l'affichage à plat est retiré (`IHI-P-FR-002`).
- `AdditivesSection` (section autonome) : supprimée comme section ; `AdditiveKpiPanel` intégré à `Synthese` (`IHI-P-FR-003`).

## Critique santé — forme concise/visuelle (`ConciseVisualCritique`)

Ordre à l'intérieur de la section 4 :

1. `EvaluatedForHeader` « Évalué pour vous : <profil> » (`IHI-N-FR-003`) + signal « profil par défaut » si applicable (`IHI-N-FR-012`).
2. **`ProfileRiskHighlights`** : pastilles/étiquettes colorées courtes, une par `IngredientRiskCard` (vigilance Modérée/Élevée pour le profil sélectionné) — nom (+ code éventuel) + marqueur de sévérité visuel. Si aucune carte : pastille neutre « Aucun risque marqué pour votre profil ». **Mise en évidence des risques pour le type d'utilisateur choisi** (`IHI-P-FR-005`/`007`).
3. `PrudenceGauge` : jauge 3 paliers (Faible/Modéré/Élevé) + texte court justificatif (`IHI-N-FR-009`).
4. `IngredientRiskCardItem` : cartes détaillées **repliables par défaut** (profondeur non dominante — `IHI-P-FR-006`).
5. `FullIngredientListToggle` : bouton « Voir tous les ingrédients analysés » (liste compacte nom + statut — `IHI-N-FR-011`).

**Ancrage** : chaque `ProfileRiskHighlight` et chaque carte MUST être ancré dans le `ValidatedIngredientSegment` (`IHI-C-FR-005`) ; aucun risque inventé (`IHI-P-SC-005`).

## Non-régression (préservé)

- `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser` — **inchangés**.
- Flux composition, `CompositionBilan`, `CompositionEnergyPastille` (Feature K), `AdditiveKpiPanel` (`additive-risk-insights`) — **inchangés** (relocalisés, non modifiés).
- Déclenchement automatique + restitution inline (Feature O) — **inchangés**.
- Conformité Feature C (`IHI-C-FR-001`..`007`) et garde-fous Feature L/N — **inchangés**.

## Test tags contractuels

| Tag | Présence | Section |
|-----|----------|---------|
| `bilan_product_section` | attendu (inconditionnel) | 1 — Produit identifié |
| `bilan_analysis_section` | attendu (inconditionnel) | 2 — Synthèse |
| `composition_energy_pastille` | attendu (dans Synthèse) | 2 — Synthèse |
| `bilan_additives_section` | **supprimé** (fusion Synthèse) | — |
| `bilan_ingredients_section` | **supprimé** | — |
| `streaming_ingredients_card` | **supprimé** | — |
| `bilan_health_impact_section` | attendu (inconditionnel) | 3 — Verdict par ingrédient |
| `inline_critique_section` | attendu (inline) | 4 — Critique santé |
| `health_result_evaluated_for` | attendu | 4 — Critique santé |
| `health_result_risk_highlights` | attendu (NOUVEAU) | 4 — Critique santé (pastilles risques profil) |
| `health_result_prudence` | attendu | 4 — Critique santé |
