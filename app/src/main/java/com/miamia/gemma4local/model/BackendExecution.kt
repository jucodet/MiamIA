package com.miamia.gemma4local.model

import com.google.ai.edge.litertlm.Backend

/**
 * Backend matériel ayant réellement exécuté une inférence Gemma locale.
 *
 * Donnée technique constatée par le runtime (jamais choisie ni simulée par la présentation).
 * `INDETERMINATE` couvre les cas : backend non reporté, backend inconnu (hors NPU/GPU/CPU),
 * ou inférence échouée avant exécution.
 */
enum class BackendExecution {
    NPU,
    GPU,
    CPU,
    INDETERMINATE;

    val label: String
        get() = when (this) {
            NPU -> "NPU"
            GPU -> "GPU"
            CPU -> "CPU"
            INDETERMINATE -> "—"
        }

    companion object {
        /**
         * Mapping propriétaire runtime : convertit un backend LiteRT-LM constaté
         * en `BackendExecution`. Tout backend non reconnu tombe sur `INDETERMINATE`.
         */
        fun from(backend: Backend?): BackendExecution = when (backend) {
            is Backend.NPU -> NPU
            is Backend.GPU -> GPU
            is Backend.CPU -> CPU
            null -> INDETERMINATE
            else -> INDETERMINATE
        }
    }
}
