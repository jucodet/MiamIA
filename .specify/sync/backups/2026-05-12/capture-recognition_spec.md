# Domain Spec - capture-recognition

## Purpose

Capturer une image produit de facon fiable, produire un `RawOcrText` rattache a une session et garantir un parcours de reprise explicite quand la capture/OCR echoue.

## Scope

- Preview camera, capture, OCR, erreurs capteurs.
- Demarrage sur camera, gestion permissions, etats "pret/analyse/succes/echec".
- Gestion temporaire des photos (ephemeres, suppression fin de cycle).
- Emission de donnees brutes vers l'ACL de normalisation.

## Invariants

- Chaque `RawOcrText` appartient a une `ScanSession`.
- Une capture invalide ne doit pas produire de texte valide.
- Aucun contenu factice ne remplace un flux camera reel ou un resultat OCR.
- La photo de scan n'est jamais conservee de facon persistante dans ce domaine.

## Functional Requirements

- CR-FR-001: le domaine MUST fournir un apercu camera reel et une capture declenchee uniquement par action explicite utilisateur.
- CR-FR-002: le domaine MUST executer OCR sur l'appareil et publier le texte extrait sans envoi image/texte vers un service distant.
- CR-FR-003: le domaine MUST exposer des etats utilisateurs explicites (verification readiness, analyse en cours, succes, echec) sans blocage silencieux.
- CR-FR-004: le domaine MUST nettoyer les artefacts temporaires de capture en succes, annulation et erreur.
- CR-FR-005: le domaine MUST refuser la progression vers l'analyse amont si le texte OCR est vide ou inexploitable.

## Cross-domain Notes

- Le segment ingredients n'est pas determine ici: delegation a `ingredient-normalization-validation`.
- La redirection automatique post-OCR vers un ecran d'analyse est orchestree par `user-guidance-experience`.

## Source Mapping

- `specs/001-scan-ingredients/spec.md`
- `specs/005-camera-start-temp-scan/spec.md`
- `specs/007-live-camera-preview-capture/spec.md`
- `specs/008-capture-photo-texte-ocr/spec.md`
- `specs/012-home-layout-mediapipe-status/spec.md`
- `specs/015-analyse-ocr-llm/spec.md` (preconditions OCR exploitables)
