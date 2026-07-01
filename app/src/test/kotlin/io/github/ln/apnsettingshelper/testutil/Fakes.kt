package io.github.ln.apnsettingshelper.testutil

import io.github.ln.apnsettingshelper.data.preset.PresetRepository
import io.github.ln.apnsettingshelper.data.store.SettingsStore
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import io.github.ln.apnsettingshelper.domain.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PresetRepository] returning fixed regions. */
class FakePresetRepository(
    private val regions: List<Region>,
) : PresetRepository {
    override fun loadRegions(): List<Region> = regions
}

/** In-memory [SettingsStore] backed by [MutableStateFlow]s; [now] stamps [recordApplied]. */
class FakeSettingsStore(
    initialFavorites: Set<String> = emptySet(),
    initialLastApplied: LastApplied? = null,
    private val now: () -> Long = { 0L },
) : SettingsStore {
    private val favoritesState = MutableStateFlow(initialFavorites)
    private val lastAppliedState = MutableStateFlow(initialLastApplied)
    private val languageState = MutableStateFlow<String?>(null)

    override val favorites: Flow<Set<String>> = favoritesState
    override val lastApplied: Flow<LastApplied?> = lastAppliedState
    override val language: Flow<String?> = languageState

    override suspend fun setFavorite(
        presetId: String,
        favorite: Boolean,
    ) {
        favoritesState.value =
            if (favorite) favoritesState.value + presetId else favoritesState.value - presetId
    }

    override suspend fun toggleFavorite(presetId: String) {
        val current = favoritesState.value
        favoritesState.value = if (presetId in current) current - presetId else current + presetId
    }

    override suspend fun recordApplied(presetId: String) {
        lastAppliedState.value = LastApplied(presetId, now())
    }

    override suspend fun setLanguage(tag: String?) {
        languageState.value = tag?.takeUnless { it.isBlank() }
    }
}
