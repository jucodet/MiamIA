# Recherche — Critique santé liste d’ingrédients (002)

## 1. Où exécuter le LLM (local vs distant)

**Decision**: Réutiliser **Gemma en inférence locale** (LiteRT-LM), sur le même principe que la spec **009** (bilan composition), pour générer à la fois le **prompt système structuré** et la **réponse** affichée dans l’app.

**Rationale**: Le dépôt est une app Android déjà équipée de LiteRT-LM et de prompts système contrôlés (`LiteRtGemmaEngine`, `AndroidGemma4LocalGateway`). La constitution impose traçabilité, tests d’acceptation et performance mesurable ; un chemin local unique simplifie confidentialité, tests instrumentés et alignement avec FR « réponse générée » sans dépendre d’un backend.

**Alternatives considered**:

- **API cloud**: permettrait des modèles plus grands ; écart avec le socle 009 et risques données / conformité à valider explicitement en spec (non retenu sans amendement spec).
- **Prompt uniquement exporté** (utilisateur colle ailleurs): satisfait FR-002 « produire un prompt » mais pas FR-004 « afficher la réponse générée » sans second canal ; retenu comme **fonction secondaire** (copie du prompt + copie réponse), le chemin principal restant génération in-app.

## 2. Structure de la sortie LLM (4 populations)

**Decision**: Imposer des **marqueurs de section** stables dans le prompt système (style `###ENFANTS`, `###FEMMES_ENCEINTES`, `###ADULTES`, `###PERSONNES_AGEES`) pour parsing et tests ; chaque bloc MUST contenir vigilance + nuance + niveau de prudence (aligné spec User Story 1 scénario 2).

**Rationale**: Même pattern éprouvé que `###LISTE` / `###ANALYSE` en composition ; facilite validation automatique partielle (présence des 4 sections) et copie utilisateur.

**Alternatives considered**:

- **JSON strict**: plus rigide pour petit modèle on-device ; rejeté pour MVP.
- **Titres libres**: difficile à tester (SC-001 95 % sections sans intervention).

## 3. Persistance « historique minimal »

**Decision**: **SharedPreferences** (ou petit fichier JSON dans `filesDir`) pour la **dernière** analyse (ingrédients, horodatage, texte réponse, éventuellement hash du prompt) ; pas de Room obligatoire pour la v1 (spec Assumptions autorise extension ultérieure).

**Rationale**: FR-006 demande au moins la dernière analyse ; le projet utilise déjà `SharedPreferences` pour l’import modèle Gemma ; coût d’implémentation minimal.

**Alternatives considered**:

- **Room**: utile si historique multi-entrées ; surdimensionné pour « dernière seule » en v1.

## 4. Validation entrée (vide / trop courte)

**Decision**: Seuils alignés sur les messages produit : **vide** = refus immédiat ; **trop courte** = longueur minimale configurable (ex. moins de 10 caractères ou moins de 2 unités lexicales simples) avec message clair — valeur exacte à figer dans les tâches d’implémentation et tests ATDD.

**Rationale**: FR-005 ; la spec ne fixe pas le seuil numérique : la décision engineering est « règle unique documentée + tests » plutôt que NEEDS CLARIFICATION bloquant.

**Alternatives considered**:

- **Seuil uniquement métier sans nombre**: rejeté (non testable).

## 5. Performance

**Decision**: Objectif perçu **cohérent avec 009** : réponse ou erreur explicite dans une fenêtre **inférieure à 30 s** après lancement sur device milieu de gamme une fois le modèle chargé ; pas d’objectif requête/sec (usage interactif ponctuel).

**Rationale**: Constitution IV ; parallèle avec SC-003 spec 009.

## 6. Entrée « liste capturée » + lecture seule (spec + clarification 2026-05-04)

**Decision**: Le **segment ingrédients validé** dans le flux caméra est la **seule source** de vérité pour `ingredientText` côté critique santé. L’UI de l’onglet santé affiche ce texte en **lecture seule** ; le moteur d’analyse reçoit **exactement** cette chaîne (SC-005). Toute correction utilisateur passe par **retour au scan** et **revalidation** du segment.

**Rationale**: Alignement strict avec FR-001 et clarification session 2026-05-04 ; évite la dérive « texte modifié hors scan ».

**Alternatives considered**:

- **Champ éditable** sur l’onglet santé : exclu par décision produit pour cette release.
