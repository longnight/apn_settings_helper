package io.github.ln.apnsettingshelper.data.preset

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.Carrier
import io.github.ln.apnsettingshelper.domain.model.LocalizedText
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.domain.model.Region
import kotlinx.serialization.Serializable

/**
 * Serialization DTOs mirroring assets/presets.json (camelCase). Optional fields
 * carry defaults; fields without a default ([id], [label], [apn], [mcc], [mnc],
 * region/carrier names + lists) are required, so kotlinx.serialization fails parsing
 * if they are absent. Enum fields fail parsing on out-of-range values.
 */
@Serializable
data class PresetFileDto(
    val schemaVersion: Int,
    val regions: List<RegionDto>,
)

@Serializable
data class RegionDto(
    val code: String,
    val name: LocalizedText,
    val carriers: List<CarrierDto>,
)

@Serializable
data class CarrierDto(
    val id: String,
    val name: LocalizedText,
    val presets: List<PresetDto>,
)

@Serializable
data class PresetDto(
    val id: String,
    val label: LocalizedText,
    val line: LocalizedText = LocalizedText("", ""),
    val apn: String,
    val mcc: String,
    val mnc: String,
    val username: String = "",
    val password: String = "",
    val authType: AuthType = AuthType.NONE,
    val protocol: ApnProtocol = ApnProtocol.IPV4V6,
    val roamingProtocol: ApnProtocol = ApnProtocol.IPV4V6,
    val mvnoType: MvnoType = MvnoType.NONE,
    val mvnoValue: String = "",
    val apnType: String = "",
    val proxy: String = "",
    val port: String = "",
    val mmsc: String = "",
    val mmsProxy: String = "",
    val mmsPort: String = "",
    val server: String = "",
    val notes: LocalizedText = LocalizedText("", ""),
    val source: String = "",
    val lastVerified: String = "",
)

internal fun PresetFileDto.toDomain(): List<Region> = regions.map { it.toDomain() }

internal fun RegionDto.toDomain(): Region = Region(code = code, name = name, carriers = carriers.map { it.toDomain() })

internal fun CarrierDto.toDomain(): Carrier = Carrier(id = id, name = name, presets = presets.map { it.toDomain() })

internal fun PresetDto.toDomain(): Preset =
    Preset(
        id = id,
        label = label,
        line = line,
        apn = apn,
        username = username,
        password = password,
        mcc = mcc,
        mnc = mnc,
        authType = authType,
        protocol = protocol,
        roamingProtocol = roamingProtocol,
        mvnoType = mvnoType,
        mvnoValue = mvnoValue,
        apnType = apnType,
        proxy = proxy,
        port = port,
        mmsc = mmsc,
        mmsProxy = mmsProxy,
        mmsPort = mmsPort,
        server = server,
        notes = notes,
        source = source,
        lastVerified = lastVerified,
    )
