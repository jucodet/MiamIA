# Domain Spec - capture-recognition

## Purpose

Capturer une image produit de facon fiable, produire un `RawOcrText` rattache a une session et garantir un parcours de reprise explicite quand la capture/OCR echoue.

## Scope

- Preview camera, capture, OCR, erreurs capteurs.
- Demarrage sur camera, gestion permissions, etats "pret/analyse/succes/echec".
- Gestion temporaire des photos (ephemeres, suppression fin de cycle).
- Emission de donnees brutes vers l'ACL de normalisation.

**Ref. domaine aval** : lorsque l’intention produit « balise / mode ingrédients » est portée **uniquement** par l’UI de capture (choix utilisateur avant la photo), ce signal est consommé par `ingredient-normalization-validation` pour l’enchaînement analyse sans écran de validation du segment (FR-010). Ce domaine ne redéfinit pas les règles d’ancrage ni le gate — voir `specs/domains/ingredient-normalization-validation/contracts/session-capture-intent-for-implicit-validation.md`.

## Invariants

- Chaque `RawOcrText` appartient a une `ScanSession`.
- Une capture invalide ne doit pas produire de texte valide.
- Aucun contenu factice ne remplace un flux camera reel ou un resultat OCR.
- La photo de scan n'est jamais conservee de facon persistante dans ce domaine.

## Functional Requirements

- CR-FR-001: le domaine MUST fournir un apercu camera reel et une capture declenchee uniquement par action explicite utilisateur.
- CR-FR-002: le domaine MUST executer OCR sur l'appareil et publier le texte extrait sans envoi image/texte vers un service distant.
- CR-FR-003: le domaine MUST exposer des etats utilisateurs explicites (verification readiness, analyse en cours, succes, echec) sans blocage silencieux.
- CR-FR-004: le domaine MUST nettoyer les artefacts temporaires de capture en succes, annulation et erreur.
- CR-FR-005: le domaine MUST refuser la progression vers l'analyse amont si le texte OCR est vide ou inexploitable. *(Note sync 2026-05-12 : le blocage est assuré par CameraViewModel, pas par ScanFailureClassifier directement. Le comportement est aligné ; l'architecture interne n'est pas prescrite par cette spec.)*
- CR-FR-006: l'écran de consultation du `RawOcrText` après capture MUST afficher le texte dans une zone à hauteur bornée avec défilement vertical lorsque le contenu dépasse l'espace alloué à cette zone.
- CR-FR-007: les actions principales (boutons) affichées sous le texte capturé MUST rester visibles et actionnables sans que l'utilisateur ait à faire défiler tout l'écran pour les atteindre, y compris lorsque le texte occupe plusieurs « pages » équivalentes à l'écran.
- CR-FR-008: lorsque le texte capturé est court, la zone de texte MUST rester lisible (pas de masquage du contenu par des contraintes de hauteur inadaptées) et ne pas imposer de défilement inutile.
- CR-FR-009: l’aperçu caméra MUST rester intégralement visible (aucun bouton, surface d’action ou élément persistant de l’UI capture ne MUST recouvrir, même partiellement, la zone de prévisualisation vidéo).
- CR-FR-010: l’action principale de capture MUST être présentée sous l’aperçu caméra (en dehors de la zone vidéo), sur une bande d’action dédiée, atteignable sans masquer le flux.
- CR-FR-011: le libellé de l’action principale de capture MUST être « Y a quoi là-dedans ? » (texte exact, casse et ponctuation comprises, dans la langue d’interface française).

## Feature increment — Bouton capture sous l’aperçu et libellé « Y a quoi là-dedans ? »

**Branche**: `020-capture-button-placement` · **Date**: 2026-05-13 · **Statut**: Draft

### User Scenarios & Testing

#### User Story 1 — Aperçu caméra non recouvert (P1)

L’utilisateur ouvre l’écran de capture. Le flux vidéo en direct est visible sans qu’aucun bouton (notamment l’action principale de capture) ne vienne recouvrir, même partiellement, l’aperçu. Le cadrage du produit reste possible jusqu’aux bords visibles de la zone vidéo.

