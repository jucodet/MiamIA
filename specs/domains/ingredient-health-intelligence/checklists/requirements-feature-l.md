# Specification Quality Checklist: personnalisation-prompt-critique (Feature L)

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-06-28
**Feature**: [spec.md](../spec.md) (section Feature L)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs) — Feature L décrit le **contenu** du prompt de critique (persona, dimensions de risque, format de sortie), pas l'implémentation Kotlin/`HealthCritiquePromptBuilder`.
- [x] Focused on user value and business needs — cadrage expert, dimensions de risque, populations vulnérables, garde-fous éthiques.
- [x] Written for non-technical stakeholders — scénarios orientés utilisatrice.
- [x] All mandatory sections completed — Clarifications, User Scenarios, Edge Cases, Functional Requirements, Key Entities, Success Criteria présents.

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous — IHI-L-FR-001 à 015 avec ancrage vers Feature C et critères associés.
- [x] Success criteria are measurable — IHI-L-SC-001 à 007 avec 100 % et ≥ 3 exécutions.
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined — US-L1 (persona + dimensions), US-L2 (populations vulnérables + garde-fous), US-L3 (format de sortie strict).
- [x] Edge cases are identified — liste longue, langue/illisible, terme ambigu, demande médicale, invention OCR.
- [x] Scope is clearly bounded — personnalisation du prompt de critique uniquement ; bilan composition et KPI additifs hors périmètre ; conformité Feature C préservée.
- [x] Dependencies and assumptions identified — ancrage Feature C inchangé ; format de sortie et parseur de sections préservés.

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria — chaînage US-L / IHI-L-SC.
- [x] User scenarios cover primary flows — P1 persona/dimensions, P1 populations vulnérables/garde-fous, P1 format de sortie.
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification — mention du « prompt construit » reste au niveau métier (contrat de contenu), pas de classe/méthode imposée.

## Notes

- Validation 2026-06-28 : tous les critères passent ; aucun marqueur [NEEDS CLARIFICATION].
- Conformité Feature C explicitement réaffirmée (IHI-L-FR-014, IHI-L-SC-006) : la personnalisation du prompt ne doit pas encourager l'invention d'ingrédients ni rompre l'ancrage.
- Non-régression du contrat de parsing vérifiée via IHI-L-SC-005 (parseur de sections existant reconnaît toujours les 4 marqueurs).
- Clarifications 2026-06-28 (5 questions résolues) :
  - Q1 → Remplacement en dur versionné dans le builder (IHI-L-FR-016).
  - Q2 → Populations vulnérables sans section dédiée traitées en vigilance transversale intégrée (IHI-L-FR-008).
  - Q3 → Personnalisation limitée au prompt de critique santé, bilan de composition non modifié (IHI-L-FR-017).
  - Q4 → Seuil « liste très longue » en nombre d'ingrédients, valeur exacte au plan (IHI-L-FR-012).
  - Q5 → Validation sémantique au MVP par relecture humaine + traçabilité, format par parseur existant (IHI-L-SC-008).
- Spécification prête pour `/speckit-plan` sur le périmètre Feature L.
