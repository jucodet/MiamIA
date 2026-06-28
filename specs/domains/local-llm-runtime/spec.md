# Domain Spec - local-llm-runtime

> **Status**: Active — feature `026-inference-backend-badge` spécifiée 2026-06-28
> **Created**: 2026-05-12 (sync-apply P15) · **Updated**: 2026-06-28
> **Source packages**: `app/src/main/java/com/miamia/gemma4local/` (15 fichiers, ~902 lignes)

## Purpose

Gérer le cycle de vie complet du modèle LLM local (Gemma) : disponibilité, chargement, exécution d'inférence, gestion des erreurs runtime, métriques et observabilité — incluant la restitution visuelle du backend d'exécution (NPU / GPU / CPU) auprès de l'info de durée d'inférence.

## Scope

- Disponibilité et chargement du modèle Gemma local (`Gemma4LocalAvailabilityChecker`, `HybridGemma4LocalGateway`)
- Exécution d'inférence locale (`Gemma4LocalClient`, requête → réponse streaming)
- Gestion des erreurs runtime (modèle absent, échec chargement, timeout) via `Gemma4LocalErrorMapper`
- Métriques et observabilité (latence, classe d'appareil, backend d'exécution via `DeviceClassResolver`)
- Restitution visuelle du backend d'exécution (pastille NPU/GPU/CPU) à côté de la durée d'inférence
- Import et téléchargement du modèle (`GemmaModelDownloader`, `GemmaModelImportManager`)
- Configuration (`Gemma4LocalConfig`)

## Invariants

- Le backend d'exécution est une donnée technique constatée après (ou pendant) l'inférence ; il n'est jamais choisi ni simulé par la couche présentation.
- Une et une seule pastille backend est affichée par résultat d'inférence présenté à l'utilisateur.
- La pastille backend reflète le backend réellement utilisé pour l'inférence, pas le backend préféré ou disponible.
- La durée d'inférence et la pastille backend proviennent de la même exécution d'inférence (cohérence temporelle).

---

# Feature Specification: Inference Backend Badge

**Feature Branch**: `026-inference-backend-badge`
**Domain Context**: `LocalLlmRuntimeContext`
**Target Domain Folder**: `specs/domains/local-llm-runtime`
**Created**: 2026-06-28
**Status**: Draft
**Input**: User description: "à côté de l'info durée d'inférence, affiche un logo pastille selon si l'inférence s'est exécutée sur NPU, GPU ou CPU"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Identifier le backend d'exécution au premier coup d'œil (Priority: P1)

Après une analyse d'ingrédients, l'utilisateur consulte le résultat. À côté de la durée d'inférence déjà affichée, une petite pastille (logo) indique immédiatement sur quel type de matériel l'inférence s'est exécutée : NPU, GPU ou CPU. Cela permet à l'utilisateur averti de comprendre la qualité d'accélération dont il bénéficie sur son appareil.

**Why this priority**: C'est la valeur coeur de la feature — rendre visible l'information de backend déjà collectée par le runtime mais non exposée à l'utilisateur. Sans cette story, la feature ne livre aucune valeur.

**Independent Test**: Peut être testé en exécutant une inférence sur un appareil donné et en vérifiant que la pastille affichée correspond au backend réellement utilisé (NPU/GPU/CPU), indépendamment de toute autre story.

**Acceptance Scenarios**:

1. **Given** une inférence terminée avec succès sur NPU, **When** l'utilisateur consulte le résultat, **Then** une pastille "NPU" est affichée à côté de la durée d'inférence.
2. **Given** une inférence terminée avec succès sur GPU, **When** l'utilisateur consulte le résultat, **Then** une pastille "GPU" est affichée à côté de la durée d'inférence.
3. **Given** une inférence terminée avec succès sur CPU, **When** l'utilisateur consulte le résultat, **Then** une pastille "CPU" est affichée à côté de la durée d'inférence.
4. **Given** plusieurs résultats d'inférence successifs sur des backends différents, **When** l'utilisateur consulte chaque résultat, **Then** chaque pastille reflète le backend de sa propre exécution.

---

### User Story 2 - Distinguer visuellement les trois backends (Priority: P2)

Chaque backend (NPU, GPU, CPU) possède une pastille visuellement distincte (icône et/ou couleur dédiée) afin que l'utilisateur reconnaisse le backend sans avoir à lire le texte, même en parcours rapide.

**Why this priority**: L'identification texte seule est fonctionnelle mais la différenciation visuelle est ce qui rend la pastille réellement "pastille" (logo) et non un simple libellé. Utile mais secondaire vs. la présence de l'info.

**Independent Test**: Présenter les trois pastilles côte à côte et vérifier qu'elles sont visuellement distinguables (icône/couleur) sans lecture du texte.

**Acceptance Scenarios**:

1. **Given** les trois backends possibles, **When** on affiche les trois pastilles, **Then** chacune a un visuel (icône ou couleur) distinct des deux autres.
2. **Given** un utilisateur en parcours rapide, **When** il aperçoit la pastille, **Then** il peut identifier le backend par le visuel seul, sans lire le libellé texte.

---

### User Story 3 - Cas d'inférence échouée ou backend indéterminé (Priority: P3)

Lorsque l'inférence échoue ou que le backend ne peut être déterminé, la zone de la pastille gère ce cas de manière explicite et non ambigüe, sans afficher une pastille backend trompeuse.

**Why this priority**: Robustesse et cohérence — éviter une pastille erronée ou absente silencieusement. Priorité plus basse car le cas nominal reste l'inférence réussie.

