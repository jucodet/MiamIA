# Implementation Plan: ingredient-health-intelligence

**Domain**: `specs/domains/ingredient-health-intelligence` | **Spec**: [spec.md](./spec.md)
**Dernière feature planifiée**: Feature P — compte rendu restructuré en 4 sections ordonnées + critique santé concise/visuelle centrée profil (2026-06-28)

> Plan cumulatif par feature. Sections historiques conservées pour traçabilité (constitution I).

---

## Feature K — Pastille kcal / 100 g (2026-05-13)

**Branch**: `016-launch-splash-screen` | **Date**: 2026-05-13  
**Input**: Spécification domaine + **clarify** 2026-05-13 (plage d’affichage **Option B** : **1 ≤ N ≤ 1100** kcal/100 g)

## Summary

La **Feature K** expose une pastille d’estimation énergétique (kcal/100 g) en tête du bilan composition (**Ref.** **UGE-A-FR-022**). Le code existant applique `EnergyEstimateValidator` avec une plage **1..950** ; la spec et le **clarify** imposent désormais **1..1100** (huiles et produits très denses). Ce plan : aligner le validateur, les tests JVM, et la documentation d’ingénierie (`research.md`, `data-model.md`, `quickstart.md`) sur **IHI-K-FR-006** / **IHI-K-SC-002**.

## Technical Context

**Language/Version**: Kotlin 2.x, Android (API min projet)  
**Primary Dependencies**: Jetpack Compose, module `composition`, UI résultat sous `camera` (`BilanResultCard`)  
**Storage**: N/A (valeur sur `CompositionBilan` en mémoire)  
**Testing**: JUnit 4, `app/src/test/java/com/miamia/composition/`  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: Aucun objectif nouveau ; parsing inchangé  
**Constraints**: Constitution ATDD ; pas de nombre trompeur hors plage (**US-K2**)  
**Scale/Scope**: `EnergyEstimateValidator.kt`, tests associés, docs domaine

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec + tests bornes |
| II. ATDD | OK — tests `EnergyEstimateValidatorTest` mis à jour |
| III. UX | OK — pastille inchangée, garde-fous renforcés |
| IV. Performance | OK — comparaison entière |
| V. Simplicité | OK — une constante de plage |
| VI. DDD | OK — IHI fournit la valeur ; UGE consomme (**contrat** `composition-energy-read-model.md`) |

**Post-design** : inchangé.

## Project Structure

### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── composition-energy-read-model.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/miamia/composition/
├── EnergyEstimateValidator.kt
├── GemmaBilanParser.kt
└── CompositionResultValidator.kt

app/src/test/java/com/miamia/composition/
└── EnergyEstimateValidatorTest.kt

app/src/main/java/com/miamia/camera/
├── BilanResultCard.kt
└── CompositionEnergyUiStrings.kt
```

**Structure Decision** : ajustement local au validateur et aux tests ; UI et parseur inchangés sauf cohérence des commentaires si besoin.

## Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0 — Recherche

Voir [research.md](./research.md) §9 (bornes **1..1100** — décision clarify **Option B**).

## Phase 1 — Design

- [data-model.md](./data-model.md) : `EstimatedEnergyPer100g` — plage **1..1100**.  
- [contracts/composition-energy-read-model.md](./contracts/composition-energy-read-model.md) : rappel borne côté IHI.  
- [quickstart.md](./quickstart.md) : scénario manuel hors plage aligné spec.

## Phase 2 — Livraison (hors scope de ce fichier)

Les tâches exécutables sont dans [tasks.md](./tasks.md) (commande `/speckit-tasks`).

---

## Feature L — Personnalisation du prompt de critique santé (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28  
**Input**: spec.md Feature L + **clarify** 2026-06-28 (5 questions résolues — remplacement en dur, vigilance transversale, critique seule, seuil en nombre d'ingrédients, relecture humaine MVP)

### Summary

La **Feature L** remplace le contenu du prompt de critique santé construit par `HealthCritiquePromptBuilder` : persona expert (nutrition clinique + cancérologie préventive), 5 dimensions de risque par ingrédient, hiérarchie faits établis / incertitudes / hypothèses (réf. CIRC/OMS), populations vulnérables élargies (immunodéprimées, antécédents familiaux cancer) en vigilance transversale, garde-fous éthiques renforcés, et format de sortie strict préservé (4 marqueurs + 3 blocs). Périmètre **critique seule** (bilan composition non modifié). Mécanisme : **remplacement en dur versionné** (pas d'externalisation).

### Technical Context

**Language/Version**: Kotlin 2.x, Android (API min projet)  
**Primary Dependencies**: module `healthcritique` existant (`HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, `HealthCritiqueConfig`)  
**Storage**: N/A (prompt construit en mémoire)  
**Testing**: JUnit 4, `app/src/test/java/com/miamia/healthcritique/` (`HealthCritiquePromptPrudenceTest`, `HealthCritiqueSectionParserTest`)  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: aucun objectif nouveau ; construction de prompt négligeable  
**Constraints**: Constitution ATDD ; non-régression du parseur de sections (`IHI-L-SC-005`) ; conformité Feature C préservée (`IHI-L-FR-014`)  
**Scale/Scope**: `HealthCritiquePromptBuilder.kt`, tests prudence/sections, docs domaine

