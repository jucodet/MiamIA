package com.miamia.gemma4local

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.miamia.gemma4local.model.BackendExecution
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Résultat d'une inférence : texte produit + backend matériel réellement utilisé.
 */
data class InferenceOutcome(
    val text: String,
    val backend: BackendExecution
)

/**
 * Gateway LiteRT-LM avec telechargement automatique du modele Gemma 4 E2B
 * depuis HuggingFace. Au premier appel, le modele est telecharge et cache
 * dans filesDir. Les appels suivants reutilisent le cache local.
 */
class HybridGemma4LocalGateway(
    private val context: Context,
    private val downloader: GemmaModelDownloader = GemmaModelDownloader(context)
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    /**
     * Telecharge le modele si absent. Appeler AVANT analyzeText (au demarrage app).
     * Le telechargement peut prendre plusieurs minutes pour ~2.6 GB.
     */
    suspend fun ensureModelDownloaded() {
        downloader.ensureModelAvailable()
    }

    override suspend fun analyzeText(inputText: String): String {
        return analyzeTextStreaming(inputText, null)
    }

    suspend fun analyzeTextStreaming(inputText: String, onPartial: ((String) -> Unit)?): String {
        return analyzeTextStreamingWithBackend(inputText, onPartial).text
    }

    /**
     * Variante exposant le backend réellement utilisé (pour la pastille UI).
     */
    suspend fun analyzeTextWithBackend(inputText: String): InferenceOutcome {
        return analyzeTextStreamingWithBackend(inputText, null)
    }

    suspend fun analyzeTextStreamingWithBackend(
        inputText: String,
        onPartial: ((String) -> Unit)?
    ): InferenceOutcome {
        val modelFile = downloader.resolveLocalModel()
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        val systemInstruction = compositionSystemInstruction()
        val userMessage = "Texte capture (OCR):\n${inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS)}"
        return withContext(Dispatchers.IO) {
            runInferenceLoop(modelFile, systemInstruction, userMessage, onPartial)
        }
    }

    /**
     * Inférence générique avec system instruction + user message fournis par l'appelant
     * (ex. critique santé). Réutilise exactement le même chemin LiteRT-LM que la composition
     * (boucle backends NPU→GPU→CPU, engine fermé en finally, streaming via sendMessageAsync).
     */
    suspend fun inferStreaming(
        systemInstruction: String,
        userMessage: String,
        onPartial: ((String) -> Unit)?
    ): String {
        val modelFile = downloader.resolveLocalModel()
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        return withContext(Dispatchers.IO) {
            runInferenceLoop(modelFile, systemInstruction, userMessage, onPartial).text
        }
    }

    override suspend fun ping(): Boolean {
        return runCatching {
            val modelFile = downloader.resolveLocalModel() ?: return false
            withContext(Dispatchers.IO) {
                val output = runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    systemInstruction = "Reponds en francais.",
                    userMessage = "ok",
                    onPartial = null,
                )
                val healthy = output.isNotBlank()
                Log.i(TAG, "health_ping healthy=$healthy backend=CPU")
                healthy
            }
        }.onFailure { t ->
            Log.w(TAG, "health_ping_failed ${t::class.java.simpleName}: ${t.message}")
        }.getOrDefault(false)
    }

    private suspend fun runInferenceLoop(
        modelFile: File,
        systemInstruction: String,
        userMessage: String,
        onPartial: ((String) -> Unit)?
    ): InferenceOutcome {
        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val name = backendName(backend)
            val started = System.currentTimeMillis()
            // On distingue l'annulation cooperative (timeout caller via withTimeout) d'un échec
            // backend : runCatching avalait la CancellationException et enchainait sur le backend
            // suivant au lieu de propager l'annulation — le withTimeout devenait inoperant.
            val output = try {
                runAnalyzeOnBackend(modelFile.absolutePath, backend, systemInstruction, userMessage, onPartial)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                val elapsed = System.currentTimeMillis() - started
                backendErrors += "$name:${t.javaClass.simpleName}:${t.message.orEmpty()}"
                Log.e(TAG, "backend_fail $name ${elapsed}ms ${t::class.java.simpleName}: ${t.message}", t)
                null
            }
            if (!output.isNullOrBlank()) {
                Log.i(TAG, "backend_success $name ${System.currentTimeMillis() - started}ms chars=${output.length}")
                return InferenceOutcome(output, BackendExecution.from(backend))
            }
        }
        throw IllegalStateException(
            "Inference Gemma echouee sur tous les backends (${backendErrors.joinToString(",")})"
        )
    }

    private suspend fun runAnalyzeOnBackend(
        modelPath: String,
        backend: Backend,
        systemInstruction: String,
        userMessage: String,
        onPartial: ((String) -> Unit)?
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
            val conversationConfig = ConversationConfig(systemInstruction = Contents.of(systemInstruction))
            engine.createConversation(conversationConfig).use { conversation ->
                if (onPartial != null) {
                    collectStreaming(conversation, userMessage, onPartial)
                } else {
                    textFromMessage(conversation.sendMessage(userMessage)).trim()
                }
            }
        } finally {
            engine.close()
        }
    }

    private fun compositionSystemInstruction(): String =
        "Reponds entierement en francais. " +
            "Tu analyses des listes d'ingredients alimentaires (contexte UE). " +
            "Ne pas inventer d'ingredients absents du texte source. " +
            "Pour ###LISTE : un ingredient par ligne avec - ; reformule chaque libelle OCR vers la graphie la plus probable (orthographe, accents, formulation UE) sans changer le sens ni l'ordre des entrees reellement lues. " +
            "Sans pourcentages entre parentheses dans les libelles (ex. farine de ble sans (50 %) ; meme regle pour le champ nom des verdicts). " +
            "Corriger polmiste -> palmiste. Si farine(s) de X et de Y sur une ligne, deux lignes - farine de X et - farine de Y. " +
            "Enlever une parenthese fermante en trop en fin de libelle si elle n'a pas de ( ouvrante (ex. huile de colza) devient huile de colza sans le dernier ). " +
            "Pour ###ADDITIFS_RISQUE et ###IMPACT_SANTE : le champ nom entre les barres doit reprendre le meme intitule normalise que dans ###LISTE pour le meme compose. " +
            "###IMPACT_SANTE : exactement une ligne par entree de ###LISTE, meme ordre, aucune omission. " +
            "Exemple correction OCR : omidon -> amidon. " +
            "Reponds uniquement avec 6 sections dans cet ordre : ###LISTE, ###PRODUIT, ###ANALYSE, ###ENERGIE_ESTIMEE, ###ADDITIFS_RISQUE, ###IMPACT_SANTE. " +
            "Exemple : ###LISTE\n- eau\n- sucre\n- E300\n###PRODUIT\nLimonade ou soda sucre|80\n###ANALYSE\nProduit simple. Peu d'additifs.\n" +
            "###ENERGIE_ESTIMEE\n38\n" +
            "###ADDITIFS_RISQUE\nVERT|E300|Vitamine C naturelle\n" +
            "###IMPACT_SANTE\nVERT|eau|Hydratation essentielle\nORANGE|sucre|Exces lie au surpoids\nVERT|E300|Sans risque aux doses alimentaires\n" +
            "Regles : ###LISTE un ingredient par ligne avec -. " +
            "###PRODUIT une seule ligne, format nom_du_produit|pourcentage_certitude (0-100). Le produit alimentaire le plus probable auquel ces ingredients appartiennent. " +
            "###ANALYSE 3 phrases max, factuelles, prudentes. " +
            "###ENERGIE_ESTIMEE une seule ligne : entier kcal pour 100 g (estimation indicative depuis la liste) ou NA si non fiable. " +
            "###ADDITIFS_RISQUE et ###IMPACT_SANTE : chaque ligne commence par VERT ou ORANGE ou ROUGE ou INCERTAIN puis | puis nom puis | puis note courte. " +
            "Si aucun additif, laisser ###ADDITIFS_RISQUE vide."

    /**
     * Collecte le flux de streaming de maniere cooperative (suspend) pour que le `withTimeout`
     * de l'appelant puisse interrompre rellement la collecte. L'anti-pattern `runBlocking`
     * bloquait le thread IO : l'annulation du timeout n'etait effective qu'au retour de
     * `runBlocking`, c'est-a-dire trop tard (apres la fin de l'inférence). Le `engine.close()`
     * du `finally` de [runAnalyzeOnBackend] s'execute a l'annulation et stoppe la generation native.
     */
    private suspend fun collectStreaming(
        conversation: Conversation,
        prompt: String,
        onPartial: (String) -> Unit
    ): String {
        var reconciled = ""
        conversation.sendMessageAsync(prompt)
            .catch { e ->
                Log.w(TAG, "stream_error ${e::class.java.simpleName}: ${e.message}")
                throw e
            }
            .collect { chunk ->
                val piece = textFromMessage(chunk)
                if (piece.isEmpty()) return@collect
                reconciled = if (piece.startsWith(reconciled)) piece else reconciled + piece
                onPartial(reconciled)
            }
        return reconciled.trim()
    }

    private fun prioritizedBackends(): List<Backend> {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        return buildList {
            add(Backend.NPU(nativeLibraryDir = nativeLibDir))
            add(Backend.GPU())
            add(Backend.CPU())
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
        private const val TAG = "HybridGemma4Gateway"
    }
}
