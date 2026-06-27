package io.github.ln.apnsettingshelper.domain.apply

/**
 * Picks the best available [ApplyStrategy] (AGENTS.md tier order): ROOT if a root shell is
 * present, else OVERLAY if permitted (v1: never — overlay is off), else MANUAL. This is the
 * one place tier selection lives, so adding/removing a tier is localized here.
 */
class ApplyStrategyResolver(
    private val shellRunner: ShellRunner,
    private val overlayAvailable: () -> Boolean = { false },
) {
    private val manual = ManualCopyStrategy()

    suspend fun resolve(): ApplyStrategy =
        when {
            shellRunner.isRootAvailable() -> RootStrategy(shellRunner)
            overlayAvailable() -> OverlayStrategy()
            else -> manual
        }
}
