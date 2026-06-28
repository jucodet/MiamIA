# Contrat — `ReferenceContext` read-model (Published Language)

**Producteur** : domaine `ingredient-knowledge` (Feature IKB-A)
**Consommateur** : domaine `ingredient-health-intelligence` (flux LLM composition + critique)
**Pattern DDD** : *Published Language* / *Open Host Service*

## Portée

`ReferenceContext` est le seul modèle publié par `ingredient-knowledge` vers l'extérieur. Les consommateurs ne dépendent **pas** du modèle interne (`AdditiveFactCard`, index, assets) — uniquement de ce read-model.

## Structure publiée

```text
ReferenceContext
├── qualification: GENERAL            # constante — contenu général (IHI-C-FR-004)
├── baseVersion: String               # version de la base référence
└── cards: List<ReferenceContextEntry>
    ├── kind: ADDITIVE | ALLERGEN
    ├── key: String                   # E-number (additif) ou id (allergène)
    ├── display: String               # libellé canonique
    ├── riskLevel: FAIBLE | MODERE | ELEVE   # additifs uniquement
    └── role: String?                 # additifs uniquement
```

## Garde-fous contractuels

| Règle | Référence |
|-------|-----------|
| `qualification` MUST être `GENERAL` ; aucune entrée ne présente un fait étiquette | `IKB-A-FR-004` / `005`, `IHI-C-FR-004` |
| `cards` MUST être borné (plafond N, défaut 12) et priorisé allergènes→risque élevé | `IKB-A-FR-011` |
| Aucun symbole/alias hors normalisations mécaniques n'est publié comme équivalence | `IKB-A-FR-006`, `IHI-C-FR-005` |
| Le consommateur reste responsable de l'ancrage « fait produit » sur le `ValidatedIngredientSegment` | `IHI-C-FR-001`/`005` |
| `ReferenceContext` vide (lookup sans correspondance) : aucune injection, flux LLM nominal | `IKB-A-FR-007` |

## Mode d'injection (côté consommateur)

- Le consommateur (`HealthCritiquePromptBuilder` / équivalent composition) insère le `ReferenceContext` dans un bloc **balisé comme contexte général**, distinct des instructions de fait étiquette.
- Toute formulation liant « ce produit » à un additif/allergène reste ancrée sur une sous-chaîne littérale du segment validé.

## Non-objectifs

- Ce contrat **ne publie pas** de KPI additifs (rôle de `additive-risk-insights`).
- Ce contrat **ne définit pas** de règle d'équivalence de synonyme (rôle de `EquivalencePolicy` du core).
