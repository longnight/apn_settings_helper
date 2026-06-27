package io.github.ln.apnsettingshelper.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.ln.apnsettingshelper.ApnApplication
import io.github.ln.apnsettingshelper.data.preset.PresetRepository
import io.github.ln.apnsettingshelper.data.store.SettingsStore
import io.github.ln.apnsettingshelper.domain.apply.ApplyOutcome
import io.github.ln.apnsettingshelper.domain.apply.ApplyStrategy
import io.github.ln.apnsettingshelper.domain.apply.ApplyStrategyResolver
import io.github.ln.apnsettingshelper.domain.apply.ApplyTier
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.common.ApnDateFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
 * [canApplyRoot] enables the one-tap "Apply now" button; [applying] guards it while running.
 */
data class PresetDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val preset: Preset? = null,
    val title: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val lastAppliedLabel: String? = null,
    val canApplyRoot: Boolean = false,
    val applying: Boolean = false,
)

/** One-shot result of a root apply, surfaced to the screen as a toast. */
sealed interface ApplyEvent {
    data object Applied : ApplyEvent

    data class Failed(
        val message: String,
    ) : ApplyEvent
}

/** Resolves a single preset by id, tracks favorite + last-applied, and drives the apply flow. */
class PresetDetailViewModel(
    private val presetId: String,
    repository: PresetRepository,
    private val settingsStore: SettingsStore,
    private val applyResolver: ApplyStrategyResolver,
    private val locale: Locale = Locale.getDefault(),
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val preset: Preset? = repository.findPreset(presetId)

    private var strategy: ApplyStrategy? = null
    private val rootAvailable = MutableStateFlow(false)
    private val applying = MutableStateFlow(false)

    private val applyEventsChannel = MutableSharedFlow<ApplyEvent>(extraBufferCapacity = 1)
    val applyEvents: SharedFlow<ApplyEvent> = applyEventsChannel

    val uiState: StateFlow<PresetDetailUiState> =
        combine(settingsStore.favorites, settingsStore.lastApplied, rootAvailable, applying) {
            favorites,
            lastApplied,
            canApplyRoot,
            isApplying,
            ->
            buildState(favorites, lastApplied, canApplyRoot, isApplying)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PresetDetailUiState(loading = true),
        )

    init {
        viewModelScope.launch {
            val resolved = applyResolver.resolve()
            strategy = resolved
            rootAvailable.value = resolved.tier == ApplyTier.ROOT
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { settingsStore.toggleFavorite(presetId) }
    }

    fun recordApplied() {
        viewModelScope.launch { settingsStore.recordApplied(presetId) }
    }

    /** Root one-tap: write the APN, and on success auto-record it as last-applied. */
    fun applyNow() {
        val current = preset ?: return
        val activeStrategy = strategy ?: return
        viewModelScope.launch {
            applying.value = true
            val outcome = activeStrategy.apply(current)
            applying.value = false
            when (outcome) {
                is ApplyOutcome.Applied -> {
                    settingsStore.recordApplied(current.id)
                    applyEventsChannel.emit(ApplyEvent.Applied)
                }

                is ApplyOutcome.Failed -> {
                    applyEventsChannel.emit(ApplyEvent.Failed(outcome.message))
                }

                ApplyOutcome.ManualGuidance -> {
                    Unit
                }
            }
        }
    }

    private fun buildState(
        favorites: Set<String>,
        lastApplied: LastApplied?,
        canApplyRoot: Boolean,
        applying: Boolean,
    ): PresetDetailUiState {
        val current = preset ?: return PresetDetailUiState(loading = false, notFound = true)
        val tag = locale.language
        return PresetDetailUiState(
            loading = false,
            preset = current,
            title = current.label.resolve(tag),
            notes = current.notes.resolve(tag),
            isFavorite = current.id in favorites,
            lastAppliedLabel =
                lastApplied
                    ?.takeIf { it.presetId == current.id }
                    ?.let { ApnDateFormat.format(it.epochMillis, locale, zone) },
            canApplyRoot = canApplyRoot,
            applying = applying,
        )
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(presetId: String) =
            viewModelFactory {
                initializer {
                    val app = this[APPLICATION_KEY] as ApnApplication
                    PresetDetailViewModel(
                        presetId,
                        app.graph.presetRepository,
                        app.graph.settingsStore,
                        app.graph.applyResolver,
                    )
                }
            }
    }
}
