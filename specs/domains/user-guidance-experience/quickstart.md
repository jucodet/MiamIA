# Quickstart - Homepage LLM Mock Trigger

## Goal

Verifier rapidement que le bouton homepage declenche le test bouchonne LLM et que la reponse (ou l'erreur) est affichee correctement.

## Preconditions

- Build Android fonctionnel sur machine locale.
- Runtime LLM local disponible pour les scenarios de succes.
- Spec de reference: `specs/domains/user-guidance-experience/spec.md`.

## Manual Validation Flow

1. Ouvrir l'application sur la homepage.
2. Verifier la presence d'un bouton dedie au test LLM.
3. Cliquer une fois sur le bouton.
4. Verifier l'affichage immediat d'un etat `running`.
5. Attendre le resultat:
   - si succes: verifier affichage lisible de la reponse LLM;
   - si echec: verifier message explicite avec categorie (`timeout`, `runtime-unavailable` ou `non-analysable-response`).
6. Pendant `running`, cliquer plusieurs fois sur le bouton:
   - verifier qu'aucune execution concurrente n'est declenchee.
7. Apres fin (`success` ou `failure`), relancer un nouveau test:
   - verifier que la relance est autorisee.

## Suggested Automated Checks

- Test ViewModel:
  - clic -> `running`;
  - `running` bloque les relances concurrentes;
  - timeout 30s -> `failure(timeout)`;
  - succes -> `success` + texte non vide.
- Test UI Compose:
  - bouton visible sur homepage;
  - rendu des etats `running/success/failure`;
  - affichage de la reponse multi-lignes.

## Expected Outcomes

- Tous les criteres SC-001 a SC-004 de la spec sont observables et verifiables.
- En cas de timeout fonctionnel, la categorie affichee doit etre `timeout` (pas `runtime-unavailable`).
