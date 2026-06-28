package com.miamia.healthcritique

/**
 * Fournit le [UserProfile] courant à la critique santé (Feature N).
 *
 * Frontière DDD : IHI **consomme** le profil via cette interface ; l'implémentation
 * persistée (Onboarding + « Paramètres / Profil ») est fournie par le domaine
 * `user-guidance-experience` sur ce même contrat (IHI-N-FR-001).
 */
interface UserProfileProvider {
    fun current(): UserProfile
}

/**
 * Implémentation par défaut IHI (fallback) — retourne [UserProfile.DEFAULT] (Adulte)
 * tant qu'aucun provider persisté UGE n'est branché. Settable en mémoire pour les tests
 * (IHI-N-FR-012 / IHI-N-SC-008).
 */
class DefaultUserProfileProvider : UserProfileProvider {

    @Volatile
    var override: UserProfile? = null

    override fun current(): UserProfile = override ?: UserProfile.DEFAULT
}
