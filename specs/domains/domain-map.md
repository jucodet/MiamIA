# Domain Map MiamIA

## Core Domain

- `ingredient-health-intelligence`: transforme un segment d'ingredients valide en bilan composition + critique sante exploitable.

## Supporting Domains

- `capture-recognition`: capture image, OCR, gestion des sessions de scan.
- `ingredient-normalization-validation`: isolation de segment (vue auxiliaire), ancre ingredients, validation utilisateur ; entree des analyses par modele de langage = texte OCR integral (spec domaine, mise a jour 2026-05-13).
- `additive-risk-insights`: projection des faits d'analyse en KPI additifs/risques.
- `user-guidance-experience`: orchestration UX transversale (accueil, readiness, transitions) ; accueil capture sans chip balise ni ligne statut « pret a scanner », OCR vers analyse sans ecran intermediaire de relecture (**Ref.** suppression segmentation cote `ingredient-normalization-validation`).

## Platform Technical Contexts

- `local-llm-runtime`: execution locale Gemma, disponibilite backend, erreurs runtime.
- `traceability-storage`: persistance des snapshots d'analyse et traces de session.

## Context Relationships (Evans)

| Upstream | Downstream | Pattern | Rationale |
|---|---|---|---|
| `capture-recognition` | `ingredient-normalization-validation` | `Anti-Corruption Layer` | Eviter la fuite du modele OCR brut vers le langage metier. |
| `ingredient-normalization-validation` | `ingredient-health-intelligence` | `Customer/Supplier` | Le core consomme un segment valide stable fourni par l'amont. |
| `ingredient-health-intelligence` | `additive-risk-insights` | `Published Language` | Exposer des faits structures plutot qu'un texte libre non contractuel. |
| `local-llm-runtime` | `ingredient-health-intelligence` | `Conformist` | Le core s'aligne sur l'API runtime locale existante. |
| `traceability-storage` | `ingredient-health-intelligence` | `Shared Kernel (restricted)` | Partage strict de snapshots techniques versionnes. |
| `user-guidance-experience` | `all domain contexts` | `Open Host Service` | La UI consomme des read-models/commandes publies. |

## Ubiquitous Language Baseline

- `RawOcrText`: texte OCR brut issu de capture.
- `IngredientSegment`: segment extrait autour de la zone ingredients.
- `ValidatedIngredientSegment`: segment confirme et immuable pour une analyse.
- `HealthAnalysisReport`: sortie metier consolidee (composition + critique).
- `AdditiveRiskFacts`: faits additifs/risques publies pour KPI.

## Ownership Propose

- Equipe domaine: `ingredient-health-intelligence`, `ingredient-normalization-validation`, `additive-risk-insights`.
- Equipe mobile platform: `capture-recognition`, `local-llm-runtime`, `traceability-storage`, `user-guidance-experience`.
# Domain Map (DDD cible)

Date: 2026-05-05  
Source: `specs/001-*` a `specs/015-*`

## 1) Taxonomie proposee

### Core domains

- `ingredient-intelligence`: extraction, isolation, validation et interpretation ingredient.
- `health-insights`: critique sante, synthese et recommandations actionnables.

### Supporting domains

- `capture-experience`: camera, capture photo, parcours home, feedback de progression.
- `ai-runtime-platform`: execution locale Gemma/LiteRT-LM, disponibilite runtime, orchestration des appels LLM.

### Generic domains

- `user-output-utilities`: copie/partage, persistance du dernier resultat, presentations standard de contenu.

## 2) Bounded contexts et frontieres (team/deploiement)

| Bounded Context | Domaine | Responsabilite | Equipe owner | Unite de deploiement |
|---|---|---|---|---|
| `CameraCaptureContext` | capture-experience | Preview live, permissions, capture explicite, ecran accueil et etats scan | Mobile UX Capture Team | Module Android `camera` + `home` |
| `OcrSegmentationContext` | ingredient-intelligence | OCR, pre-traitement segment, isolement liste ingredients, validation de segment | Mobile Intelligence Team | Module Android `ocr`/`ingredients` |
| `CompositionAnalysisContext` | ingredient-intelligence | Bilan ingredients/composition depuis segment valide, score/additifs | Mobile Intelligence Team | Module Android `composition` |
| `HealthCritiqueContext` | health-insights | Critique par population, garde-fous prudents, formatting sections | Health Insights Team | Module Android `healthcritique` |
| `GuidanceSynthesisContext` | health-insights | Synthese et recommandations pre-KPI, alternatives plus saines | Health Insights Team | Module Android `summary` |
| `LocalLlmRuntimeContext` | ai-runtime-platform | Disponibilite Gemma locale, execution inference, fallback erreurs runtime | AI Platform Team | Module Android `gemma4local` |
| `ResultPresentationContext` | user-output-utilities | Copie/partage/snapshot dernier resultat et conventions d'affichage | Shared App Experience | Bibliotheque UI/application |

