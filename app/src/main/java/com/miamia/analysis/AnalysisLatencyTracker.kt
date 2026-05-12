package com.miamia.analysis

import com.miamia.gemma4local.model.ApiCallMetric
import com.miamia.gemma4local.model.DeviceClass

object AnalysisLatencyTracker {
    private const val MAX_SAMPLES = 200
    private val recentSamples = mutableListOf<Long>()
    private val minCompatSamples = mutableListOf<Long>()

    @Synchronized
    fun record(metric: ApiCallMetric) {
        val target = when (metric.deviceClass) {
            DeviceClass.RECENT -> recentSamples
            DeviceClass.MIN_COMPAT -> minCompatSamples
        }
        target += metric.latencyMs
        if (target.size > MAX_SAMPLES) {
            target.removeAt(0)
        }
    }

    @Synchronized
    fun p95(deviceClass: DeviceClass): Long? {
        val source = when (deviceClass) {
            DeviceClass.RECENT -> recentSamples
            DeviceClass.MIN_COMPAT -> minCompatSamples
        }
        if (source.isEmpty()) return null
        val sorted = source.sorted()
        val idx = ((sorted.size - 1) * 0.95).toInt().coerceIn(0, sorted.lastIndex)
        return sorted[idx]
    }
}
