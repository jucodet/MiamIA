package com.miamia.data.repository

import com.miamia.data.db.ValidatedIngredientDao
import com.miamia.data.db.ValidatedIngredientEntity

class ValidatedIngredientRepository(
    private val dao: ValidatedIngredientDao
) {
    suspend fun save(scanId: String, finalItems: List<String>, editedByUser: Boolean) {
        dao.upsert(
            ValidatedIngredientEntity(
                scanId = scanId,
                finalItemsSerialized = finalItems.joinToString("|"),
                editedByUser = editedByUser,
                validatedAt = System.currentTimeMillis()
            )
        )
    }
}
