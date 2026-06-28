# Data Model — ingredient-health-intelligence

## Entities

- `HealthAnalysisReport` (agrégat existant)
  - `reportId`
  - `validatedSegment` : `ValidatedIngredientSegment`
  - `compositionSummary` : bilan structuré post-parse LLM (inclut **Feature K** : `estimatedKcalPer100g` optionnel sur le bilan)
  - `populationCritiques` : sections critique par population
  - `additiveRiskFacts` : projection vers read-model additifs (référence, pas texte étiquette)
  - `generatedAt`
  - `anchoringOutcome` *(étendu)* : `AnchoringOutcome` — résultat du contrôle tout-ou-rien sur la partie « fait produit »

- `EquivalencePolicy` *(nouveau concept métier — versionné)*
  - `policyVersion`
  - `mechanicalNormalizations` : liste close (ex. `LOWERCASE`, `COLLAPSE_WHITESPACE`) — **seule** extension v1 autorisée par défaut
  - `synonymRules` : vide en v1 ; entrées futures explicites + version

- `GroundedProductClaim` *(vue logique pour audit)*
  - `claimText` : fragment de sortie présenté comme fait produit
  - `sourceSpan` : référence à sous-chaîne du `ValidatedIngredientSegment` **ou** clé de règle `EquivalencePolicy`

- `AttributedAdditiveInsights` *(projection UI / read-model)*
  - `attributionLabel` : texte fixe identifiant le domaine `additive-risk-insights`
  - `linkedAdditiveTokens` : liste de mentions **littérales** présentes dans le segment et couvertes par les faits affichés
  - `payload` : données structurées KPI (détail dans domaine additifs)

## Value Objects

- `ValidatedIngredientSegment` — immuable, seule source de vérité textuelle pour faits étiquette (**IHI-C-FR-001**).
- `AdditiveRiskFact` — déjà utilisé côté présentation ; ne doit pas être confondu avec libellé étiquette.
- `PopulationCritique` — sections ; chaque assertion « ce produit » doit être ancrable (**IHI-C-FR-004** b).
- `AnchoringOutcome` : `FULLY_GROUNDED` | `REJECTED_NON_ANALYSABLE` (mappe métier vers `non-analysable-response` ou équivalent).

- `EstimatedEnergyPer100g` *(Feature K — valeur sur `CompositionBilan`)*  
  - Représentation v1 : `Int?` sur `CompositionBilan` (`estimatedKcalPer100g`) ; `null` = indisponible ou non fiable après garde-fous.  
  - Unité implicite : **kcal / 100 g**.  
  - Source : section modèle `###ENERGIE_ESTIMEE` + **EnergyEstimateValidator** (bornes **1..1100**).

- `EnergyEstimateSource` *(traçabilité minimale)*  
  - v1 : présence dans `rawModelOutput` ; pas d’ID séparé tant que le texte brut est conservé sur `BilanSuccess`.

## Aggregates

- **Root** : `HealthAnalysisReport`
  - Cohérence : aucun état **succès** si `anchoringOutcome != FULLY_GROUNDED` pour le bloc « fait produit » LLM analysé.
  - `additiveRiskFacts` peut coexister avec succès **uniquement** si contrat **IHI-C-FR-007** respecté.

## Invariants

- `validatedSegment` non vide pour une analyse lancée.
- `validatedSegment` immuable pendant le cycle de rapport.
- **IHI-C-FR-003** : pas de `FULLY_GROUNDED` si une `GroundedProductClaim` requise manque d’`sourceSpan` admissible.
- **IHI-C-FR-007** : chaque entrée `AttributedAdditiveInsights.linkedAdditiveTokens` ⊆ sous-chaînes littérales du segment (post **IHI-C-FR-005**).

## Validation rules (implémentation)

- Normaliser segment + candidats de claims avec **uniquement** `mechanicalNormalizations` v1 avant test de sous-chaîne.
- Ratio de rejet : tendre vers **0** ligne « fait produit » non ancrée en succès (stricter que le seuil historique 50 % si la spec prime).

