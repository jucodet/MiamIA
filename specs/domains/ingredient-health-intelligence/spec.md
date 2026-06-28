# Domain Spec — ingredient-health-intelligence

**Domain Context**: `ingredient-health-intelligence`
**Created**: 2026-05-06
**Last Modified**: 2026-06-28 (Feature N — critique ciblée par profil utilisateur : prompt adapté + restitution prudence/cartes ingrédients)
**Status**: Draft

## Purpose

Analyser une liste d'ingrédients via le LLM local Gemma pour produire un bilan de composition et une critique santé par population. Ce domaine fournit aussi un test bouchonné isolé pour valider le flux d'appel LLM indépendamment de la capture et de l'OCR.

## Clarifications

### Session 2026-05-13

- Q: Politique d’équivalence (v1) au premier livrable ? → A: **Option A** — v1 **stricte** : ancrage par sous-chaînes littérales du `ValidatedIngredientSegment` ; équivalence initiale **vide**, sauf **normalisations mécaniques** explicitement énumérées (ex. casse, espaces) ; tout synonyme ou règle métier additionnelle entre en politique uniquement comme entrée **explicite et versionnée** ; hors politique = non ancré.
- Q: Critique santé et faits scientifiques / nutritionnels généraux ? → A: **Option B** — autorisés dans un bloc **clairement général** ; toute mention explicite de **ce produit** (ou équivalent) ne s’appuie que sur des désignations **littéralement présentes** dans le segment (selon **IHI-C-FR-005**) ; pas d’inférence « étiquette » non sourcée.
- Q: Réponse partiellement ancrée (une partie « fait produit » ancrée, une autre non) ? → A: **Option A** — **tout ou rien** : pas de succès avec analyse produit tronquée ; **IHI-C-FR-003** si l’ensemble des affirmations « fait produit » n’est pas entièrement ancré.
- Q: Faits additifs / read-model `additive-risk-insights` à côté du bilan LLM ? → A: **Option B** — enrichissement autorisé **si** attribution explicite au domaine `additive-risk-insights` (pas comme étiquette) **et** chaque sujet/additif est **littéralement** dans le segment (voir **IHI-C-FR-007**).
- Q: Vérification indépendante (**IHI-C-FR-006**) pour le MVP ? → A: **Option A** — **relecture humaine** et traçabilité suffisent ; pas d’exigence d’audit automatisé bloquant sur chaque succès au MVP (l’automatisation peut être ajoutée ultérieurement au plan).
- Q: Plage métier d’acceptation pour afficher un entier **N** (kcal / 100 g) issu du modèle (valeurs aberrantes — **Feature K**) ? → A: **Option B** — afficher **N** comme estimation chiffrée seulement si **1 ≤ N ≤ 1100** (kcal pour 100 g, après arrondi entier documenté) ; sinon traiter comme non fiable (**IHI-K-FR-004** / **US-K2**), sans nombre trompeur.

## Scope

- Test bouchonné du flux LLM local (entrée mockée → analyse → résultat/échec)
- Analyse de composition (bilan ingrédients via Gemma)
- Critique santé par population (enfants, femmes enceintes, adultes, personnes âgées)
- **Interdiction des hallucinations produit** : aucun fait sur la composition ou l’étiquette sans ancrage dans le segment validé (Feature C)
- Gestion des erreurs et limites (timeout, modèle indisponible, réponse non analysable)
- Persistance et consultation du dernier résultat
- Copie et partage des résultats
- Estimation indicative d’énergie (kcal/100 g) en synthèse lorsque le bilan composition le permet (**Feature K**)

## Invariants

- Chaque résultat d'analyse est associé à l'entrée qui l'a produit (traçabilité).
- Les sorties présentées comme analyse fiable du produit respectent l’ancrage sur le segment validé (aucun fait produit non étayé — Feature C).
- Les catégories d'échec sont normalisées : `timeout`, `runtime-unavailable`, `non-analysable-response`.
- Le test bouchonné est exécutable manuellement, hors suites automatiques.

---

## Feature A — Test LLM Mock Ingredients

> Origine : `016-test-llm-mock`
> Input : "Créer un test bouchonné ne mettant à l'épreuve que le processus de demander au LLM local l'analyse d'une liste d'ingrédients mockée."

### Clarifications (Feature A)

#### Session 2026-05-06

- Q: Critère de "réponse exploitable" ? → A: Succès si non vide et classée analysable par le parseur de test.
- Q: Politique de timeout ? → A: 180 secondes (timeout strict). *(Backfill P7 — 2026-05-12 : 30 s → 180 s pour réalisme Gemma local.)*
- Q: Catégories d'échec ? → A: `timeout`, `runtime-unavailable`, `non-analysable-response`.
- Q: Règle d'égalité entrée mockée ? → A: Stricte caractère par caractère.
- Q: Politique de validation projet ? → A: Test manuel uniquement, hors validation régulière.

### User Scenarios (Feature A)

#### US-A1 — Valider le flux d'appel LLM local (P1)

En tant que développeuse, je veux lancer un test bouchonné avec une liste d'ingrédients mockée fixe afin de vérifier le flux d'envoi vers le LLM local et de réception de réponse, indépendamment de l'OCR et de la capture.

**Acceptance Scenarios**:

1. **Given** entrée mockée exactement égale à la référence, **When** analyse déclenchée, **Then** requête transmise au moteur LLM local et réponse exploitable renvoyée.
2. **Given** test isolé des autres modules, **When** test exécuté, **Then** aucune dépendance à la caméra, l'OCR ou une entrée interactive.

#### US-A2 — Garantir l'intégrité de l'entrée analysée (P2)

En tant que développeuse, je veux que la chaîne mockée soit transmise telle quelle pour confirmer que l'analyse correspond exactement au texte source.

**Acceptance Scenarios**:

1. **Given** chaîne mockée de référence, **When** demande d'analyse construite, **Then** charge utile conserve exactement le même contenu textuel.
2. **Given** analyse terminée, **When** résultat journalisé, **Then** trace associe explicitement le résultat à l'entrée mockée.

#### US-A3 — Rendre les échecs explicites (P3)

En tant que développeuse, je veux un comportement d'échec lisible pour distinguer un problème de runtime local d'un problème de logique du test.

**Acceptance Scenarios**:

1. **Given** runtime local indisponible, **When** demande lancée, **Then** état d'échec explicite et actionnable.

#### Edge Cases (Feature A)

- Chaîne mockée vide ou sans ingrédients exploitables.
- Runtime local répond avec un contenu non interprétable.
- Réponse dépasse le délai attendu.

### Functional Requirements (Feature A)

- **IHI-A-FR-001**: Le système MUST exécuter un test bouchonné dédié au flux d'analyse LLM local sans dépendre de la capture caméra ni de l'OCR.
- **IHI-A-FR-002**: Le système MUST utiliser comme entrée de test unique la chaîne mockée suivante :
  `Ingredients. Sucre, farine de BLÉ 33 %, farine complète de BLÉ 15 %, huile de palme, huile de colza, amidon de BLÉ, sirop de glucose, poudres à lever (carbonates d'ammonium, carbonates de sodium), émulsifiant (lécithines de SOJA), sel, LAIT écrémé en poudre, LAIT entier en poudre, arômes.`
- **IHI-A-FR-003**: Le système MUST transmettre l'entrée mockée sans altération de contenu au processus d'analyse.
- **IHI-A-FR-004**: Le système MUST retourner un résultat indiquant clairement soit une analyse reçue, soit un échec explicite.
- **IHI-A-FR-005**: Le système MUST associer chaque résultat du test à l'entrée mockée utilisée pour traçabilité.
- **IHI-A-FR-006**: Le système MUST permettre l'exécution répétable du même scénario avec les mêmes attentes.
- **IHI-A-FR-007**: Le système MUST considérer une réponse comme exploitable uniquement si non vide et classée analysable par le parseur de test.
- **IHI-A-FR-008**: Le système MUST échouer automatiquement le test si aucune réponse exploitable n'est obtenue dans une fenêtre de 180 secondes. *(Backfill P7 — 2026-05-12 : 30 s → 180 s pour réalisme Gemma local.)*
- **IHI-A-FR-009**: Le système MUST classifier chaque échec dans : `timeout`, `runtime-unavailable`, `non-analysable-response`.
- **IHI-A-FR-010**: Le système MUST vérifier une égalité stricte caractère par caractère entre `MockIngredientInput` et la charge transmise.
- **IHI-A-FR-011**: Le système MUST être exécutable manuellement et ne fait pas partie des contrôles bloquants réguliers.

### Key Entities (Feature A)

- **MockIngredientInput**: Chaîne d'ingrédients de référence du test.
- **LlmAnalysisRequest**: Demande d'analyse générée à partir de `MockIngredientInput`.
- **LlmAnalysisOutcome**: Résultat observable (succès avec contenu, ou échec avec raison).
- **TestTraceRecord**: Lien de traçabilité entre entrée mockée, demande envoyée et résultat.

### Success Criteria (Feature A)

- **SC-A-001**: 100 % des exécutions → chaîne mockée de référence utilisée.
- **SC-A-002**: ≥ 95 % des exécutions en environnement prêt → réponse exploitable en < 180 s.
- **SC-A-003**: 100 % des échecs → état d'erreur explicite avec catégorie identifiable.
- **SC-A-004**: Scénario reproductible sur ≥ 3 exécutions successives.
- **SC-A-005**: 100 % des succès → réponse non vide classée analysable.
- **SC-A-006**: 100 % des exécutions > 180 s sans réponse → marquées `timeout`.
- **SC-A-007**: 100 % des échecs → catégorie dans la liste définie.
- **SC-A-008**: 100 % des requêtes → texte source conservé (égalité caractère par caractère).

---

## Feature B — Composition & Health Critique (Placeholder)

> Origine : sync-apply P14, 2026-05-12
> Source packages : `healthcritique/` (13 fichiers, ~1030 lignes), `composition/` (9 fichiers, ~728 lignes)
> Status : à compléter via `/speckit-sync-backfill`

### Scope (Feature B)

#### Analyse de composition (`composition/`)
- Bilan ingrédients via Gemma local (`CompositionAnalysisEngine`)
- Parser bilan (`CompositionBilanParser`)
- Validation résultat (`CompositionResultValidator`)
- Messages d'erreur (`CompositionErrorMessages`)
- Modèles de données composition

#### Critique santé (`healthcritique/`)
- Moteur de critique santé par population (`HealthCritiqueEngine`)
- Prompt builder (`HealthCritiquePromptBuilder`)
- Section parser (`HealthCritiqueSectionParser`)
- Écrans UI (résultat critique, clipboard)
- Persistance snapshot dernier résultat
- Gestion des erreurs et limites

### Functional Requirements (Feature B)

