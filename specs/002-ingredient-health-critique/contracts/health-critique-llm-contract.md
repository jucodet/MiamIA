# Contract: Liste d’ingrédients → Critique santé (LLM local Gemma)

## Objectif

Définir le contrat entre l’**UI / ViewModel** et le **moteur d’inférence** pour la feature **002** : produire une analyse d’impact santé **prudente**, structurée en **quatre populations**, à partir d’une liste d’ingrédients en texte libre, **sans envoi réseau** (alignement pratique avec le socle Gemma local existant).

## Préconditions

- `ingredientText` non vide et satisfaisant les règles de longueur minimale produit (sinon état `input_invalid` sans appel LLM).
- Aucune étape de ce contrat n’envoie `ingredientText` ni la réponse vers un service distant (même politique que spec 009 pour le texte sensible).

## Commande

- **Command**: `AnalyzeIngredientHealthCritique`
- **Payload**:
  - `requestId` (`String`, UUID)
  - `ingredientText` (`String`)
  - `maxInferenceMs` (`Long`, optionnel, défaut aligné config produit / spec perf)

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
  "reasonCode": "empty | too_short",
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

## Mapping scénarios d’acceptation (spec 002)

| Scénario spec | Attente contrat |
|---------------|-----------------|
| US1 — 4 sections | `critique_ready` + 4 clés `sections` renseignées ou `parseWarnings` documentés |
| US1 — liste vide | `input_invalid` / `empty` |
| US2 — prudence / pas de diagnostic | Vérifié par contenu prompt + revue contenu ; tests manuels / sampling |
| US3 — copie | Hors contrat LLM ; UI MUST exposer buffer copie non vide (SC-003) |
| FR-006 — dernière analyse | Persistance `LastHealthAnalysisSnapshot` après `critique_ready` |
