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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale

/**
 * One preset card in the list. [carrier]/[region] drive the avatar + region filter;
 * [lastAppliedLabel] is non-null only on the last-applied preset.
 */
data class PresetRowUi(
    val id: String,
    val label: String,
    val carrier: String,
    /** The network/variant shown as the card subtitle (the label minus the carrier name); may be blank. */
    val subtitle: String,
    val region: String,
    val isFavorite: Boolean,
    val lastAppliedLabel: String?,
)

/**
 * List UI state: a flat list of preset cards. Favorited presets are floated into [favorites]
 * (the "★ Favorites" section); [presets] is the rest, filtered by [query] and [selectedRegion].
 * [regions] are the available region names for the top-right selector.
 */
data class PresetListUiState(
    val loading: Boolean = true,
    val favorites: List<PresetRowUi> = emptyList(),
    val presets: List<PresetRowUi> = emptyList(),
    val regions: List<String> = emptyList(),
    val selectedRegion: String? = null,
    val query: String = "",
    val error: String? = null,
)

/**
 * Loads the bundled presets once, then reactively combines them with the persisted favorites +
 * last-applied state and the in-memory search/region filters into a [PresetListUiState]. Both the
 * favorites section and the main list are sorted A→Z by display name.
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

    private val query = MutableStateFlow("")
    private val selectedRegion = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PresetListUiState> =
        combine(
            settingsStore.favorites,
            settingsStore.lastApplied,
            query,
            selectedRegion,
        ) { favorites, lastApplied, q, region ->
            buildState(favorites, lastApplied, q, region)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PresetListUiState(loading = true),
        )

    fun toggleFavorite(presetId: String) {
        viewModelScope.launch { settingsStore.toggleFavorite(presetId) }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setRegion(region: String?) {
        selectedRegion.value = region
    }

    private fun buildState(
        favorites: Set<String>,
        lastApplied: LastApplied?,
        query: String,
        selectedRegion: String?,
    ): PresetListUiState {
        val tag = locale.language
        val needle = query.trim()

        fun rowOf(
            preset: Preset,
            carrier: String,
            region: String,
        ): PresetRowUi {
            val resolvedLabel = preset.label.resolve(tag)
            return PresetRowUi(
                id = preset.id,
                label = resolvedLabel,
                carrier = carrier,
                subtitle = resolvedLabel.removePrefix(carrier).trim().trim('(', ')', '（', '）'),
                region = region,
                isFavorite = preset.id in favorites,
                lastAppliedLabel =
                    lastApplied
                        ?.takeIf { it.presetId == preset.id }
                        ?.let { ApnDateFormat.format(it.epochMillis, locale, zone) },
            )
        }

        fun matches(row: PresetRowUi): Boolean =
            needle.isBlank() ||
                row.label.contains(needle, ignoreCase = true) ||
                row.carrier.contains(needle, ignoreCase = true)

        val allRows =
            regions.flatMap { region ->
                val regionName = region.name.resolve(tag)
                region.carriers.flatMap { carrier ->
                    val carrierName = carrier.name.resolve(tag)
                    carrier.presets.map { rowOf(it, carrierName, regionName) }
                }
            }

        val regionNames = regions.map { it.name.resolve(tag) }
        val effectiveRegion = selectedRegion?.takeIf { it in regionNames }

        return PresetListUiState(
            loading = false,
            favorites = allRows.filter { it.isFavorite && matches(it) }.sortedBy { it.label.lowercase(locale) },
            presets =
                allRows
                    .filter {
                        !it.isFavorite && matches(it) && (effectiveRegion == null || it.region == effectiveRegion)
                    }.sortedBy { it.label.lowercase(locale) },
            regions = regionNames,
            selectedRegion = effectiveRegion,
            query = query,
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
