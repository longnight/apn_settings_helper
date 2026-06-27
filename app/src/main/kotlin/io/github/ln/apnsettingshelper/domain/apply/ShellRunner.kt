package io.github.ln.apnsettingshelper.domain.apply

/** Outcome of one shell command: [success] (exit 0) plus captured [out]/[err] lines. */
data class ShellResult(
    val success: Boolean,
    val out: List<String> = emptyList(),
    val err: List<String> = emptyList(),
)

/**
 * Thin seam over a root shell. Abstracting it (rather than calling libsu directly) keeps
 * [RootStrategy] pure and unit-testable with a fake; the libsu-backed implementation lives
 * in `data.root.LibsuShellRunner`.
 */
interface ShellRunner {
    /** Whether a root shell is actually available (e.g. libsu `Shell.getShell().isRoot`). */
    suspend fun isRootAvailable(): Boolean

    /** Run [command] in the root shell. */
    suspend fun run(command: String): ShellResult
}
