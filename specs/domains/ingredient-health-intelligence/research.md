# Research — ancrage anti-hallucination (Feature C)

**Date**: 2026-05-13 | **Domain**: `ingredient-health-intelligence`

Les « inconnues » techniques ont été résolues par la spec + session **clarify** ; ce document consolide les **décisions d’implémentation** pour la Phase 1.

## 1. Politique d’équivalence v1 (stricte)

- **Decision** : v1 = sous-chaînes **littérales** du `ValidatedIngredientSegment` ; politique d’équivalence **vide** sauf **normalisations mécaniques** explicitement listées (casse, espaces, Unicode normal form si documenté).
- **Rationale** : minimize false « synonym » matches that look like grounded claims.
- **Alternatives considered** : catalogue minimal de synonymes (rejeté pour v1 par clarify Option A).

## 2. Contenu général vs « ce produit »

- **Decision** : blocs généraux autorisés s’ils sont **identifiables** ; toute mention explicite **ce produit** → uniquement termes **littéralement** dans le segment (**IHI-C-FR-004** b + **IHI-C-FR-005**).
- **Rationale** : garde la valeur éducative sans confondre avec l’étiquette.
- **Alternatives considered** : interdire tout contenu général dans un succès (trop restrictif — clarify Option B retenue).

## 3. Ancrage partiel

- **Decision** : **tout ou rien** — aucun succès avec analyse produit tronquée ; échec contrôlé (**IHI-C-FR-003**).
- **Rationale** : audit simple, pas de UX « demi-vérité ».
- **Alternatives considered** : livraison partielle avec label UI (rejetée — clarify Option A).

## 4. Juxtaposition `additive-risk-insights`

- **Decision** : enrichissements autorisés **si** (a) additif **littéralement** dans le segment, (b) **attribution explicite** au domaine additifs, (c) pas de confusion dominante avec le texte étiquette (**IHI-C-FR-007**).
- **Rationale** : respecte la **Published Language** du domain-map sans faire du LLM une source additive.
- **Alternatives considered** : tout interdire hors segment textuel (rejeté — clarify Option B).

## 5. Vérification indépendante (MVP)

- **Decision** : **relecture humaine** + traçabilité suffisent ; pas de gate automatisée obligatoire sur chaque succès au MVP.
- **Rationale** : aligné clarify Option A et charge projet ; checks automatisés possibles en itération suivante.
- **Alternatives considered** : gate CI bloquant sur chaque réponse (reporté post-MVP).

## 6. État de l’implémentation actuelle (baseline)

- **Decision** : conserver `CompositionResultValidator.validateAgainstSource` comme point central mais le **resserrer** pour se rapprocher du ratio / règles spec (aujourd’hui seuil **50 %** de lignes checkables absentes — à faire évoluer vers politique **stricte** clarify).
- **Rationale** : moindre risque de régression que remplacer entièrement par un nouveau pipeline opaque.
- **Alternatives considered** : validation uniquement LLM-side par prompt (insuffisant pour garanties testables).

## 7. Dette spec Feature B (implémenté 2026-05-13)

- **Note** : le code `composition/` et `healthcritique/` a été aligné sur Feature C sans backfill exhaustif de **Feature B** dans `spec.md` ; poursuivre `/speckit-sync-backfill` pour refermer l’écart doc ↔ code.

## 8. Estimation énergétique Feature K (2026-05-13)

- **Decision** : section dédiée **`###ENERGIE_ESTIMEE`** placée **après** `###ANALYSE` et **avant** `###ADDITIFS_RISQUE` ; première valeur entière **kcal pour 100 g** lisible sur la première ligne du bloc (ex. `420` ou `kcal_pour_100g: 420`).
- **Rationale** : même flux LLM, parseur déjà sectionné ; ordre compatible avec extension progressive des prompts (LiteRt 6 sections, Hybrid 6, Android 5 → alignés).
- **Alternatives considered** : second passage LLM dédié (rejeté v1 — coût/latence) ; heuristique locale seule (rejeté v1 — spec privilégie sortie analysable du même flux).

## 9. Bornes d’affichage kcal/100 g

