# Tasks : Feature G — OCR direct, accueil sans chip ni ligne « prêt à scanner »

**Input** : `specs/domains/user-guidance-experience/` (plan.md, spec.md Feature G, research.md, data-model.md, contracts/, quickstart.md)  
**Prerequisites** : plan.md, spec.md  
**Tests** : Constitution ATDD — tests instrumentés / assertions UI mises à jour pour UGE-G-SC-002.

## Format

`- [ ] [TaskID] [P?] [Story?] Description avec chemin de fichier`

---

## Phase 1 : Setup

**Purpose** : cartographier le code à retirer ou à simplifier.

- [X] T001 Inventorier `SegmentConfirmationRequired`, `ingredients_framing_tag_chip`, `ingredientsFramingTagActive`, `CapturePreviewReadyStatusLabel`, `confirm_segment_button` dans `app/src/main/java/com/miamia/camera/` et `app/src/androidTest/java/com/miamia/camera/`

---

## Phase 2 : Fondations (tests / contrats avant merge)

**Purpose** : tests rouges → verts sur la chrome Feature G.

- [X] T002 [US2] Mettre à jour `app/src/androidTest/java/com/miamia/camera/CaptureScreenFeatureFUiTest.kt` : conserver assertions absence `camera_tab_llm_test_button` / « Aperçu caméra actif » ; remplacer l’exigence de texte « Caméra »+« scanner » par assertions UGE-G-SC-002 (`ingredients_framing_tag_chip` count 0 ; chaîne exacte « Caméra prête — vous pouvez scanner » count 0 ; `capture_scan_status_text` absent ou non affiché en `PreviewActive`)

---

## Phase 3 : User Story 1 (US-G1) — Enchaînement direct OCR → analyse (P1)

**Goal** : UGE-G-FR-001, UGE-G-FR-004, UGE-G-SC-001.

**Independent Test** : parcours manuel quickstart G2 ; absence `confirm_segment_button` après capture nominale.

- [X] T003 [US1] Dans `app/src/main/java/com/miamia/camera/CameraViewModel.kt`, après OCR `success`/`partial`, appeler le gate avec `implicitValidationFromIngredientsFraming = true` ; supprimer toute émission de `ScanState.SegmentConfirmationRequired` ; enchaîner `confirmSegmentAndAnalyze()` lorsque `submissionAllowed` ; retirer `rejectSegmentConfirmation()` et les champs `ingredientsFramingTagActive` / `setIngredientsFramingTagActive` / reset associé dans `onRetry`
- [X] T004 [US1] Retirer `data class SegmentConfirmationRequired` de `app/src/main/java/com/miamia/camera/ScanState.kt` et adapter les `when` exhaustifs si nécessaire

---

## Phase 4 : User Story 2 (US-G2) — Accueil épuré (P1)

**Goal** : UGE-G-FR-002, UGE-G-FR-003, UGE-G-SC-002.

**Independent Test** : `CaptureScreenFeatureFUiTest` + quickstart G1.

