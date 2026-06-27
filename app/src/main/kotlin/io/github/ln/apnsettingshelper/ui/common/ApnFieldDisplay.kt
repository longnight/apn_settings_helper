package io.github.ln.apnsettingshelper.ui.common

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.MvnoType

/**
 * Human-facing labels for the dropdown enum values, matching the wording the AOSP system
 * APN editor shows in its spinners — so the "set to X" checklist tells the user exactly
 * which option to pick. Technical terms; not localized.
 */
fun AuthType.displayName(): String =
    when (this) {
        AuthType.NONE -> "None"
        AuthType.PAP -> "PAP"
        AuthType.CHAP -> "CHAP"
        AuthType.PAP_OR_CHAP -> "PAP or CHAP"
    }

fun ApnProtocol.displayName(): String =
    when (this) {
        ApnProtocol.IPV4 -> "IPv4"
        ApnProtocol.IPV6 -> "IPv6"
        ApnProtocol.IPV4V6 -> "IPv4/IPv6"
    }

fun MvnoType.displayName(): String =
    when (this) {
        MvnoType.NONE -> "None"
        MvnoType.SPN -> "SPN"
        MvnoType.IMSI -> "IMSI"
        MvnoType.GID -> "GID"
    }