- *(à extraire du code via `/speckit-sync-backfill`)*

---

## Feature C — Interdiction des hallucinations sur l’analyse LLM

> Origine : intake 2026-05-13  
> Intention : interdire complètement au modèle de langage de produire des faits produit non fondés sur le segment validé.

### Clarifications (Feature C)

#### Session 2026-05-13

- « Hallucination » est prise au sens **métier** : affirmation présentée comme décrivant l’étiquette ou la composition du produit (ingrédient, quantité, pourcentage, allégation) **sans ancrage** dans le `ValidatedIngredientSegment` fourni en entrée.
- Les reformulations ou synonymes acceptés sont ceux explicitement couverts par une **politique d’équivalence** documentée (voir FR-C-005) ; tout le reste est hors périmètre d’ancrage.
- **V1 équivalence (clarify)** : politique **stricte** — équivalence initiale vide sauf normalisations mécaniques listées ; pas de catalogue implicite de synonymes.
- **Contenu général + critique santé (clarify)** : **Option B** — blocs éducatifs généraux autorisés s’ils sont identifiables comme tels ; toute formulation liant **ce produit** à un ingrédient ou un risque ne cite que des termes **littéralement ancrables** dans le segment (via **IHI-C-FR-005**).
- **Ancrage partiel (clarify)** : **Option A** — **tout ou rien** pour le statut succès sur la partie « fait produit » : pas de livraison d’analyse produit **tronquée** ou partiellement ancrée ; ancrage incomplet → **IHI-C-FR-003**.
- **Additifs / `additive-risk-insights` (clarify)** : **Option B** — enrichissements autorisés en juxtaposition **si** attribution explicite au read-model du domaine `additive-risk-insights` et ancrage **littéral** de chaque additif concerné dans le segment (voir **IHI-C-FR-007**).
- **Vérification MVP (clarify)** : **Option A** — **IHI-C-FR-006** satisfait au MVP par **procédure de relecture humaine** et traçabilité ; pas d’obligation d’outil automatisé d’audit d’ancrage sur chaque succès.
- « Interdire complètement » signifie : **aucune** telle affirmation ne peut être livrée à l’utilisateur dans un résultat classé comme analyse réussie ; en cas d’échec d’ancrage, le flux suit les catégories d’échec ou de dégradation déjà normalisées (`non-analysable-response` ou équivalent métier).

### User Scenarios (Feature C)

#### US-C1 — Confiance : pas d’ingrédient « inventé » (P1)

En tant qu’utilisatrice, je veux que l’analyse de composition et la critique santé ne mentionnent comme faits sur **mon** produit que ce qui est vérifiable à partir de la liste d’ingrédients que j’ai validée, afin de ne pas être induite en erreur.

**Acceptance Scenarios**:

1. **Given** un segment validé ne contenant pas l’ingrédient X, **When** l’analyse est produite et présentée comme résultat exploitable, **Then** aucune phrase ne présente X comme présent dans ce produit ni ne suggère une quantité ou un pourcentage pour X tirés de l’étiquette.
2. **Given** un segment validé minimal mais cohérent, **When** l’analyse est produite, **Then** toute liste d’ingrédients présentée comme celle du produit est un sous-ensemble fidèle (reformulations autorisées uniquement selon la politique d’équivalence documentée) du texte validé.
3. **Given** un segment contenant une mention textuelle d’additif et un bloc d’enrichissement `additive-risk-insights` affiché à côté d’une analyse LLM classée succès, **When** l’utilisatrice consulte l’écran, **Then** le bloc respecte **IHI-C-FR-007** (attribution explicite au domaine additifs, pas présenté comme libellé d’étiquette, additif littéralement dans le segment).

#### US-C2 — Refus explicite plutôt que boucher les trous (P2)

En tant qu’utilisatrice, je préfère un échec ou un message de non-analyse plutôt qu’une réponse qui comble les lacunes du texte par des suppositions présentées comme des faits.

**Acceptance Scenarios**:

1. **Given** une réponse générée contenant des segments non ancrables dans le segment validé, **When** le contrôle d’ancrage est appliqué, **Then** le résultat n’est pas livré comme analyse réussie et l’utilisatrice reçoit un état explicite (échec ou dégradation) sans faits produit inventés.
2. **Given** une demande implicite de détails absents du segment (ex. pourcentage non indiqué), **When** l’analyse est produite, **Then** le système n’affiche pas de chiffre ou d’allégation produit spécifique présentée comme issue de l’étiquette.

#### US-C3 — Séparer le général du particulier (P3)

En tant qu’utilisatrice, si des informations générales sur la nutrition sont affichées, je veux qu’elles ne soient pas confondues avec une lecture de mon étiquette.

**Acceptance Scenarios**:

1. **Given** du contenu éducatif générique affiché à proximité de l’analyse, **When** l’utilisatrice consulte l’écran, **Then** le contenu générique est identifiable comme tel et ne contredit pas US-C1.
2. **Given** une analyse classée succès incluant un bloc général et une phrase liant explicitement **ce produit** (ou formulation équivalente) à un ingrédient ou un risque, **When** le contrôle d’ancrage est appliqué, **Then** chaque ingrédient ou fait produit ainsi lié est étayé par une sous-chaîne admise du `ValidatedIngredientSegment` (selon **IHI-C-FR-005**).

#### Edge Cases (Feature C)

- Synonymes ou formes légitimes d’un même ingrédient (ex. « SOJA » / « lécithines de SOJA ») : en v1, **non** admis par défaut ; ils ne deviennent ancrables qu’après ajout explicite et versionné dans la politique d’équivalence.
- Segment partiellement illisible ou ambigu : pas d’extrapolation vers des ingrédients « probables » présentés comme certains.
- Réponse mêlant passages « fait produit » ancrés et non ancrés : **tout ou rien** pour un succès — confirmé clarify **Option A** : pas de succès avec portion tronquée ; **IHI-C-FR-003** s’applique.
- Pont « ce produit » depuis un bloc général : autorisé uniquement si chaque ancrage satisfait **IHI-C-FR-004** (b) et **IHI-C-FR-005** ; sinon **IHI-C-FR-003** s’applique (pas de livraison en succès avec pont invalide).
- Bloc enrichi `additive-risk-insights` : autorisé uniquement sous **IHI-C-FR-007** ; sinon ne pas l’afficher comme complément d’une analyse succès ou risquer la confusion avec le texte étiquette.

### Functional Requirements (Feature C)

- **IHI-C-FR-001**: Le système MUST traiter le `ValidatedIngredientSegment` comme **seule** source de vérité textuelle pour tout fait affirmé sur la composition ou le contenu du produit analysé.
- **IHI-C-FR-002**: Le système MUST NOT présenter comme issus de l’étiquette des ingrédients, quantités, pourcentages, mentions légales ou allégations qui ne sont pas ancrés dans le segment validé selon la politique d’équivalence de **IHI-C-FR-005**.
- **IHI-C-FR-003**: Le système MUST rejeter ou classer en échec contrôlé (catégorie `non-analysable-response` ou équivalent métier) toute réponse qui violerait **IHI-C-FR-001** ou **IHI-C-FR-002**, plutôt que de la livrer comme analyse réussie. **Clarify (2026-05-13)** : politique **tout ou rien** sur les affirmations « fait produit » — le système MUST NOT livrer un **succès** constitué d’une analyse produit **partiellement** ancrée (aucune livraison tronquée « uniquement les passages vérifiés » au titre d’une analyse complète).
- **IHI-C-FR-004**: Lorsque du contenu général (non spécifique au produit) coexiste avec une analyse dans le même parcours, le système MUST : (a) le présenter dans un bloc **identifiable comme contenu général**, distinct des affirmations « fait étiquette » ; (b) exiger que toute formulation liant explicitement **ce produit** (ou tournure équivalente) à un ingrédient, une quantité, un risque ou une allégation ne référence que des éléments **littéralement ancrables** dans le `ValidatedIngredientSegment` conformément à **IHI-C-FR-005**, sans inférence présentée comme issue de l’étiquette ; (c) ne pas présenter le contenu général comme une lecture d’étiquette.
- **IHI-C-FR-005**: Le système MUST publier et maintenir une politique d’équivalence textuelle **bornée** et **versionnée**. En **v1**, la politique MUST être **stricte** : tout ancrage admis repose sur une **sous-chaîne littérale** du `ValidatedIngredientSegment`, sauf entrées de **normalisation mécanique** explicitement énumérées (ex. casse, espaces) ; tout synonyme ou règle métier additionnelle MUST être une ligne explicite de la politique ; toute correspondance hors politique MUST être traitée comme **non ancrée**. Les extensions ultérieures de la politique MUST rester traçables (versionnement).
- **IHI-C-FR-006**: Le système MUST permettre la vérification indépendante : pour chaque résultat classé succès, un réviseur peut retrouver, pour chaque fait produit affirmé **dans le texte issu du flux LLM évalué**, l’extrait du segment validé qui l’étaye (ou l’entrée de la politique d’équivalence applicable). **Clarify MVP (2026-05-13)** : cette obligation est tenue par **relecture / procédure humaine** et artefacts de traçabilité ; le MVP **n’exige pas** de contrôle automatisé bloquant sur chaque succès (des checks automatisés peuvent être ajoutés hors périmètre minimal de cette exigence).
- **IHI-C-FR-007**: Le système MAY juxtaposer à une analyse LLM classée succès des contenus issus du read-model publié par le domaine `additive-risk-insights` **uniquement si** : (a) chaque additif ou sujet couvert par cet enrichissement correspond à une désignation **littéralement présente** dans le `ValidatedIngredientSegment` (selon **IHI-C-FR-005**) ; (b) l’enrichissement est **explicitement attribué** à `additive-risk-insights` (libellé ou identifiant d’attribution équivalent), et **not** présenté comme libellé ou texte d’étiquette ; (c) la juxtaposition ne crée pas d’ambiguïté dominante avec le bloc « fait étiquette » du LLM au sens de **IHI-C-FR-004** / **SC-C-003**.

### Key Entities (Feature C)

- **GroundedProductClaim**: affirmation sur le produit devant être ancrée dans le segment validé (ou équivalence documentée).
- **EquivalencePolicy**: règles explicites, limitées et versionnées de correspondance textuelle autorisée entre sortie et segment ; **v1** = stricte (voir **IHI-C-FR-005**).
- **AnchoringOutcome**: résultat du contrôle (entièrement ancré vs rejet / non analysable).
- **AttributedAdditiveInsights**: faits ou KPI du domaine `additive-risk-insights` affichés en complément, conformément à **IHI-C-FR-007**.

### Success Criteria (Feature C)