**Independent Test**: Provoquer une inférence échouée ou un backend non reporté et vérifier l'affichage explicite du cas (pastille neutre / masquée / libellé "indéterminé") cohérent avec la durée affichée.

**Acceptance Scenarios**:

1. **Given** une inférence échouée avant exécution (backend non atteint), **When** l'utilisateur consulte le résultat, **Then** aucune pastille backend trompeuse n'est affichée (zone neutre ou libellé explicite).
2. **Given** une inférence exécutée mais backend non reporté par le runtime, **When** l'utilisateur consulte le résultat, **Then** un état explicite "indéterminé" est affiché plutôt qu'une pastille par défaut.

---

### Edge Cases

- Que se passe-t-il si le runtime reporte un backend inconnu (hors NPU/GPU/CPU) ? → affichage "indéterminé" ou libellé générique, pas de crash.
- Que se passe-t-il si la durée d'inférence est absente mais le backend présent ? → la pastille reste affichée seule, sans dépendre de la durée.
- Que se passe-t-il en mode hors-ligne / appareil minimal `MIN_COMPAT` ? → le backend réellement utilisé est affiché (souvent CPU), sans jugement de valeur.
- Que se passe-t-il si l'utilisateur change d'orientation ou de thème (clair/sombre) ? → la pastille reste lisible et distinguable dans les deux thèmes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST afficher, à côté de la durée d'inférence, une pastille indiquant le backend d'exécution réellement utilisé pour cette inférence, parmi NPU, GPU ou CPU.
- **FR-002**: Le système MUST récupérer le backend d'exécution depuis le résultat d'inférence produit par le runtime local, sans l'inférer côté présentation.
- **FR-003**: Le système MUST garantir la cohérence entre la durée affichée et le backend affiché (issus de la même exécution d'inférence).
- **FR-004**: Le système MUST proposer un visuel distinct (icône et/ou couleur) pour chacun des trois backends NPU, GPU et CPU.
- **FR-005**: Le système MUST afficher un état explicite non trompeur lorsque l'inférence a échoué avant exécution ou que le backend n'est pas déterminé.
- **FR-006**: Le système MUST conserver la lisibilité de la pastille sur les thèmes clair et sombre.
- **FR-007**: Le système MUST afficher exactement une pastille backend par résultat d'inférence présenté (jamais zéro en cas nominal réussi, jamais deux).
- **FR-008**: Le système MUST traiter un backend reporté inconnu (hors NPU/GPU/CPU) comme un cas "indéterminé" sans interruption d'affichage.

### Key Entities *(include if feature involves data)*

- **BackendExecution**: type représentant le matériel ayant réellement exécuté une inférence ; valeurs possibles NPU, GPU, CPU, INDETERMINATE. Attaché à un résultat d'inférence (cf. `ApiCallMetric` / `AnalyseTextuelleResult`).
- **BackendBadge**: représentation présentation de la pastille (libellé + visuel) dérivée de `BackendExecution`, affichée à côté de la durée d'inférence.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100 % des inférences réussies affichent une pastille backend cohérente avec le backend réellement utilisé, vérifiable par contrôle croisé runtime/UI.
- **SC-002**: Un utilisateur averti identifie visuellement le backend (sans lire le texte) dans 100 % des cas sur un échantillon des trois backends.
- **SC-003**: Aucune pastille backend trompeuse n'est affichée pour les inférences échouées ou au backend indéterminé (0 cas erroné sur l'échantillon de test).
- **SC-004**: La pastille reste lisible et distinguable dans les thèmes clair et sombre (contraste conforme aux attentes d'accessibilité de l'app).

## Assumptions

- Le runtime local Gemma / LiteRT-LM est capable de reporter le backend d'exécution réellement utilisé (NPU/GPU/CPU) pour une inférence donnée, ou peut être étendu pour le faire ; cette donnée est déjà collectée ou collectable côté `gemma4local`.
- La durée d'inférence est déjà affichée aujourd'hui à l'endroit où la pastille doit être ajoutée (zone métriques du résultat d'analyse).
- L'appareil cible peut exposer NPU, GPU ou CPU selon les capacités matérielles ; aucun backend n'est privilégié a priori.
- L'affichage se fait dans le module UI existant consommant les read-models de `local-llm-runtime` (présentation transversale, hors périmètre métier des autres domaines).
- La différenciation visuelle repose sur les ressources graphiques/icônes déjà disponibles ou à produire côté présentation (pas de dépendance à un service externe).

## Cross-domain Notes

- `user-guidance-experience` orchestre l'onboarding de téléchargement du modèle (inchangé).
- `ingredient-health-intelligence` consomme le gateway pour l'analyse de composition ; il reçoit désormais aussi le backend d'exécution dans le résultat/métrique.
- `capture-recognition` n'a pas de dépendance directe sur ce domaine.
- La présentation de la pastille est transversale UI mais la donnée backend reste propriété de `local-llm-runtime` (Open Host Service : read-model publié).

## Source Mapping

- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalClient.kt`
- `app/src/main/java/com/miamia/gemma4local/HybridGemma4LocalGateway.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalAvailabilityChecker.kt`
- `app/src/main/java/com/miamia/gemma4local/GemmaModelDownloader.kt`
- `app/src/main/java/com/miamia/gemma4local/GemmaModelImportManager.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalErrorMapper.kt`
- `app/src/main/java/com/miamia/gemma4local/Gemma4LocalConfig.kt`
- `app/src/main/java/com/miamia/gemma4local/DeviceClassResolver.kt`
- `app/src/main/java/com/miamia/gemma4local/model/ApiCallMetric.kt`
- `app/src/main/java/com/miamia/gemma4local/model/AnalyseTextuelleResult.kt`