## 3) Relations upstream/downstream

- `CameraCaptureContext` -> upstream de `OcrSegmentationContext`.
- `OcrSegmentationContext` -> upstream de `CompositionAnalysisContext` et `HealthCritiqueContext`.
- `CompositionAnalysisContext` -> upstream de `GuidanceSynthesisContext` et partiellement de `ResultPresentationContext`.
- `LocalLlmRuntimeContext` -> upstream technique de `CompositionAnalysisContext`, `HealthCritiqueContext`, `GuidanceSynthesisContext`.
- `ResultPresentationContext` est un contexte transversal, consomme les sorties des contextes analytiques sans redefinir les regles metier.

## 4) Arborescence cible

```text
specs/domains/
├── domain-map.md
├── ingredient-intelligence/
│   ├── spec.md
│   ├── plan.md
│   ├── data-model.md
│   ├── contracts/
│   ├── tasks.md
│   ├── migration-index.md
│   └── traceability.csv
├── health-insights/
│   ├── spec.md
│   ├── plan.md
│   ├── data-model.md
│   ├── contracts/
│   ├── tasks.md
│   ├── migration-index.md
│   └── traceability.csv
├── capture-experience/
│   ├── spec.md
│   ├── plan.md
│   ├── data-model.md
│   ├── contracts/
│   ├── tasks.md
│   ├── migration-index.md
│   └── traceability.csv
├── ai-runtime-platform/
│   ├── spec.md
│   ├── plan.md
│   ├── data-model.md
│   ├── contracts/
│   ├── tasks.md
│   ├── migration-index.md
│   └── traceability.csv
└── user-output-utilities/
    ├── spec.md
    ├── plan.md
    ├── data-model.md
    ├── contracts/
    ├── tasks.md
    ├── migration-index.md
    └── traceability.csv
```

## 5) Matrice de fusion (source feature -> cible)

| Source feature | Domaine cible | Fichier cible principal | Section cible |
|---|---|---|---|
| `001-scan-ingredients` | ingredient-intelligence | `ingredient-intelligence/spec.md` | OCR texte editable + structuration ingredients |
| `002-ingredient-health-critique` | health-insights | `health-insights/spec.md` | Critique sante par population (segment valide read-only) |
| `003-additive-kpi-results` | ingredient-intelligence | `ingredient-intelligence/spec.md` | KPI additifs, classement et justification |
| `004-llm-summary-recommendations` | health-insights | `health-insights/spec.md` | Synthese et recommandations actionnables |
| `005-camera-start-temp-scan` | capture-experience | `capture-experience/spec.md` | Demarrage camera et scan temporaire |
| `006-identify-photo-ingredients` | ingredient-intelligence | `ingredient-intelligence/spec.md` | Extraction ingredients depuis photo + correction |
| `007-live-camera-preview-capture` | capture-experience | `capture-experience/spec.md` | Preview live + capture explicite + permissions |
| `008-capture-photo-texte-ocr` | capture-experience + ingredient-intelligence | `capture-experience/spec.md` + `ingredient-intelligence/spec.md` | Capture (capture-experience) + reconnaissance texte (ingredient-intelligence) |
| `009-llm-bilan-composition-ingredients` | ingredient-intelligence + ai-runtime-platform | `ingredient-intelligence/spec.md` + `ai-runtime-platform/spec.md` | Bilan composition (metier) + appel runtime local Gemma |
| `010-message-bienvenue-sourire` | capture-experience | `capture-experience/spec.md` | Message accueil et ton UX |
| `011-api-gemma4-telephone` | ai-runtime-platform | `ai-runtime-platform/spec.md` | Contrat de disponibilite API locale et erreurs |
| `012-home-layout-mediapipe-status` | capture-experience | `capture-experience/spec.md` | Ordonnancement ecran home + statut MediaPipe |
| `013-isoler-liste-ingredients` | ingredient-intelligence | `ingredient-intelligence/spec.md` | Isolation de la liste ingredients + predictibilite |
| `014-capture-liste-ingredients` | ingredient-intelligence + capture-experience | `ingredient-intelligence/spec.md` + `capture-experience/spec.md` | Detection vraie liste (metier) + feedback utilisateur de capture |
| `015-analyse-ocr-llm` | health-insights + ai-runtime-platform | `health-insights/spec.md` + `ai-runtime-platform/spec.md` | Redirection automatique vers analyse + orchestration runtime |

