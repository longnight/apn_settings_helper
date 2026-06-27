package io.github.ln.apnsettingshelper.data.store

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Exercises [DataStoreSettingsStore] against a real Preferences DataStore writing to a
 * temp file (pure JVM, no Android framework). State is written then read back through the
 * store's [kotlinx.coroutines.flow.Flow]s, which serialize to / re-read from disk —
 * proving it round-trips. The clock is injected so timestamps are deterministic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun newStore(
        scope: CoroutineScope,
        now: () -> Long = { T1 },
    ): DataStoreSettingsStore {
        val dataStore =
            PreferenceDataStoreFactory.create(scope = scope) {
                File(tmp.newFolder(), "settings.preferences_pb")
            }
        return DataStoreSettingsStore(dataStore, now)
    }

    @Test
    fun `favorites default to empty`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)
            assertEquals(emptySet<String>(), store.favorites.first())
        }

    @Test
    fun `setFavorite true adds and false removes`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)

            store.setFavorite("his-mobile", true)
            assertEquals(setOf("his-mobile"), store.favorites.first())

            store.setFavorite("his-mobile", false)
            assertEquals(emptySet<String>(), store.favorites.first())
        }

    @Test
    fun `setFavorite is idempotent`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)

            store.setFavorite("iijmio-d", true)
            store.setFavorite("iijmio-d", true)
            assertEquals(setOf("iijmio-d"), store.favorites.first())
        }

    @Test
    fun `toggleFavorite flips on then off`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)

            store.toggleFavorite("mineo-d")
            assertEquals(setOf("mineo-d"), store.favorites.first())

            store.toggleFavorite("mineo-d")
            assertEquals(emptySet<String>(), store.favorites.first())
        }

    @Test
    fun `multiple favorites coexist independently`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)

            store.setFavorite("his-mobile", true)
            store.toggleFavorite("rakuten")
            store.setFavorite("povo", true)
            assertEquals(setOf("his-mobile", "rakuten", "povo"), store.favorites.first())

            store.setFavorite("rakuten", false)
            assertEquals(setOf("his-mobile", "povo"), store.favorites.first())
        }

    @Test
    fun `lastApplied is null by default`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope)
            assertNull(store.lastApplied.first())
        }

    @Test
    fun `recordApplied stores id and timestamp`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope) { T1 }

            store.recordApplied("ocn")
            assertEquals(LastApplied("ocn", T1), store.lastApplied.first())
        }

    @Test
    fun `recordApplied overwrites the previous slot`() =
        runTest(UnconfinedTestDispatcher()) {
            var clock = T1
            val store = newStore(backgroundScope) { clock }

            store.recordApplied("ocn")
            assertEquals(LastApplied("ocn", T1), store.lastApplied.first())

            clock = T2
            store.recordApplied("linemo")
            assertEquals(LastApplied("linemo", T2), store.lastApplied.first())
        }

    @Test
    fun `favorites and lastApplied are independent`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = newStore(backgroundScope) { T1 }

            store.setFavorite("his-mobile", true)
            assertNull(store.lastApplied.first())

            store.recordApplied("povo")
            assertEquals(setOf("his-mobile"), store.favorites.first())
            assertEquals(LastApplied("povo", T1), store.lastApplied.first())
        }

    private companion object {
        const val T1 = 1_700_000_000_000L
        const val T2 = 1_700_000_900_000L
    }
}
