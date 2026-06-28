# Research — ancrage anti-hallucination (Feature C)

**Date**: 2026-05-13 | **Domain**: `ingredient-health-intelligence`

Les « inconnues » techniques ont été résolues par la spec + session **clarify** ; ce document consolide les **décisions d’implémentation** pour la Phase 1.

## 1. Politique d’équivalence v1 (stricte)

- **Decision** : v1 = sous-chaînes **littérales** du `ValidatedIngredientSegment` ; politique d’équivalence **vide** sauf **normalisations mécaniques** explicitement listées (casse, espaces, Unicode normal form si documenté).
- **Rationale** : minimize false « synonym » matches that look like grounded claims.
- **Alternatives considered** : catalogue minimal de synonymes (rejeté pour v1 par clarify Option A).

## 2. Contenu général vs « ce produit »

- **Decision** : blocs généraux autorisés s’ils sont **identifiables** ; toute mention explicite **ce produit** → uniquement termes **littéralement** dans le segment (**IHI-C-FR-004** b + **IHI-C-FR-005**).
- **Rationale** : garde la valeur éducative sans confondre avec l’étiquette.
- **Alternatives considered** : interdire tout contenu général dans un succès (trop restrictif — clarify Option B retenue).

## 3. Ancrage partiel

- **Decision** : **tout ou rien** — aucun succès avec analyse produit tronquée ; échec contrôlé (**IHI-C-FR-003**).
- **Rationale** : audit simple, pas de UX « demi-vérité ».
- **Alternatives considered** : livraison partielle avec label UI (rejetée — clarify Option A).

## 4. Juxtaposition `additive-risk-insights`

- **Decision** : enrichissements autorisés **si** (a) additif **littéralement** dans le segment, (b) **attribution explicite** au domaine additifs, (c) pas de confusion dominante avec le texte étiquette (**IHI-C-FR-007**).
- **Rationale** : respecte la **Published Language** du domain-map sans faire du LLM une source additive.
- **Alternatives considered** : tout interdire hors segment textuel (rejeté — clarify Option B).

## 5. Vérification indépendante (MVP)

- **Decision** : **relecture humaine** + traçabilité suffisent ; pas de gate automatisée obligatoire sur chaque succès au MVP.
- **Rationale** : aligné clarify Option A et charge projet ; checks automatisés possibles en itération suivante.
- **Alternatives considered** : gate CI bloquant sur chaque réponse (reporté post-MVP).

## 6. État de l’implémentation actuelle (baseline)

- **Decision** : conserver `CompositionResultValidator.validateAgainstSource` comme point central mais le **resserrer** pour se rapprocher du ratio / règles spec (aujourd’hui seuil **50 %** de lignes checkables absentes — à faire évoluer vers politique **stricte** clarify).
- **Rationale** : moindre risque de régression que remplacer entièrement par un nouveau pipeline opaque.
- **Alternatives considered** : validation uniquement LLM-side par prompt (insuffisant pour garanties testables).

## 7. Dette spec Feature B (implémenté 2026-05-13)

- **Note** : le code `composition/` et `healthcritique/` a été aligné sur Feature C sans backfill exhaustif de **Feature B** dans `spec.md` ; poursuivre `/speckit-sync-backfill` pour refermer l’écart doc ↔ code.

## 8. Estimation énergétique Feature K (2026-05-13)

- **Decision** : section dédiée **`###ENERGIE_ESTIMEE`** placée **après** `###ANALYSE` et **avant** `###ADDITIFS_RISQUE` ; première valeur entière **kcal pour 100 g** lisible sur la première ligne du bloc (ex. `420` ou `kcal_pour_100g: 420`).
- **Rationale** : même flux LLM, parseur déjà sectionné ; ordre compatible avec extension progressive des prompts (LiteRt 6 sections, Hybrid 6, Android 5 → alignés).
- **Alternatives considered** : second passage LLM dédié (rejeté v1 — coût/latence) ; heuristique locale seule (rejeté v1 — spec privilégie sortie analysable du même flux).

## 9. Bornes d’affichage kcal/100 g

- **Decision** : entier **inclus 1..1100** (**clarify** 2026-05-13, **Option B**) ; toute valeur hors intervalle ou non parseable → `estimatedKcalPer100g = null` (pastille sans nombre trompeur).
- **Rationale** : aligné **IHI-K-FR-006** / **IHI-K-SC-002** ; couvre huiles et matrices très denses (~900 kcal/100 g) sans accepter valeurs absurdes (ex. 12_000).
- **Alternatives considered** : **1..950** (impl antérieure — trop strict vs spec) ; plage 50..900 (trop stricte) ; laisser 0 (rejeté, min = 1).

