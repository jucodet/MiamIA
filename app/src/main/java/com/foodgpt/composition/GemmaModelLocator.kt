package com.foodgpt.composition

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

sealed class GemmaModelLocation {
    data class Ready(val modelFile: File) : GemmaModelLocation()
    data object NotFound : GemmaModelLocation()
    data class LoadFailed(val reason: String) : GemmaModelLocation()
}

/**
 * Vérifie la présence du modèle dans `assets/gemma/` et le **matérialise** sur le système de fichiers
 * (LiteRT-LM exige un chemin fichier absolu, pas `asset://`).
 *
 * Si une copie existe déjà avec la **même taille** que l'asset embarqué (via `openFd().declaredLength`,
 * fiable tant que `noCompress` inclut `.litertlm` dans le Gradle), la recopie est **évitée**.
 */
class GemmaModelLocator(private val context: Context) {

    fun resolve(): GemmaModelLocation {
        val assetPath = GemmaModelPaths.expectedAssetPath()
        return try {
            val outDir = File(context.filesDir, GemmaModelPaths.ASSET_DIRECTORY).apply { mkdirs() }
            val outFile = File(outDir, GemmaModelPaths.EXPECTED_MODEL_FILENAME)
            if (outFile.isFile && outFile.length() > 0L) {
                Log.i(
                    TAG,
                    "diag_locator_ready path=${outFile.absolutePath} bytes=${outFile.length()}"
                )
                return GemmaModelLocation.Ready(outFile)
            }
            val assetBytes = assetDeclaredLengthBytes(assetPath)
            val reuseExisting =
                assetBytes != null &&
                    assetBytes > 0L &&
                    outFile.isFile &&
                    outFile.length() == assetBytes
            if (!reuseExisting) {
                context.assets.open(assetPath).use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            if (!outFile.exists() || outFile.length() == 0L) {
                Log.e(TAG, "diag_locator_failed reason=materialize_failed path=${outFile.absolutePath}")
                GemmaModelLocation.LoadFailed("materialize_failed")
            } else if (assetBytes != null && outFile.length() != assetBytes) {
                Log.e(
                    TAG,
                    "diag_locator_failed reason=size_mismatch path=${outFile.absolutePath} expected=$assetBytes actual=${outFile.length()}"
                )
                GemmaModelLocation.LoadFailed("size_mismatch")
            } else {
                Log.i(
                    TAG,
                    "diag_locator_ready path=${outFile.absolutePath} bytes=${outFile.length()}"
                )
                GemmaModelLocation.Ready(outFile)
            }
        } catch (_: FileNotFoundException) {
            Log.e(TAG, "diag_locator_not_found assetPath=$assetPath")
            GemmaModelLocation.NotFound
        } catch (e: Exception) {
            Log.e(TAG, "diag_locator_exception message=${e.message}", e)
            GemmaModelLocation.LoadFailed(e.message ?: "asset_open_failed")
        }
    }

    private fun assetDeclaredLengthBytes(assetPath: String): Long? =
        try {
            context.assets.openFd(assetPath).use { it.declaredLength }
        } catch (_: IOException) {
            null
        }

    companion object {
        private const val TAG = "GemmaModelLocator"
    }
}
