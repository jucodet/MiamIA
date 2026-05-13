<!--
Sync Impact Report

- Version change: 0.1.0 → 0.2.0
- Modified principles:
  - V. Simplicité, lisibilité, et évolutivité contrôlée (clarifié pour cohabiter avec DDD)
- Added sections:
  - VI. Frontières DDD et autonomie des domaines
- Removed sections: N/A
- Templates requiring updates:
  - ⚠ .specify/templates/plan-template.md (ajouter un contrôle explicite des frontières DDD)
  - ⚠ .specify/templates/spec-template.md (ajouter un champ de rattachement de domaine/bounded context)
  - ⚠ .specify/templates/tasks-template.md (ajouter tâches de validation de frontières + contrats inter-domaines)
- Follow-up TODOs:
  - TODO(RATIFICATION_DATE): date de ratification initiale inconnue.
-->

# MiamIA Constitution
<!-- Constitution de gouvernance & qualité du projet MiamIA -->

## Core Principles

### I. Qualité produit et code (non négociable)
Le projet DOIT rester fiable, maintenable et prédictible.

- Toute modification DOIT être traçable (spec → tests d’acceptation → code).
- Toute PR DOIT maintenir ou améliorer la lisibilité, la cohérence et la robustesse.
- Les régressions (fonctionnelles, UX, performance) sont des bugs bloquants.

### II. Acceptance Test Driven Development (ATDD) d’abord
On développe à partir de scénarios d’acceptation observables par l’utilisateur.

- Chaque user story DOIT avoir des scénarios d’acceptation **Given/When/Then** avant implémentation.
- Les tests d’acceptation (ou tests de parcours/intégration équivalents) DOIVENT échouer avant le code, puis passer.
- Un incrément livrable DOIT être démontrable et testable indépendamment (MVP par story).

### III. UX moderne et optimale par défaut
Chaque fonctionnalité DOIT viser une expérience claire, rapide, et cohérente.

- Les parcours principaux DOIVENT être simples (réduction des frictions, feedback immédiat, états vides/erreurs soignés).
- L’UI DOIT être moderne (accessibilité, responsive si applicable, micro-copies utiles).
- Les choix UX DOIVENT être validés par des scénarios d’acceptation (incluant erreurs/edge cases).

### IV. Performance comme exigence produit
La performance est une fonctionnalité.

- Toute feature DOIT définir des objectifs mesurables (latence p95, temps d’interaction, mémoire, etc.) quand pertinent.
- Les régressions de performance sont des bugs et DOIVENT être corrigées avant livraison.
- Les optimisations DOIVENT être guidées par des mesures (benchmarks/profiling), pas par intuition.

### V. Simplicité, lisibilité, et évolutivité contrôlée
On privilégie des solutions simples, testables, et faciles à faire évoluer.

- Éviter la sur-architecture; chaque complexité DOIT être justifiée par un besoin présent.
- Les interfaces (API internes/externes) DOIVENT être stables et couvertes par des tests d’acceptation/contrat.
- Le refactor est encouragé lorsqu’il réduit le risque ou accélère la livraison future, sans casser les scénarios.

### VI. Frontières DDD et autonomie des domaines
Le modèle métier et les frontières de domaines priment sur la commodité technique.

- Toute fonctionnalité DOIT être rattachée à un domaine métier explicite (`specs/domains/<domain>`).
- Les bounded contexts DOIVENT rester autonomes: vocabulaire, invariants, et règles propres.
- Le partage direct de modèles entre domaines DOIT être évité; utiliser des contrats explicites et/ou une couche d’anti-corruption.
- La duplication de code entre domaines est AUTORISÉE (et peut être préférée) lorsqu’elle protège la clarté des frontières métier.
- Un composant "commun" n’est acceptable que s’il représente une capacité réellement partagée et non une fuite de frontière.

## Standards de livraison (Qualité, ATDD, Performance)

- Chaque feature DOIT être décrite dans une spec avec:
  - user stories priorisées,
  - scénarios d’acceptation Given/When/Then,
  - critères de succès mesurables (incluant performance si applicable).
- Chaque PR DOIT inclure:
  - tests d’acceptation/parcours pertinents,
  - une vérification des erreurs/edge cases,
  - une note de risque (si changement sensible) et un plan de rollback si nécessaire.
- Pour la documentation Spec Kit:
  - Pour toute nouvelle feature, `speckit-design` DOIT etre la premiere etape (routage bounded context + dossier domaine cible).
  - Après routage en mode intake, `speckit-specify` DOIT etre enchaîné immédiatement dans le même tour (sans étape utilisateur intermédiaire).
  - `speckit-design` DOIT être exécuté avant toute consolidation SSOT par domaine.
  - `speckit-spec-refactor` DOIT reconstruire les specs de domaines depuis les specs features (les specs de domaine sont des sorties, pas des entrées).
  - Toute ambiguïté de frontière DOIT être explicitée dans `specs/domains/domain-map.md`.

## Workflow de développement (gates)

- Avant implémentation: scénarios d’acceptation rédigés (et alignés sur l’UX attendue).
- Avant consolidation SSOT: structure `specs/domains/<domain>/spec.md` existante et validée.
- Pendant implémentation: itérations courtes; maintenir une branche livrable à tout moment.
- Avant merge:
  - scénarios d’acceptation passent,
  - pas de régression UX/perf détectée,
  - PR relue avec focus sur lisibilité, tests, risques, et respect des frontières DDD.

## Governance

- Cette constitution prévaut sur les templates/specs/plans en cas de conflit.
- Toute PR DOIT vérifier la conformité (ATDD, UX, performance, qualité).
- Politique de version:
  - **MAJOR**: suppression/redéfinition incompatible d’un principe ou d’une règle de gouvernance.
  - **MINOR**: ajout d’un principe/section ou extension substantielle des exigences.
  - **PATCH**: clarifications, reformulations, corrections sans changement de sens.
- Amendements:
  - proposer un changement avec justification + impact (templates, process),
  - approuver via PR,
  - mettre à jour le Sync Impact Report en tête de fichier.

**Version**: 0.2.0 | **Ratified**: TODO(RATIFICATION_DATE) | **Last Amended**: 2026-05-05
