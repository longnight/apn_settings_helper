package io.github.ln.apnsettingshelper.domain.model

/**
 * A single APN preset. Field names mirror the system APN editor / telephony
 * `carriers` columns. Dropdown fields ([authType], [protocol], [roamingProtocol],
 * [mvnoType]) are shown as checklist items in the UI; the rest are copyable.
 */
data class Preset(
    val id: String,
    val label: LocalizedText,
    val apn: String,
    val username: String,
    val password: String,
    val mcc: String,
    val mnc: String,
    val authType: AuthType,
    val protocol: ApnProtocol,
    val roamingProtocol: ApnProtocol,
    val mvnoType: MvnoType,
    val mvnoValue: String,
    val apnType: String,
    val proxy: String,
    val port: String,
    val mmsc: String,
    val mmsProxy: String,
    val mmsPort: String,
    val server: String,
    val notes: LocalizedText,
    val source: String,
    val lastVerified: String,
)

/** A carrier (MVNO/brand) grouping one or more presets (e.g. per network line). */
data class Carrier(
    val id: String,
    val name: LocalizedText,
    val presets: List<Preset>,
)

/** A country/region grouping carriers. Top level of the preset data model. */
data class Region(
    val code: String,
    val name: LocalizedText,
    val carriers: List<Carrier>,
)

/** Flatten all presets across regions/carriers. */
fun List<Region>.allPresets(): List<Preset> = flatMap { region -> region.carriers }.flatMap { it.presets }

/** Find a preset by id across all regions/carriers, or null. */
fun List<Region>.findPreset(presetId: String): Preset? = allPresets().firstOrNull { it.id == presetId }
