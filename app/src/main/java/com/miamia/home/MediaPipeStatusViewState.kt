package com.miamia.home

enum class MediaPipeStatusState {
    CHECKING,
    AVAILABLE,
    UNAVAILABLE
}

data class MediaPipeStatusViewState(
    val state: MediaPipeStatusState = MediaPipeStatusState.CHECKING,
    val label: String = "Verification...",
    val colorToken: MediaPipeStatusColorToken = MediaPipeStatusColorToken.NEUTRAL
) {
    companion object {
        fun checking(): MediaPipeStatusViewState = MediaPipeStatusViewState(
            state = MediaPipeStatusState.CHECKING,
            label = "Verification...",
            colorToken = MediaPipeStatusColorToken.NEUTRAL
        )

        fun available(): MediaPipeStatusViewState = MediaPipeStatusViewState(
            state = MediaPipeStatusState.AVAILABLE,
            label = "Détection d'étiquette prête",
            colorToken = MediaPipeStatusColorToken.SUCCESS
        )

        fun unavailable(): MediaPipeStatusViewState = MediaPipeStatusViewState(
            state = MediaPipeStatusState.UNAVAILABLE,
            label = "Indisponible",
            colorToken = MediaPipeStatusColorToken.WARNING_OR_ERROR
        )
    }
}

enum class MediaPipeStatusColorToken {
    NEUTRAL,
    SUCCESS,
    WARNING_OR_ERROR
}
