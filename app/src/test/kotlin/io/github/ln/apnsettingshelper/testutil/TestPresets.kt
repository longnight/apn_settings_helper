package io.github.ln.apnsettingshelper.testutil

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.Carrier
import io.github.ln.apnsettingshelper.domain.model.LocalizedText
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.domain.model.Region

/** Build a [Preset] with sensible defaults; override only the fields a test cares about. */
@Suppress("LongParameterList")
fun samplePreset(
    id: String,
    labelEn: String = id,
    labelJa: String = id,
    apn: String = "$id.jp",
    username: String = "user@$id",
    mnc: String = "10",
    authType: AuthType = AuthType.PAP_OR_CHAP,
    notesEn: String = "",
    notesJa: String = "",
): Preset =
    Preset(
        id = id,
        label = LocalizedText(labelEn, labelJa),
        apn = apn,
        username = username,
        password = "pass",
        mcc = "440",
        mnc = mnc,
        authType = authType,
        protocol = ApnProtocol.IPV4V6,
        roamingProtocol = ApnProtocol.IPV4V6,
        mvnoType = MvnoType.NONE,
        mvnoValue = "",
        apnType = "default,supl",
        proxy = "",
        port = "",
        mmsc = "",
        mmsProxy = "",
        mmsPort = "",
        server = "",
        notes = LocalizedText(notesEn, notesJa),
        source = "test",
        lastVerified = "2026-06-27",
    )

/** One JP region: HIS Mobile (2 presets) + IIJmio (1 preset). */
fun sampleRegions(): List<Region> =
    listOf(
        Region(
            code = "JP",
            name = LocalizedText("Japan", "日本"),
            carriers =
                listOf(
                    Carrier(
                        id = "his",
                        name = LocalizedText("HIS Mobile", "HISモバイル"),
                        presets =
                            listOf(
                                samplePreset("his-d", labelEn = "HIS Docomo", labelJa = "HISドコモ"),
                                samplePreset("his-sb", labelEn = "HIS SoftBank", labelJa = "HISソフトバンク", mnc = "20"),
                            ),
                    ),
                    Carrier(
                        id = "iij",
                        name = LocalizedText("IIJmio", "IIJmio"),
                        presets = listOf(samplePreset("iij-d", labelEn = "IIJmio", labelJa = "IIJmio")),
                    ),
                ),
        ),
    )
