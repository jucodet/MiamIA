# Contrat UI — catalogue partagé des phrases d'attente (`WAITING_PHRASES`)

**Domaine** : `user-guidance-experience`  
**Référence spec** : Feature E (`UGE-E-FR-001` … `UGE-E-FR-005`, `SC-E-001` … `SC-E-003`)  
**Implémentation de référence** : `app/src/main/java/com/miamia/ui/shared/WaitingPhrases.kt`

## Obligations

1. **Source unique** : toutes les chaînes affichées en rotation sur les loaders suivants MUST provenir de la même liste immuable :
   - écran résultat (streaming) — tag Compose `streaming_waiting_phrase` ;
   - écran d'attente téléchargement modèle — tag Compose `download_waiting_phrase`.
2. **Cardinalité** : après livraison Feature E, la liste MUST contenir **exactement 21** entrées distinctes (11 baseline + 10 ajouts).
3. **Contenu** : les 10 ajouts MUST être ceux de l'annexe Feature E du `spec.md` **ou** des reformulations équivalentes approuvées en revue produit, sans violer UGE-E-FR-005.
4. **Unicité** : aucune paire d'entrées ne MUST avoir le même texte après trim des espaces en tête et fin.
5. **Comportement** : la cadence de rotation et le shuffle restent ceux déjà définis (Feature A / UGE-B-FR-007) — ce contrat ne modifie pas les temporisations.

## Consommateurs (non exhaustif)

| Écran | Fichier | Comportement attendu |
|-------|---------|----------------------|
| Streaming résultat | `app/.../result/LlmResultScreen.kt` | `WAITING_PHRASES.shuffled()`, rotation index |
| Attente téléchargement | `app/.../onboarding/ModelDownloadWaitingScreen.kt` | idem |

## Vérification

- Test JVM : `WaitingPhrasesCatalogFeatureETest` (voir `plan.md`).
- Vérification manuelle : section « Addendum Feature E » de `quickstart.md`.
