# Feature Specification: homepage-llm-mock-trigger

**Feature Branch**: `016-home-llm-button`  
**Domain Context**: `user-guidance-experience`  
**Target Domain Folder**: `specs/domains/user-guidance-experience`  
**Created**: 2026-05-06  
**Status**: Draft  
**Input**: User description: "Le test bouchonné du pipeline LLM doit être lancé après clic sur un bouton sur la homepage, et doit afficher la réponse du LLM."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lancer le test depuis la homepage (Priority: P1)

En tant qu'utilisatrice, je veux cliquer sur un bouton dédié depuis la homepage pour lancer le test bouchonné du pipeline LLM local sans passer par le flux caméra/OCR.

**Why this priority**: Le bouton de déclenchement est le point d'entrée principal de la fonctionnalité et conditionne toute la valeur utilisateur.

**Independent Test**: Peut être testé en ouvrant la homepage et en déclenchant le test via le bouton, sans dépendance à d'autres parcours de l'application.

**Acceptance Scenarios**:

1. **Given** l'application est ouverte sur la homepage, **When** l'utilisatrice clique sur le bouton de test LLM, **Then** le processus de test bouchonné démarre immédiatement.
2. **Given** le bouton est disponible, **When** l'utilisatrice clique dessus, **Then** le système n'exige aucune capture photo ni saisie d'ingrédients manuelle.

---

### User Story 2 - Voir clairement la réponse LLM (Priority: P2)

En tant qu'utilisatrice, je veux voir la réponse du LLM affichée à l'écran à la fin du test pour confirmer visuellement que le pipeline fonctionne.

**Why this priority**: L'affichage de la réponse est la preuve fonctionnelle attendue du déclenchement.

**Independent Test**: Peut être testé en simulant une réponse LLM et en vérifiant qu'elle s'affiche de façon lisible sur l'écran ciblé.

**Acceptance Scenarios**:

1. **Given** le test est exécuté avec succès, **When** la réponse est reçue, **Then** la réponse LLM s'affiche intégralement dans une zone de résultat dédiée.
2. **Given** une réponse multi-lignes, **When** elle est affichée, **Then** le format reste lisible pour l'utilisatrice.

---

### User Story 3 - Comprendre les erreurs de lancement ou d'exécution (Priority: P3)

En tant qu'utilisatrice, je veux obtenir un message d'erreur clair si le test échoue pour savoir que la demande a été prise en compte mais non aboutie.

**Why this priority**: Sans feedback explicite, l'utilisatrice ne peut pas distinguer un échec d'une absence d'action.

**Independent Test**: Peut être testé en simulant un échec du pipeline et en vérifiant l'affichage d'un message d'erreur actionnable.

**Acceptance Scenarios**:

1. **Given** un échec pendant l'exécution du test, **When** l'échec est détecté, **Then** un message explicite s'affiche sur la homepage ou l'écran de résultat.

---

### Edge Cases

- Que se passe-t-il si l'utilisatrice clique plusieurs fois rapidement sur le bouton?
- Que se passe-t-il si la réponse LLM est vide ou indisponible?
- Que se passe-t-il si la homepage est quittée avant la fin de la réponse?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST exposer sur la homepage un bouton dédié au lancement du test bouchonné du pipeline LLM.
- **FR-002**: Le système MUST lancer le test immédiatement après le clic utilisateur sur ce bouton.
- **FR-003**: Le système MUST empêcher les déclenchements concurrents du même test pendant qu'une exécution est déjà en cours.
- **FR-004**: Le système MUST afficher la réponse LLM reçue dans une zone de résultat visible par l'utilisatrice.
- **FR-005**: Le système MUST afficher un état explicite pendant l'exécution (en cours, succès ou échec).
- **FR-006**: Le système MUST afficher un message d'erreur compréhensible si aucune réponse exploitable n'est obtenue.
- **FR-007**: Le système MUST permettre de relancer un nouveau test après la fin d'une exécution précédente.

### Key Entities *(include if feature involves data)*

- **HomepageLlmTestTrigger**: Représente l'action utilisateur de clic sur le bouton de lancement.
- **HomepageTestRunState**: Représente l'état courant de l'exécution (`idle`, `running`, `success`, `failure`).
- **HomepageLlmResponseView**: Représente le contenu de réponse affiché à l'utilisatrice.
- **HomepageLlmErrorView**: Représente le message d'échec affiché en cas d'erreur.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% des clics valides sur le bouton de test déclenchent une tentative d'exécution.
- **SC-002**: Dans au moins 95% des exécutions réussies, la réponse LLM est affichée en moins de 30 secondes après le clic.
- **SC-003**: 100% des exécutions en échec affichent un message d'erreur explicite en moins de 2 secondes après détection de l'échec.
- **SC-004**: 100% des clics répétés pendant une exécution active ne créent pas d'exécution concurrente supplémentaire.

## Assumptions

- La homepage est déjà le point d'entrée principal pour l'utilisatrice.
- Le test bouchonné utilise une entrée d'ingrédients mockée déjà définie dans le domaine d'analyse.
- La définition de "réponse exploitable" est portée par le domaine d'analyse et consommée par l'interface.
- Le flux s'exécute en local et ne dépend pas d'une connectivité réseau externe pour ce scénario.
