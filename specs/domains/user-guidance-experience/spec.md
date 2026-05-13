# Domain Spec — user-guidance-experience

**Domain Context**: `user-guidance-experience`
**Created**: 2026-05-06
**Last Modified**: 2026-05-12 (sync-apply consolidation)
**Status**: Draft

## Purpose

Orchestrer l'expérience utilisatrice de bout en bout : onboarding (téléchargement du modèle), capture photo, analyse LLM avec streaming progressif, affichage du résultat, et messages de bienvenue. Ce domaine ne possède pas la logique d'analyse elle-même (déléguée à `ingredient-health-intelligence` et `ingredient-normalization-validation`) mais contrôle la navigation, le feedback visuel et les états utilisateur.

## Scope

- Onboarding téléchargement du modèle LLM (confirmation, attente, reprise, refus)
- Écran de capture comme accueil (sans onglets)
- Flux photo → streaming analyse → écran résultat
- Flux test LLM bouchonné (même parcours)
- Gestion des indisponibilités (caméra, modèle, réseau)
- Messages de bienvenue et ton positif
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
> Évolutions : suppression des onglets + éviter l'écran vide "Aucun contenu à afficher"

### Clarifications (Feature A)

#### Session 2026-05-06

- Q: Où afficher le loader après une photo ? → A: Option A — indicateur sur l'écran de capture puis navigation résultat après état terminal. *(Note : le code a évolué vers streaming progressif directement sur l'écran résultat — validé par Backfill P1, 2026-05-12.)*
- Q: Comportement si l'utilisatrice quitte l'écran pendant le chargement ? → A: Aucune navigation automatique vers l'écran résultat.

#### Session 2026-05-06 (shell d'application)

- Décision produit : l'application démarre directement sur l'écran de prise de photo ; pas de barre d'onglets.

### User Scenarios (Feature A)

#### US-A1 — Disposer de l'écran de capture comme accueil, sans onglets (P1)

En tant qu'utilisatrice, je veux que le premier écran soit l'écran de prise de photo — prévisualisation, bouton photo, bouton test LLM — sans barre d'onglets.

**Acceptance Scenarios**:

1. **Given** l'application ouverte, **When** l'interface principale apparaît, **Then** l'écran de prise de photo est affiché sans passage par un écran d'accueil à onglets.
2. **Given** l'application au premier plan, **When** l'utilisatrice observe la navigation, **Then** aucune barre d'onglets multi-sections n'est présentée.
3. **Given** la caméra disponible, **When** l'écran s'affiche, **Then** la prévisualisation est visible et le bouton photo est placé sous cette zone.
4. **Given** l'écran affiché, **When** l'utilisatrice parcourt les actions, **Then** le bouton test LLM est visible directement sous le bouton photo.
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

#### US-A3 — Lancer le test LLM depuis le même écran (P2)

En tant qu'utilisatrice, je veux déclencher le test LLM depuis le bouton sous le bouton photo, avec le même flux streaming vers l'écran résultat.

**Acceptance Scenarios**:

1. **Given** l'écran de capture visible, **When** test LLM activé, **Then** navigation immédiate vers l'écran résultat avec streaming progressif (même flux que photo). *(Backfill P1.)*
2. **Given** test LLM en cours, **When** état actif, **Then** le bouton n'accepte pas de nouvelle exécution.
3. **Given** le test terminé, **When** résultat ou erreur disponible, **Then** affichage cohérent avec le parcours photo.
4. **Given** streaming en cours, **When** l'utilisatrice quitte l'écran résultat, **Then** aucune navigation automatique de retour.

#### US-A4 — Comprendre les indisponibilités (P3)

En tant qu'utilisatrice, je veux un message clair si la caméra est indisponible, tout en gardant accès au test LLM.

**Acceptance Scenarios**:

1. **Given** caméra indisponible, **When** écran capture affiché, **Then** message explicite remplace la prévisualisation.
2. **Given** caméra redevient disponible, **When** état mis à jour, **Then** la prévisualisation peut réapparaître.

#### Edge Cases (Feature A)

