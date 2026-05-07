# Migration Index - user-guidance-experience

## Source -> Target

- `005-camera-start-temp-scan/spec.md` -> `spec.md` (`Scope`: etats UX scan) [validated]
- `010-message-bienvenue-sourire/spec.md` -> `spec.md` (`Functional Requirements`) [validated]
- `012-home-layout-mediapipe-status/spec.md` -> `spec.md` (`Functional Requirements`: ordre vertical home/readiness) [validated]
- `015-analyse-ocr-llm/spec.md` -> `spec.md` (`Functional Requirements`: redirection post-OCR, reprise erreur) [validated]

## Conflict Decisions

- Owner decision: la contrainte de contenu du bilan n'est pas dans ce domaine; seule la mise en scene UX est documentee ici.

## Validation manuelle (quickstart)

- Scénarios `quickstart.md` : à rejouer sur appareil ou émulateur après merge (aperçu + ordre des boutons, flux photo → loader → écran résultat, abandon pendant chargement, test LLM, indisponibilité caméra). Aucun écart bloquant noté lors de l’implémentation automatisée (build local peut échouer si `R.jar` est verrouillé par un autre processus).
