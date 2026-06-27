package io.github.ln.apnsettingshelper.testutil

import io.github.ln.apnsettingshelper.domain.apply.ShellResult
import io.github.ln.apnsettingshelper.domain.apply.ShellRunner

/** Records issued commands and returns canned results, so the root path is testable without su. */
class FakeShellRunner(
    private val rootAvailable: Boolean = true,
    // Default: every command succeeds, and `content query` returns a row id so an insert verifies.
    private val resultFor: (String) -> ShellResult = { command ->
        if (command.trimStart().startsWith("content query")) {
            ShellResult(success = true, out = listOf("Row: 0 _id=1, name=test"))
        } else {
            ShellResult(success = true)
        }
    },
) : ShellRunner {
    val commands = mutableListOf<String>()

    override suspend fun isRootAvailable(): Boolean = rootAvailable

    override suspend fun run(command: String): ShellResult {
        commands += command
        return resultFor(command)
    }
}
