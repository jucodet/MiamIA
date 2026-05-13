# Quickstart — valider l’ancrage (Feature C)

## Prérequis

- Build debug installé sur device ou émulateur avec runtime Gemma disponible.
- Segment ingrédients de test (ou chaîne mock **Feature A**) sous la main.

## Parcours manuel (MVP — **IHI-C-FR-006**)

1. Saisir ou capturer une liste courte **A** (ex. 3 ingrédients réels).
2. Lancer l’analyse composition ; vérifier que **chaque ligne** du bilan « fait produit » correspond à une **sous-chaîne** visible dans **A** (casse/espaces : uniquement si la politique mécanique v1 les autorise).
3. Retirer volontairement un ingrédient du segment validé **B** ⊂ **A** ; relancer : le système doit **rejeter** ou **non-analysable** si le modèle invente l’ingrédient retiré (tout ou rien).
4. Ouvrir critique santé avec le même segment : tout passage présenté comme **ce produit** doit référencer un terme **littéralement** dans le segment.
5. Si KPI additifs affichés : confirmer **attribution** visible « additifs / risques » (ou libellé équivalent) et qu’aucun additif KPI n’apparaît sans token correspondant dans le segment.

## Tests automatisés (cible)

- Ajouter / étendre tests unitaires sur `CompositionResultValidator` et, une fois défini, validateur critique pour jeux **contre-exemples** alignés **SC-C-002** / **SC-C-004**.

## Implémentation (2026-05-13)

- Les KPI additifs sont construits avec `BuildAdditiveKpiDisplay(bilan, rawLlmTextForParsing, validatedIngredientSegment)` : le **premier** texte est la sortie brute du modèle (parse `###ADDITIFS_RISQUE`), le **second** est le segment ingrédients validé (filtrage ancrage).

## Fichiers utiles

- `app/src/main/java/com/miamia/composition/CompositionResultValidator.kt`
- `app/src/main/java/com/miamia/healthcritique/HealthCritiqueEngine.kt`
- `app/src/main/java/com/miamia/additives/BuildAdditiveKpiDisplay.kt`
- `app/src/main/java/com/miamia/camera/CameraViewModel.kt` (orchestration)

---

## Quickstart — pastille kcal / 100 g (Feature K)

1. Lancer une analyse composition jusqu’au **succès** avec une sortie modèle incluant `###ENERGIE_ESTIMEE` et une valeur entière plausible (ex. `420`).
2. Vérifier en tête de carte résultat une pastille du type **analyse terminée** + nombre + **/ 100 g** + mention **estimée** (ou équivalent clair).
3. Refaire avec une valeur **hors 1..1100** ou section absente : aucun entier trompeur ; pastille cohérente (**US-K2**).
4. Confirmer que le libellé ne suggère pas une **déclaration nutritionnelle officielle**.

### Fichiers utiles (Feature K)

- `app/src/main/java/com/miamia/composition/GemmaBilanParser.kt`
- `app/src/main/java/com/miamia/composition/EnergyEstimateValidator.kt`
- `app/src/main/java/com/miamia/camera/BilanResultCard.kt`
