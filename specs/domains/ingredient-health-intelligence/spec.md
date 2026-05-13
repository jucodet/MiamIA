# Domain Spec — ingredient-health-intelligence

**Domain Context**: `ingredient-health-intelligence`
**Created**: 2026-05-06
**Last Modified**: 2026-05-13 (Feature K — pastille kcal/100 g ; clarify bornes affichage 1–1100)
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

## Cross-domain Notes

- Consomme le segment validé de `ingredient-normalization-validation` (source de vérité pour l’ancrage — Feature C).
- Utilise le gateway de `local-llm-runtime` pour l'inférence.
- L'orchestration UX est gérée par `user-guidance-experience` (**Ref.** pastille kcal en tête d’écran résultat — **UGE-A-FR-022**, Feature K).
- Les KPI additifs détaillés sont du ressort de `additive-risk-insights` ; leur juxtaposition à une analyse LLM succès est régie par **IHI-C-FR-007** (attribution explicite + ancrage littéral des mentions dans le segment).

## Source Mapping

- `specs/016-test-llm-mock/` (Feature A)
- Intake `/speckit-design` 2026-05-13 (Feature C)
- Intake `/speckit-design` + `/speckit-specify` 2026-05-13 (Feature K)

## Assumptions

- Le runtime LLM local est installé et utilisable dans l'environnement de développement.
- Le test bouchonné vise le flux d'appel et de réponse, pas la qualité nutritionnelle intrinsèque.
- La chaîne mockée est la source de vérité pour le scénario de test.
- Pour Feature C, la **v1** de la politique d’équivalence est **stricte** (vide + normalisations mécaniques listées) ; les extensions (synonymes, etc.) sont **explicites, bornées et versionnées**.
- La classification `non-analysable-response` (et équivalents) est acceptée comme résultat utilisateur valide lorsque l’ancrage échoue.
- Un **contrat de read-model** (ou équivalent) avec `additive-risk-insights` est disponible pour permettre l’attribution explicite visée par **IHI-C-FR-007** ; à défaut, l’enrichissement additif ne s’affiche pas en juxtaposition d’un succès LLM.
- Au **MVP**, la conformité à **IHI-C-FR-006** est démontrable par **relecture humaine** et traçabilité ; des garde-fous automatisés supplémentaires relèvent du plan d’évolution hors obligation minimale.
- Pour **Feature K**, la **source** exacte du champ modèle et la **règle d’arrondi** vers l’entier affiché relèvent du plan d’implémentation ; les **bornes d’affichage** **1–1100** kcal/100 g et les contraintes **IHI-K-FR-004** / **IHI-K-FR-006** sont désormais fixées en spec.
