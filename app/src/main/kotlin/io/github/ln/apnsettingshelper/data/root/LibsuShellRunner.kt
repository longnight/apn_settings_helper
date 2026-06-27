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
            // Collect stdout and stderr into separate lists; libsu doesn't split them otherwise,
            // so a root failure's real reason would be lost (it stays empty by default).
            val out = ArrayList<String>()
            val err = ArrayList<String>()
            val result = Shell.cmd(command).to(out, err).exec()
            ShellResult(success = result.isSuccess, out = out, err = err)
        }
}
