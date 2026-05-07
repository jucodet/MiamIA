package com.foodgpt.composition

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
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
        } catch (_: IOException) {
            null
        }

    companion object {
        private const val TAG = "GemmaModelLocator"
    }
}