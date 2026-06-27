# Data Model — ingredient-knowledge (Feature IKB-A)

**Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Entities

### `RiskLevel` (enum)

Niveau de risque indicatif d'un additif, dérivé des étiquettes de risque OpenFoodFacts.

- `FAIBLE`
- `MODERE`
- `ELEVE`

### `KbSource`

Trace la provenance et la version d'une fiche / de la base.

- `origin`: `OFF_ADDITIVES_TAXONOMY` | `EU_ALLERGEN_LIST`
- `baseVersion`: `String` (version de la base, lue depuis `kb-version.json`)
- `sourceRef`: `String?` (référence optionnelle de la source)

### `AdditiveFactCard`

Fiche référence additif. **Clé primaire : `eNumber`** (`IKB-A-FR-012`).

- `eNumber`: `String` — identifiant réglementaire unique (ex. `E300`)
- `canonicalName`: `String` — dénomination canonique
- `aliases`: `List<String>` — dénominations courantes (alias de recherche, **pas** un synonyme métier)
- `role`: `String?` — rôle technologique (ex. antioxydant)
- `riskLevel`: `RiskLevel` — niveau de risque indicatif (`IKB-A-FR-013`)
- `source`: `KbSource`

**Invariants** :
- `eNumber` unique dans la base.
- Une recherche par alias normalisé renvoie la **même** fiche canonique (déduplication par `eNumber`).
- Aucun alias ne crée de règle d'équivalence hors `EquivalencePolicy` v1 stricte.

### `AllergenFactCard`

Fiche référence allergène réglementaire UE.

- `id`: `String` — identifiant réglementaire (ex. `GLUTEN`, `LAIT`)
- `regulatoryName`: `String` — dénomination réglementaire UE
- `aliases`: `List<String>` — dénominations courantes (alias de recherche)
- `source`: `KbSource`

**Invariants** : `id` unique ; 14 entrées au maximum (allergènes réglementaires UE).

### `IngredientDesignation`

Désignation d'ingrédient issue du segment, candidate au lookup.

- `rawText`: `String` — texte tel que présent dans le `ValidatedIngredientSegment`
- `normalized`: `String` — forme normalisée (casse, espaces, accents) via `MechanicalNormalizer`

**Règle** : la normalisation est **mécanique et listée** (casse → minuscules, espaces → collapse, accents → forme ASCII) ; aucune autre transformation (`IKB-A-FR-002`).

### `LookupOutcome`

Résultat du lookup depuis une liste d'ingrédients.

- `matchedAdditives`: `List<AdditiveFactCard>`
- `matchedAllergens`: `List<AllergenFactCard>`
- `unmatchedDesignations`: `List<IngredientDesignation>` — désignations non référencées (repli silencieux)
- `baseVersion`: `String` — version de la base utilisée

**Invariants** : aucune fiche inventée ; `unmatchedDesignations` ne déclenche aucun blocage (`IKB-A-FR-003`/`007`).

### `ReferenceContext`

Contexte de référence publié pour injection LLM. **Ensemble borné** (`IKB-A-FR-011`).

- `cards`: `List<ReferenceContextEntry>` — sélection **bornée** par plafond N (défaut 12)
- `qualification`: `GENERAL` (constante — contenu général, `IHI-C-FR-004`)
- `baseVersion`: `String`

**Priorisation de sélection** : allergènes d'abord, puis additifs `ELEVE`, puis `MODERE`, puis `FAIBLE`. Fiches au-delà du plafond : omises (repli silencieux).

**Invariants** :
- Aucune entrée ne présente un fait comme issu de l'étiquette (`IKB-A-FR-005`).
- `qualification` toujours `GENERAL`.

### `ReferenceContextEntry`

Projection d'une fiche en entrée de contexte (forme sérialisable pour le prompt).

- `kind`: `ADDITIVE` | `ALLERGEN`
- `key`: `String` — E-number ou id allergène
- `display`: `String` — libellé canonique
- `riskLevel`: `RiskLevel?` — pour additifs
- `role`: `String?` — pour additifs

## Relationships

```text
IngredientDesignation ──(lookup)──▶ LookupOutcome
                                    ├── AdditiveFactCard (key: eNumber)
                                    └── AllergenFactCard  (key: id)

LookupOutcome ──(build, cap N, prioritize)──▶ ReferenceContext
                                                └── ReferenceContextEntry
```

## Validation Rules (mapped to FRs)

| Règle | FR |
|-------|----|
| E-number unique, alias rattachés à la même fiche | `IKB-A-FR-012` |
| Matching sous-chaîne + normalisations mécaniques listées | `IKB-A-FR-002`, `IKB-A-FR-006` |
| Risque sur échelle 3 niveaux dérivée OFF | `IKB-A-FR-013` |
| Aucune fiche pour substance non référencée | `IKB-A-FR-003` |
| Repli silencieux (pas de blocage) | `IKB-A-FR-007` |
| Plafond N + priorisation allergènes→risque élevé | `IKB-A-FR-011` |
| Qualification GENERAL, aucun fait étiquette | `IKB-A-FR-004`/`005` |
| Traçabilité source/version par fiche | `IKB-A-FR-009` |
| Base absente/illisible → erreur domaine explicite | `IKB-A-FR-010` |

## State Transitions

N/A — base référence statique versionnée ; pas de cycle de vie mutable au P1.
