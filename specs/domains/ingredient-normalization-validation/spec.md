# Feature Specification: ingredient-phrase-segment

**Feature Branch**: `017-ocr-dot-end-capture`  
**Domain Context**: `ingredient-normalization-validation`  
**Target Domain Folder**: `specs/domains/ingredient-normalization-validation`  
**Created**: 2026-05-06  
**Status**: Draft  
**Input**: User description: "Modifier la capture OCR pour prendre en compte le caractère '.' suivi d'un espace ou d'un retour à la ligne comme fin de capture de la liste d'ingrédients. Un point non suivi d'un espace ou retour à la ligne (ex. abréviations, codes additifs) ne constitue pas une fin de capture."

## Clarifications

### Session 2026-05-06

- Q: Faut-il reconnaître « Ingredients » (anglais, casse tolérée) comme ancre équivalente à « Ingrédient(s) » pour la première occurrence (FR-002) ? → A: Oui (option A) — « Ingredients » est traité comme équivalent sémantique de l'ancre française, avec les mêmes tolérances de casse et de mise en forme que pour « Ingrédient(s) ».
- Q: Pour l'anglais, l'ancre doit-elle inclure le singulier « Ingredient » en plus du pluriel « Ingredients » ? → A: Oui (option A) — les formes anglaises singulier et pluriel sont toutes deux reconnues, avec la même tolérance de casse.

### Session 2026-05-11

- Évolution FR-003 : le caractère `.` n'est reconnu comme fin de capture de la liste d'ingrédients que lorsqu'il est **suivi d'un espace ou d'un retour à la ligne**. Un point non suivi d'un de ces deux caractères (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne déclenche pas la fin de capture.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Proposer une liste d'ingrédients à partir du texte reconnu (Priority: P1)

En tant qu'utilisatrice, après avoir pris une photo d'étiquette et obtenu le texte reconnu associé, je veux que l'application me propose automatiquement la liste d'ingrédients délimitée de façon prévisible selon la mention d'ingrédients sur l'étiquette (français **ou** équivalent anglais reconnu).

**Why this priority**: Sans proposition fiable et déterministe, la validation et les analyses aval n'ont pas de base commune.

**Independent Test**: À partir d'échantillons de texte représentatifs (ancre française, **ancres anglaises « Ingredient » et « Ingredients »**, plusieurs mentions, avec ou sans ponctuation de fin de phrase, **avec points internes non suivis d'espace**), vérifier que la zone proposée pour la liste respecte exactement la règle d'ancrage et de fin décrite dans les exigences.

**Acceptance Scenarios**:

1. **Given** un texte contenant une phrase qui commence par une forme reconnue du mot-ancre (« Ingrédient », « Ingrédients », « Ingredient » ou « Ingredients ») avec des variations raisonnables de casse et de mise en forme, **When** le système prépare la proposition de liste, **Then** la proposition commence au début de cette phrase et se termine à la fin de phrase si un point suivi d'un espace ou d'un retour à la ligne (ou un `!` / `?` de fin de phrase) est présent dans cette phrase.
2. **Given** une phrase-ancre contenant des points internes non suivis d'un espace ou d'un retour à la ligne (ex. « E.621 », « vit.B12 »), **When** le système délimite la proposition, **Then** ces points internes ne déclenchent pas la fin de capture et la liste continue au-delà.
3. **Given** une phrase-ancre sans ponctuation de fin de phrase reconnue dans le texte, **When** le système délimite la proposition, **Then** la proposition se termine à la fin de la ligne où s'étend cette phrase (premier saut de ligne après le début de la phrase-ancre).
4. **Given** une phrase-ancre sans ponctuation de fin de phrase ni saut de ligne applicable, **When** le système délimite la proposition, **Then** la proposition se termine à la fin du fragment de texte continu pertinent sur la même plage (fin du texte fourni pour ce cas).
5. **Given** plusieurs occurrences d'une mention « ingrédients » dans le texte, **When** le système choisit l'ancre, **Then** seule la première occurrence rencontrée en lisant le texte de l'étiquette dans l'ordre naturel sert à la proposition.
6. **Given** un texte dont la phrase-ancre commence par « Ingredients » (forme anglaise, avec variations raisonnables de casse), **When** le système prépare la proposition, **Then** les mêmes règles de délimitation (fin de phrase par `.` + espace/retour à la ligne, fin de ligne, fin de fragment) s'appliquent comme pour une ancre française.

---

### User Story 2 - Confirmer ou corriger avant analyse (Priority: P2)

En tant qu'utilisatrice, je veux voir la proposition de liste isolée et confirmer (ou corriger) avant toute analyse santé ou composition, afin de garantir que la chaîne analysée correspond à mon étiquette.

**Why this priority**: La confirmation protège les analyses contre les erreurs de reconnaissance ou de délimitation.

**Independent Test**: Vérifier qu'aucune analyse aval n'est lancée sans action de confirmation explicite après affichage de la proposition.

**Acceptance Scenarios**:

