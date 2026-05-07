# Quickstart - photo-capture-llm-result-flow

## Goal

Valider manuellement le **lancement à froid** sur l'écran de **prise de photo** **sans barre d'onglets**, l'**ordre** des boutons, le **loader** sur l'écran capture après photo, la **navigation** vers l'**écran résultat**, le bouton **test LLM**, l'absence de navigation automatique si **retour pendant le chargement**, et le **retour** depuis le résultat vers la capture (FR-016).

## Preconditions

- Build Android fonctionnel (`app`).
- Runtime LLM local disponible pour les scenarios de succes (si le pipeline photo en depend).
- Spec : `specs/domains/user-guidance-experience/spec.md`.
- Contrat : `contracts/capture-llm-result-navigation-contract.md`.

## Manual Validation Flow

### A. Lancement et mise en page capture

1. **Tuer** l'application puis la **rouvrir** depuis le lanceur (cold start).
2. Vérifier que le **premier** écran utile est l'**écran de prise de photo** (aucun écran d'accueil à onglets intermédiaire) et qu'**aucune barre d'onglets** (Accueil / Caméra / etc.) n'est visible (US1, FR-015, SC-007).
3. Vérifier l'ordre : zone preview (ou message caméra indisponible), **bouton photo**, puis **bouton test LLM** directement en dessous.
4. Si caméra indisponible : vérifier le **message explicite** à la place du preview ; le bouton test reste utilisable si pertinent (US4).

### B. Parcours photo → resultat

1. Declencher une **capture** reussie.
2. Verifier en &lt; 1 s un **chargement visible** sur l'**ecran capture** (overlay acceptable), **sans** ecran resultat encore affiche.
3. Rester sur l'ecran jusqu'a la fin : verifier **navigation** vers l'ecran **resultat** avec texte lisible (ou ecran erreur coherent en cas d'echec).
4. Utiliser **Retour** depuis l'écran résultat : vérifier le retour sur l'écran **capture** sans onglets (FR-016).
5. Vérifier qu'une **nouvelle** capture ou un nouveau test est possible (FR-012).

### C. Abandon pendant chargement

1. Lancer une capture (ou test LLM) et des que le loader est visible, utiliser le **retour arrière** (ou quitter l'ecran capture).
2. Attendre la fin eventuelle du traitement en arriere-plan : verifier **aucune** ouverture automatique de l'ecran resultat ni pop-up incoherente.

### D. Test LLM

1. Sur l'ecran capture, appuyer sur **test LLM**.
2. Verifier le meme pattern : loader sur capture, puis resultat si restee sur l'ecran ; bouton test **desactive** pendant l'execution.
3. Pendant `running`, taps repetes : **aucune** execution concurrente.

## Suggested Automated Checks

- ViewModel / coordinator :
  - `in_progress` -> boutons photo et test desactives ;
  - terminal + ecran actif -> evenement navigation resultat emis ;
  - abandon -> pas d'evenement navigation au terminal.
- Tests UI (si disponibles) : ordre des noeuds / semantics pour les boutons.

## Expected Outcomes

- Conformité aux **SC-001** à **SC-007** de la spec dans les conditions d'application (notamment SC-004 / SC-006 : uniquement si l'utilisatrice reste sur l'écran capture jusqu'à la fin du traitement ou de la détection d'échec ; SC-007 : lancement à froid vers capture sans onglets).
