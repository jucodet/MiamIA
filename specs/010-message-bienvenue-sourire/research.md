# Research - Message de bienvenue souriant

## Decision 1: Source des messages

- **Decision**: Utiliser une bibliotheque fixe de messages pre-valides en francais.
- **Rationale**: Controle du ton "positif et chaleureux", risque contenu reduit, validation produit simple.
- **Alternatives considered**:
  - Generation dynamique: rejetee pour risque de variabilite et moderation.
  - Mode hybride: rejetee pour complexite inutile en v1.

## Decision 2: Regle de variation

- **Decision**: Selection aleatoire a chaque connexion, repetition immediate autorisee.
- **Rationale**: Aligne exactement la clarification utilisateur et simplifie la logique.
- **Alternatives considered**:
  - Interdiction repetition immediate: rejetee (non souhaitee).
  - Rotation stricte: rejetee (plus complexe, pas demandee).

## Decision 3: Comportement si bibliotheque vide

- **Decision**: N'afficher aucun message (pas de fallback).
- **Rationale**: Clarification explicite utilisateur; evite d'introduire du contenu non valide.
- **Alternatives considered**:
  - Fallback unique: rejete par clarification.
  - Fallback multiple: rejete pour meme raison.

## Decision 4: Point d'integration dans le parcours

- **Decision**: Afficher uniquement apres connexion reussie.
- **Rationale**: Perimetre clair, compatible avec spec clarifiee, test de parcours direct.
- **Alternatives considered**:
  - Affichage aussi apres inscription: hors perimetre v1.
  - Affichage a chaque ouverture app: ambiguite produit accrue.

## Validation implementation

- Les composants `welcome` (provider, selector, policy, etat UI, logger) sont implementes et relies a l'ecran principal.
- Les tests unitaires et d'instrumentation de la feature sont ajoutes dans `app/src/test/java/com/miamia/welcome/` et `app/src/androidTest/java/com/miamia/welcome/`.
- Execution automatique locale non finalisee dans cet environnement car le wrapper Gradle est incomplet (`org.gradle.wrapper.GradleWrapperMain` introuvable).
