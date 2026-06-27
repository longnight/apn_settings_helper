package io.github.ln.apnsettingshelper.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.ln.apnsettingshelper.ApnApplication
import io.github.ln.apnsettingshelper.data.preset.PresetRepository
import io.github.ln.apnsettingshelper.data.store.SettingsStore
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.common.ApnDateFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale

/** One preset row in the list. [lastAppliedLabel] is non-null only on the last-applied preset. */
data class PresetRowUi(
    val id: String,
    val label: String,
    val carrier: String,
    val isFavorite: Boolean,
    val lastAppliedLabel: String?,
)

/** A carrier and its (non-favorited) preset rows. */
data class CarrierSectionUi(
    val carrier: String,
    val rows: List<PresetRowUi>,
)

/** A region and its carriers. */
data class RegionSectionUi(
    val region: String,
    val carriers: List<CarrierSectionUi>,
)

/**
 * List UI state. Favorited presets are floated out into [favorites] (the "★ Favorites"
 * section); [regions] holds the full region → carrier grouping of the remaining presets.
 */
data class PresetListUiState(
    val loading: Boolean = true,
    val favorites: List<PresetRowUi> = emptyList(),
    val regions: List<RegionSectionUi> = emptyList(),
    val error: String? = null,
)

/**
 * Loads the bundled presets once, then reactively combines them with the persisted
 * favorites + last-applied state into a [PresetListUiState].
 */
class PresetListViewModel(
    private val repository: PresetRepository,
    private val settingsStore: SettingsStore,
    private val locale: Locale = Locale.getDefault(),
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val loadResult = runCatching { repository.loadRegions() }
    private val regions = loadResult.getOrDefault(emptyList())
    private val loadError = loadResult.exceptionOrNull()?.message

    val uiState: StateFlow<PresetListUiState> =
        combine(settingsStore.favorites, settingsStore.lastApplied) { favorites, lastApplied ->
            buildState(favorites, lastApplied)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PresetListUiState(loading = true),
        )

    fun toggleFavorite(presetId: String) {
        viewModelScope.launch { settingsStore.toggleFavorite(presetId) }
    }

    private fun buildState(
        favorites: Set<String>,
        lastApplied: LastApplied?,
    ): PresetListUiState {
        val tag = locale.language

        fun rowOf(
            preset: Preset,
            carrier: String,
        ): PresetRowUi =
            PresetRowUi(
                id = preset.id,
                label = preset.label.resolve(tag),
                carrier = carrier,
                isFavorite = preset.id in favorites,
                lastAppliedLabel =
                    lastApplied
                        ?.takeIf { it.presetId == preset.id }
                        ?.let { ApnDateFormat.format(it.epochMillis, locale, zone) },
            )

        val favoriteRows =
            regions
                .flatMap { region -> region.carriers }
                .flatMap { carrier -> carrier.presets.map { carrier to it } }
                .filter { (_, preset) -> preset.id in favorites }
                .map { (carrier, preset) -> rowOf(preset, carrier.name.resolve(tag)) }
                .sortedBy { it.label.lowercase(locale) }

        val regionSections =
            regions.mapNotNull { region ->
                val carrierSections =
                    region.carriers.mapNotNull { carrier ->
                        val rows =
                            carrier.presets
                                .filter { it.id !in favorites }
                                .map { rowOf(it, carrier.name.resolve(tag)) }
                        if (rows.isEmpty()) null else CarrierSectionUi(carrier.name.resolve(tag), rows)
                    }
                if (carrierSections.isEmpty()) null else RegionSectionUi(region.name.resolve(tag), carrierSections)
            }

        return PresetListUiState(
            loading = false,
            favorites = favoriteRows,
            regions = regionSections,
            error = loadError,
        )
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory =
            viewModelFactory {
                initializer {
                    val app = this[APPLICATION_KEY] as ApnApplication
                    PresetListViewModel(app.graph.presetRepository, app.graph.settingsStore)
                }
            }
    }
}
