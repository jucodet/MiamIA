# Migration Index - user-guidance-experience

## Source -> Target

- `005-camera-start-temp-scan/spec.md` -> `spec.md` (`Scope`: etats UX scan) [validated]
- `010-message-bienvenue-sourire/spec.md` -> `spec.md` (`Functional Requirements`) [validated]
- `012-home-layout-mediapipe-status/spec.md` -> `spec.md` (`Functional Requirements`: ordre vertical home/readiness) [validated]
- `015-analyse-ocr-llm/spec.md` -> `spec.md` (`Functional Requirements`: redirection post-OCR, reprise erreur) [validated]

## Feature D — Suppression du message d'accueil (2026-05-13)

- **Rétractation explicite** de la portion d'exigence d'affichage de la bannière de message d'accueil introduite par la Feature 010 (`010-message-bienvenue-sourire`) et reflétée dans la Feature C de ce domaine.
- **Portée de la rétractation** : uniquement la **projection UI** sur l'écran capture (`CameraScreen.kt`). La **logique de sélection** (catalogue, sélecteur, policy, règles de ton) du package `app/src/main/java/com/miamia/welcome/` reste valide et exécutée côté `CameraViewModel` (flow `welcomeUiState` conservé mais désormais non consommé sur l'écran capture).
- **Impact tests** : nouveau test d'instrumentation `NoWelcomeBannerOnCaptureUiTest` (3 scénarios `ScanState`) atteste l'absence. Les tests `US1WelcomeAfterLoginFlowTest`, `US2PositiveToneWelcomeTest`, `US3EmptyCatalogNoMessageTest` restent **inchangés** — audit confirme qu'ils ne testent que la logique policy / sélecteur / règles de ton (pas le rendu Compose), donc non contradictoires avec la rétractation UI.
- **Statut** : specs/code mis à jour (`validated` côté repository) ; exécution de la suite instrumentée différée hors environnement sandbox (poste avec Android SDK).

## Feature E — Dix phrases humoristiques loaders (2026-05-13)

- **Revue copy / livraison (SC-E-002)** : les dix entrées ajoutées au catalogue `WAITING_PHRASES` (`app/src/main/java/com/miamia/ui/shared/WaitingPhrases.kt`) correspondent à l’annexe Feature E du `spec.md` ; ton aligné sur UGE-E-FR-002 et garde-fous UGE-E-FR-005.
- **Vérification auto** : `WaitingPhrasesCatalogFeatureETest` (JUnit) impose 21 entrées distinctes et la présence exacte des dix chaînes annexées.
- **Statut** : implémenté dans le dépôt ; tests instrumentés téléchargement à rejouer sur poste Android (SC-E-003).

## Feature F — Libellés capture, suppression test LLM (2026-05-13)

- **Spec** : `spec.md` Feature F (UGE-F-FR-001..004). **Plan** : `plan.md` courant.
- **Contrat UGE** : `contracts/capture-screen-feature-f-status-copy.md`. **Contrat technique aligné** : `specs/domains/capture-recognition/contracts/capture-action-bar.md` (révision même date).
- **Impact code attendu** : `CameraScreen.kt`, `CameraViewModel.kt`, `MediaPipeStatusViewState.kt`, `MainActivity.kt`, tests `CameraCaptureLayoutUiTest` / `CameraUnavailableLlmButtonUiTest`.
- **Statut** : `planned` (documentation livrée avec `/speckit.plan` ; implémentation à suivre via `/speckit.tasks` ou backlog).

## Conflict Decisions

- Owner decision: la contrainte de contenu du bilan n'est pas dans ce domaine; seule la mise en scene UX est documentee ici.

## Validation manuelle (quickstart)

- Scénarios `quickstart.md` : à rejouer sur appareil ou émulateur après merge (aperçu + bande d'action, flux photo → loader → écran résultat, abandon pendant chargement, indisponibilité caméra). Le parcours « Test LLM » depuis l'accueil est retiré (Feature F, 2026-05-13).
