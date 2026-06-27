package io.github.ln.apnsettingshelper.data.root

import com.topjohnwu.superuser.Shell
import io.github.ln.apnsettingshelper.domain.apply.ShellResult
import io.github.ln.apnsettingshelper.domain.apply.ShellRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * libsu-backed [ShellRunner]. Shell creation and command execution block, so both run on
 * [Dispatchers.IO]. On a non-rooted device libsu falls back to a non-root shell and
 * [isRootAvailable] returns false (no hang, no crash).
 */
class LibsuShellRunner : ShellRunner {
    override suspend fun isRootAvailable(): Boolean =
        withContext(Dispatchers.IO) {
            Shell.getShell().isRoot
        }

    override suspend fun run(command: String): ShellResult =
        withContext(Dispatchers.IO) {
            val result = Shell.cmd(command).exec()
            ShellResult(success = result.isSuccess, out = result.out, err = result.err)
        }
}
