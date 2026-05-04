# Contract: Liste d’ingrédients → Critique santé (LLM local Gemma)

## Objectif

Définir le contrat entre l’**UI / ViewModel** et le **moteur d’inférence** pour la feature **002** : analyse d’impact santé **prudente**, **quatre populations**, à partir du **segment ingrédients validé** issu du **scan** (pas de saisie libre comme source principale), **sans envoi réseau**.

## Préconditions

- `ingredientText` MUST être **identique** au **segment ingrédients validé** courant du parcours scan (FR-001, **SC-005**). L’UI associée MUST être **lecture seule** sur ce texte (clarification 2026-05-04).
- Si aucun segment validé n’est disponible, l’analyse MUST **not** appeler le LLM : état `input_invalid` avec `reasonCode = no_validated_segment` (ou équivalent documenté).
- `ingredientText` non vide et satisfaisant les règles de longueur minimale produit après les règles ci-dessus (sinon `input_invalid` sans appel LLM).
- Aucune étape de ce contrat n’envoie `ingredientText` ni la réponse vers un service distant.

## Commande

- **Command**: `AnalyzeIngredientHealthCritique`
- **Payload**:
  - `requestId` (`String`, UUID)
  - `ingredientText` (`String`) — **copie conforme** du segment validé
  - `maxInferenceMs` (`Long`, optionnel)

## Sorties attendues (Result Contract)

### 1) Critique prête

```json
{
  "state": "critique_ready",
  "requestId": "uuid",
  "llmRawText": "… texte complet avec marqueurs de section …",
  "sections": {
    "ENFANTS": "…",
    "FEMMES_ENCEINTES": "…",
    "ADULTES": "…",
    "PERSONNES_AGEES": "…"
  },
  "parseWarnings": [],
  "disclaimer": "Information indicative à visée éducative ; ne remplace pas un avis médical ou nutritionnel personnalisé.",
  "processedAt": "2026-05-04T12:00:00Z"
}
```

**Contraintes**:

- `state=critique_ready` ⇒ `llmRawText` non vide.
- Les quatre clés de `sections` SHOULD être présentes pour SC-001 ; si le parseur ne retrouve pas un marqueur, la clé peut être une chaîne vide **et** une entrée correspondante MUST apparaître dans `parseWarnings`.

### 2) Erreur moteur / modèle

```json
{
  "state": "inference_error",
  "requestId": "uuid",
  "errorCode": "gemma_not_found | gemma_load_failed | gemma_timeout | inference_failed",
  "message": "… message utilisateur clair …",
  "processedAt": "2026-05-04T12:00:00Z"
}
```

### 3) Entrée invalide (métier)

```json
{
  "state": "input_invalid",
  "requestId": "uuid",
  "reasonCode": "empty | too_short | no_validated_segment",
  "message": "…",
  "processedAt": "2026-05-04T12:00:00Z"
}
```

## Contrat prompt (marqueurs)

Le prompt système MUST exiger des sections avec les marqueurs **exactement** :

- `###ENFANTS`
- `###FEMMES_ENCEINTES`
- `###ADULTES`
- `###PERSONNES_AGEES`

Chaque section MUST demander explicitement : **points de vigilance**, **explication / nuance** (faits vs incertitudes), **niveau de prudence** ; interdiction de **diagnostic** ; encouragement à consulter un professionnel de santé pour les situations à risque (notamment grossesse).

## Implémentation Kotlin (référence)

- `reasonCode` JSON `no_validated_segment` ↔ `InputInvalidReason.NO_VALIDATED_SEGMENT` dans `app/src/main/java/com/foodgpt/healthcritique/HealthCritiqueModels.kt` (sérialisation UI / logs si applicable).

## Mapping scénarios d’acceptation (spec 002)

| Scénario spec | Attente contrat |
|---------------|-----------------|
| US1 — 4 sections | `critique_ready` + 4 clés `sections` renseignées ou `parseWarnings` documentés |
| US1 — lecture seule | Aucun chemin d’analyse sans `ingredientText` issu du segment validé ; pas d’édition côté contrat |
| US1 — sans segment | `input_invalid` / `no_validated_segment` |
| US1 — liste vide | `input_invalid` / `empty` |
| SC-005 | Assertion : chaîne envoyée au runner == segment validé (hors normalisation explicitement partagée avec le scan) |
| US3 — copie | UI MUST exposer copie non vide (SC-003) |
| FR-006 — dernière analyse | Persistance `LastHealthAnalysisSnapshot` après `critique_ready` avec même `ingredientRaw` que segment analysé |
