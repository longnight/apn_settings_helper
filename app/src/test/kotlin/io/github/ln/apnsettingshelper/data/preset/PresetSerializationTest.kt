package io.github.ln.apnsettingshelper.data.preset

import io.github.ln.apnsettingshelper.domain.model.ApnProtocol
import io.github.ln.apnsettingshelper.domain.model.AuthType
import io.github.ln.apnsettingshelper.domain.model.findPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetSerializationTest {
    /** A minimal valid file; optional fields are omitted so defaults apply. */
    private val validJson =
        """
        {
          "schemaVersion": 1,
          "regions": [{
            "code": "JP",
            "name": {"en": "Japan", "ja": "日本"},
            "carriers": [{
              "id": "c1",
              "name": {"en": "Carrier 1", "ja": "事業者1"},
              "presets": [{
                "id": "p1",
                "label": {"en": "P1", "ja": "P1"},
                "apn": "a.example.jp",
                "mcc": "440",
                "mnc": "10"
              }]
            }]
          }]
        }
        """.trimIndent()

    private fun jsonWithPreset(presetBody: String) =
        """
        {"schemaVersion":1,"regions":[{"code":"JP","name":{"en":"Japan","ja":"日本"},
        "carriers":[{"id":"c1","name":{"en":"C1","ja":"C1"},"presets":[$presetBody]}]}]}
        """.trimIndent()

    @Test
    fun `parses valid json and groups region to carrier to preset`() {
        val regions = PresetSerialization.parseAndValidate(validJson)

        assertEquals(1, regions.size)
        assertEquals("JP", regions[0].code)
        assertEquals("日本", regions[0].name.ja)
        assertEquals(1, regions[0].carriers.size)
        assertEquals("c1", regions[0].carriers[0].id)
        assertEquals(1, regions[0].carriers[0].presets.size)
    }

    @Test
    fun `applies defaults for omitted optional fields`() {
        val preset = PresetSerialization.parseAndValidate(validJson).findPreset("p1")!!

        assertEquals("a.example.jp", preset.apn)
        assertEquals("", preset.username)
        assertEquals(AuthType.NONE, preset.authType)
        assertEquals(ApnProtocol.IPV4V6, preset.protocol)
    }

    @Test
    fun `rejects duplicate preset ids`() {
        val body =
            """
            {"id":"dup","label":{"en":"A","ja":"A"},"apn":"a.jp","mcc":"440","mnc":"10"},
            {"id":"dup","label":{"en":"B","ja":"B"},"apn":"b.jp","mcc":"440","mnc":"10"}
            """.trimIndent()
        val ex =
            assertThrows(PresetException::class.java) {
                PresetSerialization.parseAndValidate(jsonWithPreset(body))
            }
        assertTrue(ex.message!!.contains("duplicate preset id: dup"))
    }

    @Test
    fun `rejects blank apn`() {
        val body = """{"id":"p1","label":{"en":"A","ja":"A"},"apn":"","mcc":"440","mnc":"10"}"""
        val ex =
            assertThrows(PresetException::class.java) {
                PresetSerialization.parseAndValidate(jsonWithPreset(body))
            }
        assertTrue(ex.message!!.contains("blank apn"))
    }

    @Test
    fun `rejects invalid mcc and mnc`() {
        val body = """{"id":"p1","label":{"en":"A","ja":"A"},"apn":"a.jp","mcc":"44","mnc":"1"}"""
        val ex =
            assertThrows(PresetException::class.java) {
                PresetSerialization.parseAndValidate(jsonWithPreset(body))
            }
        assertTrue(ex.message!!.contains("invalid mcc"))
        assertTrue(ex.message!!.contains("invalid mnc"))
    }

    @Test
    fun `rejects missing required field`() {
        // No "apn" key at all -> kotlinx missing-field error, wrapped as PresetException.
        val body = """{"id":"p1","label":{"en":"A","ja":"A"},"mcc":"440","mnc":"10"}"""
        assertThrows(PresetException::class.java) {
            PresetSerialization.parseAndValidate(jsonWithPreset(body))
        }
    }

    @Test
    fun `rejects out-of-range enum value`() {
        val body =
            """{"id":"p1","label":{"en":"A","ja":"A"},"apn":"a.jp","mcc":"440","mnc":"10","authType":"BOGUS"}"""
        assertThrows(PresetException::class.java) {
            PresetSerialization.parseAndValidate(jsonWithPreset(body))
        }
    }

    @Test
    fun `rejects unsupported schema version`() {
        val json = validJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 999")
        val ex =
            assertThrows(PresetException::class.java) {
                PresetSerialization.parseAndValidate(json)
            }
        assertTrue(ex.message!!.contains("schemaVersion"))
    }

    @Test
    fun `rejects malformed json`() {
        assertThrows(PresetException::class.java) {
            PresetSerialization.parseAndValidate("{ not json")
        }
    }
}
