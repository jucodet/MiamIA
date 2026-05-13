package com.miamia.gemma4local

import android.content.Context
import android.util.Log
import com.miamia.composition.GemmaModelLocation
import com.miamia.composition.GemmaModelLocator
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Point d'integration local Gemma4 via runtime LLM on-device (LiteRT-LM).
 * Cette implementation evite toute dependance a un endpoint HTTP local.
 */
class AndroidGemma4LocalGateway(
    private val context: Context,
    private val modelLocator: GemmaModelLocator = GemmaModelLocator(context)
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    override suspend fun analyzeText(inputText: String): String {
        return withContext(Dispatchers.IO) { runAnalyze(inputText) }
    }

    private fun runAnalyze(inputText: String): String {
        val modelFile = resolveModelFileForInference()

        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val backendName = backendName(backend)
            val startedAt = System.currentTimeMillis()
            val output = runCatching {
                runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = backend,
                    inputText = inputText
                )
            }.getOrElse { t ->
                val elapsedMs = System.currentTimeMillis() - startedAt
                backendErrors += "$backendName:${t.javaClass.simpleName}:${t.message.orEmpty()}"
                Log.e(
                    TAG,
                    "diag_backend_fail backend=$backendName elapsedMs=$elapsedMs throwable=${t::class.java.simpleName} message=${t.message}",
                    t
                )
                null
            }
            if (!output.isNullOrBlank()) {
                val elapsedMs = System.currentTimeMillis() - startedAt
                Log.i(
                    TAG,
                    "diag_backend_success backend=$backendName elapsedMs=$elapsedMs outputChars=${output.length}"
                )
                return output
            }
            Log.w(TAG, "diag_backend_empty backend=$backendName")
        }
        throw IllegalStateException(
            "Execution Gemma4 locale en echec sur tous les backends (${backendErrors.joinToString(",")})."
        )
    }

    private fun resolveModelFileForInference(): java.io.File {
        val locatedModel = modelLocator.resolve()
        val modelFile = (locatedModel as? GemmaModelLocation.Ready)?.modelFile
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        if (!modelFile.isFile || modelFile.length() <= 0L) {
            throw IllegalStateException("Modele Gemma local invalide (fichier absent ou vide).")
        }
        if (!modelFile.name.endsWith(".litertlm", ignoreCase = true)) {
            throw IllegalStateException(
                "Format modele invalide: ${modelFile.name}. Extension .litertlm requise."
            )
        }
        Log.i(
            TAG,
            "diag_model_selected path=${modelFile.absolutePath} bytes=${modelFile.length()} format=.litertlm"
        )
        return modelFile
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
                "Reponds entierement en francais. Tu analyses des listes d'ingredients alimentaires (contexte UE). " +
                    "Ne pas inventer d'ingredients absents du texte source. " +
                    "Pour ###LISTE : un ingredient par ligne avec - ; reformule chaque libelle OCR vers la graphie la plus probable sans changer le sens ni l'ordre des entrees lues. " +
                    "Pas de pourcentages entre parentheses dans les libelles ni dans le champ nom des verdicts (ex. farine de ble sans (50 %)). " +
                    "Pour ###ADDITIFS_RISQUE et ###IMPACT_SANTE : le nom entre les barres = meme intitule normalise que dans ###LISTE pour le meme compose. " +
                    "###IMPACT_SANTE : une ligne par entree ###LISTE meme ordre sans omission. Exemple OCR : omidon -> amidon. " +
                    "Reponds uniquement avec 4 sections dans cet ordre : ###LISTE, ###ANALYSE, ###ADDITIFS_RISQUE, ###IMPACT_SANTE. " +
                    "Exemple : ###LISTE\n- eau\n- sucre\n- E300\n###ANALYSE\nProduit simple. Peu d'additifs.\n" +
                    "###ADDITIFS_RISQUE\nVERT|E300|Vitamine C naturelle\n" +
                    "###IMPACT_SANTE\nVERT|eau|Hydratation essentielle\nORANGE|sucre|Exces lie au surpoids\nVERT|E300|Sans risque aux doses alimentaires\n" +
                    "Regles : ###LISTE un ingredient par ligne avec -. ###ANALYSE 3 phrases max. " +
                    "###ADDITIFS_RISQUE et ###IMPACT_SANTE : chaque ligne commence par VERT ou ORANGE ou ROUGE ou INCERTAIN puis | puis nom puis | puis note courte. " +
                    "Si aucun additif, laisser ###ADDITIFS_RISQUE vide."
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
            val modelFile = resolveModelFileForInference()
            val healthPrompt = "ok"
            val output = runAnalyzeOnBackend(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(),
                inputText = healthPrompt
            )
            val healthy = output.isNotBlank()
            Log.i(
                TAG,
                "diag_health_ping healthy=$healthy outputChars=${output.length} backend=CPU"
            )
            healthy
        }
    }

    private fun backendName(backend: Backend): String = when (backend) {
        is Backend.NPU -> "NPU"
        is Backend.GPU -> "GPU"
        is Backend.CPU -> "CPU"
        else -> backend.javaClass.simpleName
    }

    private fun textFromMessage(message: Message): String =
        message.contents.contents
            .asSequence()
            .filterIsInstance<Content.Text>()
            .map { it.text }
            .joinToString("\n")

    companion object {
        private const val TAG = "Gemma4LocalGateway"
    }
}
