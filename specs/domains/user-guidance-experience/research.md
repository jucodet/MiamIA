# Research - user-guidance-experience

## Decision 1: Declenchement unique avec verrou d'execution (homepage / test LLM)

- **Decision**: Autoriser une seule execution du test LLM a la fois et ignorer/neutraliser les clics additionnels pendant l'etat `running`.
- **Rationale**: Evite les courses, simplifie la comprehension utilisateur et garantit un affichage deterministic des resultats.
- **Alternatives considered**:
  - Lancer plusieurs executions en parallele -> rejete (UX confuse, risque de collision d'etats).
  - Mettre en file d'attente les clics -> rejete (complexite non necessaire pour ce besoin).

## Decision 2: Modele d'etat UX explicite

- **Decision**: Utiliser un etat de run borne (`idle`, `running`, `success`, `failure`) pilote par l'ecran concerne (homepage historique ; etendre au flux capture + resultat).
- **Rationale**: Rend les transitions verifiables en tests et aligne les messages utilisateur avec les scenarios d'acceptation.
- **Alternatives considered**:
  - Etats implicites derives de plusieurs flags -> rejete (ambigu, fragile en maintenance).

## Decision 3: Timeout produit a 30 secondes (runner mock)

- **Decision**: Si aucune reponse exploitable n'est disponible dans 30 secondes, terminer en `failure` avec categorie `timeout` (alignement avec configuration runtime existante).
- **Rationale**: Borne l'attente et maintient une experience percue predictable.
- **Alternatives considered**:
  - 60 secondes -> rejete (latence trop longue pour un bouton home).
  - Pas de timeout -> rejete (risque de blocage percu).

## Decision 4: Frontiere domaine preservee

- **Decision**: `user-guidance-experience` orchestre le clic, l'etat, le rendu et la navigation ; la qualification metier de la reponse reste dans les domaines d'analyse (`ingredient-health-intelligence` / composition).
- **Rationale**: Respecte la constitution DDD et evite la fuite de regles metier vers l'UI.
- **Alternatives considered**:
  - Valider la "reponse exploitable" cote ecran capture -> rejete (duplique la logique du domaine metier).

## Decision 5: Contrat UI centre interaction

- **Decision**: Definir des contrats d'interface documentant les commandes et sorties observables (etat, texte resultat, erreur, navigation).
- **Rationale**: Facilite les tests d'acceptation et clarifie les attentes sans imposer de details d'implementation.
- **Alternatives considered**:
  - Absence de contrat formel -> rejete (ambiguite accrue pour tests et evolution).

---

## Decision 6: Loader sur l'ecran de capture (clarification 2026-05-06)

- **Decision**: Presenter l'indicateur de chargement sur l'ecran de capture (recouvrement plein cadre autorise) jusqu'a l'etat terminal ; ne pas afficher l'ecran resultat avant cette fin.
- **Rationale**: Cohérent avec la clarification utilisateur et reduit l'impression de "saut" vers un ecran vide.
- **Alternatives considered**:
  - Loader uniquement sur l'ecran resultat -> rejete (non retenu par le produit).
  - Loader global detache -> rejete (non retenu par le produit).

## Decision 7: Abandon du flux (retour pendant chargement)

- **Decision**: Si l'utilisatrice quitte l'ecran de capture pendant le chargement, ne pas declencher de navigation automatique vers l'ecran resultat lorsque le traitement se termine ; pas de pop-up resultat inattendu.
- **Rationale**: Respecte l'intention utilisateur et l'edge case formalise en spec (FR-014).
- **Alternatives considered**:
  - Toujours naviguer vers le resultat a la fin -> rejete (incoherent avec abandon).
  - Bloquer le retour arriere -> rejete (trop intrusif pour une premiere version ; spec ne l'exige pas).

**Implementation guidance**: lier la navigation resultat a la presence active de l'ecran capture (Lifecycle `STARTED`, ou equivalent Navigation : ne pas `navigate` si le back stack entry n'est plus le sommet / scope annule).

## Decision 8: Bouton photo pendant traitement LLM

- **Decision**: Desactiver le bouton de prise de photo pendant tout `LlmProcessingState` en `en_cours` (parcours declenche par la photo ou par le test LLM), en plus du verrou du bouton test LLM (FR-009).
- **Rationale**: Evite une double capture ou double pipeline concurrent ; aligne avec l'edge case "clics rapides" et SC-005 / scenario "pas de deuxieme analyse concurrente".
- **Alternatives considered**:
  - Laisser le bouton photo actif -> rejete (risque de courses et violations implicites des scenarios).
  - Masquer le bouton photo -> acceptable en variante UX mais non necessaire ; desactivation plus explicite.

## Decision 9: Navigation vers ecran resultat

- **Decision**: Introduire (ou etendre) une route Compose Navigation dediee `LlmResult` (nom logique) recevant un payload minimal affichable (texte principal + indicateur erreur le cas echeant) ; navigation depuis le thread/UI principal apres `success` ou `failure` **uniquement** si l'ecran capture etait toujours actif.
- **Rationale**: Separe clairement capture et lecture du resultat ; testable et conforme FR-007 / FR-010.
- **Alternatives considered**:
  - Rester sur un seul ecran avec panneau -> rejete (contradictoire avec la spec actuelle et la demande utilisateur "ecran" resultat).

---

## Decision 10: Accueil = capture, sans barre d'onglets (spec 2026-05-06)

- **Decision**: Le premier écran après lancement normal est l'écran de prise de photo ; supprimer la structure de navigation principale par onglets (ex. Accueil / Caméra / Critique santé). La racine de l'UI est le contenu actuellement associé au parcours capture (+ sous-navigation vers résultat LLM).
- **Rationale**: Alignement FR-001, FR-015, SC-007 ; réduction de friction ; une seule pile de navigation principale à tester.
- **Alternatives considered**:
  - Conserver des onglets avec onglet « Caméra » sélectionné par défaut -> rejete (contradictoire FR-015).
  - Drawer / menu hamburger pour anciennes sections -> hors périmètre sauf nouvelle spec (assumption spec).

## Decision 11: Retour depuis résultat vers « accueil » capture (FR-016)

- **Decision**: Après `popBackStack` (ou équivalent) depuis l'écran résultat LLM, l'utilisatrice retrouve l'écran de capture comme contexte principal, sans réafficher d'onglets.
- **Rationale**: Cohérent avec FR-016 et edge case « retour arrière sans réintroduction d'onglets ».
- **Alternatives considered**:
  - Fermer l'application au lieu de revenir à la capture -> rejete (non spécifié ; FR-012 impose nouvelle capture possible).
