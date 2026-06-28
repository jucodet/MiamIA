# Specification Quality Checklist: Feature N — Critique santé ciblée par profil utilisateur

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-06-28
**Feature**: [spec.md](../spec.md) — section « Feature N — Critique santé ciblée par profil utilisateur »

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

## Notes

- **Clarify session 2026-06-28** : 5 questions posées et résolues (Q1 fallback profil par défaut « Adulte » ; Q2 liste compacte pour « Voir tous les ingrédients analysés » ; Q3 « alertes » = KPI `additive-risk-insights` existants ; Q4 suppression totale du flux 4-profils ; Q5 profil modifiable dans « Paramètres / Profil » UGE). Toutes intégrées dans `### Clarifications (Feature N)` + FR/SC/Edge Cases/Assumptions.
- Feature N **supersède et retire** le format 4-marqueurs strict de Feature L (**IHI-L-FR-009** / **IHI-L-SC-004**) ; traçabilité conservée en spec (clarifications Feature N + assumptions).
- Les exigences Feature L **non format-strict** (persona expert, 5 dimensions de risque, hiérarchie faits/incertitudes/hypothèses, populations vulnérables transversales, garde-fous éthiques, seuil « liste très longue », rédaction française, disclaimer) restent applicables et sont référencées par **IHI-N-FR-004**.
- La saisie/persistance du profil utilisateur est déléguée au domaine `user-guidance-experience` (Onboarding + écran « Paramètres / Profil ») (cross-domain note + assumption) ; IHI consomme uniquement le profil.
- Items incomplets nécessitent une mise à jour de la spec avant `/speckit-plan`.
