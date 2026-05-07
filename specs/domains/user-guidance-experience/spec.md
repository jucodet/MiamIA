# Feature Specification: photo-capture-llm-result-flow

**Feature Branch**: `017-photo-analyse-ecran-resultat`  
**Domain Context**: `user-guidance-experience`  
**Target Domain Folder**: `specs/domains/user-guidance-experience`  
**Created**: 2026-05-06  
**Status**: Draft  
**Input**: User description: "Je veux arriver sur l'écran de prise de photo avec le bouton de prise de photo dessous et avec le bouton de test LLM juste en dessous. Une fois la photo prise et analysée par le LLM, je suis redirigé vers un écran qui m'affiche l'output du LLM. Pendant l'analyse par le LLM, je veux un loader m'indiquant que le processus est en cours." — *Évolutions*: « l'écran d'accueil doit être l'écran de prise de photo. Supprime les onglets. » + « la photo est bien prise, le texte est bien capturé mais je suis redirigé sur un écran "Analyse - Erreur, Aucun contenu à afficher" ; ce cas doit être évité par un affichage utile. »

## Clarifications

### Session 2026-05-06

- Q: Où afficher le loader après une photo (écran capture, écran résultat ou plein écran détaché) ? → A: Option A — indicateur sur l’écran de capture, y compris via un recouvrement plein cadre au-dessus ; navigation vers l’écran résultat uniquement après fin ou échec du traitement.
- Q: Comportement si l’utilisatrice quitte l’écran de capture pendant le chargement LLM ? → A: Option A — aucune navigation automatique vers l’écran résultat à la fin du traitement si elle a quitté l’écran capture pendant le chargement ; pas de présentation incohérente du résultat.

### Session 2026-05-06 (shell d’application)

- Décision produit : l’application démarre directement sur l’écran de prise de photo ; la structure principale ne comporte pas de barre d’onglets entre plusieurs sections (accueil / caméra / autre).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Disposer de l'écran de capture comme accueil, sans onglets (Priority: P1)

En tant qu'utilisatrice, je veux que le premier écran de l'application soit l'écran de prise de photo — avec la zone de prévisualisation (lorsque la caméra est disponible), puis le bouton de prise de photo, et immédiatement en dessous le bouton de test LLM — sans barre d'onglets entre plusieurs sections, afin d'enchaîner capture ou test sans changer d'onglet.

**Why this priority**: C'est le point d'entrée du produit ; l'ordre des contrôles et l'absence d'onglets définissent l'expérience par défaut.

**Independent Test**: Lancer l'application à froid et vérifier l'affichage direct de l'écran de prise de photo, l'absence de barre d'onglets principale, et l'ordre vertical : prévisualisation (ou message de repli), bouton photo, puis bouton test LLM.

**Acceptance Scenarios**:

1. **Given** l'application vient d'être ouverte depuis le lanceur, **When** l'interface principale apparaît, **Then** l'écran de prise de photo est affiché sans passage obligatoire par un autre écran d'accueil à onglets.
2. **Given** l'application est au premier plan, **When** l'utilisatrice observe le chrome de navigation principal, **Then** aucune barre d'onglets multi-sections n'est présentée comme structure principale.
3. **Given** la caméra est disponible, **When** l'écran de prise de photo s'affiche, **Then** la prévisualisation réelle est visible et le bouton de prise de photo est placé sous cette zone.
4. **Given** l'écran est affiché, **When** l'utilisatrice parcourt les actions principales, **Then** le bouton de test LLM est visible directement sous le bouton de prise de photo.
5. **Given** le bouton de prise de photo est activé, **When** l'utilisatrice le déclenche, **Then** le flux existant de capture photo démarre.
6. **Given** un contrôle de mise au point est prévu sur la prévisualisation, **When** l'utilisatrice l'utilise, **Then** la zone de prévisualisation reflète la mise au point.

---

### User Story 2 - Suivre l'analyse LLM puis consulter le résultat sur un écran dédié (Priority: P1)

En tant qu'utilisatrice, après une photo, je veux voir un indicateur de chargement pendant l'analyse LLM, puis être conduite automatiquement vers un écran qui affiche clairement la sortie du LLM, y compris lorsque la transcription est longue.

**Why this priority**: La promesse centrale est la lisibilité du résultat après capture, avec feedback pendant le traitement.

**Independent Test**: Prendre une photo, vérifier l'affichage du chargement pendant l'analyse, puis la présence du texte de sortie sur l'écran de résultat après fin du traitement.

**Acceptance Scenarios**:

1. **Given** une photo vient d'être capturée et l'analyse LLM démarre, **When** le traitement est en cours, **Then** un loader (ou équivalent visuel non ambigu) est présenté sur l’écran de capture — y compris comme recouvrement plein cadre — et l’écran de résultat n’est pas encore affiché.
2. **Given** l'analyse LLM se termine avec succès, **When** le résultat est prêt et l'utilisatrice est restée sur l'écran de capture jusqu'à la fin du chargement, **Then** l'application affiche un écran distinct dédié à la sortie du LLM contenant le contenu exploitable.
3. **Given** l'analyse LLM échoue, **When** l'échec est connu et l'utilisatrice est restée sur l'écran de capture jusqu'à la fin du chargement, **Then** l'utilisatrice voit un message explicite sur l'écran de résultat ou un écran d'erreur cohérent avec ce parcours.
4. **Given** le loader est affiché, **When** l'utilisatrice attend, **Then** aucune deuxième analyse concurrente n'est lancée sans action explicite supplémentaire.
5. **Given** le loader est affiché après une photo, **When** l'utilisatrice quitte l'écran de capture (retour arrière ou équivalent) avant la fin du traitement, **Then** à l'issue du traitement aucune ouverture automatique de l'écran de résultat n'a lieu.
6. **Given** la capture est validée et le texte OCR est disponible, **When** la navigation vers l'écran d'analyse/résultat se produit, **Then** l'écran n'affiche pas un état "Aucun contenu à afficher" sans proposer de contenu exploitable ou une récupération guidée.
7. **Given** la sortie de transcription est très longue, **When** l'écran de résultat est affiché, **Then** le contenu textuel reste lisible dans une zone qui n'expulse pas les contrôles hors écran et permet d'atteindre les actions situées en dessous.

---

### User Story 3 - Lancer le test LLM depuis le même écran (Priority: P2)

En tant qu'utilisatrice, je veux pouvoir déclencher le test LLM depuis le bouton sous le bouton photo, avec le même type de retour utilisateur (chargement puis écran de résultat ou message d'erreur), en réutilisant le mécanisme de test LLM déjà prévu pour la homepage.

**Why this priority**: Le bouton de test reste un contrôle qualité visible sans refaire un parcours caché.

**Independent Test**: Depuis l'écran de capture, appuyer sur test LLM, vérifier chargement puis affichage résultat sur l'écran dédié (ou erreur explicite).

**Acceptance Scenarios**:

1. **Given** l'écran de capture est visible, **When** l'utilisatrice active le test LLM, **Then** le runner de test LLM existant est utilisé sans nouveau parcours utilisateur parallèle et le chargement suit la même règle que le parcours photo (sur l’écran de capture avant navigation résultat).
2. **Given** une exécution test LLM est en cours, **When** l'état « en cours » est actif, **Then** le bouton test LLM n'accepte pas de nouvelle exécution jusqu'à la fin.
3. **Given** le test se termine, **When** le résultat ou l'erreur est disponible et l'utilisatrice est restée sur l'écran de capture jusqu'à la fin du chargement, **Then** l'utilisatrice est orientée vers l'écran de résultat (ou message) de façon cohérente avec le parcours photo.
4. **Given** le chargement du test LLM est affiché, **When** l'utilisatrice quitte l'écran de capture avant la fin du traitement, **Then** à l'issue du traitement aucune ouverture automatique de l'écran de résultat n'a lieu.

---

### User Story 4 - Comprendre les indisponibilités (Priority: P3)

En tant qu'utilisatrice, je veux un message clair si la caméra est indisponible, tout en gardant accès au bouton de test LLM lorsque c'est pertinent.

**Why this priority**: Évite la confusion entre « rien ne se passe » et un blocage technique.

**Independent Test**: Simuler caméra indisponible, vérifier le message à la place de la prévisualisation et le comportement des boutons restants.

**Acceptance Scenarios**:

1. **Given** la caméra est indisponible, **When** l'écran de capture s'affiche, **Then** un message explicite remplace la prévisualisation.
2. **Given** la caméra redevient disponible pendant la session, **When** l'état est mis à jour, **Then** la prévisualisation peut réapparaître sans redémarrage d'application obligatoire (si le produit le permet).

---

### Edge Cases