- **SC-C-001**: Sur l’ensemble des scénarios d’acceptation documentés pour cette feature, 100 % des résultats classés succès satisfont **IHI-C-FR-001** et **IHI-C-FR-002** lors d’audit manuel ou procédure de relecture équivalente.
- **SC-C-002**: Sur un jeu de contre-exemples documenté (invitant à inventer ingrédients ou pourcentages), 100 % des exécutions aboutissent à un rejet ou à un état d’échec explicite sans livrer de faits produit non ancrés.
- **SC-C-003**: 100 % des parcours qui affichent à la fois contenu général et contenu produit respectent **IHI-C-FR-004** (distinguable par l’utilisatrice sans ambiguïté majeure).
- **SC-C-004**: Sur les scénarios documentés mêlant passages « fait produit » ancrés et non ancrés, 0 % aboutissent à un statut **succès** (100 % → **IHI-C-FR-003** ou équivalent).
- **SC-C-005**: 100 % des blocs enrichis `additive-risk-insights` montrés à côté d’un succès LLM respectent **IHI-C-FR-007** (a)(b)(c) sur les scénarios documentés.

---

## Feature K — Pastille énergie (kcal / 100 g) sur l’écran de synthèse

> Origine : intake `/speckit-design` + `/speckit-specify` 2026-05-13  
> Intention : après analyse de composition terminée, afficher en tête de l’écran de synthèse une pastille du type « Analyse terminée : **N** kcal estimées / 100 g », valeur **estimée** à partir de la composition (liste d’ingrédients + produit identifié si présent), avec formulation prudente (indicatif, non valeur réglementaire).

### Clarifications (Feature K)

#### Session 2026-05-13

- **Indicateur vs allégation** : la pastille est un **résumé indicatif** ; elle MUST NOT remplacer une déclaration nutritionnelle réglementée sur l’étiquette.
- **Source du chiffre (v1 par défaut)** : estimation produite dans le **même flux** que le bilan composition (champ ou section dédiée dans la sortie analysable du modèle ou post-traitement contractuellement aligné sur cette sortie). Toute évolution (heuristique locale seule, seconde passe LLM) MUST rester documentée dans le plan d’implémentation et respecter les garde-fous ci-dessous.
- **Ref. UX** : placement visuel, accessibilité et cohérence avec les autres bandeaux du haut de l’écran de résultat → domaine **`user-guidance-experience`** (**UGE-A-FR-022**).
- **Bornes affichage kcal/100 g (clarify 2026-05-13)** : **Option B** — une estimation numérique **N** n’est affichée dans la pastille que si **1 ≤ N ≤ 1100** (kcal pour 100 g, entier issu de la sortie analysable après règle d’arrondi publiée) ; toute valeur strictement en dehors de cet intervalle (y compris négative ou nulle) est considérée comme non fiable pour l’affichage chiffré.

### User Scenarios (Feature K)

#### US-K1 — Voir l’estimation énergétique après succès (P1)

En tant qu’utilisatrice, lorsque mon analyse de composition est terminée avec succès, je veux voir en haut de l’écran de synthèse une pastille indiquant une estimation de kcal pour 100 g, afin d’avoir une indication rapide d’ordre de grandeur.

**Acceptance Scenarios**:

1. **Given** un bilan composition classé succès et une estimation kcal/100 g disponible selon les garde-fous, **When** l’écran de synthèse s’affiche, **Then** une pastille en tête montre un entier (ou valeur arrondie documentée) suivi de la mention « / 100 g » et d’une qualification du type « estimé » / « indicatif ».
2. **Given** le même cas, **When** la pastille est visible, **Then** le libellé inclut explicitement que l’analyse est terminée (ou équivalent clair) sans contredire l’état réel du flux.
3. **Given** une estimation disponible, **When** l’utilisatrice lit la pastille, **Then** aucune formulation ne présente la valeur comme analyse nutritionnelle officielle ou certifiée du produit.

#### US-K2 — Absence de chiffre plutôt qu’invention (P2)

En tant qu’utilisatrice, je préfère ne pas voir de nombre inventé si le modèle ou les garde-fous ne permettent pas une estimation fiable.

**Acceptance Scenarios**:

1. **Given** bilan succès mais estimation non disponible ou non fiable, **When** l’écran de synthèse s’affiche, **Then** la pastille indique l’état d’analyse terminée **sans** valeur numérique d’énergie trompeuse, ou un libellé d’« estimation indisponible » cohérent avec le design UX (**Ref.** UGE).
2. **Given** une sortie modèle incohérente (ex. **N** hors de l'intervalle 1–1100 kcal/100 g, ou non numérique), **When** le contrôle métier s’applique, **Then** aucune valeur aberrante n’est affichée comme estimation.

#### US-K3 — Cohérence avec l’ancrage (Feature C) (P1)

En tant qu’utilisatrice, je ne veux pas qu’une estimation soit présentée comme un « fait étiquette » si elle n’est pas compatible avec les règles d’ancrage du domaine.

**Acceptance Scenarios**:

1. **Given** les règles Feature C sur le segment validé, **When** l’estimation est affichée, **Then** elle est présentée comme **dérivée de la composition analysée** (indication), et non comme relevé de tableau nutritionnel étiquette.

#### Edge Cases (Feature K)

- Modèle qui omet toute information d’énergie malgré un bilan succès.
- Valeur négative, nulle, strictement supérieure à 1100, non entière après arrondi documenté, ou non numérique : pas d’affichage chiffré en pastille (**clarify** : plage d’affichage **1–1100** kcal/100 g).
- Streaming partiel : pas de pastille définitive incohérente avant fin de traitement (comportement aligné sur l’écran résultat existant).
- Accessibilité : contraste et taille de police de la pastille (détails **Ref.** UGE).

### Functional Requirements (Feature K)

- **IHI-K-FR-001**: Le système MUST, lorsque le bilan de composition est classé **succès** et qu’une estimation d’énergie en **kcal pour 100 g** est **disponible** selon les garde-fous documentés, exposer cette valeur à l’UI de synthèse pour affichage dans la pastille décrite en **IHI-K-FR-002**.
- **IHI-K-FR-002**: Le système MUST présenter la valeur dans une **pastille** (ou composant équivalent visuellement distinct) **en tête** de l’écran de synthèse / résultat composition, avec libellé du type « Analyse terminée : **N** kcal estimées / 100 g » (formulation française acceptable sous réserve d’équivalence claire : état terminé + nombre + unité + caractère estimé).
- **IHI-K-FR-003**: Le système MUST qualifier explicitement la valeur comme **estimée** ou **indicative** (libellé ou pictogramme + texte d’aide court) ; MUST NOT la présenter comme donnée réglementaire ou analyse nutritionnelle certifiée.
- **IHI-K-FR-004**: Le système MUST dériver l’estimation des **éléments de composition** connus du bilan (liste d’ingrédients ; produit identifié **si** présent dans le bilan) ; MUST NOT **inventer** un nombre lorsque la sortie analysable ne fournit pas d’estimation exploitable ou lorsque les garde-fous concluent à une fiabilité insuffisante.
- **IHI-K-FR-005**: Le système MUST respecter **Feature C** : l’estimation MUST être présentée comme **indication liée à l’analyse de composition**, et non comme extraction du tableau nutritionnel de l’étiquette.
- **IHI-K-FR-006**: Le système MUST documenter en conception d’implémentation la **source** de l’estimation (champ / section modèle, validations, règle d’arrondi vers entier). Les **bornes** d’acceptation pour **afficher** une valeur numérique **N** (kcal pour 100 g) dans la pastille sont **1 ≤ N ≤ 1100** (entier) ; toute valeur en dehors de cet intervalle MUST être traitée comme non fiable pour l’affichage chiffré (**IHI-K-FR-004** / **US-K2**).

### Key Entities (Feature K)

- **EstimatedEnergyPer100g**: valeur entière dans **1–1100** (kcal/100 g) si affichage chiffré autorisé, sinon état sans nombre exploitable + unité + flag « disponible / indisponible » + raison d’indisponibilité optionnelle.
- **EnergyEstimateSource**: trace minimale reliant l’estimation au contenu analysé (sortie modèle / règle de validation).

### Success Criteria (Feature K)

- **IHI-K-SC-001**: Sur un jeu de bilans de démo/fixtures où une estimation valide est fournie, 100 % des affichages montrent **N** cohérent avec la sortie attendue après validation.
- **IHI-K-SC-002**: Sur un jeu de contre-exemples (sortie sans estimation, **N** non compris dans **1–1100** kcal/100 g, ou non numérique), 0 % n’affichent de nombre trompeur ; la pastille reste cohérente avec **US-K2**.
- **IHI-K-SC-003**: 100 % des affichages incluent la qualification **estimé / indicatif** visible sans action utilisateur supplémentaire.

---

## Feature L — Personnalisation du prompt de critique santé

> Origine : intake `/speckit-design` + `/speckit-specify` 2026-06-28
> Intention : personnaliser le prompt utilisé pour l'analyse des ingrédients (critique santé) afin d'expliciter le **persona expert** (nutrition clinique + cancérologie préventive), les **dimensions de risque** évaluées par ingrédient, la **hiérarchie faits / incertitudes / hypothèses** (avec références CIRC/OMS), l'attention aux **populations vulnérables**, et le **format de sortie strict** par population.

### Clarifications (Feature L)

#### Session 2026-06-28

- **Portée** : la personnalisation porte sur le **prompt de critique santé** (flux LLM critique par population) ; elle ne modifie pas le bilan de composition ni les KPI additifs (`additive-risk-insights`), et reste soumise à l'ancrage Feature C (aucun fait étiquette non ancré).
- **Persona expert** : le prompt MUST positionner le modèle comme **expert de renommée mondiale en nutrition clinique et en cancérologie préventive, spécialisé dans l'évaluation des risques alimentaires** ; ce persona est une instruction de cadrage, pas une allégation produit.
- **Dimensions de risque** : le prompt MUST demander l'évaluation explicite, par ingrédient, des potentiels **cancérogène, mutagène, neurotoxique, métabolique (ex. pics glycémiques, cholestérol) et inflammatoire**.
- **Hiérarchie des preuves** : le prompt MUST imposer de distinguer **1) les faits établis** (ex. classification CIRC/OMS, consensus scientifique), **2) les incertitudes scientifiques** (ex. débats actuels, effets à doses massives chez l'animal), **3) les hypothèses ou mécanismes suspectés**.
- **Populations vulnérables** : le prompt MUST porter une attention particulière aux **femmes enceintes/allaitantes, enfants, personnes immunodéprimées et personnes ayant des antécédents familiaux de cancer** (au-delà des 4 sections de sortie, comme axe transversal de vigilance).
- **Format de sortie** : les 4 marqueurs de section (`###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`) et la structure par bloc (Points de vigilance / Analyse par ingrédient & Nuances / Niveau de prudence) sont **inchangés** ; seuls les libellés et le contenu de cadrage du prompt évoluent.
- **Conformité Feature C** : le prompt personnalisé MUST NOT encourager le modèle à produire des faits produit non ancrés ; il MUST réaffirmer l'interdiction d'inventer des ingrédients absents et de poser un diagnostic/prescription.
- Q: Mécanisme de personnalisation du prompt ? → A: **Remplacement en dur versionné** dans le builder de prompt (contenu intégré au code, testable, répétable) ; pas d'externalisation configurée ni de registre de prompts au périmètre Feature L.
- Q: Affichage des populations vulnérables sans section dédiée (immunodéprimées, antécédents familiaux cancer) ? → A: **Mention transversale intégrée** dans chaque section pertinente (Points de vigilance / Nuances), comme axe de vigilance, sans ajouter de section ni de préambule (préserve le format strict des 4 marqueurs).
- Q: La personnalisation s'applique-t-elle aussi au prompt du bilan de composition ? → A: **Non, uniquement au prompt de critique santé** (périmètre Feature L strict) ; le bilan de composition garde son propre contrat, une extension éventuelle ferait l'objet d'une feature distincte.
- Q: Seuil déclenchant la synthèse pour « liste très longue » ? → A: **Seuil en nombre d'ingrédients** (ex. ≥ 20), non en caractères ; la valeur exacte est laissée au plan d'implémentation.
- Q: Validation de la conformité au prompt (persona / hiérarchie des preuves / populations vulnérables) au MVP ? → A: **Relecture humaine + traçabilité** sur un jeu fixe d'ingrédients (aligné Feature C / `IHI-C-FR-006` MVP) ; le format de sortie reste vérifié par le parseur existant, la conformité sémantique n'est pas automatisée au MVP.

### User Scenarios (Feature L)

#### US-L1 — Cadre expert et dimensions de risque explicites (P1)

En tant qu'utilisatrice, je veux que la critique santé soit produite avec un cadrage d'expert en nutrition clinique et cancérologie préventive, évaluant explicitement plusieurs dimensions de risque par ingrédient, afin d'obtenir une analyse plus structurée et plus crédible.

**Why this priority**: cœur de la personnalisation demandée ; sans persona et sans dimensions de risque explicites, la sortie reste générique.

**Independent Test**: vérifiable en inspectant le prompt construit (présence du persona expert + des 5 dimensions de risque) et en contrôlant que la sortie LLM distingue faits / incertitudes / hypothèses sur un jeu fixe.

**Acceptance Scenarios**:

1. **Given** le prompt de critique santé construit, **When** on inspecte son contenu, **Then** il contient explicitement le cadrage « expert de renommée mondiale en nutrition clinique et en cancérologie préventive, spécialisé dans l'évaluation des risques alimentaires ».
2. **Given** le même prompt, **When** on inspecte ses dimensions d'évaluation, **Then** il exige l'évaluation par ingrédient des potentiels cancérogène, mutagène, neurotoxique, métabolique et inflammatoire.
3. **Given** une analyse produite depuis ce prompt sur un jeu d'ingrédients fixe, **When** on lit la sortie, **Then** chaque ingrédient analysé distingue visiblement faits établis, incertitudes scientifiques et hypothèses/mécanismes suspectés.

#### US-L2 — Populations vulnérables élargies et garde-fous éthiques (P1)

En tant qu'utilisatrice faisant partie d'une population vulnérable (ex. immunodéprimée, antécédents familiaux de cancer), je veux que le prompt porte une attention particulière à ma situation et conserve les garde-fous éthiques (pas de diagnostic, pas de prescription), afin de rester prudente et informée.

**Why this priority**: sécurise l'usage médical et l'inclusion des populations au-delà des 4 sections de sortie.

**Independent Test**: vérifiable en inspectant le prompt (mention des populations vulnérables élargies + garde-fous éthiques) et en contrôlant le refus de diagnostic sur une demande explicite simulée.

**Acceptance Scenarios**:

1. **Given** le prompt construit, **When** on inspecte son contenu, **Then** il mentionne explicitement l'attention particulière aux femmes enceintes/allaitantes, enfants, personnes immunodéprimées et personnes ayant des antécédents familiaux de cancer.
2. **Given** le prompt construit, **When** on inspecte les garde-fous éthiques, **Then** il interdit explicitement de poser un diagnostic, de prescrire un régime ou un traitement, et oriente vers un professionnel de santé en cas de demande d'avis médical personnalisé.
3. **Given** une demande simulée d'avis médical personnalisé transmise au flux, **When** l'analyse est produite, **Then** la sortie refuse poliment de poser un diagnostic/prescription et oriente vers un professionnel de santé, tout en conservant la structure des 4 sections.

#### US-L3 — Format de sortie strict préservé (P1)

En tant qu'utilisatrice, je veux que la personnalisation du prompt ne casse pas le format de sortie attendu (4 sections, blocs structurés), afin de garder une présentation cohérente avec l'existant.

**Why this priority**: garantit la non-régression du contrat de parsing et de l'UX de résultat.

**Independent Test**: vérifiable en exécutant le flux sur un jeu fixe et en contrôlant que les marqueurs `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES` et les 3 blocs attendus sont présents et ordonnés.

**Acceptance Scenarios**:

1. **Given** le prompt personnalisé, **When** on inspecte la section « format de sortie », **Then** il exige **uniquement** les marqueurs `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES` dans cet ordre, avec aucun texte avant `###ENFANTS`.
2. **Given** le même prompt, **When** on inspecte la structure exigée sous chaque marqueur, **Then** il requiert obligatoirement : 1) Points de vigilance (liste à puces courte), 2) Analyse par ingrédient & Nuances (faits établis vs incertitudes), 3) Niveau de prudence (Faible / Modéré / Élevé) avec justification prudente.
3. **Given** une analyse produite depuis ce prompt sur un jeu fixe, **When** le parseur de sections traite la sortie, **Then** les 4 sections sont reconnues dans l'ordre attendu, sans régression par rapport au comportement pré-Feature L.

