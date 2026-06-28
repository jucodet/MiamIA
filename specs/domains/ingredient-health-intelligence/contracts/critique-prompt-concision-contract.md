# Contrat — concision maximale du prompt de critique santé (Feature Q)

**Domain**: `ingredient-health-intelligence`
**Created**: 2026-06-28
**Spec ref**: Feature Q — `IHI-Q-FR-001` à `IHI-Q-FR-010` / `IHI-Q-SC-001` à `IHI-Q-SC-008`
**Étend**: `critique-prompt-contract.md` (Feature L) et `critique-profil-contract.md` (Feature N) — Feature Q **ajoute** la directive de concision, ne déroge pas.

## Purpose

Contrat de la **contrainte de concision maximale** intégrée au `systemInstruction` du prompt de critique santé (`HealthCritiquePromptBuilder.buildSystemInstruction(profile)`). Le LLM doit produire un **contenu court et dense**, borné par le format strict Feature N, en préservant l'ancrage Feature C et les garde-fous Feature L/N.

## Mécanisme

- Directive **intégrée au texte de l'instruction système** (bloc « CONCISION MAXIMALE »), entre les contraintes médicales/éthiques existantes et le `FORMAT DE SORTIE STRICT` Feature N.
- **Répétable** (même profil → même prompt — `IHI-Q-FR-008`) ; **contenu intégré au code** (pas de config externe — `IHI-L-FR-016` / `IHI-N-FR-015`).
- Périmètre **critique seule** (bilan composition non modifié — `IHI-Q-FR-009`).

## Contenu obligatoire de la `ConcisionDirective`

| Élément | Exigence | Réf spec |
|---------|----------|----------|
| Formulations courtes/denses | « formulations courtes et denses » ; aller à l'essentiel | `IHI-Q-FR-001` |
| Pas de préambule | aucun texte avant le rappel « Évalué pour vous : <profil> » (réaffirme `IHI-N-FR-006`) | `IHI-Q-FR-001` |
| Pas de prose narrative | aucun paragraphe narratif autour des blocs | `IHI-Q-FR-001` |
| Pas de répétitions | pas de reformulations entre blocs | `IHI-Q-FR-001` |
| Niveau de prudence concis | un palier (Faible/Modéré/Élevé) + **une phrase courte** (≤ ~25 mots indicatif) | `IHI-Q-FR-003` |
| Cartes concises | sous-lignes (Impact / Fait établi / Nuance / Cible particulièrement) ≤ ~15 mots indicatif ; références CIRC/OMS **compactes** (ex. « CIRC 2A ») quand applicables | `IHI-Q-FR-004` |
| Bornage format strict | « sans supprimer ni fusionner les blocs exigés » : rappel + marqueur unique + 3 blocs obligatoires et parsables | `IHI-Q-FR-002` |
| Ancrage préservé | « ne jamais inventer ni résumer au point de produire un fait non ancré » ; chaque ingrédient ancré dans le `ValidatedIngredientSegment` | `IHI-Q-FR-005` |
| Garde-fous préservés | disclaimer + pas de diagnostic/prescription (réaffirme `IHI-L-FR-007`/`011`) | `IHI-Q-FR-006` |

## Bornage — format strict Feature N (préservé)

La concision agit sur la **longueur des formulations**, pas sur la structure. La sortie DOIT rester parsable par `HealthCritiqueSectionParser` :

1. Rappel `EvaluatedForHeader` « Évalué pour vous : <profil> » (`IHI-N-FR-003`).
2. Marqueur unique du profil sélectionné (`IHI-N-FR-006`).
3. Bloc 1 — Niveau de prudence (palier + phrase courte) (`IHI-N-FR-007`).
4. Bloc 2 — Cartes d'ingrédients à vigilance Modérée/Élevée (`IHI-N-FR-008`/`010`).
5. Bloc 3 — Liste complète des ingrédients analysés (`IHI-N-FR-011`).

Une sortie trop courte/tronquée ne respectant pas ce format est rejetée comme `non-analysable-response` (`IHI-N-FR-013`).

## Tests de conformité minimaux

- **Prompt** : tests JVM sur `buildSystemInstruction(profile)` — présence des marqueurs de la `ConcisionDirective` (« concision » / « formulations courtes » / « pas de préambule » / « pas de prose narrative » / « pas de répétitions »), et préservation des marqueurs Feature L/N (persona, dimensions, hiérarchie, populations, disclaimer, marqueur unique, 3 blocs).
- **Répétabilité** : 2 exécutions → prompt identique (`IHI-Q-SC-006`).
- **Sortie** : non-régression `HealthCritiqueSectionParser` — marqueur unique + 3 blocs reconnus sur un jeu fixe (`IHI-N-SC-009`).
- **Sémantique** (MVP) : relecture humaine + traçabilité sur jeu fixe — concision des formulations, absence de préambule/prose, ancrage Feature C, garde-fous préservés (`IHI-Q-SC-008`, aligné `IHI-C-FR-006`).
