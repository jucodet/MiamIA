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

---

## Quickstart — accès UI à la critique santé (Feature M)

### Prérequis

- Build debug installé sur device/émulateur avec runtime Gemma disponible.
- Un produit scannable sous la main (liste d'ingrédients imprimée).

### Parcours manuel (MVP — `IHI-M-SC-001`..`005`)

1. Scanner un produit jusqu'au **résultat composition** (`LlmResultScreen`, état `Complete`).
2. Vérifier la présence d'un bouton **« Critique santé »** sous la carte de résultat (test tag `llm_result_critique_sante`).
3. Appuyer sur **« Critique santé »** : la navigation atteint `HealthCritiqueScreen` ; la liste d'ingrédients s'affiche **en lecture seule**, synchronisée avec le segment validé du scan.
4. Appuyer sur **« Analyser »** : la navigation atteint `HealthCritiqueResultScreen` (chaîne `analyze()` → `navigateToResult` inchangée).
5. Vérifier l'affichage des sections par population : **ENFANTS**, **FEMMES ENCEINTES**, **ADULTES**, **PERSONNES AGEES** (titres + corps ; les marqueurs `###` ne s'affichent pas littéralement, par conception du parseur — `IHI-L-SC-005`).
6. Tester le **retour** depuis `HealthCritiqueResultScreen` puis `HealthCritiqueScreen` : retour au résultat composition sans état cassé.
7. Cas sans segment validé : le bouton **« Critique santé »** est désactivé/masqué (`IHI-M-FR-003`).

### Fichiers utiles (Feature M)

- `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt`
- `app/src/main/java/com/miamia/result/LlmResultScreen.kt`
- `app/src/main/java/com/miamia/MainActivity.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueScreen.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueViewModel.kt`

---

## Quickstart — critique ciblée par profil utilisateur (Feature N)

### Prérequis

- Build debug installé (ou tests JVM) ; runtime Gemma disponible pour le parcours LLM.
- Jeu fixe d'ingrédients de référence (ex. chaîne mock Feature A ou liste courte réelle).

### Parcours automatique (tests JVM — `IHI-N-SC-001`..`010`)

1. Exécuter `UserProfileTest` : vérifier les 5 profils (labels français + marqueurs canoniques `###FEMME_ENCEINTE` / `###ENFANT` / `###PERSONNE_AGEE` / `###ADULTE` / `###SPORTIF`) et `DEFAULT = ADULTE`.
2. Exécuter `HealthCritiqueProfilePromptTest` (pour ≥ 2 profils, ex. `FEMME_ENCEINTE` et `SPORTIF`) :
   - `buildSystemInstruction(profile)` contient le rappel « Évalué pour vous : <label> » et **uniquement** le marqueur du profil sélectionné ;
   - les autres marqueurs de profil sont **absents** ;
   - les blocs exigés sont présents : « Niveau de prudence », « Impact », « Fait établi », « Nuance », « Cible particulièrement », « Liste complète des ingrédients analysés » ;
   - héritage Feature L préservé (persona « nutrition clinique »/« cancérologie préventive », 5 dimensions, garde-fous « diagnostic »/« prescription », disclaimer).
3. Exécuter `HealthCritiqueSectionParserTest` (étendu Feature N) :
   - une sortie profil unique (rappel + `###ADULTE` + prudence + cartes + liste compacte) est parsée : `prudenceLevel` non null, `riskCards` et `fullIngredientList` remplis ;
   - une sortie 4-marqueurs legacy (`###ENFANTS`…`###PERSONNES_AGEES`) est **rejetée** (warning + résultat non succès).
4. Vérifier la répétabilité : deux appels `buildSystemInstruction(profile)` + `buildUserMessage(list)` produisent un prompt identique.

### Parcours manuel (MVP — `IHI-N-SC-012`, aligné `IHI-C-FR-006`)