- [X] T005 [US2] Dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`, supprimer le `FilterChip` « Balise ingrédients », retirer la collecte `ingredientsFramingTagActive`, supprimer la branche UI `SegmentConfirmationRequired`, ne pas afficher de ligne de statut sous l’aperçu pour `PreviewActive` (conserver libellés utiles pour états transitoires hors chaîne interdite) ; retirer imports inutilisés (`FilterChip`, `Row` si orphelin)

---

## Phase 5 : Polish & traçabilité

- [X] T006 [P] Vérifier `rg "SegmentConfirmationRequired|ingredients_framing_tag_chip|Balise ingrédients|Caméra prête — vous pouvez scanner" app/src/main/java/com/miamia/camera/` → 0 occurrence résiduelle hors commentaires de migration si autorisés
- [X] T007 Confirmer cohérence avec `specs/domains/user-guidance-experience/contracts/capture-screen-feature-g-direct-scan.md` et scénarios G1–G2 du `specs/domains/user-guidance-experience/quickstart.md`

---

## Dependencies & Execution Order

| Phase | Dépend de | Bloque |
|-------|-----------|--------|
| 1 | — | — |
| 2 | T001 (recommandé) | validation CI tests |
| 3 US-G1 | T002 rédigé (ATDD strict) ou parallèle après T001 | T004 ordre avec T003 |
| 4 US-G2 | T003–T004 (ViewModel + ScanState) | T005 peut suivre T003–T004 |
| 5 | 3 + 4 | — |

**Ordre conseillé** : T001 → T002 → T003 → T004 → T005 → T006 → T007.

---

## Parallel Example

```bash
# Après impl (wrapper Gradle fonctionnel) :
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.miamia.camera.CaptureScreenFeatureFUiTest
```

---

## Implementation Strategy

**MVP** : Phases 3–4 (US-G1 + US-G2).  
**Polish** : Phase 5.

## Notes

- `AnalysisSubmissionGate` reste inchangé : les tests JVM `AnalysisSubmissionGateContractTest` / `AnalysisSubmissionDecisionAcceptanceTest` valident toujours les combinaisons `userConfirmed` / `implicit`.
- Le domaine `ingredient-normalization-validation` peut aligner specs secondaires (segmentation retirée) dans un lot ultérieur.

---

# Tasks : Feature I — Sélection du profil sur l'écran de capture (défaut Adulte, requise avant photo)

**Input** : `specs/domains/user-guidance-experience/` (plan.md Feature I, spec.md Feature I, research.md, data-model.md, contracts/capture-screen-profile-selection.md, quickstart.md)
**Prerequisites** : plan.md Feature I, spec.md Feature I
**Tests** : Constitution ATDD — test Robolectric provider + parcours quickstart UI.

## Format

`- [ ] [TaskID] [P?] [Story?] Description avec chemin de fichier`

---

## Phase 1 : Setup

**Purpose** : cartographier les points d'extension UGE ↔ IHI pour le profil.

- [X] T401 Inventorier le contrat `UserProfileProvider` / `UserProfile` (package `com.miamia.healthcritique`), la factory `HealthCritiqueViewModel.factory(applicationContext)`, et les hooks `CameraViewModel` / `MainActivity.prepareApplicationUi` à étendre dans `app/src/main/java/com/miamia/`

---

## Phase 2 : Fondations (contrat + provider persisté + tests)

**Purpose** : contrat UGE + impl persistance + tests rouges→verts.

- [X] T402 [P] Créer `app/src/main/java/com/miamia/profile/MutableUserProfileProvider.kt` : interface `MutableUserProfileProvider` étendant `com.miamia.healthcritique.UserProfileProvider` avec `fun setProfile(profile: UserProfile)`
- [X] T403 [P] Créer `app/src/main/java/com/miamia/profile/PersistentUserProfileProvider.kt` : impl `MutableUserProfileProvider` via `SharedPreferences` (clé `user_profile`, valeur `UserProfile.name`) ; `current()` résout via `valueOf` avec **repli `UserProfile.DEFAULT`** si absent/inconnu ; `setProfile` écrit `name`
- [X] T404 Créer `app/src/test/java/com/miamia/profile/PersistentUserProfileProviderTest.kt` (Robolectric) : défaut Adulte quand clé absente ; persistance `setProfile`→`current()` ; repli Adulte sur valeur corrompue/inconnue ; retourne les 5 profils valides

---

## Phase 3 : US-I1 — Voir / changer le profil sur l'écran de capture (défaut Adulte)

**Story goal** : l'utilisatrice voit un sélecteur de profil sur l'écran de capture, initialisé à « Adulte » par défaut, et peut changer de profil.
**Independent test criteria** : sélecteur `capture_profile_selector` visible sur l'écran de capture ; propose les 5 profils ; affiche « Adulte » au premier lancement ; `selectProfile` met à jour l'état + persiste.

- [X] T405 [US-I1] Étendre `app/src/main/java/com/miamia/camera/CameraViewModel.kt` : ajouter un paramètre `userProfileProvider: MutableUserProfileProvider` (défaut `PersistentUserProfileProvider(application.applicationContext)`) ; exposer `selectedProfile: StateFlow<UserProfile>` initialisé à `provider.current()` ; ajouter `fun selectProfile(profile: UserProfile)` (appelle `provider.setProfile` + met à jour l'état)
- [X] T406 [US-I1] Étendre `app/src/main/java/com/miamia/camera/CameraScreen.kt` : ajouter un composable `ProfileSelector` (menu déroulant Material3 listant `UserProfile.values()` via `label`, test tag `capture_profile_selector` + `capture_profile_option_<name>`), affiché en haut de l'écran de capture (sous `MediaPipeStatusIndicator`), visible dans tous les états de scan ; lié à `viewModel.selectedProfile` / `viewModel.selectProfile`
- [X] T407 [US-I1] Mettre à jour `app/src/main/java/com/miamia/MainActivity.kt` `prepareApplicationUi` : créer une instance unique `PersistentUserProfileProvider(applicationContext)`, l'injecter dans `CameraViewModel.factory` (écriture) et dans `HealthCritiqueViewModel.factory` (lecture)

---

## Phase 4 : US-I2 — Profil requis avant photo (gate) + publication vers la critique

**Story goal** : le profil est requis avant la prise de photo (satisfait par invariant défaut Adulte) et publié vers la critique via le provider partagé.
**Independent test criteria** : `canCapturePhoto()` tient compte de la validité du profil (toujours valide par défaut) ; `HealthCritiqueViewModel.factory` accepte un `UserProfileProvider` injecté et `analyze()` lit `current()`.

- [X] T408 [US-I2] Étendre `app/src/main/java/com/miamia/camera/CameraViewModel.kt` : garder `canCapturePhoto()` activé seulement si `selectedProfile.value` est un profil valide (garde-fou défensif — invariant défaut Adulte rend l'état invalide inaccessible) ; documenter UGE-I-FR-005/006
- [X] T409 [US-I2] Étendre `app/src/main/java/com/miamia/healthcritique/HealthCritiqueViewModel.kt` : faire accepter à `factory(application, userProfileProvider: UserProfileProvider = DefaultUserProfileProvider())` le provider injecté (passé à `HealthCritiqueViewModel`) ; `analyze()` lit `userProfileProvider.current()` (comportement déjà présent via `DefaultUserProfileProvider` — aligner sur l'instance partagée)

---

## Phase 5 : US-I3 — Persistance entre sessions

**Story goal** : le profil sélectionné persiste entre les lancements de l'app.
**Independent test criteria** : `PersistentUserProfileProvider` persiste via SharedPreferences (T403/T404) ; l'instance partagée (T407) garantit que la critique lit le profil sélectionné lors d'une session précédente.

- [X] T410 [US-I3] Vérifier (revue + test T404) que `selectProfile` → `setProfile` → relance app → `current()` retourne le profil persisté ; couvert par `PersistentUserProfileProviderTest` (parcours I3)

---

## Phase 6 : US-I4 — Modifier le profil avant une nouvelle capture

**Story goal** : après une analyse, l'utilisatrice peut changer de profil sur l'écran de capture avant une nouvelle capture.
**Independent test criteria** : « Nouveau scan » ramène à l'écran de capture ; le sélecteur affiche le profil persisté ; un changement de profil est pris en compte par la prochaine critique.

- [X] T411 [US-I4] Vérifier (revue) que `CameraViewModel.onRetry()` préserve le profil persisté (ne réinitialise pas `selectedProfile`) et que `selectProfile` reste disponible après un nouveau scan ; parcours quickstart I5

---

## Phase 7 : Polish & non-régression

**Purpose** : vérifications transverses et documentation.

- [X] T412 [P] Lancer `./gradlew :app:testDebugUnitTest --tests "com.miamia.profile.PersistentUserProfileProviderTest"` ; vérifier les tests IHI existants (`HealthCritiqueEngineTest`, `HealthCritiqueProfilePromptTest`, `HealthCritiqueSectionParserTest`) non régressés
- [X] T413 [P] Vérifier la compilation Material3 du sélecteur (`ExposedDropdownMenuBox`/`DropdownMenu`) selon la version Material3 du projet (1.2.1) dans `app/src/main/java/com/miamia/camera/CameraScreen.kt`

---

## Dépendances (Feature I)

```text
T401 (inventory)
   ├── T402 [P] + T403 [P] (contrat + provider)
   │        └── T404 (tests provider)
   ├── T405 [US-I1] (CameraViewModel profil)
   ├── T406 [US-I1] (CameraScreen sélecteur)
   └── T407 [US-I1] (MainActivity wiring)
              ├── T408 [US-I2] (gate capture)
              ├── T409 [US-I2] (HealthCritique factory)
              ├── T410 [US-I3] (persistance)
              └── T411 [US-I4] (modification)
                       └── T412 [P] + T413 [P] (polish)
```

## Exécution parallèle (Feature I)

- T402 et T403 sont parallèles (fichiers distincts, aucun cycle).
- T412 et T413 sont parallèles (vérifications distinctes).
- T405 / T406 / T407 dépendent de T402+T403 mais ciblent des fichiers différents → exécutables en parallèle après les fondations.

## Stratégie MVP (Feature I)

- **MVP** : US-I1 (T402→T407) — sélecteur visible, défaut Adulte, wiring provider partagé. Suffit pour activer la critique ciblée par profil (déjà implémentée Feature N) depuis l'écran de capture.
- Puis US-I2 (gate + publication), US-I3 (persistance déjà par construction), US-I4 (modification), polish.

## Format validation

Tous les tâches respectent `- [ ] [TaskID] [P?] [Story?] Description avec chemin de fichier`.
