package com.miamia.ingredients

data class IngredientUiItem(
    val position: Int,
    val text: String,
    val confidence: Float
)

data class IngredientListState(
    val isLoading: Boolean = false,
    val message: String = "",
    val items: List<IngredientUiItem> = emptyList(),
    val canRetry: Boolean = false
)
