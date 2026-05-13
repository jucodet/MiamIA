# Feature Specification: test-llm-mock-ingredients

**Feature Branch**: `016-test-llm-mock`  
**Domain Context**: `CompositionAnalysisContext`  
**Target Domain Folder**: `specs/domains/ingredient-health-intelligence`  
**Created**: 2026-05-06  
**Status**: Draft  
**Input**: User description: "Créer un test bouchonné ne mettant à l'épreuve que le processus de demander au LLM local l'analyse d'une liste d'ingrédients mockée."

## Clarifications

### Session 2026-05-06

- Q: Quel est le critère exact de "réponse exploitable" ? → A: Succès si la réponse est non vide et classée comme analysable par le parseur de test (format souple contrôlé).
- Q: Quelle politique de timeout doit appliquer le test ? → A: Échec automatique si aucune réponse exploitable n'est obtenue dans une fenêtre de 30 secondes (timeout strict).
- Q: Quelles catégories d'échec doivent être tracées ? → A: Tracer exactement 3 catégories: timeout, runtime-unavailable, non-analysable-response.
- Q: Quelle règle d'égalité doit s'appliquer à l'entrée mockée ? → A: Égalité stricte caractère par caractère entre la chaîne mockée et la charge envoyée.
- Q: Quelle politique de validation projet doit s'appliquer à ce test ? → A: Le test est manuel uniquement, hors validation régulière.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Valider le flux d'appel LLM local (Priority: P1)

En tant que développeuse, je veux lancer un test bouchonné avec une liste d'ingrédients mockée fixe afin de vérifier que le processus d'envoi vers le LLM local et de réception de réponse fonctionne de bout en bout, indépendamment de l'OCR et de la capture caméra.

**Why this priority**: Ce flux est le coeur de la valeur d'analyse; s'il est instable, les autres couches n'apportent pas de valeur fiable.

**Independent Test**: Peut être validé en exécutant uniquement le test bouchonné avec la chaîne d'entrée figée, puis en vérifiant qu'une réponse d'analyse est renvoyée sans dépendre de la caméra ou de l'OCR.

**Acceptance Scenarios**:

1. **Given** une entrée mockée exactement égale à la liste d'ingrédients de référence, **When** le processus d'analyse est déclenché, **Then** la requête est transmise au moteur LLM local et une réponse exploitable est renvoyée.
2. **Given** le test bouchonné isolé des autres modules, **When** le test s'exécute, **Then** aucune dépendance à la capture caméra, à l'OCR ou à une entrée utilisateur interactive n'est requise.

---

### User Story 2 - Garantir l'intégrité de l'entrée analysée (Priority: P2)

En tant que développeuse, je veux que la chaîne d'ingrédients mockée soit transmise telle quelle pour confirmer que l'analyse produite correspond exactement au texte source attendu.

**Why this priority**: L'analyse perd sa crédibilité si l'entrée transmise diffère du texte de référence du test.

**Independent Test**: Peut être validé en comparant la charge utile effectivement analysée avec la chaîne mockée définie dans le scénario.

**Acceptance Scenarios**:

1. **Given** la chaîne mockée de référence, **When** le processus construit la demande d'analyse, **Then** la charge utile conserve exactement le même contenu textuel.
2. **Given** une analyse terminée, **When** le résultat est journalisé dans le contexte de test, **Then** la trace associe explicitement le résultat à la chaîne d'entrée mockée.

---

### User Story 3 - Rendre les échecs explicites dans le test (Priority: P3)

En tant que développeuse, je veux un comportement d'échec lisible pour distinguer un problème de runtime local d'un problème de logique du test.

**Why this priority**: Des erreurs ambiguës ralentissent le diagnostic et la stabilisation de la fonctionnalité.

**Independent Test**: Peut être validé en simulant l'absence de réponse du runtime local et en vérifiant qu'un résultat d'échec explicite est produit.

**Acceptance Scenarios**:

1. **Given** un runtime local indisponible pendant le test, **When** la demande d'analyse est lancée, **Then** le test renvoie un état d'échec explicite et actionnable.

---

### Edge Cases

