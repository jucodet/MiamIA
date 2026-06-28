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
10. Vérifier l'**absence** des actions « Copier la réponse » / « Copier le prompt » dans la section critique inline (retirées — `IHI-O-FR-009` supersédé).
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

---

## Feature P — Compte rendu restructuré (4 sections) + critique concise/visuelle par profil (2026-06-28)

### Parcours manuel (4 sections ordonnées + critique concise/visuelle — `IHI-P-SC-001`..`010`)

1. Scanner un produit → bilan composition succès (`StreamingBilanState.Complete`).
2. Vérifier que le compte rendu expose **exactement 4 sections** dans cet ordre : **Produit identifié** → **Synthèse** → **Verdict par ingrédient** → **Critique santé** (`IHI-P-SC-001`).
3. Vérifier l'**absence** de la liste brute des ingrédients identifiés : test tag `bilan_ingredients_section` absent (Complete) et `streaming_ingredients_card` absent (streaming) (`IHI-P-SC-002`).
4. Vérifier que la **Synthèse** contient la pastille kcal (Feature K) et le panneau KPI additifs (`AdditiveKpiPanel`, attribution `additive-risk-insights`) ; la section « Additifs » autonome est absente (`IHI-P-SC-003`).
5. Vérifier la **Critique santé** : rappel « Évalué pour vous : <profil> » en tête, puis une **liste de pastilles visuelles** (une par ingrédient à vigilance Modérée/Élevée pour le profil) avant la jauge 3 paliers (`IHI-P-SC-004`).
6. Vérifier que les pastilles risques profil ne mentionnent que des ingrédients **présents dans l'étiquette** (ancrage Feature C) ; aucun risque inventé (`IHI-P-SC-005`).
7. Vérifier que les **cartes détaillées** (Impact / Fait établi / Nuance / Cible particulièrement) restent **repliables** par défaut (« Touchez pour le détail ») — profondeur non dominante (`IHI-P-SC-006`).
8. Vérifier le **déclenchement automatique** et la **restitution inline** de la critique (non-régression Feature O — `IHI-P-SC-007`).
9. Edge cases :
   - **Produit non identifié** : la section « Produit identifié » affiche un état neutre « Produit non identifié » (4 sections préservées — `IHI-P-SC-008`).
   - **Aucun ingrédient à vigilance** : la section « Verdict par ingrédient » affiche un état neutre ; la critique affiche une pastille neutre « Aucun risque marqué pour votre profil ».
   - **Profil par défaut (Adulte)** : signal visuel « profil par défaut » + pastilles risques Adulte (`IHI-P-SC-009`).
   - **Critique en cours / erreur** : la section 4 rend l'état inline sans casser les sections 1–3 (`IHI-P-SC-008`).
10. Non-régression : flux composition, KPI additifs juxtaposés, moteur/prompt/parseur Feature L/N, déclenchement Feature O — inchangés (`IHI-P-FR-008`/`012`).

### Fichiers utiles (Feature P)

- `app/src/main/java/com/miamia/camera/BilanResultCard.kt`
- `app/src/main/java/com/miamia/result/LlmResultScreen.kt`
- `app/src/main/java/com/miamia/healthcritique/InlineCritiqueSection.kt`
- `app/src/androidTest/java/com/miamia/result/LlmResultScreenUiTest.kt`
- `app/src/androidTest/java/com/miamia/healthcritique/HealthCritiqueReadOnlySegmentAndroidTest.kt`

---

## Feature Q — Concision maximale de la critique santé intégrée au prompt (2026-06-28)

### Parcours manuel (prompt concis + sortie concise préservant ancrage/garde-fous — `IHI-Q-SC-001`..`008`)

1. Inspecter le prompt construit par `HealthCritiquePromptBuilder.buildSystemInstruction(profile)` : vérifier la présence d'un bloc **CONCISION MAXIMALE** exigeant formulations courtes/denses, pas de préambule, pas de prose narrative, pas de répétitions (`IHI-Q-SC-001`).
2. Vérifier que la directive **borne** la concision au format strict Feature N (rappel + marqueur unique + 3 blocs obligatoires — `IHI-Q-SC-002`).
3. Produire une critique sur un jeu fixe d'ingrédients : vérifier que la sortie est **plus courte** (pas de préambule avant « Évalué pour vous : <profil> », formulations courtes, pas de prose narrative) qu'une sortie sans la directive, tout en restant parsable (marqueur + 3 blocs — `IHI-Q-SC-003`).
4. Vérifier l'ancrage : chaque ingrédient mentionné (cartes, liste complète) est **littéralement ancrable** dans le `ValidatedIngredientSegment` ; aucun fait inventé (`IHI-Q-SC-004`).
5. Vérifier les garde-fous : disclaimer présent, pas de diagnostic/prescription, références CIRC/OMS **compactes** quand applicables (`IHI-Q-SC-005`).
6. Vérifier la **répétabilité** : 2 exécutions de `buildSystemInstruction(profile)` → prompt identique (`IHI-Q-SC-006`).
7. Non-régression : `HealthCritiqueSectionParser` reconnaît toujours le marqueur unique + les 3 blocs ; moteur, flux composition, KPI additifs, restitution Feature P (4 sections, pastilles) inchangés (`IHI-Q-SC-007`).
8. Edge cases : (a) liste très longue → synthèse des risques majeurs concise en tête du bloc 2 ; (b) aucun ingrédient à vigilance → sortie courte (Niveau Faible + justificatif court + liste RAS) ; (c) terme ambigu → `Nuance` d'opacité en formulation courte ; (d) sortie trop courte/tronquée ne respectant pas le format → rejet `non-analysable-response` par le parseur.

### Fichiers utiles (Feature Q)

- `app/src/main/java/com/miamia/healthcritique/HealthCritiquePromptBuilder.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueConfig.kt`
- `app/src/test/java/com/miamia/healthcritique/` (tests JVM `buildSystemInstruction()`)
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueSectionParser.kt` (non-régression)
