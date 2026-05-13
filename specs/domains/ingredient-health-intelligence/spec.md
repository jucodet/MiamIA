# Domain Spec — ingredient-health-intelligence

**Domain Context**: `ingredient-health-intelligence`
**Created**: 2026-05-06
**Last Modified**: 2026-05-12 (sync-apply consolidation)
**Status**: Draft

## Purpose

Analyser une liste d'ingrédients via le LLM local Gemma pour produire un bilan de composition et une critique santé par population. Ce domaine fournit aussi un test bouchonné isolé pour valider le flux d'appel LLM indépendamment de la capture et de l'OCR.

## Scope

- Test bouchonné du flux LLM local (entrée mockée → analyse → résultat/échec)
- Analyse de composition (bilan ingrédients via Gemma)
- Critique santé par population (enfants, femmes enceintes, adultes, personnes âgées)
- Gestion des erreurs et limites (timeout, modèle indisponible, réponse non analysable)
- Persistance et consultation du dernier résultat
- Copie et partage des résultats

## Invariants

- Chaque résultat d'analyse est associé à l'entrée qui l'a produit (traçabilité).
- Les catégories d'échec sont normalisées : `timeout`, `runtime-unavailable`, `non-analysable-response`.
- Le test bouchonné est exécutable manuellement, hors suites automatiques.

---

## Feature A — Test LLM Mock Ingredients

> Origine : `016-test-llm-mock`
> Input : "Créer un test bouchonné ne mettant à l'épreuve que le processus de demander au LLM local l'analyse d'une liste d'ingrédients mockée."

### Clarifications (Feature A)

#### Session 2026-05-06

- Q: Critère de "réponse exploitable" ? → A: Succès si non vide et classée analysable par le parseur de test.
- Q: Politique de timeout ? → A: 180 secondes (timeout strict). *(Backfill P7 — 2026-05-12 : 30 s → 180 s pour réalisme Gemma local.)*
- Q: Catégories d'échec ? → A: `timeout`, `runtime-unavailable`, `non-analysable-response`.
- Q: Règle d'égalité entrée mockée ? → A: Stricte caractère par caractère.
- Q: Politique de validation projet ? → A: Test manuel uniquement, hors validation régulière.

### User Scenarios (Feature A)

#### US-A1 — Valider le flux d'appel LLM local (P1)

En tant que développeuse, je veux lancer un test bouchonné avec une liste d'ingrédients mockée fixe afin de vérifier le flux d'envoi vers le LLM local et de réception de réponse, indépendamment de l'OCR et de la capture.

**Acceptance Scenarios**:

1. **Given** entrée mockée exactement égale à la référence, **When** analyse déclenchée, **Then** requête transmise au moteur LLM local et réponse exploitable renvoyée.
2. **Given** test isolé des autres modules, **When** test exécuté, **Then** aucune dépendance à la caméra, l'OCR ou une entrée interactive.

#### US-A2 — Garantir l'intégrité de l'entrée analysée (P2)

En tant que développeuse, je veux que la chaîne mockée soit transmise telle quelle pour confirmer que l'analyse correspond exactement au texte source.

**Acceptance Scenarios**:

1. **Given** chaîne mockée de référence, **When** demande d'analyse construite, **Then** charge utile conserve exactement le même contenu textuel.
2. **Given** analyse terminée, **When** résultat journalisé, **Then** trace associe explicitement le résultat à l'entrée mockée.

#### US-A3 — Rendre les échecs explicites (P3)

En tant que développeuse, je veux un comportement d'échec lisible pour distinguer un problème de runtime local d'un problème de logique du test.

**Acceptance Scenarios**:

1. **Given** runtime local indisponible, **When** demande lancée, **Then** état d'échec explicite et actionnable.

#### Edge Cases (Feature A)

- Chaîne mockée vide ou sans ingrédients exploitables.
- Runtime local répond avec un contenu non interprétable.
- Réponse dépasse le délai attendu.

### Functional Requirements (Feature A)

- **IHI-A-FR-001**: Le système MUST exécuter un test bouchonné dédié au flux d'analyse LLM local sans dépendre de la capture caméra ni de l'OCR.
- **IHI-A-FR-002**: Le système MUST utiliser comme entrée de test unique la chaîne mockée suivante :
  `Ingredients. Sucre, farine de BLÉ 33 %, farine complète de BLÉ 15 %, huile de palme, huile de colza, amidon de BLÉ, sirop de glucose, poudres à lever (carbonates d'ammonium, carbonates de sodium), émulsifiant (lécithines de SOJA), sel, LAIT écrémé en poudre, LAIT entier en poudre, arômes.`
