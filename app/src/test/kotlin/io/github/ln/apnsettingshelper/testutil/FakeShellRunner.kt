package io.github.ln.apnsettingshelper.testutil

import io.github.ln.apnsettingshelper.domain.apply.ShellResult
import io.github.ln.apnsettingshelper.domain.apply.ShellRunner

/** Records issued commands and returns canned results, so the root path is testable without su. */
class FakeShellRunner(
    private val rootAvailable: Boolean = true,
    private val resultFor: (String) -> ShellResult = { ShellResult(success = true) },
) : ShellRunner {
    val commands = mutableListOf<String>()

    override suspend fun isRootAvailable(): Boolean = rootAvailable

    override suspend fun run(command: String): ShellResult {
        commands += command
        return resultFor(command)
    }
}
