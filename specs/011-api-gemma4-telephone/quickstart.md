# Quickstart: Utilisation API Gemma4 telephone

## Objectif

Verifier rapidement le comportement de la fonctionnalite d'analyse textuelle via l'API locale Gemma4, sans modele embarque lourd.

## Prerequis

- Build Android fonctionnel.
- Appareil Android compatible avec Gemma4 local installe et activable.
- Parcours applicatif menant a l'ecran d'analyse textuelle disponible.

## Parcours 1 - Succes nominal (API locale disponible)

1. Lancer l'application sur un appareil recent compatible.
2. Ouvrir le parcours d'analyse textuelle.
3. Saisir un texte valide et lancer l'analyse.
4. Verifier:
   - reponse analysee affichee,
   - aucun telechargement de modele,
   - trace technique sans contenu utilisateur.

## Parcours 2 - API locale indisponible

1. Desactiver le service/endpoint local Gemma4 sur l'appareil.
2. Relancer une analyse textuelle.
3. Verifier:
   - echec explicite avec message utilisateur clair en moins de 2 secondes,
   - aucun fallback vers moteur local/distant,
   - application reste utilisable.

## Parcours 3 - Performance cible

1. Executer un lot representatif de requetes textuelles (au moins 30) sur:
   - un appareil recent,
   - un appareil minimum compatible.
2. Verifier:
   - p95 < 3s sur appareil recent,
   - p95 < 6s sur appareil minimum compatible.

## Parcours 4 - Confidentialite des traces

1. Executer plusieurs analyses texte (succes + echec).
2. Inspecter les journaux/metriques techniques.
3. Verifier:
   - presence des metriques attendues (latence/statut/type erreur),
   - absence de contenu brut utilisateur dans les traces.

## Verification executee (2026-04-27)

- Build/tests automatiques: non executes localement (wrapper Gradle incomplet: `org.gradle.wrapper.GradleWrapperMain` manquant).
- Verification statique:
  - integration `Gemma4LocalCompositionEngine` active dans `MainActivity`.
  - absence de fallback moteur dans `Gemma4LocalClient`.
  - messages explicites d'indisponibilite via `Gemma4LocalUiMessageResolver`.
  - metriques non sensibles via `Gemma4LocalMetricsLogger` + `AnalysisLatencyTracker`.
- Points a valider des que l'environnement de build est retabli:
  - tests unitaires `com.miamia.gemma4local.*`
  - tests Android `com.miamia.gemma4local.*`
  - mesure p95 sur appareils recents et minimum compatibles.