1. **Given** une proposition de liste affichée, **When** l'utilisatrice confirme explicitement, **Then** le segment validé devient la référence pour les étapes suivantes.
2. **Given** une proposition de liste affichée, **When** l'utilisatrice corrige le texte puis confirme, **Then** le segment validé reflète exactement les corrections saisies.

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
- Texte très bruité ou lignes fusionnées : la borne « fin de ligne » dépend des sauts de ligne présents dans le texte fourni.
- Ancre présente mais liste tronquée hors champ photo : la proposition peut être incomplète ; le système doit pouvoir signaler l'incertitude sans inventer de contenu (comportement aligné sur les parcours existants de reprise).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST appliquer une règle unique d'isolation de la liste d'ingrédients sur le texte issu de l'étiquette ; toute règle antérieure fondée sur une autre forme d'ancre prioritaire ou sur une fin de segment systématiquement au premier saut de ligne sans tenir compte de la phrase « Ingrédient(s) » est **abrogée** et ne doit plus être utilisée pour la proposition automatique.
- **FR-002**: Le système MUST identifier comme point de départ la **première** occurrence, dans l'ordre de lecture du texte, d'une phrase qui commence par un libellé équivalent à « Ingrédient » ou « Ingrédients » **ou**, à équivalence sémantique pour les étiquettes bilingues, par « Ingredient » ou « Ingredients » (anglais), en tolérant des variations raisonnables de casse, d'accents (y compris absence d'accent sur formes attendues), d'espaces et de ponctuation immédiate (par exemple deux-points) après le mot-ancre.
- **FR-003**: Le système MUST délimiter la fin de la proposition au premier caractère `.` (point) **suivi d'un espace ou d'un retour à la ligne** rencontré après l'ancre dans la même phrase. Un point non suivi d'un espace ou d'un retour à la ligne (ex. codes additifs « E.621 », abréviations « vit.B12 ») ne MUST NOT être considéré comme fin de capture. Les caractères `!` et `?` restent reconnus comme fin de phrase standard sans condition contextuelle supplémentaire.
- **FR-004**: Lorsqu'aucune ponctuation de fin de phrase n'est présente dans cette phrase (ni `.` + espace/retour à la ligne, ni `!`, ni `?`), le système MUST délimiter la fin de la proposition au **saut de ligne** qui termine la ligne sur laquelle s'étend la phrase-ancre (fin de ligne).
- **FR-005**: Lorsqu'il n'existe ni ponctuation de fin de phrase applicable ni saut de ligne applicable après l'ancre, le système MUST délimiter la fin de la proposition à la **fin du texte disponible** pour ce fragment continu.
- **FR-006**: Lorsque plusieurs occurrences d'une ancre reconnue (française ou anglaise selon FR-002) existent, le système MUST **ignorer** toute occurrence autre que la première pour construire la proposition automatique.
- **FR-007**: Le système MUST afficher la proposition isolée à l'utilisatrice et exiger une **confirmation explicite** avant de lancer une analyse santé ou composition qui repose sur cette liste.
- **FR-008**: Le système MUST refuser de lancer une analyse aval sur une liste non confirmée ou lorsque l'ancre est absente, la proposition est vide ou manifestement inexploitable comme liste, avec un message compréhensible et une voie de reprise (nouvelle capture ou correction selon le parcours existant).
- **FR-009**: Le système MUST conserver la traçabilité entre le texte brut fourni pour l'étiquette, la proposition isolée et le segment validé après confirmation.

### Key Entities *(include if feature involves data)*

- **Texte étiquette (entrée)**: Texte brut associé à une capture d'étiquette, tel que fourni au domaine par le parcours de reconnaissance amont.
- **Proposition de segment ingrédients**: Portion du texte étiquette délimitée par la règle d'ancrage et de fin, avant validation utilisateur.
- **Segment ingrédients validé**: Chaîne confirmée (éventuellement corrigée) par l'utilisatrice, seule base autorisée pour les analyses aval.
- **État d'isolation**: Représente le résultat du traitement (proposition prête, impossible sans ancre, proposition vide, etc.).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Pour un jeu de cas de test documenté couvrant ancre unique, ancres multiples, **au moins un cas avec ancre « Ingredient » et un cas avec ancre « Ingredients »**, combinaisons ponctuation / saut de ligne / texte monoligne, **et au moins un cas avec point interne non suivi d'espace (ex. « E.621 ») qui ne doit pas déclencher la fin de capture**, **100 %** des propositions automatiques respectent les règles FR-002 à FR-006 (vérification par contrôle manuel ou jeu de référence agréé).
- **SC-002**: **100 %** des textes sans ancre exploitable selon FR-002 conduisent à un comportement décrit par FR-008 (pas de liste présentée comme définitive sans action corrective).
- **SC-003**: **100 %** des tentatives d'analyse aval sans confirmation explicite du segment sont refusées (FR-007, FR-008).
- **SC-004**: Dans au moins **95 %** des sessions où une proposition est affichée, l'utilisatrice peut identifier l'état « prêt à confirmer » ou « bloqué avec message » en moins de **10 secondes** après l'affichage de la proposition ou du message d'échec.

## Assumptions

- Le parcours « photo prise → texte étiquette disponible » reste assuré par le domaine **capture-recognition** ; ce document ne redéfinit pas la reconnaissance elle-même, seulement l'isolation métier sur le texte fourni.
- Les analyses santé et composition aval consomment uniquement le **segment validé**, pas le texte brut complet, une fois la confirmation obtenue.
- Les variantes « raisonnables » du mot-ancre excluent les correspondances purement partielles ambiguës sur d'autres mots ; les cas limites sont couverts par les jeux de test de SC-001.
- Hors scope pour cette spécification : autres langues que le français et l'anglais pour l'ancre (pas d'équivalent « Zutaten », « Ingredienti », etc., sauf évolution ultérieure explicite).
- La règle unique décrite ici remplace, pour toute nouvelle implémentation et documentation produit, les formulations historiques du domaine fondées sur une ancre « ingrédients: » prioritaire et une fin au premier saut de ligne systématique.
- Le point (`.`) est extrêmement fréquent dans les textes OCR d'étiquettes (codes additifs, abréviations, numéros de version) ; la condition « suivi d'un espace ou retour à la ligne » est nécessaire pour éviter les coupures prématurées de la liste.