## 6) Regles de merge et traceabilite

- Conserver tous les IDs d'exigences (`FR-*`, `SC-*`) dans une colonne `source_requirement_id` de chaque `traceability.csv`.
- Ajouter `source_feature_id` (`001`..`015`) pour chaque exigence migree.
- Ne jamais dupliquer une exigence metier entre domaines: utiliser une reference croisee (`Ref:`) vers le domaine owner.
- Creer `migration-index.md` par domaine avec:
  - liens vers les anciens fichiers source,
  - sections cibles exactes,
  - statut (`planned`, `migrated`, `validated`),
  - strategie de conflit.
- Conflits inter-domaines: owner = domaine le plus proche de la regle metier; les autres domaines ne gardent qu'un lien de dependance.

## 7) Strategie de migration (rollback-safe)

### Phase 0 - Baseline

- Creer `specs/domains/domain-map.md`.
- Geler les specs source (pas de suppression).
- Checkpoint: aucune perte documentaire.

### Phase 1 - Domain skeleton

- Creer les 5 dossiers de domaines et fichiers vides canoniques.
- Initialiser `migration-index.md` et `traceability.csv`.
- Checkpoint: structure prete pour migration incrementale.

### Phase 2 - Migration core

- Migrer `ingredient-intelligence` (001,003,006,009-part,013,014-part,008-part).
- Migrer `health-insights` (002,004,015-part).
- Checkpoint: toutes regles metier critiques disponibles en structure domaine.

### Phase 3 - Migration supporting

- Migrer `capture-experience` (005,007,010,012,008-part,014-part).
- Migrer `ai-runtime-platform` (011,009-part,015-part).
- Checkpoint: responsabilites techniques/UX separees des regles metier.

### Phase 4 - Generic et harmonisation

- Consolider `user-output-utilities` (copie/partage/persistance conventions).
- De-dupliquer exigences en references croisées.
- Checkpoint: plus de recouvrement non justifie.

### Phase 5 - Validation finale

- Verification complete de couverture des features 001..015.
- Revue de comprehension (objectif < 30 min pour nouveau dev).
- Suppression eventuelle des anciennes specs uniquement apres validation.

## 8) Checklist de validation

- [ ] Chaque feature spec est mappee vers >= 1 domaine.
- [ ] Chaque domaine a un owner explicite (equipe + unite de deploiement).
- [ ] Chaque overlap a une strategie de conflit documentee.
- [ ] Tous les `FR-*` et `SC-*` sont tracables vers source + cible.
- [ ] Aucun fichier source supprime sans entree `migration-index.md`.

## 9) Risques et questions ouvertes (max 10)

1. Frontiere `ingredient-intelligence` vs `health-insights` autour des recommandations pre-KPI a figer.
2. Niveau de couplage souhaite entre `CameraCaptureContext` et `OcrSegmentationContext` (events vs shared state).
3. Statut de `MediaPipe` (capture-experience) a clarifier: pure UX ou precondition metier.
4. Politique de normalisation de texte (byte-for-byte vs normalisation canonique) a unifier cross-domaines.
5. `user-output-utilities` pourrait rester transverse sans dossier dedie si l'equipe prefere limiter les domaines.
6. Besoin d'ADR explicites pour choix d'owner en cas d'exigences mixtes UX+metier.
7. Strategie de versioning des contrats `contracts/` lors de la migration a definir.
8. Risque de divergence temporaire entre specs source et specs domaine pendant phases 2-4.
9. Charge de maintenance des `traceability.csv` si non automatisee.
10. Definition exacte des deployment units (gradle modules actuels vs futurs) a confirmer.
