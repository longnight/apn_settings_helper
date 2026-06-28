package io.github.ln.apnsettingshelper.ui

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.LocalizedText
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.detail.PresetDetailUiState
import io.github.ln.apnsettingshelper.ui.list.PresetListUiState
import io.github.ln.apnsettingshelper.ui.list.PresetRowUi

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
                    label = "IIJmio (Docomo)",
                    carrier = "IIJmio",
                    subtitle = "Docomo",
                    region = "Japan",
                    isFavorite = true,
                    lastAppliedLabel = "2026-06-27 14:30",
                ),
            ),
        presets =
            listOf(
                PresetRowUi(
                    id = "his-sb",
                    label = "HIS Mobile (SoftBank)",
                    carrier = "HIS Mobile",
                    subtitle = "SoftBank",
                    region = "Japan",
                    isFavorite = false,
                    lastAppliedLabel = null,
                ),
            ),
        regions = listOf("Japan"),
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