- Que se passe-t-il si la chaîne mockée est vide ou ne contient pas d'ingrédients exploitables?
- Comment le flux réagit-il si le runtime local répond avec un contenu non interprétable?
- Que se passe-t-il si la réponse dépasse le délai attendu pour le scénario de test?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST exécuter un test bouchonné dédié au flux d'analyse LLM local sans dépendre de la capture caméra ni de l'OCR.
- **FR-002**: Le système MUST utiliser comme entrée de test unique la chaîne mockée suivante:
  `Ingredients. Sucre, farine de BLÉ 33 %, farine complète de BLÉ 15 %, huile de palme, huile de colza, amidon de BLÉ, sirop de glucose, poudres à lever (carbonates d'ammonium, carbonates de sodium), émulsifiant (lécithines de SOJA), sel, LAIT écrémé en poudre, LAIT entier en poudre, arômes.`
- **FR-003**: Le système MUST transmettre l'entrée mockée sans altération de contenu au processus d'analyse.
- **FR-004**: Le système MUST retourner un résultat indiquant clairement soit une analyse reçue, soit un échec explicite.
- **FR-007**: Le système MUST considérer une réponse comme exploitable uniquement si elle est non vide et classée analysable par le parseur de test.
- **FR-005**: Le système MUST associer chaque résultat du test à l'entrée mockée utilisée pour assurer la traçabilité.
- **FR-006**: Le système MUST permettre l'exécution répétable du même scénario avec le même jeu de données et des attentes identiques.
- **FR-008**: Le système MUST échouer automatiquement le test si aucune réponse exploitable n'est obtenue dans une fenêtre de 30 secondes.
- **FR-009**: Le système MUST classifier chaque échec dans l'une des catégories suivantes: `timeout`, `runtime-unavailable`, `non-analysable-response`.
- **FR-010**: Le système MUST vérifier une égalité stricte caractère par caractère entre `MockIngredientInput` et la charge textuelle transmise à l'analyse.
- **FR-011**: Le système MUST être exécutable manuellement pour validation ciblée et ne fait pas partie des contrôles bloquants réguliers de validation feature.

### Key Entities *(include if feature involves data)*

- **MockIngredientInput**: Représente la chaîne d'ingrédients de référence utilisée par le test bouchonné.
- **LlmAnalysisRequest**: Représente la demande d'analyse générée à partir de `MockIngredientInput` pour le runtime local.
- **LlmAnalysisOutcome**: Représente le résultat observable du test (succès avec contenu d'analyse, ou échec explicite avec raison).
- **TestTraceRecord**: Représente le lien de traçabilité entre l'entrée mockée, la demande envoyée et le résultat obtenu.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% des exécutions du test bouchonné utilisent exactement la chaîne mockée de référence comme entrée d'analyse.
- **SC-002**: En environnement local prêt, au moins 95% des exécutions produisent une réponse d'analyse exploitable en moins de 30 secondes.
- **SC-003**: 100% des échecs d'exécution retournent un état d'erreur explicite permettant d'identifier la catégorie de panne.
- **SC-004**: Le scénario de test est reproductible à l'identique sur au moins 3 exécutions successives sans variation des critères d'acceptation.
- **SC-005**: 100% des résultats marqués "succès" correspondent à une réponse non vide classée analysable par le parseur de test.
- **SC-006**: 100% des exécutions dépassant 30 secondes sans réponse exploitable sont marquées en échec timeout.
- **SC-007**: 100% des exécutions en échec sont étiquetées dans l'une des 3 catégories définies, sans catégorie hors-liste.
- **SC-008**: 100% des requêtes d'analyse du test conservent strictement le texte source (égalité caractère par caractère validée).

## Assumptions

- Le runtime LLM local est déjà installé et utilisable dans l'environnement de développement ciblé.
- Le test bouchonné vise uniquement le flux d'appel et de réponse, pas la qualité nutritionnelle intrinsèque de la réponse.
- La chaîne mockée fournie est considérée comme source de vérité pour ce scénario.
- Le contexte de test dispose d'un mécanisme de trace minimal pour relier entrée et résultat.
