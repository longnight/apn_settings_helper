package io.github.ln.apnsettingshelper.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.ln.apnsettingshelper.ApnApplication
import io.github.ln.apnsettingshelper.data.preset.PresetRepository
import io.github.ln.apnsettingshelper.data.store.SettingsStore
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.common.ApnDateFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale

/**
 * Detail UI state. [preset] carries the literal field values for the copy/checklist UI;
 * [title]/[notes] are the locale-resolved strings. [notFound] is set if the id is unknown.
 */
data class PresetDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val preset: Preset? = null,
    val title: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val lastAppliedLabel: String? = null,
)

/** Resolves a single preset by id and tracks its favorite + last-applied state. */
class PresetDetailViewModel(
    private val presetId: String,
    repository: PresetRepository,
    private val settingsStore: SettingsStore,
    private val locale: Locale = Locale.getDefault(),
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val preset: Preset? = repository.findPreset(presetId)

    val uiState: StateFlow<PresetDetailUiState> =
        combine(settingsStore.favorites, settingsStore.lastApplied) { favorites, lastApplied ->
            val current = preset
            if (current == null) {
                PresetDetailUiState(loading = false, notFound = true)
            } else {
                val tag = locale.language
                PresetDetailUiState(
                    loading = false,
                    preset = current,
                    title = current.label.resolve(tag),
                    notes = current.notes.resolve(tag),
                    isFavorite = current.id in favorites,
                    lastAppliedLabel =
                        lastApplied
                            ?.takeIf { it.presetId == current.id }
                            ?.let { ApnDateFormat.format(it.epochMillis, locale, zone) },
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PresetDetailUiState(loading = true),
        )

    fun toggleFavorite() {
        viewModelScope.launch { settingsStore.toggleFavorite(presetId) }
    }

    fun recordApplied() {
        viewModelScope.launch { settingsStore.recordApplied(presetId) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(presetId: String) =
            viewModelFactory {
                initializer {
                    val app = this[APPLICATION_KEY] as ApnApplication
                    PresetDetailViewModel(presetId, app.graph.presetRepository, app.graph.settingsStore)
                }
            }
    }
}
