package com.miamia.home

import com.google.ai.edge.litertlm.Backend

/**
 * Probe technique limitee a la disponibilite de l'API/runtime LLM locale.
 * Ne verifie pas la presence d'un modele Gemma embarque.
 */
class MediaPipeLlmAvailabilityProbe {
    fun isAvailable(): Boolean {
        return runCatching {
            // Si cette ligne echoue (NoClassDefFoundError / linkage), l'API n'est pas exploitable.
            Backend.CPU()
            true
        }.getOrDefault(false)
    }
}
