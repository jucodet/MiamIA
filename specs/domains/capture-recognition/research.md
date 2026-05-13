# Research — Bouton capture sous l’aperçu et libellé « Y a quoi là-dedans ? »

Date: 2026-05-13  
Domaine: `capture-recognition`  
Statut: Phase 0 complète.

## R-001 — Placement du bouton de capture vis-à-vis de l’aperçu vidéo

- **Decision**: Conserver la structure `Column` parente actuelle (aperçu en `Box(...height(360.dp))` puis actions), mais introduire une **bande d’action explicite (`CaptureActionBar`)** placée sous l’aperçu, séparée par un espacement vertical non ambigu (`Arrangement.spacedBy(HomeSpacingRules.standardFixedSpacing)` déjà en place, à renforcer si nécessaire). La bande d’action est un `Column` interne hors de la `Box` de prévisualisation.
- **Rationale**:
  - Garantit CR-FR-009 (aucun recouvrement) par construction : aucun composable d’action n’est enfant de la `Box` qui contient `CameraPreviewBox`.
  - Garantit CR-FR-010 (bande d’action dédiée sous l’aperçu).
  - Préserve CR-FR-007 (bande d’action visible sans scroll global).
  - Compatible avec les états `CameraUnavailable` (placeholder 360.dp) ET les états live preview (preview 360.dp) sans branche supplémentaire si extraction du sous-composable.
- **Alternatives considérées**:
  - **Empiler le bouton dans la `Box` d’aperçu (overlay)** : rejeté — viole CR-FR-009 (recouvrement) et CR-FR-010, motif central de la demande.
  - **Réduire la hauteur de l’aperçu (`height(300.dp)` ou ratio)** : non nécessaire ; le problème vient d’un empilement perçu, pas d’une hauteur excessive. À reconsidérer uniquement si des cas paysage / petits écrans révèlent une friction post-implémentation (cas limite déjà couvert dans la spec).
  - **`Scaffold` avec `bottomBar`** : sur-architecture pour 1 écran ; ne s’intègre pas naturellement avec le `Column` actuel qui contient déjà MediaPipeStatusIndicator + welcome message.

## R-002 — Renommage du libellé « Prendre la photo » → « Y a quoi là-dedans ? »

- **Decision**: Remplacer les deux occurrences hard-codées du libellé `"Prendre la photo"` dans `CameraScreen.kt` (états `CameraUnavailable` et états live preview) par `"Y a quoi là-dedans ?"` exactement, en conservant les test tags (`capture_photo_button`) et les semantics existants. Pas d’extraction immédiate en `strings.xml` (suivi possible).
- **Rationale**:
  - Diff minimal, traçable, sans risque sur le module i18n actuel (pas de string resources françaises dédiées à ce libellé aujourd’hui ; cohérent avec le reste de l’écran qui utilise des littéraux français).
  - Conserve les tests existants ciblant `capture_photo_button` (ils peuvent assert le libellé visible sans dépendre d’un id de resource).
- **Alternatives considérées**:
  - **Introduire `R.string.capture_action_primary`** : plus propre à long terme, mais hors scope minimal — proposé comme tâche de suivi (post-merge) pour ne pas mélanger refactor i18n et fix UI.
  - **Renommer aussi `capturePhoto()` / test tag** : non — le nom interne décrit l’action technique (prendre une photo), pas le libellé utilisateur. Le découplage est sain.

## R-003 — Espacement visuel garantissant « clairement dessous »

- **Decision**: Réutiliser `HomeSpacingRules.standardFixedSpacing` (déjà utilisé dans le `Column` interne des états live). Vérifier visuellement que l’espacement est ≥ 12 dp et donne une rupture nette entre la fin de la zone vidéo et le haut du bouton. Si la perception reste insuffisante après implémentation, ajouter un `Spacer(Modifier.height(8.dp))` dans la bande d’action plutôt que d’augmenter globalement l’espacement de la `Column` parente.
- **Rationale**:
  - Respecte la simplicité (V) : réutilisation de l’existant.
  - Évite une régression de densité sur d’autres écrans utilisant `HomeSpacingRules`.
- **Alternatives considérées**:
  - **`Divider`/`HorizontalDivider`** : ajoute un élément visuel non demandé. À envisager seulement si une revue UX explicite le requiert.

