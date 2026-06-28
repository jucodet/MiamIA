# Domain Spec — ingredient-knowledge

**Domain Context**: `ingredient-knowledge`
**Created**: 2026-06-27
**Last Modified**: 2026-06-28 (Feature IKB-B — auto-update base additifs au démarrage + enrichissement OFF/Ciqual ; offline fallback)
**Status**: Draft

## Purpose

Fournir une **base de référence** sur les additifs (E-numbers) et les allergènes réglementaires, consultable **hors-ligne**, et exposer un **contexte de référence** structuré injectable dans les flux d'analyse LLM (composition + critique santé) du domaine `ingredient-health-intelligence`. Ce domaine ne produit **pas** d'analyse produit : il publie des faits généraux sur les substances, que le LLM utilise comme contexte éducatif, sans que cela ne crée un fait étiquette (ancrage `ingredient-health-intelligence` Feature C inchangé).

## Clarifications

### Session 2026-06-27

- Q: Portée de la base pour le premier livrable ? → A: **Additifs (E-numbers) + allergènes réglementaires UE** uniquement ; valeurs nutritionnelles (Ciqual) et lookup code-barres (OpenFoodFacts) reportés à des features ultérieures (P2/P3).
- Q: Contrainte de connectivité ? → A: **Offline-first** : la base référence est **embarquée** et toujours disponible sans réseau ; un enrichissement réseau (avec cache) est envisagé plus tard, hors périmètre P1.
- Q: Relation avec l'ancrage Feature C (`IHI-C-FR-001`..`007`) ? → A: Le contexte de référence publié ici est du **contenu général** au sens `IHI-C-FR-004` ; toute formulation liant « ce produit » à un additif/allergène reste soumise à l'ancrage littéral sur le `ValidatedIngredientSegment`. La base **ne définit aucun synonyme** d'équivalence (`EquivalencePolicy` v1 stricte inchangée).
- Q: Relation avec `additive-risk-insights` ? → A: `ingredient-knowledge` est **amont** : il fournit des faits référence ; `additive-risk-insights` reste propriétaire de la projection en KPI risques des faits d'analyse. Aucun court-circuitage du read-model KPI.
- Q: Comment garantit-on la non-invention quand une substance n'est pas référencée ? → A: **Repli silencieux** : absence de fiche → aucun fait injecté pour cette désignation ; le flux d'analyse conserve son comportement nominal (sans blocage, sans invention).
- Q: Comment borner la taille du contexte de référence injecté quand beaucoup de fiches correspondent ? → A: **Plafond dur sur le nombre de fiches injectées**, priorisées **allergènes d'abord, puis additifs à niveau de risque élevé** ; les fiches au-delà du plafond ne sont pas injectées (repli silencieux côté injection), sans blocage du flux.
- Q: Quelle est la clé canonique d'identification d'un additif dans la base ? → A: **E-number comme clé primaire** ; les dénominations courantes sont des **alias de recherche** rattachés à la même fiche canonique (pas de règle de synonyme métier, juste un index de recherche).
- Q: Quelle est la source authoritative de la base référence additifs/allergènes au P1 ? → A: **Taxonomie additifs OpenFoodFacts** (structurée par E-number) **+ liste des 14 allergènes réglementaires UE** ; version de la base dérivée des versions de ces sources, tracée par fiche.
- Q: Quelle échelle et source pour le niveau de risque indicatif des additifs ? → A: **3 niveaux fixes (faible / modéré / élevé)** dérivés des **étiquettes de risque OpenFoodFacts** ; le niveau est tracé comme attribut de la fiche et utilisé pour la priorisation d'injection.
- Q: Quelle strictesse de matching pour le lookup désignation → fiche ? → A: **Sous-chaîne littérale + normalisations mécaniques explicitement listées (casse, espaces, accents)** uniquement ; cohérent avec `IHI-C-FR-005`, aucune règle de synonyme métier.

### Session 2026-06-28 (Feature IKB-B)

