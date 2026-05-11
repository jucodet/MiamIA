package com.foodgpt.gemma4local

import android.content.Context
import android.util.Log
import com.foodgpt.composition.GemmaModelPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Telecharge le modele Gemma 4 E2B au format .litertlm depuis HuggingFace
 * et le cache dans [Context.getFilesDir]/gemma/ pour reutilisation.
 */
class GemmaModelDownloader(private val context: Context) {

    fun resolveLocalModel(): File? {
        val target = targetFile()
        return if (target.isFile && target.length() > 0L) target else null
    }

    suspend fun ensureModelAvailable(): File {
        resolveLocalModel()?.let { return it }
        return downloadModel()
    }

    private suspend fun downloadModel(): File = withContext(Dispatchers.IO) {
        val target = targetFile()
        val temp = File(target.parentFile, "${target.name}.downloading")
        target.parentFile?.mkdirs()

        Log.i(TAG, "download_start url=$MODEL_URL")
        val url = URL(MODEL_URL)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 30_000
            conn.readTimeout = 60_000
            conn.instanceFollowRedirects = true
            conn.connect()

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException(
                    "Telechargement echoue (HTTP $responseCode)"
                )
            }

            val totalBytes = conn.contentLengthLong
            var downloaded = 0L
            var lastLogPercent = -1

            conn.inputStream.buffered().use { input ->
                temp.outputStream().buffered().use { output ->
                    val buffer = ByteArray(131_072)
                    while (true) {
                        val n = input.read(buffer)
                        if (n == -1) break
                        output.write(buffer, 0, n)
                        downloaded += n
                        if (totalBytes > 0) {
                            val percent = (downloaded * 100 / totalBytes).toInt()
                            if (percent != lastLogPercent && percent % 10 == 0) {
                                lastLogPercent = percent
                                Log.i(TAG, "download_progress $percent% ($downloaded/$totalBytes)")
                            }
                        }
                    }
                    output.flush()
                    output.fd.sync()
                }
            }

            if (temp.length() <= 0L) {
                temp.delete()
                throw IllegalStateException("Fichier telecharge vide.")
            }

            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }

            Log.i(TAG, "download_complete path=${target.absolutePath} bytes=${target.length()}")
            target
        } finally {
            conn.disconnect()
            if (temp.exists()) temp.delete()
        }
    }

    private fun targetFile(): File {
        val outDir = File(context.filesDir, GemmaModelPaths.ASSET_DIRECTORY).apply { mkdirs() }
        return File(outDir, GemmaModelPaths.EXPECTED_MODEL_FILENAME)
    }

    companion object {
        private const val TAG = "GemmaModelDownloader"
        private const val MODEL_URL =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    }
}
