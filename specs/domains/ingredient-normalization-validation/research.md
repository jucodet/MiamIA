# Research: ingredient-phrase-segment

## Decision 1: Règle d'ancrage canonique multilingue minimale

- **Decision**: reconnaître uniquement les ancres `Ingrédient`, `Ingrédients`, `Ingredient`, `Ingredients` (casse tolérée) et sélectionner la première occurrence rencontrée dans l'ordre de lecture.
- **Rationale**: couvre le besoin métier validé (FR + EN) tout en évitant un élargissement non cadré à d'autres langues qui augmenterait les faux positifs.
- **Alternatives considered**:
  - Limiter aux formes françaises uniquement (rejeté: perte de couverture d'étiquettes bilingues/anglaises).
  - Ouvrir à une longue liste multilingue (rejeté: ambiguïtés et maintenance plus lourde sans besoin validé).

## Decision 2: Stratégie de borne de fin hiérarchique

- **Decision**: appliquer la hiérarchie de fin suivante après ancre:
  1) fin de phrase si ponctuation terminale standard (`.`, `!`, `?`) détectée,
  2) sinon fin de ligne,
  3) sinon fin du texte disponible.
- **Rationale**: respecte les arbitrages de clarification, reste déterministe, et couvre les sorties OCR dégradées (ponctuation absente, monoligne).
- **Alternatives considered**:
  - Fin systématique à la première nouvelle ligne (rejeté: contredit la règle métier validée).
  - Fin systématique à la fin du texte (rejeté: capte trop de bruit hors liste).

## Decision 3: Politique d'échec et non-invention

- **Decision**: si aucune ancre reconnue n'est trouvée ou si le segment extrait est vide/inexploitable, bloquer l'analyse aval et retourner un état explicite de reprise.
- **Rationale**: aligne les invariants de fiabilité (`pas de contenu inventé`) et protège les domaines aval.
- **Alternatives considered**:
  - Fallback heuristique sans ancre (rejeté: risque de faux positifs élevés).
  - Analyse aval malgré segment douteux (rejeté: régressions qualité).

## Decision 4: Placement DDD des responsabilités

- **Decision**: conserver la production de `RawOcrText` dans `capture-recognition`, et centraliser l'isolation + validation de segment dans `ingredient-normalization-validation`.
- **Rationale**: maintient l'intégrité du modèle et évite la fuite de règles métier dans le contexte OCR.
- **Alternatives considered**:
  - Déplacer la règle dans `capture-recognition` (rejeté: mélange technique/métier).
  - Déplacer la règle dans `ingredient-health-intelligence` (rejeté: analyses dépendraient d'une extraction non stabilisée).
