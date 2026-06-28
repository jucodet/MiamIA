# Implementation Plan: User guidance & experience — Feature I (Sélection du profil sur l'écran de capture)

**Branch**: `016-launch-splash-screen` (courante) | **Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md) (Feature I)
**Input**: Spécification domaine `specs/domains/user-guidance-experience/spec.md` (Feature I)
**Dernière feature planifiée**: Feature I — sélection du profil utilisateur sur l'écran de capture (2026-06-28)

> Plan cumulatif par feature. Section historique Feature G conservée pour traçabilité (constitution I).

## Summary (Feature I)

La **Feature I** introduit la **sélection du profil utilisateur sur l'écran de prise de photo** : un sélecteur des 5 profils (`Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif`) est rendu sur l'écran de capture, initialisé à **« Adulte » par défaut**, et le profil MUST être renseigné avant la prise de photo (gate de capture). Le profil sélectionné est **persisté entre sessions** (stockage local) et **publié via le contrat `UserProfileProvider`** défini par `ingredient-health-intelligence` (Feature N), afin que la critique santé soit ciblée pour le profil sélectionné. La sélection vit sur l'écran de capture — **supersession** de l'hypothèse Feature N (clarify Q5) d'un écran « Paramètres / Profil » distinct. En cas de profil persisté illisible/corrompu, repli sur « Adulte » (défaut) sans interruption.

## Technical Context (Feature I)

**Language/Version**: Kotlin 2.x, Jetpack Compose, Android (API min 26)
**Primary Dependencies**: contrat `UserProfile` / `UserProfileProvider` (défini dans `com.miamia.healthcritique`, Feature N — Published Language) ; modules `camera` (`CameraScreen`, `CameraViewModel`), `healthcritique` (`HealthCritiqueViewModel` factory)
**Storage**: persistance locale du profil via `SharedPreferences` (clé `user_profile`, valeur = `UserProfile.name`) ; pas de backend
**Testing**: JUnit 4 + Robolectric (`app/src/test`) pour le provider persisté ; tests JVM sur `UserProfile` (déjà couverts côté IHI) ; parcours quickstart pour la UI
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique
**Performance Goals**: aucun objectif nouveau (sélecteur UI léger, lecture prefs synchrone)
**Constraints**: Constitution ATDD ; frontières DDD — UGE fournit l'implémentation persistée de `UserProfileProvider` + l'UI ; l'énumération `UserProfile` reste publiée par IHI (pas de redéfinition) ; non-régression capture/critique
**Scale/Scope**: nouveau `profile/` package (`MutableUserProfileProvider`, `PersistentUserProfileProvider`) ; `CameraViewModel` (selectedProfile, selectProfile) ; `CameraScreen` (sélecteur) ; `HealthCritiqueViewModel.factory` (provider injecté) ; `MainActivity` (wiring provider partagé) ; tests

## Constitution Check (Feature I)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature I + clarify (décisions par défaut) → tests provider/UI → code |
| II. ATDD | OK — test Robolectric provider (défaut Adulte, persistance, repli corrompu) + parcours quickstart UI (sélecteur, gate) |
| III. UX | OK — sélecteur visible sur l'écran de capture, défaut Adulte (aucune friction pour capturer), modification libre avant capture |
| IV. Performance | OK — aucun travail lourd (prefs locales, sélecteur en mémoire) |
| V. Simplicité | OK — un provider persisté unique partagé capture↔critique ; pas d'écran paramètres supplémentaire (supersession) |
| VI. DDD | OK — UGE implémente `UserProfileProvider` (contrat IHI) ; `UserProfile` non redéfinie ; persistance = UGE, consommation = IHI |

**Post-design** : inchangé.

## Project Structure (Feature I)

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md (section Feature I)
├── research.md (Feature I decisions)
├── data-model.md (Feature I addendum)
├── quickstart.md (Feature I parcours)
├── contracts/capture-screen-profile-selection.md
└── tasks.md (section Feature I)
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/profile/
├── MutableUserProfileProvider.kt     # NOUVEAU — interface (extends UserProfileProvider) + setProfile
└── PersistentUserProfileProvider.kt  # NOUVEAU — impl SharedPreferences, repli Adulte

app/src/main/java/com/miamia/camera/
├── CameraViewModel.kt                # étendu : selectedProfile StateFlow + selectProfile()
└── CameraScreen.kt                   # étendu : ProfileSelector sur l'écran de capture + gate

app/src/main/java/com/miamia/healthcritique/
└── HealthCritiqueViewModel.kt        # factory : UserProfileProvider injecté (provider partagé)

