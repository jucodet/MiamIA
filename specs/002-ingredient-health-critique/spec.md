# Feature Specification: Critique santé d’une liste d’ingrédients (prompt LLM)

**Feature Branch**: `002-ingredient-health-critique`  
**Created**: 2026-04-19  
**Status**: Draft  
**Input**: User description: "la question suivante doit être posée à un LLM : \"critique de la façon la plus objective possible cette liste d'ingrédients en fonction de son impact pour la santé, en ciblant 1 les enfants, 2 les femmes enceintes, 3 les adultes, 4 les personnes agées\""  
**Refinement (2026-05-04)**: La liste d’ingrédients servant d’entrée à la critique santé **doit être** la liste d’ingrédients **capturée lors du scan** (segment validé pour l’analyse), et non une saisie libre distincte comme parcours principal.

## Clarifications

### Session 2026-05-04

- Q: Sur l’écran critique santé, la liste issue du scan validé peut-elle être éditée avant l’analyse, ou reste-t-elle en lecture seule ? → A: **Lecture seule** sur l’écran critique santé ; le texte envoyé au LLM est strictement le **segment validé** ; toute correction de texte repasse par le **flux scan / confirmation de segment** (pas d’édition caractère par caractère dans l’onglet santé).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Obtenir une critique santé par population (Priority: P1)

En tant qu’utilisateur, après une **capture** qui fournit une **liste d’ingrédients** (issue du scan et
du flux de validation du segment ingrédients), je veux lancer une critique aussi objective que possible
de son impact potentiel sur la santé, avec une analyse séparée pour:
1) enfants, 2) femmes enceintes, 3) adultes, 4) personnes âgées.

**Why this priority**: C’est la valeur centrale: une lecture/critique adaptée à des profils différents,
**alignée sur le produit réellement scanné**.

**Independent Test**: Réaliser un scan aboutissant à une liste d’ingrédients validée, ouvrir la
critique santé, lancer l’analyse, puis vérifier que la réponse contient 4 sections (une par population)
et que le **texte soumis au modèle correspond à la liste capturée** (même contenu que la liste validée).

**Acceptance Scenarios**:

1. **Given** une liste d’ingrédients **issue du scan** (segment validé), **When** l’utilisateur lance la
   critique santé, **Then** le système retourne une réponse structurée en 4 sections (enfants, femmes
   enceintes, adultes, personnes âgées) **sans exiger une ressaisie manuelle de la liste comme prérequis**
   et **la liste affichée sur l’écran critique santé n’est pas modifiable** (lecture seule).
2. **Given** une liste courte (3–6 ingrédients) **provenant du scan**, **When** l’analyse est générée,
   **Then** chaque section contient au minimum: points de vigilance, explication, et niveau de prudence.
3. **Given** **aucune** liste exploitable après scan (segment vide ou non validé), **When** l’utilisateur
   tente la critique santé, **Then** le système refuse avec un message clair (pas d’analyse sur une entrée
   vide ou non issue d’une capture validée).

---

### User Story 2 - Obtenir une réponse “prudente” et non alarmiste (Priority: P2)

En tant qu’utilisateur, je veux que l’analyse distingue clairement faits, incertitudes et hypothèses,
et qu’elle évite les affirmations médicales définitives.

**Why this priority**: Réduit le risque de mauvaise interprétation et améliore la confiance.

**Independent Test**: Vérifier que la réponse inclut des formulations prudentes, et qu’elle ne donne
pas de diagnostic; elle doit inciter à demander un avis professionnel pour les cas à risque.

**Acceptance Scenarios**:

1. **Given** une liste **capturée** contenant des additifs/termes ambigus, **When** l’analyse est générée,
   **Then** la réponse explicite les incertitudes et évite les conclusions catégoriques.
2. **Given** une population sensible (grossesse), **When** l’analyse est générée,
   **Then** la réponse inclut une recommandation de prudence et de consultation si nécessaire.

---

### User Story 3 - Réutiliser la sortie (copier/partager) (Priority: P3)

En tant qu’utilisateur, je veux pouvoir copier le prompt final (et/ou la réponse) afin de l’utiliser
ailleurs, et conserver un historique minimal de mes analyses.

**Why this priority**: Permet d’exploiter immédiatement le résultat et de garder une trace.

