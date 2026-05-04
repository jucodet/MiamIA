package com.foodgpt.healthcritique

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.foodgpt.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthCritiqueScreenState(
    val ingredientText: String = "",
    val isLoading: Boolean = false,
    val result: HealthCritiqueResult? = null,
    val restoredSnapshot: LastHealthAnalysisSnapshot? = null,
    val lastSystemPrompt: String = "",
)

class HealthCritiqueViewModel(
    private val engine: HealthCritiqueEngine,
    private val store: LastHealthAnalysisStore,
    private val promptBuilder: HealthCritiquePromptBuilder,
) : ViewModel() {

    private val _ui = MutableStateFlow(HealthCritiqueScreenState())
    val ui: StateFlow<HealthCritiqueScreenState> = _ui.asStateFlow()

    /** Segment validé courant (null = pas encore de bilan prêt côté scan). */
    private var validatedSegmentFromScan: String? = null

    init {
        store.load()?.let { snap ->
            setValidatedSegmentFromScan(snap.ingredientRaw)
            _ui.update { it.copy(restoredSnapshot = snap) }
        }
    }

    fun setValidatedSegmentFromScan(segment: String?) {
        validatedSegmentFromScan = segment
        _ui.update { it.copy(ingredientText = segment.orEmpty()) }
        if (BuildConfig.DEBUG) {
            check(_ui.value.ingredientText == (segment ?: "")) {
                "SC-005 : l’affichage doit refléter exactement le segment synchronisé."
            }
        }
    }

    fun analyze() {
        val segmentSnapshot = validatedSegmentFromScan
        val displaySnapshot = _ui.value.ingredientText
        if (BuildConfig.DEBUG) {
            check(segmentSnapshot == null || displaySnapshot == segmentSnapshot) {
                "SC-005 : buffer affiché et segment validé doivent rester alignés."
            }
        }
        val systemPreview = promptBuilder.buildSystemInstruction()
        _ui.update { it.copy(isLoading = true, result = null, lastSystemPrompt = systemPreview) }
        viewModelScope.launch {
            val outcome = engine.analyze(ingredientText = segmentSnapshot)
            _ui.update { st ->
                st.copy(isLoading = false, result = outcome)
            }
            if (outcome is HealthCritiqueResult.CritiqueReady) {
                val ingredientRaw = segmentSnapshot?.trim().orEmpty()
                val savedAt = System.currentTimeMillis()
                val snap = LastHealthAnalysisSnapshot(
                    savedAtEpochMs = savedAt,
                    ingredientRaw = ingredientRaw,
                    resultRaw = outcome.llmRawText,
                    systemPromptSnapshot = systemPreview,
                )
                store.save(snap)
                _ui.update { st -> st.copy(restoredSnapshot = snap) }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = context.applicationContext
                    val store = LastHealthAnalysisStore(app)
                    val runner = LiteRtHealthCritiqueRunner(app)
                    val promptBuilder = HealthCritiquePromptBuilder()
                    val engine = HealthCritiqueEngine(
                        promptBuilder = promptBuilder,
                        llmRunner = runner,
                    )
                    return HealthCritiqueViewModel(engine, store, promptBuilder) as T
                }
            }
    }
}
