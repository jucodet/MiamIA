# Research — ancrage anti-hallucination (Feature C)

**Date**: 2026-05-13 | **Domain**: `ingredient-health-intelligence`

Les « inconnues » techniques ont été résolues par la spec + session **clarify** ; ce document consolide les **décisions d’implémentation** pour la Phase 1.

## 1. Politique d’équivalence v1 (stricte)

- **Decision** : v1 = sous-chaînes **littérales** du `ValidatedIngredientSegment` ; politique d’équivalence **vide** sauf **normalisations mécaniques** explicitement listées (casse, espaces, Unicode normal form si documenté).
- **Rationale** : minimize false « synonym » matches that look like grounded claims.
- **Alternatives considered** : catalogue minimal de synonymes (rejeté pour v1 par clarify Option A).

## 2. Contenu général vs « ce produit »

- **Decision** : blocs généraux autorisés s’ils sont **identifiables** ; toute mention explicite **ce produit** → uniquement termes **littéralement** dans le segment (**IHI-C-FR-004** b + **IHI-C-FR-005**).
- **Rationale** : garde la valeur éducative sans confondre avec l’étiquette.
- **Alternatives considered** : interdire tout contenu général dans un succès (trop restrictif — clarify Option B retenue).

## 3. Ancrage partiel

- **Decision** : **tout ou rien** — aucun succès avec analyse produit tronquée ; échec contrôlé (**IHI-C-FR-003**).
- **Rationale** : audit simple, pas de UX « demi-vérité ».
- **Alternatives considered** : livraison partielle avec label UI (rejetée — clarify Option A).

## 4. Juxtaposition `additive-risk-insights`

- **Decision** : enrichissements autorisés **si** (a) additif **littéralement** dans le segment, (b) **attribution explicite** au domaine additifs, (c) pas de confusion dominante avec le texte étiquette (**IHI-C-FR-007**).
- **Rationale** : respecte la **Published Language** du domain-map sans faire du LLM une source additive.
- **Alternatives considered** : tout interdire hors segment textuel (rejeté — clarify Option B).

## 5. Vérification indépendante (MVP)

- **Decision** : **relecture humaine** + traçabilité suffisent ; pas de gate automatisée obligatoire sur chaque succès au MVP.
- **Rationale** : aligné clarify Option A et charge projet ; checks automatisés possibles en itération suivante.
- **Alternatives considered** : gate CI bloquant sur chaque réponse (reporté post-MVP).

## 6. État de l’implémentation actuelle (baseline)

- **Decision** : conserver `CompositionResultValidator.validateAgainstSource` comme point central mais le **resserrer** pour se rapprocher du ratio / règles spec (aujourd’hui seuil **50 %** de lignes checkables absentes — à faire évoluer vers politique **stricte** clarify).
- **Rationale** : moindre risque de régression que remplacer entièrement par un nouveau pipeline opaque.
- **Alternatives considered** : validation uniquement LLM-side par prompt (insuffisant pour garanties testables).

## 7. Dette spec Feature B (implémenté 2026-05-13)

- **Note** : le code `composition/` et `healthcritique/` a été aligné sur Feature C sans backfill exhaustif de **Feature B** dans `spec.md` ; poursuivre `/speckit-sync-backfill` pour refermer l’écart doc ↔ code.
