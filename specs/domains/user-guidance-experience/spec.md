# Domain Spec — user-guidance-experience

**Domain Context**: `user-guidance-experience`
**Created**: 2026-05-06
**Last Modified**: 2026-06-28 (Feature I — sélection du profil utilisateur sur l'écran de capture, défaut Adulte, requise avant photo)
**Status**: Draft

**Dernière entrée utilisateur (intake)** : « la sélection du profil de l'utilisateur doit être faite sur l'écran de prise de photo, elle est réglée par défaut sur "Adulte" et doit être renseignée avant la prise de photo »

## Purpose

Orchestrer l'expérience utilisatrice de bout en bout : moment de marque au lancement (splash), onboarding (téléchargement du modèle), capture photo, analyse LLM avec streaming progressif, affichage du résultat, et messages de bienvenue. Ce domaine ne possède pas la logique d'analyse elle-même (déléguée à `ingredient-health-intelligence` et `ingredient-normalization-validation`) mais contrôle la navigation, le feedback visuel et les états utilisateur.

## Scope

- Écran splash de lancement (marque, courte durée) avant le premier écran applicatif
- Onboarding téléchargement du modèle LLM (confirmation, attente, reprise, refus)
- Écran de capture comme accueil (sans onglets)
- Flux photo → analyse LLM (sans ecran intermediaire de relecture du transcript OCR) → streaming / ecran resultat
- Gestion des indisponibilités (caméra, modèle, réseau)
- Messages de bienvenue et ton positif
- Catalogue partagé de phrases humoristiques pour écrans d'attente (téléchargement modèle, analyse LLM)
- Évaluation du code legacy home screen

## Invariants

- Au lancement à froid, un splash de marque de courte durée précède le premier écran applicatif (sauf adaptation liée aux préférences utilisateur de réduction du mouvement — Feature H).
- L'écran de capture est toujours le point d'entrée après onboarding (et après le splash lorsqu'il est affiché).
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

En tant qu'utilisatrice, je veux que le premier écran **fonctionnel** soit l'écran de prise de photo — prévisualisation, bouton photo — sans barre d'onglets ni action de test LLM, **une fois** tout splash de lancement (Feature H) et, le cas échéant, le parcours d'onboarding modèle (Feature B) terminés.

**Acceptance Scenarios**:

1. **Given** l'application ouverte jusqu'à la fin du splash et de l'onboarding modèle si requis, **When** l'interface principale applicative apparaît, **Then** l'écran de prise de photo est affiché sans passage par un écran d'accueil à onglets.
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
8. **Given** bilan de composition classé succès par `ingredient-health-intelligence` et une estimation d’énergie pour 100 g **disponible** selon les garde-fous de ce domaine (**Feature K**, **IHI-K-FR-001**), **When** l’écran de résultat affiche le bilan complet, **Then** une pastille en **tête** d’écran respecte **UGE-A-FR-022** (libellé d’analyse terminée, valeur ou absence sûre, caractère **estimé** visible — **Ref.** **IHI-K-FR-002**, **IHI-K-FR-003**).

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
- Estimation kcal/100 g absente ou rejetée par garde-fous : pastille cohérente sans nombre trompeur (**Ref.** `ingredient-health-intelligence` **US-K2**).

### Functional Requirements (Feature A)

- **UGE-A-FR-001**: Le système MUST présenter un écran de prise de photo comme premier écran **fonctionnel** affiché après lancement normal, **après** tout écran splash de lancement (Feature H) et **après** le parcours d'onboarding téléchargement du modèle lorsqu'il s'applique (Feature B) ; hors ces phases transitoires, l'écran d'accueil = écran de capture.
- **UGE-A-FR-002**: Le système MUST afficher une prévisualisation caméra réelle lorsque la caméra est disponible.
- **UGE-A-FR-003**: Le système MUST permettre un contrôle de mise au point sur la prévisualisation.
- **UGE-A-FR-004**: Le système MUST afficher sous la prévisualisation un bouton de prise de photo qui lance le flux existant de capture.
- **UGE-A-FR-005** *(révoqué — Feature F)*: ~~bouton test LLM~~ — voir UGE-F-FR-002.
- **UGE-A-FR-006**: Le système MUST, après capture photo et lancement de l'analyse LLM, naviguer vers l'écran de résultat dédié et y afficher un indicateur de chargement (fouet animé, phrases humoristiques rotatives) suivi du streaming progressif des sections d'analyse (produit identifié, ingrédients, synthèse, impacts santé) au fur et à mesure de leur disponibilité, **sans** interposer au préalable un écran de relecture du texte OCR (**Ref.** Feature G, UGE-G-FR-001). *(Backfill P1 — 2026-05-12.)*
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
- **UGE-A-FR-022**: Lorsque le bilan de composition fourni par `ingredient-health-intelligence` est classé **succès**, le système MUST afficher en **tête** de l’écran de résultat / synthèse une **pastille** (ou bandeau équivalent visuellement prioritaire) conforme aux exigences **IHI-K-FR-001** à **IHI-K-FR-003** : état d’analyse terminée, estimation d’énergie en **kcal pour 100 g** lorsqu’elle est **disponible** côté domaine d’analyse, qualification **estimée / indicative**, et **aucune** présentation comme donnée nutritionnelle réglementaire ou certifiée. Lorsque l’estimation n’est pas disponible ou n’est pas fiable, le système MUST respecter **IHI-K-FR-004** / **US-K2** (pas de chiffre inventé ; libellé d’indisponibilité ou pastille sans valeur numérique trompeuse, selon design validé). La pastille MUST respecter **contraste** et **taille de texte** au moins équivalents aux autres bandeaux d’état du haut de l’écran résultat (détails de conformité accessibilité documentés en plan d’implémentation).

### Key Entities (Feature A)

- **AppNavigationShell**: Navigation sans onglets ; après splash (Feature H) et onboarding modèle si requis (Feature B), premier écran fonctionnel = capture ; empilement simple.
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

- **UGE-D-FR-001** : Le système MUST NOT afficher de bannière de message d'accueil sur l'écran capture (= écran d'accueil) dans **aucun** état (`CameraReady`, `PreviewInitializing`, `PreviewActive`, `Capturing`, `Analyzing`, `CameraUnavailable`, `PermissionDenied`, `Empty`, `Error`, `BilanReady`, `CompositionLimit`, `CompositionAnalyzing`, `GemmaUnavailable`, `Success`).
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

#### US-F1 — ~~Libellé « caméra prête » sous l'aperçu (P1)~~ *(révoqué — Feature G, 2026-05-13)*

*L'exigence d'une ligne explicite sous la prévisualisation (ex. « Caméra prête — vous pouvez scanner ») est **abrogée**. Voir **US-G2** et **UGE-G-FR-003**.*

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

- **Accessibilité** : le bouton principal de capture et les zones d'état utiles (erreurs, chargement) restent lisibles avec police agrandie.
- **Traduction future** : la règle « pas de "Disponible" seul » et « pas d'« Aperçu caméra actif » » s'applique aux chaînes équivalentes dans chaque locale (même intention).
- **Tests automatisés** : les tests qui assertaient la présence du bouton test LLM ou du libellé « Aperçu caméra actif » doivent être mis à jour ou retirés pour refléter la nouvelle vérité produit.

### Functional Requirements (Feature F)

- **UGE-F-FR-001** *(révoqué — Feature G, 2026-05-13)* : ~~Le système MUST, lorsque la prévisualisation caméra est prête pour une capture, afficher un libellé d'état en français qui décrit explicitement la disponibilité de la caméra pour scanner~~ — **remplacé par** **UGE-G-FR-003** (aucune ligne d'invitation imposée ; chaîne « Caméra prête — vous pouvez scanner » interdite).
- **UGE-F-FR-002** : Le système MUST retirer de l'interface tout bouton, menu ou raccourci « Test LLM » (ou sémantique équivalente de test/démo LLM) présent sur l'écran d'accueil / capture ; le système MUST retirer la logique produit associée (navigation, runner dédié exposé à l'utilisatrice). *(Les utilitaires purement internes ou tests instrumentés hors UI MAY rester dans le dépôt si isolés.)*
- **UGE-F-FR-003** : Le système MUST NOT afficher la chaîne « Aperçu caméra actif » sur l'écran d'accueil (= écran capture), dans aucun état d'interface.
- **UGE-F-FR-004** : Le domaine `capture-recognition` MUST aligner ses contrats UI documentés (`contracts/capture-action-bar.md`, test tags, quickstarts) sur l'absence du bouton test LLM et sur la suppression du libellé « Aperçu caméra actif » lors de l'implémentation — **Ref** propriétaire technique des tags et de la disposition de la `PreviewRegion`.

### Success Criteria (Feature F)

- **UGE-F-SC-001** *(révoqué — Feature G, 2026-05-13)* : ~~Sur 100 % des sessions observées en prévisualisation active, 0 occurrence du seul libellé « Disponible »~~ — le critère de « libellé prêt obligatoire » ne s'applique plus ; **UGE-G-SC-002** couvre l'absence de la ligne marketing interdite et du chip balise.
- **UGE-F-SC-002** : 100 % des parcours depuis l'écran d'accueil → aucun élément interactif « Test LLM » visible ni activable.
- **UGE-F-SC-003** : 100 % des inspections d'UI (manuel ou test Compose) → 0 occurrence de « Aperçu caméra actif ».

### Assumptions (Feature F)

- ~~La formulation exacte du libellé « prêt » peut être ajustée par le design (A/B mineur) tant que UGE-F-FR-001 et UGE-F-SC-001 sont respectés.~~ *(Assumption révoquée — Feature G : pas de libellé d'invitation imposé sous l'aperçu.)*
- La suppression du test LLM n'empêche pas les tests unitaires ou d'intégration d'invoquer le moteur LLM avec des données fictives hors écran capture.

---

## Feature G — OCR direct, sans écran intermédiaire, accueil sans chip ni statut « prêt à scanner »

**Branche** : *(via hook `speckit.git.feature` si utilisé)* · **Date** : 2026-05-13 · **Statut** : Draft

> Input : « L'analyse doit se déclencher directement à partir de l'OCR, sur la base de la totalité du texte capturé. Je ne veux plus voir l'écran intermédiaire qui affiche le texte capturé. Supprime toute la logique de segmentation de la liste des ingrédients. Supprime aussi de l'écran d'accueil, la pastille balise ingrédients et le texte caméra prête vous pouvez scanner. »

### Clarifications (session 2026-05-13)

- **Révocation partielle de Feature F** : les scénarios US-F1 et les exigences **UGE-F-FR-001** / **UGE-F-SC-001** qui imposaient une ligne de statut explicite (« Caméra prête — vous pouvez scanner » ou équivalent) sous la prévisualisation sont **abrogés** pour l'écran accueil (= capture). L'état « prêt » est désormais **purement visuel** (flux vidéo + bouton principal de capture) ; **aucune** phrase marketing ou pédagogique obligatoire ne doit occuper la zone sous l'aperçu pour signifier « prêt ».
- **Segmentation** : la fin de toute logique d'isolation / ancrage / proposition de segment ingrédients et des validations associées **avant** l'analyse LLM relève du domaine **`ingredient-normalization-validation`** (spec et code) ; le présent document fixe les **attendus UX et de navigation** correspondants (pas d'écran intermédiaire de relecture, pas de chrome « balise » sur l'accueil).

### User Scenarios (Feature G)

#### US-G1 — Enchaînement direct OCR → analyse (P1)

En tant qu'utilisatrice, après une capture et une reconnaissance réussies avec un texte exploitable, je veux que l'application enchaîne vers l'analyse (indicateur de chargement / navigation vers le résultat) **sans** m'obliger à passer par un écran intermédiaire dont le rôle principal est d'afficher le texte capturé pour relecture ou confirmation.

**Acceptance Scenarios** :

1. **Given** un résultat OCR `success` ou `partial` avec transcript non vide admissible pour l'analyse, **When** le flux se poursuit, **Then** aucun écran ou feuille intermédiaire dont l'objet principal est la relecture du transcript ne s'intercale avant le démarrage de l'analyse LLM ou l'affichage du chargement résultat.
2. **Given** le même contexte, **When** l'analyse est invoquée, **Then** l'entrée transmise au parcours d'analyse est l'intégralité du texte capturé disponible pour la session (**Ref.** FR-012 domaine `ingredient-normalization-validation`).

#### US-G2 — Accueil sans pastille « balise ingrédients » ni ligne « Caméra prête — vous pouvez scanner » (P1)

En tant qu'utilisatrice sur l'écran d'accueil (= écran de capture), je ne veux **ni** pastille, chip ou interrupteur « balise ingrédients » (ou libellé fonctionnellement équivalent), **ni** la ligne de texte « Caméra prête — vous pouvez scanner » (casse et tiret comme ci-dessus) ni une formulation imposée au même emplacement pour le même rôle d'« invitation à scanner ».

**Acceptance Scenarios** :

1. **Given** l'écran capture affiché en état prêt à photographier, **When** l'utilisatrice examine la zone sous la prévisualisation (hors bande d'action principale et hors indicateurs techniques déjà prévus, ex. disponibilité moteur), **Then** aucun contrôle « balise ingrédients » n'est visible.
2. **Given** le même écran, **When** le texte sous l'aperçu est inspecté, **Then** la chaîne exacte « Caméra prête — vous pouvez scanner » n'apparaît pas.
3. **Given** les états transitoires (`PreviewInitializing`, `Capturing`, `Analyzing`), **When** affichés, **Then** aucune réintroduction de la pastille balise ni de la chaîne interdite n'est faite à la place des messages d'état utiles déjà prévus ailleurs (ex. traitement en cours).

### Edge Cases (Feature G)

- **OCR vide ou non exploitable** : un message d'erreur ou de reprise reste autorisé ; il ne constitue pas un « écran de relecture » du transcript.
- **Indicateur technique** (ex. disponibilité du modèle / MediaPipe) : peut rester distinct de la zone visée par US-G2 tant qu'il n'est pas substitut d'une pastille « balise » ni de la ligne de statut marketing révoquée.
- **Accessibilité** : la suppression des libellés marketing ne dispense pas d'alternatives accessibles sur le bouton d'action principal (libellé existant « Y a quoi là-dedans ? » ou évolution ultérieure documentée).

### Functional Requirements (Feature G)

- **UGE-G-FR-001** : Le système MUST, après capture et reconnaissance aboutissant à un transcript utilisable pour l'analyse, enchaîner vers le parcours d'analyse LLM **sans** présenter d'étape utilisateur dont la fonction première est la consultation ou la validation du transcript avant analyse.
- **UGE-G-FR-002** : Le système MUST NOT afficher sur l'écran d'accueil (= écran capture) de pastille, chip, interrupteur ou entrée de menu « balise ingrédients » (ou libellé sémantiquement équivalent d'intention de cadrage « ingrédients seuls »).
- **UGE-G-FR-003** : Le système MUST NOT afficher sur l'écran d'accueil la chaîne exacte « Caméra prête — vous pouvez scanner » ; le système MUST NOT exiger une ligne de texte d'invitation à scanner sous la prévisualisation pour l'état « prêt » (la prévisualisation live et le bouton principal suffisent).
- **UGE-G-FR-004** : Le système MUST retirer de l'expérience produit tout écran intermédiaire équivalent historique à `SegmentConfirmationRequired` pour le parcours nominal de scan vers analyse (**Ref.** implémentation `ingredient-normalization-validation` + module capture).

### Success Criteria (Feature G)

- **UGE-G-SC-001** : 100 % des parcours nominaux capture → OCR réussi → analyse : 0 affichage d'un écran intermédiaire de relecture du transcript (vérification manuelle ou tests d'UI ciblés).
- **UGE-G-SC-002** : 100 % des inspections d'UI sur l'écran accueil capture en état prêt : 0 chip « balise ingrédients » (ou équivalent) ; 0 occurrence de la chaîne « Caméra prête — vous pouvez scanner ».

### Assumptions (Feature G)

- Le bouton principal de capture et l'indicateur de disponibilité du moteur / pipeline restent les leviers d'action et de confiance technique ; leur évolution de libellé hors périmètre de la phrase interdite reste libre tant que UGE-G-FR-003 est respecté.
- La mise en œuvre technique de la suppression de code (segmentation, gate, états) est répartie entre modules capture et domaine ingrédients ; la présente spec reste la source de vérité **comportementale** vis-à-vis de l'utilisatrice.

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

## Feature H — Splash de lancement (marque MiamIA, marmite, pastel)

> Input (intake 2026-05-13) : écran splash de quelques secondes avant le premier écran applicatif ; illustration marmite au style visuel volontairement simplifié et cartoonesque (référence produit : esthétique proche du dessin animé *South Park*) ; libellé « MiamIA » avec une typographie du même esprit ; couleurs pastels alignées sur le thème visuel de l'application.

### User Scenarios (Feature H)

#### US-H1 — Voir la marque au démarrage (P1)

En tant qu'utilisatrice, au lancement à froid de l'application, je veux voir un écran plein dédié à la marque pendant une courte durée avant d'accéder au reste du parcours (onboarding modèle ou écran de capture selon les règles existantes).

**Acceptance Scenarios**:

1. **Given** un lancement à froid de l'application, **When** l'interface apparaît, **Then** un écran splash plein écran s'affiche en premier, avant tout écran de capture ou d'onboarding modèle.
2. **Given** le splash affiché, **When** la durée prévue s'écoule, **Then** l'application enchaîne automatiquement vers le prochain écran du parcours nominal (Feature B si le modèle n'est pas prêt, sinon écran de capture — cohérent avec Feature A).
3. **Given** le splash affiché, **When** l'utilisatrice l'observe, **Then** elle y distingue clairement une illustration centrale de marmite et le texte de marque « MiamIA ».
4. **Given** le splash affiché, **When** l'utilisatrice compare les couleurs au reste de l'application, **Then** la palette est cohérente avec le thème pastel existant (pas de rupture chromatique brutale avec l'accueil capture).

#### US-H2 — Lire une identité visuelle décalée et lisible (P2)

En tant qu'utilisatrice, je veux que le splash soit visuellement marquant et lisible : traits simples, aplats, contours nets pour la marmite, et un titre « MiamIA » au caractère informel et robuste évoquant l'esthétique des génériques du dessin animé de référence, sans nuire à la lisibilité du nom de l'app.

**Acceptance Scenarios**:

1. **Given** le splash affiché, **When** l'utilisatrice lit le nom « MiamIA », **Then** les lettres restent identifiables sur téléphone en portrait à distance de lecture normale.
2. **Given** le splash affiché, **When** l'utilisatrice regarde l'illustration marmite, **Then** celle-ci est reconnaissable comme marmite (couvercle, corps, anses ou équivalent stylisé) et adopte une esthétique volontairement « cut-out » / cartoonesque plutôt que photoréaliste.

### Edge Cases (Feature H)

- Relance de l'app depuis le multitâche (app déjà en mémoire) : le splash ne s'impose pas à chaque retour au premier plan ; il s'applique au lancement à froid (définition : session applicative démarrée depuis zéro après fermeture ou eviction mémoire — formulation vérifiable par parcours « tuer l'app puis rouvrir »).
- Préférence utilisateur de réduction des animations / mouvement : le splash MUST éviter les effets de mouvement agressifs ; la durée d'exposition MAY être réduite ou l'écran simplifié tant que la marque reste identifiable (détail exact laissé au design d'accessibilité, borne : pas plus long que le parcours nominal sans préférence).
- Écran très petit ou grand facteur d'échelle : le logo texte et la marmite restent entièrement visibles sans recadrage tronquant le nom « MiamIA ».
- Thème clair / sombre si l'application en propose : le splash reste harmonisé pastel dans chaque variante sans perdre le contraste minimal pour la lisibilité.

### Functional Requirements (Feature H)

- **UGE-H-FR-001** : Le système MUST, sur lancement à froid, afficher un écran splash plein écran avant le premier écran fonctionnel (capture ou onboarding modèle).
- **UGE-H-FR-002** : Le splash MUST rester affiché pendant une durée brève et bornée (cible produit : entre **2 s** et **4 s** inclusivement sur parcours nominal sans préférence de réduction du mouvement), puis céder la place automatiquement sans action utilisateur obligatoire.
- **UGE-H-FR-003** : Le splash MUST inclure une illustration principale représentant une marmite, stylisée selon une esthétique simple, plate ou quasi plate, à contours marqués, évoquant l'animation américaine *South Park* (cut-out, humour visuel léger).
- **UGE-H-FR-004** : Le splash MUST afficher le nom de marque « MiamIA » avec une typographie assortie à la même esthétique (lettres pleines, informelles, lisibles).
- **UGE-H-FR-005** : Le splash MUST utiliser une palette de couleurs pastels cohérente avec le thème visuel global de l'application.
- **UGE-H-FR-006** : Le système MUST NOT exiger d'interaction (bouton « Continuer ») pour quitter le splash sur le parcours nominal ; la transition est automatique à l'issue du temporisage.
- **UGE-H-FR-007** : Le système MUST, lorsque l'application revient simplement au premier plan sans redémarrage de session, ne pas réinsérer le splash comme s'il s'agissait d'un nouveau lancement à froid.

### Success Criteria (Feature H)

- **UGE-H-SC-001** : Sur trois lancements à froid consécutifs observés, le splash apparaît systématiquement avant capture ou onboarding modèle, puis disparaît sans action utilisateur dans la fenêtre 2–4 s (sauf parcours avec préférence de réduction du mouvement documenté).
- **UGE-H-SC-002** : En test d'utilisabilité informel (≥ 3 personnes) ou revue produit interne, 100 % des participantes identifient le mot « MiamIA » et la marmite sur le splash en moins de 5 s d'exposition.
- **UGE-H-SC-003** : Aucune régression documentée sur l'ordre du parcours post-splash : Feature B et Feature A conservent leurs enchaînements relatifs (onboarding si requis, sinon capture).

### Hypothèses (Feature H)

- L'acquisition ou la création des actifs graphiques et typographiques respecte les contraintes légales sur les polices et les marques tierces ; l'inspiration stylistique reste une direction produit, pas une reproduction de fichiers protégés.
- « Quelques secondes » est interprété comme 2–4 s pour rester testable ; ajustement mineur acceptable si la revue accessibilité impose une borne inférieure.
- Le splash est purement présentationnel : aucune donnée métier ni OCR n'y est saisie.

---

---

## Feature I — Sélection du profil utilisateur sur l'écran de capture (défaut Adulte, requise avant photo)

> Input (intake 2026-06-28) : la sélection du profil utilisateur doit se faire sur l'écran de prise de photo ; profil par défaut « Adulte » ; le profil MUST être renseigné avant la prise de photo.
>
> **Relation inter-domaines** : `ingredient-health-intelligence` (Feature N) **consomme** le profil via un contrat `UserProfile` / `UserProfileProvider` (5 profils : Femme enceinte, Enfant, Agé, Adulte, Sportif ; défaut Adulte). Feature I (UGE) fournit l'**implémentation persistée** de `UserProfileProvider` + l'UI de sélection sur l'écran de capture.
>
> **Supersession** : la sélection du profil vit désormais **sur l'écran de capture** (Feature I), ce qui **supersède** l'hypothèse Feature N (clarify Q5) d'un écran séparé « Paramètres / Profil ». L'écran de capture devient le point de sélection canonique du profil ; un écran « Paramètres / Profil » distinct est hors périmètre Feature I.

### User Scenarios (Feature I)

#### US-I1 — Voir et confirmer son profil sur l'écran de capture (P1) 🎯 MVP

En tant qu'utilisatrice, sur l'écran de prise de photo, je veux voir le profil actuellement sélectionné (par défaut « Adulte ») et pouvoir le changer parmi les 5 profils proposés, avant de prendre la photo.

**Why this priority** : sans profil sélectionné, la critique santé (Feature N) ne peut pas être ciblée ; le défaut « Adulte » garantit une valeur immédiate tout en permettant la personnalisation.

**Independent Test** : sur l'écran de capture, le profil courant est visible et affiche « Adulte » au premier lancement ; l'utilisatrice peut sélectionner un autre profil (ex. « Femme enceinte ») qui s'affiche alors comme profil courant.

**Acceptance Scenarios**:

1. **Given** l'écran de capture au premier lancement, **When** l'utilisatrice regarde la zone de sélection de profil, **Then** le profil affiché est « Adulte » (défaut).
2. **Given** la zone de sélection de profil sur l'écran de capture, **When** l'utilisatrice ouvre le sélecteur et choisit « Femme enceinte », **Then** le profil courant affiché devient « Femme enceinte ».
3. **Given** un profil sélectionné « Sportif », **When** l'utilisatrice revient à l'écran de capture, **Then** le profil affiché reste « Sportif » (cohérence intra-session).
4. **Given** le sélecteur de profil, **When** l'utilisatrice consulte les options, **Then** exactement 5 profils sont proposés : Femme enceinte, Enfant, Agé, Adulte, Sportif.

#### US-I2 — Profil requis avant la prise de photo (P1)

En tant qu'utilisatrice, je veux que la prise de photo soit possible uniquement une fois un profil renseigné, afin que la critique santé soit toujours ciblée pour un profil valide.

**Why this priority** : garantit l'invariant Feature N (critique toujours associée à un profil) ; le défaut « Adulte » évite tout blocage frustrant.

**Independent Test** : la commande de capture est activée dès qu'un profil est sélectionné (par défaut Adulte dès le premier lancement) ; il n'existe pas d'état « aucun profil » permettant la capture.

**Acceptance Scenarios**:

1. **Given** l'écran de capture au premier lancement, **When** l'utilisatrice observe la commande de capture, **Then** celle-ci est activée car le profil par défaut « Adulte » est déjà renseigné.
2. **Given** un état où aucun profil n'est valide (cas d'erreur / profil corrompu), **When** l'utilisatrice tente de capturer, **Then** la capture est désactivée et un message invite à sélectionner un profil.
3. **Given** un profil sélectionné « Enfant », **When** l'utilisatrice prend la photo, **Then** l'analyse enchaîne avec le profil « Enfant » comme profil cible de la critique (consommé via `UserProfileProvider`).

#### US-I3 — Persistance du profil entre sessions (P2)

En tant qu'utilisatrice, je veux que le profil choisi soit mémorisé entre les lancements de l'application, afin de ne pas avoir à le re-sélectionner à chaque fois.

**Why this priority** : confort ; le défaut Adulte reste fonctionnel sans cela, mais la persistance réduit la friction pour les profils non-Adulte.

**Independent Test** : sélectionner « Femme enceinte », quitter et relancer l'app à froid ; l'écran de capture affiche « Femme enceinte ».

**Acceptance Scenarios**:

1. **Given** un profil sélectionné « Agé » et persisté, **When** l'utilisatrice relance l'application à froid, **Then** l'écran de capture affiche « Agé » comme profil courant.
2. **Given** un premier lancement sans profil préalablement persisté, **When** l'écran de capture s'affiche, **Then** le profil courant est « Adulte » (défaut) jusqu'à choix explicite.

#### US-I4 — Modifier le profil avant une nouvelle capture (P2)

En tant qu'utilisatrice, je veux pouvoir changer de profil à tout moment sur l'écran de capture avant une nouvelle prise, afin que la critique suivante soit ciblée pour le bon profil.

**Why this priority** : flexibilité (changement de contexte : grossesse, enfant, sportif) ; cohérent avec la sélection sur l'écran de capture.

**Independent Test** : capturer avec « Adulte », revenir à l'écran de capture, sélectionner « Sportif », capturer à nouveau ; la seconde critique est ciblée « Sportif ».

**Acceptance Scenarios**:

1. **Given** un profil courant « Adulte » après une première analyse, **When** l'utilisatrice sélectionne « Sportif » puis prend une nouvelle photo, **Then** la critique suivante est ciblée « Sportif ».
2. **Given** un changement de profil en cours de session, **When** l'utilisatrice consulte le profil courant, **Then** l'affichage reflète immédiatement le nouveau profil.

### Edge Cases (Feature I)

- Profil persisté illisible / corrompu : le système MUST retomber sur « Adulte » (défaut) sans planter, et la capture reste possible (US-I2).
- Modification du profil pendant qu'une analyse est déjà en cours : la critique en cours conserve le profil avec lequel elle a été lancée ; le nouveau profil s'applique à la capture suivante (pas de modification rétroactive).
- Sélection du profil avant disponibilité de la caméra (permission refusée / caméra indisponible) : le profil reste sélectionnable et persisté ; la capture reste désactivée pour cause caméra, indépendamment du profil.
- Conflit de périmètre avec un écran « Paramètres / Profil » : Feature I place la sélection sur l'écran de capture ; aucun écran paramètres distinct n'est introduit (supersession de l'hypothèse Feature N).

### Functional Requirements (Feature I)

- **UGE-I-FR-001** : Le système MUST exposer, sur l'écran de prise de photo, un contrôle de sélection du profil utilisateur proposant exactement 5 profils : Femme enceinte, Enfant, Agé, Adulte, Sportif.
- **UGE-I-FR-002** : Le système MUST initialiser le profil courant à « Adulte » par défaut au premier lancement (aucun profil préalablement persisté).
- **UGE-I-FR-003** : Le système MUST afficher en permanence, sur l'écran de capture, le profil actuellement sélectionné.
- **UGE-I-FR-004** : Le système MUST permettre à l'utilisatrice de changer le profil sélectionné directement depuis l'écran de capture, à tout moment avant une prise.
- **UGE-I-FR-005** : Le système MUST exiger qu'un profil valide soit renseigné pour activer la commande de capture ; le défaut « Adulte » satisfait cette exigence dès le premier lancement.
- **UGE-I-FR-006** : Le système MUST désactiver la commande de capture si, pour quelque raison que ce soit, aucun profil valide n'est disponible (cas d'erreur), avec un message invitant à sélectionner un profil.
- **UGE-I-FR-007** : Le système MUST persister le profil sélectionné entre les lancements de l'application (au-delà de la session courante).
- **UGE-I-FR-008** : Le système MUST, en cas de profil persisté illisible ou corrompu, retomber sur « Adulte » (défaut) sans interruption de service.
- **UGE-I-FR-009** : Le système MUST publier le profil courant via le contrat `UserProfileProvider` défini par `ingredient-health-intelligence` (Feature N), afin que la critique santé soit ciblée pour le profil sélectionné.
- **UGE-I-FR-010** : Le système MUST NOT modifier rétroactivement le profil d'une critique déjà lancée : un changement de profil ne s'applique qu'à la capture suivante.
- **UGE-I-FR-011** : Le système MUST NOT introduire d'écran « Paramètres / Profil » distinct dans le périmètre Feature I (la sélection vit sur l'écran de capture — supersession de l'hypothèse Feature N clarify Q5).
- **UGE-I-FR-012** : La sélection du profil MUST rester indépendante de l'état de disponibilité de la caméra (le profil est sélectionnable et persisté même si la caméra est indisponible ou la permission refusée).

### Key Entities (Feature I)

- **Profil utilisateur** : valeur parmi l'ensemble fermé {Femme enceinte, Enfant, Agé, Adulte, Sportif} ; définie et publiée par `ingredient-health-intelligence` (Published Language `UserProfile`) ; défaut Adulte.
- **Profil courant** : profil sélectionné à un instant t, affiché sur l'écran de capture, persisté entre sessions, exposé via `UserProfileProvider`.
- **Sélecteur de profil (capture)** : contrôle UI de l'écran de capture permettant la consultation et le changement du profil courant.

### Success Criteria (Feature I)

- **UGE-I-SC-001** : Sur 3 lancements à froid consécutifs sans profil préalable, l'écran de capture affiche systématiquement « Adulte » comme profil courant et la commande de capture est activée.
- **UGE-I-SC-002** : Après sélection de « Femme enceinte » puis relance à froid, l'écran de capture affiche « Femme enceinte » (persistance vérifiée).
- **UGE-I-SC-003** : Une capture lancée avec un profil donné produit une critique santé explicitement ciblée pour ce profil (rappel « Évalué pour vous : <profil> » — cohérent avec IHI-N-FR-003 / IHI-N-SC-001).
- **UGE-I-SC-004** : Aucun état « capture possible sans profil valide » n'est observé (le défaut Adulte couvre le parcours nominal ; le cas d'erreur désactive la capture avec message).
- **UGE-I-SC-005** : En cas de profil persisté corrompu, l'app retombe sur « Adulte » sans crash et la capture reste disponible (vérifié par parcours de corruption volontaire).
- **UGE-I-SC-006** : Un changement de profil en cours de session est reflété immédiatement sur l'écran de capture et s'applique à la capture suivante, sans affecter une analyse déjà en cours.

### Hypothèses (Feature I)

- L'ensemble des profils et le contrat `UserProfile` / `UserProfileProvider` sont définis par `ingredient-health-intelligence` (Feature N) ; UGE fournit l'implémentation persistée et l'UI, sans redéfinir l'énumération.
- « Adulte » est le profil par défaut neutre/général utilisé au premier lancement et en cas de repli.
- La persistance du profil s'appuie sur le stockage local existant de l'app (pas de backend) ; détail technique laissé au plan.
- La sélection du profil sur l'écran de capture supersède l'hypothèse Feature N d'un écran « Paramètres / Profil » distinct ; ce dernier reste possible dans une feature ultérieure mais n'est pas requis par Feature I.
- Le contrôle de sélection est conçu pour une friction minimale (le défaut Adulte n'oblige aucune action pour capturer).

---

## Cross-domain Notes

- Le segment ingrédients n'est pas déterminé ici : délégation à `ingredient-normalization-validation`.
- L'analyse de composition et la critique santé sont du ressort de `ingredient-health-intelligence` ; la pastille d’énergie estimée (kcal/100 g) en tête d’écran résultat est **orchestrée** ici (**UGE-A-FR-022**) et **spécifiée** côté analyse (**Feature K**, **IHI-K-FR-***).
- Le runtime LLM local (chargement modèle, inférence) est du ressort de `local-llm-runtime`.
- La capture OCR est du ressort de `capture-recognition`.
- Les KPI additifs sont du ressort de `additive-risk-insights`.
- La sélection/persistance du profil utilisateur (Feature I) est possédée par UGE ; l'énumération des profils et le contrat `UserProfileProvider` sont publiés par `ingredient-health-intelligence` (Feature N, Published Language). UGE publie l'implémentation persistée consommée par la critique santé.

## Source Mapping

- `specs/017-photo-analyse-ecran-resultat/` (Feature A)
- `specs/018-llm-download-onboarding/` (Feature B)
- `specs/012-home-layout-mediapipe-status/` (Feature C — home)
- Intake `/speckit-design` + `/speckit-specify` 2026-05-13 (pastille kcal — **UGE-A-FR-022** ; ref. `specs/domains/ingredient-health-intelligence/spec.md` Feature K)
- Intake `/speckit-design` + `/speckit-specify` 2026-06-28 (Feature I — sélection profil sur l'écran de capture ; ref. `specs/domains/ingredient-health-intelligence/spec.md` Feature N — contrat `UserProfile`/`UserProfileProvider`)

## Assumptions

- L'écran de capture est l'écran d'accueil fonctionnel après splash et onboarding modèle ; pas de page d'accueil distincte à onglets.
- Le flux de capture photo reste la référence fonctionnelle pour l'entrée utilisateur vers l'analyse (le test LLM UI est retiré — Feature F).
- La nature du résultat exploitable relève des domaines d'analyse ; cet écran se contente d'une présentation lisible.
- Le parcours reste local (pas de dépendance réseau pour le flux de base, sauf téléchargement modèle).
- Le modèle LLM est obligatoire ; l'app ne fonctionne pas sans.
- Le texte OCR validé est le contenu de repli quand l'analyse échoue.