- Q: Fréquence de mise à jour de la base référence ? → A: **À chaque démarrage** de l'application, le système tente un rafraîchissement **asynchrone et non bloquant** depuis les sources amont.
- Q: Comportement en l'absence de réseau ou en cas d'échec ? → A: **Repli offline** : utiliser la **dernière version persistée localement** (cache), à défaut la **baseline embarquée** dans l'APK ; aucun blocage, le lookup reste exploitable.
- Q: Portée de l'enrichissement « Ciqual + OFF » ? → A: La **taxonomie additive** (E-numbers, alias, rôle, niveau de risque) reste issue d'**OpenFoodFacts** en **couverture exhaustive** (vs l'extrait P1) ; **Ciqual** apporte des **attributs de composition/nutritionnels** (ex. énergie) pour les substances présentes **quand disponibles**. Attributs Ciqual absents → omis (repli silencieux, pas d'invention).
- Q: Persistance de la version rafraîchie ? → A: **Cache local persistant** (offline) pour les démarrages suivants ; la **baseline embarquée** reste le **filet de sécurité ultime** (cache absent/corrompu + réseau absent).
- Q: Conformité Feature C avec les nouveaux attributs ? → A: **Inchangée** : tout attribut supplémentaire (ex. nutritionnel) reste **contenu général**, aucun fait étiquette, **aucune extension** de `EquivalencePolicy` v1 stricte.
- Q: Le rafraîchissement peut-il bloquer l'utilisateur ? → A: **Non** : l'app reste utilisable pendant la mise à jour ; le lookup utilise la version courante disponible (persistée ou baseline) jusqu'à disponibilité de la version rafraîchie.

## Scope

- Base référence **embarquée** : fiches additifs (E-number, dénomination, rôle, niveau de risque indicatif, sources) issues de la taxonomie OpenFoodFacts, et fiches allergènes réglementaires UE issues de la liste réglementaire des 14 allergènes.
- **Lookup offline** depuis une liste d'ingrédients (désignations détectées) vers les fiches référence correspondantes.
- Publication d'un **contexte de référence** structuré, qualifié comme contenu général, destiné à l'injection dans les prompts LLM (composition + critique).
- **Jeu fixe d'ingrédients de référence** permettant de vérifier le lookup et la constitution du contexte, exécutable isolément (hors capture/OCR).
- Garde-fous de non-invention et de repli silencieux.
- Versionnement et traçabilité de la base (source/version des fiches).

## Invariants

