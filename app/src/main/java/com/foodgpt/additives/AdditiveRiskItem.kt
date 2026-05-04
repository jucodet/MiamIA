package com.foodgpt.additives

data class AdditiveRiskItem(
    val canonicalName: String,
    val displayName: String,
    val level: AdditiveRiskLevel,
    val justification: String,
    val confidence: AdditiveLineConfidence,
)
