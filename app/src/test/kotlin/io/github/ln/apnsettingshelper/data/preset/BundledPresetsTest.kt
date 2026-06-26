package io.github.ln.apnsettingshelper.data.preset

import io.github.ln.apnsettingshelper.domain.model.Region
import io.github.ln.apnsettingshelper.domain.model.allPresets
import io.github.ln.apnsettingshelper.domain.model.findPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the actual bundled assets/presets.json (exposed to tests via the
 * test resources srcDir in app/build.gradle.kts). Guards against shipping bad data.
 */
class BundledPresetsTest {
    private fun loadBundled(): List<Region> {
        val text =
            javaClass
                .getResourceAsStream("/presets.json")
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: error("presets.json not found on the test classpath")
        return PresetSerialization.parseAndValidate(text)
    }

    @Test
    fun `bundled presets parse and validate`() {
        val regions = loadBundled()
        assertEquals("single JP region", listOf("JP"), regions.map { it.code })
    }

    @Test
    fun `covers at least the broad-JP carrier set`() {
        val carrierIds = loadBundled().flatMap { it.carriers }.map { it.id }
        // v1 target: 10+ JP MVNOs (plan M-B).
        assertTrue("expected 10+ carriers, got ${carrierIds.size}", carrierIds.size >= 10)
        val expected =
            listOf(
                "his-mobile",
                "iijmio",
                "mineo",
                "ocn-mobile-one",
                "rakuten-mobile",
                "linemo",
                "povo",
                "nuro-mobile",
                "biglobe-mobile",
                "yu-mobile",
                "aeon-mobile",
                "jcom-mobile",
            )
        assertTrue("missing carriers: ${expected - carrierIds.toSet()}", carrierIds.containsAll(expected))
    }

    @Test
    fun `every preset is a Japanese MCC and carries provenance`() {
        loadBundled().allPresets().forEach { p ->
            assertEquals("preset ${p.id} should be MCC 440 (Japan)", "440", p.mcc)
            assertTrue("preset ${p.id} missing source", p.source.isNotBlank())
            assertTrue("preset ${p.id} missing lastVerified", p.lastVerified.isNotBlank())
        }
    }

    @Test
    fun `spot-check verified APN values`() {
        val regions = loadBundled()
        assertEquals("dm.jplat.net", regions.findPreset("his-mobile-docomo")!!.apn)
        assertEquals("sb.mvno", regions.findPreset("his-mobile-softbank")!!.apn)
        assertEquals("mineo.jp", regions.findPreset("mineo-a")!!.apn)
        assertEquals("lte.ocn.ne.jp", regions.findPreset("ocn-mobile-one")!!.apn)
        // Easy to confuse with Y!mobile's "ymobile.jp" — verified as yumobile.jp.
        assertEquals("yumobile.jp", regions.findPreset("yu-mobile")!!.apn)
        assertEquals("rakuten.jp", regions.findPreset("rakuten-mobile")!!.apn)
    }
}
