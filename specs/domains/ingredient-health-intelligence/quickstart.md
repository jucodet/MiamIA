# Quickstart — valider l’ancrage (Feature C)

## Prérequis

- Build debug installé sur device ou émulateur avec runtime Gemma disponible.
- Segment ingrédients de test (ou chaîne mock **Feature A**) sous la main.

## Parcours manuel (MVP — **IHI-C-FR-006**)

1. Saisir ou capturer une liste courte **A** (ex. 3 ingrédients réels).
2. Lancer l’analyse composition ; vérifier que **chaque ligne** du bilan « fait produit » correspond à une **sous-chaîne** visible dans **A** (casse/espaces : uniquement si la politique mécanique v1 les autorise).
3. Retirer volontairement un ingrédient du segment validé **B** ⊂ **A** ; relancer : le système doit **rejeter** ou **non-analysable** si le modèle invente l’ingrédient retiré (tout ou rien).
4. Ouvrir critique santé avec le même segment : tout passage présenté comme **ce produit** doit référencer un terme **littéralement** dans le segment.
5. Si KPI additifs affichés : confirmer **attribution** visible « additifs / risques » (ou libellé équivalent) et qu’aucun additif KPI n’apparaît sans token correspondant dans le segment.

## Tests automatisés (cible)

- Ajouter / étendre tests unitaires sur `CompositionResultValidator` et, une fois défini, validateur critique pour jeux **contre-exemples** alignés **SC-C-002** / **SC-C-004**.

## Implémentation (2026-05-13)

- Les KPI additifs sont construits avec `BuildAdditiveKpiDisplay(bilan, rawLlmTextForParsing, validatedIngredientSegment)` : le **premier** texte est la sortie brute du modèle (parse `###ADDITIFS_RISQUE`), le **second** est le segment ingrédients validé (filtrage ancrage).

## Fichiers utiles

- `app/src/main/java/com/miamia/composition/CompositionResultValidator.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueEngine.kt`
- `app/src/main/java/com/miamia/additives/BuildAdditiveKpiDisplay.kt`
- `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (orchestration)

---

## Quickstart — pastille kcal / 100 g (Feature K)

1. Lancer une analyse composition jusqu’au **succès** avec une sortie modèle incluant `###ENERGIE_ESTIMEE` et une valeur entière plausible (ex. `420`).
2. Vérifier en tête de carte résultat une pastille du type **analyse terminée** + nombre + **/ 100 g** + mention **estimée** (ou équivalent clair).
3. Refaire avec une valeur **hors 1..1100** ou section absente : aucun entier trompeur ; pastille cohérente (**US-K2**).
4. Confirmer que le libellé ne suggère pas une **déclaration nutritionnelle officielle**.

### Fichiers utiles (Feature K)

- `app/src/main/java/com/miamia/composition/GemmaBilanParser.kt`
- `app/src/main/java/com/miamia/composition/EnergyEstimateValidator.kt`
- `app/src/main/java/com/miamia/camera/BilanResultCard.kt`

---

## Quickstart — prompt de critique santé personnalisé (Feature L)

### Prérequis

- Build debug installé (ou tests JVM) ; runtime Gemma disponible pour le parcours LLM.
- Jeu fixe d'ingrédients de référence (ex. chaîne mock Feature A ou liste courte réelle).

### Parcours automatique (tests JVM — `IHI-L-SC-001`..`005`, `007`)

1. Exécuter `HealthCritiquePromptPrudenceTest` : vérifier la présence dans `buildSystemInstruction()` du persona expert (« nutrition clinique », « cancérologie préventive »), des 5 dimensions de risque (cancérogène, mutagène, neurotoxique, métabolique, inflammatoire), des 3 tiers de preuve (faits établis, incertitudes, hypothèses), des 4 populations vulnérables (immunodéprimées, antécédents familiaux cancer), du disclaimer, et des 4 marqueurs de section ordonnés.
2. Exécuter `HealthCritiqueSectionParserTest` : non-régression — 4 sections reconnues dans l'ordre `###ENFANTS` → `###FEMMES_ENCEINTES` → `###ADULTES` → `###PERSONNES_AGEES`.
3. Vérifier la répétabilité : deux appels `buildSystemInstruction()` + `buildUserMessage(list)` produisent un prompt identique.

### Parcours manuel (MVP — `IHI-L-SC-008`, aligné `IHI-C-FR-006`)

1. Saisir une liste d'ingrédients de test ; lancer la critique santé.
2. Vérifier (relecture humaine) que la sortie distingue visiblement, par ingrédient, faits établis / incertitudes / hypothèses, et évalue les dimensions de risque demandées.
3. Vérifier la présence d'une vigilance transversale pour immunodéprimées / antécédents familiaux cancer dans les sections pertinentes (sans section ni préambule ajouté).
4. Simuler une demande d'avis médical personnalisé : la sortie refuse poliment de poser un diagnostic/prescription et oriente vers un professionnel de santé, en conservant les 4 sections.
5. Avec une liste ≥ `LONG_LIST_INGREDIENT_THRESHOLD` ingrédients : vérifier une synthèse des risques majeurs en tête de la section 2 de chaque partie.

### Fichiers utiles (Feature L)

- `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueConfig.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueSectionParser.kt`
- `app/src/test/java/com/miamia/healthcritique/HealthCritiquePromptPrudenceTest.kt`
- `app/src/test/java/com/miamia/healthcritique/HealthCritiqueSectionParserTest.kt`