**Independent Test**: Lancer une analyse à partir d’une **liste capturée**, puis copier le texte et
retrouver la dernière analyse.

**Acceptance Scenarios**:

1. **Given** une analyse affichée, **When** l’utilisateur appuie sur “Copier”,
   **Then** le contenu sélectionné est copié.
2. **Given** une analyse terminée, **When** l’utilisateur revient plus tard,
   **Then** il peut retrouver au moins la dernière analyse (**liste issue du scan** + date + résultat).

### Edge Cases

- Ingrédients dans une autre langue ou avec fautes: la réponse doit rester structurée et demander des
  précisions si nécessaire.
- Liste très longue: la réponse doit rester lisible (résumé + détails) et ne pas ignorer des éléments
  critiques.
- Ingrédients ambigus (ex: “arômes”): signaler l’ambiguïté et l’impact sur la confiance de l’analyse.
- Demande de conseils médicaux: refuser poliment le diagnostic et recommander un professionnel.
- **Écart scan / affichage**: si le segment ingrédients est corrigé **avant validation** dans le flux
  scan, la critique santé MUST refléter **la version validée** (dernière vérité utilisateur avant
  lancement).
- **Après validation**: sur l’écran critique santé, **aucune édition** du texte liste ; pour corriger
  une erreur OCR ou un segment, l’utilisateur MUST repasser par le parcours scan / revalidation du
  segment.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système MUST utiliser comme **entrée principale** de la critique santé la **liste
  d’ingrédients capturée lors du scan** et **validée** pour l’analyse (segment ingrédients), et MUST **ne
  pas** imposer une saisie manuelle indépendante comme seul moyen d’obtenir la liste à analyser. Sur
  l’écran critique santé, cette liste MUST être **affichée en lecture seule** ; le texte transmis au LLM
  MUST être **identique** au segment validé (alignement SC-005).
- **FR-002**: Le système MUST produire un **prompt** destiné à un LLM qui demande une critique la plus
  objective possible, avec 4 populations cibles (enfants, femmes enceintes, adultes, personnes âgées).
- **FR-003**: Le système MUST inclure dans le prompt une exigence de réponse structurée (4 sections) et
  des consignes de prudence (incertitudes, pas de diagnostic).
- **FR-004**: Le système MUST afficher la réponse générée et la rendre copiable.
- **FR-005**: Le système MUST gérer les entrées invalides (liste capturée vide, trop courte après
  validation, ou absence de capture validée) avec des messages clairs.
- **FR-006**: Le système MUST conserver un historique minimal (au moins la dernière analyse), incluant la
  **référence à la liste capturée** utilisée (contenu ou identifiant de session suffisant pour
  reconstituer ce qui a été analysé).

### Key Entities *(include if feature involves data)*

- **IngredientList**: liste **issue du parcours de capture** (texte du segment validé ; éventuelle
  normalisation pour affichage ou comparaison). Présentation **lecture seule** sur l’écran critique santé.
- **AnalysisRequest**: requête d’analyse (date/heure, populations, prompt final, **lien logique vers la
  capture / segment** dont la liste est issue).
- **AnalysisResult**: réponse du LLM (texte, structure détectée, avertissements).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% des analyses retournent une réponse structurée en 4 sections sans intervention
  manuelle.
- **SC-002**: 90% des utilisateurs comprennent la différence entre faits/incertitudes (mesuré via un
  test utilisateur: question de compréhension après lecture).
- **SC-003**: 99% des tentatives de copie du résultat réussissent (sans contenu vide).
- **SC-004**: 90% des utilisateurs trouvent l’analyse “claire et non alarmiste” (questionnaire post
  tâche).
- **SC-005**: 100% des analyses lancées depuis le parcours nominal utilisent une liste d’ingrédients
  **identique** à la liste capturée validée (contrôle par échantillonnage ou test de parcours sur jeux
  de données représentatifs).

## Assumptions

- L’application n’est pas un dispositif médical et ne remplace pas un avis professionnel.
- L’objectif est une aide à la lecture/éducation, pas une prescription.
- Une première version peut se limiter à un historique minimal (dernière analyse) avant d’étendre.
- **Dépendance**: un parcours **scan → liste d’ingrédients validée** existe ou est livré en parallèle ;
  la critique santé s’y branche comme consommateur de cette liste.
