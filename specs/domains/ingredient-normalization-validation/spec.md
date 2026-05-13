# Feature Specification: ingredient-phrase-segment

**Feature Branch**: `021-auto-analyze-ingredients-tag` *(évolution 2026-05-13 ; règles d’ancrage issues de `017-ocr-dot-end-capture` inchangées)*  
**Domain Context**: `ingredient-normalization-validation`  
**Target Domain Folder**: `specs/domains/ingredient-normalization-validation`  
**Created**: 2026-05-06  
**Status**: Draft — *Backfill P12 applied 2026-05-12*  
**Input**: User description: "Modifier la capture OCR pour prendre en compte le caractère '.' suivi d'un espace ou d'un retour à la ligne comme fin de capture de la liste d'ingrédients. Un point non suivi d'un espace ou retour à la ligne (ex. abréviations, codes additifs) ne constitue pas une fin de capture." — *Évolution 2026-05-13* : « Après un OCR réussi, l'analyse est déclenchée immédiatement si un texte a été capturé avec la balise ingrédients, sans passer par l'écran de validation du texte segmenté. »

## Clarifications

### Session 2026-05-06

- Q: Faut-il reconnaître « Ingredients » (anglais, casse tolérée) comme ancre équivalente à « Ingrédient(s) » pour la première occurrence (FR-002) ? → A: Oui (option A) — « Ingredients » est traité comme équivalent sémantique de l'ancre française, avec les mêmes tolérances de casse et de mise en forme que pour « Ingrédient(s) ».
- Q: Pour l'anglais, l'ancre doit-elle inclure le singulier « Ingredient » en plus du pluriel « Ingredients » ? → A: Oui (option A) — les formes anglaises singulier et pluriel sont toutes deux reconnues, avec la même tolérance de casse.

### Session 2026-05-11

- Évolution FR-003 : le caractère `.` n'est reconnu comme fin de capture de la liste d'ingrédients que lorsqu'il est **suivi d'un espace ou d'un retour à la ligne**. Un point non suivi d'un de ces deux caractères (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne déclenche pas la fin de capture.

### Session 2026-05-13 — Enchaînement analyse sans écran de validation (balise ingrédients)

- Lorsque la session de capture est explicitement associée à la **balise (ou mode) « ingrédients »** fournie par le parcours amont, que l'OCR se termine avec **succès** et qu'une **proposition de segment exploitable** est disponible selon les mêmes critères que pour une validation manuelle, le segment proposé est **considéré comme validé implicitement** pour les analyses aval et l'application **ne doit pas** afficher l'écran de validation du texte segmenté avant de lancer l'analyse.
- Tout autre parcours de capture conserve l'exigence de **confirmation explicite** (alignement historique FR-007).
- Q: Le critère **SC-004** (compréhension en moins de 10 secondes) doit-il inclure le parcours FR-010 où l'écran de proposition / validation n'est pas affiché ? → A: Option **A** — SC-004 ne s'applique qu'aux parcours où l'écran de proposition ou de validation est affiché (**hors** périmètre FR-010).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Proposer une liste d'ingrédients à partir du texte reconnu (Priority: P1)

En tant qu'utilisatrice, après avoir pris une photo d'étiquette et obtenu le texte reconnu associé, je veux que l'application me propose automatiquement la liste d'ingrédients délimitée de façon prévisible selon la mention d'ingrédients sur l'étiquette (français **ou** équivalent anglais reconnu).

**Why this priority**: Sans proposition fiable et déterministe, la validation et les analyses aval n'ont pas de base commune.

**Independent Test**: À partir d'échantillons de texte représentatifs (ancre française, **ancres anglaises « Ingredient » et « Ingredients »**, plusieurs mentions, avec ou sans ponctuation de fin de phrase, **avec points internes non suivis d'espace**), vérifier que la zone proposée pour la liste respecte exactement la règle d'ancrage et de fin décrite dans les exigences.

**Acceptance Scenarios**:

1. **Given** un texte contenant une phrase qui commence par une forme reconnue du mot-ancre (« Ingrédient », « Ingrédients », « Ingredient » ou « Ingredients ») avec des variations raisonnables de casse et de mise en forme, **When** le système prépare la proposition de liste, **Then** la proposition commence au début de cette phrase et se termine à la fin de phrase si un point suivi d'un espace ou d'un retour à la ligne (ou un `!` / `?` de fin de phrase) est présent dans cette phrase.
2. **Given** une phrase-ancre contenant des points internes non suivis d'un espace ou d'un retour à la ligne (ex. « E.621 », « vit.B12 »), **When** le système délimite la proposition, **Then** ces points internes ne déclenchent pas la fin de capture et la liste continue au-delà.
3. **Given** un texte multi-lignes sans terminateur de phrase (`.` + espace/newline, `!`, `?`) après l'ancre, **When** le système délimite la proposition, **Then** la capture traverse les sauts de ligne et se termine à la fin du texte disponible.
4. **Given** un texte monoligne sans terminateur de phrase, **When** le système délimite la proposition, **Then** la proposition se termine à la fin du texte fourni.
5. **Given** plusieurs occurrences d'une mention « ingrédients » dans le texte, **When** le système choisit l'ancre, **Then** seule la première occurrence rencontrée en lisant le texte de l'étiquette dans l'ordre naturel sert à la proposition.
6. **Given** un texte dont la phrase-ancre commence par « Ingredients » (forme anglaise, avec variations raisonnables de casse), **When** le système prépare la proposition, **Then** les mêmes règles de délimitation (`.` + espace/retour à la ligne, `!`, `?`, ou fin de texte) s'appliquent comme pour une ancre française.

---

### User Story 2 - Confirmer ou corriger avant analyse (Priority: P2)

En tant qu'utilisatrice, je veux voir la proposition de liste isolée et confirmer (ou corriger) avant toute analyse santé ou composition, afin de garantir que la chaîne analysée correspond à mon étiquette.

**Why this priority**: La confirmation protège les analyses contre les erreurs de reconnaissance ou de délimitation.

**Independent Test**: Vérifier qu'aucune analyse aval n'est lancée sans confirmation explicite **lorsque FR-010 ne s'applique pas** ; pour FR-010, vérifier l'absence d'écran de validation et l'analyse sur la proposition automatique.

**Acceptance Scenarios**:

1. **Given** une proposition de liste affichée, **When** l'utilisatrice confirme explicitement, **Then** le segment validé devient la référence pour les étapes suivantes.
2. **Given** une proposition de liste affichée, **When** l'utilisatrice corrige le texte puis confirme, **Then** le segment validé reflète exactement les corrections saisies.
3. **Given** un parcours **sans** balise « ingrédients » (ou équivalent produit), **When** une proposition est prête, **Then** l'écran de validation reste obligatoire avant toute analyse aval.

---

### User Story 2b - Parcours accéléré balise ingrédients (Priority: P1)

En tant qu'utilisatrice, lorsque j'ai choisi la capture avec la balise « ingrédients » et que la reconnaissance de texte réussit avec une liste exploitable, je veux que l'analyse (composition / santé selon le parcours produit) démarre **tout de suite**, sans étape intermédiaire où je dois valider le texte segmenté, afin de gagner du temps lorsque la cible de capture est déjà la liste d'ingrédients.

**Why this priority**: Réduit la friction sur le parcours le plus intentionnel ; la balise signale déjà l'intention « liste d'ingrédients ».

**Independent Test**: Simuler une session balise « ingrédients », OCR réussi, segment non vide conforme aux règles d'ancrage ; vérifier absence d'écran de validation et lancement d'analyse sur le segment proposé tel quel.

**Acceptance Scenarios**:

1. **Given** une session de capture associée à la balise « ingrédients », **When** l'OCR se termine avec succès et une proposition de segment exploitable est disponible, **Then** le système enchaîne vers l'analyse aval **sans** afficher l'écran de validation du texte segmenté.
2. **Given** le même contexte, **When** l'analyse démarre, **Then** le texte analysé est exactement la proposition automatique issue des règles FR-002 à FR-006 (aucune saisie utilisateur intermédiaire).

---

### User Story 3 - Comprendre l'absence de liste exploitable (Priority: P3)

En tant qu'utilisatrice, si le texte ne permet pas d'appliquer la règle d'ancrage, je veux un message clair et la possibilité de reprendre (nouvelle capture ou correction), sans liste inventée.

**Why this priority**: Évite la frustration et les analyses fondées sur du contenu arbitraire.

**Independent Test**: Fournir des textes sans ancre exploitable et vérifier message explicite et absence de liste factice.

**Acceptance Scenarios**:

1. **Given** un texte sans occurrence exploitable de « Ingrédient(s) » selon la règle d'ancrage, **When** le système tente l'isolation, **Then** aucune liste n'est présentée comme valide sans intervention et un message explicite l'indique.
2. **Given** une ancre présente mais une proposition vide ou non lisible comme liste, **When** l'utilisatrice tente de continuer, **Then** le système bloque l'analyse aval et propose une reprise ou une correction.

---

### Edge Cases

