package com.miamia.data.repository

import com.miamia.data.db.ScanSessionDao
import com.miamia.data.db.ScanSessionEntity

class ScanSessionRepository(
    private val dao: ScanSessionDao
) {
    suspend fun startSession(sessionId: String, startedAt: Long) {
        dao.upsert(
            ScanSessionEntity(
                sessionId = sessionId,
                startedAt = startedAt,
                status = "capturing"
            )
        )
    }

    suspend fun completeSession(
        sessionId: String,
        startedAt: Long,
        finishedAt: Long,
        status: String,
        tempImageDeleted: Boolean,
        errorCode: String? = null,
        fingerprint: String? = null
    ) {
        dao.upsert(
            ScanSessionEntity(
                sessionId = sessionId,
                startedAt = startedAt,
                finishedAt = finishedAt,
                status = status,
                analysisDurationMs = finishedAt - startedAt,
                tempImageDeleted = tempImageDeleted,
                errorCode = errorCode,
                imageFingerprintSha256 = fingerprint
            )
        )
    }
}