**Priorité P1** : un cadrage partiellement obstrué dégrade directement la qualité OCR et la confiance utilisateur dans l’app ; c’est le motif initial de la demande.

**Test indépendant** : ouverture de l’écran capture ; mesure visuelle / instrumentée que la bounding box de la zone vidéo n’est intersectée par aucune surface persistante de bouton d’action.

**Scénarios d’acceptation** :

1. **Given** l’écran de capture affiché en orientation portrait, **When** l’utilisateur observe l’aperçu caméra, **Then** aucun bouton de l’UI capture (ni l’action principale, ni les actions secondaires persistantes) n’apparaît au-dessus de la zone vidéo ; les boutons sont disposés dans une bande dédiée située sous l’aperçu.
2. **Given** un produit positionné en bas du cadre vidéo, **When** l’utilisateur ajuste le cadrage, **Then** la partie basse de l’aperçu reste entièrement visible (non occultée par un bouton).

#### User Story 2 — Libellé conversationnel de l’action de capture (P1)

L’utilisateur identifie immédiatement l’action principale comme « savoir ce qu’il y a dans ce produit ». Le bouton porte le libellé exact « Y a quoi là-dedans ? » plutôt qu’un libellé technique.

**Priorité P1** : libellé prescrit par le produit, condition d’acceptation explicite de la demande.

**Test indépendant** : capture d’écran de l’écran capture ; vérifier que le libellé exact « Y a quoi là-dedans ? » est affiché sur le bouton principal et qu’aucun ancien libellé (« Prendre la photo ») n’apparaît.

**Scénarios d’acceptation** :

1. **Given** l’écran de capture affiché, **When** l’utilisateur regarde le bouton principal d’action, **Then** son libellé est exactement « Y a quoi là-dedans ? ».
2. **Given** l’écran de capture, **When** l’utilisateur active ce bouton, **Then** le comportement reste celui d’une capture photo déclenchée explicitement (aucun changement de parcours par rapport au flux existant).

### Cas limites

- Petite hauteur utile (paysage / écran compact) : l’aperçu peut se contracter, mais la bande d’action reste sous la vidéo ; aucun bouton ne migre par-dessus l’aperçu pour gagner de la place.
- Clavier logiciel ouvert (improbable sur cet écran mais possible si un champ est focalisé) : aucun bouton de capture ne doit se superposer à l’aperçu pour rester visible ; le comportement plateforme standard prime sur la zone bouton, pas sur l’aperçu.
- Affichage long de libellé (traduction future / accessibilité grande typo) : le libellé reste sur la bande d’action, en plusieurs lignes si nécessaire, sans empiéter sur l’aperçu vidéo.
- Présence d’actions secondaires (ex. action de diagnostic) : elles partagent la bande d’action sous l’aperçu et respectent la même règle de non-recouvrement.

### Critères de succès mesurables

- **SC-CR-003** : sur 100 % des ouvertures de l’écran capture (portrait, configuration standard), la zone vidéo n’est recouverte par aucun bouton d’action persistant (vérification visuelle ou instrumentée par bounding boxes).
- **SC-CR-004** : 100 % des utilisateurs sollicités dans une revue qualitative identifient correctement, en moins de 3 secondes et sans assistance, le bouton « Y a quoi là-dedans ? » comme l’action déclenchant l’analyse à partir d’une photo.
- **SC-CR-005** : 0 régression sur le déclenchement effectif de la capture après renommage (taux de réussite du déclenchement identique à l’état précédent, à comportement back-end constant).

### Hypothèses

- L’action « Y a quoi là-dedans ? » remplace 1-pour-1 l’ancien bouton « Prendre la photo » ; aucune nouvelle action n’est introduite par cette évolution.
- Les actions secondaires éventuellement présentes sur l’écran (ex. action de test de disponibilité du moteur local) ne sont pas renommées ni déplacées dans le cadre de cette évolution ; elles doivent simplement respecter la règle de non-recouvrement (CR-FR-009).
- Le parcours fonctionnel post-clic reste inchangé : capture explicite → OCR → suite du flux existant.

