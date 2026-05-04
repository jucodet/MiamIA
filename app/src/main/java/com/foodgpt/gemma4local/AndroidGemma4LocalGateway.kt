package com.foodgpt.gemma4local

import android.content.Context
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Point d'integration local Gemma4 via runtime LLM on-device (LiteRT-LM).
 * Cette implementation evite toute dependance a un endpoint HTTP local.
 */
class AndroidGemma4LocalGateway(
    private val context: Context,
    private val modelLocator: GemmaModelLocator = GemmaModelLocator(context)
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    override suspend fun analyzeText(inputText: String): String {
        return withContext(Dispatchers.IO) {
            val future = CompletableFuture.supplyAsync({
                runAnalyze(inputText)
            }, inferenceExecutor)
            try {
                future.get(Gemma4LocalConfig.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                future.cancel(true)
                throw e
            } catch (e: ExecutionException) {
                val cause = e.cause
                if (cause is Exception) {
                    throw cause
                }
                throw IllegalStateException("Execution Gemma4 locale en echec.")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                future.cancel(true)
                throw TimeoutException("Execution Gemma4 interrompue.")
            }
        }
    }

    private fun runAnalyze(inputText: String): String {
        val locatedModel = modelLocator.resolve()
        val modelFile = (locatedModel as? GemmaModelLocation.Ready)?.modelFile
            ?: throw IllegalStateException("Modele Gemma local indisponible")

        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val output = runCatching {
                runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = backend,
                    inputText = inputText
                )
            }.getOrElse { t ->
                backendErrors += "${backend.javaClass.simpleName}:${t.javaClass.simpleName}"
                null
            }
            if (!output.isNullOrBlank()) return output
        }
        throw IllegalStateException(
            "Execution Gemma4 locale en echec sur tous les backends (${backendErrors.joinToString(",")})."
        )
    }

    private fun runAnalyzeOnBackend(
        modelPath: String,
        backend: Backend,
        inputText: String
    ): String {
        val engine = Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                cacheDir = context.cacheDir.absolutePath
            )
        )
        return try {
            engine.initialize()
            val systemInstruction = Contents.of(
                "Tu analyses des listes d'ingredients alimentaires. " +
                    "Reponds uniquement avec les sections dans l'ordre : ###LISTE puis ###ANALYSE puis ###ADDITIFS_RISQUE. " +
                    "Sous ###LISTE : lignes - ingredient. Sous ###ANALYSE : au plus 3 phrases courtes, factuelles. " +
                    "Sous ###ADDITIFS_RISQUE : une ligne par additif au format exact NIVEAU|nom_additif|justification_courte ; " +
                    "NIVEAU dans {VERT, ORANGE, ROUGE, INCERTAIN}. Pas de texte libre hors ces lignes dans cette section."
            )
            val conversationConfig = ConversationConfig(systemInstruction = systemInstruction)
            val prompt = "Texte capture (OCR):\n${inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS)}"
            engine.createConversation(conversationConfig).use { conversation ->
                textFromMessage(conversation.sendMessage(prompt)).trim()
            }
        } finally {
            engine.close()
        }
    }

    private fun prioritizedBackends(): List<Backend> {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        return buildList {
            add(Backend.NPU(nativeLibraryDir = nativeLibDir))
            add(Backend.GPU())
            add(Backend.CPU())
        }
    }

    override suspend fun ping(): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                Backend.CPU()
                true
            }.getOrDefault(false)
        }
    }

    private fun textFromMessage(message: Message): String =
        message.contents.contents
            .asSequence()
            .filterIsInstance<Content.Text>()
            .map { it.text }
            .joinToString("\n")

    companion object {
        private val inferenceExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    }
}
