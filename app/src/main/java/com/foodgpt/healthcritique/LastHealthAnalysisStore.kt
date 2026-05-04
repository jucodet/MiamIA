package com.foodgpt.healthcritique

import android.content.Context

class LastHealthAnalysisStore(
    context: Context,
    private val prefsName: String = "foodgpt_health_critique_last",
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun save(snapshot: LastHealthAnalysisSnapshot) {
        prefs.edit()
            .putLong(KEY_SAVED_AT, snapshot.savedAtEpochMs)
            .putString(KEY_INGREDIENT, snapshot.ingredientRaw)
            .putString(KEY_RESULT, snapshot.resultRaw)
            .putString(KEY_SYSTEM, snapshot.systemPromptSnapshot)
            .apply()
    }

    fun load(): LastHealthAnalysisSnapshot? {
        val at = prefs.getLong(KEY_SAVED_AT, 0L)
        if (at <= 0L) return null
        val ing = prefs.getString(KEY_INGREDIENT, null) ?: return null
        val res = prefs.getString(KEY_RESULT, null) ?: return null
        val sys = prefs.getString(KEY_SYSTEM, "") ?: ""
        return LastHealthAnalysisSnapshot(
            savedAtEpochMs = at,
            ingredientRaw = ing,
            resultRaw = res,
            systemPromptSnapshot = sys,
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_SAVED_AT = "savedAtEpochMs"
        private const val KEY_INGREDIENT = "ingredientRaw"
        private const val KEY_RESULT = "resultRaw"
        private const val KEY_SYSTEM = "systemPromptSnapshot"
    }
}
