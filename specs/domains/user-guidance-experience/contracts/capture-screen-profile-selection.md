# Contract : Sélection du profil utilisateur sur l'écran de capture (Feature I)

**Domaine** : `user-guidance-experience`
**Feature** : I — Sélection du profil sur l'écran de capture (défaut Adulte, requise avant photo)
**Date** : 2026-06-28
**Statut** : Implémenté

## Objet

Définir le contrat UI de la sélection du profil sur l'écran de capture, et le contrat de publication du profil sélectionné vers `ingredient-health-intelligence` (Feature N) via `UserProfileProvider`.

## Published Language consommé (depuis IHI — Feature N)

- `com.miamia.healthcritique.UserProfile` (enum) — 5 profils, `DEFAULT = ADULTE`.
- `com.miamia.healthcritique.UserProfileProvider` — `fun current(): UserProfile`.

UGE **ne redéfinit pas** ces types ; elle les consomme et fournit une implémentation persistée.

## Contrat UGE : `MutableUserProfileProvider`

```kotlin
package com.miamia.profile

interface MutableUserProfileProvider : com.miamia.healthcritique.UserProfileProvider {
    fun setProfile(profile: UserProfile)
}
```

## Impl UGE : `PersistentUserProfileProvider`

- Stockage : `SharedPreferences` (préférences privées), clé `user_profile`.
- `current(): UserProfile` — résout `UserProfile.valueOf(name)` ; **repli `UserProfile.DEFAULT`** si absent/inconnu. Non-null garanti.
- `setProfile(profile)` — écrit `profile.name`.

## Contrat UI (écran de capture)

- **Présence** : un sélecteur de profil est rendu sur l'écran de capture, **en haut** (sous l'indicateur MediaPipe), visible dans tous les états de scan (UGE-I-FR-003, UGE-I-FR-012).
- **Valeurs proposées** : exactement les 5 profils de `UserProfile` : `Femme enceinte`, `Enfant`, `Agé`, `Adulte`, `Sportif` (via `UserProfile.label`).
- **Défaut** : `UserProfile.DEFAULT` (`Adulte`) au premier lancement (UGE-I-FR-002).
- **Synchronisation** : la valeur affichée reflète `MutableUserProfileProvider.current()` au démarrage ; `selectProfile(profile)` appelle `provider.setProfile(profile)` puis met à jour l'état UI.
- **Modification** : libre, à tout moment avant la capture (UGE-I-FR-004). Aucune confirmation requise (le défaut Adulte autorise la capture immédiate).
- **Gate de capture** : la capture reste activée selon les conditions existantes (`PreviewActive`, pas de scan en cours). Le profil étant toujours valide (défaut + repli), il n'existe pas d'état « capture désactivée faute de profil » accessible (UGE-I-FR-005 satisfait par invariant ; UGE-I-FR-006 branche défensive documentée).
- **Test tags** :
  - `capture_profile_selector` — racine du sélecteur.
  - `capture_profile_option_<name>` — option d'un profil (ex. `capture_profile_option_ADULTE`).

## Contrat de publication (UGE → IHI)

- `MainActivity` crée **une** instance de `PersistentUserProfileProvider` et l'injecte :
  - dans `CameraViewModel` (écriture — sélection sur l'écran de capture),
  - dans `HealthCritiqueViewModel.factory` (lecture — `analyze()` lit `current()` au moment de la critique).
- `HealthCritiqueViewModel.factory(application, userProfileProvider)` — le paramètre `userProfileProvider` est injecté (défaut `DefaultUserProfileProvider()` pour les tests IHI existants).

## Non-régressions

- Le flux composition / OCR / critique n'est pas modifié au-delà de la consommation du profil via le provider partagé.
- Les tests IHI existants (`HealthCritiqueEngineTest`, etc.) restent valides (ils utilisent `DefaultUserProfileProvider` ou passent le profil explicitement).

## Supersession

- Supersede l'hypothèse Feature N (clarify Q5) d'un écran « Paramètres / Profil » distinct : la sélection vit sur l'écran de capture (UGE-I-FR-011).
