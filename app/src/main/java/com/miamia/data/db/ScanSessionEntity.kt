package com.miamia.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey val sessionId: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val status: String,
    val errorCode: String? = null,
    val analysisDurationMs: Long? = null,
    val tempImageDeleted: Boolean = false,
    val imageFingerprintSha256: String? = null
)
