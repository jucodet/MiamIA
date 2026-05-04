# Quickstart — Critique santé ingrédients (002)

## Prérequis

- Android Studio / JDK 17 / SDK alignés sur `app/build.gradle.kts` (minSdk 26, compileSdk 34).
- Modèle Gemma `.litertlm` comme pour le flux **009**, pour un test d’inférence réel.

## Vérification manuelle (happy path)

1. Lancer l’app (`MainActivity`). Réaliser un **scan** jusqu’à un **segment ingrédients validé** et un bilan cohérent (flux caméra existant).
2. Ouvrir l’onglet **« Critique santé »** : la liste se **synchronise automatiquement** depuis le dernier bilan prêt (`lastValidatedSegmentForHealth`). Vérifier qu’elle correspond au **segment validé** et qu’elle est **non éditable** (lecture seule).
3. Appuyer sur **Analyser** (liste ≥ seuil produit, ex. 10 caractères — `HealthCritiqueConfig.MIN_INGREDIENT_TEXT_LENGTH` si inchangé).
4. Contrôler :
   - quatre sections ou marqueurs équivalents pour les populations ;
   - **SC-005** : le texte analysé est le même que le segment validé (comparaison visuelle ou log) ;
   - **Copier** résultat / prompt si disponibles.
5. Redémarrer l’app : la **dernière** analyse reste accessible avec la **même liste** persistée (FR-006).

## Cas limites rapides

- Pas de scan / pas de segment validé → message du type « effectuez un scan » / `no_validated_segment`, **pas** d’appel LLM.
- Segment trop court après validation → `input_invalid` / `too_short` (FR-005).
- Correction liste → uniquement via **retour caméra** et **revalidation** du segment (pas d’édition dans l’onglet santé).

## Tests automatisés

- **Unitaires** : parseur, validation entrée, règle **identité** payload / segment (SC-005).
- **Instrumentés** : persistance, presse-papiers, état lecture seule du champ liste.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Les tests instrumentés exigent un appareil ou émulateur avec SDK configurés.
