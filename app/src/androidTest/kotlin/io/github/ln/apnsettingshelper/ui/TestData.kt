package io.github.ln.apnsettingshelper.ui

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.LocalizedText
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.detail.PresetDetailUiState
import io.github.ln.apnsettingshelper.ui.list.CarrierSectionUi
import io.github.ln.apnsettingshelper.ui.list.PresetListUiState
import io.github.ln.apnsettingshelper.ui.list.PresetRowUi
import io.github.ln.apnsettingshelper.ui.list.RegionSectionUi

const val TEST_APN = "test.apn.jp"

fun testPreset(): Preset =
    Preset(
        id = "his-d",
        label = LocalizedText("HIS Docomo", "HISドコモ"),
        apn = TEST_APN,
        username = "his@his",
        password = "his",
        mcc = "440",
        mnc = "10",
        authType = AuthType.PAP_OR_CHAP,
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
        notes = LocalizedText("", ""),
        source = "test",
        lastVerified = "2026-06-27",
    )

fun testListState(): PresetListUiState =
    PresetListUiState(
        loading = false,
        favorites =
            listOf(
                PresetRowUi(
                    id = "fav-preset",
                    label = "Fav Preset",
                    carrier = "Carrier X",
                    isFavorite = true,
                    lastAppliedLabel = "2026-06-27 14:30",
                ),
            ),
        regions =
            listOf(
                RegionSectionUi(
                    region = "Japan",
                    carriers =
                        listOf(
                            CarrierSectionUi(
                                carrier = "HIS Mobile",
                                rows =
                                    listOf(
                                        PresetRowUi(
                                            id = "his-sb",
                                            label = "HIS SoftBank",
                                            carrier = "HIS Mobile",
                                            isFavorite = false,
                                            lastAppliedLabel = null,
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
    )

fun testDetailState(): PresetDetailUiState =
    PresetDetailUiState(
        loading = false,
        preset = testPreset(),
        title = "HIS Docomo",
        notes = "",
        isFavorite = false,
        lastAppliedLabel = null,
    )
