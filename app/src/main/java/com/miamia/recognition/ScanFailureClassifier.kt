package com.miamia.recognition

class ScanFailureClassifier {
    fun classify(confidence: Float, textLength: Int): String {
        if (textLength == 0) return "empty"
        if (confidence < 0.35f) return "blur"
        if (textLength < 6) return "incomplete"
        return "low-contrast"
    }
}
