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
    fun `sorts presets A to Z by name and exposes regions when no favorites`() =
        runTest {
            val state = viewModel().uiState.first { !it.loading }

            assertTrue(state.favorites.isEmpty())
            assertEquals(listOf("Japan"), state.regions)
            assertEquals(listOf("his-d", "his-sb", "iij-d"), state.presets.map { it.id })
            assertEquals(listOf("HIS Mobile", "HIS Mobile", "IIJmio"), state.presets.map { it.carrier })
        }

    @Test
    fun `favorited presets appear in the favorites section and stay in the list`() =
        runTest {
            val state = viewModel(FakeSettingsStore(initialFavorites = setOf("his-d"))).uiState.first { !it.loading }

            assertEquals(listOf("his-d"), state.favorites.map { it.id })
            assertTrue(state.favorites[0].isFavorite)
            // The favorited preset stays in All Presets at its A→Z position (no longer popped out).
            assertEquals(listOf("his-d", "his-sb", "iij-d"), state.presets.map { it.id })
            assertTrue(state.presets.single { it.id == "his-d" }.isFavorite)
        }

    @Test
    fun `last applied label is set only on the matching preset`() =
        runTest {
            val millis = Instant.parse("2026-06-27T05:30:00Z").toEpochMilli()
            val store = FakeSettingsStore(initialLastApplied = LastApplied("iij-d", millis))

            val state = viewModel(store).uiState.first { !it.loading }
            val expected = ApnDateFormat.format(millis, Locale.ENGLISH, tokyo)
            val iij = state.presets.single { it.id == "iij-d" }
            val his = state.presets.single { it.id == "his-d" }

            assertEquals(expected, iij.lastAppliedLabel)
            assertNull(his.lastAppliedLabel)
        }

    @Test
    fun `toggleFavorite adds a preset to favorites while keeping it in the list`() =
        runTest {
            val vm = viewModel()
            val initial = vm.uiState.first { !it.loading }
            assertTrue(initial.favorites.isEmpty())

            vm.toggleFavorite("his-d")

            val after = vm.uiState.first { it.favorites.isNotEmpty() }
            assertEquals(listOf("his-d"), after.favorites.map { it.id })
            // Favoriting no longer removes it from All Presets — it stays in its A→Z slot.
            assertEquals(listOf("his-d", "his-sb", "iij-d"), after.presets.map { it.id })
        }

    @Test
    fun `setQuery filters presets by label or carrier`() =
        runTest {
            val vm = viewModel()
            vm.uiState.first { !it.loading }

            vm.setQuery("softbank")

            val state = vm.uiState.first { it.query == "softbank" }
            assertEquals(listOf("his-sb"), state.presets.map { it.id })
        }

    @Test
    fun `setRegion narrows the list to the chosen region`() =
        runTest {
            val vm = viewModel()
            vm.uiState.first { !it.loading }

            vm.setRegion("Japan")

            val state = vm.uiState.first { it.selectedRegion == "Japan" }
            assertEquals(listOf("his-d", "his-sb", "iij-d"), state.presets.map { it.id })
        }

    @Test
    fun `resolves japanese labels for a japanese locale`() =
        runTest {
            val state = viewModel(locale = Locale.JAPANESE).uiState.first { !it.loading }

            assertEquals(listOf("日本"), state.regions)
            // Sorted A→Z by label: katakana ソ (SoftBank) precedes ド (Docomo), so SoftBank is first.
            assertEquals(listOf("HISソフトバンク", "HISドコモ", "IIJmio"), state.presets.map { it.label })
            assertEquals("HISモバイル", state.presets[0].carrier)
        }
}
