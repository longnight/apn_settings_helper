package io.github.ln.apnsettingshelper.ui.detail

import io.github.ln.apnsettingshelper.R
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.common.displayName

/** A copyable field: a string-resource [labelRes] above the literal [value] to copy. */
internal data class CopyFieldUi(
    val labelRes: Int,
    val value: String,
)

/** A dropdown field rendered as a "set to X" checklist item; [key] tracks its checkbox state. */
internal data class ChecklistFieldUi(
    val key: String,
    val labelRes: Int,
    val value: String,
)

/** Non-blank, non-dropdown fields, in system-APN-editor order. APN/MCC/MNC are always present. */
internal fun copyableFields(preset: Preset): List<CopyFieldUi> =
    buildList {
        add(CopyFieldUi(R.string.field_apn, preset.apn))
        if (preset.username.isNotBlank()) add(CopyFieldUi(R.string.field_username, preset.username))
        if (preset.password.isNotBlank()) add(CopyFieldUi(R.string.field_password, preset.password))
        add(CopyFieldUi(R.string.field_mcc, preset.mcc))
        add(CopyFieldUi(R.string.field_mnc, preset.mnc))
        if (preset.mvnoValue.isNotBlank()) add(CopyFieldUi(R.string.field_mvno_value, preset.mvnoValue))
        if (preset.apnType.isNotBlank()) add(CopyFieldUi(R.string.field_apn_type, preset.apnType))
        if (preset.proxy.isNotBlank()) add(CopyFieldUi(R.string.field_proxy, preset.proxy))
        if (preset.port.isNotBlank()) add(CopyFieldUi(R.string.field_port, preset.port))
        if (preset.mmsc.isNotBlank()) add(CopyFieldUi(R.string.field_mmsc, preset.mmsc))
        if (preset.mmsProxy.isNotBlank()) add(CopyFieldUi(R.string.field_mms_proxy, preset.mmsProxy))
        if (preset.mmsPort.isNotBlank()) add(CopyFieldUi(R.string.field_mms_port, preset.mmsPort))
        if (preset.server.isNotBlank()) add(CopyFieldUi(R.string.field_server, preset.server))
    }

/** The dropdown fields, always shown as "set to X" checklist items. */
internal fun checklistFields(preset: Preset): List<ChecklistFieldUi> =
    listOf(
        ChecklistFieldUi("authType", R.string.field_auth_type, preset.authType.displayName()),
        ChecklistFieldUi("protocol", R.string.field_protocol, preset.protocol.displayName()),
        ChecklistFieldUi("roamingProtocol", R.string.field_roaming_protocol, preset.roamingProtocol.displayName()),
        ChecklistFieldUi("mvnoType", R.string.field_mvno_type, preset.mvnoType.displayName()),
    )