- Aucune fiche référence n'est présentée comme un fait étiquette du produit analysé (rôle « contenu général » uniquement).
- Aucune entrée de la base ne crée une règle d'équivalence de synonyme hors `EquivalencePolicy` du domaine core.
- Une substance non référencée ne produit aucune fiche injectée (pas d'extrapolation, pas de blocage).
- Chaque fiche référence est traçable à sa source et à sa version de base.
- La base reste utilisable intégralement sans connexion réseau.

---

## Feature IKB-A — Base référence additifs/allergènes offline + injection contexte

> Origine : intake `/speckit-design` 2026-06-27
> Input : « P1 — KB locale additifs (E-numbers) + allergènes en assets, lookup offline, injection RAG dans le prompt composition + critique. Tests sur un jeu fixe d'ingrédients. »

### User Scenarios (Feature IKB-A)

#### US-IKB-A1 — Disposer d'une fiche référence hors-ligne pour un additif/allergène détecté (P1)

En tant que domaine d'analyse, je veux qu'à partir d'une liste d'ingrédients, chaque désignation d'additif ou d'allergène reconnue soit associée à une fiche référence issue d'une base embarquée, afin que l'analyse s'appuie sur des faits généraux vérifiables plutôt que sur la seule mémoire du modèle.

**Why this priority**: condition d'existence du domaine ; sans lookup offline, pas de contexte de référence injectable.

**Independent Test**: vérifiable avec une liste d'ingrédients fixe et la base embarquée seule, sans capture ni réseau.

**Acceptance Scenarios**:

1. **Given** une liste d'ingrédients contenant un additif référencé (ex. un E-number connu), **When** le lookup est exécuté, **Then** la fiche référence correspondante est retournée (dénomination, rôle, niveau de risque indicatif, source).
2. **Given** une liste contenant un allergène réglementaire UE référencé, **When** le lookup est exécuté, **Then** la fiche allergène est retournée avec sa dénomination réglementaire.
3. **Given** une substance non référencée dans la base, **When** le lookup est exécuté, **Then** aucune fiche n'est retournée pour cette désignation et aucune donnée n'est inventée.

#### US-IKB-A2 — Le LLM reçoit un contexte de référence qualifié comme général (P1)

En tant qu'utilisatrice finale, je veux que l'analyse de composition et la critique santé s'appuient sur un contexte de référence factuel sur les additifs/allergènes présents, présenté comme connaissance générale et non comme une lecture de mon étiquette.

**Why this priority**: c'est la valeur métier directe attendue : fiabiliser le LLM sans rompre la confiance d'ancrage.

**Independent Test**: vérifiable en comparant le contexte de référence produit pour un jeu fixe d'ingrédients au contenu attendu (faits présents, qualification « général » explicite, aucun fait étiquette).

**Acceptance Scenarios**:

1. **Given** un lookup ayant retourné des fiches pour des désignations présentes dans le segment, **When** le contexte de référence est constitué, **Then** il est explicitement balisé comme contenu général et ne contient aucune formulation présentant un fait comme issu de l'étiquette du produit.
2. **Given** un contexte de référence injecté dans le flux LLM, **When** l'analyse est produite, **Then** toute formulation liant « ce produit » à un additif/allergène reste ancrée sur une sous-chaîne littérale du segment validé (respect `IHI-C-FR-004`/`IHI-C-FR-005`).
3. **Given** un lookup ne retournant aucune fiche, **When** le contexte de référence est constitué, **Then** aucun contexte n'est injecté et le flux LLM conserve son comportement nominal.

#### US-IKB-A3 — Vérifier le lookup et le contexte sur un jeu fixe d'ingrédients (P2)

En tant que développeuse, je veux un jeu fixe d'ingrédients de référence exécutable isolément, à la manière d'un test bouchonné, afin de vérifier la stabilité du lookup et de la constitution du contexte indépendamment de la capture et de l'OCR.

**Why this priority**: traçabilité et reproductibilité du comportement domaine ; non bloquant pour la production d'une analyse mais requis pour la qualité continue.

**Independent Test**: exécutable seul, hors suites dépendantes de la caméra/OCR/runtime LLM, avec des attentes fixes.

**Acceptance Scenarios**:

1. **Given** le jeu fixe d'ingrédients de référence, **When** le test est exécuté, **Then** le lookup retourne exactement les fiches attendues (substances référencées) et aucune fiche pour les substances non référencées.
2. **Given** le même jeu, **When** le contexte de référence est constitué, **Then** son contenu et sa qualification « général » correspondent aux attentes fixes.
3. **Given** le test exécuté sur ≥ 3 exécutions successives, **When** les résultats sont comparés, **Then** ils sont identiques (répétabilité).

### Edge Cases (Feature IKB-A)

- Désignation d'additif ambigüe ou partiellement lisible (OCR) : aucune fiche extrapolée ; absence de fiche.
- Variations orthographiques non couvertes par les normalisations mécaniques listées (casse, espaces, accents) : aucune correspondance, aucune fiche (pas de table d'alias étendue au P1).
- Additif référencé par un E-number et par une dénomination courante simultanément : une seule fiche canonique retournée (clé primaire = E-number, dénomination courante = alias de recherche), sans créer de règle de synonyme hors politique d'équivalence du core.
- Liste d'ingrédients vide ou sans aucune substance référencée : contexte vide, aucun blocage du flux d'analyse.
- Base embarquée absente ou illisible : repli explicite documented (erreur domaine) ; pas de contexte inventé.
- Nombre de substances référencées dépassant le plafond d'injection : seules les fiches prioritaires (allergènes, puis additifs à risque élevé) sont injectées ; les autres sont omises sans blocage et sans signal d'erreur utilisateur.

### Functional Requirements (Feature IKB-A)

- **IKB-A-FR-001**: Le système MUST embarquer une base référence **hors-ligne** couvrant les additifs (E-numbers) et les allergènes réglementaires UE, disponible sans connexion réseau.
- **IKB-A-FR-002**: Le système MUST exposer un lookup qui, à partir d'une liste d'ingrédients, retourne les fiches référence des substances référencées effectivement présentes par **sous-chaîne littérale**, en admettant uniquement les **normalisations mécaniques explicitement listées (casse, espaces, accents)** — cohérent avec `IHI-C-FR-005`.
- **IKB-A-FR-003**: Le système MUST NOT retourner de fiche pour une substance non référencée ; il MUST NOT extrapoler ni inventer de fait sur une substance absente de la base.
- **IKB-A-FR-004**: Le système MUST publier un **contexte de référence** structuré à partir des fiches retournées, explicitement qualifié comme **contenu général** (au sens `IHI-C-FR-004`), distinct de tout fait étiquette.
- **IKB-A-FR-005**: Le système MUST NOT présenter un fait du contexte de référence comme issu de l'étiquette du produit ; l'ancrage « fait produit » reste régi par `ingredient-health-intelligence` Feature C.
- **IKB-A-FR-006**: Le système MUST NOT introduire, via la base, de règle de synonyme ou d'équivalence hors la `EquivalencePolicy` v1 stricte du domaine core (`IHI-C-FR-005`).
- **IKB-A-FR-007**: Le système MUST assurer un **repli silencieux** en l'absence de fiche pour une désignation : aucun contexte injecté pour cette désignation, sans bloquer le flux d'analyse.
- **IKB-A-FR-008**: Le système MUST fournir un **jeu fixe d'ingrédients de référence** permettant de vérifier le lookup et la constitution du contexte de façon isolée, répétable, et hors dépendance à la capture, à l'OCR et au runtime LLM.
- **IKB-A-FR-009**: Le système MUST tracer, pour chaque fiche référence, sa **source** (taxonomie additifs OpenFoodFacts ou liste allergènes réglementaire UE) et la **version** de la base dont elle provient.
- **IKB-A-FR-010**: Le système MUST détecter un état de base embarquée absente ou illisible et le signaler comme erreur domaine explicite, sans produire de contexte inventé.
- **IKB-A-FR-011**: Le système MUST appliquer un **plafond** au nombre de fiches injectées dans le `ReferenceContext` ; au-delà du plafond, il MUST prioriser les **allergènes** puis les **additifs à niveau de risque élevé**, et MUST NOT injecter les fiches restantes (repli silencieux côté injection, sans blocage du flux).
- **IKB-A-FR-012**: Le système MUST identifier chaque additif par un **E-number unique** (clé primaire) ; les dénominations courantes MUST être traitées comme **alias de recherche** rattachés à la même fiche canonique, sans constituer une règle de synonyme métier.
- **IKB-A-FR-013**: Le système MUST exprimer le niveau de risque indicatif d'un additif sur une échelle fixe à **3 niveaux (faible / modéré / élevé)**, dérivée des étiquettes de risque OpenFoodFacts, et MUST tracer cette valeur comme attribut de la fiche.

### Key Entities (Feature IKB-A)

- **AdditiveFactCard**: fiche référence additif identifiée par un **E-number** (clé primaire), avec dénomination canonique, **dénominations courantes (alias de recherche)**, rôle, **niveau de risque indicatif (faible / modéré / élevé)**, source, version de base.
- **AllergenFactCard**: fiche référence allergène réglementaire UE (dénomination réglementaire, identifiant).
- **IngredientDesignation**: désignation d'ingrédient issue du segment, candidate au lookup.
- **ReferenceContext**: contexte de référence structuré (ensemble **borné** de fiches + qualification « contenu général »), destiné à l'injection dans un flux LLM ; sélection priorisée allergènes puis additifs à risque élevé, dans la limite d'un plafond.
- **LookupOutcome**: résultat du lookup (fiches retournées, désignations non référencées, trace source/version).

### Success Criteria (Feature IKB-A)

- **IKB-A-SC-001**: 100 % des substances référencées présentes dans un jeu d'ingrédients donné produisent la fiche canonique attendue.
- **IKB-A-SC-002**: 100 % des substances non référencées produisent aucune fiche et aucune donnée inventée.
- **IKB-A-SC-003**: 100 % des contextes de référence publiés sont qualifiés explicitement comme contenu général et ne contiennent aucune formulation de fait étiquette.
- **IKB-A-SC-004**: 100 % des exécutions du jeu fixe de référence sont répétables (résultats identiques sur ≥ 3 exécutions successives).
- **IKB-A-SC-005**: 100 % des fiches référence sont traçables à une source et une version de base.
- **IKB-A-SC-006**: 100 % des exécutions en l'absence de réseau renvoient un résultat de lookup exploitable (offline intégral).
- **IKB-A-SC-007**: 100 % des contextes de référence injectés respectent le plafond de fiches, avec priorisation allergènes puis additifs à risque élevé.

---

## Feature IKB-B — Auto-update de la base additifs au démarrage + enrichissement OFF/Ciqual

> Origine : intake `/speckit-design` 2026-06-28
> Input : « La base référence embarquée des additifs doit se mettre à jour à chaque démarrage de l'application. Si pas de connexion réseau, alors l'appli travaille avec la version embarquée. Enrichis cette base avec tous les additifs possibles issus de Ciqual et OFF. »
> Réalise l'enrichissement réseau + cache précédemment reporté (Cross-domain Notes IKB-A « hors périmètre P1 »).

### User Scenarios (Feature IKB-B)

#### US-IKB-B1 — Rafraîchir la base additifs à chaque démarrage (P1)

En tant qu'utilisatrice, je veux qu'à chaque ouverture de l'application la base référence des additifs se rafraîchisse automatiquement depuis les sources amont (OpenFoodFacts + Ciqual), afin que mon analyse s'appuie sur la base la plus récente et la plus complète possible.

**Why this priority**: c'est le mécanisme central de la feature ; sans rafraîchissement, pas d'enrichissement ni de mise à jour.

**Independent Test**: vérifiable au démarrage avec réseau disponible → la base utilisée pour le lookup reflète la version rafraîchie ; l'UI n'est pas bloquée pendant l'opération.

**Acceptance Scenarios**:

1. **Given** l'application démarre et une connexion réseau est disponible, **When** le rafraîchissement est tenté, **Then** la base référence est mise à jour depuis les sources amont et la version rafraîchie est utilisée pour les analyses suivantes.
2. **Given** le rafraîchissement en cours, **When** l'utilisatrice interagit avec l'app, **Then** aucune interaction n'est bloquée ; le lookup utilise la version courante disponible jusqu'à disponibilité de la version rafraîchie.
3. **Given** le rafraîchissement aboutit, **When** il se termine, **Then** la version rafraîchie est persistée localement pour les démarrages suivants.

#### US-IKB-B2 — Travailler hors-ligne avec la version disponible (P1)

En tant qu'utilisatrice sans connexion réseau, je veux que l'application continue de fonctionner avec la meilleure base disponible localement, afin de ne jamais être bloquée par l'absence de réseau.

**Why this priority**: garantit l'offline-first et la disponibilité continue ; sans repli, l'absence de réseau casserait l'analyse.

**Independent Test**: vérifiable au démarrage sans réseau → le lookup reste exploitable (version persistée ou baseline embarquée), aucun blocage.

**Acceptance Scenarios**:

1. **Given** l'application démarre sans connexion réseau, **When** le rafraîchissement est tenté, **Then** il échoue silencieusement et le lookup utilise la **dernière version persistée localement**.
2. **Given** l'application démarre sans réseau **et** sans cache local exploitable, **When** le lookup est requis, **Then** le système utilise la **baseline embarquée** dans l'APK.
3. **Given** un échec réseau, **When** l'utilisatrice consulte l'analyse, **Then** aucune erreur bloquante n'est affichée et le flux d'analyse reste nominal.

#### US-IKB-B3 — Couverture additive exhaustive + attributs Ciqual (P2)

En tant que domaine d'analyse, je veux que la base enrichie couvre l'ensemble des additifs de la taxonomie OpenFoodFacts et intègre les attributs de composition Ciqual quand ils sont disponibles, afin de maximiser la couverture et la richesse factuelle du contexte de référence.

**Why this priority**: valeur métier d'enrichissement (couverture + attributs) ; dépend du mécanisme de rafraîchissement (US-IKB-B1) mais peut être livrée incrémentalement.

**Independent Test**: vérifiable en comparant la base enrichie à la taxonomie OFF (couverture exhaustive) et en contrôlant la présence/trace des attributs Ciqual sur un échantillon connu.

**Acceptance Scenarios**:

1. **Given** la taxonomie additive OpenFoodFacts complète, **When** la base enrichie est constituée, **Then** chaque E-number de la taxonomie possède une fiche canonique dans la base.
2. **Given** un additif pour lequel Ciqual fournit des attributs de composition, **When** la fiche est enrichie, **Then** ces attributs sont présents et traçables (source Ciqual + version).
3. **Given** un additif pour lequel Ciqual ne fournit aucun attribut, **When** la fiche est constituée, **Then** les attributs Ciqual sont omis (repli silencieux) et aucune donnée n'est inventée.

### Edge Cases (Feature IKB-B)

- Réseau partiel : une source disponible, l'autre non → utiliser ce qui est disponible ; attributs/fiches manquants omis (repli silencieux, pas d'invention).
- Source amont modifie ou supprime un E-number → la version rafraîchie reflète l'amont ; la baseline embarquée conserve l'ancien état (filet de sécurité).
- Cache local corrompu ou illisible → repli sur la baseline embarquée ; erreur domaine tracée, pas d'invention.
- Données amont incohérentes (E-number dupliqué, niveau de risque invalide, attribut Ciqual incohérent) → entrée rejetée et tracée ; pas d'invention ni de blocage.
- Rafraîchissement plus lent que le premier lookup requis → le lookup utilise la version courante disponible (persistée/baseline), puis la version rafraîchie pour les suivants.

### Functional Requirements (Feature IKB-B)

- **IKB-B-FR-001**: Le système MUST tenter à **chaque démarrage** un rafraîchissement **asynchrone et non bloquant** de la base référence additifs depuis les sources amont (taxonomie OpenFoodFacts + attributs Ciqual).
- **IKB-B-FR-002**: Le système MUST rester **utilisable pendant le rafraîchissement** : le lookup utilise la version courante disponible (persistée ou baseline) jusqu'à disponibilité de la version rafraîchie.
- **IKB-B-FR-003**: Le système MUST, en cas d'absence de réseau ou d'échec amont, assurer un **repli offline** : **dernière version persistée localement**, à défaut **baseline embarquée** ; aucun blocage, lookup reste exploitable.
- **IKB-B-FR-004**: Le système MUST **persister localement** la version rafraîchie (cache offline) pour les démarrages suivants.
- **IKB-B-FR-005**: Le système MUST conserver la **baseline embarquée** dans l'APK comme **filet de sécurité ultime** (cache absent/corrompu + réseau absent).
- **IKB-B-FR-006**: Le système MUST couvrir l'**ensemble des additifs** de la taxonomie OpenFoodFacts dans la base enrichie (couverture exhaustive vs l'extrait P1).
- **IKB-B-FR-007**: Le système MUST enrichir chaque fiche additive avec les **attributs de composition Ciqual disponibles** (ex. énergie) ; attributs absents MUST être omis (repli silencieux, pas d'invention).
- **IKB-B-FR-008**: Le système MUST tracer, pour chaque attribut/fiche, sa **source** (OpenFoodFacts ou Ciqual) et la **version** de la base dont il provient.
- **IKB-B-FR-009**: Le système MUST respecter **Feature C** : tout attribut supplémentaire (ex. nutritionnel) reste **contenu général**, aucun fait étiquette ; MUST NOT étendre `EquivalencePolicy` v1 stricte.
- **IKB-B-FR-010**: Le système MUST détecter un **cache corrompu/illisible** et replier sur la baseline embarquée, en signalant une erreur domaine explicite (tracée, sans invention).
- **IKB-B-FR-011**: Le système MUST rejeter et tracer toute **entrée amont incohérente** (E-number dupliqué, niveau de risque invalide, attribut incohérent) sans produire de fiche inventée ni bloquer le flux.

### Key Entities (Feature IKB-B)

- **AdditiveFactCard (étendu)**: fiche référence additif enrichie d'**attributs Ciqual optionnels** (ex. composition/énergie) lorsque disponibles, en plus des attributs OFF (E-number, alias, rôle, niveau de risque, source, version).
- **KbCache**: version rafraîchie **persistée localement** (offline) pour les démarrages suivants ; version + timestamp + sources.
- **KbRefreshOutcome**: résultat du rafraîchissement (succès / partiel / repli offline), sources consultées, version obtenue, timestamp, raisons d'échec éventuelles.
- **KbBaseline**: version **embarquée** dans l'APK (filet de sécurité ultime) — correspond à la base P1 (Feature IKB-A).

### Success Criteria (Feature IKB-B)

- **IKB-B-SC-001**: 100 % des démarrages avec réseau disponible aboutissent à une base rafraîchie utilisée pour les analyses suivantes, sans blocage de l'UI.
- **IKB-B-SC-002**: 100 % des démarrages sans réseau fournissent un lookup exploitable (version persistée ou baseline embarquée), sans erreur bloquante.
- **IKB-B-SC-003**: 100 % des E-numbers de la taxonomie OpenFoodFacts sont présents dans la base enrichie (couverture exhaustive).
- **IKB-B-SC-004**: 100 % des attributs Ciqual injectés sont traçables à une source et une version.
- **IKB-B-SC-005**: 100 % des attributs Ciqual absents sont omis sans invention.
- **IKB-B-SC-006**: 0 % de blocage de l'UI pendant le rafraîchissement (l'app reste interactive).
- **IKB-B-SC-007**: 100 % des contextes publiés depuis la base enrichie respectent Feature C (contenu général, aucun fait étiquette, aucune extension d'équivalence).

---

## Cross-domain Notes

- **Upstream de** `ingredient-health-intelligence` : publie un `ReferenceContext` consommé comme contexte général par les flux LLM composition + critique (pattern *Published Language* / *Open Host Service*).
- **Ne court-circuite pas** `additive-risk-insights` : les KPI additifs restent la projection des faits d'analyse par ce domaine ; `ingredient-knowledge` ne fournit que des faits référence amont.
- **Respecte** `ingredient-normalization-validation` : le lookup s'appuie sur des désignations issues du `ValidatedIngredientSegment` ; aucune nouvelle règle d'équivalence.
- **Réalise (Feature IKB-B)** l'enrichissement réseau + cache précédemment reporté : auto-update au démarrage (OFF + Ciqual), offline fallback (cache persisté puis baseline embarquée) ; conformité Feature C inchangée.
- **Hors périmètre (reste reporté)** : lookup code-barres OpenFoodFacts produit (feature ultérieure).

## Source Mapping

- Intake `/speckit-design` 2026-06-27 (Feature IKB-A).
- Intake `/speckit-design` 2026-06-28 (Feature IKB-B).

## Assumptions

- La base référence P1 (Feature IKB-A) se limite aux additifs (E-numbers) issus de la taxonomie OpenFoodFacts et aux 14 allergènes réglementaires UE ; couverture exhaustive des E-numbers non exigée au premier livrable (jeu fixe de référence suffisant).
- **Feature IKB-B** : la taxonomie additive (E-numbers, alias, rôle, risque) reste issue d'OpenFoodFacts ; Ciqual apporte des **attributs de composition/nutritionnels** (ex. énergie) et non une taxonomie d'additifs. Si l'intention était différente (ex. Ciqual comme source d'additifs), lancer `/speckit-clarify`.
- Le flux LLM consommateur (`ingredient-health-intelligence`) accepte un bloc de contexte général balisé, conformément à `IHI-C-FR-004`.
- La politique d'équivalence du core reste **stricte v1** ; aucune extension de synonyme n'est apportée par ce domaine.
- La baseline embarquée (Feature IKB-A) reste toujours disponible dans l'APK comme filet de sécurité ultime, indépendamment du cache et du réseau.
- Le rafraîchissement au démarrage est **non bloquant** : l'app reste interactive et le lookup utilise la meilleure version disponible à l'instant considéré.
- Le jeu fixe d'ingrédients de référence est exécutable hors suites dépendantes de la caméra/OCR/runtime LLM.
