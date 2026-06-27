package io.github.ln.apnsettingshelper.domain.apply

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.MvnoType
import io.github.ln.apnsettingshelper.domain.model.Preset

/**
 * Writes the preset into `content://telephony/carriers` via `su` and selects it as the
 * preferred APN. Pure logic over [ShellRunner] (the libsu wiring is in `LibsuShellRunner`),
 * so the command-building and outcome handling are unit-testable without real root.
 *
 * Non-destructive: it inserts a new row (it does not delete existing APNs). Repeated applies
 * of the same preset can therefore create duplicate rows — acceptable for v1.
 */
class RootStrategy(
    private val shellRunner: ShellRunner,
) : ApplyStrategy {
    override val tier: ApplyTier = ApplyTier.ROOT

    override suspend fun apply(preset: Preset): ApplyOutcome {
        if (!shellRunner.isRootAvailable()) {
            return ApplyOutcome.Failed("Root access is not available")
        }
        val insert = shellRunner.run(buildInsertCommand(preset))
        return if (insert.success) {
            selectPreferredApn(preset)
            ApplyOutcome.Applied(preset.id)
        } else {
            ApplyOutcome.Failed(insert.err.joinToString("\n").ifBlank { "Failed to write the APN" })
        }
    }

    /** Insert one row with the preset's fields. Optional (blank) fields are omitted. */
    private fun buildInsertCommand(preset: Preset): String {
        val strings =
            buildList {
                add("name" to preset.label.en.ifBlank { preset.id })
                add("numeric" to "${preset.mcc}${preset.mnc}")
                add("mcc" to preset.mcc)
                add("mnc" to preset.mnc)
                add("apn" to preset.apn)
                if (preset.username.isNotBlank()) add("user" to preset.username)
                if (preset.password.isNotBlank()) add("password" to preset.password)
                if (preset.proxy.isNotBlank()) add("proxy" to preset.proxy)
                if (preset.port.isNotBlank()) add("port" to preset.port)
                if (preset.mmsc.isNotBlank()) add("mmsc" to preset.mmsc)
                if (preset.mmsProxy.isNotBlank()) add("mmsproxy" to preset.mmsProxy)
                if (preset.mmsPort.isNotBlank()) add("mmsport" to preset.mmsPort)
                if (preset.server.isNotBlank()) add("server" to preset.server)
                if (preset.apnType.isNotBlank()) add("type" to preset.apnType)
                add("protocol" to preset.protocol.toProviderValue())
                add("roaming_protocol" to preset.roamingProtocol.toProviderValue())
                if (preset.mvnoType != MvnoType.NONE) {
                    add("mvno_type" to preset.mvnoType.toProviderValue())
                    if (preset.mvnoValue.isNotBlank()) add("mvno_match_data" to preset.mvnoValue)
                }
            }
        val stringBinds = strings.joinToString(" ") { (column, value) -> "--bind ${quote("$column:s:$value")}" }
        val intBinds =
            listOf(
                "--bind ${quote("authtype:i:${preset.authType.toProviderInt()}")}",
                "--bind ${quote("current:i:$CURRENT_TRUE")}",
            ).joinToString(" ")
        return "content insert --uri $CARRIERS_URI $stringBinds $intBinds"
    }

    /** Best-effort: look up the inserted row's id and mark it the preferred APN. */
    private suspend fun selectPreferredApn(preset: Preset) {
        val where = "apn='${preset.apn}' AND mcc='${preset.mcc}' AND mnc='${preset.mnc}'"
        val query =
            "content query --uri $CARRIERS_URI --projection _id " +
                "--where ${quote(where)} --sort ${quote("_id DESC")}"
        val result = shellRunner.run(query)
        if (!result.success) return
        val id = result.out.firstNotNullOfOrNull { ID_REGEX.find(it)?.groupValues?.get(1) } ?: return
        shellRunner.run("content insert --uri $PREFERAPN_URI --bind ${quote("apn_id:i:$id")}")
    }

    private companion object {
        const val CARRIERS_URI = "content://telephony/carriers"
        const val PREFERAPN_URI = "content://telephony/carriers/preferapn"
        const val CURRENT_TRUE = 1
        val ID_REGEX = Regex("""_id=(\d+)""")

        // Telephony provider auth-type codes.
        const val AUTH_NONE = 0
        const val AUTH_PAP = 1
        const val AUTH_CHAP = 2
        const val AUTH_PAP_OR_CHAP = 3

        fun AuthType.toProviderInt(): Int =
            when (this) {
                AuthType.NONE -> AUTH_NONE
                AuthType.PAP -> AUTH_PAP
                AuthType.CHAP -> AUTH_CHAP
                AuthType.PAP_OR_CHAP -> AUTH_PAP_OR_CHAP
            }

        fun ApnProtocol.toProviderValue(): String =
            when (this) {
                ApnProtocol.IPV4 -> "IP"
                ApnProtocol.IPV6 -> "IPV6"
                ApnProtocol.IPV4V6 -> "IPV4V6"
            }

        fun MvnoType.toProviderValue(): String =
            when (this) {
                MvnoType.NONE -> ""
                MvnoType.SPN -> "spn"
                MvnoType.IMSI -> "imsi"
                MvnoType.GID -> "gid"
            }

        /** POSIX single-quote a token so spaces/parentheses in values survive the shell. */
        fun quote(token: String): String = "'" + token.replace("'", "'\\''") + "'"
    }
}
