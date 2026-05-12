package com.miamia.ingredients

import com.miamia.core.FeatureConfig

class AutoValidationPolicy {
    fun shouldAutoValidate(ocrConfidenceGlobal: Float): Boolean {
        return ocrConfidenceGlobal >= FeatureConfig.AUTO_VALIDATE_OCR_THRESHOLD
    }
}