- Plusieurs langues sur la même étiquette : la règle s'applique à la **première** occurrence d'ancre reconnue (français « Ingrédient(s) » **ou** anglais « Ingredients ») dans l'ordre de lecture du texte fourni ; l'autre langue n'est utilisée que si elle apparaît en premier.
- Étiquette uniquement en anglais : une phrase commençant par « Ingredients » déclenche la même logique d'isolation qu'une ancre française.
- **Point interne (`.` non suivi d'espace ou retour à la ligne)** : ne déclenche pas la fin de capture. Exemples courants : codes additifs (« E.621 »), abréviations (« vit.B12 »), numéros de lot (« L.12345 »). La capture continue jusqu'au prochain marqueur de fin valide.
- **Point suivi d'un espace ou retour à la ligne** : reconnu comme fin de la liste d'ingrédients. Couvre le point final classique d'une phrase « Ingrédients: eau, sel, sucre. » ainsi que les cas où le point clôt un bloc suivi d'un paragraphe distinct.
- Ponctuation `!` et `?` : conservent leur rôle de fin de phrase standard ; ces signes ne nécessitent pas de condition contextuelle supplémentaire (espace/retour à la ligne) car ils sont sans ambiguïté dans le contexte des étiquettes alimentaires.
- Texte multi-lignes sans terminateur : la capture traverse les sauts de ligne et inclut tout le texte restant jusqu'au terminateur ou à la fin du texte. L'utilisatrice peut corriger lors de la confirmation (US2) **sauf** sur le parcours balise « ingrédients » réussi où la correction intermédiaire n'est pas proposée (US2b) ; les voies de reprise ou correction après analyse restent celles du parcours produit existant.
- Ancre présente mais liste tronquée hors champ photo : la proposition peut être incomplète ; le système doit pouvoir signaler l'incertitude sans inventer de contenu (comportement aligné sur les parcours existants de reprise).
- **Balise « ingrédients » mais OCR en échec ou texte vide** : aucun enchaînement automatique vers l'analyse ; messages et reprises alignés sur FR-008.
- **Balise « ingrédients » mais proposition vide ou inexploitable** : le système MUST appliquer FR-008 (pas d'analyse silencieuse sur contenu arbitraire), même sans écran de validation intermédiaire.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST appliquer une règle unique d'isolation de la liste d'ingrédients sur le texte issu de l'étiquette ; toute règle antérieure fondée sur une autre forme d'ancre prioritaire ou sur une fin de segment systématiquement au premier saut de ligne sans tenir compte de la phrase « Ingrédient(s) » est **abrogée** et ne doit plus être utilisée pour la proposition automatique.
- **FR-002**: Le système MUST identifier comme point de départ la **première** occurrence, dans l'ordre de lecture du texte, d'une phrase qui commence par un libellé équivalent à « Ingrédient » ou « Ingrédients » **ou**, à équivalence sémantique pour les étiquettes bilingues, par « Ingredient » ou « Ingredients » (anglais), en tolérant des variations raisonnables de casse, d'accents (y compris absence d'accent sur formes attendues), d'espaces et de ponctuation immédiate (par exemple deux-points) après le mot-ancre.
- **FR-003**: Le système MUST délimiter la fin de la proposition au premier caractère `.` (point) **suivi d'un espace ou d'un retour à la ligne** rencontré après l'ancre **dans tout le texte disponible** (la recherche traverse les sauts de ligne). Un point non suivi d'un espace ou d'un retour à la ligne (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne MUST NOT être considéré comme fin de capture. Un `.` en dernière position du texte (fin absolue) est reconnu comme terminateur. Les caractères `!` et `?` restent reconnus comme fin de phrase standard sans condition contextuelle supplémentaire.
- **FR-004**: ~~Abrogé~~ — le saut de ligne seul ne constitue plus un point d'arrêt de la capture. La liste d'ingrédients peut s'étendre sur plusieurs lignes.
- **FR-005**: Lorsqu'aucun terminateur de phrase (`.` + espace/retour à la ligne, `!`, `?`) n'est trouvé dans tout le texte après l'ancre, le système MUST délimiter la fin de la proposition à la **fin du texte disponible**.
- **FR-006**: Lorsque plusieurs occurrences d'une ancre reconnue (française ou anglaise selon FR-002) existent, le système MUST **ignorer** toute occurrence autre que la première pour construire la proposition automatique.
- **FR-007**: Le système MUST afficher la proposition isolée à l'utilisatrice et exiger une **confirmation explicite** avant de lancer une analyse santé ou composition qui repose sur cette liste, **sauf** lorsque les conditions de **FR-010** sont réunies (parcours balise « ingrédients », OCR réussi, proposition exploitable).
- **FR-008**: Le système MUST refuser de lancer une analyse aval sur une liste non confirmée ou lorsque l'ancre est absente, la proposition est vide ou manifestement inexploitable comme liste, avec un message compréhensible et une voie de reprise (nouvelle capture ou correction selon le parcours existant).
- **FR-009**: Le système MUST conserver, au minimum en mémoire pendant la session active, la traçabilité entre le texte brut fourni pour l’étiquette (`scanId`), la proposition isolée et le segment validé (confirmation explicite ou implicite selon FR-010). La persistance au-delà de la session est une évolution souhaitable mais non bloquante pour le MVP. *(Backfill P12 — 2026-05-12 : traçabilité mémoire via scanId suffisante pour le MVP ; persistance Room reportée.)*
- **FR-010**: Lorsque la session de capture est associée à la **balise (ou mode produit) « ingrédients »** telle que publiée par le parcours amont, que l'OCR se termine par un **succès** et qu'une proposition de segment **exploitable** est obtenue (non vide, conforme aux critères d'exploitabilité déjà visés par FR-008 pour une liste), le système MUST **enchaîner immédiatement** vers l'analyse aval en considérant cette proposition comme **segment validé implicite**, **sans** afficher l'écran de validation du texte segmenté.
- **FR-011**: Lorsque la session **n'est pas** couverte par FR-010, le système MUST conserver l'exigence de confirmation explicite de FR-007 (aucune analyse aval sur liste non confirmée).

### Key Entities *(include if feature involves data)*

- **Texte étiquette (entrée)**: Texte brut associé à une capture d'étiquette, tel que fourni au domaine par le parcours de reconnaissance amont.
- **Proposition de segment ingrédients**: Portion du texte étiquette délimitée par la règle d'ancrage et de fin, avant validation explicite par l'utilisatrice ou acceptation implicite (FR-010).
- **Segment ingrédients validé**: Chaîne confirmée explicitement par l'utilisatrice **ou** acceptée implicitement selon FR-010 après proposition automatique ; seule base autorisée pour les analyses aval.
- **État d'isolation**: Représente le résultat du traitement (proposition prête, impossible sans ancre, proposition vide, etc.).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Pour un jeu de cas de test documenté couvrant ancre unique, ancres multiples, **au moins un cas avec ancre « Ingredient » et un cas avec ancre « Ingredients »**, combinaisons ponctuation / saut de ligne / texte monoligne, **et au moins un cas avec point interne non suivi d'espace (ex. « E.621 ») qui ne doit pas déclencher la fin de capture**, **100 %** des propositions automatiques respectent les règles FR-002 à FR-006 (vérification par contrôle manuel ou jeu de référence agréé).
- **SC-002**: **100 %** des textes sans ancre exploitable selon FR-002 conduisent à un comportement décrit par FR-008 (pas de liste présentée comme définitive sans action corrective).
- **SC-003**: **100 %** des tentatives d'analyse aval **hors** périmètre FR-010 sans confirmation explicite du segment sont refusées (FR-007, FR-008, FR-011).
- **SC-004**: Dans au moins **95 %** des sessions **hors périmètre FR-010** où l'écran de proposition ou de validation est affiché, l'utilisatrice peut identifier l'état « prêt à confirmer » ou « bloqué avec message » en moins de **10 secondes** après l'affichage de la proposition ou du message d'échec. *(Périmètre clarifié session 2026-05-13 : le parcours FR-010, sans cet écran, est exclu de ce critère.)*
- **SC-005**: **100 %** des sessions satisfaisant FR-010 enchaînent vers l'analyse aval **sans** affichage de l'écran de validation du texte segmenté, sur la base du segment proposé automatiquement.

## Assumptions

- Le parcours « photo prise → texte étiquette disponible » reste assuré par le domaine **capture-recognition** ; ce document ne redéfinit pas la reconnaissance elle-même, seulement l'isolation métier sur le texte fourni. La présence de la **balise « ingrédients »** sur la session est supposée être signalée de façon non ambiguë par ce parcours amont (contrat UX / anti-corruption inchangé en substance).
- Les analyses santé et composition aval consomment uniquement le **segment validé** (explicite ou implicite selon FR-010), pas le texte brut complet.
- **Ref.** : le domaine **ingredient-health-intelligence** reste en aval « Customer/Supplier » : il consomme un segment déjà validé au sens du présent document ; FR-010 ne redéfinit pas le contenu métier d'une analyse.
- Les variantes « raisonnables » du mot-ancre excluent les correspondances purement partielles ambiguës sur d'autres mots ; les cas limites sont couverts par les jeux de test de SC-001.
- Hors scope pour cette spécification : autres langues que le français et l'anglais pour l'ancre (pas d'équivalent « Zutaten », « Ingredienti », etc., sauf évolution ultérieure explicite).
- La règle unique décrite ici remplace, pour toute nouvelle implémentation et documentation produit, les formulations historiques du domaine fondées sur une ancre « ingrédients: » prioritaire et une fin au premier saut de ligne systématique.
- Le point (`.`) est extrêmement fréquent dans les textes OCR d'étiquettes (codes additifs, abréviations, numéros de version) ; la condition « suivi d'un espace ou retour à la ligne » est nécessaire pour éviter les coupures prématurées de la liste.
