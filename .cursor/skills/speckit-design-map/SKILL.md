---
name: "speckit-design-map"
description: "Route une demande vers le bounded context et dossier specs/domains cible, persiste .specify/feature.json, sortie minimale, puis enchaîne speckit-specify si l’intention produit est présente."
disable-model-invocation: true
compatibility: "Requires spec-kit project structure with specs/ directory"
metadata:
  author: "foodgpt"
  source: "custom"
---

# Speckit Design Map

## Rôle

Classer la demande dans le bon domaine DDD **avant** toute spec détaillée. Raisonnement DDD (Evans) : **en interne uniquement** — ne pas le déverser dans la réponse utilisateur.

## Détection de mode

- **Intake (défaut)** : l’utilisateur décrit une capacité, un bug, une évolution produit → routage + **enchaînement immédiat** vers `speckit-specify`.
- **Topo seule** : uniquement réorganisation / migration / mise à jour de `domain-map.md`, sans intention produit à spécifier → routage ou édition ciblée, **pas** d’enchaînement `speckit-specify`.

## Workflow intake (défaut)

1. Lire `specs/domains/domain-map.md` (source de vérité pour le routage).
2. Choisir le dossier domaine `specs/domains/<domain-kebab>/` le plus cohérent avec l’intention.
3. Si ambigu, poser **au plus 3** questions ciblées (frontière métier) ; sinon ne pas demander.
4. Créer le dossier si besoin ; si `spec.md` absent, l’initialiser depuis `.specify/templates/spec-template.md`.
5. Écrire ou mettre à jour `.specify/feature.json` :
   ```json
   { "feature_directory": "specs/domains/<domain-kebab>" }
   ```
   (chemin relatif à la racine du repo, comme les autres commandes Spec Kit.)

## Sortie utilisateur (STRICT — minimal)

Une **seule** ligne, aucun autre paragraphe ni liste pour cette étape :

```text
→ specs/domains/<domain-kebab>
```

Optionnel : ajouter après un tiret **≤ 8 mots** de justification (ex. contexte bounded). Pas de tableau, pas de dump du domain-map.

## Enchaînement obligatoire (intake)

Immédiatement après la ligne ci-dessus, **sans attendre une nouvelle instruction utilisateur**, appliquer le skill **`speckit-specify`** dans la **même** réponse :

- Réutiliser le message utilisateur d’origine (intention / `$ARGUMENTS`) comme description de feature.
- Suivre `speckit-specify` à la lettre (spec dans le `spec.md` du domaine routé, checklist, etc.).
- Ne pas répéter le contenu du routage au-delà de la ligne unique.

## Workflow topo seule (sans intention produit)

- Mettre à jour ou proposer les changements pertinents sous `specs/domains/` (ex. `domain-map.md`) selon la demande.
- **Sortie utilisateur** : au plus **2 lignes** (fichiers touchés + résumé en une phrase).
- Ne **pas** invoquer `speckit-specify`.

## Garde-fous (internes, non affichés)

- Noms de domaines en kebab-case, langage métier ; pas de domaine fourre-tout (`utils`, `common`, etc.) sauf exception explicite utilisateur (voir constitution).
- Ne pas supprimer d’anciennes specs sans trace documentée ailleurs.
- En cas de conflit de langage ubiquitaire, trancher ou noter dans `specs/domains/domain-map.md` plutôt que dans le chat.
