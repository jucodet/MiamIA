package com.miamia.profile

import android.content.Context
import android.content.SharedPreferences
import com.miamia.healthcritique.UserProfile

/**
 * Implémentation persistée de [MutableUserProfileProvider] (Feature I, 2026-06-28).
 *
 * Stockage : `SharedPreferences` privées (clé [KEY]), valeur = `UserProfile.name`.
 *
 * Invariants (UGE-I-FR-002 / UGE-I-FR-008, data-model INV-I-1) :
 *  - [current] ne retourne **jamais** `null` ni une valeur hors énumération :
 *    repli sur [UserProfile.DEFAULT] (« Adulte ») si la clé est absente ou la
 *    valeur corrompue/inconnue.
 *  - [setProfile] ignore un profil qui ne ferait pas partie de l'énumération
 *    (garde-fou défensif — la signature typée rend ce cas inaccessible en pratique).
 */
class PersistentUserProfileProvider(
    context: Context,
) : MutableUserProfileProvider {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun current(): UserProfile {
        val name = prefs.getString(KEY, null) ?: return UserProfile.DEFAULT
        return resolveOrNull(name) ?: UserProfile.DEFAULT
    }

    override fun setProfile(profile: UserProfile) {
        prefs.edit().putString(KEY, profile.name).apply()
    }

    private fun resolveOrNull(name: String): UserProfile? =
        UserProfile.values().firstOrNull { it.name == name }

    companion object {
        private const val PREFS_NAME = "miamia_user_profile"
        private const val KEY = "user_profile"
    }
}
