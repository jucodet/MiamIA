# Feature Specification: llm-download-onboarding

**Feature Branch**: `018-llm-download-onboarding`  
**Domain Context**: `user-guidance-experience`  
**Target Domain Folder**: `specs/domains/user-guidance-experience`  
**Created**: 2026-05-11  
**Status**: Draft  
**Input**: User description: "Je voudrais améliorer l'expérience de la première utilisation qui nécessite le téléchargement du fichier de LLM. Tout d'abord, une confirmation qu'un fichier volumineux doit être téléchargé, et que l'utilisateur est bien en wifi ou bien a prévu le coup en 4G. S'il valide, redirection vers un écran pour patienter avec un message titre, des phrases humoristiques rotatives, et un loader fouet animé."

## Clarifications

### Session 2026-05-11

- Q: L'écran d'attente doit-il afficher un indicateur de progression (barre/pourcentage) en plus du fouet et des phrases ? → A: Oui — barre de progression + pourcentage affichés sous le titre.
- Q: Comportement au premier lancement sans aucune connexion réseau ? → A: Écran dédié "Connexion requise" avec explication et bouton "Réessayer" (pas de boîte de confirmation de téléchargement).
- Q: Format de la confirmation de téléchargement (dialog, bottom sheet, écran plein) ? → A: Écran plein dédié (page d'onboarding) avec titre, explication, info réseau détecté, et boutons Confirmer / Plus tard.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Confirmer le téléchargement du modèle avant lancement (Priority: P1)

En tant qu'utilisatrice ouvrant l'application pour la première fois (ou après suppression du modèle), je veux être informée qu'un fichier volumineux doit être téléchargé et confirmer que ma connexion est adaptée (Wi-Fi ou 4G accepté volontairement), afin de ne pas consommer involontairement un forfait limité.

**Why this priority**: C'est le point d'entrée obligatoire de l'onboarding ; sans confirmation explicite, l'utilisatrice pourrait consommer plusieurs centaines de Mo de data sans le savoir.

**Independent Test**: Simuler un premier lancement sans modèle local présent et vérifier l'affichage de la boîte de confirmation, la détection du type de connexion, et le blocage du téléchargement tant que la confirmation n'est pas donnée.

**Acceptance Scenarios**:

1. **Given** le modèle LLM n'est pas présent localement, **When** l'utilisatrice arrive sur l'écran principal, **Then** une boîte de confirmation s'affiche avant toute autre action, informant de la taille approximative du téléchargement.
2. **Given** la boîte de confirmation est affichée, **When** l'appareil est connecté en Wi-Fi, **Then** le message indique clairement que la connexion Wi-Fi est détectée.
3. **Given** la boîte de confirmation est affichée, **When** l'appareil est connecté en données mobiles (4G/5G), **Then** le message avertit explicitement que le téléchargement consommera des données mobiles et demande une confirmation renforcée.
4. **Given** la boîte de confirmation est affichée, **When** l'utilisatrice décline le téléchargement, **Then** aucun téléchargement ne démarre et un message explique que l'application nécessite le modèle pour fonctionner, avec possibilité de relancer plus tard.
5. **Given** l'utilisatrice confirme le téléchargement, **When** la validation est enregistrée, **Then** le téléchargement démarre immédiatement et l'utilisatrice est redirigée vers l'écran d'attente.

---

### User Story 2 - Patienter avec feedback pendant le téléchargement du modèle (Priority: P1)

En tant qu'utilisatrice ayant confirmé le téléchargement, je veux voir un écran d'attente engageant avec un titre clair, des phrases humoristiques rotatives et une animation de fouet mixeur, afin de savoir que le processus est actif et de ne pas m'ennuyer.

**Why this priority**: Le téléchargement peut durer plusieurs minutes ; sans feedback engageant, l'utilisatrice pourrait croire que l'application est gelée et la fermer.

**Independent Test**: Déclencher le téléchargement et vérifier l'affichage de l'écran d'attente avec le titre, la rotation des phrases humoristiques toutes les 5 secondes, et l'animation du fouet.

**Acceptance Scenarios**:

1. **Given** le téléchargement est en cours, **When** l'écran d'attente est affiché, **Then** le titre "Téléchargement du modèle de langage en cours..." est visible en permanence.
2. **Given** l'écran d'attente est affiché, **When** 5 secondes s'écoulent, **Then** la phrase humoristique affichée change pour la suivante (ordre aléatoire dans la liste définie).
3. **Given** l'écran d'attente est affiché, **When** l'utilisatrice observe l'animation, **Then** un fouet mixeur animé est visible et oscille de manière continue pour indiquer que le processus est actif.
4. **Given** le téléchargement se termine avec succès, **When** le fichier modèle est prêt, **Then** l'utilisatrice est redirigée automatiquement vers l'écran principal (capture photo) sans action supplémentaire.
5. **Given** le téléchargement échoue (perte de connexion, espace insuffisant), **When** l'erreur est détectée, **Then** un message explicite est affiché avec une action de récupération (réessayer ou vérifier la connexion).

---

### User Story 3 - Reprendre un téléchargement interrompu (Priority: P2)

En tant qu'utilisatrice dont le téléchargement a été interrompu (fermeture de l'app, perte réseau), je veux pouvoir reprendre le téléchargement sans tout recommencer, afin de ne pas perdre le temps et les données déjà consommés.

**Why this priority**: L'expérience de reprise est importante pour ne pas frustrer l'utilisatrice en cas d'interruption sur un fichier volumineux.

**Independent Test**: Interrompre un téléchargement à mi-parcours, relancer l'application, et vérifier que le téléchargement reprend là où il s'est arrêté.