- Clics rapides répétés sur le bouton photo ou test LLM pendant un chargement.
- Sortie de l'écran de capture pendant le loader : pas de navigation automatique vers l'écran résultat à la fin du traitement ; aucune pop-up ou écran résultat inattendu.
- Sortie LLM vide ou partielle : feedback explicite sur l'écran de résultat.
- Sortie de transcription très volumineuse : le contenu ne déborde pas visuellement et les contrôles sous le texte restent atteignables.
- OCR présent mais charge utile d'analyse absente à la navigation : présenter un contenu de repli exploitable ou une erreur actionnable, jamais un écran vide bloquant.
- Durée d'analyse très longue : le loader reste visible ou le système indique clairement que le traitement continue.
- Retour arrière depuis un écran secondaire (ex. résultat LLM) : comportement cohérent avec une pile de navigation simple, sans réintroduction d'onglets.
- Utilisatrice habituée à une ancienne version à onglets : le premier écran reste la capture ; aucune exigence de tutoriel dans ce périmètre.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST présenter un écran de prise de photo comme premier écran affiché après lancement normal de l'application (écran d'accueil = écran de capture).
- **FR-002**: Le système MUST afficher une prévisualisation caméra réelle lorsque la caméra est disponible.
- **FR-003**: Le système MUST permettre, le cas échéant, un contrôle de mise au point sur la prévisualisation.
- **FR-004**: Le système MUST afficher sous la zone de prévisualisation (ou sous le message de repli caméra) un bouton de prise de photo qui lance le flux existant de capture.
- **FR-005**: Le système MUST afficher directement sous le bouton de prise de photo un bouton de test LLM distinct.
- **FR-006**: Le système MUST, après capture photo et lancement de l'analyse LLM, afficher un indicateur de chargement clair sur l’écran de capture (y compris recouvrement plein cadre de cet écran) jusqu'à la fin du traitement ou à l'échec, sans afficher l’écran de résultat avant cet état terminal.
- **FR-007**: Le système MUST, lorsque l'analyse LLM après photo se termine avec un résultat exploitable et que l'utilisatrice est restée sur l'écran de capture jusqu'à la fin du chargement, conduire l'utilisatrice vers un écran dédié affichant la sortie du LLM.
- **FR-008**: Le système MUST réutiliser le runner de test LLM existant pour le bouton test LLM.
- **FR-009**: Le système MUST désactiver le bouton test LLM pendant toute exécution de ce test confirmée en cours.
- **FR-010**: Le système MUST afficher un message explicite sur l'écran de résultat (ou équivalent) en cas d'échec d'analyse ou de test LLM lorsque l'utilisatrice est restée sur l'écran de capture jusqu'à la détection de l'échec.
- **FR-011**: Le système MUST afficher un message explicite à la place de la prévisualisation lorsque la caméra est indisponible.
- **FR-012**: Le système MUST permettre une nouvelle capture ou un nouveau test après la fin du cycle en cours, sans exiger de redémarrage de l'application.
- **FR-013**: Le système MUST, pour le parcours déclenché par le bouton test LLM, appliquer la même règle qu’en FR-006 : chargement sur l’écran de capture (ou recouvrement équivalent) jusqu’à l’état terminal, puis navigation ou présentation cohérente vers l’écran de résultat lorsque l'utilisatrice est restée sur l'écran de capture jusqu'à la fin du chargement.
- **FR-014**: Le système MUST, si l'utilisatrice quitte l'écran de capture pendant l'affichage du chargement LLM (parcours photo ou test LLM), ne pas ouvrir automatiquement l'écran de résultat lorsque le traitement se termine ensuite.
- **FR-015**: Le système MUST ne pas utiliser de barre d'onglets (ou équivalent de navigation par onglets entre sections majeures) comme structure de navigation principale de l'application.
- **FR-016**: Le système MUST conserver l'écran de prise de photo comme contexte d'accueil après retour depuis les écrans du flux décrit (ex. résultat LLM), sauf si l'utilisatrice a quitté l'application ou une règle système impose un autre état.
- **FR-017**: Le système MUST interdire l'affichage d'un écran d'analyse/résultat vide après une capture réussie avec texte OCR disponible ; une navigation vers cet écran n'est autorisée que si un contenu affichable ou un état d'erreur actionnable est prêt.
- **FR-018**: Le système MUST, lorsqu'aucun contenu d'analyse n'est disponible mais que le texte OCR a bien été capturé, afficher un état de repli utile (ex. texte reconnu et prochaine action claire) au lieu du message bloquant "Aucun contenu à afficher".
- **FR-019**: Le système MUST, lorsqu'une erreur d'analyse survient, afficher un message explicite avec action immédiate de récupération dans le même flux utilisateur (retenter l'analyse, revenir à la capture, ou équivalent), plutôt qu'un simple constat sans suite.
- **FR-020**: Le système MUST, sur l'écran de résultat, contenir une transcription longue dans une zone de lecture adaptée afin d'éviter tout dépassement d'écran qui masquerait ou expulserait les contrôles affichés sous le contenu.
- **FR-021**: Le système MUST permettre à l'utilisatrice d'accéder aux contrôles situés sous la transcription, même lorsque le texte occupe plusieurs écrans de hauteur, sans perte d'action disponible.

### Key Entities *(include if feature involves data)*

- **AppNavigationShell**: Enveloppe de navigation sans onglets ; premier écran = capture ; empilement simple vers écrans secondaires (ex. résultat).
- **CaptureScreenState**: État de la prévisualisation (`disponible`, `indisponible`) et visibilité des boutons.
- **PhotoCaptureIntent**: Action de prise de photo vers le flux existant.
- **LlmTestIntent**: Action sur le bouton test LLM.
- **LlmProcessingState**: `inactif`, `en_cours` (loader présenté sur le contexte « écran de capture » jusqu’à l’état terminal), `termine_succès`, `termine_échec`.
- **LlmResultPresentation**: Contenu présenté sur l'écran de résultat (sortie principale et message d'erreur le cas échéant).
- **ResultContentViewport**: Zone de lecture qui affiche le contenu long sans débordement hors écran et préserve l'accès aux contrôles situés en dessous.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Dans au moins 95 % des ouvertures de l'écran avec caméra disponible, la prévisualisation est visible en moins de 2 secondes.
- **SC-002**: 100 % des appuis valides sur le bouton photo déclenchent le flux de capture existant.
- **SC-003**: 100 % des lancements d'analyse LLM après photo affichent l'indicateur de chargement en moins de 1 seconde après le début du traitement côté produit.
- **SC-004**: Dans au moins 95 % des analyses réussies après photo, **lorsque l'utilisatrice reste sur l'écran de capture jusqu'à la fin du traitement**, elle accède à l'écran de résultat avec la sortie visible en moins de 30 secondes après la fin de capture (hors cas limite matériel).
- **SC-005**: 100 % des appuis sur test LLM pendant une exécution confirmée en cours ne déclenchent pas d'exécution concurrente.
- **SC-006**: 100 % des indisponibilités caméra affichent un message explicite en moins de 2 secondes après détection ; pour les échecs d'analyse ou de test, la même exigence s'applique **lorsque l'utilisatrice est restée sur l'écran de capture jusqu'à la détection de l'échec**.
- **SC-007**: Dans au moins 95 % des lancements à froid, l'utilisatrice voit l'écran de prise de photo (sans barre d'onglets principale) en moins de 3 secondes après l'icône d'application, sur matériel représentatif du produit.
- **SC-008**: Dans 100 % des cas de capture réussie avec texte OCR disponible, l'écran d'analyse/résultat affiche un contenu utile ou une erreur actionnable ; le taux d'apparition d'un écran "Aucun contenu à afficher" sans issue est de 0 %.
- **SC-009**: Dans 100 % des parcours où la transcription dépasse la hauteur visible initiale, aucune action principale placée sous le texte n'est perdue hors écran ; l'utilisatrice peut l'atteindre via le défilement du contenu.

## Assumptions

- L'écran de prise de photo est l'écran d'accueil au lancement ; il n'existe pas de page d'accueil distincte avec onglets avant la capture.
- Le flux de capture photo et le runner de test LLM existants restent la référence fonctionnelle ; ce périmètre décrit l'orchestration UX et la navigation vers l'écran de résultat.
- La nature détaillée du « résultat exploitable » du LLM relève des domaines d'analyse ; l'écran de résultat se contente d'une présentation lisible conforme aux contrats produit existants.
- Le parcours reste local (pas de dépendance à une connectivité réseau externe pour valider le flux de base).
- Après abandon du flux (quitte l'écran capture pendant le chargement), aucune exigence de récupération ultérieure du résultat n'est imposée dans ce périmètre ; une évolution produit pourrait la définir.
- Les parcours ou contenus auparavant accessibles uniquement via d'autres onglets ne sont pas réimplémentés dans ce périmètre sous une autre forme ; une évolution distincte peut réintroduire des accès secondaires (menu, raccourci) sans remettre une barre d'onglets principale, sauf nouvelle décision produit.
- Le texte OCR validé est considéré comme contenu de base disponible pour éviter un écran de résultat vide lorsque l'analyse détaillée n'est pas encore exploitable.
- En cas de transcription très longue, le besoin produit prioritaire est la préservation de la lisibilité du texte et de l'accessibilité des actions situées sous ce texte.