1. Sans profil sélectionné (provider par défaut) : lancer la critique → l'écran affiche « Évalué pour vous : Adulte » + un badge « profil par défaut » (invitation à personnaliser).
2. With `UserProfileProvider` retournant `FEMME_ENCEINTE` : lancer la critique → rappel « Évalué pour vous : Femme enceinte », **une seule** section, marqueur `###FEMME_ENCEINTE` attendu.
3. Vérifier (relecture humaine) que la sortie ne contient **aucune** section ENFANTS/ADULTES/PERSONNES_AGEES/SPORTIF.
4. Vérifier l'affichage du **Niveau de prudence** (jauge 3 paliers + texte court) sous la zone KPI additifs.
5. Vérifier que seules les cartes d'ingrédients à vigilance (Modérée/Élevée) sont affichées en clair (pas de carte « RAS »).
6. Appuyer sur **« Voir tous les ingrédients analysés »** : la liste compacte (nom + statut RAS/Modéré/Élevé) se déploie.
7. Simuler une sortie 4-marqueurs (jeu de test) : le parseur la rejette ; l'écran n'affiche pas un succès.

### Fichiers utiles (Feature N)

- `app/src/main/java/com/miamia/healthcritique/UserProfile.kt`
- `app/src/main/java/com/miamia/healthcritique/UserProfileProvider.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueSectionParser.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueModels.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueEngine.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueViewModel.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueResultScreen.kt`
- `app/src/test/java/com/miamia/healthcritique/UserProfileTest.kt`
- `app/src/test/java/com/miamia/healthcritique/HealthCritiqueProfilePromptTest.kt`
- `app/src/test/java/com/miamia/healthcritique/HealthCritiqueSectionParserTest.kt`

---

## Feature O — Critique santé intégrée à l'écran principal des résultats (2026-06-28)

### Parcours manuel (auto-trigger + restitution inline — `IHI-O-SC-001`..`008`)

1. Scanner un produit → bilan composition succès (`StreamingBilanState.Complete`) avec segment validé disponible.
2. Vérifier que **la section « Critique santé » s'affiche directement sur `LlmResultScreen`**, en continuité sous le bilan / pastille kcal / KPI additifs, **sans aucune action utilisateur ni navigation** (`IHI-O-SC-001`/`002`).
3. Vérifier l'absence du bouton « Critique santé » (test tag `llm_result_critique_sante` supprimé) et l'absence de route `HealthCritiqueEntry` / `HealthCritiqueResult` (`IHI-O-SC-003`).
4. Vérifier que la critique passe par l'état `en cours` (loading + streaming texte) puis `prête` (rappel « Évalué pour vous : <profil> » + jauge 3 paliers + cartes Modéré/Élevé + bouton « Voir tous les ingrédients analysés ») inline.
5. Simuler une erreur d'inférence critique (runtime indisponible) : le message d'erreur s'affiche **inline** dans la section critique, le bilan composition reste intact au-dessus (`IHI-O-SC-004`).
6. Simuler un bilan composition en `Error` : la critique **n'est pas déclenchée** ; la section critique est absente / neutre (`IHI-O-SC-006`).
7. Simuler un segment validé vide au `Complete` : la critique n'est pas déclenchée.
8. Appuyer sur « Retour » : retour direct au scan (pas d'écran critique intermédiaire) ; l'inférence en cours est annulée proprement (`IHI-O-SC-007`).
9. Re-scanner le même produit : un seul `Complete` → une seule inférence critique (idempotence — `IHI-O-SC-008`).
10. Vérifier les actions « Copier la réponse » / « Copier le prompt » au niveau de la section critique inline.
11. Non-régression : flux composition, KPI additifs juxtaposés, moteur/prompt/parseur inchangés (`IHI-O-SC-005`).

### Fichiers utiles (Feature O)

- `app/src/main/java/com/miamia/result/LlmResultScreen.kt`
- `app/src/main/java/com/miamia/MainActivity.kt`
- `app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueViewModel.kt`
- (supprimés) `app/src/main/java/com/miamia/healthcritique/HealthCritiqueScreen.kt`
- (supprimés) `app/src/main/java/com/miamia/healthcritique/HealthCritiqueResultScreen.kt`
- `app/src/androidTest/java/com/miamia/result/LlmResultScreenUiTest.kt`
- `app/src/androidTest/java/com/miamia/healthcritique/HealthCritiqueReadOnlySegmentAndroidTest.kt`
- `app/src/androidTest/java/com/miamia/healthcritique/HealthCritiquePersistenceAndroidTest.kt`