### Edge Cases (Feature L)

- Liste d'ingrédients très longue : le prompt MUST demander une **synthèse des risques majeurs en tête de la section 2** (Analyse par ingrédient & Nuances), puis le détail des ingrédients pertinents, sans déroger au format des 4 sections.
- Liste dans une autre langue ou illisible : le prompt MUST conserver la structure des marqueurs et demander poliment des précisions / une meilleure capture dans la section 2 de chaque partie concernée.
- Terme ambigu (« arômes », « épices », additif non spécifié) : le prompt MUST demander de signaler l'opacité et son impact négatif sur la confiance de l'analyse.
- Demande d'avis médical personnalisé : le prompt MUST imposer un refus poli et une orientation vers un professionnel de santé, sans rompre la structure de sortie.
- Risque d'invention d'ingrédients (correction OCR) : le prompt MUST autoriser la **correction mentale** des erreurs OCR et l'usage de la dénomination scientifique/réglementaire la plus probable, tout en **interdisant d'inventer des ingrédients absents** (cohérent Feature C / `IHI-C-FR-001`).

### Functional Requirements (Feature L)

- **IHI-L-FR-001**: Le système MUST construire le prompt de critique santé en positionnant le modèle comme **expert de renommée mondiale en nutrition clinique et en cancérologie préventive, spécialisé dans l'évaluation des risques alimentaires**.
- **IHI-L-FR-002**: Le système MUST exiger dans le prompt l'analyse **ingrédient par ingrédient**, avec correction mentale des erreurs typiques d'OCR vers la dénomination scientifique ou réglementaire la plus probable, **sans jamais inventer d'ingrédients absents** (cohérent `IHI-C-FR-001` / `IHI-C-FR-002`).
- **IHI-L-FR-003**: Le système MUST exiger dans le prompt l'évaluation explicite, par ingrédient, des potentiels **cancérogène, mutagène, neurotoxique, métabolique (ex. pics glycémiques, cholestérol) et inflammatoire**.
- **IHI-L-FR-004**: Le système MUST exiger dans le prompt la distinction explicite entre : **1) faits établis** (ex. classification CIRC/OMS, consensus scientifique), **2) incertitudes scientifiques** (ex. débats actuels, effets à doses massives chez l'animal), **3) hypothèses ou mécanismes suspectés**.
- **IHI-L-FR-005**: Le système MUST exiger dans le prompt la **contextualisation de la dose et de l'exposition** (un ingrédient n'est toxique que si sa dose l'est) et l'interdiction des conclusions catégoriques (« toujours toxique », « poison »).
- **IHI-L-FR-006**: Le système MUST exiger dans le prompt le signalement de l'**opacité** pour les termes ambigus (« arômes », « épices », additifs non spécifiés) et de son impact négatif sur la confiance de l'analyse.
- **IHI-L-FR-007**: Le système MUST exiger dans le prompt les **garde-fous éthiques** : aucun diagnostic, aucune prescription de régime ou traitement ; refus poli et orientation vers un professionnel de santé en cas de demande d'avis médical personnalisé.
- **IHI-L-FR-008**: Le système MUST exiger dans le prompt une **attention particulière aux populations vulnérables** : femmes enceintes/allaitantes, enfants, personnes immunodéprimées et personnes ayant des antécédents familiaux de cancer. Le prompt MUST indiquer que les populations sans section dédiée (immunodéprimées, antécédents familiaux de cancer) sont traitées comme une **vigilance transversale intégrée** dans chaque section pertinente (Points de vigilance / Nuances), **sans** ajouter de section ni de préambule au format de sortie strict.
- **IHI-L-FR-009**: Le système MUST conserver le **format de sortie strict** : uniquement les marqueurs `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES` dans cet ordre, aucun texte avant `###ENFANTS`.
- **IHI-L-FR-010**: Le système MUST exiger, sous chaque marqueur, un bloc structuré contenant obligatoirement : **1) Points de vigilance** (liste à puces courte des ingrédients préoccupants pour cette population), **2) Analyse par ingrédient & Nuances** (faits établis vs incertitudes scientifiques, détaillée ingrédient par ingrédient), **3) Niveau de prudence** (Faible / Modéré / Élevé) avec justification prudente basée sur les doses probables et les risques à long terme.
- **IHI-L-FR-011**: Le système MUST exiger dans le prompt la **rédaction intégrale en français** (y compris synthèses et formulations de prudence) et la présence du **disclaimer** indiquant que l'information est indicative à visée éducative et ne remplace pas un avis médical ou nutritionnel personnalisé.
- **IHI-L-FR-012**: Le système MUST exiger, pour les listes très longues, une **synthèse des risques majeurs en tête de la section 2** de chaque partie, puis le détail des ingrédients pertinents. Le seuil « très longue » est défini en **nombre d'ingrédients** (valeur exacte fixée au plan d'implémentation, ex. ≥ 20), non en caractères.
- **IHI-L-FR-013**: Le système MUST exiger, pour les listes dans une autre langue ou illisibles, le maintien de la structure des marqueurs et une demande polie de précisions / meilleure capture dans la section 2 de chaque partie concernée.
- **IHI-L-FR-014**: Le système MUST respecter **Feature C** : le prompt personnalisé MUST NOT encourager la production de faits produit non ancrés sur le `ValidatedIngredientSegment` ; toute formulation liant « ce produit » à un ingrédient/risque reste soumise à `IHI-C-FR-004` / `IHI-C-FR-005`.
- **IHI-L-FR-015**: Le système MUST permettre la **répétabilité** de la construction du prompt (même entrée → même prompt construit) pour faciliter les tests unitaires et la non-régression du contrat de critique.
- **IHI-L-FR-016**: Le système MUST matérialiser le prompt personnalisé comme un **contenu intégré au code (remplacement en dur versionné)** dans le builder de prompt ; il MUST NOT introduire de configuration externe modifiable sans recompilation ni de registre de prompts sélectionnables au périmètre Feature L.
- **IHI-L-FR-017**: Le système MUST limiter la personnalisation Feature L au **prompt de critique santé** ; le prompt du bilan de composition MUST conserver son propre contrat (non modifié par Feature L). Toute extension du persona/des dimensions de risque au flux composition ferait l'objet d'une feature distincte.