**Acceptance Scenarios**:

1. **Given** un téléchargement a été interrompu à 60 % de progression, **When** l'utilisatrice relance l'application, **Then** la boîte de confirmation propose de reprendre le téléchargement (pas de recommencer depuis zéro).
2. **Given** la reprise est proposée, **When** l'utilisatrice confirme, **Then** le téléchargement reprend à partir du point d'interruption.

---

### Edge Cases

- L'appareil passe de Wi-Fi à données mobiles pendant le téléchargement : le téléchargement continue (la confirmation a déjà été donnée) mais si la connexion est perdue, l'erreur est gérée.
- L'espace disque est insuffisant pour stocker le modèle : message explicite avant le début du téléchargement.
- L'utilisatrice quitte l'application pendant le téléchargement : le téléchargement continue en arrière-plan si possible, sinon reprend au prochain lancement.
- Plusieurs ouvertures rapides de l'application : pas de téléchargements concurrents.
- Le modèle est déjà présent (mise à jour) : pas de boîte de confirmation, passage direct à l'écran principal.
- L'appareil est complètement hors-ligne au premier lancement : écran "Connexion requise" avec bouton "Réessayer" ; pas de boîte de confirmation tant que le réseau n'est pas disponible.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST détecter l'absence du modèle LLM local au lancement de l'application.
- **FR-002**: Le système MUST afficher un écran plein dédié (page d'onboarding) informant de la taille approximative du téléchargement, du type de connexion détecté, et proposant les actions "Confirmer" et "Plus tard".
- **FR-003**: Le système MUST détecter le type de connexion réseau actuel (Wi-Fi vs données mobiles) et adapter le message de confirmation en conséquence.
- **FR-004**: Le système MUST bloquer le téléchargement tant que l'utilisatrice n'a pas confirmé explicitement.
- **FR-005**: Le système MUST, après confirmation, rediriger vers un écran d'attente dédié au téléchargement.
- **FR-006**: Le système MUST afficher sur l'écran d'attente le titre "Téléchargement du modèle de langage en cours..." de façon permanente.
- **FR-007**: Le système MUST afficher une phrase humoristique en rotation (ordre aléatoire) avec changement toutes les 5 secondes, en utilisant la même liste que l'écran de streaming analyse.
- **FR-008**: Le système MUST afficher un fouet mixeur animé (même animation que l'écran de streaming analyse) comme indicateur de processus actif.
- **FR-015**: Le système MUST afficher une barre de progression avec pourcentage sous le titre de l'écran d'attente, reflétant l'avancement réel du téléchargement.
- **FR-009**: Le système MUST rediriger automatiquement vers l'écran principal une fois le téléchargement terminé avec succès.
- **FR-010**: Le système MUST afficher un message d'erreur explicite avec action de récupération en cas d'échec de téléchargement.
- **FR-011**: Le système MUST permettre à l'utilisatrice de décliner le téléchargement et afficher un état explicatif de l'impossibilité d'utiliser l'application sans le modèle.
- **FR-012**: Le système MUST empêcher le lancement de téléchargements concurrents.
- **FR-013**: Le système SHOULD supporter la reprise du téléchargement après interruption.
- **FR-014**: Le système SHOULD vérifier l'espace disque disponible avant de lancer le téléchargement et avertir si insuffisant.
- **FR-016**: Le système MUST, lorsque l'appareil est hors-ligne au lancement sans modèle présent, afficher un écran "Connexion requise" avec explication et bouton "Réessayer" au lieu de la boîte de confirmation de téléchargement.

### Key Entities

- **LlmModelReadiness**: État de disponibilité du modèle local (`absent`, `downloading`, `ready`, `error`).
- **DownloadConfirmation**: Décision utilisateur (connexion détectée, confirmation accordée ou refusée).
- **DownloadProgress**: Progression du téléchargement (pourcentage, octets transférés, vitesse estimée).
- **NetworkType**: Type de connexion réseau détecté (`wifi`, `mobile_data`, `offline`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100 % des premiers lancements sans modèle présent affichent la boîte de confirmation avant tout téléchargement.
- **SC-002**: 100 % des confirmations utilisateur déclenchent le téléchargement en moins de 2 secondes.
- **SC-003**: L'écran d'attente affiche les phrases humoristiques en rotation toutes les 5 secondes ± 500 ms.
- **SC-004**: Le fouet animé est visible et en mouvement pendant toute la durée du téléchargement.
- **SC-005**: Dans au moins 95 % des téléchargements réussis, la redirection vers l'écran principal se fait en moins de 3 secondes après la fin du téléchargement.
- **SC-006**: 100 % des échecs de téléchargement présentent un message d'erreur actionnable (réessayer, vérifier connexion) en moins de 5 secondes après détection.
- **SC-007**: L'utilisatrice n'est jamais bloquée sans issue (bouton retour, réessayer, ou information claire toujours disponible).

## Assumptions

- Le modèle LLM est téléchargé depuis un serveur distant accessible via Internet (pas de distribution locale via le Play Store).
- La taille du modèle est significative (plusieurs centaines de Mo) justifiant la confirmation réseau.
- L'animation du fouet et les phrases humoristiques sont les mêmes composants déjà implémentés pour l'écran de streaming d'analyse (réutilisation).
- Le comportement en arrière-plan (téléchargement continu après fermeture) est un objectif souhaitable mais non bloquant pour la V1 ; la reprise au relancement est suffisante.
- L'écran d'attente du téléchargement est distinct de l'écran de streaming d'analyse, même si les composants visuels sont partagés.
