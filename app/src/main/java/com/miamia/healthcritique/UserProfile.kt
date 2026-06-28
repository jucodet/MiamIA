package com.miamia.healthcritique

/**
 * Profil utilisateur consommé par la critique santé (Feature N, 2026-06-28).
 *
 * Contrat de consommation IHI : la saisie/persistance du profil est du ressort du
 * domaine `user-guidance-experience` (Onboarding + écran « Paramètres / Profil ») ;
 * IHI ne fait que consommer le profil sélectionné via [UserProfileProvider].
 *
 * `DEFAULT = ADULTE` : fallback implicite en l'absence de profil sélectionné
 * (IHI-N-FR-012) — la critique est produite pour « Adulte » avec un signal visuel
 * « profil par défaut », sans jamais rétropédalager vers le flux 4-profils.
 *
 * Marqueurs canoniques de sortie (profil unique) — supersèdent le format 4-marqueurs
 * strict Feature L (IHI-L-FR-009 / IHI-L-SC-004, retirés).
 */
enum class UserProfile(
    val label: String,
    val marker: String,
) {
    FEMME_ENCEINTE(label = "Femme enceinte", marker = "###FEMME_ENCEINTE"),
    ENFANT(label = "Enfant", marker = "###ENFANT"),
    PERSONNE_AGEE(label = "Agé", marker = "###PERSONNE_AGEE"),
    ADULTE(label = "Adulte", marker = "###ADULTE"),
    SPORTIF(label = "Sportif", marker = "###SPORTIF"),
    ;

    companion object {
        val DEFAULT: UserProfile = ADULTE

        /** Rappel explicite exigé en tête de sortie (IHI-N-FR-003). */
        fun evaluatedForHeader(profile: UserProfile): String =
            "Évalué pour vous : ${profile.label}"

        /** Marqueurs du format legacy 4-profils (Feature L, retiré) — détectés pour rejet. */
        val legacyFourProfileMarkers: List<String> = listOf(
            "###ENFANTS",
            "###FEMMES_ENCEINTES",
            "###ADULTES",
            "###PERSONNES_AGEES",
        )
    }
}