### Constitution Check (Feature L)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature L + clarify → tests prudence/sections → code |
| II. ATDD | OK — tests builder (persona/dimensions/hiérarchie) + parser (4 marqueurs) avant/avec impl |
| III. UX | OK — format de sortie inchangé, non-régression UI |
| IV. Performance | OK — construction de prompt négligeable |
| V. Simplicité | OK — remplacement en dur, pas d'externalisation (clarify Q1) |
| VI. DDD | OK — Feature L cantonnée à critique (IHI), pas de fuite vers composition (clarify Q3) |

**Post-design** : inchangé.

### Project Structure (Feature L)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature L)
├── research.md (§10 Feature L)
├── data-model.md (entités Feature L)
├── quickstart.md (parcours Feature L)
├── contracts/
│   └── critique-prompt-contract.md
└── tasks.md (section Feature L)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/healthcritique/
├── HealthCritiquePromptBuilder.kt   # remplacement du contenu du prompt
└── HealthCritiqueConfig.kt          # seuil "liste très longue" (LONG_LIST_INGREDIENT_THRESHOLD)

app/src/test/java/com/miamia/healthcritique/
├── HealthCritiquePromptPrudenceTest.kt       # étendu : persona + 5 dimensions + populations vulnérables
└── HealthCritiqueSectionParserTest.kt        # non-régression : 4 marqueurs ordonnés inchangés
```

**Structure Decision** : ajustement local au builder (contenu du prompt) + une constante de seuil ; parseur, engine, UI et flux composition inchangés.

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature L)

Voir [research.md](./research.md) §10 (décisions clarify 2026-06-28 : mécanisme en dur, vigilance transversale, périmètre critique, seuil en nombre d'ingrédients, validation MVP).

### Phase 1 — Design (Feature L)

- [data-model.md](./data-model.md) : entités Feature L — `HealthCritiquePrompt`, `RiskDimension`, `EvidenceTier`, `VulnerablePopulation`, `CritiqueSectionMarker`.
- [contracts/critique-prompt-contract.md](./contracts/critique-prompt-contract.md) : contrat de contenu du prompt de critique (persona, dimensions, hiérarchie, format de sortie strict).
- [quickstart.md](./quickstart.md) : parcours manuel de relecture (jeu fixe) aligné `IHI-L-SC-008`.

### Phase 2 — Livraison (Feature L)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature L).

---

## Feature M — Accès UI à la critique santé (câblage de navigation) (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28  
**Input**: spec.md Feature M + clarify implicite (aucun NEEDS CLARIFICATION — spec bornée)

### Summary

La **Feature M** rend la critique santé accessible en production : `HealthCritiqueScreen` (écran d'entrée avec bouton « Analyser ») n'est aujourd'hui monté qu'en tests instrumentés. On ajoute une route `HealthCritiqueEntry` dans le `NavHost` de `MainActivity` et un bouton « Critique santé » dans `LlmResultScreen` (activé si segment validé disponible). Aucune modification du moteur, du prompt, du parseur ou du flux composition.

### Technical Context

**Language/Version**: Kotlin 2.x, Jetpack Compose, Navigation Compose  
**Primary Dependencies**: `CameraFlowRoutes`, `MainActivity` NavHost, `LlmResultScreen`, `HealthCritiqueScreen`, `HealthCritiqueViewModel`, `CameraViewModel.lastValidatedSegmentForHealth`  
**Storage**: N/A  
**Testing**: tests instrumentés existants (`HealthCritiqueReadOnlySegmentAndroidTest`) + parcours quickstart  
**Target Platform**: Application Android (module `app`)  
**Project Type**: mobile-app monolithique  
**Performance Goals**: aucun objectif nouveau  
**Constraints**: non-régression flux composition + chaîne `analyze()` → `navigateToResult` inchangée  
**Scale/Scope**: `CameraFlowRoutes.kt`, `LlmResultScreen.kt`, `MainActivity.kt`

### Constitution Check (Feature M)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature M → parcours quickstart → code |
| II. ATDD | OK — parcours quickstart + tests instrumentés existing |
| III. UX | OK — point d'entrée explicite, désactivé si pas de segment |
| IV. Performance | OK — navigation Compose négligeable |
| V. Simplicité | OK — réutilisation écrans/flux existants, 1 route + 1 bouton |
| VI. DDD | OK — périmètre navigation ; moteur/prompt/parseur/composition inchangés |

**Post-design** : inchangé.

### Project Structure (Feature M)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature M)
├── research.md (§11 Feature M)
├── data-model.md (entités Feature M)
├── quickstart.md (parcours Feature M)
├── contracts/
│   └── critique-sante-navigation-contract.md
└── tasks.md (section Feature M)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt   # + HealthCritiqueEntry
app/src/main/java/com/miamia/result/LlmResultScreen.kt        # + bouton "Critique santé" + callback onCritiqueSante
app/src/main/java/com/miamia/MainActivity.kt                  # + composable(HealthCritiqueEntry) + wire onCritiqueSante
```

