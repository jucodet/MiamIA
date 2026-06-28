package com.miamia.ingredientknowledge

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Implémentation [KbCacheStore] de production : persistance JSON atomique dans
 * `Context.filesDir/ingredientkb/` (IKB-B-FR-004).
 *
 * Écriture atomique (`.tmp` → rename) pour éviter un cache corrompu en cas d'interruption
 * (principe : cohérent avec le pattern `GemmaModelDownloader`). `read()` renvoie `null` si le
 * cache est absent ou illisible (corrompu) — repli baseline, IKB-B-FR-010. Aucune exception
 * bloquante n'est propagée.
 */
class FileKbCacheStore(
    private val context: Context,
    private val dirName: String = "ingredientkb",
    private val fileName: String = "kb-cache.json",
    private val json: Json = Json { ignoreUnknownKeys = true },
) : KbCacheStore {

    private val dir: File
        get() = File(context.filesDir, dirName).apply { mkdirs() }

    private val cacheFile: File
        get() = File(dir, fileName)

    private val tmpFile: File
        get() = File(dir, "$fileName.tmp")

    override fun read(): KbCache? {
        val file = cacheFile
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(KbCache.serializer(), file.readText())
        }.getOrNull()
    }

    override fun write(cache: KbCache) {
        dir.mkdirs()
        tmpFile.writeText(json.encodeToString(KbCache.serializer(), cache))
        if (cacheFile.exists()) {
            // renameTo peut échouer si la cible existe selon le filesystem → on libère d'abord.
            cacheFile.delete()
        }
        if (!tmpFile.renameTo(cacheFile)) {
            // Fallback non-atomique : recopie le contenu puis supprime le temporaire.
            cacheFile.writeText(tmpFile.readText())
            tmpFile.delete()
        }
    }

    override fun clear() {
        cacheFile.delete()
        tmpFile.delete()
    }
}
