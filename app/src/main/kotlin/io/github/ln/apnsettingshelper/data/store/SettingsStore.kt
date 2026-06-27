package io.github.ln.apnsettingshelper.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Local, network-free persistence for the two pieces of user state (AGENTS.md):
 * - [favorites]: any number of favorited preset ids.
 * - [lastApplied]: a single slot overwritten on each apply, or `null` until first apply.
 *
 * Favorites and last-applied are independent — they may coincide or differ. Exposed as
 * [Flow]s so the UI recomposes on change; mutators are `suspend` (DataStore I/O).
 */
interface SettingsStore {
    val favorites: Flow<Set<String>>
    val lastApplied: Flow<LastApplied?>

    /** Add or remove [presetId] from favorites. Idempotent. */
    suspend fun setFavorite(
        presetId: String,
        favorite: Boolean,
    )

    /** Flip the favorite state of [presetId]. */
    suspend fun toggleFavorite(presetId: String)

    /** Overwrite the last-applied slot with [presetId] stamped at the current time. */
    suspend fun recordApplied(presetId: String)
}

private const val DATASTORE_NAME = "settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

/**
 * [SettingsStore] backed by a Preferences DataStore. The [now] clock is injectable so
 * [recordApplied] is deterministic in tests; production uses the system wall clock.
 */
class DataStoreSettingsStore(
    private val dataStore: DataStore<Preferences>,
    private val now: () -> Long = System::currentTimeMillis,
) : SettingsStore {
    // A read error (e.g. corrupt file) yields empty state rather than crashing collectors.
    private val data: Flow<Preferences> =
        dataStore.data.catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    override val favorites: Flow<Set<String>> =
        data.map { it[KEY_FAVORITES] ?: emptySet() }

    override val lastApplied: Flow<LastApplied?> =
        data.map { prefs ->
            val id = prefs[KEY_LAST_APPLIED_ID]
            val at = prefs[KEY_LAST_APPLIED_AT]
            if (id != null && at != null) LastApplied(id, at) else null
        }

    override suspend fun setFavorite(
        presetId: String,
        favorite: Boolean,
    ) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES] ?: emptySet()
            prefs[KEY_FAVORITES] = if (favorite) current + presetId else current - presetId
        }
    }

    override suspend fun toggleFavorite(presetId: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES] ?: emptySet()
            prefs[KEY_FAVORITES] = if (presetId in current) current - presetId else current + presetId
        }
    }

    override suspend fun recordApplied(presetId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_APPLIED_ID] = presetId
            prefs[KEY_LAST_APPLIED_AT] = now()
        }
    }

    companion object {
        private val KEY_FAVORITES = stringSetPreferencesKey("favorites")
        private val KEY_LAST_APPLIED_ID = stringPreferencesKey("last_applied_id")
        private val KEY_LAST_APPLIED_AT = longPreferencesKey("last_applied_at")

        /** Build the app-wide store from a [Context] (uses the single process DataStore). */
        fun from(context: Context): DataStoreSettingsStore = DataStoreSettingsStore(context.applicationContext.settingsDataStore)
    }
}
