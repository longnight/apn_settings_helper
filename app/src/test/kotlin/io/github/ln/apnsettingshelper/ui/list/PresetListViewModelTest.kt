package io.github.ln.apnsettingshelper.ui.list

import io.github.ln.apnsettingshelper.domain.model.LastApplied
import io.github.ln.apnsettingshelper.testutil.FakePresetRepository
import io.github.ln.apnsettingshelper.testutil.FakeSettingsStore
import io.github.ln.apnsettingshelper.testutil.MainDispatcherRule
import io.github.ln.apnsettingshelper.testutil.sampleRegions
import io.github.ln.apnsettingshelper.ui.common.ApnDateFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class PresetListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tokyo = ZoneId.of("Asia/Tokyo")

    private fun viewModel(
        store: FakeSettingsStore = FakeSettingsStore(),
        locale: Locale = Locale.ENGLISH,
    ) = PresetListViewModel(FakePresetRepository(sampleRegions()), store, locale, tokyo)

    @Test
    fun `groups presets by region and carrier when no favorites`() =
        runTest {
            val state = viewModel().uiState.first { !it.loading }

            assertTrue(state.favorites.isEmpty())
            assertEquals(1, state.regions.size)
            assertEquals("Japan", state.regions[0].region)
            val carriers = state.regions[0].carriers
            assertEquals(listOf("HIS Mobile", "IIJmio"), carriers.map { it.carrier })
            assertEquals(listOf("his-d", "his-sb"), carriers[0].rows.map { it.id })
            assertEquals(listOf("iij-d"), carriers[1].rows.map { it.id })
        }

    @Test
    fun `favorited presets float into the favorites section and out of the groups`() =
        runTest {
            val state = viewModel(FakeSettingsStore(initialFavorites = setOf("his-d"))).uiState.first { !it.loading }

            assertEquals(listOf("his-d"), state.favorites.map { it.id })
            assertTrue(state.favorites[0].isFavorite)
            // his-d no longer appears under its carrier; his-sb still does.
            assertEquals(
                listOf("his-sb"),
                state.regions[0]
                    .carriers[0]
                    .rows
                    .map { it.id },
            )
        }

    @Test
    fun `last applied label is set only on the matching preset`() =
        runTest {
            val millis = Instant.parse("2026-06-27T05:30:00Z").toEpochMilli()
            val store = FakeSettingsStore(initialLastApplied = LastApplied("iij-d", millis))

            val state = viewModel(store).uiState.first { !it.loading }

            val expected = ApnDateFormat.format(millis, Locale.ENGLISH, tokyo)
            val allRows = state.regions.flatMap { it.carriers }.flatMap { it.rows }
            assertEquals(expected, allRows.first { it.id == "iij-d" }.lastAppliedLabel)
            assertNull(allRows.first { it.id == "his-d" }.lastAppliedLabel)
        }

    @Test
    fun `toggleFavorite moves a preset into favorites`() =
        runTest {
            val vm = viewModel()
            assertTrue(
                vm.uiState
                    .first { !it.loading }
                    .favorites
                    .isEmpty(),
            )

            vm.toggleFavorite("his-d")

            val after = vm.uiState.first { it.favorites.isNotEmpty() }
            assertEquals(listOf("his-d"), after.favorites.map { it.id })
            assertEquals(
                listOf("his-sb"),
                after.regions[0]
                    .carriers[0]
                    .rows
                    .map { it.id },
            )
        }

    @Test
    fun `resolves japanese labels for a japanese locale`() =
        runTest {
            val state = viewModel(locale = Locale.JAPANESE).uiState.first { !it.loading }

            assertEquals("日本", state.regions[0].region)
            assertEquals("HISモバイル", state.regions[0].carriers[0].carrier)
            assertEquals(
                "HISドコモ",
                state.regions[0]
                    .carriers[0]
                    .rows[0]
                    .label,
            )
        }
}
