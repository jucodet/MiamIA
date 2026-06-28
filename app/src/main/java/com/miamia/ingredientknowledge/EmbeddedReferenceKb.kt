package com.miamia.ingredientknowledge

import android.content.Context
import com.miamia.ingredientknowledge.dto.AdditiveDto
import com.miamia.ingredientknowledge.dto.AllergenDto
import com.miamia.ingredientknowledge.dto.KbVersionDto
import kotlinx.serialization.json.Json

/**
 * Erreur domaine explicite : base référence embarquée absente ou illisible (IKB-A-FR-010).
 * Aucun contexte n'est inventé en cas d'échec de chargement.
 */
class IngredientKbLoadException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * Implémentation [ReferenceKb] de production : parse les assets embarqués
 * (`app/src/main/assets/ingredientkb/`) via kotlinx.serialization et construit l'index in-memory
 * (clé primaire E-number + alias de recherche, IKB-A-FR-012).
 *
 * Offline intégral (aucune dépendance réseau au P1, IKB-A-FR-001).
 */
class EmbeddedReferenceKb private constructor(
    private val baseVersion: String,
    val additives: List<AdditiveFactCard>,
    val allergens: List<AllergenFactCard>,
) : ReferenceKb {

    override fun lookup(designations: List<IngredientDesignation>): LookupOutcome =
        IngredientKbLookup.match(
            designations = designations,
            additives = additives,
            allergens = allergens,
            baseVersion = baseVersion,
        )

    override fun baseVersion(): String = baseVersion

    companion object {
        private const val ASSET_DIR = "ingredientkb"
        private const val ADDITIVES_FILE = "additives.json"
        private const val ALLERGENS_FILE = "allergens.json"
        private const val VERSION_FILE = "kb-version.json"

        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Charge la base référence depuis les assets Android.
         * @throws IngredientKbLoadException si un asset est absent ou illisible (IKB-A-FR-010).
         */
        fun load(context: Context): EmbeddedReferenceKb {
            val version = parseVersion(readAsset(context, VERSION_FILE))
            val baseVersion = version.version
            val additivesSource = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion, version.additivesSource)
            val allergensSource = KbSource(KbSource.Origin.EU_ALLERGEN_LIST, baseVersion, version.allergensSource)

            val additiveDtos = json.decodeFromString<List<AdditiveDto>>(readAsset(context, ADDITIVES_FILE))
            val allergenDtos = json.decodeFromString<List<AllergenDto>>(readAsset(context, ALLERGENS_FILE))

            val additives = additiveDtos.map { dto ->
                AdditiveFactCard(
                    eNumber = dto.eNumber,
                    canonicalName = dto.canonicalName,
                    aliases = dto.aliases,
                    role = dto.role,
                    riskLevel = parseRiskLevel(dto.riskLevel, dto.eNumber),
                    source = additivesSource,
                )
            }
            val allergens = allergenDtos.map { dto ->
                AllergenFactCard(
                    id = dto.id,
                    regulatoryName = dto.regulatoryName,
                    aliases = dto.aliases,
                    source = allergensSource,
                )
            }
            return EmbeddedReferenceKb(baseVersion, additives, allergens)
        }

        private fun readAsset(context: Context, name: String): String =
            try {
                context.assets.open("$ASSET_DIR/$name").bufferedReader().use { it.readText() }
            } catch (e: Throwable) {
                throw IngredientKbLoadException("Base référence illisible ou absente: $name", e)
            }

        private fun parseVersion(raw: String): KbVersionDto =
            try {
                json.decodeFromString(KbVersionDto.serializer(), raw)
            } catch (e: Throwable) {
                throw IngredientKbLoadException("Version de base illisible: $VERSION_FILE", e)
            }

        private fun parseRiskLevel(raw: String, eNumber: String): RiskLevel =
            runCatching { RiskLevel.valueOf(raw.trim().uppercase()) }
                .getOrElse {
                    throw IngredientKbLoadException("Niveau de risque invalide pour $eNumber: '$raw'")
                }
    }
}
