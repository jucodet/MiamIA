package com.miamia.analysis.ingredientsegment

class IngredientSegmentBoundaryResolver {

    data class Resolution(
        val endIndexExclusive: Int,
        val boundaryEndReason: IngredientSegmentBoundaryEndReason
    )

    /**
     * FR-003 (révisé 017): scanne tout le texte depuis l'ancre.
     * `.` termine seulement si suivi d'un espace, `\n`, ou en fin de texte.
     * `!` et `?` restent des terminateurs inconditionnels.
     * FR-005: si aucun terminateur trouvé → fin du texte.
     */
    fun resolveEnd(text: String, anchorIndex: Int): Resolution {
        for (i in anchorIndex until text.length) {
            when (text[i]) {
                '!' , '?' -> return Resolution(
                    endIndexExclusive = i + 1,
                    boundaryEndReason = IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR
                )
                '.' -> {
                    val nextIndex = i + 1
                    if (nextIndex >= text.length || text[nextIndex] == ' ' || text[nextIndex] == '\n') {
                        return Resolution(
                            endIndexExclusive = nextIndex,
                            boundaryEndReason = IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR
                        )
                    }
                }
            }
        }

        return Resolution(
            endIndexExclusive = text.length,
            boundaryEndReason = IngredientSegmentBoundaryEndReason.TEXT_END
        )
    }
}
