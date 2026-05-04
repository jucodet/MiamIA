# Quickstart — KPI additifs (003)

## Prérequis

- Même environnement que le flux composition **009** : Android Studio, SDK, modèle Gemma `.litertlm` si test bout-en-bout réel.

## Vérification manuelle

1. Réaliser un scan jusqu’à l’état **Bilan composition** (`ScanState.BilanReady`).
2. Vérifier la présence du **panneau KPI additifs** (ou équivalent) sous ou à côté du texte d’analyse existant.
3. Contrôler :
   - ordre **rouge → orange → vert** (puis « à confirmer » / gris si applicable) ;
   - une **justification courte** par ligne ;
   - **totaux** en tête cohérents avec le nombre de lignes.
4. Cas vide : utiliser une analyse sans additifs détectables → message d’état vide.

## Tests automatisés

- **Unitaires** : parseur `BuildAdditiveKpiDisplay` / `AdditiveKpiParser` avec fixtures texte LLM (lignes structurées + cas partiels).
- **Compose** : tests de liste ordonnée + compteurs (tags de test sur KPI).

Commandes (SDK configuré) :

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```
