# Domain Spec — user-guidance-experience

**Domain Context**: `user-guidance-experience`
**Created**: 2026-05-06
**Last Modified**: 2026-05-13 (Feature F — libellés capture, fin test LLM)
**Status**: Draft

## Purpose

Orchestrer l'expérience utilisatrice de bout en bout : onboarding (téléchargement du modèle), capture photo, analyse LLM avec streaming progressif, affichage du résultat, et messages de bienvenue. Ce domaine ne possède pas la logique d'analyse elle-même (déléguée à `ingredient-health-intelligence` et `ingredient-normalization-validation`) mais contrôle la navigation, le feedback visuel et les états utilisateur.

## Scope

- Onboarding téléchargement du modèle LLM (confirmation, attente, reprise, refus)
- Écran de capture comme accueil (sans onglets)
- Flux photo → streaming analyse → écran résultat
- Gestion des indisponibilités (caméra, modèle, réseau)
- Messages de bienvenue et ton positif
- Catalogue partagé de phrases humoristiques pour écrans d'attente (téléchargement modèle, analyse LLM)
- Évaluation du code legacy home screen

## Invariants

- L'écran de capture est toujours le point d'entrée après onboarding.
- Aucune barre d'onglets multi-sections comme navigation principale.
- L'utilisatrice n'est jamais bloquée sans issue (bouton retour, réessayer, ou information claire).
- Aucune navigation automatique si l'utilisatrice a quitté l'écran actif pendant un traitement.

---

## Feature A — Photo Capture & LLM Result Flow

> Origine : `017-photo-analyse-ecran-resultat`
> Input : "Je veux arriver sur l'écran de prise de photo avec le bouton de prise de photo dessous et avec le bouton de test LLM juste en dessous. Une fois la photo prise et analysée par le LLM, je suis redirigé vers un écran qui m'affiche l'output du LLM."  
> **Révision 2026-05-13 (Feature F)** : le bouton test LLM et tout parcours associé sont retirés du produit ; seul le flux capture photo → analyse subsiste.
> Évolutions : suppression des onglets + éviter l'écran vide "Aucun contenu à afficher"

### Clarifications (Feature A)

#### Session 2026-05-06

- Q: Où afficher le loader après une photo ? → A: Option A — indicateur sur l'écran de capture puis navigation résultat après état terminal. *(Note : le code a évolué vers streaming progressif directement sur l'écran résultat — validé par Backfill P1, 2026-05-12.)*
- Q: Comportement si l'utilisatrice quitte l'écran pendant le chargement ? → A: Aucune navigation automatique vers l'écran résultat.

#### Session 2026-05-06 (shell d'application)

- Décision produit : l'application démarre directement sur l'écran de prise de photo ; pas de barre d'onglets.

### User Scenarios (Feature A)

#### US-A1 — Disposer de l'écran de capture comme accueil, sans onglets (P1)

En tant qu'utilisatrice, je veux que le premier écran soit l'écran de prise de photo — prévisualisation, bouton photo — sans barre d'onglets ni action de test LLM.

**Acceptance Scenarios**:

1. **Given** l'application ouverte, **When** l'interface principale apparaît, **Then** l'écran de prise de photo est affiché sans passage par un écran d'accueil à onglets.
2. **Given** l'application au premier plan, **When** l'utilisatrice observe la navigation, **Then** aucune barre d'onglets multi-sections n'est présentée.
3. **Given** la caméra disponible, **When** l'écran s'affiche, **Then** la prévisualisation est visible et le bouton photo est placé sous cette zone.
4. **Given** l'écran affiché, **When** l'utilisatrice parcourt les actions sous la prévisualisation, **Then** aucun bouton ni entrée « Test LLM » (ni équivalent de diagnostic) n'est proposé.
5. **Given** le bouton photo activé, **When** déclenché, **Then** le flux de capture existant démarre.
6. **Given** un contrôle de mise au point prévu, **When** utilisé, **Then** la prévisualisation reflète la mise au point.

#### US-A2 — Suivre l'analyse LLM puis consulter le résultat en streaming (P1)

En tant qu'utilisatrice, après une photo, je veux voir le streaming progressif des résultats d'analyse (produit, ingrédients, synthèse, impacts) sur un écran dédié, avec feedback visuel pendant le chargement.

**Acceptance Scenarios**:

1. **Given** une photo capturée et l'analyse LLM démarre, **When** le traitement est en cours, **Then** l'application navigue vers l'écran de résultat dédié et y affiche un indicateur de chargement (fouet animé, phrases rotatives) suivi du streaming progressif. *(Backfill P1.)*
2. **Given** l'analyse réussie, **When** le résultat complet est prêt, **Then** le bilan complet s'affiche (carte résultat avec toutes les sections).
3. **Given** l'analyse en échec, **When** l'échec détecté, **Then** un message explicite avec action de récupération est affiché sur l'écran résultat.
4. **Given** le loader affiché, **When** l'utilisatrice attend, **Then** aucune deuxième analyse concurrente n'est lancée.
5. **Given** le streaming en cours, **When** l'utilisatrice quitte l'écran résultat (retour arrière), **Then** aucune navigation automatique de retour à la fin du traitement. *(Backfill P1.)*
6. **Given** texte OCR disponible mais analyse échouée, **When** l'écran résultat affiché, **Then** un état de repli utile (texte reconnu + action) est proposé.
7. **Given** transcription très longue, **When** résultat affiché, **Then** le contenu reste lisible et les contrôles sous le texte restent atteignables.