**Structure Decision** : ajustement local — 1 constante de route, 1 bouton dans `LlmResultScreen` (avec callback), 1 entrée `composable` dans le `NavHost`. `HealthCritiqueScreen`, `HealthCritiqueViewModel`, `HealthCritiqueResultScreen` et la chaîne `analyze()` → `navigateToResult` inchangés.

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature M)

Voir [research.md](./research.md) §11 (décisions : réutilisation écran existant, point d'entrée depuis `LlmResultScreen`, signal de disponibilité segment, non-régression).

### Phase 1 — Design (Feature M)

- [data-model.md](./data-model.md) : entités Feature M — `HealthCritiqueEntryRoute`, `CritiqueSanteEntryTrigger`.
- [contracts/critique-sante-navigation-contract.md](./contracts/critique-sante-navigation-contract.md) : contrat de navigation (route + callback + condition d'activation).
- [quickstart.md](./quickstart.md) : parcours manuel bout-en-bout (scan → résultat composition → Critique santé → Analyser → sections).

### Phase 2 — Livraison (Feature M)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature M).

---

## Feature N — Critique santé ciblée par profil utilisateur (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28
**Input**: spec.md Feature N + **clarify** 2026-06-28 (5 décisions : fallback « Adulte » par défaut, liste compacte pour « Voir tous », « alertes » = KPI `additive-risk-insights`, suppression totale du flux 4-profils, profil modifiable dans « Paramètres / Profil » UGE)

### Summary

La **Feature N** remplace la sortie critique 4-profils par une **sortie à profil unique** correspondant au profil utilisateur sélectionné (`Femme enceinte` / `Enfant` / `Agé` / `Adulte` / `Sportif`). Le prompt de critique est adapté pour exiger un seul marqueur canonique par profil, précédé du rappel « Évalué pour vous : <profil> », et structuré en : (1) Niveau de prudence (Faible/Modéré/Élevé + texte court), (2) cartes d'ingrédients à vigilance (Modérée/Élevée) avec champs titre/type/Impact/Fait établi/Nuance/Cible particulièrement, (3) liste compacte de tous les ingrédients analysés (nom + statut). La restitution UI affiche le Niveau de prudence (jauge 3 paliers) juste sous les KPI `additive-risk-insights` existants, puis les cartes problématiques (filtrées), avec un bouton « Voir tous les ingrédients analysés ». En l'absence de profil sélectionné, fallback implicite sur « Adulte » avec signal visuel « profil par défaut ». Le format 4-marqueurs strict Feature L (**IHI-L-FR-009** / **IHI-L-SC-004**) est **supersédé et retiré** ; les exigences Feature L non format-strict (persona, dimensions, hiérarchie, garde-fous, populations vulnérables transversales, disclaimer, seuil liste longue) restent applicables. La sélection/persistance du profil est du ressort du domaine `user-guidance-experience` ; IHI **consomme** le profil via un contrat (`UserProfile` + `UserProfileProvider`).

### Technical Context

**Language/Version**: Kotlin 2.x, Jetpack Compose, Navigation Compose
**Primary Dependencies**: module `healthcritique` existant (`HealthCritiquePromptBuilder`, `HealthCritiqueSectionParser`, `HealthCritiqueEngine`, `HealthCritiqueViewModel`, `HealthCritiqueModels`, `HealthCritiqueResultScreen`, `HealthCritiqueConfig`) ; contrat profil `UserProfile` / `UserProfileProvider` (nouveau, IHI)
**Storage**: persistance profil = UGE (hors scope IHI) ; IHI conserve un provider par défaut « Adulte » fallback (en mémoire, settable pour tests)
**Testing**: JUnit 4, `app/src/test/java/com/miamia/healthcritique/` (`HealthCritiquePromptPrudenceTest`, `HealthCritiqueSectionParserTest`, + nouveau `UserProfileTest` / `HealthCritiqueProfilePromptTest`) ; UI via parcours quickstart
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique
**Performance Goals**: aucun objectif nouveau ; prompt ciblé réduit vs 4-profils (gain attendu de latence/token)
**Constraints**: Constitution ATDD ; non-régression Feature C (ancrage) ; suppression du flux 4-profils (parseur MUST rejeter 4-marqueurs) ; fallback « Adulte » sans rétropédalage silencieux
**Scale/Scope**: `HealthCritiquePromptBuilder.kt`, `HealthCritiqueSectionParser.kt`, `HealthCritiqueModels.kt`, `HealthCritiqueEngine.kt`, `HealthCritiqueViewModel.kt`, `HealthCritiqueResultScreen.kt`, nouveau `UserProfile.kt` / `UserProfileProvider.kt`, tests JVM, docs domaine

### Constitution Check (Feature N)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature N + clarify → tests prompt/parser/engine → code ; supersession 4-profils tracée en spec |
| II. ATDD | OK — tests JVM (prompt profil unique, parser marqueur unique + blocs, fallback) avant/avec impl |
| III. UX | OK — jauge prudence + cartes filtrées + « Voir tous » + rappel « Évalué pour vous » + signal « profil par défaut » |
| IV. Performance | OK — prompt ciblé (1 profil) vs 4-profils : réduction de tokens/latence |
| V. Simplicité | OK — suppression du flux 4-profils (une seule branche prompt/parseur/UI) ; profil via contrat simple |
| VI. DDD | OK — IHI consomme le profil via contrat `UserProfile`/`UserProfileProvider` ; saisie/persistance = UGE (cross-domain note) ; pas de fuite vers composition |

**Post-design** : inchangé.

### Project Structure (Feature N)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature N)
├── research.md (§12 Feature N)
├── data-model.md (entités Feature N)
├── quickstart.md (parcours Feature N)
├── contracts/
│   └── critique-profil-contract.md
└── tasks.md (section Feature N)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/healthcritique/
├── UserProfile.kt                 # NOUVEAU — enum 5 profils (label + marker) + default ADULTE
├── UserProfileProvider.kt         # NOUVEAU — interface + DefaultUserProfileProvider (fallback ADULTE)
├── HealthCritiquePromptBuilder.kt # adapté : buildSystemInstruction(profile) — marqueur unique + rappel
├── HealthCritiqueSectionParser.kt # adapté : parse profil unique (prudence + cartes + liste compacte)
├── HealthCritiqueModels.kt        # étendu : PrudenceLevel, IngredientRiskCard, FullIngredientStatut, ProfileCritiqueResult
├── HealthCritiqueEngine.kt        # adapté : analyze(profile) — passe profil au builder
├── HealthCritiqueViewModel.kt     # adapté : expose/consomme UserProfileProvider
└── HealthCritiqueResultScreen.kt  # adapté : restitution « Évalué pour vous » + jauge + cartes + « Voir tous »

app/src/test/java/com/miamia/healthcritique/
├── UserProfileTest.kt                     # NOUVEAU — labels/marqueurs/default
├── HealthCritiqueProfilePromptTest.kt     # NOUVEAU — prompt profil unique + rappel + blocs
└── HealthCritiqueSectionParserTest.kt     # étendu — parse marqueur unique + rejet 4-marqueurs
```

**Structure Decision** : ajout de 2 fichiers (profil contrat) + adaptation locale du builder, parseur, modèles, engine, ViewModel, écran de résultat ; tests JVM étendus/nouveaux. Le flux composition et `additive-risk-insights` ne sont pas modifiés (les KPI « alertes » sont juxtaposés via le contrat existant **IHI-C-FR-007**).

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature N)

Voir [research.md](./research.md) §12 (décisions clarify 2026-06-28 : fallback Adulte, liste compacte, alertes = KPI additifs, suppression 4-profils, profil modifiable UGE).

### Phase 1 — Design (Feature N)

- [data-model.md](./data-model.md) : entités Feature N — `UserProfile`, `UserProfileProvider`, `PrudenceLevel`, `IngredientRiskCard`, `FullIngredientStatutEntry`, `ProfileCritiqueResult`.
- [contracts/critique-profil-contract.md](./contracts/critique-profil-contract.md) : contrat de consommation du profil (IHI) + format de sortie profil unique + restitution (jauge, cartes, liste compacte).
- [quickstart.md](./quickstart.md) : parcours manuel + tests JVM Feature N (profil unique, rappel, jauge, cartes, « Voir tous », fallback Adulte, rejet 4-marqueurs).

### Phase 2 — Livraison (Feature N)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature N).

---

## Feature O — Critique santé intégrée à l'écran principal des résultats (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28
**Input**: spec.md Feature O + **clarify** 2026-06-28 (2 décisions : déclenchement **automatique** au `Complete` + segment validé ; **suppression** des écrans/route séparés `HealthCritiqueScreen` / `HealthCritiqueEntry` / `HealthCritiqueResultScreen` / `HealthCritiqueResult`)

### Summary

La **Feature O** supersède la Feature M : la critique santé par profil (Feature N) n'est plus atteinte via un bouton naviguant vers un écran d'entrée séparé, mais **rendue directement sur l'écran principal des résultats** (`LlmResultScreen`), en continuité sous le bilan composition / pastille kcal / KPI additifs juxtaposés. Le déclenchement est **automatique** dès que le bilan composition est classé `Complete` et qu'un segment validé est disponible (`lastValidatedSegmentForHealth` non vide). Les écrans séparés `HealthCritiqueScreen` (entrée) et `HealthCritiqueResultScreen` (résultat), ainsi que les routes `HealthCritiqueEntry` et `HealthCritiqueResult`, sont **supprimés** ; la restitution (rappel « Évalué pour vous », avertissements, jauge de prudence, cartes filtrées, « Voir tous les ingrédients analysés », disclaimers, actions copier) est rendue **inline** sur `LlmResultScreen`. Les états `en cours` (loading + streaming), `erreur` (`InferenceError` / `InputInvalid`) et `prête` (`CritiqueReady`) du `HealthCritiqueViewModel` sont rendus inline. `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N) et `HealthCritiqueSectionParser` sont **inchangés** ; conformité Feature C et garde-fous Feature L/N préservés.

### Technical Context

**Language/Version**: Kotlin 2.x, Jetpack Compose, Navigation Compose
**Primary Dependencies**: `LlmResultScreen`, `MainActivity` (NavHost), `CameraFlowRoutes`, `HealthCritiqueViewModel` (états + `analyze()` + `ui`/`streamingText`/`navigateToResult`), `CameraViewModel.lastValidatedSegmentForHealth` + `streamingBilan` (`StreamingBilanState.Complete`), `HealthCritiqueResultScreen` (composables de restitution à réutiliser/extraire en inline), `LastHealthAnalysisStore`
**Storage**: `LastHealthAnalysisStore` (conservé, sans écran séparé)
**Testing**: tests instrumentés existants (`LlmResultScreenUiTest`, `HealthCritiqueReadOnlySegmentAndroidTest`, `HealthCritiquePersistenceAndroidTest`) à adapter ; parcours quickstart Feature O
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique
**Performance Goals**: la critique est une seconde inférence LLM (jusqu'au timeout Feature A 180 s) ; rendu **non-bloquant** (bilan composition affiché immédiatement, critique en streaming inline). Pas de nouvel objectif chiffré ; non-régression latence bilan.
**Constraints**: Constitution ATDD ; non-régression flux composition + KPI additifs juxtaposés (`IHI-C-FR-007`) ; idempotence du déclenchement automatique (`IHI-O-FR-013`) ; annulation propre au retour (pas de fuite d'inférence) ; moteur/prompt/parseur inchangés
**Scale/Scope**: `LlmResultScreen.kt`, `MainActivity.kt` (NavHost), `CameraFlowRoutes.kt`, extraction de composables restitution depuis `HealthCritiqueResultScreen.kt`, suppression `HealthCritiqueScreen.kt` + `HealthCritiqueResultScreen.kt` + routes, tests instrumentés, docs domaine

### Constitution Check (Feature O)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature O + clarify → parcours quickstart + tests instrumentés → code ; supersession Feature M tracée en spec/plan/contract |
| II. ATDD | OK — parcours quickstart Feature O (auto-trigger, restitution inline, états erreur/chargement, suppression navigation) + tests instrumentés adaptés |
| III. UX | OK — suppression friction (bouton + navigation) ; feedback immédiat (streaming inline) ; états vides/erreurs soignés inline |
| IV. Performance | OK — seconde inférence non-bloquante (streaming inline) ; bilan composition rendu sans attendre la critique |
| V. Simplicité | OK — suppression de 2 écrans + 2 routes + 1 bouton (réduction de surfaces) ; réutilisation des composables de restitution existants |
| VI. DDD | OK — périmètre IHI (critique santé + restitution) ; moteur/prompt/parseur/composition inchangés ; UGE non modifié (Ref. ordonnancement écran résultat) |

**Post-design** : inchangé.

### Project Structure (Feature O)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature O)
├── research.md (§13 Feature O)
├── data-model.md (entités Feature O)
├── quickstart.md (parcours Feature O)
├── contracts/
│   └── critique-inline-restitution-contract.md   # NOUVEAU (supersede critique-sante-navigation-contract.md)
└── tasks.md (section Feature O)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/result/LlmResultScreen.kt        # section critique inline + auto-trigger (collecte streamingBilan/segment)
app/src/main/java/com/miamia/MainActivity.kt                  # NavHost : retirer HealthCritiqueEntry/HealthCritiqueResult ; injecter HealthCritiqueViewModel dans LlmResultScreen ; auto-trigger wiring
app/src/main/java/com/miamia/navigation/CameraFlowRoutes.kt   # retirer HealthCritiqueEntry + HealthCritiqueResult
app/src/main/java/com/miamia/healthcritique/
├── HealthCritiqueScreen.kt          # SUPPRIMÉ
├── HealthCritiqueResultScreen.kt    # SUPPRIMÉ (composables de restitution extraits vers LlmResultScreen ou module partagé)
└── HealthCritiqueViewModel.kt       # inchangé (expose analyze/ui/streamingText/navigateToResult) ; navigateToResult devient no-op ou est retiré

app/src/androidTest/java/com/miamia/healthcritique/   # tests adaptés (retrait navigation, ajout auto-trigger inline)
```

**Structure Decision** : retrait de 2 écrans séparés + 2 routes + 1 bouton ; extraction des composables de restitution (`CritiqueProfileContent`, `PrudenceGauge`, `IngredientRiskCardItem`, `FullIngredientListToggle`) vers une section inline de `LlmResultScreen` (ou un composable partagé `InlineCritiqueSection`) ; déclenchement automatique via `LaunchedEffect(streamingBilan)` observant `Complete` + segment validé. `HealthCritiqueEngine` / `HealthCritiquePromptBuilder` / `HealthCritiqueSectionParser` / flux composition **inchangés**.

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature O)

Voir [research.md](./research.md) §13 (décisions clarify 2026-06-28 : déclenchement automatique, suppression écrans/route séparés, extraction composables restitution, idempotence, annulation, non-régression).

### Phase 1 — Design (Feature O)

- [data-model.md](./data-model.md) : entités Feature O — `InlineCritiqueSection`, `CritiqueAutoTrigger` ; retrait (superseded) `HealthCritiqueEntryRoute`, `CritiqueSanteEntryTrigger`.
- [contracts/critique-inline-restitution-contract.md](./contracts/critique-inline-restitution-contract.md) : contrat de restitution inline + auto-trigger (supersede `critique-sante-navigation-contract.md`).
- [quickstart.md](./quickstart.md) : parcours manuel Feature O (auto-trigger au Complete, restitution inline, états erreur/chargement, suppression navigation, non-régression composition).

### Phase 2 — Livraison (Feature O)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature O).

## Feature P — Compte rendu restructuré (4 sections ordonnées) + critique santé concise/visuelle par profil (2026-06-28)

**Branch**: `016-launch-splash-screen` (branche courante domaine) | **Date**: 2026-06-28
**Input**: spec.md Feature P + **clarify** implicite (spec bornée — 4 questions résolues en clarifications : intégration kcal/KPI dans Synthèse, Verdict = détail Feature N renommé, suppression liste brute sans impact ancrage, pas de mode comparer tous les profils)

### Summary

La **Feature P** restructure l'écran de compte rendu (`LlmResultScreen` / `BilanResultCard`) en **exactement 4 sections ordonnées fixes** : **Produit identifié** → **Synthèse** → **Verdict par ingrédient** → **Critique santé**. L'affichage brut de la **liste des ingrédients identifiés** (`IngredientsSection` / carte streaming « Ingrédients identifiés ») est **supprimé** ; le `ValidatedIngredientSegment` reste entrée d'analyse (ancrage Feature C), simplement non exposé à plat. La **pastille kcal** (Feature K) et les **KPI additifs juxtaposés** (`additive-risk-insights`, `IHI-C-FR-007`) sont **intégrés à la section « Synthèse »** (plus de section « Additifs » autonome). La **Critique santé** (section 4, `InlineCritiqueSection`) évolue vers une forme **concise et visuelle** : rappel « Évalué pour vous : <profil> » en tête, puis une **mise en évidence des risques spécifiques au profil sélectionné** (pastilles/étiquettes colorées courtes, une par ingrédient à vigilance Modérée/Élevée), puis la **jauge 3 paliers** ; les cartes détaillées Feature N restent accessibles en **repli dépliable** (profondeur non dominante). `HealthCritiqueEngine`, `HealthCritiquePromptBuilder` (Feature L/N), `HealthCritiqueSectionParser` et le flux composition sont **inchangés** ; conformité Feature C et garde-fous Feature L/N préservés.

### Technical Context

**Language/Version**: Kotlin 2.x, Jetpack Compose, Material 3
**Primary Dependencies**: `LlmResultScreen.kt` (ordre des sections + état `Complete`), `BilanResultCard.kt` (`IngredientsSection` à supprimer, `AnalysisSection`/`AdditivesSection`/`CompositionEnergyPastille` à fusionner dans « Synthèse », `ProductSection`/`HealthImpactSection` à rendre inconditionnels), `InlineCritiqueSection.kt` (ajout `ProfileRiskHighlights` concis/visuel en tête de `CritiqueProfileContent`), `HealthCritiqueModels.kt` (`IngredientRiskCard` existant — réutilisé pour les pastilles), `CompositionBilan`, `AdditiveKpiPanel`, `MiamIAColors`
**Storage**: inchangé (`LastHealthAnalysisStore` conservé)
**Testing**: tests instrumentés existants (`LlmResultScreenUiTest`, `HealthCritiqueReadOnlySegmentAndroidTest`, `HealthCritiquePersistenceAndroidTest`) à adapter (retrait `bilan_ingredients_section` / `streaming_ingredients_card`, attente 4 sections, attente pastilles risques profil) ; parcours quickstart Feature P
**Target Platform**: Application Android (module `app`)
**Project Type**: mobile-app monolithique
**Performance Goals**: aucun nouvel objectif chiffré ; la critique reste une seconde inférence non-bloquante (Feature O) ; le rendu 4-sections est plus léger (suppression d'une section + meta allégée).
**Constraints**: Constitution ATDD ; **exactement 4 sections** stables sur tous les états du compte rendu (`IHI-P-FR-001`/`IHI-P-SC-008`) ; suppression liste brute d'ingrédients (`IHI-P-FR-002`) ; intégration kcal/KPI dans Synthèse (`IHI-P-FR-003`) ; concision/visuel critique + risques profil en tête (`IHI-P-FR-005`/`007`) ; non-régression Feature C / N / O ; moteur/prompt/parseur/composition inchangés (`IHI-P-FR-012`)
**Scale/Scope**: `BilanResultCard.kt`, `LlmResultScreen.kt`, `InlineCritiqueSection.kt`, tests instrumentés, docs domaine (plan/research/data-model/quickstart/contract)

### Constitution Check (Feature P)

| Principe | Statut |
|----------|--------|
| I. Qualité / traçabilité | OK — spec Feature P → parcours quickstart + tests instrumentés → code ; pas de supersession (Feature N/O préservées) |
| II. ATDD | OK — parcours quickstart Feature P (4 sections, absence liste brute, concision/visuel critique, risques profil en tête) + tests instrumentés adaptés |
| III. UX | OK — ordre prévisible 4 sections + scan rapide (pastilles risques profil) ; états vides neutres (produit non identifié, aucun ingrédient à vigilance) ; charge visuelle réduite |
| IV. Performance | OK — suppression d'une section (rendu plus léger) ; critique reste non-bloquante (Feature O inchangée) |
| V. Simplicité | OK — suppression de la section « Ingrédients identifiés » et de la section « Additifs » autonome (fusion dans Synthèse) ; réduction de surfaces ; profondeur conservée en repli |
| VI. DDD | OK — périmètre IHI (compte rendu + restitution critique) ; moteur/prompt/parseur/composition inchangés ; UGE non modifié (Ref. ordonnancement écran résultat) |

**Post-design** : inchangé.

### Project Structure (Feature P)

#### Documentation (this feature)

```text
specs/domains/ingredient-health-intelligence/
├── plan.md (section Feature P)
├── research.md (§14 Feature P)
├── data-model.md (entités Feature P)
├── quickstart.md (parcours Feature P)
├── contracts/
│   └── report-layout-contract.md   # NOUVEAU — contrat de mise en page 4 sections du compte rendu
└── tasks.md (section Feature P)
```

#### Source Code (repository root)

```text
app/src/main/java/com/miamia/camera/BilanResultCard.kt        # retrait IngredientsSection ; fusion kcal+AdditivesSection dans AnalysisSection (Synthèse) ; ProductSection/HealthImpactSection inconditionnels (états neutres)
app/src/main/java/com/miamia/result/LlmResultScreen.kt        # retrait carte streaming « Ingrédients identifiés » (StreamingContent) ; ordre 4 sections préservé (BilanResultCard → InlineCritiqueSection)
app/src/main/java/com/miamia/healthcritique/InlineCritiqueSection.kt   # ajout ProfileRiskHighlights (pastilles visuelles risques profil) en tête de CritiqueProfileContent, avant PrudenceGauge
app/src/androidTest/java/com/miamia/result/LlmResultScreenUiTest.kt    # adapté (attente 4 sections, absence bilan_ingredients_section/streaming_ingredients_card, attente pastilles risques)
```

**Structure Decision** : `BilanResultCard` expose désormais **3 sections** (Produit identifié, Synthèse, Verdict par ingrédient) + meta (BilanHeader, Disclaimer, InferenceTimeBadge, RawTranscriptToggle) ; la **4ᵉ section (Critique santé)** est rendue par `InlineCritiqueSection` dans `LlmResultScreen` en continuité (Feature O inchangée). La `Synthèse` absorbe la pastille kcal et le panneau KPI additifs (attribution explicite `IHI-C-FR-007` préservée via `AdditiveKpiPanel`). `InlineCritiqueSection` ajoute une couche concise `ProfileRiskHighlights` (pastilles colorées par `IngredientRiskCard`) entre le rappel « Évalué pour vous » et la jauge ; les cartes détaillées restent repliables (inchangées).

### Complexity Tracking

> Aucune violation constitutionnelle à justifier.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

### Phase 0 — Recherche (Feature P)

Voir [research.md](./research.md) §14 (décisions : intégration kcal/KPI dans Synthèse, retrait section Ingrédients identifiés sans impact ancrage, états neutres pour sections inconditionnelles, pastilles risques profil concises/visuelles, profondeur en repli, non-régression Feature N/O).

### Phase 1 — Design (Feature P)

- [data-model.md](./data-model.md) : entités Feature P — `ReportSection`, `ProduitIdentifie`, `Synthese`, `VerdictParIngredient`, `ConciseVisualCritique`, `ProfileRiskHighlight` ; retrait (UI) `RawIngredientListDisplay`.
- [contracts/report-layout-contract.md](./contracts/report-layout-contract.md) : contrat de mise en page du compte rendu (4 sections ordonnées fixes + intégration kcal/KPI dans Synthèse + suppression liste brute + critique concise/visuelle).
- [quickstart.md](./quickstart.md) : parcours manuel Feature P (4 sections, absence liste brute, pastilles risques profil, jauge, cartes en repli, états neutres, non-régression composition/critique).

### Phase 2 — Livraison (Feature P)

Tâches exécutables dans [tasks.md](./tasks.md) (section Feature P).
