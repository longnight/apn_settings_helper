package io.github.ln.apnsettingshelper

import android.content.Context
import io.github.ln.apnsettingshelper.data.preset.AssetPresetRepository
import io.github.ln.apnsettingshelper.data.preset.PresetRepository
import io.github.ln.apnsettingshelper.data.root.LibsuShellRunner
import io.github.ln.apnsettingshelper.data.store.DataStoreSettingsStore
import io.github.ln.apnsettingshelper.data.store.SettingsStore
import io.github.ln.apnsettingshelper.domain.apply.ApplyStrategyResolver
import io.github.ln.apnsettingshelper.domain.apply.ShellRunner

/**
 * Tiny manual DI container (no Hilt — FOSS-only, single module). Holds the app-wide
 * singletons; built once by [ApnApplication] and read by the ViewModel factories.
 */
class AppGraph(
    context: Context,
) {
    private val appContext = context.applicationContext

    val presetRepository: PresetRepository by lazy { AssetPresetRepository(appContext.assets) }
    val settingsStore: SettingsStore by lazy { DataStoreSettingsStore.from(appContext) }
    val shellRunner: ShellRunner by lazy { LibsuShellRunner() }
    val applyResolver: ApplyStrategyResolver by lazy { ApplyStrategyResolver(shellRunner) }
}
