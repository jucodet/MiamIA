---
name: "speckit-domain-ddd"
description: "Define a DDD-aligned specification topology and migration map from existing Spec Kit features to business domains. Use when the user wants to reorganize specs by functional domains, bounded contexts, and team/deployment boundaries."
disable-model-invocation: true
compatibility: "Requires spec-kit project structure with specs/ directory"
metadata:
  author: "foodgpt"
  source: "custom"
---

# Speckit Domain DDD

## Purpose

Create a target DDD model that transforms a feature-indexed spec tree into a domain-indexed structure readable by humans and scalable for parallel teams.

Align all decisions with Eric Evans DDD strategic/tactical best practices (Evans 2003 + DDD Reference): domain language first, explicit context boundaries, and protected model integrity.

## Expected Input

Use user constraints if provided:
- Domain vocabulary and ubiquitous language
- Expected team topology
- Desired deployment units
- Existing `specs/*` artifacts to include

If missing, infer from current specs and state assumptions explicitly.

## Workflow

1. Inventory current artifacts under `specs/`:
   - `spec.md`, `plan.md`, `data-model.md`, `contracts/`, `tasks.md`, `research.md`, `quickstart.md`
2. Build a domain map:
   - Identify candidate subdomains (core, supporting)
   - Define bounded contexts
   - Define upstream/downstream context relations
   - For each context relation, assign an explicit context-map pattern:
     - `Partnership`, `Shared Kernel`, `Customer/Supplier`, `Conformist`, `Anti-Corruption Layer`, `Open Host Service`, `Published Language`, or `Separate Ways`
   - For each proposed domain, provide a mandatory justification:
     - Why this boundary is the most relevant for business autonomy
     - Why alternatives were rejected
     - Which business rules and invariants belong inside
     - Which logic must stay outside
   - Run a model integrity pass:
     - Detect ambiguous terms that violate Ubiquitous Language
     - Detect overlapping concepts modeled in multiple contexts
     - Propose anti-corruption translation where upstream models would pollute downstream language
3. Distill the model before structuring files:
   - Identify the Core Domain where competitive/business differentiation is strongest
   - Separate Supporting Domains from Generic/Cross-cutting technical concerns
   - Ensure domain naming uses business language, not framework/component names
3. Propose target information architecture by domain:
   - One domain folder per bounded context
   - A canonical `spec.md` per domain (functional SSOT)
   - A canonical `plan.md` per domain (implementation SSOT)
   - A canonical `data-model.md` per domain (model SSOT)
   - Optional `contracts/` and `adr/` per domain
4. Define merge and traceability rules:
   - Preserve requirement IDs and source feature IDs
   - Keep a migration index linking old files to new sections
   - Mark conflicts and unresolved overlaps
5. Produce a migration plan:
   - Phase-by-phase move/merge sequence
   - Rollback-safe checkpoints
   - Validation checklist for completeness and non-regression
6. Present a full pre-generation tree for approval:
   - Show the complete directory and file tree that will be generated/updated/moved
   - Include domain folders and all planned files per domain
   - Mark each node with action: `create`, `update`, `move`, or `unchanged`
   - Stop and request explicit user validation before generation

## Evans-Inspired Modeling Rules (Mandatory)

1. Ubiquitous Language First
   - Normalize synonyms and contradictory terms before final boundaries.
   - Reject technical names (`utils`, `service-layer`, `orchestrator`) as domain names unless explicitly business-defined.

2. Bounded Context Autonomy
   - Each context must define its own model semantics and consistency rules.
   - Shared data does not imply shared model.

3. Context Map Explicitness
   - Every inter-context dependency must have one named relationship pattern and rationale.
   - If no healthy relationship fits, prefer `Separate Ways`.

4. Protect Model Integrity
   - When consuming external/upstream models with semantic drift, require `Anti-Corruption Layer`.
   - Forbid direct concept leakage across contexts without translation.

