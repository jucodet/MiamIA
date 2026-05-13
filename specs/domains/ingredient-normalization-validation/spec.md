# Feature Specification: ingredient-phrase-segment

**Feature Branch**: `021-auto-analyze-ingredients-tag` *(évolution 2026-05-13 ; règles d’ancrage issues de `017-ocr-dot-end-capture` inchangées)*  
**Domain Context**: `ingredient-normalization-validation`  
**Target Domain Folder**: `specs/domains/ingredient-normalization-validation`  
**Created**: 2026-05-06  
**Status**: Draft — *Backfill P12 applied 2026-05-12* · *Doc sync Feature G 2026-05-13*  
**Input**: User description: "Modifier la capture OCR pour prendre en compte le caractère '.' suivi d'un espace ou d'un retour à la ligne comme fin de capture de la liste d'ingrédients. Un point non suivi d'un espace ou retour à la ligne (ex. abréviations, codes additifs) ne constitue pas une fin de capture." — *Évolutions 2026-05-13* : (1) « Après un OCR réussi, l'analyse est déclenchée immédiatement si un texte a été capturé avec la balise ingrédients, sans passer par l'écran de validation du texte segmenté. » (2) « Supprimer la phase de segmentation de la liste des ingrédients avant l'envoi au modèle de langage ; transmettre l'intégralité du texte OCR pour l'analyse. »

## Clarifications

### Session 2026-05-06

- Q: Faut-il reconnaître « Ingredients » (anglais, casse tolérée) comme ancre équivalente à « Ingrédient(s) » pour la première occurrence (FR-002) ? → A: Oui (option A) — « Ingredients » est traité comme équivalent sémantique de l'ancre française, avec les mêmes tolérances de casse et de mise en forme que pour « Ingrédient(s) ».
- Q: Pour l'anglais, l'ancre doit-elle inclure le singulier « Ingredient » en plus du pluriel « Ingredients » ? → A: Oui (option A) — les formes anglaises singulier et pluriel sont toutes deux reconnues, avec la même tolérance de casse.

### Session 2026-05-11

