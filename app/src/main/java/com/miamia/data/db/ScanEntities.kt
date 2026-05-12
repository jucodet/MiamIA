package com.miamia.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_attempts")
data class ScanAttemptEntity(
    @PrimaryKey val scanId: String,
    val outcome: String,
    val ocrConfidenceGlobal: Float,
    val autoValidated: Boolean,
    val userMessage: String,
    val completedAt: Long
)

@Entity(tableName = "validated_ingredients")
data class ValidatedIngredientEntity(
    @PrimaryKey val scanId: String,
    val finalItemsSerialized: String,
    val editedByUser: Boolean,
    val validatedAt: Long
)