## Feature increment — Zone défilante texte capturé (bannière actions fixe)

**Branche**: `019-captured-text-scroll` · **Date**: 2026-05-13 · **Statut**: Draft

### User Scenarios & Testing

#### User Story 1 — Parcours texte long (P1)

Après une capture réussie, l'utilisateur consulte un texte OCR très long (liste d'ingrédients dense). Il fait défiler uniquement la zone de texte pour relire le haut et le bas, tout en voyant en permanence les boutons de confirmation ou de poursuite du parcours.

**Priorité P1**: sans cela, le parcours de capture peut être bloqué ou source d'erreurs (actions inaccessibles).

**Test indépendant**: scénario instrumenté ou manuel avec jeu de données texte dépassant la hauteur utile de l'écran ; vérifier accès scroll au texte et visibilité des boutons.

**Scénarios d'acceptation**:

1. **Given** un `RawOcrText` dont la hauteur rendue dépasse la zone texte, **When** l'utilisateur affiche l'écran de relecture, **Then** le texte est entièrement consultable par défilement à l'intérieur de la zone dédiée et les boutons d'action sous la zone restent visibles sans scroll global cachant ces boutons.
2. **Given** le même écran, **When** l'utilisateur a fait défiler le texte jusqu'en bas de la zone, **Then** les boutons d'action restent visibles et utilisables.

#### User Story 2 — Parcours texte court (P2)

Le texte OCR tient en peu de lignes. L'écran reste équilibré : le texte reste lisible, pas de barre de défilement trompeuse ou de zone vide disproportionnée.

**Scénarios d'acceptation**:

1. **Given** un `RawOcrText` court, **When** l'écran s'affiche, **Then** le texte est lisible sans défilement obligatoire et les boutons restent visibles sous la zone texte.

### Cas limites

- Clavier logiciel ouvert sur l'écran : les boutons restent atteignables (comportement conforme aux attentes plateforme sans masquer définitivement les actions).
- Orientation paysage ou petite hauteur utile : la zone texte se contracte, le défilement interne prévaut ; les boutons restent dans la partie visible inférieure ou suivent le pattern d'accessibilité déjà retenu par l'application pour les barres d'action fixes.
- Texte vide ou quasi vide : inchangé par rapport à CR-FR-005 ; pas de régression sur le refus de progression.

### Critères de succès mesurables

- **SC-CR-001**: pour un jeu de contenus texte représentatif « long étiquette » (hauteur supérieure à la zone utile), 100 % des testeurs peuvent atteindre le dernier caractère du texte et activer un bouton d'action principal sans quitter l'écran.
- **SC-CR-002**: pour un texte court (≤ quelques lignes), au moins 90 % des évaluations qualitatives jugent l'écran « lisible et sans friction » (pas de scroll forcé inutile).

### Hypothèses

- L'écran concerné est celui qui présente le `RawOcrText` issu de la capture avec des actions disposées sous le bloc de texte (même parcours qu'aujourd'hui, seule la disposition change).
- Les libellés et nombre de boutons ne changent pas dans le cadre de cette évolution ; seule la géométrie / contrainte de mise en page est ajustée.

## Cross-domain Notes

- Le segment ingredients n'est pas determine ici: delegation a `ingredient-normalization-validation`.
- La redirection automatique post-OCR vers un ecran d'analyse est orchestree par `user-guidance-experience`.

## Source Mapping

- `specs/001-scan-ingredients/spec.md`
- `specs/005-camera-start-temp-scan/spec.md`
- `specs/007-live-camera-preview-capture/spec.md`
- `specs/008-capture-photo-texte-ocr/spec.md`
- `specs/012-home-layout-mediapipe-status/spec.md`
- `specs/015-analyse-ocr-llm/spec.md` (preconditions OCR exploitables)
