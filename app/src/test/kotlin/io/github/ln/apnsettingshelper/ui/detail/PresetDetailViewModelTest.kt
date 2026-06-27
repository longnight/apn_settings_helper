package io.github.ln.apnsettingshelper.ui.detail

import app.cash.turbine.test
import io.github.ln.apnsettingshelper.domain.apply.ApplyStrategyResolver
import io.github.ln.apnsettingshelper.domain.apply.ShellRunner
import io.github.ln.apnsettingshelper.domain.model.LastApplied
import io.github.ln.apnsettingshelper.testutil.FakePresetRepository
import io.github.ln.apnsettingshelper.testutil.FakeSettingsStore
import io.github.ln.apnsettingshelper.testutil.FakeShellRunner
import io.github.ln.apnsettingshelper.testutil.MainDispatcherRule
import io.github.ln.apnsettingshelper.testutil.sampleRegions
import io.github.ln.apnsettingshelper.ui.common.ApnDateFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class PresetDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tokyo = ZoneId.of("Asia/Tokyo")
    private val fixedNow = 1_700_000_000_000L

    private fun viewModel(
        presetId: String,
        store: FakeSettingsStore = FakeSettingsStore(now = { fixedNow }),
        locale: Locale = Locale.ENGLISH,
        shellRunner: ShellRunner = FakeShellRunner(rootAvailable = false),
    ) = PresetDetailViewModel(
        presetId,
        FakePresetRepository(sampleRegions()),
        store,
        ApplyStrategyResolver(shellRunner),
        locale,
        tokyo,
    )

    @Test
    fun `loads preset fields and is not favorite by default`() =
        runTest {
            val state = viewModel("his-d").uiState.first { !it.loading }

            assertFalse(state.notFound)
            assertEquals("his-d", state.preset?.id)
            assertEquals("HIS Docomo", state.title)
            assertFalse(state.isFavorite)
            assertNull(state.lastAppliedLabel)
        }

    @Test
    fun `unknown id yields not found`() =
        runTest {
            val state = viewModel("does-not-exist").uiState.first { !it.loading }

            assertTrue(state.notFound)
            assertNull(state.preset)
        }

    @Test
    fun `toggleFavorite favorites then unfavorites`() =
        runTest {
            val vm = viewModel("his-d")
            assertFalse(vm.uiState.first { !it.loading }.isFavorite)

            vm.toggleFavorite()
            assertTrue(vm.uiState.first { it.isFavorite }.isFavorite)

            vm.toggleFavorite()
            assertFalse(vm.uiState.first { !it.loading && !it.isFavorite }.isFavorite)
        }

    @Test
    fun `recordApplied sets the last applied label`() =
        runTest {
            val vm = viewModel("his-d")

            vm.uiState.test {
                val loaded = awaitItem().let { if (it.loading) awaitItem() else it }
                assertNull(loaded.lastAppliedLabel)

                vm.recordApplied()

                val applied = awaitItem()
                assertEquals(ApnDateFormat.format(fixedNow, Locale.ENGLISH, tokyo), applied.lastAppliedLabel)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `canApplyRoot is false without root`() =
        runTest {
            val state = viewModel("his-d").uiState.first { !it.loading }
            assertFalse(state.canApplyRoot)
        }

    @Test
    fun `applyNow on a rooted device records last-applied and emits Applied`() =
        runTest {
            val store = FakeSettingsStore(now = { fixedNow })
            val vm = viewModel("his-d", store = store, shellRunner = FakeShellRunner(rootAvailable = true))
            assertTrue(vm.uiState.first { it.canApplyRoot }.canApplyRoot)

            vm.applyEvents.test {
                vm.applyNow()
                assertEquals(ApplyEvent.Applied, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(LastApplied("his-d", fixedNow), store.lastApplied.first())
        }
}
