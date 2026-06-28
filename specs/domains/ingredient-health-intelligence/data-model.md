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
  - Composables de restitution réutilisés depuis `HealthCritiqueResultScreen` : `CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem` (filtré Modéré/Élevé), `FullIngredientListToggle`. Les actions « Copier la réponse » / « Copier le prompt » sont **retirées** (`IHI-O-FR-009` supersédé).
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

---

## Feature P — Compte rendu restructuré (4 sections) + critique concise/visuelle par profil

### Entités (Feature P)

- `ReportSection` *(section ordonnée du compte rendu — `IHI-P-FR-001`)*
  - Valeurs canoniques dans l'ordre exact : `ProduitIdentifie` → `Synthese` → `VerdictParIngredient` → `CritiqueSante`.
  - Invariant : **exactement 4 sections** exposées sur le compte rendu `Complete` ; aucun section hors cadre ; ordre stable跨-états (`IHI-P-SC-001`/`008`).

- `ProduitIdentifie` *(section 1 — `IHI-P-FR-001`)*
  - Restitue le produit identifié (`CompositionBilan.identifiedProduct` + `productConfidence`) ou un **état neutre** « Produit non identifié » si absent (edge case).
  - Invariant : section **inconditionnelle** (toujours rendue sur `Complete`).

- `Synthese` *(section 2 — `IHI-P-FR-003`)*
  - Agrège : `CompositionEnergyPastille` (Feature K, kcal/100 g) **en tête** + texte d'analyse (`CompositionBilan.compositionAnalysis`) + `AdditiveKpiPanel` (KPI `additive-risk-insights`, attribution explicite `IHI-C-FR-007`).
  - Invariant : la section « Additifs » n'est plus une section autonome (fusion dans Synthèse) ; pas de perte d'information KPI.

- `VerdictParIngredient` *(section 3 — `IHI-P-FR-004`)*
  - Restitue les impacts santé ingrédient par ingrédient (`CompositionBilan.healthImpacts`, `IngredientHealthImpact`) ou un **état neutre** « Aucun ingrédient à vigilance identifié » si vide.
  - Remplace l'affichage brut du segment (liste « Ingrédients identifiés » supprimée — `IHI-P-FR-002`).
  - Invariant : section **inconditionnelle** (toujours rendue sur `Complete`).

- `ConciseVisualCritique` *(forme de restitution critique — section 4 — `IHI-P-FR-005`/`006`)*
  - Structure ordonnée : (1) rappel `EvaluatedForHeader` « Évalué pour vous : <profil> » (`IHI-N-FR-003`) + signal « profil par défaut » le cas échéant (`IHI-N-FR-012`) ; (2) `ProfileRiskHighlights` (pastilles risques profil) ; (3) `PrudenceGauge` (jauge 3 paliers + texte court, `IHI-N-FR-009`) ; (4) cartes `IngredientRiskCardItem` **repliables** (Feature N, `IHI-N-FR-010`) ; (5) `FullIngredientListToggle` (Feature N, `IHI-N-FR-011`).
  - Invariant : rendu par défaut **concis et visuel** (pas de mur narratif) ; profondeur accessible en repli.

- `ProfileRiskHighlight` *(pastille visuelle de risque profil — `IHI-P-FR-005`/`007`)*
  - Une pastille par `IngredientRiskCard` (ingrédient à vigilance Modérée/Élevée pour le profil sélectionné) : **nom** (+ `code` éventuel) + **marqueur de sévérité visuel** (couleur / emoji dérivé du niveau de vigilance).
  - Mis en évidence **en tête de la critique**, avant la jauge.
  - Invariant : chaque ingrédient mentionné MUST être ancré dans le `ValidatedIngredientSegment` (`IHI-C-FR-005`) ; aucun risque inventé (`IHI-P-SC-005`). Si aucune carte : pastille neutre « Aucun risque marqué pour votre profil ».

### Entités retirées de l'UI (Feature P)

- `RawIngredientListDisplay` *(exposition à plat du `ValidatedIngredientSegment` — `IngredientsSection` du `BilanResultCard` + carte streaming « Ingrédients identifiés »)* — **supprimée de l'UI** (`IHI-P-FR-002`). Le segment reste entrée d'analyse (ancrage Feature C, inchangé).
- `AdditivesSection` *(section « Additifs » autonome)* — **supprimée comme section** ; le `AdditiveKpiPanel` est intégré à `Synthese` (`IHI-P-FR-003`).

## Validation rules (Feature P)

- Le compte rendu `Complete` MUST exposer **exactement 4 sections** dans l'ordre `ProduitIdentifie` → `Synthese` → `VerdictParIngredient` → `CritiqueSante` (`IHI-P-FR-001` / `IHI-P-SC-001`).
- `BilanResultCard` MUST NOT exposer `IngredientsSection` (test tag `bilan_ingredients_section` absent) ; `LlmResultScreen` MUST NOT exposer la carte streaming « Ingrédients identifiés » (test tag `streaming_ingredients_card` absent) (`IHI-P-FR-002` / `IHI-P-SC-002`).
- `Synthese` MUST contenir la pastille kcal (Feature K) et le `AdditiveKpiPanel` (attribution `IHI-C-FR-007`) ; la section « Additifs » autonome MUST être absente (`IHI-P-FR-003` / `IHI-P-SC-003`).
- `ProductSection` et `HealthImpactSection` MUST être inconditionnelles (états neutres si vide) — 4 sections stables跨-états (`IHI-P-FR-001` / `IHI-P-SC-008`).
- `CritiqueProfileContent` MUST afficher `ProfileRiskHighlights` (pastilles risques profil) entre le rappel « Évalué pour vous » et la `PrudenceGauge` (`IHI-P-FR-005` / `IHI-P-SC-004`).
- Chaque `ProfileRiskHighlight` MUST être ancré dans le `ValidatedIngredientSegment` (`IHI-C-FR-005`) ; 0 % risque inventé (`IHI-P-SC-005`).
- Les cartes détaillées `IngredientRiskCardItem` MUST rester repliables par défaut (profondeur non dominante — `IHI-P-FR-006` / `IHI-P-SC-006`).
- `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser`, flux composition, déclenchement automatique (Feature O) MUST être inchangés (`IHI-P-FR-008`/`009`/`012`).
