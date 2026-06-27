# Contrat — Interface `ReferenceKb` (frontière de domaine)

**Domaine** : `ingredient-knowledge`
**Rôle** : frontière de domaine + couche d'anti-corruption. `ReferenceKb` expose le lookup offline ; l'implémentation Android assets (`EmbeddedReferenceKb`) et l'implémentation de test (`InMemoryReferenceKb`) sont interchangeables.

## Interface

```kotlin
interface ReferenceKb {
    fun lookup(designations: List<IngredientDesignation>): LookupOutcome
    fun baseVersion(): String
}
```

## Comportement attendu

- `lookup` : pour chaque désignation, recherche par **sous-chaîne littérale** après normalisation mécanique (casse, espaces, accents) ; renvoie les fiches canoniques correspondantes (additifs par E-number/alias, allergènes par id/alias).
- Substance non référencée → présente dans `unmatchedDesignations`, **aucune** fiche inventée, **aucun** blocage (`IKB-A-FR-003`/`007`).
- `baseVersion` : version de la base (depuis `kb-version.json`).

## Gestion d'erreur

- Base absente ou illisible → erreur domaine explicite (pas de contexte inventé, `IKB-A-FR-010`).
- Aucune dépendance réseau au P1 (`IKB-A-FR-001`).

## Implémentations

| Impl | Cible | Usage |
|------|-------|-------|
| `EmbeddedReferenceKb` | Android (assets + kotlinx.serialization) | Production |
| `InMemoryReferenceKb` | JVM pur (fixtures) | Tests unitaires / jeu fixe (US-IKB-A3) |

## Tests de conformité minimaux

- Jeu **positif** : désignations référencées → fiches canoniques attendues (additif par E-number et par alias → même fiche).
- Jeu **négatif** : désignation non référencée → `unmatchedDesignations`, aucune fiche, pas de blocage.
- Répétabilité : ≥ 3 exécutions → résultats identiques (`IKB-A-SC-004`).
