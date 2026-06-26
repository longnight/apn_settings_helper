package io.github.ln.apnsettingshelper.data.preset

import android.content.res.AssetManager
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.domain.model.Region
import io.github.ln.apnsettingshelper.domain.model.findPreset

/** Source of bundled APN presets, grouped region → carrier → preset. */
interface PresetRepository {
    /** Load + validate the grouped presets. Throws [PresetException] if the data is invalid. */
    fun loadRegions(): List<Region>

    /** Convenience lookup by preset id across all regions/carriers. */
    fun findPreset(presetId: String): Preset? = loadRegions().findPreset(presetId)
}

/**
 * Loads presets from the app's `assets/` (default: `presets.json`). The heavy lifting
 * (parse/validate/group) is in [PresetSerialization]; this only reads the asset bytes.
 */
class AssetPresetRepository(
    private val assets: AssetManager,
    private val fileName: String = DEFAULT_FILE,
) : PresetRepository {
    override fun loadRegions(): List<Region> {
        val text =
            try {
                assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: java.io.IOException) {
                throw PresetException("could not read asset '$fileName'", e)
            }
        return PresetSerialization.parseAndValidate(text)
    }

    companion object {
        const val DEFAULT_FILE: String = "presets.json"
    }
}