- Évolution FR-003 : le caractère `.` n'est reconnu comme fin de capture de la liste d'ingrédients que lorsqu'il est **suivi d'un espace ou d'un retour à la ligne**. Un point non suivi d'un de ces deux caractères (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne déclenche pas la fin de capture.

### Session 2026-05-13 — Enchaînement analyse sans écran de validation (historique : balise ingrédients)

- *Comportement d'enchaînement et de contenu d'entrée mis à jour par la session **« OCR intégral en entrée du modèle de langage »** ci-dessous (FR-010, FR-012).*
- Q: Le critère **SC-004** (compréhension en moins de 10 secondes) doit-il inclure le parcours FR-010 où l'écran de proposition / validation n'est pas affiché ? → A: Option **A** — SC-004 ne s'applique qu'aux parcours où l'écran de proposition ou de validation est affiché (**hors** périmètre FR-010).

### Session 2026-05-13 — OCR intégral en entrée du modèle de langage

- Toute analyse aval exécutée via un **modèle de langage** reçoit comme entrée l'**intégralité du texte étiquette** issu de la reconnaissance réussie pour la session, **sans** phase préalable d'isolation, d'ancrage, de proposition ou de validation de segment pour constituer cette entrée (**FR-012**, **FR-014**).
- Les règles historiques d'ancrage et de fin de segment (**FR-001** à **FR-006**) **ne filtrent pas** et **ne tronquent pas** le texte transmis au modèle ; elles ne s'appliquent qu'à d'éventuelles sorties **purement informatives** distinctes de l'entrée d'analyse LLM, si le produit les conserve.
- **FR-010** est aligné : enchaînement immédiat post-OCR réussi (**transcript admissible par le gate**, parcours nominal) vers l'analyse **sans** écran de validation du texte segmenté **et** avec entrée modèle = texte OCR intégral. L'ancienne UI « balise ingrédients » est **retirée** (**Ref.** `user-guidance-experience` Feature G) ; l'équivalent intentionnel est l'**orchestration capture** documentée dans `contracts/session-capture-intent-for-implicit-validation.md`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Analyse sur le texte OCR intégral (Priority: P1)

En tant qu'utilisatrice, après une reconnaissance réussie de mon étiquette, je veux que l'analyse par modèle de langage s'appuie sur **tout** le texte reconnu (sans découpe préalable automatique autour de la mention « ingrédients »), afin que le modèle dispose du contexte complet visible dans l'OCR pour interpréter la composition.

**Why this priority**: Répond à l'intention produit actuelle ; supprime une étape d'isolation susceptible d'exclure du contexte utile ou d'introduire une coupure erronée avant le raisonnement du modèle.

**Independent Test**: Sur un jeu d'étiquettes où le texte OCR contient plusieurs sections, vérifier que la chaîne transmise au service d'analyse par modèle de langage est **identique** au texte OCR sessionnel disponible après succès de reconnaissance (hors corrections utilisateur explicitement enregistrées avant envoi, si le produit en prévoit).

**Acceptance Scenarios**:

1. **Given** une reconnaissance réussie produisant un texte multi-paragraphes, **When** l'analyse par modèle de langage démarre, **Then** l'entrée du modèle inclut l'intégralité de ce texte, sans sous-chaîne issue seule des règles d'ancrage FR-002 à FR-006.
2. **Given** une session sans correction manuelle du texte OCR avant analyse, **When** l'appel au modèle est formé, **Then** aucune étape d'isolation ni de validation de segment n'est requise comme prérequis à cet appel.

---

### User Story 1b - Proposition déterministe de zone ingrédients (hors entrée LLM) (Priority: P2)

En tant qu'utilisatrice, lorsque le produit expose encore une **vue** ou un **résumé** dérivé des règles d'ancrage (affichage d'aide à la lecture, comparaison, traçabilité), je veux que cette proposition reste délimitée de façon prévisible selon la mention d'ingrédients sur l'étiquette (français **ou** équivalent anglais reconnu), **sans** que cette zone ne remplace ou ne filtre le texte envoyé au modèle de langage.

**Why this priority**: Préserve la prévisibilité des règles métier d'ancrage là où elles servent l'UX ou la traçabilité, tout en les dissociant du chemin d'analyse LLM.

**Independent Test**: À partir d'échantillons de texte représentatifs (ancre française, **ancres anglaises « Ingredient » et « Ingredients »**, plusieurs mentions, avec ou sans ponctuation de fin de phrase, **avec points internes non suivis d'espace**), vérifier que toute **proposition auxiliaire** respecte FR-002 à FR-006, et qu'en parallèle l'entrée LLM reste le texte OCR intégral (FR-012).

**Acceptance Scenarios**:

1. **Given** un texte contenant une phrase qui commence par une forme reconnue du mot-ancre (« Ingrédient », « Ingrédients », « Ingredient » ou « Ingredients ») avec des variations raisonnables de casse et de mise en forme, **When** le système prépare une **proposition auxiliaire** (hors entrée LLM), **Then** cette proposition respecte les règles de délimitation décrites dans FR-002 à FR-006.
2. **Given** une phrase-ancre contenant des points internes non suivis d'un espace ou d'un retour à la ligne (ex. « E.621 », « vit.B12 »), **When** le système délimite cette proposition auxiliaire, **Then** ces points internes ne déclenchent pas la fin de capture pour cette vue.
3. **Given** plusieurs occurrences d'une mention « ingrédients » dans le texte, **When** le système choisit l'ancre pour la proposition auxiliaire, **Then** seule la première occurrence rencontrée en lisant le texte de l'étiquette dans l'ordre naturel sert à cette proposition.
4. **Given** un texte multi-lignes sans terminateur de phrase (`.` + espace/newline, `!`, `?`) après l'ancre, **When** le système délimite la proposition auxiliaire, **Then** la capture traverse les sauts de ligne et se termine à la fin du texte disponible.
5. **Given** un texte monoligne sans terminateur de phrase, **When** le système délimite la proposition auxiliaire, **Then** la proposition auxiliaire se termine à la fin du texte fourni.
6. **Given** un texte dont la phrase-ancre commence par « Ingredients » (forme anglaise, avec variations raisonnables de casse), **When** le système prépare la proposition auxiliaire, **Then** les mêmes règles de délimitation (`.` + espace/retour à la ligne, `!`, `?`, ou fin de texte) s'appliquent comme pour une ancre française.

---

### User Story 2 - Confirmer ou corriger une vue segmentée (Priority: P3)

En tant qu'utilisatrice, **lorsque** le produit affiche encore une **proposition auxiliaire** de zone ingrédients (US1b) et propose une confirmation ou une correction **à des fins d'affichage ou de traçabilité**, je veux pouvoir valider ou ajuster cette vue **sans** que cette étape ne conditionne l'entrée du modèle de langage (FR-012).

**Why this priority**: Découple l'option UX « segment informatif » du chemin d'analyse LLM désormais fondé sur l'OCR intégral.

**Independent Test**: Parcours avec proposition auxiliaire affichée : toute confirmation ou correction n'altère pas le principe « entrée LLM = texte OCR intégral » tant que le produit n'offre pas explicitement une étape « texte à analyser » distincte validée par l'utilisatrice.

**Acceptance Scenarios**:

1. **Given** une proposition auxiliaire affichée, **When** l'utilisatrice confirme explicitement cette vue, **Then** l'entrée du modèle de langage reste le texte OCR intégral sauf si le parcours produit introduit séparément une intention explicite d'analyser un texte utilisateur modifié (hors scope par défaut).
2. **Given** une proposition auxiliaire affichée, **When** l'utilisatrice corrige le texte de cette vue, **Then** la correction affecte au plus la proposition auxiliaire et n'est pas imposée comme entrée LLM sans action produit explicite supplémentaire.

---

### User Story 2b - Parcours accéléré post-OCR (ancien libellé : balise ingrédients) (Priority: P1)

En tant qu'utilisatrice, lorsque la reconnaissance de texte réussit avec un **texte OCR non vide** admissible sur le **parcours nominal** (sans étape de relecture transcript), je veux que l'analyse par modèle de langage démarre **tout de suite**, sans étape intermédiaire de validation d'un texte segmenté, afin de gagner du temps lorsque ma cible de capture est l'étiquette.

**Why this priority**: Réduit la friction ; l'intention « analyse directe » est portée par la politique produit / orchestration (Feature G) plutôt que par un chip UI séparé.

**Independent Test** : Parcours nominal : OCR réussi, transcript non vide et admissible par le gate ; vérifier absence d'écran de validation du texte segmenté **comme prérequis** à l'analyse LLM et envoi du texte OCR intégral au modèle.

**Acceptance Scenarios**:

1. **Given** une session de capture sur le parcours nominal (transcript non vide et admissible par le gate), **When** l'OCR se termine avec succès, **Then** le système enchaîne vers l'analyse par modèle de langage **sans** afficher l'écran de validation du texte segmenté comme condition d'entrée du modèle.
2. **Given** le même contexte, **When** l'analyse par modèle de langage démarre, **Then** l'entrée du modèle est l'intégralité du texte OCR (FR-012), sans sous-chaîne issue seule des règles FR-002 à FR-006.

---

### User Story 3 - Comprendre l'absence de texte exploitable ou d'ancre pour vue auxiliaire (Priority: P3)

En tant qu'utilisatrice, si la reconnaissance ne fournit pas de texte exploitable, **ou** si le texte ne permet pas d'appliquer la règle d'ancrage pour une **proposition auxiliaire** (US1b), je veux un message clair et la possibilité de reprendre (nouvelle capture ou correction), sans contenu inventé.

**Why this priority**: Évite la frustration et les analyses fondées sur du vide ou sur une liste auxiliaire factice.

**Independent Test**: Fournir des OCR vides ou échoués et vérifier FR-008 ; fournir des textes sans ancre exploitable et vérifier le comportement attendu pour la vue auxiliaire **sans** bloquer à tort une analyse LLM sur le texte complet lorsque celui-ci est non vide.

**Acceptance Scenarios**:

1. **Given** un texte sans occurrence exploitable de « Ingrédient(s) » selon la règle d'ancrage, **When** le système tente une proposition auxiliaire, **Then** aucune liste auxiliaire n'est présentée comme définitive sans intervention et un message explicite l'indique **tout en** permettant, si le texte OCR global est non vide, l'analyse LLM sur ce texte intégral (FR-012).
2. **Given** une reconnaissance en échec ou un texte OCR vide, **When** l'utilisatrice tente de continuer, **Then** le système bloque l'analyse aval et propose une reprise ou une correction (FR-008).

---

### Edge Cases

- Plusieurs langues sur la même étiquette : pour toute **proposition auxiliaire**, la règle s'applique à la **première** occurrence d'ancre reconnue (français « Ingrédient(s) » **ou** anglais « Ingredients ») dans l'ordre de lecture du texte fourni ; l'autre langue n'est utilisée que si elle apparaît en premier. **L'entrée du modèle de langage reste le texte OCR intégral (FR-012).**
- Étiquette uniquement en anglais : une phrase commençant par « Ingredients » déclenche la même logique d'isolation **pour la proposition auxiliaire** qu'une ancre française.
- **Point interne (`.` non suivi d'espace ou retour à la ligne)** : ne déclenche pas la fin de capture **de la proposition auxiliaire**. Exemples courants : codes additifs (« E.621 »), abréviations (« vit.B12 »), numéros de lot (« L.12345 »). La capture continue jusqu'au prochain marqueur de fin valide.
- **Point suivi d'un espace ou retour à la ligne** : reconnu comme fin de la **proposition auxiliaire** de liste. Couvre le point final classique d'une phrase « Ingrédients: eau, sel, sucre. » ainsi que les cas où le point clôt un bloc suivi d'un paragraphe distinct.
- Ponctuation `!` et `?` : conservent leur rôle de fin de phrase standard pour la **proposition auxiliaire** ; ces signes ne nécessitent pas de condition contextuelle supplémentaire (espace/retour à la ligne) car ils sont sans ambiguïté dans le contexte des étiquettes alimentaires.
- Texte multi-lignes sans terminateur : pour la proposition auxiliaire, la capture traverse les sauts de ligne et inclut tout le texte restant jusqu'au terminateur ou à la fin du texte. Les voies de reprise ou correction après analyse restent celles du parcours produit existant.
- Ancre présente mais **proposition auxiliaire** tronquée hors champ photo : la proposition auxiliaire peut être incomplète ; le système doit pouvoir signaler l'incertitude sans inventer de contenu (comportement aligné sur les parcours existants de reprise), **sans** imposer cette troncature à l'entrée LLM (FR-012).
- **Balise « ingrédients » mais OCR en échec ou texte vide** : aucun enchaînement automatique vers l'analyse ; messages et reprises alignés sur FR-008.
- **Texte OCR non vide mais sans ancre exploitable pour une vue auxiliaire** : l'analyse LLM sur texte intégral reste permise (FR-012) ; le message d'indisponibilité de proposition auxiliaire ne bloque pas cette analyse **sauf** si le parcours produit impose explicitement une autre règle (hors scope par défaut).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST appliquer une règle unique d'isolation de la liste d'ingrédients sur le texte issu de l'étiquette **lorsqu'une proposition auxiliaire de segment est produite (US1b)** ; toute règle antérieure fondée sur une autre forme d'ancre prioritaire ou sur une fin de segment systématiquement au premier saut de ligne sans tenir compte de la phrase « Ingrédient(s) » est **abrogée** pour cette proposition auxiliaire. **FR-001** ne MUST NOT réduire ni filtrer le texte transmis au modèle de langage (FR-014).
- **FR-002**: Le système MUST identifier comme point de départ la **première** occurrence, dans l'ordre de lecture du texte, d'une phrase qui commence par un libellé équivalent à « Ingrédient » ou « Ingrédients » **ou**, à équivalence sémantique pour les étiquettes bilingues, par « Ingredient » ou « Ingredients » (anglais), en tolérant des variations raisonnables de casse, d'accents (y compris absence d'accent sur formes attendues), d'espaces et de ponctuation immédiate (par exemple deux-points) après le mot-ancre — **uniquement pour la construction d'une proposition auxiliaire** ; ces critères ne MUST NOT déterminer les bornes de l'entrée du modèle de langage (FR-014).
- **FR-003**: Le système MUST délimiter la fin de la **proposition auxiliaire** au premier caractère `.` (point) **suivi d'un espace ou d'un retour à la ligne** rencontré après l'ancre **dans tout le texte disponible** (la recherche traverse les sauts de ligne). Un point non suivi d'un espace ou d'un retour à la ligne (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne MUST NOT être considéré comme fin de capture **de cette proposition auxiliaire**. Un `.` en dernière position du texte (fin absolue) est reconnu comme terminateur. Les caractères `!` et `?` restent reconnus comme fin de phrase standard sans condition contextuelle supplémentaire.
- **FR-004**: ~~Abrogé~~ — le saut de ligne seul ne constitue plus un point d'arrêt de la **proposition auxiliaire**. La zone auxiliaire peut s'étendre sur plusieurs lignes.
- **FR-005**: Lorsqu'aucun terminateur de phrase (`.` + espace/retour à la ligne, `!`, `?`) n'est trouvé dans tout le texte après l'ancre, le système MUST délimiter la fin de la **proposition auxiliaire** à la **fin du texte disponible**.
- **FR-006**: Lorsque plusieurs occurrences d'une ancre reconnue (française ou anglaise selon FR-002) existent, le système MUST **ignorer** toute occurrence autre que la première pour construire la **proposition auxiliaire**.
- **FR-007**: Le système MUST exiger une **confirmation explicite** de l'utilisatrice avant tout traitement aval **non couvert** par FR-012 **qui continuerait à imposer** une liste isolée comme entrée obligatoire ; **les analyses par modèle de langage sur le texte OCR intégral sont exemptées**. Lorsqu'une **proposition auxiliaire** est affichée avec intention de validation métier distincte de l'entrée LLM, les scénarios de confirmation restent couverts par **User Story 2**.
- **FR-008**: Le système MUST refuser de lancer une analyse aval lorsque la reconnaissance a **échoué** ou que le **texte OCR disponible est vide ou non exploitable** (bruit sans contenu lisible), avec un message compréhensible et une voie de reprise. **L'absence d'ancre « ingrédients » ne constitue pas à elle seule** un motif de refus d'une analyse par modèle de langage lorsque le texte OCR est autrement non vide (cohérent avec FR-012).
- **FR-009**: Le système MUST conserver, au minimum en mémoire pendant la session active, la traçabilité entre le texte brut fourni pour l'étiquette (`scanId`), toute **proposition auxiliaire** de segment et toute confirmation utilisateur associée **lorsque le produit les utilise**. La persistance au-delà de la session est une évolution souhaitable mais non bloquante pour le MVP. *(Backfill P12 — 2026-05-12 : traçabilité mémoire via scanId suffisante pour le MVP ; persistance Room reportée.)*
- **FR-010**: Lorsque l'OCR se termine par un **succès** avec un **texte OCR non vide** admissible par le gate de soumission (transcript non vide, non limité au libellé « ingrédients » seul, etc.), le système MUST **enchaîner immédiatement** vers l'analyse par modèle de langage **sans** afficher l'écran de validation du texte segmenté comme prérequis, et MUST transmettre au modèle l'**intégralité** du texte OCR (FR-012). *(Depuis Feature G / UGE-G, il n'existe plus de chip UI « balise ingrédients » : l'équivalent de l'ancienne condition « session avec balise » est la **décision produit d'orchestration** publiée par le module capture — voir `contracts/session-capture-intent-for-implicit-validation.md`.)*
- **FR-011**: Lorsque la session **n'est pas** couverte par FR-010, le système MUST appliquer FR-007 pour tout traitement aval **hors** FR-012 qui imposerait encore une liste isolée confirmée.
- **FR-012**: Toute analyse aval exécutée par **modèle de langage** MUST utiliser comme entrée l'**intégralité du texte étiquette** issu de la reconnaissance **réussie** pour la session, **sans** étape préalable d'isolation, d'ancrage, de proposition ni de validation de segment pour constituer cette entrée.
- **FR-014**: Les règles **FR-001** à **FR-006** ne MUST NOT **filtrer, tronquer ni substituer** le texte transmis au modèle de langage ; elles s'appliquent au plus à des **propositions auxiliaires** ou vues distinctes (US1b).

### Key Entities *(include if feature involves data)*

- **Texte étiquette (entrée / OCR sessionnel)**: Texte brut associé à une capture d'étiquette, tel que fourni au domaine par le parcours de reconnaissance amont ; **base obligatoire de l'entrée** des analyses par modèle de langage (FR-012).
- **Proposition auxiliaire de segment ingrédients**: Portion du texte étiquette délimitée par les règles FR-001 à FR-006 **à des fins d'affichage, d'aide à la lecture ou de traçabilité** ; **distincte** de l'entrée du modèle de langage (FR-014).
- **Segment ingrédients validé (vue auxiliaire)**: Chaîne confirmée ou corrigée par l'utilisatrice **lorsque** le produit maintient une étape de validation sur la proposition auxiliaire (User Story 2) ; ne remplace pas par défaut le texte OCR intégral comme entrée LLM.
- **État d'isolation (vue auxiliaire)**: Représente le résultat du traitement d'ancrage pour la proposition auxiliaire (proposition prête, impossible sans ancre, proposition vide, etc.).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Pour un jeu de cas de test documenté couvrant ancre unique, ancres multiples, **au moins un cas avec ancre « Ingredient » et un cas avec ancre « Ingredients »**, combinaisons ponctuation / saut de ligne / texte monoligne, **et au moins un cas avec point interne non suivi d'espace (ex. « E.621 ») qui ne doit pas déclencher la fin de capture**, **100 %** des **propositions auxiliaires** respectent les règles FR-002 à FR-006 lorsque ces propositions sont produites (vérification par contrôle manuel ou jeu de référence agréé).
- **SC-002**: **100 %** des textes sans ancre exploitable selon FR-002 conduisent, pour la **vue auxiliaire**, à un comportement explicite (message, absence de liste auxiliaire factice) **sans** interdire à tort une analyse LLM sur texte intégral non vide lorsque FR-012 s'applique.
- **SC-003**: **100 %** des tentatives de traitement aval **hors** FR-012 et FR-010 qui imposeraient encore une liste isolée non confirmée sont refusées conformément à FR-007 et FR-011.
- **SC-004**: Dans au moins **95 %** des sessions **hors périmètre FR-010** où un écran de proposition ou de validation **de la vue auxiliaire** est affiché, l'utilisatrice peut identifier l'état « prêt à confirmer » ou « bloqué avec message » en moins de **10 secondes** après l'affichage de la proposition ou du message d'échec. *(Périmètre clarifié session 2026-05-13 : le parcours FR-010, sans cet écran, est exclu de ce critère.)*
- **SC-005**: **100 %** des sessions satisfaisant FR-010 (OCR réussi, transcript admissible, parcours nominal) enchaînent vers l'analyse par modèle de langage **sans** affichage de l'écran de validation du texte segmenté comme prérequis, avec entrée modèle conforme à FR-012.
- **SC-006**: **100 %** des analyses par modèle de langage documentées dans le périmètre de cette spécification utilisent une entrée **identique au texte OCR sessionnel complet** après succès de reconnaissance (hors corrections utilisateur explicitement enregistrées comme « texte à analyser » si le produit les prévoit), sans troncature issue des seules règles FR-001 à FR-006.

## Assumptions

- Le parcours « photo prise → texte étiquette disponible » reste assuré par le domaine **capture-recognition** ; ce document ne redéfinit pas la reconnaissance elle-même. L'hypothèse « balise UI sur la session » pour FR-010 est **remplacée** par : le parcours nominal applique FR-010 pour tout OCR réussi admissible, conformément au contrat session-capture et à **Feature G** (`user-guidance-experience`).
- Les analyses par **modèle de langage** (composition, interprétation d'étiquette, ou équivalent aval) consomment le **texte OCR intégral** de la session (FR-012), et non une zone préalablement isolée par ancrage.
- **Ref.** : le domaine **ingredient-health-intelligence** reste en aval « Customer/Supplier » pour le **contenu métier** des rapports, mais l'**entrée** des analyses LLM est désormais le texte OCR intégral défini ici ; toute divergence documentaire dans ce domaine aval doit être alignée lors d'une revue transverse.
- Les règles FR-001 à FR-006 restent utiles **uniquement** pour des **vues auxiliaires** ou traçabilités non bloquantes pour l'entrée LLM ; si le produit supprime entièrement ces vues, ces exigences deviennent inactives sans remettre en cause FR-012.
- Les variantes « raisonnables » du mot-ancre excluent les correspondances purement partielles ambiguës sur d'autres mots ; les cas limites sont couverts par les jeux de test de SC-001.
- Hors scope pour cette spécification : autres langues que le français et l'anglais pour l'ancre auxiliaire (pas d'équivalent « Zutaten », « Ingredienti », etc., sauf évolution ultérieure explicite).
- La règle unique décrite ici pour la **proposition auxiliaire** remplace, pour toute nouvelle implémentation et documentation produit, les formulations historiques du domaine fondées sur une ancre « ingrédients: » prioritaire et une fin au premier saut de ligne systématique **lorsque cette proposition auxiliaire est encore produite**.
- Le point (`.`) est extrêmement fréquent dans les textes OCR d'étiquettes (codes additifs, abréviations, numéros de version) ; la condition « suivi d'un espace ou retour à la ligne » est nécessaire pour éviter les coupures prématurées de la **proposition auxiliaire**.
- **Ref.** : toute politique de **troncature ou de refus** lorsque le texte OCR excède une capacité d'entrée du modèle est portée par le domaine d'exécution du modèle, sous réserve qu'aucune règle d'ancrage métier ne soit appliquée **avant** la constitution de l'entrée (FR-014).