## Entities — Feature L (personnalisation du prompt de critique)

- `HealthCritiquePrompt` *(construction — remplacement en dur versionné)*
  - `systemInstruction` : texte d'instruction système (persona expert + dimensions de risque + hiérarchie des preuves + populations vulnérables + garde-fous éthiques + format de sortie strict)
  - `userMessage` : message utilisateur construit à partir du segment ingrédients (plafond `MAX_INGREDIENT_TEXT_CHARS`)
  - Invariant : construction **répétable** (même entrée → même prompt) — `IHI-L-SC-007`.

- `RiskDimension` *(énumération fermée — `IHI-L-FR-003`)*
  - Valeurs : `CANCEROGENE`, `MUTAGENE`, `NEUROTOXIQUE`, `METABOLIQUE`, `INFLAMMATOIRE`.
  - Note : « métabolique » couvre pics glycémiques, cholestérol.

- `EvidenceTier` *(énumération fermée — `IHI-L-FR-004`)*
  - Valeurs : `FAIT_ETABLI` (ex. classification CIRC/OMS, consensus scientifique), `INCERTITUDE_SCIENTIFIQUE` (débats, effets à doses massives chez l'animal), `HYPOTHESE_MECANISME` (mécanismes suspectés).

- `VulnerablePopulation` *(énumération fermée — `IHI-L-FR-008`)*
  - Valeurs : `FEMMES_ENCEINTES_ALLAITANTES`, `ENFANTS`, `IMMUNODEPRIMEES`, `ANTECEDENTS_FAMILIAUX_CANCER`.
  - Note : les deux dernières n'ont pas de section de sortie dédiée → traitées en **vigilance transversale intégrée** (clarify Q2).

- `CritiqueSectionMarker` *(contrat de sortie strict — `IHI-L-FR-009`)*
  - Marqueurs ordonnés : `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`.
  - Blocs obligatoires sous chaque marqueur : (1) Points de vigilance, (2) Analyse par ingrédient & Nuances, (3) Niveau de prudence (Faible / Modéré / Élevé).
  - Invariant : aucun texte avant `###ENFANTS` ; ordre immuable (non-régression `HealthCritiqueSectionParser`).

## Validation rules (Feature L)

- Le prompt construit MUST contenir le persona expert, les 5 `RiskDimension`, les 3 `EvidenceTier`, les 4 `VulnerablePopulation`, le disclaimer, et le format `CritiqueSectionMarker` (`IHI-L-SC-001`..`004`).
- Le seuil « liste très longue » = `HealthCritiqueConfig.LONG_LIST_INGREDIENT_THRESHOLD` (nombre d'ingrédients, valeur au plan, ex. 20) — `IHI-L-FR-012`.
- Conformité Feature C préservée : le prompt MUST NOT encourager l'invention d'ingrédients absents (`IHI-L-FR-014` / `IHI-L-SC-006`).

## Entities — Feature M (accès UI à la critique santé)

- `HealthCritiqueEntryRoute` *(route de navigation — `IHI-M-FR-001`)*
  - Valeur : `CameraFlowRoutes.HealthCritiqueEntry` (constante chaîne, ex. `"health_critique_entry"`).
  - Cible : `HealthCritiqueScreen` (écran d'entrée existant, non modifié).

- `CritiqueSanteEntryTrigger` *(point d'entrée UI — `IHI-M-FR-002`/`003`)*
  - Composant : bouton « Critique santé » rendu dans `LlmResultScreen` (état terminal `Complete`/`Error`).
  - Condition d'activation : `lastValidatedSegmentForHealth` non null/non vide.
  - Callback : `onCritiqueSante: () -> Unit` → `cameraNavController.navigate(HealthCritiqueEntry)`.
  - Invariant : désactivé (ou masqué) si aucun segment validé disponible (`InputInvalidReason.NO_VALIDATED_SEGMENT`).

## Validation rules (Feature M)

- La route `HealthCritiqueEntry` MUST être enregistrée dans le `NavHost` de `MainActivity` (`IHI-M-FR-001`).
- Le bouton « Critique santé » MUST être désactivé lorsque `lastValidatedSegmentForHealth` est vide/null (`IHI-M-FR-003`).
- La chaîne `analyze()` → `navigateToResult` → `HealthCritiqueResult` MUST rester inchangée (`IHI-M-FR-006`).
- `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, le flux composition MUST NOT être modifiés (`IHI-M-FR-007`).

## Entities — Feature N (critique ciblée par profil utilisateur)

- `UserProfile` *(énumération fermée — contrat de consommation IHI, `IHI-N-FR-001`/`006`)*
  - Valeurs : `FEMME_ENCEINTE` (label « Femme enceinte », marker `###FEMME_ENCEINTE`), `ENFANT` (label « Enfant », marker `###ENFANT`), `PERSONNE_AGEE` (label « Agé », marker `###PERSONNE_AGEE`), `ADULTE` (label « Adulte », marker `###ADULTE`), `SPORTIF` (label « Sportif », marker `###SPORTIF`).
  - `DEFAULT` : `ADULTE` (fallback en l'absence de profil sélectionné — clarify Q1, `IHI-N-FR-012`).

- `UserProfileProvider` *(interface — frontière DDD UGE↔IHI)*
  - `current(): UserProfile` — retourne le profil sélectionné (ou `DEFAULT` si non défini).
  - `DefaultUserProfileProvider` *(implémentation IHI, fallback)* : retourne `UserProfile.DEFAULT` ; settable en mémoire pour tests.
  - L'implémentation persistée (Onboarding + « Paramètres / Profil ») est fournie par le domaine `user-guidance-experience` sur le même contrat.

- `PrudenceLevel` *(énumération fermée — `IHI-N-FR-007`)*
  - Valeurs : `FAIBLE`, `MODERE`, `ELEVE` (libellés UI : Faible / Modéré / Élevé).
  - Champ associé : `justificationCourte: String` (texte court du LLM).

- `IngredientRiskCard` *(carte d'un ingrédient à vigilance — `IHI-N-FR-008`)*
  - `nom: String`, `code: String?` (ex. E-number), `type: String` (ex. « Conservateur — Additif »), `impact: String`, `faitEtabli: String`, `nuance: String`, `cibleParticulierement: String`.
  - Invariant : `nom` ancrable dans le `ValidatedIngredientSegment` (**IHI-C-FR-005** / **IHI-N-FR-005**).

- `FullIngredientStatutEntry` *(ligne de la liste compacte — `IHI-N-FR-011`)*
  - `nom: String`, `statut: IngredientVigilanceStatut` (`RAS` | `MODERE` | `ELEVE`).
  - Invariant : `nom` ancrable dans le segment.

- `ProfileCritiqueResult` *(sortie parsée profil unique — supersede `ParsedHealthSections` 4-sections)*
  - `profile: UserProfile`, `evaluatedForHeader: String` (« Évalué pour vous : <label> »), `prudenceLevel: PrudenceLevel?`, `prudenceJustification: String?`, `riskCards: List<IngredientRiskCard>`, `fullIngredientList: List<FullIngredientStatutEntry>`, `warnings: List<String>`, `disclaimer: String`, `isDefaultProfile: Boolean`.
  - Invariant : si la sortie contient 4 marqueurs (format legacy), `warnings` signale le rejet et le résultat n'est pas un succès (`IHI-N-FR-013` / `IHI-N-SC-009`).

- `EvaluatedForHeader` *(value object)* : chaîne « Évalué pour vous : <profil label> » (`IHI-N-FR-003`).

## Validation rules (Feature N)

- `HealthCritiquePromptBuilder.buildSystemInstruction(profile)` MUST exiger uniquement le marqueur du profil sélectionné + rappel « Évalué pour vous : <label> » (`IHI-N-SC-001`/`002`).
- `HealthCritiqueSectionParser` MUST extraire (marqueur unique, prudence, cartes, liste compacte) ; MUST rejeter une sortie 4-marqueurs comme `non-analysable-response` (`IHI-N-FR-013` / `IHI-N-SC-009`).
- Chaque `IngredientRiskCard.nom` / `FullIngredientStatutEntry.nom` MUST être ancrable dans le segment (Feature C / `IHI-N-FR-005`).
- En l'absence de profil (`UserProfileProvider.current()` → `DEFAULT`), `isDefaultProfile = true` et rappel « Évalué pour vous : Adulte » (`IHI-N-FR-012` / `IHI-N-SC-008`).
- La restitution UI n'affiche en clair que les `IngredientRiskCard` (vigilance Modérée/Élevée) ; le bouton « Voir tous les ingrédients analysés » déploie `fullIngredientList` (`IHI-N-FR-010`/`011`, `IHI-N-SC-005`/`006`).

---

## Feature O — Critique santé intégrée à l'écran principal des résultats

### Entités (Feature O)

- `InlineCritiqueSection` *(section de `LlmResultScreen` rendant la critique inline)*
  - Rend les états `en cours` / `erreur` / `prête` du `HealthCritiqueViewModel` (collecte `ui` + `streamingText`), en continuité sous le bilan composition / pastille kcal / KPI additifs juxtaposés.
  - Composables de restitution réutilisés depuis `HealthCritiqueResultScreen` : `CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem` (filtré Modéré/Élevé), `FullIngredientListToggle`, actions « Copier la réponse » / « Copier le prompt ».
  - Invariant : aucune navigation ; aucune route séparée ; restitution 100 % inline (`IHI-O-FR-002` / `IHI-O-FR-005`).

- `CritiqueAutoTrigger` *(règle de déclenchement automatique — `IHI-O-FR-001`)*
  - Condition : `streamingBilan == StreamingBilanState.Complete` **et** `lastValidatedSegmentForHealth` non vide.
  - Mécanisme : `LaunchedEffect(streamingBilan, validatedSegment)` → `healthCritiqueViewModel.analyze()`.
  - Invariant : idempotent (un seul `analyze()` par `Complete` — `IHI-O-FR-013`) ; annulation propre au retour (`IHI-O-FR-014`) ; non-déclenchement si bilan `Error` ou segment vide (`IHI-O-FR-010`).

### Entités retirées (supersede Feature M)

- `HealthCritiqueEntryRoute` *(ancienne constante `CameraFlowRoutes.HealthCritiqueEntry`)* — **supprimé** (`IHI-O-FR-004`, supersede `IHI-M-FR-001`).
- `CritiqueSanteEntryTrigger` *(ancien bouton « Critique santé » de `LlmResultScreen`)* — **supprimé** (`IHI-O-FR-003`, supersede `IHI-M-FR-002`).
- `HealthCritiqueScreen` (écran d'entrée) + `HealthCritiqueResultScreen` (écran de restitution séparé) + route `HealthCritiqueResult` — **supprimés** (`IHI-O-FR-004` / `IHI-O-FR-005`).

## Validation rules (Feature O)

- `LlmResultScreen` MUST déclencher `healthCritiqueViewModel.analyze()` automatiquement quand `streamingBilan == Complete` + segment validé non vide (`IHI-O-FR-001` / `IHI-O-SC-001`).
- `LlmResultScreen` MUST rendre les états `en cours` / `erreur` / `prête` **inline** ; `InferenceError`/`InputInvalid` ne casse pas le bilan composition affiché (`IHI-O-FR-006` / `IHI-O-SC-004`).
- Le `NavHost` de `MainActivity` MUST NOT contenir `HealthCritiqueEntry` ni `HealthCritiqueResult` ; `LlmResultScreen` MUST NOT contenir le bouton `llm_result_critique_sante` (`IHI-O-FR-003`/`004`/`005`, `IHI-O-SC-003`).
- `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser` MUST être inchangés (`IHI-O-FR-007` / `IHI-O-SC-005`).
- Un même `Complete` MUST déclencher au plus une inférence critique (`IHI-O-FR-013` / `IHI-O-SC-008`).
- `LastHealthAnalysisStore` MUST être conservé sans écran séparé (`IHI-O-FR-011`).
