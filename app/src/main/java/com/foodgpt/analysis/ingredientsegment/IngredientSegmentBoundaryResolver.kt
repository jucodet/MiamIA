package com.foodgpt.analysis.ingredientsegment

class IngredientSegmentBoundaryResolver {

    data class Resolution(
        val endIndexExclusive: Int,
        val boundaryEndReason: IngredientSegmentBoundaryEndReason
    )

    /**
     * FR-003 (révisé 017): `.` termine seulement si suivi d'un espace, `\n`, ou en fin de texte.
     * `!` et `?` restent des terminateurs inconditionnels.
     * FR-004: sinon fin de ligne (avant le prochain saut de ligne).
     * FR-005: sinon fin du texte.
     */
    fun resolveEnd(text: String, anchorIndex: Int): Resolution {
        val lineEnd = text.indexOf('\n', anchorIndex).let { if (it < 0) text.length else it }
        val lineSliceEnd = minOf(lineEnd, text.length)

        for (i in anchorIndex until lineSliceEnd) {
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

        return if (lineEnd >= 0 && lineEnd < text.length) {
            Resolution(
                endIndexExclusive = lineEnd,
                boundaryEndReason = IngredientSegmentBoundaryEndReason.LINE_END
            )
        } else {
            Resolution(
                endIndexExclusive = text.length,
                boundaryEndReason = IngredientSegmentBoundaryEndReason.TEXT_END
            )
        }
    }
}