app/src/main/java/com/miamia/MainActivity.kt  # wiring : provider partagé → Camera + HealthCritique

app/src/test/java/com/miamia/profile/
└── PersistentUserProfileProviderTest.kt      # NOUVEAU (Robolectric) — défaut, persistance, repli
```

**Structure Decision** : ajout d'un package `profile/` (UGE) pour le contrat persisté + impl SharedPreferences ; adaptations localisées du `CameraViewModel` (état profil), `CameraScreen` (sélecteur UI), `HealthCritiqueViewModel.factory` (provider injecté) et `MainActivity` (wiring du provider partagé). Le flux composition et la logique critique (IHI) ne sont pas modifiés au-delà de la consommation du provider.

## Complexity Tracking (Feature I)

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature I)

Voir [research.md](./research.md) — Feature I decisions (mécanisme de persistance, gate de capture, partage provider capture↔critique, repli Adulte, supersession écran paramètres).

### Phase 1 — Design (Feature I)

- [data-model.md](./data-model.md) — Feature I addendum (`MutableUserProfileProvider`, `PersistentUserProfileProvider`, `SelectedProfile` read-model).
- [contracts/capture-screen-profile-selection.md](./contracts/capture-screen-profile-selection.md) — contrat UI de sélection + publication `UserProfileProvider`.
- [quickstart.md](./quickstart.md) — parcours Feature I (défaut Adulte, sélection, persistance, repli, gate).

### Phase 2 — Livraison (Feature I)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature I).

---

## Feature G — Historique (OCR direct, accueil épuré)

**Branch**: `016-full-ocr-llm` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md) (Feature G, révisions Feature F)
**Input**: Spécification domaine `specs/domains/user-guidance-experience/spec.md`

## Summary

La **Feature G** impose : (1) enchaînement **OCR → analyse LLM** sans écran intermédiaire de relecture du transcript ; (2) écran capture **sans** chip « balise ingrédients » et **sans** la chaîne exacte « Caméra prête — vous pouvez scanner » ni ligne d’invitation obligatoire sous l’aperçu pour l’état prêt ; (3) retrait du parcours nominal vers `SegmentConfirmationRequired`. L’approche technique : `CameraViewModel` passe toujours une validation implicite au `AnalysisSubmissionGate` pour les prévisualisations gate (équivalent produit « analyse directe sur transcript complet »), supprime l’état `SegmentConfirmationRequired` du modèle d’état, et simplifie `CameraScreen` (chrome capture). Le domaine **`ingredient-normalization-validation`** reste la référence pour retirer progressivement la segmentation hors chemin nominal (déjà alignée sur transcript complet côté gate).

## Technical Context

**Language/Version**: Kotlin 2.x, Android (API min 26 cible projet)  
**Primary Dependencies**: Jetpack Compose, CameraX, coroutines, modules internes `camera`, `analysis.ingredientsegment`, `composition`  
**Storage**: N/A (états en mémoire ViewModel)  
**Testing**: JUnit 4/5, tests JVM `app/src/test`, AndroidTest `app/src/androidTest`  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: Pas de régression sur latence capture → navigation résultat (inchangé hors suppression d’étapes UI)  
**Constraints**: Constitution ATDD ; frontières DDD — comportement UX documenté ici, garde-fous transcript dans domaine ingrédients  
**Scale/Scope**: Écran capture + `CameraViewModel` + `ScanState` ; tests UI associés

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature G + contrats + tâches + tests UI |
| II. ATDD | OK — mise à jour `CaptureScreenFeatureFUiTest` / assertions absence chip & chaîne interdite |
| III. UX | OK — réduction friction (scan direct) |
| IV. Performance | OK — pas d’objectif nouveau ; pas de travail lourd ajouté |
| V. Simplicité | OK — suppression branches UI et état mort |
| VI. DDD | OK — UX et navigation dans UGE ; gate/transcript déjà partagés avec `ingredient-normalization-validation` via contrat implicite |

**Post-design** : inchangé.

## Project Structure

### Documentation (this feature)

```text
specs/domains/user-guidance-experience/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/camera/
├── CameraScreen.kt
├── CameraViewModel.kt
├── ScanState.kt
app/src/androidTest/java/com/miamia/camera/
├── CaptureScreenFeatureFUiTest.kt
app/src/main/java/com/miamia/analysis/ingredientsegment/
├── AnalysisSubmissionGate.kt   # inchangé comportement contractuel ; appelant force implicit=true
```

**Structure Decision** : modifications localisées au module capture et tests instrumentés associés ; pas de nouveau module.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
