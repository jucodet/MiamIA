# Quickstart — Critique santé ingrédients (002)

## Prérequis

- Android Studio / JDK 17 / SDK alignés sur `app/build.gradle.kts` (minSdk 26, compileSdk 34).
- Modèle Gemma `.litertlm` installé comme pour le flux **009** (bilan composition), si les tests instrumentés exécutent l’inférence réelle.

## Vérification manuelle (happy path)

1. Lancer l’app (`MainActivity`). En haut de l’écran, choisir l’onglet **« Critique santé »** (à côté de **« Caméra »**).
2. Saisir une liste d’au moins **10 caractères** (ex. `eau, sucre, huile de palme, arômes`) — seuil `HealthCritiqueConfig.MIN_INGREDIENT_TEXT_LENGTH`.
3. Appuyer sur **Analyser**.
4. Contrôler :
   - quatre sections visibles ou quatre blocs titrés correspondant aux populations ;
   - ton prudent et mention de consultation professionnelle si pertinent (grossesse) ;
   - actions **Copier** sur le résultat et, si prévu, sur le prompt.
5. Fermer et rouvrir l’app : la **dernière** analyse est toujours accessible (FR-006).

## Cas limites rapides

- Liste vide → message clair, pas d’appel LLM.
- Liste très courte → message « trop courte » (seuil documenté dans les tests).
- Terme ambigu (`arômes`) → réponse peut signaler l’ambiguïté (parseWarnings ou texte).

## Tests automatisés

- **Unitaires**: validation entrée, construction du prompt, parseur des marqueurs `###…`.
- **Instrumentés / Compose**: états `InvalidInput`, `ResultReady`, copie presse-papiers non vide (SC-003).

Commande type (depuis la racine du repo) :

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Les tests instrumentés couvrent la persistance (`HealthCritiquePersistenceAndroidTest`) et le presse-papiers (`HealthCritiqueClipboardAndroidTest`). Ils exigent un appareil ou émulateur avec `ANDROID_HOME` / SDK configurés.