- **Decision** : entier **inclus 1..1100** (**clarify** 2026-05-13, **Option B**) ; toute valeur hors intervalle ou non parseable → `estimatedKcalPer100g = null` (pastille sans nombre trompeur).
- **Rationale** : aligné **IHI-K-FR-006** / **IHI-K-SC-002** ; couvre huiles et matrices très denses (~900 kcal/100 g) sans accepter valeurs absurdes (ex. 12_000).
- **Alternatives considered** : **1..950** (impl antérieure — trop strict vs spec) ; plage 50..900 (trop stricte) ; laisser 0 (rejeté, min = 1).

## 10. Personnalisation du prompt de critique santé (Feature L — 2026-06-28)

### 10.1 Mécanisme de personnalisation

- **Decision** : **remplacement en dur versionné** du contenu du prompt dans `HealthCritiquePromptBuilder` (contenu intégré au code, testable, répétable) ; pas d'externalisation configurée ni de registre de prompts.
- **Rationale** : aligné avec l'existait (`HealthCritiquePromptBuilder`), MVP testabilité/répétabilité (`IHI-L-SC-007`), pas de complexité de configuration hors périmètre.
- **Alternatives considered** : externalisation dans asset/fichier de config modifiable sans recompilation (rejeté — YAGNI, complexité non justifiée) ; registre versionné de prompts sélectionnables (rejeté — sur-architecture).

### 10.2 Populations vulnérables sans section dédiée

- **Decision** : populations sans section propre (immunodéprimées, antécédents familiaux cancer) traitées comme **vigilance transversale intégrée** dans chaque section pertinente (Points de vigilance / Nuances), sans nouvelle section ni préambule.
- **Rationale** : préserve le format de sortie strict des 4 marqueurs (non-régression parser, `IHI-L-SC-004`/`SC-005`) tout en honorant l'attention particulière.
- **Alternatives considered** : sous-bloc dédié dans `###ADULTES` (rejeté — perd la transversalité) ; préambule commun avant `###ENFANTS` (rejeté — casse le format « aucun texte avant ###ENFANTS »).

### 10.3 Périmètre critique seule

- **Decision** : personnalisation **limitée au prompt de critique santé** ; prompt du bilan de composition non modifié.
- **Rationale** : le prompt fourni vise l'évaluation des risques alimentaires et la sortie par population (critique) ; fusion avec le bilan composition (contrat distinct, pastille kcal — Feature K) introduirait un couplage hors scope.
- **Alternatives considered** : appliquer aux deux flux (rejeté — couplage inter-contrats) ; extraire un socle commun persona+hiérarchie (rejeté pour Feature L — ferait l'objet d'une feature distincte si besoin).

### 10.4 Seuil « liste très longue »

- **Decision** : seuil défini en **nombre d'ingrédients** (ex. ≥ 20), valeur exacte laissée au plan d'implémentation (constante `LONG_LIST_INGREDIENT_THRESHOLD` dans `HealthCritiqueConfig`), non en caractères.
- **Rationale** : la notion de « longue » renvoie au nombre d'ingrédients à analyser (charge de lecture), pas à la longueur textuelle déjà plafonnée par `MAX_INGREDIENT_TEXT_CHARS`.
- **Alternatives considered** : seuil en caractères aligné sur `MAX_INGREDIENT_TEXT_CHARS` (rejeté — mal corrélé à la charge d'analyse) ; pas de seuil chiffré, modèle juge (rejeté — critère non testable).

### 10.5 Validation de la conformité au prompt (MVP)

- **Decision** : conformité sémantique (persona, hiérarchie des preuves, populations vulnérables, garde-fous) tenue au MVP par **relecture humaine + traçabilité** sur un jeu fixe d'ingrédients (aligné `IHI-C-FR-006` MVP) ; format de sortie vérifié par le parseur existant (`IHI-L-SC-005`).
- **Rationale** : la conformité sémantique sur texte libre n'est pas fiablement automatisable au MVP ; le format, lui, est contractuel et parsable.
- **Alternatives considered** : audit automatisé bloquant sur chaque sortie (rejeté au MVP — reporté post-MVP) ; validation hybride parseur+LLM-juge (rejeté au MVP — coût/complexité).

### 10.6 Disclaimer

- **Decision** : conserver la constante `DISCLAIMER` existante (« Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé. ») déjà alignée sur le texte fourni par l'utilisatrice.
- **Rationale** : non-régression ; le disclaimer correspond déjà exactement à l'input Feature L.