## 10. Personnalisation du prompt de critique santé (Feature L — 2026-06-28)

### 10.1 Mécanisme de personnalisation

- **Decision** : **remplacement en dur versionné** du contenu du prompt dans `HealthCritiquePromptBuilder` (contenu intégré au code, testable, répétable) ; pas d'externalisation configurée ni de registre de prompts.
- **Rationale** : aligné avec l'existait (`HealthCritiquePromptBuilder`), MVP testabilité/répétabilité (`IHI-L-SC-007`), pas de complexité de configuration hors périmètre.
- **Alternatives considered** : externalisation dans asset/fichier de config modifiable sans recompilation (rejeté — YAGNI, complexité non justifiée) ; registre versionné de prompts sélectionnables (rejeté — sur-architecture).

### 10.2 Populations vulnérables sans section dédiée

- **Decision** : populations sans section propre (immunodéprimées, antécédents familiaux cancer) traitées comme **vigilance transversale intégrée** dans chaque section pertinente (Points de vigilance / Nuances), sans nouvelle section ni préambule.
- **Rationale** : préserve le format de sortie strict des 4 marqueurs (non-régression parser, `IHI-L-SC-004`/`SC-005`) tout en honorant l'attention particulière.
- **Alternatives considered** : sous-bloc dédié dans `###ADULTES` (rejeté — perd la transversalité) ; préambule commun avant `###ENFANTS` (rejeté — casse le format « aucun texte avant ###ENFANTS »).

### 10.3 Périmètre critique seule

- **Decision** : personnalisation **limitée au prompt de critique santé** ; prompt du bilan de composition non modifié.
- **Rationale** : le prompt fourni vise l'évaluation des risques alimentaires et la sortie par population (critique) ; fusion avec le bilan composition (contrat distinct, pastille kcal — Feature K) introduirait un couplage hors scope.
- **Alternatives considered** : appliquer aux deux flux (rejeté — couplage inter-contrats) ; extraire un socle commun persona+hiérarchie (rejeté pour Feature L — ferait l'objet d'une feature distincte si besoin).

### 10.4 Seuil « liste très longue »

- **Decision** : seuil défini en **nombre d'ingrédients** (ex. ≥ 20), valeur exacte laissée au plan d'implémentation (constante `LONG_LIST_INGREDIENT_THRESHOLD` dans `HealthCritiqueConfig`), non en caractères.
- **Rationale** : la notion de « longue » renvoie au nombre d'ingrédients à analyser (charge de lecture), pas à la longueur textuelle déjà plafonnée par `MAX_INGREDIENT_TEXT_CHARS`.
- **Alternatives considered** : seuil en caractères aligné sur `MAX_INGREDIENT_TEXT_CHARS` (rejeté — mal corrélé à la charge d'analyse) ; pas de seuil chiffré, modèle juge (rejeté — critère non testable).

### 10.5 Validation de la conformité au prompt (MVP)

- **Decision** : conformité sémantique (persona, hiérarchie des preuves, populations vulnérables, garde-fous) tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe d'ingrédients (aligné `IHI-C-FR-006` MVP) ; format de sortie vérifié par le parseur existant (`IHI-L-SC-005`).
- **Rationale** : la conformité sémantique sur texte libre n'est pas fiablement automatisable au MVP ; le format, lui, est contractuel et parsable.
- **Alternatives considered** : audit automatisé bloquant sur chaque sortie (rejeté au MVP — reporté post-MVP) ; validation hybride parseur+LLM-juge (rejeté au MVP — coût/complexité).

### 10.6 Disclaimer

- **Decision** : conserver la constante `DISCLAIMER` existante (« Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé. ») déjà alignée sur le texte fourni par l'utilisatrice.
- **Rationale** : non-régression ; le disclaimer correspond déjà exactement à l'input Feature L.

## 11. Accès UI à la critique santé (Feature M — 2026-06-28)

### 11.1 Réutilisation de l'écran d'entrée existant

- **Decision** : réutiliser `HealthCritiqueScreen` tel quel (champ lecture seule + bouton « Analyser ») ; l'enregistrer dans le `NavHost` via une nouvelle route `HealthCritiqueEntry` plutôt que de créer un nouvel écran.
- **Rationale** : l'écran existe et est validé par tests instrumentés (`HealthCritiqueReadOnlySegmentAndroidTest`) ; ne pas dupliquer (constitution V).
- **Alternatives considered** : créer un nouvel écran d'entrée (rejeté — duplication) ; fusionner entrée + résultat (rejeté — casserait la séparation existante).

### 11.2 Point d'entrée depuis le résultat composition

- **Decision** : exposer un bouton « Critique santé » dans `LlmResultScreen`, visible quand le bilan est dans un état terminal (`Complete`/`Error`), activé uniquement si un segment validé est disponible.
- **Rationale** : respecte la dépendance amont (segment validé synchronisé depuis le scan via `lastValidatedSegmentForHealth`) ; point d'entrée découvrable juste après le bilan composition.
- **Alternatives considered** : onglet persistant bas (rejeté au MVP — complexité structurelle) ; entrée depuis l'accueil/capture (rejeté — la critique dépend du segment issu du bilan).

### 11.3 Signal de disponibilité du segment

- **Decision** : utiliser le flux existant `cameraViewModel.lastValidatedSegmentForHealth` (déjà consommé dans `MainActivity` pour alimenter `setValidatedSegmentFromScan`) comme condition d'activation du bouton dans `LlmResultScreen`.
- **Rationale** : pas de nouvelle source de segment ; cohérent avec le câblage existant.
- **Alternatives considered** : dériver du `streamingBilan` `Complete` (insuffisant — la complétion du bilan ne garantit pas la disponibilité du segment validé synchronisé).

### 11.4 Wiring de navigation

- **Decision** : `LlmResultScreen` reçoit un callback `onCritiqueSante: () -> Unit` ; `MainActivity` passe `cameraNavController.navigate(CameraFlowRoutes.HealthCritiqueEntry)` ; une entrée `composable(HealthCritiqueEntry) { HealthCritiqueScreen(healthCritiqueViewModel) }` est ajoutée au `NavHost`.
- **Rationale** : pattern conforme au câblage existant de `LlmResult` et `HealthCritiqueResult` ; pas d'état partagé supplémentaire.
- **Alternatives considered** : navigation par événement via SharedFlow (rejeté — sur-engineering pour un simple bouton).

### 11.5 Non-régression

- **Decision** : la chaîne `analyze()` → `navigateToResult` → `HealthCritiqueResult` reste inchangée ; le flux composition et `LlmResultScreen` existant ne sont pas modifiés au-delà de l'ajout du bouton.
- **Rationale** : périmètre strict (constitution V) ; les garde-fous Feature C et L restent intacts.
- **Alternatives considered** : refactor de la navigation (rejeté — hors scope).

## 12. Critique ciblée par profil utilisateur (Feature N — 2026-06-28)

### 12.1 Cas d'absence de profil (fallback)

- **Decision** : **fallback implicite sur le profil par défaut « Adulte »** (profil unique, pas 4-profils) ; la critique est produite pour « Adulte » avec rappel « Évalué pour vous : Adulte » + signal visuel « profil par défaut » (invitation à personnaliser). Aucune critique 4-profils (pas de rétropédalage Feature L silencieux).
- **Rationale** : évite un refus frustrant (clarify Q1 Option C) tout en préservant le ciblage profil unique ; « Adulte » est le profil le plus neutre/général.
- **Alternatives considered** : exiger la saisie avant analyse (rejeté — friction) ; refuser poliment (rejeté — frustration) ; fallback 4-profils (rejeté — rétropédalage Feature L).

### 12.2 Contenu « Voir tous les ingrédients analysés »

- **Decision** : **liste compacte** (nom + statut de vigilance RAS / Modéré / Élevé) de tous les ingrédients analysés ; pas de cartes complètes pour les ingrédients RAS.
- **Rationale** : « liste complète » dans l'intention produit = consultation, pas re-déploiement de cartes ; limite la charge prompt/parseur/UI (clarify Q2 Option A).
- **Alternatives considered** : cartes complètes pour tous (rejeté — explosion visuelle) ; hybride cartes RAS + noms (rejeté — inutilement hybride).

### 12.3 « Alertes » au-dessus du Niveau de prudence

- **Decision** : les « alertes » désignent les **KPI additifs/risques existants** du domaine `additive-risk-insights` (juxtaposition régie par **IHI-C-FR-007**) ; Feature N n'introduit **pas** de nouveau bloc d'alertes ; le Niveau de prudence se place juste en dessous de cette zone KPI.
- **Rationale** : réutilise le contrat existant (Published Language additive) sans dupliquer (constitution V/VI) (clarify Q3 Option A).
- **Alternatives considered** : nouveau bloc « Alertes critiques » produit par le prompt critique (rejeté — couplage et nouvelle source) ; formulation générique sans ancrage KPI (rejeté — ambigu).

### 12.4 Suppression du flux 4-profils

- **Decision** : **suppression totale** du flux 4-profils (prompt 4-marqueurs, parseur 4-sections, UI 4-sections) ; seul le mode profil unique reste. **IHI-L-FR-009** / **IHI-L-SC-004** supersédés et retirés (traçabilité en spec). Le parseur MUST rejeter comme `non-analysable-response` toute sortie 4-marqueurs non conforme au format profil unique.
- **Rationale** : l'utilisatrice « se fiche des 3 profils qui ne la concernent pas » ; simplicité (constitution V) — une seule branche prompt/parseur/UI ; gain de tokens/latence (constitution IV) (clarify Q4 Option A).
- **Alternatives considered** : mode 4-profils caché/expert « comparer » (rejeté — YAGNI, double branche) ; support parseur 4-marqueurs pour back-compat (rejeté — entretient une voie morte).

### 12.5 Modifiabilité du profil hors Onboarding

- **Decision** : profil modifiable dans un écran **« Paramètres / Profil »** (UGE) en plus de l'Onboarding ; la critique suivante utilise le nouveau profil après changement. IHI ne fait que consommer le profil via `UserProfileProvider`.
- **Rationale** : modèle mobile standard, faible friction (clarify Q5 Option A) ; respecte la frontière DDD (persistance = UGE).
- **Alternatives considered** : fixé uniquement à l'Onboarding (rejeté — verrouillage) ; sélecteur rapide sur l'écran de critique (rejeté — mélange UX + risque de changement involontaire).

### 12.6 Contrat de consommation du profil (DDD)

- **Decision** : IHI définit un contrat `UserProfile` (enum 5 profils : `FEMME_ENCEINTE`, `ENFANT`, `PERSONNE_AGEE`, `ADULTE`, `SPORTIF` ; `label` français + `marker` canonique `###<...>` ; `DEFAULT = ADULTE`) et une interface `UserProfileProvider` (lit le profil courant). IHI fournit `DefaultUserProfileProvider` (fallback `ADULTE`, settable pour tests) ; l'implémentation persistée côté UGE (Onboarding + « Paramètres / Profil ») sera fournie par une feature UGE distincte et branchée sur le même contrat.
- **Rationale** : respecte la frontière DDD (constitution VI) — IHI consomme, UGE persiste ; Published Language du profil via l'enum + interface ; MVP testable sans attendre UGE (provider par défaut).
- **Alternatives considered** : IHI stocke lui-même le profil (rejeté — fuite de frontière) ; UGE définit l'enum (rejeté — IHI est le consommateur canon du langage profil critique).

### 12.7 Format de sortie profil unique (prompt)

- **Decision** : le prompt exige, pour le profil sélectionné, la structure suivante (après rappel « Évalué pour vous : <label> ») :
  - ligne `###<MARKER>` (marqueur canonique du profil, ex. `###FEMME_ENCEINTE`) ;
  - bloc **Niveau de prudence** : `Niveau de prudence : <Faible|Modéré|Élevé> — <texte court justificatif>` ;
  - bloc **Cartes d'ingrédients à vigilance** : pour chaque ingrédient Modéré/Élevé, une entrée `• <nom> | <code éventuel> | <type>` suivie des sous-lignes `Impact :`, `Fait établi :`, `Nuance :`, `Cible particulièrement :` ;
  - bloc **Liste complète des ingrédients analysés** : une ligne par ingrédient `- <nom> : <RAS|Modéré|Élevé>`.
  Aucun texte de critique avant le rappel « Évalué pour vous ». Les exigences Feature L non format-strict (persona, 5 dimensions, hiérarchie, garde-fous, populations vulnérables transversales, disclaimer, seuil liste longue, langue illisible) restent intégrées au prompt.
- **Rationale** : structure parsable (palier + cartes + liste) alignée sur la restitution UI (jauge + cartes filtrées + « Voir tous ») ; préserve l'ancrage Feature C (chaque ingrédiment cité ancrable dans le segment).
- **Alternatives considered** : JSON structuré (rejeté — Gemma local plus fiable en markdown léger) ; cartes pour tous les ingrédients (rejeté — clarify Q2).

### 12.8 Validation MVP

- **Decision** : conformité sémantique (ciblage profil unique, rappel « Évalué pour vous », structure des cartes, niveau de prudence) tenue par **relecture humaine + traçabilité** sur un jeu fixe (aligné `IHI-C-FR-006` MVP) ; le format de sortie (marqueur unique + blocs) est vérifié par le parseur étendu (tests JVM `IHI-N-SC-009`).
- **Rationale** : la conformité sémantique sur texte libre n'est pas fiablement automatisable au MVP ; le format, lui, est contractuel et parsable.
- **Alternatives considered** : audit automatisé bloquant sur chaque sortie (rejeté au MVP — reporté post-MVP).

---

## 13. Critique santé intégrée à l'écran principal des résultats (Feature O)

### 13.1 Déclenchement automatique (clarify 2026-06-28 — Option A)

- **Decision** : la critique santé est déclenchée **automatiquement** dès que `CameraViewModel.streamingBilan` passe à `StreamingBilanState.Complete` **et** que `lastValidatedSegmentForHealth` est non vide. Implémentation via `LaunchedEffect(streamingBilan, validatedSegment)` dans `LlmResultScreen` appelant `healthCritiqueViewModel.analyze()` ; aucune action utilisateur, aucun bouton, aucune navigation. Le bilan composition est rendu immédiatement (non-bloquant) ; la critique s'affiche en streaming inline.
- **Rationale** : aligne l'UX sur l'attente produit (« présente sur l'écran principal ») ; supprime la friction du bouton (Feature M supersédée) ; la seconde inférence est non-bloquante donc n'impacte pas la perception du bilan.
- **Alternatives considered** : bouton sur place « Analyser la critique » (rejeté — friction) ; streaming différé après scroll (rejeté — complexité, valeur faible).

### 13.2 Suppression des écrans/route séparés (clarify 2026-06-28 — Option A)

- **Decision** : supprimer `HealthCritiqueScreen.kt` (entrée + bouton « Analyser »), `HealthCritiqueResultScreen.kt` (écran de restitution séparé), la route `CameraFlowRoutes.HealthCritiqueEntry` et la route `CameraFlowRoutes.HealthCritiqueResult` + leurs entrées `composable(...)` dans le `NavHost` de `MainActivity`. Le callback `onCritiqueSante` de `LlmResultScreen` est retiré. La restitution est **100 % inline** sur `LlmResultScreen`.
- **Rationale** : évite la duplication de surfaces UI (Feature M supersédée) ; pile de navigation simplifiée (retour direct résultat → scan) ; cohérent avec le déclenchement automatique (plus besoin d'écran d'entrée).
- **Alternatives considered** : conserver les écrans séparés en plus du rendu inline (rejeté — duplication, confusion UX).

### 13.3 Extraction des composables de restitution

- **Decision** : les composables de restitution de `HealthCritiqueResultScreen` (`CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem`, `FullIngredientListToggle`, et les actions « Copier la réponse » / « Copier le prompt ») sont **extraits** vers une section inline de `LlmResultScreen` (ou un composable partagé `InlineCritiqueSection` dans le module `result`/`healthcritique`). La logique de `HealthCritiqueViewModel` (états `ui`/`streamingText`/`isLoading`/`result`, `analyze()`) est **consommée** par `LlmResultScreen` via le ViewModel injecté. `navigateToResult` (SharedFlow) devient **no-op** ou est retiré (plus de navigation vers `HealthCritiqueResult`).
- **Rationale** : réutilisation des composables éprouvés (non-régression de la restitution Feature N) ; le moteur/parseur/prompt restent inchangés.
- **Alternatives considered** : ré-écrire les composables dans `LlmResultScreen` (rejeté — duplication, DRY) ; conserver `HealthCritiqueResultScreen` comme composable embedded sans route (rejeté — clarif Option A = suppression).

### 13.4 Idempotence et annulation

- **Decision** : le `LaunchedEffect` d'auto-trigger est gardé par l'état du `HealthCritiqueViewModel` (un seul `analyze()` par `Complete`) — idempotence (`IHI-O-FR-013`). Au retour (`onBack` / `popBackStack`), le job d'inférence en cours est **annulé proprement** (coroutine scope du ViewModel / `viewModelStore` cleared) — pas de fuite d'inférence (`IHI-O-FR-014`, edge case).
- **Rationale** : évite les doubles inférences (coût LLM) et les fuites (battery/perf) ; cohérent constitution IV.
- **Alternatives considered** : laisser tourner l'inférence en arrière-plan au retour (rejeté — fuite, UX confusion).

### 13.5 États inline et non-déclenchement

- **Decision** : les états `en cours` (loading + `streamingText`), `erreur` (`InferenceError` / `InputInvalid`), et `prête` (`CritiqueReady`) sont rendus **inline** dans la section critique de `LlmResultScreen`. La critique **n'est pas déclenchée** si le bilan est en `Error` ou si le segment validé est vide au `Complete` (section critique masquée / état neutre — `IHI-O-FR-010`).
- **Rationale** : robustesse du rendu inline ; non-régression du bilan composition (l'erreur critique ne casse pas le bilan affiché au-dessus).
- **Alternatives considered** : afficher un message d'erreur critique bloquant (rejeté — le bilan reste consultable).

### 13.6 Persistance et actions copier (révision 2026-06-28)

- **Decision** : `LastHealthAnalysisStore` est **conservé** (persistance du dernier résultat critique) et consommé inline (rotation/process death) sans dépendre d'un écran séparé (`IHI-O-FR-011`). Les actions « Copier la réponse » et « Copier le prompt » sont **retirées** de la section critique inline (`IHI-O-FR-009` supersédé) — décision produit post-livraison Feature O (allègement UI).
- **Rationale** : non-régression de la persistance Feature B ; l'écran principal reste lisible sans actions secondaires.
- **Alternatives considered** : conserver les actions copier (rejeté — produit) ; déplacer les actions copier dans un menu global (rejeté — hors périmètre, friction).

### 13.7 Délégation de l'inférence critique au gateway composition (correctif 2026-06-28)

- **Decision** : `LiteRtHealthCritiqueRunner` ne pilote plus son propre `Engine` LiteRT-LM (wrapper `CompletableFuture.supplyAsync` + `synchronized` + rétention d'engine + `sendMessage`/`sendMessageAsync` avec re-tentative sur le même engine). Il délègue à `HybridGemma4LocalGateway.inferStreaming(systemInstruction, userMessage, onPartial)` — **le même chemin d'inférence que la composition** (boucle backends NPU→GPU→CPU, `Engine` fermé en `finally`, streaming `sendMessageAsync`). Délai borné via `withTimeout(maxInferenceMs)` (Feature A) ; résolution modèle via `GemmaModelLocator` pour distinguer `GEMMA_NOT_FOUND` / `GEMMA_LOAD_FAILED` avant l'inférence.
- **Rationale** : le runner dédié levait une `IllegalStateException` (cycle de vie conversation/backend) sur **tous** les backends, y compris en `sendMessage` synchrone sur engine frais. La composition fonctionne via `HybridGemma4LocalGateway` sur le même appareil/modèle ; réutiliser ce chemin éprouvé élimine la divergence. `HybridGemma4LocalGateway` expose désormais `inferStreaming` générique (system + user fournis), `runAnalyzeOnBackend` ayant été paramétré (system/user extraits du prompt composition).
- **Alternatives considered** : `sendMessage` synchrone seul (essayé — échec identique) ; partager un `Engine` unique composition/critique (rejeté — refactor cross-domaine hors scope Feature O) ; wrapper `CompletableFuture` de deadline (rejeté — source du bug threading).

### 13.7 Non-régression et ordonnancement

- **Decision** : l'ordonnancement de `LlmResultScreen` est préservé — bilan composition → pastille kcal (Feature K) → KPI additifs juxtaposés (`additive-risk-insights`, `IHI-C-FR-007`) → section critique inline (`IHI-O-FR-012`). `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser` et le flux composition sont **inchangés** (`IHI-O-FR-007`).
- **Rationale** : respect des frontières DDD (constitution VI) ; Feature O = câblage + restitution, pas de logique métier nouvelle.
- **Alternatives considered** : réordonner l'écran (rejeté — hors scope, impact UX non spécifié).

