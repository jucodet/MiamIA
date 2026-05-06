# Feature Specification: home-camera-llm-panel

**Feature Branch**: `016-home-camera-llm-panel`  
**Domain Context**: `user-guidance-experience`  
**Target Domain Folder**: `specs/domains/user-guidance-experience`  
**Created**: 2026-05-06  
**Status**: Draft  
**Input**: User description: "Homepage avec preview camera reel immediat et mise au point, bouton prise de photo sur flux existant, bouton Test LLM qui relance HomeLlmMockRunner, bouton desactive pendant execution, resultat sur panneau dedie, fallback explicite si camera indisponible."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Visualiser et piloter la capture depuis la homepage (Priority: P1)

En tant qu'utilisatrice, je veux voir un aperçu caméra réel immédiatement sur la homepage avec contrôle de mise au point, puis capturer une photo via le bouton dédié au flux existant.

**Why this priority**: La capture est le parcours principal de la homepage et doit rester fluide et explicite.

**Independent Test**: Peut être testé en ouvrant la homepage, en vérifiant la preview live, le focus, puis en déclenchant la capture via le bouton photo existant.

**Acceptance Scenarios**:

1. **Given** l'application est ouverte sur la homepage et la caméra est disponible, **When** l'écran est affiché, **Then** un flux caméra réel est visible immédiatement dans un cadre d'aperçu.
2. **Given** le flux est affiché, **When** l'utilisatrice ajuste la mise au point, **Then** le cadre reflète la zone mise au point.
3. **Given** le bouton de prise de photo est visible sous le cadre, **When** l'utilisatrice clique dessus, **Then** le flux existant de prise de photo est lancé.

---

### User Story 2 - Relancer le test LLM et afficher le resultat (Priority: P2)

En tant qu'utilisatrice, je veux lancer un test LLM depuis la homepage et voir le résultat sur un panneau dédié pour confirmer le bon fonctionnement du pipeline.

**Why this priority**: Le bouton Test LLM est un contrôle qualité utilisateur visible directement sur la homepage.

**Independent Test**: Peut être testé en cliquant sur le bouton Test LLM, en vérifiant sa désactivation pendant l'exécution et l'affichage final du résultat dans le panneau dédié.

**Acceptance Scenarios**:

1. **Given** la homepage est visible, **When** l'utilisatrice clique sur le bouton Test LLM, **Then** le test réutilise exactement le runner de test LLM existant.
2. **Given** une exécution est confirmée en cours, **When** l'état d'exécution est actif, **Then** le bouton Test LLM est désactivé jusqu'à la fin de cette exécution.
3. **Given** le test se termine avec succès, **When** la réponse est disponible, **Then** elle s'affiche lisiblement dans un panneau de résultat dédié.

---

### User Story 3 - Comprendre les indisponibilites et erreurs (Priority: P3)

En tant qu'utilisatrice, je veux recevoir un message explicite quand la caméra est indisponible ou quand le test LLM échoue pour comprendre immédiatement ce qui bloque.

**Why this priority**: Sans feedback explicite, l'utilisatrice ne peut pas distinguer un échec d'une absence d'action.

**Independent Test**: Peut être testé en simulant une indisponibilité caméra et un échec de test LLM pour vérifier les messages explicites attendus.

**Acceptance Scenarios**:

1. **Given** la caméra est indisponible, **When** la homepage se charge, **Then** un message explicite remplace le cadre d'aperçu caméra.
2. **Given** une erreur survient pendant l'exécution du test LLM, **When** l'échec est détecté, **Then** un message d'erreur explicite s'affiche dans le panneau dédié.

---

### Edge Cases

- Que se passe-t-il si l'utilisatrice clique plusieurs fois rapidement sur le bouton Test LLM?
- Que se passe-t-il si la caméra devient indisponible pendant une session deja ouverte?
- Que se passe-t-il si la réponse LLM est vide ou indisponible?
- Que se passe-t-il si la homepage est quittée avant la fin de la réponse?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST afficher sur la homepage un cadre d'aperçu caméra basé sur un flux réel immédiat quand la caméra est disponible.
- **FR-002**: Le système MUST permettre un contrôle de mise au point depuis ce cadre d'aperçu.
- **FR-003**: Le système MUST afficher sous le cadre un bouton de prise de photo qui lance le flux existant de capture.
- **FR-004**: Le système MUST afficher sous le bouton de prise de photo un bouton Test LLM dédié.
- **FR-005**: Le système MUST relancer le test LLM via le runner existant de la homepage sans introduire de nouveau parcours utilisateur.
- **FR-006**: Le système MUST désactiver le bouton Test LLM pendant toute exécution confirmée.
- **FR-007**: Le système MUST afficher le résultat du test (succès ou erreur) dans un panneau dédié distinct du cadre caméra.
- **FR-008**: Le système MUST afficher un message explicite a la place du cadre d'aperçu si la caméra est indisponible.
- **FR-009**: Le système MUST permettre une nouvelle exécution Test LLM après la fin de l'exécution en cours.

### Key Entities *(include if feature involves data)*

- **HomepageCameraPreviewState**: Représente l'état du cadre caméra (`available`, `unavailable`).
- **HomepageCaptureAction**: Représente l'action utilisateur sur le bouton de prise de photo vers le flux existant.
- **HomepageLlmTestTrigger**: Représente l'action utilisateur de clic sur le bouton Test LLM.
- **HomepageLlmRunState**: Représente l'état courant du test (`idle`, `running`, `success`, `failure`).
- **HomepageLlmResultPanel**: Représente le panneau dédié d'affichage du résultat (message de succès ou d'erreur).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Dans au moins 95% des ouvertures de homepage avec caméra disponible, le flux d'aperçu est visible en moins de 2 secondes.
- **SC-002**: 100% des activations du bouton de prise de photo déclenchent le flux existant de capture.
- **SC-003**: 100% des clics valides sur le bouton Test LLM déclenchent une tentative d'exécution unique.
- **SC-004**: 100% des clics répétés sur Test LLM pendant une exécution confirmée n'ouvrent pas d'exécution concurrente.
- **SC-005**: Dans au moins 95% des exécutions Test LLM réussies, le résultat est visible dans le panneau dédié en moins de 30 secondes.
- **SC-006**: 100% des indisponibilités caméra et des échecs Test LLM affichent un message explicite en moins de 2 secondes après détection.

## Assumptions

- La homepage reste le point d'entrée principal pour l'utilisatrice.
- Le flux de prise de photo existant est déjà opérationnel et réutilisable sans changement de périmètre.
- Le test LLM réutilise le runner existant de la homepage.
- La définition de "résultat exploitable" est portée par le domaine d'analyse et seulement présentée par l'interface.
- Le scénario reste local et ne dépend pas d'une connectivité réseau externe.
