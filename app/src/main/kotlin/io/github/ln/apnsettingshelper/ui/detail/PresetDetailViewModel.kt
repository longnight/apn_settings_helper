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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale

/**
 * Detail UI state. [preset] carries the literal field values for the copy/checklist UI;
 * [title]/[notes] are the locale-resolved strings. [notFound] is set if the id is unknown.
 *
 * Root apply is opt-in: [rootRequested] reflects the user's toggle; while we probe for `su`
 * [rootChecking] is true; [canApplyRoot] enables the one-tap "Apply now" button once root is
 * confirmed; [applying] guards it while a write is in flight.
 */
data class PresetDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val preset: Preset? = null,
    val title: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val lastAppliedLabel: String? = null,
    val rootRequested: Boolean = false,
    val rootChecking: Boolean = false,
    val canApplyRoot: Boolean = false,
    val applying: Boolean = false,
)

/** One-shot result of a root apply, surfaced to the screen as a toast. */
sealed interface ApplyEvent {
    /** The APN was written and selected as active. */
    data object Applied : ApplyEvent

    /** The APN was written but couldn't be selected; the user must pick it manually. */
    data object WrittenNotSelected : ApplyEvent

    /** The apply failed; [detail] is an optional technical reason (e.g. captured stderr). */
    data class Failed(
        val detail: String?,
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

    // preset + locale are fixed for the VM's lifetime, so resolve the localized strings once
    // rather than on every uiState emission.
    private val title: String = preset?.label?.resolve(locale.language).orEmpty()
    private val notes: String = preset?.notes?.resolve(locale.language).orEmpty()

    private var strategy: ApplyStrategy? = null
    private val rootStatus = MutableStateFlow(RootStatus())
    private val applying = MutableStateFlow(false)

    // Buffered so a one-shot result isn't lost if the collector is briefly gone (e.g. rotation).
    private val applyEventsChannel = Channel<ApplyEvent>(Channel.BUFFERED)
    val applyEvents: Flow<ApplyEvent> = applyEventsChannel.receiveAsFlow()

    val uiState: StateFlow<PresetDetailUiState> =
        combine(settingsStore.favorites, settingsStore.lastApplied, rootStatus, applying) {
            favorites,
            lastApplied,
            root,
            isApplying,
            ->
            buildState(favorites, lastApplied, root, isApplying)
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

    /**
     * Opt-in root: the probe (which may pop the superuser-grant dialog) runs only when the user
     * turns this on — honoring the "invisible until opened" design, so opening a detail screen
     * never requests root on its own.
     */
    fun setRootApplyEnabled(enabled: Boolean) {
        if (preset == null) return
        rootStatus.update { it.copy(requested = enabled, available = if (enabled) it.available else null) }
        if (enabled) probeRoot()
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun probeRoot() {
        if (strategy?.tier == ApplyTier.ROOT) {
            rootStatus.update { it.copy(available = true) }
            return
        }
        rootStatus.update { it.copy(available = null) }
        viewModelScope.launch {
            // Any failure (no su, denied, dead shell) degrades to "no root" — never a crash.
            val resolved =
                try {
                    applyResolver.resolve()
                } catch (e: Exception) {
                    null
                }
            strategy = resolved
            rootStatus.update { it.copy(available = resolved?.tier == ApplyTier.ROOT) }
        }
    }

    /** Root one-tap: write the APN, and on success auto-record it as last-applied. */
    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun applyNow() {
        val current = preset ?: return
        val activeStrategy = strategy ?: return
        // Synchronous guard so a fast double-tap can't launch two concurrent applies.
        if (!applying.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                when (val outcome = activeStrategy.apply(current)) {
                    is ApplyOutcome.Applied -> {
                        settingsStore.recordApplied(current.id)
                        applyEventsChannel.send(ApplyEvent.Applied)
                    }

                    is ApplyOutcome.WrittenNotSelected -> {
                        settingsStore.recordApplied(current.id)
                        applyEventsChannel.send(ApplyEvent.WrittenNotSelected)
                    }

                    is ApplyOutcome.Failed -> {
                        applyEventsChannel.send(ApplyEvent.Failed(outcome.message.ifBlank { null }))
                    }

                    ApplyOutcome.ManualGuidance -> {
                        Unit
                    }
                }
            } catch (e: Exception) {
                applyEventsChannel.send(ApplyEvent.Failed(e.message))
            } finally {
                applying.value = false
            }
        }
    }

    private fun buildState(
        favorites: Set<String>,
        lastApplied: LastApplied?,
        root: RootStatus,
        applying: Boolean,
    ): PresetDetailUiState {
        val current = preset ?: return PresetDetailUiState(loading = false, notFound = true)
        return PresetDetailUiState(
            loading = false,
            preset = current,
            title = title,
            notes = notes,
            isFavorite = current.id in favorites,
            lastAppliedLabel =
                lastApplied
                    ?.takeIf { it.presetId == current.id }
                    ?.let { ApnDateFormat.format(it.epochMillis, locale, zone) },
            rootRequested = root.requested,
            rootChecking = root.requested && root.available == null,
            canApplyRoot = root.available == true,
            applying = applying,
        )
    }

    /** Root opt-in + probe state. [available] is null while unknown / probing. */
    private data class RootStatus(
        val requested: Boolean = false,
        val available: Boolean? = null,
    )

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
