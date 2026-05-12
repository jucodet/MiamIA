# Contract: Home Layout Order and Status Indicator

## Purpose

Definir le contrat fonctionnel d'affichage de l'ecran d'accueil pour l'ordre vertical de reference, le voyant MediaPipe, et la compatibilite inter-specs.

## Layout Composition Contract

### `HomeLayoutRenderInput`

```json
{
  "screen": "home",
  "orientation": "portrait",
  "sections": [
    "STATUS_INDICATOR",
    "WELCOME_MESSAGE",
    "PHOTO_PREVIEW",
    "CAPTURE_BUTTON"
  ]
}
```

### `HomeLayoutRenderOutput`

```json
{
  "status": "rendered",
  "resolvedOrder": [
    {"section": "STATUS_INDICATOR", "orderIndex": 1},
    {"section": "WELCOME_MESSAGE", "orderIndex": 2},
    {"section": "PHOTO_PREVIEW", "orderIndex": 3},
    {"section": "CAPTURE_BUTTON", "orderIndex": 4}
  ],
  "spacing": {
    "from": "PHOTO_PREVIEW",
    "to": "CAPTURE_BUTTON",
    "type": "STANDARD_FIXED"
  }
}
```

## MediaPipe Status Contract

### `MediaPipeStatusViewState`

```json
{
  "state": "CHECKING|AVAILABLE|UNAVAILABLE",
  "label": "Verification...|Disponible|Indisponible",
  "colorToken": "neutral|success|warning_or_error",
  "isFirstVisibleElement": true
}
```

## Inter-Spec Priority Contract

### `HomeSpecPriorityRule`

```json
{
  "scope": "HOME_UI_ORDER",
  "authoritativeSpec": "012-home-layout-mediapipe-status",
  "compatibleBehaviorSpecs": [
    "010-message-bienvenue-sourire",
    "007-live-camera-preview-capture"
  ],
  "rule": "UI order from 012 MUST be respected while preserving 010/007 business behavior"
}
```

## Invariants

- L'ordre des 4 blocs d'accueil est strictement determine par `012-home-layout-mediapipe-status`.
- Le voyant de statut est toujours le premier element visible en haut de l'accueil.
- Le voyant combine couleur et libelle texte a tout instant.
- Un etat `Verification...` est affiche tant que la detection initiale n'est pas terminee.
- Le bouton capture reste juste sous l'encart photo avec un espacement standard fixe.
- Le perimetre de ce contrat est limite au mode portrait.

## Implementation Mapping (2026-04-28)

- Etat voyant et mapping libelle/couleur: `app/src/main/java/com/miamia/home/MediaPipeStatusViewState.kt`
- Ordre de reference portrait: `app/src/main/java/com/miamia/home/HomeLayoutSections.kt`
- Espacement standard fixe: `app/src/main/java/com/miamia/home/HomeSpacingRules.kt`
- Priorite inter-specs 012: `app/src/main/java/com/miamia/home/HomeSpecPriorityResolver.kt`
