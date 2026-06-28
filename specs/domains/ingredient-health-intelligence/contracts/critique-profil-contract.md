# Contrat — critique santé ciblée par profil utilisateur (Feature N)

## Portée

- S'applique au **prompt de critique santé** + **parseur** + **restitution UI** construits par le module `healthcritique`.
- Supersède et **retire** le format 4-marqueurs strict Feature L (`IHI-L-FR-009` / `IHI-L-SC-004`) — traçabilité en spec Feature N.
- Respecte Feature C : aucune incitation à inventer des ingrédients absents (`IHI-N-FR-005`).
- Ne modifie pas le bilan de composition ni les KPI `additive-risk-insights` (les « alertes » juxtaposées au-dessus du Niveau de prudence sont les KPI existants — `IHI-C-FR-007`).

## Frontière DDD — consommation du profil

- IHI **consomme** le profil via le contrat :
  - `UserProfile` (enum : `FEMME_ENCEINTE`, `ENFANT`, `PERSONNE_AGEE`, `ADULTE`, `SPORTIF` ; `label` français ; `marker` canonique `###<...>` ; `DEFAULT = ADULTE`).
  - `UserProfileProvider.current(): UserProfile` (retourne le profil sélectionné ou `DEFAULT`).
- La **saisie/persistance** du profil (Onboarding + écran « Paramètres / Profil ») est du ressort du domaine `user-guidance-experience`, qui fournira une implémentation de `UserProfileProvider` sur ce même contrat.
- IHI fournit `DefaultUserProfileProvider` (fallback `ADULTE`, settable pour tests) afin que le flux soit testable indépendamment de UGE.

## Mécanisme (prompt)

- Remplacement **en dur versionné** dans `HealthCritiquePromptBuilder.buildSystemInstruction(profile: UserProfile)` (pas d'externalisation — `IHI-N-FR-015`).
- Construction **répétable** : même (segment, profil) → même prompt (`IHI-N-FR-014`).

## Contenu obligatoire du `systemInstruction(profile)`

| Bloc | Exigence | Réf spec |
|------|----------|----------|
| Persona expert | « expert de renommée mondiale en nutrition clinique et en cancérologie préventive » (hérité Feature L) | `IHI-N-FR-004` / `IHI-L-FR-001` |
| Langue | rédaction intégrale en français | `IHI-L-FR-011` |
| Disclaimer | « Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé. » | `IHI-L-FR-011` |
| Analyse | ingrédient par ingrédient ; correction OCR sans invention | `IHI-L-FR-002` |
| Dimensions de risque | cancérogène, mutagène, neurotoxique, métabolique, inflammatoire | `IHI-L-FR-003` |
| Hiérarchie des preuves | faits établis (CIRC/OMS) / incertitudes / hypothèses | `IHI-L-FR-004` |
| Dose/exposition | contextualiser ; interdire conclusions catégoriques | `IHI-L-FR-005` |
| Opacité | signaler termes ambigus | `IHI-L-FR-006` |
| Garde-fous éthiques | aucun diagnostic/prescription ; refus poli + orientation professionnel de santé | `IHI-L-FR-007` |
| Populations vulnérables | vigilance transversale intégrée (immunodéprimées, antécédents familiaux cancer) | `IHI-L-FR-008` |
| Rappel profil | exigé en tête : « Évalué pour vous : <profil label> » | `IHI-N-FR-003` |
| Format de sortie | **uniquement** le marqueur canonique du profil sélectionné (ex. `###FEMME_ENCEINTE`) ; aucun texte de critique avant le rappel ; aucun autre marqueur | `IHI-N-FR-002`/`006`/`009` |
| Niveau de prudence | `Niveau de prudence : <Faible\|Modéré\|Élevé> — <texte court>` | `IHI-N-FR-007` |
| Cartes d'ingrédients à vigilance | par ingrédient Modéré/Élevé : `• <nom> \| <code> \| <type>` + `Impact :`, `Fait établi :`, `Nuance :`, `Cible particulièrement :` | `IHI-N-FR-008` |
| Liste complète | une ligne par ingrédient `- <nom> : <RAS\|Modéré\|Élevé>` | `IHI-N-FR-011` |
| Liste très longue | synthèse des risques majeurs en tête des cartes (seuil `LONG_LIST_INGREDIENT_THRESHOLD`) | `IHI-L-FR-012` |
| Langue/illisible | conserver le marqueur + demander précisions | `IHI-L-FR-013` |

## Format de sortie attendu (profil unique)

```text
Évalué pour vous : <profil label>

###<MARKER>

Niveau de prudence : <Faible|Modéré|Élevé> — <texte court justificatif>

• <nom ingrédient 1> | <code éventuel> | <type>
  Impact : <texte>
  Fait établi : <texte (réf CIRC/OMS si applicable)>
  Nuance : <texte (dose/fréquence/cuisson)>
  Cible particulièrement : <populations>

• <nom ingrédient 2> | ...

Liste complète des ingrédients analysés :
- <nom 1> : <RAS|Modéré|Élevé>
- <nom 2> : <RAS|Modéré|Élevé>
```

## Restitution UI (consommateur du `ProfileCritiqueResult`)

- **Rappel** « Évalué pour vous : <label> » en tête (avec badge « profil par défaut » si `isDefaultProfile`).
- **Niveau de prudence** : jauge 3 paliers (Faible / Modéré / Élevé) + texte court, placé juste sous la zone KPI `additive-risk-insights` (« alertes »).
- **Cartes ingrédients** : seules les `IngredientRiskCard` (vigilance Modérée/Élevée) sont affichées en clair (accordéon repliable).
- **Bouton** « Voir tous les ingrédients analysés » : déploie la liste compacte `fullIngredientList` (nom + statut).
- Cas « profil par défaut » : signal visuel + invitation à personnaliser.
- Cas « sortie 4-marqueurs » (legacy) : rejetée (`non-analysable-response`), non affichée comme succès.

## Tests de conformité minimaux

- **Prompt** : tests JVM sur `buildSystemInstruction(profile)` — marqueur unique du profil + rappel « Évalué pour vous » + absence des autres marqueurs + blocs prudence/cartes/liste + héritage Feature L (persona, dimensions, garde-fous).
- **Parseur** : tests JVM — extraction (marqueur unique, prudence, cartes, liste compacte) ; rejet d'une sortie 4-marqueurs.
- **Profil** : tests JVM — `UserProfile` labels/marqueurs/`DEFAULT` ; `DefaultUserProfileProvider` fallback `ADULTE`.
- **Sémantique** (MVP) : relecture humaine + traçabilité sur jeu fixe d'ingrédients (`IHI-N-SC-012`, aligné `IHI-C-FR-006`).
