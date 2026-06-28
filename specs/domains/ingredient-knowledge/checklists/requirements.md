# Specification Quality Checklist: ingredient-knowledge (Features IKB-A + IKB-B)

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-06-27 (IKB-A) | **Updated**: 2026-06-28 (IKB-B)
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Feature IKB-B Coverage (2026-06-28)

- [x] IKB-B user stories (B1 refresh, B2 offline fallback, B3 exhaustive coverage + Ciqual) defined with Given/When/Then
- [x] IKB-B FRs (001–011) testable and unambiguous
- [x] IKB-B SCs (001–007) measurable and technology-agnostic
- [x] IKB-B edge cases (réseau partiel, cache corrompu, entrée amont incohérente, refresh lent) identified
- [x] Offline-first preserved (cache persisté → baseline embarquée) — IKB-A invariants inchangés
- [x] Feature C compliance preserved for enriched attributes (contenu général, aucune extension d'équivalence)
- [x] Traçabilité source/version étendue à Ciqual (IKB-B-FR-008)

## Notes

- Domaine `ingredient-knowledge` créé et ajouté à `domain-map.md` (supporting domain, upstream de `ingredient-health-intelligence` via *Published Language*).
- Langage ubiquitaire enrichi : `ReferenceContext`, `AdditiveFactCard`, `AllergenFactCard` ; IKB-B ajoute `KbCache`, `KbRefreshOutcome`, `KbBaseline`, `AdditiveFactCard` étendu (attributs Ciqual).
- Ancrage Feature C préservé : le contexte publié est qualifié « contenu général » (`IHI-C-FR-004`), aucune extension de `EquivalencePolicy` v1 stricte — y compris pour les attributs Ciqual (IKB-B).
- **Interprétation Ciqual (IKB-B)** : Ciqual apporte des attributs de composition/nutritionnels (ex. énergie), non une taxonomie d'additifs ; taxonomie additive issue d'OpenFoodFacts en couverture exhaustive. Documenté en Assumptions — lancer `/speckit-clarify` si l'intention différait.
- Hors périmètre (reste reporté) : lookup code-barres OpenFoodFacts produit.
- Prêt pour `/speckit-clarify` (recommandé vu l'interprétation Ciqual) ou `/speckit-plan`.
