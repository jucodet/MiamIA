package com.foodgpt.additives

/** Niveau de risque affiché + ordre de tri (HIGH en premier). */
enum class AdditiveRiskLevel(val sortKey: Int) {
    HIGH(0),
    MEDIUM(1),
    LOW(2),
    UNKNOWN(3),
}
