plugins {
    id("com.android.application") version "8.12.3" apply false
    // LiteRT-LM 0.10.x est compilé avec métadonnées Kotlin 2.3 — aligner le toolchain.
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
}
