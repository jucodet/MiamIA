package com.miamia.profile

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.miamia.healthcritique.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ATDD — Feature I : `PersistentUserProfileProvider` — défaut « Adulte »,
 * persistance de la sélection, repli « Adulte » sur valeur corrompue/inconnue,
 * et résolution des 5 profils valides (UGE-I-FR-002 / UGE-I-FR-008, INV-I-1).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersistentUserProfileProviderTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun clearPrefs() {
        context.getSharedPreferences("miamia_user_profile", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `current returns ADULTE default when no profile persisted`() {
        val provider = PersistentUserProfileProvider(context)
        assertEquals(UserProfile.DEFAULT, provider.current())
        assertEquals(UserProfile.ADULTE, provider.current())
    }

    @Test
    fun `setProfile then current returns persisted profile`() {
        val provider = PersistentUserProfileProvider(context)
        provider.setProfile(UserProfile.FEMME_ENCEINTE)
        assertEquals(UserProfile.FEMME_ENCEINTE, provider.current())

        provider.setProfile(UserProfile.ENFANT)
        assertEquals(UserProfile.ENFANT, provider.current())
    }

    @Test
    fun `persists across new provider instances (between sessions)`() {
        PersistentUserProfileProvider(context).setProfile(UserProfile.SPORTIF)
        // New instance simulates a fresh app launch reading the same SharedPreferences.
        val relaunched = PersistentUserProfileProvider(context)
        assertEquals(UserProfile.SPORTIF, relaunched.current())
    }

    @Test
    fun `current falls back to ADULTE on corrupted unknown value`() {
        context.getSharedPreferences("miamia_user_profile", Context.MODE_PRIVATE)
            .edit().putString("user_profile", "INCONNU").commit()

        val provider = PersistentUserProfileProvider(context)
        assertEquals(UserProfile.DEFAULT, provider.current())
        assertEquals(UserProfile.ADULTE, provider.current())
    }

    @Test
    fun `all five profiles round-trip through persistence`() {
        val provider = PersistentUserProfileProvider(context)
        UserProfile.values().forEach { profile ->
            provider.setProfile(profile)
            assertEquals(profile, provider.current())
        }
    }

    @Test
    fun `current never returns null - default invariant holds`() {
        val provider = PersistentUserProfileProvider(context)
        // Repeated reads remain valid (non-null by type + default fallback).
        repeat(5) { assertEquals(UserProfile.ADULTE, provider.current()) }
    }
}