- Clics rapides répétés sur photo ou test LLM pendant un chargement.
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
- **UGE-A-FR-005**: Le système MUST afficher directement sous le bouton photo un bouton de test LLM distinct.
- **UGE-A-FR-006**: Le système MUST, après capture photo et lancement de l'analyse LLM, naviguer vers l'écran de résultat dédié et y afficher un indicateur de chargement (fouet animé, phrases humoristiques rotatives) suivi du streaming progressif des sections d'analyse (produit identifié, ingrédients, synthèse, impacts santé) au fur et à mesure de leur disponibilité. *(Backfill P1 — 2026-05-12.)*
- **UGE-A-FR-007**: Le système MUST, lorsque l'analyse LLM se termine avec succès et que l'utilisatrice est sur l'écran de résultat, afficher le bilan complet. En cas d'échec, afficher un état d'erreur actionnable sur le même écran. *(Backfill P1 — 2026-05-12.)*
- **UGE-A-FR-008**: Le système MUST réutiliser le runner de test LLM existant pour le bouton test LLM.
- **UGE-A-FR-009**: Le système MUST désactiver le bouton test LLM pendant toute exécution confirmée en cours.
- **UGE-A-FR-010**: Le système MUST afficher un message explicite sur l'écran de résultat en cas d'échec d'analyse ou de test LLM.
- **UGE-A-FR-011**: Le système MUST afficher un message explicite à la place de la prévisualisation lorsque la caméra est indisponible.
- **UGE-A-FR-012**: Le système MUST permettre une nouvelle capture ou un nouveau test après la fin du cycle en cours.
- **UGE-A-FR-013**: Le système MUST, pour le parcours test LLM, appliquer le même flux que UGE-A-FR-006 : navigation immédiate vers l'écran de résultat avec streaming progressif. *(Backfill P1 — 2026-05-12.)*
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
- **LlmTestIntent**: Action sur le bouton test LLM.
- **LlmProcessingState**: `inactif`, `en_cours` (streaming progressif sur l'écran de résultat dédié), `termine_succès`, `termine_échec`.
- **LlmResultPresentation**: Contenu sur l'écran résultat (sortie principale, erreur, repli OCR).
- **ResultContentViewport**: Zone de lecture pour contenu long avec accès aux contrôles en dessous.

### Success Criteria (Feature A)

- **SC-A-001**: ≥ 95 % des ouvertures avec caméra disponible → prévisualisation visible en < 2 s.
- **SC-A-002**: 100 % des appuis photo → flux de capture déclenché.
- **SC-A-003**: 100 % des lancements d'analyse → indicateur de chargement visible en < 1 s.
- **SC-A-004**: ≥ 95 % des analyses réussies → résultat accessible en < 30 s après capture.
- **SC-A-005**: 100 % des appuis test LLM pendant exécution en cours → pas d'exécution concurrente.
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
- `HomeLlmMockRunner` : utilisé activement par le parcours test LLM (→ à conserver)
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

En tant qu'utilisatrice, le retrait du message d'accueil ne MUST modifier ni le comportement de la caméra, ni le bouton « Y a quoi là-dedans ? », ni le bouton « Test LLM », ni l'indicateur MediaPipe, ni la navigation vers le résultat.

**Acceptance Scenarios** :

1. **Given** l'écran capture, **When** l'utilisatrice active « Y a quoi là-dedans ? », **Then** le flux de capture démarre comme avant (aucune régression UGE-A-FR-001..016 ou capture-recognition CR-FR-001..011).
2. **Given** l'écran capture, **When** l'utilisatrice active « Test LLM », **Then** le flux test LLM démarre comme avant.
3. **Given** un état d'erreur (caméra indisponible, modèle absent), **When** l'écran affiche les messages d'état correspondants, **Then** aucune bannière d'accueil n'apparaît même en mode dégradé.

### Functional Requirements (Feature D)

- **UGE-D-FR-001** : Le système MUST NOT afficher de bannière de message d'accueil sur l'écran capture (= écran d'accueil) dans **aucun** état (`CameraReady`, `PreviewInitializing`, `PreviewActive`, `Capturing`, `Analyzing`, `CameraUnavailable`, `PermissionDenied`, `Empty`, `Error`, `BilanReady`, `CompositionLimit`, `CompositionAnalyzing`, `GemmaUnavailable`, `SegmentConfirmationRequired`, `Success`).
- **UGE-D-FR-002** : Le système MUST NOT exposer dans l'arbre Compose de l'écran capture un nœud porteur du test tag `welcome_message_banner` ou de toute sémantique de « message d'accueil ».
- **UGE-D-FR-003** : Le système MUST conserver, dans le code de l'application, les autres éléments de l'écran capture (`MediaPipeStatusIndicator`, aperçu caméra, `CaptureActionBar`) à l'identique de leur définition courante (aucune régression de structure imposée par cette évolution).
- **UGE-D-FR-004** : Les tests existants nommés `US1WelcomeAfterLoginFlowTest`, `US2PositiveToneWelcomeTest`, `US3EmptyCatalogNoMessageTest` (sous `app/src/androidTest/java/com/miamia/welcome/`) sont, après audit, des tests de **logique pure** (policy / sélecteur / règles de ton) — ils n'instancient pas de `createAndroidComposeRule` ni n'asservissent l'arbre Compose. Ils ne sont **pas contradictoires** avec UGE-D-FR-001/002 et MUST être laissés inchangés. La couverture « pas de message d'accueil rendu sur l'écran capture » est portée par le nouveau test d'instrumentation dédié (cf. plan / tasks Feature D).
- **UGE-D-FR-005** : Cette évolution **rétracte** la portion d'exigence de la Feature 010 / Feature C qui imposait le rendu de la bannière d'accueil sur l'écran d'accueil ; les FRs « ton positif », « catalogue », « sélection aléatoire » restent valides en tant que politiques du package `welcome/` mais ne MUST pas être consommées par l'UI.

### Critères de succès mesurables (Feature D)

- **SC-D-001** : Sur 100 % des lancements à froid de l'application (configuration standard portrait), aucune chaîne issue du catalogue `welcome/` n'apparaît à l'écran (vérification instrumentée : `onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)`).
- **SC-D-002** : Gain d'espace vertical mesuré sur la `PreviewRegion` de l'écran capture ≥ 1 ligne `bodyLarge` (≈ 22..28 dp selon densité) par rapport à l'état avant suppression.
- **SC-D-003** : 0 régression sur les tests d'acceptation de l'écran capture (`CameraCaptureLayoutUiTest`, `CameraUnavailableLlmButtonUiTest`, `CaptureActionLabelUiTest`) après retrait.
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
- Le flux de capture photo et le runner de test LLM restent la référence fonctionnelle.
- La nature du résultat exploitable relève des domaines d'analyse ; cet écran se contente d'une présentation lisible.
- Le parcours reste local (pas de dépendance réseau pour le flux de base, sauf téléchargement modèle).
- Le modèle LLM est obligatoire ; l'app ne fonctionne pas sans.
- Le texte OCR validé est le contenu de repli quand l'analyse échoue.
