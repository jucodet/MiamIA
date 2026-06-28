package com.miamia.profile

import com.miamia.healthcritique.UserProfile
import com.miamia.healthcritique.UserProfileProvider

/**
 * Contrat UGE (Feature I, 2026-06-28) : étend le contrat de consommation
 * [UserProfileProvider] publié par `ingredient-health-intelligence` (Feature N)
 * avec la capacité de **persister** le profil sélectionné sur l'écran de capture.
 *
 * UGE fournit l'implémentation persistée ([PersistentUserProfileProvider]) ;
 * IHI ne consomme que la partie lecture ([UserProfileProvider]).
 *
 * L'instance de ce contrat est partagée entre la capture (écriture) et la
 * critique santé (lecture) afin que la critique soit ciblée pour le profil
 * sélectionné (UGE-I-FR-007 / UGE-I-FR-009).
 */
interface MutableUserProfileProvider : UserProfileProvider {
    /**
     * Persiste le [profile] sélectionné sur l'écran de capture (UGE-I-FR-004).
     * Idempotent. Un profil inconnu est ignoré (garde-fou défensif).
     */
    fun setProfile(profile: UserProfile)
}
