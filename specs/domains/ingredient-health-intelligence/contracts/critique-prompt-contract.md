# Contrat — contenu du prompt de critique santé (Feature L)

## Portée

- S'applique au **prompt de critique santé** construit par `HealthCritiquePromptBuilder` (flux LLM critique par population).
- Ne modifie **pas** le prompt du bilan de composition (périmètre critique seule — clarify Q3 / `IHI-L-FR-017`).
- Respecte Feature C : aucune incitation à inventer des ingrédients absents (`IHI-L-FR-014`).

## Mécanisme

- Remplacement **en dur versionné** dans le builder (pas d'externalisation, pas de registre — clarify Q1 / `IHI-L-FR-016`).
- Construction **répétable** : même entrée → même prompt (`IHI-L-SC-007`).

## Contenu obligatoire du `systemInstruction`

| Bloc | Exigence | Réf spec |
|------|----------|----------|
| Persona expert | « expert de renommée mondiale en nutrition clinique et en cancérologie préventive, spécialisé dans l'évaluation des risques alimentaires » | `IHI-L-FR-001` |
| Langue | rédaction intégrale en français (synthèses + formulations de prudence) | `IHI-L-FR-011` |
| Disclaimer | « Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé. » | `IHI-L-FR-011` |
| Analyse | ingrédient par ingrédient ; correction mentale OCR vers dénomination scientifique/réglementaire la plus probable ; **sans jamais inventer d'ingrédients absents** | `IHI-L-FR-002` |
| Dimensions de risque | cancérogène, mutagène, neurotoxique, métabolique (pics glycémiques, cholestérol), inflammatoire | `IHI-L-FR-003` |
| Hiérarchie des preuves | (1) faits établis (CIRC/OMS, consensus), (2) incertitudes scientifiques (débats, doses massives animal), (3) hypothèses/mécanismes suspectés | `IHI-L-FR-004` |
| Dose/exposition | contextualiser dose et exposition ; interdire conclusions catégoriques (« toujours toxique », « poison ») | `IHI-L-FR-005` |
| Opacité | signaler l'opacité pour termes ambigus (« arômes », « épices », additifs non spécifiés) | `IHI-L-FR-006` |
| Garde-fous éthiques | aucun diagnostic, aucune prescription (régime/traitement) ; refus poli + orientation professionnel de santé si avis médical personnalisé | `IHI-L-FR-007` |
| Populations vulnérables | femmes enceintes/allaitantes, enfants, immunodéprimées, antécédents familiaux cancer — en **vigilance transversale intégrée** (pas de section/préambule ajouté) | `IHI-L-FR-008` |
| Format de sortie | uniquement `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES` dans cet ordre ; aucun texte avant `###ENFANTS` | `IHI-L-FR-009` |
| Blocs par section | (1) Points de vigilance (liste à puces courte), (2) Analyse par ingrédient & Nuances (faits vs incertitudes), (3) Niveau de prudence (Faible/Modéré/Élevé) avec justification prudente | `IHI-L-FR-010` |
| Liste très longue | synthèse des risques majeurs en tête de la section 2, puis détail (seuil = nombre d'ingrédients, `LONG_LIST_INGREDIENT_THRESHOLD`) | `IHI-L-FR-012` |
| Langue/illisible | conserver la structure des marqueurs ; demander poliment précisions/meilleure capture dans la section 2 | `IHI-L-FR-013` |

## Tests de conformité minimaux

- **Prompt** : tests JVM sur `buildSystemInstruction()` — présence des marqueurs de contenu (persona, 5 dimensions, 3 tiers de preuve, 4 populations vulnérables, disclaimer, 4 marqueurs de section).
- **Sortie** : non-régression `HealthCritiqueSectionParser` — 4 marqueurs ordonnés reconnus sur un jeu fixe (`IHI-L-SC-005`).
- **Sémantique** (MVP) : relecture humaine + traçabilité sur jeu fixe d'ingrédients (`IHI-L-SC-008`, aligné `IHI-C-FR-006`).
