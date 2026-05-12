package com.miamia.ingredients

import com.miamia.recognition.IngredientRecognitionItem

class ExtractedIngredientMapper {
    fun toUi(items: List<IngredientRecognitionItem>): List<IngredientUiItem> {
        return items.map {
            IngredientUiItem(
                position = it.position,
                text = it.normalizedText,
                confidence = it.confidence
            )
        }
    }
}