#### US-A3 — ~~Lancer le test LLM depuis le même écran (P2)~~ *(révoqué — Feature F, 2026-05-13)*

#### US-A4 — Comprendre les indisponibilités (P3)

En tant qu'utilisatrice, je veux un message clair si la caméra est indisponible.

**Acceptance Scenarios**:

1. **Given** caméra indisponible, **When** écran capture affiché, **Then** message explicite remplace la prévisualisation.
2. **Given** caméra redevient disponible, **When** état mis à jour, **Then** la prévisualisation peut réapparaître.

#### Edge Cases (Feature A)

- Clics rapides répétés sur le bouton photo pendant un chargement.
- Sortie de l'écran résultat pendant le streaming : pas de navigation automatique de retour.
- Sortie LLM vide ou partielle : feedback explicite (état d'attente = contenu non vide).
- Transcription très volumineuse : pas de débordement, contrôles accessibles.
- OCR présent mais analyse absente : repli OCR exploitable, jamais écran vide bloquant.
- Analyse très longue : le streaming reste visible ou indication claire que le traitement continue.
- Retour arrière depuis résultat : pile de navigation simple, pas de réintroduction d'onglets.

### Functional Requirements (Feature A)

- **UGE-A-FR-001**: Le système MUST présenter un écran de prise de photo comme premier écran affiché après lancement normal (écran d'accueil = écran de capture).
- **UGE-A-FR-002**: Le système MUST afficher une prévisualisation caméra réelle lorsque la caméra est disponible.
- **UGE-A-FR-003**: Le système MUST permettre un contrôle de mise au point sur la prévisualisation.
- **UGE-A-FR-004**: Le système MUST afficher sous la prévisualisation un bouton de prise de photo qui lance le flux existant de capture.
- **UGE-A-FR-005** *(révoqué — Feature F)*: ~~bouton test LLM~~ — voir UGE-F-FR-002.
- **UGE-A-FR-006**: Le système MUST, après capture photo et lancement de l'analyse LLM, naviguer vers l'écran de résultat dédié et y afficher un indicateur de chargement (fouet animé, phrases humoristiques rotatives) suivi du streaming progressif des sections d'analyse (produit identifié, ingrédients, synthèse, impacts santé) au fur et à mesure de leur disponibilité. *(Backfill P1 — 2026-05-12.)*
- **UGE-A-FR-007**: Le système MUST, lorsque l'analyse LLM se termine avec succès et que l'utilisatrice est sur l'écran de résultat, afficher le bilan complet. En cas d'échec, afficher un état d'erreur actionnable sur le même écran. *(Backfill P1 — 2026-05-12.)*
- **UGE-A-FR-008** *(révoqué — Feature F)*: ~~runner test LLM~~ — voir UGE-F-FR-002.
- **UGE-A-FR-009** *(révoqué — Feature F)*: ~~désactivation bouton test~~ — voir UGE-F-FR-002.
- **UGE-A-FR-010**: Le système MUST afficher un message explicite sur l'écran de résultat en cas d'échec d'analyse après capture photo.
- **UGE-A-FR-011**: Le système MUST afficher un message explicite à la place de la prévisualisation lorsque la caméra est indisponible.
- **UGE-A-FR-012**: Le système MUST permettre une nouvelle capture après la fin du cycle en cours.
- **UGE-A-FR-013** *(révoqué — Feature F)*: ~~parcours test LLM~~ — voir UGE-F-FR-002.
- **UGE-A-FR-014**: Le système MUST, si l'utilisatrice quitte l'écran de résultat pendant le streaming, ne pas la ramener automatiquement vers cet écran lorsque le traitement se termine. *(Backfill P1 — 2026-05-12.)*
- **UGE-A-FR-015**: Le système MUST ne pas utiliser de barre d'onglets comme structure de navigation principale.
- **UGE-A-FR-016**: Le système MUST conserver l'écran de capture comme contexte d'accueil après retour depuis les écrans secondaires.
- **UGE-A-FR-017**: Le système MUST, sur l'écran de résultat, afficher un état d'attente visuel (animation fouet + phrases rotatives) lorsque le streaming est en cours et qu'aucune section n'est encore disponible. Cet état constitue un contenu non vide. *(Backfill P2a — 2026-05-12.)*
- **UGE-A-FR-018**: Le système MUST, lorsqu'aucun contenu d'analyse n'est disponible mais que le texte OCR est capturé, afficher un état de repli utile au lieu de "Aucun contenu à afficher".
- **UGE-A-FR-019**: Le système MUST, lorsqu'une erreur d'analyse survient, afficher un message explicite avec action de récupération (retenter, revenir à la capture, ou équivalent).
- **UGE-A-FR-020**: Le système MUST contenir une transcription longue dans une zone de lecture adaptée sans débordement.
- **UGE-A-FR-021**: Le système MUST permettre l'accès aux contrôles sous la transcription même lorsque le texte occupe plusieurs écrans de hauteur.

### Key Entities (Feature A)

- **AppNavigationShell**: Navigation sans onglets ; premier écran = capture ; empilement simple.
- **CaptureScreenState**: État prévisualisation (`disponible`, `indisponible`) et visibilité des boutons.
- **PhotoCaptureIntent**: Action de prise de photo vers le flux existant.
- **LlmTestIntent** *(hors produit depuis Feature F)*: conservé uniquement en documentation historique ; aucune entrée UI ne l'expose.
- **LlmProcessingState**: `inactif`, `en_cours` (streaming progressif sur l'écran de résultat dédié), `termine_succès`, `termine_échec`.
- **LlmResultPresentation**: Contenu sur l'écran résultat (sortie principale, erreur, repli OCR).
- **ResultContentViewport**: Zone de lecture pour contenu long avec accès aux contrôles en dessous.

### Success Criteria (Feature A)

- **SC-A-001**: ≥ 95 % des ouvertures avec caméra disponible → prévisualisation visible en < 2 s.
- **SC-A-002**: 100 % des appuis photo → flux de capture déclenché.
- **SC-A-003**: 100 % des lancements d'analyse → indicateur de chargement visible en < 1 s.
- **SC-A-004**: ≥ 95 % des analyses réussies → résultat accessible en < 30 s après capture.
- **SC-A-005** *(révoqué — Feature F)*: critère test LLM supprimé ; remplacé par UGE-F-SC-002.
- **SC-A-006**: 100 % des indisponibilités caméra → message explicite en < 2 s.
- **SC-A-007**: ≥ 95 % des lancements à froid → écran capture visible en < 3 s.
- **SC-A-008**: 100 % des captures réussies avec OCR → contenu utile ou erreur actionnable, jamais écran vide.
- **SC-A-009**: 100 % des transcriptions longues → contrôles sous le texte atteignables via défilement.

---

## Feature B — LLM Download Onboarding

> Origine : `018-llm-download-onboarding`
> Input : "Améliorer l'expérience de première utilisation avec téléchargement du fichier LLM : confirmation, écran d'attente engageant, reprise."

### Clarifications (Feature B)

#### Session 2026-05-11

- Q: Indicateur de progression en plus du fouet ? → A: Oui — barre de progression + pourcentage.
- Q: Premier lancement sans connexion ? → A: Écran "Connexion requise" avec bouton "Réessayer".
- Q: Format de la confirmation ? → A: Écran plein dédié (page d'onboarding).

### User Scenarios (Feature B)

#### US-B1 — Confirmer le téléchargement du modèle (P1)

En tant qu'utilisatrice ouvrant l'app pour la première fois (ou après suppression du modèle), je veux être informée qu'un fichier volumineux doit être téléchargé et confirmer que ma connexion est adaptée.

**Acceptance Scenarios**:

1. **Given** modèle LLM absent, **When** l'utilisatrice arrive sur l'écran principal, **Then** confirmation affichée avant toute action.
2. **Given** confirmation affichée, **When** Wi-Fi détecté, **Then** message indique la connexion Wi-Fi.
3. **Given** confirmation affichée, **When** données mobiles, **Then** avertissement explicite et confirmation renforcée.
4. **Given** confirmation affichée, **When** utilisatrice décline, **Then** l'application se ferme proprement (`finishAffinity()`). *(Backfill P3 — 2026-05-12.)*
5. **Given** utilisatrice confirme, **When** validation enregistrée, **Then** téléchargement démarre et redirection vers l'écran d'attente.

#### US-B2 — Patienter avec feedback pendant le téléchargement (P1)

En tant qu'utilisatrice ayant confirmé, je veux un écran d'attente engageant avec titre, phrases humoristiques rotatives et animation de marmite.

**Acceptance Scenarios**:

1. **Given** téléchargement en cours, **When** écran d'attente affiché, **Then** titre "Téléchargement du modèle de langage en cours..." visible.
2. **Given** écran affiché, **When** 5 s s'écoulent, **Then** phrase humoristique change (ordre aléatoire).
3. **Given** écran affiché, **When** utilisatrice observe l'animation, **Then** `AnimatedMarmite` visible avec progression intégrée. *(Backfill P4 — 2026-05-12.)*
4. **Given** téléchargement réussi, **When** fichier prêt, **Then** redirection automatique vers l'écran principal.
5. **Given** téléchargement échoue, **When** erreur détectée, **Then** message explicite avec action de récupération.

#### US-B3 — Reprendre un téléchargement interrompu (P2)

En tant qu'utilisatrice dont le téléchargement a été interrompu, je veux pouvoir reprendre sans tout recommencer.

**Acceptance Scenarios**:

1. **Given** téléchargement interrompu à 60 %, **When** relance de l'app, **Then** proposition de reprendre le téléchargement.
2. **Given** reprise proposée, **When** utilisatrice confirme, **Then** téléchargement reprend à partir du point d'interruption.

#### Edge Cases (Feature B)

- Wi-Fi → données mobiles pendant le téléchargement : continue (confirmation déjà donnée).
- Espace disque insuffisant : message explicite avant le début.
- Quitte l'app pendant le téléchargement : reprend au prochain lancement.
- Ouvertures rapides multiples : pas de téléchargements concurrents.
- Modèle déjà présent : pas de confirmation, passage direct à l'écran principal.
- Hors-ligne au premier lancement : écran "Connexion requise" avec "Réessayer".

### Functional Requirements (Feature B)

- **UGE-B-FR-001**: Le système MUST détecter l'absence du modèle LLM local au lancement.
- **UGE-B-FR-002**: Le système MUST afficher un écran plein dédié (onboarding) informant de la taille du téléchargement, du type de connexion détecté, et proposant "Confirmer" et "Refuser et quitter". *(Backfill P3 — 2026-05-12.)*
- **UGE-B-FR-003**: Le système MUST détecter le type de connexion réseau (Wi-Fi vs mobile) et adapter le message.
- **UGE-B-FR-004**: Le système MUST bloquer le téléchargement tant que l'utilisatrice n'a pas confirmé.
- **UGE-B-FR-005**: Le système MUST, après confirmation, rediriger vers un écran d'attente dédié.
- **UGE-B-FR-006**: Le système MUST afficher le titre "Téléchargement du modèle de langage en cours..." de façon permanente.
- **UGE-B-FR-007**: Le système MUST afficher une phrase humoristique en rotation (ordre aléatoire) toutes les 5 s, même liste que le streaming analyse.
- **UGE-B-FR-008**: Le système MUST afficher une animation de marmite se remplissant (`AnimatedMarmite`) intégrant la progression du téléchargement. *(Backfill P4 — 2026-05-12.)*
- **UGE-B-FR-009**: Le système MUST rediriger automatiquement vers l'écran principal une fois le téléchargement réussi.
- **UGE-B-FR-010**: Le système MUST afficher un message d'erreur explicite avec action de récupération en cas d'échec.
- **UGE-B-FR-011**: Le système MUST, lorsque l'utilisatrice décline le téléchargement, fermer l'application proprement (`finishAffinity()`). *(Backfill P3 — 2026-05-12.)*
- **UGE-B-FR-012**: Le système MUST empêcher les téléchargements concurrents.
- **UGE-B-FR-013**: Le système SHOULD supporter la reprise du téléchargement après interruption.
- **UGE-B-FR-014**: Le système SHOULD vérifier l'espace disque avant le téléchargement et avertir si insuffisant.
- **UGE-B-FR-015**: Le système MUST afficher une barre de progression avec pourcentage sous le titre.
- **UGE-B-FR-016**: Le système MUST, lorsque l'appareil est hors-ligne au lancement sans modèle, afficher un écran "Connexion requise" avec "Réessayer".

### Key Entities (Feature B)

- **LlmModelReadiness**: `absent`, `downloading`, `ready`, `error`.
- **DownloadConfirmation**: Connexion détectée, confirmation accordée ou refusée.
- **DownloadProgress**: Pourcentage, octets transférés, vitesse estimée.
- **NetworkType**: `wifi`, `mobile_data`, `offline`.

### Success Criteria (Feature B)

- **SC-B-001**: 100 % des premiers lancements sans modèle → confirmation avant tout téléchargement.
- **SC-B-002**: 100 % des confirmations → téléchargement en < 2 s.
- **SC-B-003**: Phrases rotatives toutes les 5 s ± 500 ms.
- **SC-B-004**: Animation visible et en mouvement pendant toute la durée du téléchargement.
- **SC-B-005**: ≥ 95 % des téléchargements réussis → redirection en < 3 s.
- **SC-B-006**: 100 % des échecs → message actionnable en < 5 s.
- **SC-B-007**: L'utilisatrice n'est jamais bloquée sans issue.

---

## Feature C — Welcome & Home (Placeholder)

> Origine : sync-apply P16, 2026-05-12
> Source packages : `welcome/` (7 fichiers, ~169 lignes), `home/` (10 fichiers, ~410 lignes)
> Status : à compléter via `/speckit-sync-backfill`

### Scope (Feature C)

#### Welcome (`welcome/`)
- Catalogue de messages de bienvenue
- Sélection aléatoire du message
- Policy de ton (positif, encourageant)
- Gestion du catalogue vide

#### Home Legacy (`home/`)
- ⚠️ **À évaluer** : `HomeScreen` et `HomeViewModel` sont potentiellement des vestiges de l'ancienne architecture à onglets (cf. UGE-A-FR-015)
- `HomeLlmMockRunner` : **hors parcours utilisateur** depuis Feature F (2026-05-13) — suppression attendue avec le bouton test LLM ; à retirer ou isoler en tests internes uniquement si encore référencé.
- Layout, MediaPipe status, spacing

### Functional Requirements (Feature C)

- *(à extraire du code via `/speckit-sync-backfill`)*

### Actions requises (Feature C)

1. **Backfill welcome/** : Générer les user stories et FRs depuis le code
2. **Évaluer home/** : Identifier le code actif vs le code vestige ; migrer ou supprimer le code mort

---

## Feature D — Suppression du message d'accueil sur l'écran capture

**Branche**: `022-remove-welcome-banner` · **Date**: 2026-05-13 · **Statut**: Draft

> Décision produit (2026-05-13) : retirer la bannière de message d'accueil affichée en haut de l'écran capture. L'écran capture devient l'écran d'accueil dans une forme épurée, centrée sur la caméra et l'action de capture. Cette décision **rétracte** l'exigence visuelle d'affichage du message d'accueil dans Feature C / 010-message-bienvenue-sourire pour l'écran d'accueil ; le code du package `welcome/` peut être conservé temporairement comme legacy mais ne MUST plus être rendu.

### User Scenarios (Feature D)

#### US-D1 — Écran d'accueil sans bannière d'accueil (P1)

En tant qu'utilisatrice, lorsque j'ouvre l'application et arrive sur l'écran d'accueil (= écran capture), je ne veux **plus voir aucun message de bienvenue** au-dessus de l'aperçu caméra. L'écran présente uniquement l'indicateur de statut technique (MediaPipe), la zone caméra, et la bande d'action.

**Pourquoi P1** : la bannière occupe de l'espace vertical critique sur petits écrans et n'apporte pas la valeur attendue ; la décision produit est explicite et bloquante avant toute autre évolution UX de l'écran d'accueil.

**Test indépendant** : à l'ouverture de l'application sur émulateur / appareil, vérifier qu'aucune chaîne issue du catalogue de messages de bienvenue n'est rendue et qu'aucun composable porteur du test tag `welcome_message_banner` n'est présent dans l'arbre Compose de l'écran capture.

**Acceptance Scenarios** :

1. **Given** l'application ouverte (lancement à froid ou retour au premier plan), **When** l'écran capture s'affiche, **Then** aucun message d'accueil (issu de `welcome/` ou de toute autre source) n'est rendu au-dessus, en dessous, ou autour de la zone caméra.
2. **Given** l'écran capture affiché, **When** l'arbre Compose est inspecté, **Then** aucun nœud porteur du test tag `welcome_message_banner` n'est trouvé.
3. **Given** l'écran capture, **When** l'utilisatrice mesure la hauteur disponible pour l'aperçu caméra, **Then** elle constate un gain équivalent à la hauteur précédemment occupée par la bannière (≥ 1 ligne de texte `bodyLarge`).
4. **Given** un retour depuis un écran secondaire (résultat, erreur, paramètres), **When** l'écran capture réapparaît, **Then** aucune bannière d'accueil n'est rendue (cohérence avec UGE-A-FR-016).

#### US-D2 — Aucune régression sur les autres parcours (P2)

En tant qu'utilisatrice, le retrait du message d'accueil ne MUST modifier ni le comportement de la caméra, ni le bouton « Y a quoi là-dedans ? », ni l'indicateur MediaPipe, ni la navigation vers le résultat. *(Révision Feature F : le bouton « Test LLM » n'existe plus — ne pas l'exiger ici.)*

**Acceptance Scenarios** :

1. **Given** l'écran capture, **When** l'utilisatrice active « Y a quoi là-dedans ? », **Then** le flux de capture démarre comme avant (aucune régression UGE-A-FR-001..016 ou capture-recognition CR-FR-001..011, sous réserve des révocations Feature F pour le test LLM).
2. **Given** un état d'erreur (caméra indisponible, modèle absent), **When** l'écran affiche les messages d'état correspondants, **Then** aucune bannière d'accueil n'apparaît même en mode dégradé.

### Functional Requirements (Feature D)

- **UGE-D-FR-001** : Le système MUST NOT afficher de bannière de message d'accueil sur l'écran capture (= écran d'accueil) dans **aucun** état (`CameraReady`, `PreviewInitializing`, `PreviewActive`, `Capturing`, `Analyzing`, `CameraUnavailable`, `PermissionDenied`, `Empty`, `Error`, `BilanReady`, `CompositionLimit`, `CompositionAnalyzing`, `GemmaUnavailable`, `SegmentConfirmationRequired`, `Success`).
- **UGE-D-FR-002** : Le système MUST NOT exposer dans l'arbre Compose de l'écran capture un nœud porteur du test tag `welcome_message_banner` ou de toute sémantique de « message d'accueil ».
- **UGE-D-FR-003** : Le système MUST conserver, dans le code de l'application, les autres éléments structurants de l'écran capture (`MediaPipeStatusIndicator`, aperçu caméra, bande d'action avec au minimum le bouton « Y a quoi là-dedans ? ») ; les libellés secondaires d'état peuvent évoluer selon Feature F sans violer cette exigence.
- **UGE-D-FR-004** : Les tests existants nommés `US1WelcomeAfterLoginFlowTest`, `US2PositiveToneWelcomeTest`, `US3EmptyCatalogNoMessageTest` (sous `app/src/androidTest/java/com/miamia/welcome/`) sont, après audit, des tests de **logique pure** (policy / sélecteur / règles de ton) — ils n'instancient pas de `createAndroidComposeRule` ni n'asservissent l'arbre Compose. Ils ne sont **pas contradictoires** avec UGE-D-FR-001/002 et MUST être laissés inchangés. La couverture « pas de message d'accueil rendu sur l'écran capture » est portée par le nouveau test d'instrumentation dédié (cf. plan / tasks Feature D).
- **UGE-D-FR-005** : Cette évolution **rétracte** la portion d'exigence de la Feature 010 / Feature C qui imposait le rendu de la bannière d'accueil sur l'écran d'accueil ; les FRs « ton positif », « catalogue », « sélection aléatoire » restent valides en tant que politiques du package `welcome/` mais ne MUST pas être consommées par l'UI.

### Critères de succès mesurables (Feature D)

- **SC-D-001** : Sur 100 % des lancements à froid de l'application (configuration standard portrait), aucune chaîne issue du catalogue `welcome/` n'apparaît à l'écran (vérification instrumentée : `onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)`).
- **SC-D-002** : Gain d'espace vertical mesuré sur la `PreviewRegion` de l'écran capture ≥ 1 ligne `bodyLarge` (≈ 22..28 dp selon densité) par rapport à l'état avant suppression.
- **SC-D-003** : 0 régression sur les tests d'acceptation de l'écran capture pertinents après retrait (`CameraCaptureLayoutUiTest`, `CaptureActionLabelUiTest`, etc.) ; tout test nommé spécifiquement pour le bouton test LLM (`CameraUnavailableLlmButtonUiTest` ou équivalent) MUST être retiré ou réécrit lorsque Feature F est livrée.
- **SC-D-004** : Aucune référence active (non-mortelle) à `WelcomeMessageUiState` dans `app/src/main/java/com/miamia/camera/` (vérification statique : `rg "WelcomeMessageUiState|welcome_message_banner" app/src/main/java/com/miamia/camera/` ⇒ 0 occurrence).

### Cas limites

- **Catalogue welcome non vide** : peu importe le contenu du catalogue ou la politique de sélection — rien ne doit être rendu sur l'écran capture.
- **Catalogue welcome vide** : comportement déjà couvert par `US3EmptyCatalogNoMessageTest` ; aucun changement (rien à afficher de toute façon). Le test peut être conservé tel quel ou retiré selon la décision de cleanup.
- **Récupération d'erreur** (`CameraUnavailable`, `PermissionDenied`) : aucune bannière n'apparaît même en mode dégradé.
- **Rotation paysage / petit écran** : gain d'espace d'autant plus visible ; pas de comportement spécifique à orchestrer.
- **Tests legacy `welcome/`** : ils restent valides en tant que tests **de logique de sélection** (sélecteur, policy) mais ne MUST plus assert un rendu UI.

### Hypothèses (Feature D)

- Le code du package `app/src/main/java/com/miamia/welcome/` n'est pas supprimé dans cette livraison (suivi de nettoyage possible, hors scope). Seules les **références d'affichage** dans l'UI capture sont retirées.
- Le `CameraViewModel` peut continuer d'exposer `welcomeUiState` sans rupture de contrat — l'UI cesse simplement de le consommer. Un suivi peut viser à supprimer le flux entièrement.
- Aucune nouvelle exigence visuelle de remplacement (pas de slogan, pas de logo). L'écran d'accueil reste minimaliste.

### Décisions de rétrocompatibilité

- Cette feature **prévaut** sur toute exigence antérieure imposant l'affichage du message d'accueil (`010-message-bienvenue-sourire/spec.md` US1/US2 d'affichage). Une trace de cette rétraction doit apparaître dans `migration-index.md` du domaine.

---

## Feature F — Libellé caméra explicite, suppression test LLM, retrait « Aperçu caméra actif »

**Branche** : *(via hook `speckit.git.feature`)* · **Date** : 2026-05-13 · **Statut** : Draft

> Input : « Explicite le libellé "Disponible" sur la page de capture. Supprime le bouton Test LLM et la logique associée. Supprime le libellé "Apercu camera actif" de l'écran d'accueil. »

### User Scenarios (Feature F)

#### US-F1 — Comprendre l'état « caméra prête » sans jargon ambigu (P1)

En tant qu'utilisatrice, lorsque la prévisualisation caméra est opérationnelle, je veux un texte d'état qui dit clairement que je peux scanner l'étiquette, et non un mot isolé comme « Disponible » qui ne précise pas *de quoi* il s'agit.

**Pourquoi P1** : la clarté réduit l'hésitation avant la capture et l'impression d'interface « technique ».

**Test indépendant** : en état prévisualisation active, vérifier que la chaîne affichée pour l'état « prêt » contient une formulation explicite (ex. présence de « caméra » ou « prêt » + intention de scan) et n'est pas réduite au seul terme « Disponible ».

**Acceptance Scenarios** :

1. **Given** la caméra en prévisualisation active, **When** l'utilisatrice lit le statut sous la zone caméra / bande d'action, **Then** le libellé MUST communiquer sans ambiguïté que l'aperçu est prêt pour une capture (ex. « Caméra prête — vous pouvez scanner » ou formulation équivalente en français, ≤ 80 caractères).
2. **Given** le même état, **When** le texte est inspecté, **Then** il MUST NOT être exactement ni « Disponible » seul, ni une variante à un seul mot générique sans lien avec la capture.
3. **Given** les autres états transitoires (initialisation, capture en cours, traitement), **When** affichés, **Then** leurs libellés restent compréhensibles sans jargon interne non défini dans le glossaire utilisateur.

#### US-F2 — Parcours produit sans test LLM (P1)

En tant qu'utilisatrice, je ne veux plus voir ni utiliser de bouton « Test LLM » sur l'écran d'accueil ; seule l'action de scan photo mène à l'analyse.

**Acceptance Scenarios** :

1. **Given** l'écran capture affiché, **When** l'utilisatrice examine la bande d'action, **Then** aucun bouton ou lien « Test LLM » (ni libellé équivalent de démonstration) n'est visible.
2. **Given** l'application en fonctionnement, **When** un parcours utilisateur normal est suivi depuis l'accueil, **Then** aucune navigation n'est déclenchée par une action de test LLM réservée aux développeurs.
3. **Given** une analyse LLM, **When** elle est lancée, **Then** elle ne provient que du flux capture photo (ou des écrans métier ultérieurs documentés ailleurs), pas d'un raccourci de test sur l'écran d'accueil.

#### US-F3 — Écran d'accueil sans ligne « Aperçu caméra actif » (P2)

En tant qu'utilisatrice, je ne veux plus voir le libellé redondant « Aperçu caméra actif » (ou chaîne équivalente mot pour mot) sur l'écran d'accueil, la prévisualisation visuelle suffisant à l'information.

**Acceptance Scenarios** :

1. **Given** la prévisualisation visible, **When** l'écran capture est affiché, **Then** la chaîne exacte « Aperçu caméra actif » (casse et accents comme en production actuelle) n'apparaît nulle part.
2. **Given** un état sans prévisualisation (permission refusée, caméra indisponible), **When** les messages d'erreur s'affichent, **Then** aucune ligne « Aperçu caméra actif » n'est injectée dans l'UI.

### Edge Cases (Feature F)

- **Accessibilité** : le libellé explicite reste lisible avec police agrandie (troncature élégante ou multiligne dans la zone prévue).
- **Traduction future** : la règle « pas de "Disponible" seul » et « pas d'« Aperçu caméra actif » » s'applique aux chaînes équivalentes dans chaque locale (même intention).
- **Tests automatisés** : les tests qui assertaient la présence du bouton test LLM ou du libellé « Aperçu caméra actif » doivent être mis à jour ou retirés pour refléter la nouvelle vérité produit.

### Functional Requirements (Feature F)

- **UGE-F-FR-001** : Le système MUST, lorsque la prévisualisation caméra est prête pour une capture, afficher un libellé d'état en français qui décrit explicitement la disponibilité de la caméra pour scanner (intention utilisateur), et MUST NOT utiliser seul le mot « Disponible » comme seul contenu informatif de cet état.
- **UGE-F-FR-002** : Le système MUST retirer de l'interface tout bouton, menu ou raccourci « Test LLM » (ou sémantique équivalente de test/démo LLM) présent sur l'écran d'accueil / capture ; le système MUST retirer la logique produit associée (navigation, runner dédié exposé à l'utilisatrice). *(Les utilitaires purement internes ou tests instrumentés hors UI MAY rester dans le dépôt si isolés.)*
- **UGE-F-FR-003** : Le système MUST NOT afficher la chaîne « Aperçu caméra actif » sur l'écran d'accueil (= écran capture), dans aucun état d'interface.
- **UGE-F-FR-004** : Le domaine `capture-recognition` MUST aligner ses contrats UI documentés (`contracts/capture-action-bar.md`, test tags, quickstarts) sur l'absence du bouton test LLM et sur la suppression du libellé « Aperçu caméra actif » lors de l'implémentation — **Ref** propriétaire technique des tags et de la disposition de la `PreviewRegion`.

### Success Criteria (Feature F)

- **UGE-F-SC-001** : Sur 100 % des sessions observées en prévisualisation active, 0 occurrence du seul libellé « Disponible » comme unique texte d'état « prêt » ; ≥ 95 % des testeurs comprennent sans aide que la caméra est prête (enquête interne légère ou test d'utilisabilité à 5 personnes).
- **UGE-F-SC-002** : 100 % des parcours depuis l'écran d'accueil → aucun élément interactif « Test LLM » visible ni activable.
- **UGE-F-SC-003** : 100 % des inspections d'UI (manuel ou test Compose) → 0 occurrence de « Aperçu caméra actif ».

### Assumptions (Feature F)

- La formulation exacte du libellé « prêt » peut être ajustée par le design (A/B mineur) tant que UGE-F-FR-001 et UGE-F-SC-001 sont respectés.
- La suppression du test LLM n'empêche pas les tests unitaires ou d'intégration d'invoquer le moteur LLM avec des données fictives hors écran capture.

---

## Feature E — Dix phrases humoristiques supplémentaires pour les loaders

**Branche**: *(à créer via hook `speckit.git.feature`)* · **Date**: 2026-05-13 · **Statut**: Draft

> Input : « rajoute 10 messages humoristiques pour les loaders »
> Contexte : un **catalogue partagé unique** de phrases alimente déjà la rotation sur l'écran résultat (streaming) et l'écran d'attente téléchargement (UGE-B-FR-007, UGE-A-FR-006 / UGE-A-FR-017). Cette feature **enrichit** ce catalogue sans changer la cadence ni le mécanisme de rotation.

### User Scenarios (Feature E)

#### US-E1 — Plus de variété pendant l'attente (P1)

En tant qu'utilisatrice en attente (téléchargement du modèle ou analyse LLM), je veux que les phrases humoristiques qui défilent incluent **dix formulations nouvelles**, dans le même esprit léger et lié à l'alimentation / la lecture d'étiquette, afin de réduire la répétition sur les longues attentes.

**Pourquoi P1** : la valeur est entièrement portée par le contenu textuel ; sans nouvelles entrées, l'objectif produit n'est pas atteint.

**Test indépendant** : vérifier que le catalogue source de vérité des phrases d'attente contient au moins dix chaînes nouvelles par rapport à la baseline documentée au 2026-05-13 (onze phrases existantes listées dans la spécification / code), qu'elles sont en français, et qu'aucune n'est un doublon exact d'une entrée existante.

**Acceptance Scenarios** :

1. **Given** l'écran d'attente téléchargement ou l'état loader du streaming résultat, **When** les phrases défilent, **Then** le pool inclut les onze formulations historiques plus **dix** nouvelles distinctes.
2. **Given** le catalogue mis à jour, **When** on compare texte à texte avec la baseline, **Then** exactement dix entrées sont nouvelles (pas de simple reformulation identique caractère pour caractère d'une phrase existante).
3. **Given** une session d'attente longue, **When** plusieurs rotations se succèdent, **Then** le ton reste humoristique, accessible, sans insulte ni stéréotype de population, et cohérent avec les messages déjà admis (cf. Feature A/B).

#### Edge Cases (Feature E)

- **Longueur à l'écran** : chaque nouvelle phrase MUST tenir dans la zone d'affichage actuelle (pas de débordement sur petit écran portrait) — même contrainte implicite que pour les entrées existantes.
- **Caractères spéciaux** : éviter les glyphes rares non supportés par la police courante ; apostrophes typographiques acceptées si déjà utilisées ailleurs.
- **Traduction** : hors scope — uniquement français.

### Functional Requirements (Feature E)

- **UGE-E-FR-001** : Le système MUST enrichir le catalogue partagé des phrases d'attente humoristiques de **exactement dix** nouvelles entrées textuelles, en **plus** des formulations déjà présentes au 2026-05-13.
- **UGE-E-FR-002** : Les dix nouvelles phrases MUST être rédigées en français et MUST respecter le même registre humoristique « léger, alimentaire / étiquette » que le catalogue existant.
- **UGE-E-FR-003** : Les dix nouvelles phrases MUST NOT être des doublons exacts d'une entrée déjà présente dans le catalogue avant enrichissement (même texte après suppression des espaces en tête et fin de chaîne).
- **UGE-E-FR-004** : Les dix nouvelles phrases MUST rester lisibles dans les mêmes zones d'affichage que les phrases existantes ; la cadence et l'ordre aléatoire de rotation restent ceux déjà exigés (UGE-B-FR-007, Feature A).
- **UGE-E-FR-005** : Le contenu MUST éviter tout propos discriminatoire, dénigrant envers une population, ou tout humour « corps / poids » ciblant l'utilisatrice de façon personnelle.

### Annexe — Proposition de textes (livraison cible)

Les formulations suivantes satisfont UGE-E-FR-002 à UGE-E-FR-005 et sont distinctes des onze messages historiques ; l'implémentation MAY ajuster ponctuation ou casse mineure tant que le sens et l'humour restent équivalents.

1. Nos algorithmes goûtent virtuellement chaque ligne… le verdict arrive.
2. Les conservateurs jouent à cache-cache ; on les débusque.
3. Prise de pouls de votre tableau nutritionnel, un instant.
4. Les lipides préparent leur plaidoirie ; le juge délibère.
5. Recensement des sucres qui prétendent être naturels…
6. Lecture entre les lignes des tout petits caractères en bas d'étiquette…
7. Arbitrage tendu entre « bon pour la ligne » et « irrésistible ».
8. Les protéines s'étirent pendant qu'on compte les grammes.
9. Vérification que le « sans gluten » ne cache pas d'autres surprises…
10. La casserole des données mijote à feu doux, encore un peu de patience.

### Success Criteria (Feature E)

- **SC-E-001** : 100 % des parcours utilisant le catalogue (téléchargement + loader résultat) exposent un pool d'au moins **21** phrases distinctes après livraison.
- **SC-E-002** : Revue éditoriale interne (ou équivalent produit) valide les dix nouvelles entrées comme conformes à UGE-E-FR-002 et UGE-E-FR-005 (trace : commentaire de revue ou entrée changelog).
- **SC-E-003** : Aucune régression sur la rotation toutes les 5 s (téléchargement) ni sur le comportement de rotation du loader streaming (Feature A/B).

### Hypothèses (Feature E)

- Aucun changement de produit sur la cadence de rotation ni sur la liste partagée entre les deux écrans (décision historique conservée).
- Les onze phrases existantes au 2026-05-13 restent en place ; seules des **ajouts** sont demandés.

---

## Cross-domain Notes

- Le segment ingrédients n'est pas déterminé ici : délégation à `ingredient-normalization-validation`.
- L'analyse de composition et la critique santé sont du ressort de `ingredient-health-intelligence`.
- Le runtime LLM local (chargement modèle, inférence) est du ressort de `local-llm-runtime`.
- La capture OCR est du ressort de `capture-recognition`.
- Les KPI additifs sont du ressort de `additive-risk-insights`.

## Source Mapping

- `specs/017-photo-analyse-ecran-resultat/` (Feature A)
- `specs/018-llm-download-onboarding/` (Feature B)
- `specs/012-home-layout-mediapipe-status/` (Feature C — home)

## Assumptions

- L'écran de capture est l'écran d'accueil ; pas de page d'accueil distincte à onglets.
- Le flux de capture photo reste la référence fonctionnelle pour l'entrée utilisateur vers l'analyse (le test LLM UI est retiré — Feature F).
- La nature du résultat exploitable relève des domaines d'analyse ; cet écran se contente d'une présentation lisible.
- Le parcours reste local (pas de dépendance réseau pour le flux de base, sauf téléchargement modèle).
- Le modèle LLM est obligatoire ; l'app ne fonctionne pas sans.
- Le texte OCR validé est le contenu de repli quand l'analyse échoue.
