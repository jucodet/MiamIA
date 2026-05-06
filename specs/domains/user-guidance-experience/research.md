# Research - user-guidance-experience / homepage-llm-mock-trigger

## Decision 1: Declenchement unique avec verrou d'execution

- **Decision**: Autoriser une seule execution du test LLM a la fois et ignorer/neutraliser les clics additionnels pendant l'etat `running`.
- **Rationale**: Evite les courses, simplifie la comprehension utilisateur et garantit un affichage deterministic des resultats.
- **Alternatives considered**:
  - Lancer plusieurs executions en parallele -> rejete (UX confuse, risque de collision d'etats).
  - Mettre en file d'attente les clics -> rejete (complexite non necessaire pour ce besoin).

## Decision 2: Modele d'etat UX explicite

- **Decision**: Utiliser un etat de run borne (`idle`, `running`, `success`, `failure`) pilote par la homepage.
- **Rationale**: Rend les transitions verifiables en tests et aligne les messages utilisateur avec les scenarios d'acceptation.
- **Alternatives considered**:
  - Etats implicites derives de plusieurs flags -> rejete (ambigu, fragile en maintenance).

## Decision 3: Timeout produit a 30 secondes

- **Decision**: Si aucune reponse exploitable n'est disponible dans 30 secondes, terminer en `failure` avec categorie `timeout`.
- **Rationale**: Borne l'attente et maintient une experience percue predictable.
- **Alternatives considered**:
  - 60 secondes -> rejete (latence trop longue pour un bouton home).
  - Pas de timeout -> rejete (risque de blocage percu).

## Decision 4: Frontiere domaine preservee

- **Decision**: `user-guidance-experience` orchestre le clic, l'etat et le rendu; la qualification metier de la reponse reste dans `ingredient-health-intelligence`.
- **Rationale**: Respecte la constitution DDD et evite la fuite de regles metier vers l'UI.
- **Alternatives considered**:
  - Valider la "reponse exploitable" cote homepage -> rejete (duplique la logique du domaine metier).

## Decision 5: Contrat UI centré interaction

- **Decision**: Definir un contrat d'interface documentant les commandes (`tap run`) et sorties observables (etat, texte resultat, erreur).
- **Rationale**: Facilite les tests d'acceptation et clarifie les attentes sans imposer de details d'implementation.
- **Alternatives considered**:
  - Absence de contrat formel -> rejete (ambiguite accrue pour tests et evolution).
