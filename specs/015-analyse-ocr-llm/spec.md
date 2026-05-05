# Feature Specification: Redirection analyse OCR LLM

**Feature Branch**: `[015-analyse-ocr-llm]`  
**Created**: 2026-05-05  
**Status**: Draft  
**Input**: User description: "plutôt qu'un onglet critique santé, je voudrais qu'après le scan et l'ocr sur la photo je sois redirigé sur un écran d'analyse par le llm du texte issu de l'ocr, soit la liste des ingrédients. Le nouvel écran doit afficher la liste des ingrédients capturée, ainsi que le bilan du llm"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Redirection automatique vers analyse (Priority: P1)

En tant qu'utilisateur, après avoir pris une photo et obtenu un texte OCR exploitable, je suis automatiquement redirigé vers un ecran d'analyse qui lance l'analyse LLM sur la liste d'ingredients capturee.

**Why this priority**: C'est le flux principal attendu. La valeur metier est de supprimer l'etape manuelle de navigation vers l'analyse.

**Independent Test**: Realiser un scan avec OCR valide et verifier que l'utilisateur arrive directement sur l'ecran d'analyse, sans action supplementaire de navigation.

**Acceptance Scenarios**:

1. **Given** un scan vient de produire une liste d'ingredients OCR valide, **When** le traitement OCR est termine, **Then** l'application redirige automatiquement vers l'ecran d'analyse LLM.
2. **Given** l'utilisateur est redirige sur l'ecran d'analyse, **When** l'ecran s'affiche, **Then** la liste affichee correspond au texte OCR capture pour ce scan.

---

### User Story 2 - Affichage resultat complet (Priority: P2)

En tant qu'utilisateur, sur le nouvel ecran d'analyse, je vois a la fois la liste d'ingredients capturee et le bilan du LLM pour comprendre rapidement le resultat.

**Why this priority**: La redirection seule ne suffit pas; l'utilisateur doit voir les deux informations utiles dans le meme parcours.

**Independent Test**: Depuis une redirection reussie, verifier que l'ecran contient les deux blocs attendus: liste capturee + bilan LLM lisible.

**Acceptance Scenarios**:

1. **Given** une analyse LLM aboutit, **When** le resultat est affiche, **Then** le bilan est visible sur le meme ecran que la liste capturee.
2. **Given** le bilan est disponible, **When** l'utilisateur consulte l'ecran, **Then** il peut distinguer clairement la liste source et le resultat d'analyse.

---

### User Story 3 - Gestion des echecs OCR ou analyse (Priority: P3)

En tant qu'utilisateur, si le texte OCR est vide/inexploitable ou si l'analyse n'aboutit pas, je recois un message clair et une action pour relancer le scan.

**Why this priority**: Les erreurs sont frequentes sur photo; un comportement clair evite la confusion et l'abandon.

**Independent Test**: Simuler OCR vide et echec d'analyse; verifier la presence d'un message comprehensible et d'une action de reprise.

**Acceptance Scenarios**:

1. **Given** le scan ne fournit pas de liste OCR exploitable, **When** le flux tente de passer a l'analyse, **Then** l'utilisateur voit un message explicite et reste dans un parcours de reprise scan.
2. **Given** le LLM ne retourne pas de bilan exploitable, **When** l'analyse se termine en erreur, **Then** l'ecran affiche une erreur lisible et propose de recommencer.

---

### Edge Cases

- Que se passe-t-il si l'utilisateur lance un nouveau scan pendant qu'une analyse est en cours?
- Que se passe-t-il si la liste OCR est extremement longue (troncature visuelle ou affichage progressif)?
- Que se passe-t-il si l'utilisateur revient en arriere depuis l'ecran d'analyse redirige?
- Que se passe-t-il si deux scans consecutifs produisent des listes differentes avant affichage final?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le systeme MUST rediriger automatiquement l'utilisateur vers l'ecran d'analyse LLM des que le scan et l'OCR produisent une liste d'ingredients exploitable.
- **FR-002**: Le systeme MUST utiliser comme entree d'analyse la liste d'ingredients issue du resultat OCR du scan courant.
- **FR-003**: Le nouvel ecran MUST afficher la liste d'ingredients capturee pour que l'utilisateur voie clairement le texte source analyse.
- **FR-004**: Le nouvel ecran MUST afficher le bilan genere par le LLM dans la meme vue que la liste capturee.
- **FR-005**: Le systeme MUST presenter un etat de chargement explicite pendant l'analyse afin que l'utilisateur comprenne que le traitement est en cours.
- **FR-006**: Le systeme MUST afficher un message d'erreur comprehensible si le texte OCR est vide/inexploitable ou si l'analyse LLM echoue.
- **FR-007**: Le systeme MUST proposer une action de reprise du parcours de scan apres erreur OCR ou erreur d'analyse.
- **FR-008**: Le systeme MUST eviter toute incoherence entre la liste affichee et la liste effectivement analysee dans un meme parcours utilisateur.

### Key Entities *(include if feature involves data)*

- **ResultatOCRCapture**: represente la liste d'ingredients extraite de la photo scannee, associee a un scan donne.
- **AnalyseLLM**: represente l'etat d'analyse (en cours, succes, echec) et le bilan affiche a l'utilisateur.
- **SessionAnalysePostScan**: represente le parcours utilisateur declenche automatiquement entre fin OCR et affichage du bilan.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Dans au moins 95% des scans avec OCR exploitable, la redirection automatique vers l'ecran d'analyse se produit sans action manuelle supplementaire.
- **SC-002**: Dans au moins 95% des analyses reussies, la liste d'ingredients affichee a l'utilisateur correspond exactement a la liste analysee.
- **SC-003**: Dans au moins 90% des parcours de test, les utilisateurs identifient en moins de 5 secondes la liste source et le bilan sur le nouvel ecran.
- **SC-004**: Pour 100% des echecs OCR/LLM, un message d'erreur comprehensible et une action de reprise scan sont proposes.

## Assumptions

- Le scan et l'OCR restent le point d'entree principal pour alimenter l'analyse des ingredients.
- La redirection automatique remplace l'acces principal via onglet dedie pour ce cas d'usage.
- Le bilan LLM attendu suit la meme logique metier de critique ingredients deja etablie dans le produit.
- Le flux de navigation existant permet de revenir vers le scan apres un echec ou apres consultation du resultat.
