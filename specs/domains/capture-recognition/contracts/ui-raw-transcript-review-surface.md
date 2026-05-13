# Contract — UI surface « relecture texte capturé » (capture-recognition)

**Type** : contrat comportemental UI (application Android Compose).  
**Consommateurs** : équipe mobile ; pas d’API réseau.

## Obligations

1. **Zone texte** : tout contenu affiché issu du `RawOcrText` / transcription brute présenté comme « texte capturé » ou équivalent MUST être rendu dans une région à **hauteur contrainte** par rapport à la fenêtre visible courante, avec **défilement vertical** lorsque le contenu dépasse cette région.
2. **Zone actions** : les boutons d’action primaires associés à cette relecture (ex. confirmer, reprendre photo, nouveau scan) MUST rester **en dehors** du défilement du bloc texte principal, dans une bande **toujours visible** en disposition portrait standard (hauteur utile normale).
3. **Texte court** : si le contenu tient dans la région texte sans débordement, aucun défilement n’est **obligatoire** ; le texte MUST reste entièrement lisible (pas de troncature par `maxHeight` trop agressif).

## Non-obligations

- Contenu des écrans aval (`ingredient-normalization-validation`, résultats LLM sur autre route) : couverts par leurs propres specs sauf réutilisation volontaire du même pattern.

## Traçabilité spec

- `spec.md` — CR-FR-006, CR-FR-007, CR-FR-008 ; scénarios User Story 1 et 2.