5. Distillation Discipline
   - Core domain decisions get priority in decomposition and ownership.
   - Supporting/generic concerns must not drive core boundaries.

6. Tactical Sanity Check
   - For each domain, identify expected Entities, Value Objects, Aggregates (or explain why not needed yet).
   - Verify that invariants are owned by one aggregate boundary/context only.

## Target Directory Convention

Use this default unless user overrides:

```text
specs/domains/
├── <domain-name>/
│   ├── spec.md
│   ├── plan.md
│   ├── data-model.md
│   ├── contracts/
│   ├── tasks.md
│   ├── migration-index.md
│   └── traceability.csv
└── domain-map.md
```

Rules:
- Use stable kebab-case domain names.
- Keep one domain per folder.
- Keep cross-domain policies in `specs/domains/domain-map.md`.

## Output Contract

Return:
1. Proposed domain taxonomy
2. Bounded context map
3. Context-map relationship table (context A -> context B -> pattern -> rationale)
4. Core/supporting/generic distillation rationale
5. Full pre-generation tree preview (mandatory, with action labels)
6. Artifact merge matrix (`source feature -> target domain/file/section`)
7. Domain justification dossier (mandatory, one section per domain)
8. Risks and open questions (max 10)

Generation is blocked until the user approves the full tree preview.

## Domain Justification Dossier (Mandatory)

For each domain, include this exact structure:

1. `Why this domain exists`
   - Business capability and outcome served
   - Ubiquitous language terms
2. `Why this boundary is best`
   - Coupling/cohesion rationale
   - Team ownership and deployment autonomy rationale
   - Rejected boundary options with short reasons
3. `Logic inside the domain`
   - Core business rules
   - Invariants and consistency rules
   - Main use cases and decisions
4. `Logic outside the domain`
   - Shared technical concerns (infra, observability, auth plumbing, etc.)
   - Upstream/downstream integrations
   - Cross-domain orchestration points
5. `Evidence from source specs`
   - Explicit mapping to source requirements and artifacts

If one of these five blocks is missing for any domain, the result is invalid.

## Generic Domain Policy (Strict)

`Generic domain` is forbidden by default.

- Do not propose a generic domain as a catch-all bucket.
- Do not create `utilities` domains as functional domains.
- Do not use labels such as `generic`, `common`, `misc`, `shared`, `utils`, `utilities` as business domains.
- Shared technical capabilities must be classified as:
  - cross-cutting technical platform concerns, or
  - explicit supporting domains with clear business service boundaries.
- If a choice must be made between:
  - introducing a non-business shared domain, or
  - duplicating technical implementation across business domains,
  prefer controlled duplication to preserve clear business boundaries.

Exception path (rare):
- A generic domain can only be proposed if the user explicitly requests it.
- In that case, include a mandatory warning explaining why this is usually a DDD anti-pattern and list at least two safer alternatives.

## Quality Gates

Do not finalize until all pass:
- Every feature spec is mapped to at least one domain
- Every domain has explicit owner boundary (team/deployment)
- Every domain includes a complete Domain Justification Dossier
- Every inter-context dependency has an explicit Evans context-map pattern
- Ubiquitous language conflicts are resolved or explicitly tracked as open issues
- Core domain is identified and justified
- Overlaps are documented with conflict strategy
- Migration keeps backward traceability
- No generic/catch-all domain exists unless explicitly requested by the user
- No `utilities` or pseudo-technical domain is used as a business domain
- Full pre-generation tree preview has been shown and approved
- Result is understandable by a new developer in less than 30 minutes

## Guardrails

- Prioritize business semantics over technical layers.
- Never delete source specs without a migration index entry.
- Avoid duplicating requirements across domains; reference instead.
- If ambiguity is high, ask targeted questions before locking boundaries.
- Refuse catch-all domain proposals that hide unclear modeling.
- Every created domain must map to a real business capability and explicit business language.
- Accept implementation duplication when needed to keep domain boundaries pure.
- When model integrity conflicts with implementation convenience, prefer model integrity.