## R-004 — Tests d’acceptation Compose UI

- **Decision**: Étendre/ajouter trois tests UI Compose dans `app/src/androidTest/java/com/miamia/camera/` :
  1. `CameraCaptureLayoutUiTest` (étendu) : assert que le composable porteur du test tag `capture_photo_button` est positionné **strictement sous** la bounding box du composable `photo_preview_box` (top du bouton ≥ bottom du preview).
  2. `CaptureActionLabelUiTest` (nouveau) : assert que le test tag `capture_photo_button` affiche exactement le texte « Y a quoi là-dedans ? » dans l’état live preview.
  3. `CameraUnavailableLlmButtonUiTest` (étendu) : assert que le même test tag affiche « Y a quoi là-dedans ? » dans l’état `CameraUnavailable`.
- **Rationale**:
  - Tests indépendants des deux user stories (US1 placement, US2 libellé).
  - Réutilise les test tags existants — aucun renommage requis.
- **Alternatives considérées**:
  - **Screenshot tests** : utile, mais dépendant d’une infra non encore en place ; les assertions de bounding box Compose suffisent pour CR-FR-009.

## R-006 — Hauteur d’aperçu adaptative (corrige perception « à cheval »)

- **Decision (révision post-implémentation)** : remplacer la hauteur fixe `Box(...height(360.dp))` de l’aperçu (live et placeholder `CameraUnavailable`) par `Box(...weight(1f).heightIn(min=220.dp, max=480.dp))` à l’intérieur d’une `Column` qui détient elle-même `weight(1f)` dans la `Column` racine `fillMaxSize`. La bande d’action `CaptureActionBar` (hauteur naturelle) reste ainsi **toujours visible sous l’aperçu**, quel que soit le format d’écran ou la présence simultanée du bandeau d’accueil et du statut MediaPipe.
- **Rationale** :
  - Sur écrans courts, une hauteur fixe `360.dp` poussait visuellement la bande d’action contre le bord inférieur du flux vidéo (couleur du bouton flush contre la zone sombre), produisant une perception « à cheval » alors que la stack Compose plaçait correctement les enfants en vertical.
  - `weight(1f)` redistribue l’espace résiduel à l’aperçu (jamais à la bande d’action), donc la bande conserve sa hauteur naturelle et est précédée d’un gap explicite (`spacedBy(12 dp)` parent + `Spacer(8 dp)` interne ⇒ ≥ 20 dp de séparation visuelle).
  - `heightIn(min, max)` garantit (i) un aperçu utilisable même sur écrans très courts et (ii) un aperçu non « gonflé » sur écrans très grands.
- **Alternatives considérées** :
  - **Conserver `height(360.dp)` + augmenter le `Spacer` à 24 dp** : laisse le bouton hors écran sur petits téléphones — non viable.
  - **`Scaffold.bottomBar` pour la bande d’action** : viable mais plus invasif (modifie l’imbrication avec MediaPipe/welcome). Rejeté pour minimiser le diff.

## R-005 — Impact sur les autres domaines

- **Decision**: Aucun changement de contrat publié vers `ingredient-normalization-validation`, `user-guidance-experience`, ni `local-llm-runtime`. Le contrat de session (`session-capture-intent-for-implicit-validation.md`) reste inchangé : seule l’UI déclenchant `capturePhoto(...)` change visuellement.
- **Rationale**:
  - VI (DDD) — l’incrément reste interne à `capture-recognition`.
- **Alternatives considérées**:
  - Aucune — aucun signal inter-domaine n’est porté par le libellé ou le placement.

## Décisions agrégées

| ID | Décision | Statut |
|---|---|---|
| R-001 | Bande d’action sous l’aperçu, hors `Box` preview | ✅ |
| R-002 | Renommage libellé en littéral (suivi : string resource) | ✅ |
| R-003 | Espacement via `HomeSpacingRules.standardFixedSpacing` | ✅ |
| R-004 | 3 tests Compose UI (1 placement + 2 libellé) | ✅ |
| R-005 | Pas de changement inter-domaines | ✅ |
| R-006 | Hauteur d’aperçu adaptative (`weight(1f) + heightIn 200..480 dp`) | ✅ (révision post-implémentation) |

Aucune entrée NEEDS CLARIFICATION résiduelle.
