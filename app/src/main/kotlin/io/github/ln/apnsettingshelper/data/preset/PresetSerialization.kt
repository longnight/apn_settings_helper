package io.github.ln.apnsettingshelper.data.preset

import io.github.ln.apnsettingshelper.domain.model.Region
import io.github.ln.apnsettingshelper.domain.model.allPresets
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Thrown when presets.json is malformed or fails validation. */
class PresetException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Pure (Android-free) parsing + validation + grouping of preset data. Testable on
 * the JVM with a raw JSON string; the Android asset loading lives in [PresetRepository].
 */
object PresetSerialization {
    const val SCHEMA_VERSION: Int = 1
    private val MCC_REGEX = Regex("""\d{3}""")
    private val MNC_REGEX = Regex("""\d{2,3}""")

    private val json = Json { ignoreUnknownKeys = true }

    /** Parse JSON into DTOs. Throws [PresetException] on malformed JSON, missing required fields, or out-of-range enums. */
    fun parse(text: String): PresetFileDto =
        try {
            json.decodeFromString<PresetFileDto>(text)
        } catch (e: SerializationException) {
            throw PresetException("malformed presets.json: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw PresetException("malformed presets.json: ${e.message}", e)
        }

    /** Parse, validate, and return the grouped region → carrier → preset structure. */
    fun parseAndValidate(text: String): List<Region> {
        val file = parse(text)
        val regions = file.toDomain()
        validate(file.schemaVersion, regions)
        return regions
    }

    private fun validate(
        schemaVersion: Int,
        regions: List<Region>,
    ) {
        val errors = mutableListOf<String>()

        if (schemaVersion != SCHEMA_VERSION) {
            errors += "unsupported schemaVersion=$schemaVersion (expected $SCHEMA_VERSION)"
        }

        val presets = regions.allPresets()
        if (presets.isEmpty()) errors += "no presets found"

        duplicates(presets.map { it.id }).forEach { errors += "duplicate preset id: $it" }
        duplicates(regions.flatMap { it.carriers }.map { it.id }).forEach { errors += "duplicate carrier id: $it" }
        duplicates(regions.map { it.code }).forEach { errors += "duplicate region code: $it" }

        presets.forEach { p ->
            if (p.apn.isBlank()) errors += "preset '${p.id}': blank apn"
            if (!MCC_REGEX.matches(p.mcc)) errors += "preset '${p.id}': invalid mcc '${p.mcc}' (need 3 digits)"
            if (!MNC_REGEX.matches(p.mnc)) errors += "preset '${p.id}': invalid mnc '${p.mnc}' (need 2-3 digits)"
        }

        if (errors.isNotEmpty()) {
            throw PresetException("invalid presets.json:\n- " + errors.joinToString("\n- "))
        }
    }

    private fun duplicates(ids: List<String>): Set<String> =
        ids
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
}