### Key Entities (Feature L)

- **HealthCritiquePrompt**: prompt de critique santé construit (instruction système + message utilisateur), intégrant persona expert, dimensions de risque, hiérarchie des preuves, populations vulnérables et format de sortie strict.
- **RiskDimension**: dimension d'évaluation par ingrédient parmi {cancérogène, mutagène, neurotoxique, métabolique, inflammatoire}.
- **EvidenceTier**: niveau de preuve parmi {fait établi, incertitude scientifique, hypothèse / mécanisme suspecté}.
- **VulnerablePopulation**: population à vigilance accrue parmi {femmes enceintes/allaitantes, enfants, personnes immunodéprimées, antécédents familiaux de cancer}.
- **CritiqueSectionMarker**: marqueur de section de sortie (`###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`) et structure de bloc associée.

### Success Criteria (Feature L)

- **IHI-L-SC-001**: 100 % des prompts construits contiennent le persona expert (nutrition clinique + cancérologie préventive) et les 5 dimensions de risque (cancérogène, mutagène, neurotoxique, métabolique, inflammatoire).
- **IHI-L-SC-002**: 100 % des prompts construits exigent la distinction faits établis / incertitudes scientifiques / hypothèses (avec références CIRC/OMS pour les faits établis).
- **IHI-L-SC-003**: 100 % des prompts construits mentionnent les populations vulnérables élargies (femmes enceintes/allaitantes, enfants, immunodéprimées, antécédents familiaux de cancer) et les garde-fous éthiques (pas de diagnostic, pas de prescription, orientation professionnel de santé).
- **IHI-L-SC-004**: 100 % des prompts construits préservent le format de sortie strict (4 marqueurs ordonnés, aucun texte avant `###ENFANTS`, 3 blocs obligatoires par section).
- **IHI-L-SC-005**: 100 % des sorties LLM produites depuis le prompt personnalisé sur un jeu fixe sont parsables par le parseur de sections existant sans régression (4 sections reconnues dans l'ordre).
- **IHI-L-SC-006**: 100 % des prompts construits préservent la conformité Feature C (aucune incitation à inventer des ingrédients absents, ancrage préservé).
- **IHI-L-SC-007**: 100 % des exécutions de construction du prompt sont répétables (même entrée → même prompt) sur ≥ 3 exécutions successives.
- **IHI-L-SC-008**: La conformité sémantique de la sortie au prompt personnalisé (persona expert, hiérarchie faits/incertitudes/hypothèses, populations vulnérables, garde-fous éthiques) est tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe d'ingrédients (aligné `IHI-C-FR-006` MVP) ; le format de sortie (4 marqueurs, 3 blocs) reste vérifié par le parseur existant (`IHI-L-SC-005`). Aucun audit automatisé bloquant n'est exigé au MVP.

---

## Feature M — Accès UI à la critique santé (câblage de navigation)

> Origine : intake `/speckit-design` 2026-06-28
> Intention : rendre la critique santé par population accessible depuis l'application de production. Aujourd'hui `HealthCritiqueScreen` (écran d'entrée avec bouton « Analyser ») n'est monté que dans les tests instrumentés ; il n'est pas enregistré dans le `NavHost` de `MainActivity`. Seul `HealthCritiqueResultScreen` est routé, et il n'est atteignable que via `navigateToResult` émis par `analyze()`, lui-même appelé uniquement depuis `HealthCritiqueScreen`. Le flux critique santé est donc **inaccessible** en production (l'utilisatrice ne voit que le résultat composition/additifs). Cette feature comble le manque prévu par `specs/002-ingredient-health-critique/plan.md` (« Onglet « Critique santé » dans `MainActivity` »).

### Clarifications (Feature M)

#### Session 2026-06-28

- **Portée** : câblage de navigation uniquement (route + point d'entrée UI) ; **aucune modification** du moteur `HealthCritiqueEngine`, du prompt (`HealthCritiquePromptBuilder`), du parseur (`HealthCritiqueSectionParser`) ou du flux composition.
- **Écran d'entrée** : `HealthCritiqueScreen` existant (champ lecture seule + bouton « Analyser ») est réutilisé tel quel ; il est monté dans le `NavHost` via une nouvelle route.
- **Point d'entrée** : un bouton « Critique santé » est exposé depuis l'écran de résultat composition (`LlmResultScreen`) une fois le bilan prêt, afin de respecter la dépendance amont (segment validé synchronisé depuis le scan). Aucun onglet persistant requis au MVP.
- **Synchronisation du segment** : réutilise le flux existant `cameraViewModel.lastValidatedSegmentForHealth` → `healthCritiqueViewModel.setValidatedSegmentFromScan(...)` (déjà câblé dans `MainActivity`) ; l'écran d'entrée affiche donc la liste prête, en lecture seule.
- **Recherche d'effets de bord** : `viewModel.analyze()` n'est appelé que depuis `HealthCritiqueScreen` ; l'ajouter à la navigation ne modifie pas les autres routes. La route `HealthCritiqueResult` existe déjà et reste inchangée.
- **Garde-fou** : le bouton d'entrée est désactivé si aucun segment validé n'est disponible (cohérent avec `InputInvalidReason.NO_VALIDATED_SEGMENT`).

### User Scenarios (Feature M)

#### US-M1 — Atteindre la critique santé depuis le résultat composition (P1)

En tant qu'utilisatrice, après avoir obtenu le bilan de composition d'un produit scanné, je veux un point d'entrée explicite vers la critique santé par population, afin de consulter l'analyse `###ENFANTS` / `###FEMMES_ENCEINTES` / `###ADULTES` / `###PERSONNES_AGEES` qui sinon reste invisible.

**Why this priority**: sans ce point d'entrée, la critique santé (Feature L et spec 002) est totalement inaccessible en production — la feature est inutilisable.

**Independent Test**: naviguer jusqu'au résultat composition, vérifier la présence d'un bouton « Critique santé », le déclencher, et confirmer l'affichage de `HealthCritiqueScreen` (liste en lecture seule + bouton « Analyser »).

**Acceptance Scenarios**:

1. **Given** le résultat composition affiché (`LlmResultScreen`) avec un segment validé disponible, **When** l'utilisatrice consulte l'écran, **Then** un point d'entrée « Critique santé » est visible.
2. **Given** ce point d'entrée, **When** l'utilisatrice le déclenche, **Then** la navigation atteint `HealthCritiqueScreen` (route enregistrée dans le `NavHost`) et la liste d'ingrédients s'affiche en lecture seule, synchronisée avec le segment validé du scan.
3. **Given** aucun segment validé disponible, **When** l'utilisatrice est sur le résultat composition, **Then** le point d'entrée « Critique santé » est désactivé (ou masqué) et ne déclenche pas la navigation.

#### US-M2 — Lancer la critique et voir les sections par population (P1)

En tant qu'utilisatrice, depuis `HealthCritiqueScreen`, je veux pouvoir lancer l'analyse (« Analyser ») et atterrir sur l'écran de résultat affichant les sections par population, afin de valider bout-en-bout le flux critique santé.

**Why this priority**: confirme que le câblage complète la chaîne : entrée → `analyze()` → `navigateToResult` → `HealthCritiqueResultScreen` (sections parsées).

**Independent Test**: depuis `HealthCritiqueScreen`, appuyer sur « Analyser » et confirmer la navigation vers `HealthCritiqueResultScreen` puis l'affichage des titres « ENFANTS / FEMMES ENCEINTES / ADULTES / PERSONNES AGEES » (les marqueurs `###` ne s'affichent pas littéralement, par conception du parseur — `IHI-L-SC-005`).

**Acceptance Scenarios**:

1. **Given** `HealthCritiqueScreen` affiché avec un segment validé non vide, **When** l'utilisatrice appuie sur « Analyser », **Then** `viewModel.analyze()` est déclenché et la navigation atteint `HealthCritiqueResult` (`navigateToResult`).
2. **Given** l'analyse aboutit à un `HealthCritiqueResult.CritiqueReady`, **When** l'écran de résultat s'affiche, **Then** les sections par population sont rendues (titres + corps), conformément à `HealthCritiqueResultScreen`.
3. **Given** l'analyse aboutit à un `HealthCritiqueResult.InferenceError` ou `InputInvalid`, **When** l'écran de résultat s'affiche, **Then** le message d'erreur est affiché (comportement existant inchangé).

### Edge Cases (Feature M)

- Retour navigation : depuis `HealthCritiqueScreen` ou `HealthCritiqueResultScreen`, le retour (`onBack` / `popBackStack`) ramène au résultat composition sans état cassé.
- Segment validé devenu vide entre le scan et l'ouverture de la critique : le bouton « Analyser » reste géré par l'existant (`HealthIngredientInputValidator` → `InputInvalid`).
- Rotation / recréation d'activité : la route et le `ViewModel` survivent (state already handled par `HealthCritiqueViewModel` + `LastHealthAnalysisStore`).
- Double déclenchement du point d'entrée : navigation standard Compose (pas de double empilement de routes).

### Functional Requirements (Feature M)

- **IHI-M-FR-001**: Le système MUST enregistrer une route de navigation pour `HealthCritiqueScreen` dans le `NavHost` de `MainActivity` (ex. `CameraFlowRoutes.HealthCritiqueEntry`).
- **IHI-M-FR-002**: Le système MUST exposer un point d'entrée « Critique santé » depuis `LlmResultScreen` (résultat composition) permettant de naviguer vers la route `HealthCritiqueEntry`.
- **IHI-M-FR-003**: Le système MUST désactiver (ou masquer) le point d'entrée « Critique santé » lorsqu'aucun segment validé n'est disponible (`InputInvalidReason.NO_VALIDATED_SEGMENT`).
- **IHI-M-FR-004**: Le système MUST réutiliser `HealthCritiqueScreen` existant sans modification de son comportement (champ lecture seule + bouton « Analyser »).
- **IHI-M-FR-005**: Le système MUST réutiliser le flux de synchronisation existant `lastValidatedSegmentForHealth` → `setValidatedSegmentFromScan(...)` pour alimenter l'écran d'entrée ; aucune nouvelle source de segment.
- **IHI-M-FR-006**: Le système MUST conserver la route `HealthCritiqueResult` et le flux `analyze()` → `navigateToResult` inchangés (non-régression).
- **IHI-M-FR-007**: Le système MUST NOT modifier `HealthCritiqueEngine`, `HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, ni le flux composition (périmètre navigation stricte).
- **IHI-M-FR-008**: Le système MUST assurer le retour navigation (`onBack` / `popBackStack`) depuis `HealthCritiqueScreen` vers l'écran précédent sans casser la pile de navigation.

### Key Entities (Feature M)

- **HealthCritiqueEntryRoute**: route de navigation vers `HealthCritiqueScreen` (nouvelle constante dans `CameraFlowRoutes`).
- **CritiqueSanteEntryTrigger**: point d'entrée UI (bouton) exposé depuis `LlmResultScreen`, activé conditionnellement à la disponibilité d'un segment validé.

### Success Criteria (Feature M)

- **IHI-M-SC-001**: 100 % des parcours « résultat composition → bouton Critique santé » avec un segment validé disponible aboutissent à l'affichage de `HealthCritiqueScreen`.
- **IHI-M-SC-002**: 100 % des déclenchements du bouton « Analyser » depuis `HealthCritiqueScreen` naviguent vers `HealthCritiqueResultScreen` (route `HealthCritiqueResult`).
- **IHI-M-SC-003**: 100 % des cas sans segment validé désactivent (ou masquent) le point d'entrée « Critique santé ».
- **IHI-M-SC-004**: 0 % de régression sur le flux composition (`LlmResultScreen`), le moteur critique, le prompt et le parseur (inchangés).
- **IHI-M-SC-005**: 100 % des retours navigation depuis `HealthCritiqueScreen` / `HealthCritiqueResultScreen` ramènent à l'écran précédent sans état cassé.

---

## Feature N — Critique santé ciblée par profil utilisateur (prompt adapté + restitution prudence/cartes)

> Origine : intake `/speckit-design` + `/speckit-specify` 2026-06-28
> Intention : ne plus produire les 4 profils (ENFANTS / FEMMES_ENCEINTES / ADULTES / PERSONNES_AGEES) indifféremment. L'utilisatrice renseigne **son profil** lors de l'Onboarding (`Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`) ; le prompt de critique santé est ensuite **adapté pour ne produire que l'analyse la concernant**, en rappelant explicitement le profil ciblé (« Évalué pour vous : Femme enceinte »). La restitution évolue : un **Niveau de prudence** (jauge Faible / Modéré / Élevé + texte court) juste sous les alertes, puis un **détail ingrédient par ingrédient** sous forme de cartes à accordéon limitées aux ingrédients ayant déclenché une vigilance (Modéré / Élevé), avec un bouton « Voir tous les ingrédients analysés » en bas.

### Clarifications (Feature N)

#### Session 2026-06-28

- **Portée** : Feature N modifie le **prompt de critique santé**, le **format de sortie** et la **restitution UI critique** ; elle ne modifie pas le bilan de composition ni les KPI additifs (`additive-risk-insights`), et reste soumise à l'ancrage Feature C (aucun fait étiquette non ancré).
- **Évolution du format strict Feature L** : Feature L (**IHI-L-FR-009** / **IHI-L-SC-004**) imposait 4 marqueurs ordonnés `###ENFANTS` / `###FEMMES_ENCEINTES` / `###ADULTES` / `###PERSONNES_AGEES`. Feature N **supprime entièrement** ce flux 4-profils (prompt 4-marqueurs, parseur 4-sections, UI 4-sections) et le remplace par une **sortie à profil unique** : un seul marqueur correspondant au profil sélectionné, précédé d'un rappel « Évalué pour vous : <profil> ». **IHI-L-FR-009** et **IHI-L-SC-004** sont **supersédés et retirés** (traçabilité conservée dans cette section) ; les autres exigences Feature L (persona expert, 5 dimensions de risque, hiérarchie faits/incertitudes/hypothèses, populations vulnérables transversales, garde-fous éthiques, rédaction française, disclaimer, seuil « liste très longue ») **restent applicables** au prompt personnalisé Feature N. Aucun mode « comparer tous les profils » n'est conservé.
- **Nouveau profil « Sportif »** : le jeu de profils passe de 4 à 5 avec l'ajout de **Sportif** (profil non couvert par les 4 marqueurs historiques). Un marqueur canonique est défini pour chaque profil (voir **IHI-N-FR-006**).
- **Sélection du profil (Onboarding)** : la saisie du profil est effectuée dans l'Onboarding et sa persistance/édition sont du ressort du domaine **`user-guidance-experience`** (**Ref.** UGE) ; le profil est également modifiable dans un écran **« Paramètres / Profil »** (UGE) hors Onboarding. Le domaine `ingredient-health-intelligence` **consomme** le profil sélectionné comme entrée du prompt et de la restitution, sans le stocker lui-même ; la critique suivante utilise le nouveau profil après changement.
- **Restitution du Niveau de prudence** : la jauge affiche **Faible / Modéré / Élevé** (3 paliers) accompagnée d'un **texte court justificatif** produit par le LLM (ex. « Niveau modéré : présence de phosphate qui peut perturber l'absorption du fer »). Ce niveau est **celui du profil sélectionné**, pas une moyenne des 4 populations. Les « alertes » placées au-dessus du Niveau de prudence désignent les **KPI additifs/risques existants** publiés par le domaine `additive-risk-insights` (juxtaposition régie par **IHI-C-FR-007**) ; Feature N n'introduit **pas** de nouveau bloc d'alertes.
- **Filtrage des cartes ingrédients** : la restitution ne liste en clair, par défaut, que les ingrédients ayant déclenché une vigilance **Modérée** ou **Élevée** pour le profil sélectionné. Les ingrédients sans vigilance (« RAS ») ne sont pas affichés en clair ; ils restent accessibles via un bouton **« Voir tous les ingrédients analysés »** qui déplie une **liste compacte** (nom + statut de vigilance RAS / Modéré / Élevé) de tous les ingrédients analysés — pas des cartes complètes pour les RAS.
- **Contenu d'une carte ingrédient** : chaque carte problématique contient a minima : **titre** (marqueur visuel de sévérité + code éventuel tel qu'E-number + nom), **sous-titre type** (ex. « Conservateur — Additif »), **Impact** (formulation courte), **Fait établi** (avec réf. CIRC/OMS le cas échéant), **Nuance** (dépend de la dose / fréquence / cuisson, etc.), **Cible particulièrement** (autres populations concernées, même si non sélectionnées).
- **Conformité Feature C** : le prompt personnalisé Feature N MUST NOT encourager la production de faits produit non ancrés ; chaque ingrédient mentionné dans une carte MUST être **littéralement ancrable** dans le `ValidatedIngredientSegment` (selon **IHI-C-FR-005**), hors corrections OCR autorisées par **IHI-L-FR-002**.
- **Validation MVP** : conformité sémantique (ciblage profil unique, rappel « Évalué pour vous », structure des cartes, niveau de prudence) tenue par **relecture humaine + traçabilité** sur un jeu fixe (aligné **IHI-C-FR-006** MVP) ; le format de sortie (marqueur unique + blocs attendus) reste vérifié par le parseur existant étendu Feature N.
- Q: Comportement en l'absence de profil sélectionné (Onboarding non terminé / profil effacé) ? → A: **Fallback implicite sur le profil par défaut « Adulte »** (profil unique, pas 4-profils) ; la critique est produite pour « Adulte » avec le rappel « Évalué pour vous : Adulte », et l'UI signale visuellement qu'il s'agit du profil par défaut (invitation à personnaliser dans l'Onboarding/paramètres). Aucune critique 4-profils n'est produite (pas de rétropédalage Feature L silencieux).
- Q: Contenu déployé par le bouton « Voir tous les ingrédients analysés » ? → A: **Liste compacte** (nom + statut de vigilance RAS / Modéré / Élevé) de tous les ingrédients analysés ; pas de cartes complètes pour les ingrédients RAS.
- Q: Origine des « alertes » affichées au-dessus du Niveau de prudence ? → A: Les « alertes » désignent les **KPI additifs/risques existants** du domaine `additive-risk-insights` (juxtaposition régie par **IHI-C-FR-007**) ; Feature N n'introduit **pas** de nouveau bloc d'alertes, le Niveau de prudence se place juste en dessous de ces KPI.
- Q: Sortie 4-profils : conservée ou supprimée ? → A: **Supprimée entièrement** (prompt 4-marqueurs, parseur 4-sections, UI 4-sections) ; seul le mode profil unique reste. Aucun mode « comparer tous les profils » n'est conservé. **IHI-L-FR-009** / **IHI-L-SC-004** supersédés et retirés (traçabilité en spec).
- Q: Profil modifiable hors Onboarding ? → A: **Oui, dans un écran « Paramètres / Profil »** (UGE) en plus de l'Onboarding ; la critique suivante utilise le nouveau profil après changement.

### User Scenarios (Feature N)

#### US-N1 — Profil renseigné lors de l'Onboarding (P1)

En tant que nouvelle utilisatrice, lors de l'Onboarding je veux renseigner **mon profil** parmi `Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`, afin que les analyses ultérieures ne concernent que moi.

**Why this priority**: sans profil sélectionné, le ciblage du prompt et le rappel « Évalué pour vous » sont impossibles — la feature entière est bloquée.

**Independent Test**: démarrer l'app la première fois, parcourir l'Onboarding, choisir un profil, et vérifier qu'il est persisté et repris comme entrée de la critique santé.

**Acceptance Scenarios**:

1. **Given** une première ouverture de l'app, **When** l'utilisatrice atteint l'étape Onboarding, **Then** elle peut sélectionner **un et un seul** profil parmi `Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`.
2. **Given** un profil sélectionné, **When** l'utilisatrice termine l'Onboarding, **Then** le profil est persisté (stockage UGE) et reste disponible pour les analyses suivantes sans re-saisie.
3. **Given** l'app rouverte après fermeture, **When** l'utilisatrice déclenche une critique santé, **Then** le profil précédemment choisi est réutilisé (persistance).

#### US-N2 — Prompt adapté au profil et rappel « Évalué pour vous » (P1)

En tant qu'utilisatrice dont le profil est « Femme enceinte », je veux que l'analyse ne produise **que** la critique me concernant, et qu'elle rappelle explicitement « Évalué pour vous : Femme enceinte », afin de ne pas lire 3 sections qui ne me concernent pas.

**Why this priority**: cœur de l'intention produit décrite ; supprime le bruit des 3 profils non pertinents et personnalise la sortie.

**Independent Test**: avec un profil donné, inspecter le prompt construit (un seul marqueur de profil, rappel du profil) et la sortie LLM (rappel « Évalué pour vous : <profil> » + une seule section).

**Acceptance Scenarios**:

1. **Given** un profil sélectionné (ex. « Femme enceinte »), **When** le prompt de critique santé est construit, **Then** il exige **uniquement** le marqueur canonique du profil sélectionné (ex. `###FEMME_ENCEINTE`) et **aucun** des autres marqueurs de profil.
2. **Given** le même prompt, **When** on inspecte le format de sortie exigé, **Then** il exige un **rappel explicite** du profil ciblé en tête, de la forme « Évalué pour vous : <profil> » (libellé français équivalent accepté).
3. **Given** une analyse produite depuis ce prompt sur un jeu fixe, **When** on lit la sortie, **Then** elle contient **une seule** section de critique (celle du profil sélectionné), précédée du rappel « Évalué pour vous : <profil> ».

#### US-N3 — Niveau de prudence visible en haut de la critique (P1)

En tant qu'utilisatrice, juste sous les alertes je veux voir un **Niveau de prudence** (jauge Faible / Modéré / Élevé) accompagné d'un texte court, afin de saisir en 3–10 secondes l'ordre de vigilance pour **mon** profil.

**Why this priority**: réduit la charge cognitive d'entrée de lecture ; c'est le signal prudence principal du profil sélectionné.

**Independent Test**: produire une critique pour un profil donné et vérifier la présence de la jauge (3 paliers) + d'un texte court justificatif, sous les alertes et au-dessus du détail ingrédient.

**Acceptance Scenarios**:

1. **Given** une critique réussie pour le profil sélectionné, **When** l'écran de résultat s'affiche, **Then** un **Niveau de prudence** est visible juste sous les **alertes existantes** (KPI additifs/risques `additive-risk-insights`), avant le détail ingrédient.
2. **Given** ce niveau, **When** l'utilisatrice le consulte, **Then** il est restitué sous forme d'une **jauge à 3 paliers** (Faible / Modéré / Élevé) avec le palier actif mis en évidence, accompagné d'un **texte court justificatif** produit pour le profil sélectionné.
3. **Given** plusieurs profils auraient eu des niveaux différents, **When** le niveau est affiché, **Then** il reflète **uniquement** le niveau du profil sélectionné (pas une moyenne des profils).

#### US-N4 — Détail ingrédient par ingrédient sous forme de cartes (P1)

En tant qu'utilisatrice curieuse (ou confrontée à un produit à vigilance), je veux scroller dans un **détail ingrédient par ingrédient** sous forme de cartes à accordéon, limité aux ingrédients problématiques, afin de comprendre en 10 s et + ce qui me concerne.

**Why this priority**: supporte la profondeur d'analyse là où le niveau de prudence est synthétique.

**Independent Test**: produire une critique avec au moins un ingrédient à vigilance Modéré/Élevé et vérifier qu'une carte s'affiche pour celui-ci (titre, type, Impact, Fait établi, Nuance, Cible particulièrement) ; qu'aucune carte « RAS » n'est visible par défaut ; et qu'un bouton « Voir tous les ingrédients analysés » est présent en bas.

**Acceptance Scenarios**:

1. **Given** une critique réussie pour le profil sélectionné, **When** l'utilisatrice scrolle sous le Niveau de prudence, **Then** seuls les ingrédients ayant déclenché une vigilance **Modérée** ou **Élevée** sont affichés en clair, sous forme de cartes repliables (accordéon).
2. **Given** une carte d'un ingrédient problématique, **When** l'utilisatrice l'ouvre, **Then** elle contient a minima : titre (marqueur de sévérité + code éventuel + nom), sous-titre type, **Impact**, **Fait établi**, **Nuance**, **Cible particulièrement**.
3. **Given** des ingrédients sans vigilance pour le profil sélectionné, **When** la restitution par défaut s'affiche, **Then** ils ne sont **pas** listés en clair (pas de « Farine de blé : RAS »).
4. **Given** l'utilisatrice souhaite la liste complète, **When** elle atteint le bas de la restitution, **Then** un bouton **« Voir tous les ingrédients analysés »** est disponible et déplie une **liste compacte** (nom + statut de vigilance RAS / Modéré / Élevé) de tous les ingrédients analysés, y compris ceux sans vigilance.

### Edge Cases (Feature N)

- **Profil non encore sélectionné** (Onboarding skip / non terminé / profil effacé) : le système MUST se rabattre sur le **profil par défaut « Adulte »** (profil unique) ; la critique est produite pour « Adulte » avec le rappel « Évalué pour vous : Adulte », et l'UI signale visuellement qu'il s'agit du profil par défaut (invitation à personnaliser). Le système MUST NOT produire une critique 4-profils par défaut (rétropédalage Feature L silencieux interdit).
- **Changement de profil en cours de session** : l'utilisatrice peut modifier son profil dans l'écran « Paramètres / Profil » (UGE) hors Onboarding ; la critique suivante MUST cibler le **nouveau** profil et rappeler le nouveau libellé ; la critique déjà affichée n'est pas recalculée automatiquement sauf action explicite.
- **Profil « Sportif »** : n'a pas de marqueur historique ; un marqueur canonique `###SPORTIF` MUST être défini et le prompt doit traiter le profil sportif comme un profil à part entière (vigilances spécifiques : apports énergétiques, sucres rapides, sel, compléments, etc.) sans diagnostic ni prescription.
- **Liste très longue** : la synthèse des risques majeurs en tête de section 2 (**IHI-L-FR-012**) reste applicable ; elle est produite **pour le profil sélectionné uniquement**.
- **Aucun ingrédient à vigilance (Modéré/Élevé)** : la restitution indique clairement l'absence d'ingrédient problématique pour le profil sélectionné (niveau Faible affiché) ; le bouton « Voir tous les ingrédients analysés » reste disponible.
- **Terme ambigu / opacité** : un ingrédient opaque peut faire l'objet d'une carte si son opacité déclenche une vigilance ; la carte indique l'opacité comme Nuance (cohérent **IHI-L-FR-006**).
- **Demande d'avis médical** : garde-fou éthique **IHI-L-FR-007** inchangé ; le refus poli et l'orientation vers un professionnel de santé restent exigés, sans rompre le format de sortie ciblé.
- **Ancrage Feature C** : un ingrédient mentionné dans une carte MUST être ancré dans le `ValidatedIngredientSegment` (selon **IHI-C-FR-005**) ; un échec d'ancrage suit **IHI-C-FR-003** (pas de succès avec carte non ancrée).

### Functional Requirements (Feature N)

- **IHI-N-FR-001**: Le système MUST consommer le **profil utilisateur sélectionné** (parmi `Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`) comme entrée du prompt de critique santé et de la restitution. La saisie/persistance du profil est du ressort du domaine `user-guidance-experience` ; IHI ne fait que **consommer** cette entrée.
- **IHI-N-FR-002**: Le système MUST adapter le prompt de critique santé pour exiger **uniquement** la production de la section correspondant au profil sélectionné ; le prompt MUST NOT demander les sections des autres profils.
- **IHI-N-FR-003**: Le système MUST exiger dans le prompt un **rappel explicite** du profil ciblé en tête de sortie, de la forme « Évalué pour vous : <profil> » (libellé français équivalent accepté sous réserve d'équivalence claire).
- **IHI-N-FR-004**: Le système MUST préserver les exigences Feature L non format-strict : persona expert (**IHI-L-FR-001**), dimensions de risque (**IHI-L-FR-003**), hiérarchie faits/incertitudes/hypothèses (**IHI-L-FR-004**), contextualisation de la dose (**IHI-L-FR-005**), signalement d'opacité (**IHI-L-FR-006**), garde-fous éthiques (**IHI-L-FR-007**), populations vulnérables transversales (**IHI-L-FR-008**), rédaction française + disclaimer (**IHI-L-FR-011**), seuil « liste très longue » (**IHI-L-FR-012**).
- **IHI-N-FR-005**: Le système MUST préserver la conformité Feature C (**IHI-C-FR-001** à **IHI-C-FR-007**) : aucun fait produit non ancré ; chaque ingrédient mentionné dans une carte MUST être ancré dans le `ValidatedIngredientSegment`.
- **IHI-N-FR-006**: Le système MUST définir un **marqueur canonique unique** par profil : `###FEMME_ENCEINTE`, `###ENFANT`, `###PERSONNE_AGEE`, `###ADULTE`, `###SPORTIF`. Le prompt exige **uniquement** le marqueur du profil sélectionné, et **aucun texte de critique** avant le rappel « Évalué pour vous : <profil> ».
- **IHI-N-FR-007**: Le système MUST exiger dans le prompt un bloc **Niveau de prudence** (palier parmi **Faible / Modéré / Élevé**) **pour le profil sélectionné**, accompagné d'un **texte court justificatif** prudent basé sur les doses probables et les risques à long terme.
- **IHI-N-FR-008**: Le système MUST exiger dans le prompt un **détail ingrédient par ingrédient** structuré en cartes, où chaque carte problématique contient a minima : **titre** (sévérité + code éventuel + nom), **sous-titre type** (ex. « Conservateur — Additif »), **Impact**, **Fait établi** (avec réf. CIRC/OMS le cas échéant), **Nuance** (dose/fréquence/cuisson/etc.), **Cible particulièrement** (autres populations concernées).
- **IHI-N-FR-009**: Le système MUST restituer le **Niveau de prudence** juste sous les **alertes existantes** (KPI additifs/risques publiés par `additive-risk-insights`, juxtaposition régie par **IHI-C-FR-007**), avant le détail ingrédient, sous forme d'une **jauge à 3 paliers** (Faible / Modéré / Élevé) avec le palier actif mis en évidence et le **texte court justificatif** du LLM. Feature N n'introduit pas de nouveau bloc d'alertes.
- **IHI-N-FR-010**: Le système MUST restituer par défaut **uniquement** les cartes des ingrédients ayant déclenché une vigilance **Modérée** ou **Élevée** pour le profil sélectionné ; les ingrédients sans vigilance ne sont **pas** affichés en clair dans la restitution par défaut.
- **IHI-N-FR-011**: Le système MUST exposer un bouton **« Voir tous les ingrédients analysés »** en bas de la restitution, permettant de déplier une **liste compacte** (nom + statut de vigilance RAS / Modéré / Élevé) de tous les ingrédients analysés (y compris ceux sans vigilance pour le profil) ; les ingrédients RAS ne font pas l'objet de cartes complètes.
- **IHI-N-FR-012**: Le système MUST, en l'absence de profil sélectionné (Onboarding non terminé / profil effacé), se rabattre sur le **profil par défaut « Adulte »** (profil unique) et produire la critique pour « Adulte » avec le rappel « Évalué pour vous : Adulte » ; l'UI MUST signaler visuellement qu'il s'agit du profil par défaut (invitation à personnaliser). Le système MUST NOT produire silencieusement une critique 4-profils (pas de rétropédalage Feature L implicite).
- **IHI-N-FR-013**: Le système MUST étendre le parseur de sections pour reconnaître le **marqueur unique du profil sélectionné** et les blocs associés (rappel de profil, Niveau de prudence, cartes ingrédients) ; le support du format 4-marqueurs strict (**IHI-L-FR-009**) est **retiré** (supersédé par Feature N — traçabilité conservée en spec). Le parseur MUST NOT accepter de sortie 4-profils ; il MUST rejeter comme `non-analysable-response` toute sortie 4-marqueurs non conforme au format profil unique.
- **IHI-N-FR-014**: Le système MUST permettre la **répétabilité** du prompt construit (même segment validé + même profil → même prompt) pour les tests unitaires et la non-régression du contrat de critique.
- **IHI-N-FR-015**: Le système MUST matérialiser l'adaptation du prompt comme un **contenu intégré au code** (cohérent **IHI-L-FR-016**) ; il MUST NOT introduire de configuration externe modifiable sans recompilation au périmètre Feature N.
- **IHI-N-FR-016**: Le système MUST limiter Feature N au **prompt de critique santé** et à sa **restitution** ; le prompt du bilan de composition MUST conserver son propre contrat (cohérent **IHI-L-FR-017**).

### Key Entities (Feature N)

- **UserProfile**: profil sélectionné par l'utilisatrice, parmi {`Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`} ; consommé par IHI, saisi et persisté par `user-guidance-experience`.
- **ProfileCritiqueMarker**: marqueur canonique de section pour le profil sélectionné (`###FEMME_ENCEINTE`, `###ENFANT`, `###PERSONNE_AGEE`, `###ADULTE`, `###SPORTIF`).
- **EvaluatedForHeader**: rappel explicite « Évalué pour vous : <profil> » en tête de sortie.
- **PrudenceLevel**: niveau de prudence du profil sélectionné parmi {Faible, Modéré, Élevé} + texte court justificatif.
- **IngredientRiskCard**: carte d'un ingrédient à vigilance (titre, type, Impact, Fait établi, Nuance, Cible particulièrement) ; ancrage Feature C requis.
- **FullIngredientListToggle**: action « Voir tous les ingrédients analysés » dépliable en bas de restitution, exposant une liste compacte (nom + statut de vigilance) de tous les ingrédients analysés.

### Success Criteria (Feature N)

- **IHI-N-SC-001**: 100 % des prompts construits avec un profil sélectionné exigent **uniquement** le marqueur canonique du profil sélectionné et **aucun** des autres marqueurs.
- **IHI-N-SC-002**: 100 % des prompts construits exigent le rappel « Évalué pour vous : <profil> » en tête de sortie.
- **IHI-N-SC-003**: 100 % des sorties LLM produites depuis le prompt personnalisé sur un jeu fixe contiennent **une seule** section de critique (profil sélectionné) précédée du rappel « Évalué pour vous : <profil> ».
- **IHI-N-SC-004**: 100 % des restitutions affichent un **Niveau de prudence** (jauge Faible/Modéré/Élevé + texte court) juste sous les alertes, reflétant uniquement le profil sélectionné.
- **IHI-N-SC-005**: 100 % des restitutions par défaut n'affichent en clair que les cartes des ingrédients à vigilance **Modérée/Élevée** ; 0 % affichent des cartes « RAS » en clair par défaut.
- **IHI-N-SC-006**: 100 % des restitutions exposent le bouton **« Voir tous les ingrédients analysés »** en bas, déployant une **liste compacte** (nom + statut de vigilance) de tous les ingrédients analysés.
- **IHI-N-SC-007**: 100 % des cartes d'ingrédients problématiques contiennent les champs a minima (titre, type, Impact, Fait établi, Nuance, Cible particulièrement) et chaque ingrédient est ancré dans le `ValidatedIngredientSegment` (Feature C).
- **IHI-N-SC-008**: 100 % des cas « profil non sélectionné » déclenchent un **fallback implicite sur le profil par défaut « Adulte »** (profil unique, rappel « Évalué pour vous : Adulte » + signal visuel « profil par défaut ») ; 0 % produisent une critique 4-profils silencieuse.
- **IHI-N-SC-009**: 100 % des sorties LLM produites depuis le prompt personnalisé Feature N sont parsables par le parseur étendu (marqueur unique + blocs attendus) ; 100 % des sorties 4-marqueurs non conformes au format profil unique sont rejetées (pas de support 4-profils résiduel).
- **IHI-N-SC-010**: 100 % des exécutions de construction du prompt sont répétables (même segment + même profil → même prompt) sur ≥ 3 exécutions successives.
- **IHI-N-SC-011**: 100 % des prompts construits préservent la conformité Feature C (aucune incitation à inventer des ingrédients absents, ancrage préservé) et les garde-fous éthiques Feature L (pas de diagnostic, pas de prescription).
- **IHI-N-SC-012**: La conformité sémantique au prompt personnalisé Feature N (ciblage profil unique, rappel « Évalué pour vous », structure des cartes, niveau de prudence) est tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe (aligné **IHI-C-FR-006** MVP) ; le format de sortie (marqueur unique + blocs) reste vérifié par le parseur étendu (**IHI-N-SC-009**). Aucun audit automatisé bloquant n'est exigé au MVP.

---

## Cross-domain Notes

- Consomme le segment validé de `ingredient-normalization-validation` (source de vérité pour l’ancrage — Feature C).
- Utilise le gateway de `local-llm-runtime` pour l'inférence.
- L'orchestration UX est gérée par `user-guidance-experience` (**Ref.** pastille kcal en tête d’écran résultat — **UGE-A-FR-022**, Feature K ; **Ref.** sélection du profil utilisateur lors de l'Onboarding et persistance du profil — Feature N).
- Les KPI additifs détaillés sont du ressort de `additive-risk-insights` ; leur juxtaposition à une analyse LLM succès est régie par **IHI-C-FR-007** (attribution explicite + ancrage littéral des mentions dans le segment).

## Source Mapping

- `specs/016-test-llm-mock/` (Feature A)
- Intake `/speckit-design` 2026-05-13 (Feature C)
- Intake `/speckit-design` + `/speckit-specify` 2026-05-13 (Feature K)
- Intake `/speckit-design` + `/speckit-specify` 2026-06-28 (Feature L)
- Intake `/speckit-design` + `/speckit-specify` 2026-06-28 (Feature M)
- Intake `/speckit-design` + `/speckit-specify` 2026-06-28 (Feature N)

## Assumptions

- Le runtime LLM local est installé et utilisable dans l'environnement de développement.
- Le test bouchonné vise le flux d'appel et de réponse, pas la qualité nutritionnelle intrinsèque.
- La chaîne mockée est la source de vérité pour le scénario de test.
- Pour Feature C, la **v1** de la politique d’équivalence est **stricte** (vide + normalisations mécaniques listées) ; les extensions (synonymes, etc.) sont **explicites, bornées et versionnées**.
- La classification `non-analysable-response` (et équivalents) est acceptée comme résultat utilisateur valide lorsque l’ancrage échoue.
- Un **contrat de read-model** (ou équivalent) avec `additive-risk-insights` est disponible pour permettre l’attribution explicite visée par **IHI-C-FR-007** ; à défaut, l’enrichissement additif ne s’affiche pas en juxtaposition d’un succès LLM.
- Au **MVP**, la conformité à **IHI-C-FR-006** est démontrable par **relecture humaine** et traçabilité ; des garde-fous automatisés supplémentaires relèvent du plan d’évolution hors obligation minimale.
- Pour **Feature K**, la **source** exacte du champ modèle et la **règle d’arrondi** vers l’entier affiché relèvent du plan d’implémentation ; les **bornes d’affichage** **1–1100** kcal/100 g et les contraintes **IHI-K-FR-004** / **IHI-K-FR-006** sont désormais fixées en spec.
- Pour **Feature L**, le prompt personnalisé est un **remplacement en dur versionné** dans le builder (pas d’externalisation ni de registre) ; la personnalisation est **limitée au prompt de critique santé** (le bilan de composition garde son propre contrat) ; le seuil « liste très longue » est défini en **nombre d’ingrédients** (valeur exacte au plan) ; la conformité sémantique au prompt est tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe (aligné Feature C), le format restant vérifié par le parseur existant.
- Pour **Feature M**, le correctif est strictement un **câblage de navigation** (route `HealthCritiqueEntry` + bouton dans `LlmResultScreen`) ; `HealthCritiqueScreen` existant est réutilisé sans modification ; la synchronisation du segment repose sur le flux existant `lastValidatedSegmentForHealth` ; aucune modification du moteur, du prompt, du parseur ou du flux composition.
- Pour **Feature N**, la sélection et la persistance du profil utilisateur sont du ressort de `user-guidance-experience` (saisie lors de l'Onboarding + édition dans un écran « Paramètres / Profil ») ; IHI **consomme** le profil. En l'absence de profil sélectionné, IHI se rabat sur le **profil par défaut « Adulte »** (profil unique, pas 4-profils) avec un signal visuel « profil par défaut ». Le format 4-marqueurs strict Feature L (**IHI-L-FR-009** / **IHI-L-SC-004**) est **supersédé et retiré** (flux 4-profils supprimé entièrement) au profit d'une sortie à **profil unique** (marqueur canonique par profil, dont `###SPORTIF` nouvellement introduit) — traçabilité conservée en spec. Les exigences Feature L non format-strict (persona, dimensions de risque, hiérarchie des preuves, populations vulnérables transversales, garde-fous éthiques, seuil « liste très longue », rédaction française, disclaimer) restent applicables. La restitution affiche un **Niveau de prudence** (jauge 3 paliers + texte court) juste sous les KPI additifs/risques existants (`additive-risk-insights`, **IHI-C-FR-007**), puis des **cartes ingrédients** limitées aux vigilances Modéré/Élevé, avec bouton « Voir tous les ingrédients analysés » déployant une **liste compacte** (nom + statut de vigilance). La conformité sémantique est tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe (aligné Feature C).