- **IHI-A-FR-003**: Le système MUST transmettre l'entrée mockée sans altération de contenu au processus d'analyse.
- **IHI-A-FR-004**: Le système MUST retourner un résultat indiquant clairement soit une analyse reçue, soit un échec explicite.
- **IHI-A-FR-005**: Le système MUST associer chaque résultat du test à l'entrée mockée utilisée pour traçabilité.
- **IHI-A-FR-006**: Le système MUST permettre l'exécution répétable du même scénario avec les mêmes attentes.
- **IHI-A-FR-007**: Le système MUST considérer une réponse comme exploitable uniquement si non vide et classée analysable par le parseur de test.
- **IHI-A-FR-008**: Le système MUST échouer automatiquement le test si aucune réponse exploitable n'est obtenue dans une fenêtre de 180 secondes. *(Backfill P7 — 2026-05-12 : 30 s → 180 s pour réalisme Gemma local.)*
- **IHI-A-FR-009**: Le système MUST classifier chaque échec dans : `timeout`, `runtime-unavailable`, `non-analysable-response`.
- **IHI-A-FR-010**: Le système MUST vérifier une égalité stricte caractère par caractère entre `MockIngredientInput` et la charge transmise.
- **IHI-A-FR-011**: Le système MUST être exécutable manuellement et ne fait pas partie des contrôles bloquants réguliers.

### Key Entities (Feature A)

- **MockIngredientInput**: Chaîne d'ingrédients de référence du test.
- **LlmAnalysisRequest**: Demande d'analyse générée à partir de `MockIngredientInput`.
- **LlmAnalysisOutcome**: Résultat observable (succès avec contenu, ou échec avec raison).
- **TestTraceRecord**: Lien de traçabilité entre entrée mockée, demande envoyée et résultat.

### Success Criteria (Feature A)

- **SC-A-001**: 100 % des exécutions → chaîne mockée de référence utilisée.
- **SC-A-002**: ≥ 95 % des exécutions en environnement prêt → réponse exploitable en < 180 s.
- **SC-A-003**: 100 % des échecs → état d'erreur explicite avec catégorie identifiable.
- **SC-A-004**: Scénario reproductible sur ≥ 3 exécutions successives.
- **SC-A-005**: 100 % des succès → réponse non vide classée analysable.
- **SC-A-006**: 100 % des exécutions > 180 s sans réponse → marquées `timeout`.
- **SC-A-007**: 100 % des échecs → catégorie dans la liste définie.
- **SC-A-008**: 100 % des requêtes → texte source conservé (égalité caractère par caractère).

---

## Feature B — Composition & Health Critique (Placeholder)

> Origine : sync-apply P14, 2026-05-12
> Source packages : `healthcritique/` (13 fichiers, ~1030 lignes), `composition/` (9 fichiers, ~728 lignes)
> Status : à compléter via `/speckit-sync-backfill`

### Scope (Feature B)

#### Analyse de composition (`composition/`)
- Bilan ingrédients via Gemma local (`CompositionAnalysisEngine`)
- Parser bilan (`CompositionBilanParser`)
- Validation résultat (`CompositionResultValidator`)
- Messages d'erreur (`CompositionErrorMessages`)
- Modèles de données composition

#### Critique santé (`healthcritique/`)
- Moteur de critique santé par population (`HealthCritiqueEngine`)
- Prompt builder (`HealthCritiquePromptBuilder`)
- Section parser (`HealthCritiqueSectionParser`)
- Écrans UI (résultat critique, clipboard)
- Persistance snapshot dernier résultat
- Gestion des erreurs et limites

### Functional Requirements (Feature B)

- *(à extraire du code via `/speckit-sync-backfill`)*

---

## Cross-domain Notes

- Consomme le segment validé de `ingredient-normalization-validation`.
- Utilise le gateway de `local-llm-runtime` pour l'inférence.
- L'orchestration UX est gérée par `user-guidance-experience`.
- Les KPI additifs détaillés sont du ressort de `additive-risk-insights`.

## Source Mapping

- `specs/016-test-llm-mock/` (Feature A)

## Assumptions

- Le runtime LLM local est installé et utilisable dans l'environnement de développement.
- Le test bouchonné vise le flux d'appel et de réponse, pas la qualité nutritionnelle intrinsèque.
- La chaîne mockée est la source de vérité pour le scénario de test.
