package com.foodgpt.healthcritique

import android.content.Context
import android.util.Log
import com.foodgpt.BuildConfig
import com.foodgpt.composition.CompositionMessages
import com.foodgpt.composition.GemmaModelLocation
import com.foodgpt.composition.GemmaModelLocator
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Inférence Gemma locale pour la critique santé (spec 002), même pile LiteRT-LM que [com.foodgpt.composition.LiteRtGemmaEngine].
 * L’appel JNI reste bloquant : délai borné via [java.util.concurrent.Future.get] (cf. spec perf).
 */
class LiteRtHealthCritiqueRunner(
    private val context: Context,
    private val locator: GemmaModelLocator = GemmaModelLocator(context),
) : HealthCritiqueLlmRunner {

    private val inferenceLock = Any()
    private var retainedEngine: Engine? = null
    private var retainedModelAbsolutePath: String? = null

    override suspend fun generate(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
    ): HealthCritiqueLlmGenerateResult =
        withContext(Dispatchers.Default) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "health_critique_llm_userMessage_len=${userMessage.length}")
            }
            val deadlineMs = maxInferenceMs.coerceAtLeast(1L)
            when (val located = locator.resolve()) {
                GemmaModelLocation.NotFound ->
                    HealthCritiqueLlmGenerateResult.Failure(
                        HealthInferenceErrorCode.GEMMA_NOT_FOUND,
                        CompositionMessages.GEMMA_NOT_FOUND_USER,
                    )

                is GemmaModelLocation.LoadFailed ->
                    HealthCritiqueLlmGenerateResult.Failure(
                        HealthInferenceErrorCode.GEMMA_LOAD_FAILED,
                        CompositionMessages.GEMMA_LOAD_FAILED_USER,
                    )

                is GemmaModelLocation.Ready ->
                    runWithDeadline(located.modelFile, systemInstruction, userMessage, deadlineMs)
            }
        }

    private suspend fun runWithDeadline(
        modelFile: File,
        systemInstruction: String,
        userMessage: String,
        deadlineMs: Long,
    ): HealthCritiqueLlmGenerateResult {
        val abandonRetainedAfterRun = AtomicBoolean(false)
        return withContext(Dispatchers.IO) {
            val future = java.util.concurrent.CompletableFuture.supplyAsync(
                {
                    synchronized(inferenceLock) {
                        try {
                            runLitertLm(modelFile, systemInstruction, userMessage)
                        } finally {
                            if (abandonRetainedAfterRun.get()) {
                                disposeRetainedLocked()
                            }
                        }
                    }
                },
                inferenceExecutor,
            )
            try {
                future.get(deadlineMs, TimeUnit.MILLISECONDS)
            } catch (_: TimeoutException) {
                abandonRetainedAfterRun.set(true)
                future.cancel(true)
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_TIMEOUT,
                    CompositionMessages.GEMMA_TIMEOUT_USER,
                )
            } catch (e: ExecutionException) {
                val cause = e.cause
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_LOAD_FAILED,
                    if (cause is Exception) {
                        "${CompositionMessages.GEMMA_LOAD_FAILED_USER} (${cause.javaClass.simpleName})"
                    } else {
                        CompositionMessages.GEMMA_LOAD_FAILED_USER
                    },
                )
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                abandonRetainedAfterRun.set(true)
                future.cancel(true)
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_TIMEOUT,
                    CompositionMessages.GEMMA_TIMEOUT_USER,
                )
            }
        }
    }

    private fun disposeRetainedLocked() {
        retainedEngine?.close()
        retainedEngine = null
        retainedModelAbsolutePath = null
    }

    private fun litertLmBackendChain(): List<Backend> {
        val npuDir = context.applicationInfo.nativeLibraryDir
        return buildList {
            add(Backend.NPU(nativeLibraryDir = npuDir))
            add(Backend.GPU())
            add(Backend.CPU())
        }
    }

    private fun runLitertLm(
        modelFile: File,
        systemInstruction: String,
        userMessage: String,
    ): HealthCritiqueLlmGenerateResult {
        val path = modelFile.absolutePath
        if (retainedModelAbsolutePath != null && retainedModelAbsolutePath != path) {
            disposeRetainedLocked()
        }
        val warm = retainedEngine
        if (warm != null && retainedModelAbsolutePath == path) {
            try {
                return runInferenceOnEngine(warm, systemInstruction, userMessage)
            } catch (_: Exception) {
                disposeRetainedLocked()
            }
        }
        var lastError: HealthCritiqueLlmGenerateResult.Failure? = null
        for (backend in litertLmBackendChain()) {
            when (val result = tryLoadAndInfer(modelFile, backend, systemInstruction, userMessage)) {
                is HealthCritiqueLlmGenerateResult.Success -> return result
                is HealthCritiqueLlmGenerateResult.Failure -> lastError = result
            }
        }
        return lastError ?: HealthCritiqueLlmGenerateResult.Failure(
            HealthInferenceErrorCode.GEMMA_LOAD_FAILED,
            CompositionMessages.GEMMA_LOAD_FAILED_USER,
        )
    }

    private fun tryLoadAndInfer(
        modelFile: File,
        backend: Backend,
        systemInstruction: String,
        userMessage: String,
    ): HealthCritiqueLlmGenerateResult {
        val engineConfig = EngineConfig(
            modelPath = modelFile.absolutePath,
            backend = backend,
            cacheDir = context.cacheDir.absolutePath,
        )
        val engine = Engine(engineConfig)
        return try {
            engine.initialize()
            when (val result = runInferenceOnEngine(engine, systemInstruction, userMessage)) {
                is HealthCritiqueLlmGenerateResult.Success -> {
                    disposeRetainedLocked()
                    retainedEngine = engine
                    retainedModelAbsolutePath = modelFile.absolutePath
                    result
                }

                is HealthCritiqueLlmGenerateResult.Failure -> {
                    engine.close()
                    result
                }
            }
        } catch (e: Exception) {
            engine.close()
            HealthCritiqueLlmGenerateResult.Failure(
                HealthInferenceErrorCode.INFERENCE_FAILED,
                "${CompositionMessages.GEMMA_LOAD_FAILED_USER} (${e.javaClass.simpleName})",
            )
        }
    }

    private fun runInferenceOnEngine(
        engine: Engine,
        systemInstruction: String,
        userMessage: String,
    ): HealthCritiqueLlmGenerateResult {
        val conversationConfig = ConversationConfig(
            systemInstruction = Contents.of(systemInstruction),
        )
        val text = try {
            engine.createConversation(conversationConfig).use { conversation ->
                textFromMessage(conversation.sendMessage(userMessage))
            }.trim()
        } catch (e: IllegalStateException) {
            return HealthCritiqueLlmGenerateResult.Failure(
                HealthInferenceErrorCode.INFERENCE_FAILED,
                "${CompositionMessages.GEMMA_LOAD_FAILED_USER} (${e.javaClass.simpleName})",
            )
        }
        return if (text.isEmpty()) {
            HealthCritiqueLlmGenerateResult.Failure(
                HealthInferenceErrorCode.INFERENCE_FAILED,
                "Réponse vide du modèle. Réessayez.",
            )
        } else {
            HealthCritiqueLlmGenerateResult.Success(text)
        }
    }

    private fun textFromMessage(message: Message): String =
        message.contents.contents
            .asSequence()
            .filterIsInstance<Content.Text>()
            .map { it.text }
            .joinToString("\n")

    companion object {
        private const val TAG = "LiteRtHealthCritique"

        private val inferenceExecutor = Executors.newCachedThreadPool(
            object : ThreadFactory {
                private val seq = AtomicInteger(0)
                override fun newThread(r: Runnable): Thread {
                    val t = Thread(r, "foodgpt-health-critique-${seq.incrementAndGet()}")
                    t.isDaemon = true
                    return t
                }
            },
        )
    }
}
