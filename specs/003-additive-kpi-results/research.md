# Recherche — Écran KPI additifs (003)

## 1. Format de sortie LLM pour niveau + justification par additif

**Decision**: Étendre le **prompt composition** (flux existant `###LISTE` / `###ANALYSE`) avec une **sous-structure explicite dans `###ANALYSE`** (ou un troisième bloc `###ADDITIFS_RISQUE` si migration plus claire) où chaque ligne d’additif à risque suit un **motif machine** : `NIVEAU|nom_additif|justification_courte` avec `NIVEAU ∈ {VERT, ORANGE, ROUGE, INCERTAIN}`. Le parseur MVP lit ce bloc en priorité ; à défaut, repli **heuristique** sur l’analyse libre + liste d’ingrédients (marquage `à confirmer`).

**Rationale**: Le spec FR-001 exige niveau + justification **par additif** ; le bilan actuel (`GemmaBilanParser`) ne structure pas le risque. Un motif fixe reste compatible Gemma on-device et testable (SC-001). JSON strict est plus fragile pour petits modèles.

**Alternatives considered**:

- **Deuxième passage LLM** dédié additifs : meilleure qualité mais latence/coût ; reporté hors MVP.
- **Base locale E-additives** : précision élevée mais nouveau stockage et maintenance ; hors scope MVP sauf décision produit ultérieure.

## 2. Intégration UI avec le flux caméra / bilan

**Decision**: Afficher les KPI **dans la continuité du parcours `ScanState.BilanReady`** (Compose dans `CameraScreen` ou composant extrait `AdditiveKpiPanel` alimenté par `AnalysisDisplayResult` dérivé du texte modèle + `CompositionBilan`), sans navigation multi-écrans obligatoire (FR-006 « sans navigation complexe »).

**Rationale**: L’état `BilanReady` possède déjà `bilan` + `rawTranscript` ; l’écran résultat spec 003 est une **vue enrichie** du même moment fonctionnel.

**Alternatives considered**:

- **Écran dédié + navigation**: utile si d’autres entrées (saisie manuelle) alimentent le même écran ; possible en phase 2.

## 3. Couleurs et accessibilité

**Decision**: Pastilles **Material** (`AssistChip` / badge couleur) + **libellé textuel** du niveau (Faible / Modéré / Élevé / À confirmer) et `contentDescription` pour lecteurs d’écran — la couleur ne porte pas l’information seule (WCAG).

**Rationale**: Alignement constitution UX + évite erreurs d’interprétation si daltonisme.

## 4. Doublons, incohérences, troncature

**Decision**: Clé de dédoublonnage = **nom normalisé** (`trim`, `lowercase`, espaces compressés). Incohérence niveau vs justification → flag `confidence = INCOHERENT` sur la ligne (spec edge case). Justification tronquée à **N caractères** (ex. 120) avec ellipse, en conservant début (motifs sensibles en tête).

**Rationale**: FR-005 et edge cases spec.

## 5. Performance parsing / KPI

**Decision**: Parsing + agrégation KPI sur **Dispatchers.Default** ; objectif engineering **p95 inférieur à 50 ms** pour jusqu’à ~80 lignes d’additifs sur device milieu de gamme (mesurable avec test micro-benchmark optionnel).

**Rationale**: Constitution IV ; volume typique d’étiquette bien inférieur à 80 entrées.
