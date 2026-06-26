package io.github.ln.apnsettingshelper.domain.model

import kotlinx.serialization.Serializable

/**
 * Dropdown-valued APN fields. These render as "set to X" checklist items in the
 * detail UI (not copy buttons), because the system APN editor exposes them as pickers.
 * Serialized by enum name (e.g. "PAP_OR_CHAP"); unknown values fail parsing.
 */

@Serializable
enum class AuthType { NONE, PAP, CHAP, PAP_OR_CHAP }

@Serializable
enum class ApnProtocol { IPV4, IPV6, IPV4V6 }

@Serializable
enum class MvnoType { NONE, SPN, IMSI, GID }
