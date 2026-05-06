package com.foodgpt.gemma4local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.foodgpt.composition.GemmaModelPaths
import java.io.File
import java.io.FileOutputStream

class GemmaModelImportManager(
    private val context: Context
) {
    private var lastImportErrorMessage: String? = null

    fun importFromUri(uri: Uri, overwriteExisting: Boolean = false): Boolean {
        lastImportErrorMessage = null
        val extensionError = validateModelExtension(uri)
        if (extensionError != null) {
            lastImportErrorMessage = extensionError
            return false
        }
        val target = targetModelFile()
        if (!overwriteExisting && hasLocalModel()) {
            persistModelUri(uri)
            return true
        }
        val temp = File(target.parentFile, "${target.name}.tmp")
        return runCatching {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Impossible de lire le modele selectionne." }
                FileOutputStream(temp).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }
            require(temp.length() > 0L) { "Le fichier modele est vide." }
            if (target.exists() && !target.delete()) {
                throw IllegalStateException("Impossible de remplacer le modele local.")
            }
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            persistModelUri(uri)
            true
        }.getOrElse {
            temp.delete()
            lastImportErrorMessage = it.message ?: "Import du modele impossible."
            false
        }
    }

    fun persistedModelUri(): Uri? =
        prefs().getString(PREF_MODEL_URI, null)?.let(Uri::parse)

    fun hasLocalModel(): Boolean {
        val target = targetModelFile()
        return target.isFile && target.length() > 0L
    }

    fun getLastImportErrorMessage(): String? = lastImportErrorMessage

    private fun persistModelUri(uri: Uri) {
        prefs().edit().putString(PREF_MODEL_URI, uri.toString()).apply()
    }

    private fun prefs() =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun targetModelFile(): File {
        val outDir = File(context.filesDir, GemmaModelPaths.ASSET_DIRECTORY).apply { mkdirs() }
        return File(outDir, GemmaModelPaths.EXPECTED_MODEL_FILENAME)
    }

    private fun validateModelExtension(uri: Uri): String? {
        val fileName = resolveDisplayName(uri) ?: uri.lastPathSegment.orEmpty()
        if (fileName.isBlank()) {
            return "Modele invalide: nom de fichier introuvable. Fichier .litertlm requis."
        }
        if (!fileName.endsWith(".litertlm", ignoreCase = true)) {
            return "Modele invalide: extension non supportee ($fileName). Utilisez un fichier .litertlm."
        }
        return null
    }

    private fun resolveDisplayName(uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
        }.getOrNull()
    }

    companion object {
        private const val PREFS_NAME = "gemma_model_import"
        private const val PREF_MODEL_URI = "persisted_model_uri"
    }
}
