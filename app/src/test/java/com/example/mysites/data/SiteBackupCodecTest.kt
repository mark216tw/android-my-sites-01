package com.example.mysites.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SiteBackupCodecTest {
    private val sites = listOf(
        Site(id = "a", name = "Example", url = "https://example.com", createdAt = 1000L),
        Site(id = "b", name = "Local", url = "http://localhost:8080", createdAt = 2000L),
    )

    @Test
    fun roundTripPreservesOrderAndFields() {
        val encoded = SiteBackupCodec.encode(sites, exportedAt = Instant.parse("2026-10-18T00:00:00Z"))
        val decoded = SiteBackupCodec.decode(encoded)

        assertEquals(sites, decoded)
    }

    @Test
    fun encodedJsonContainsVersionAndExportTime() {
        val encoded = SiteBackupCodec.encode(sites, exportedAt = Instant.parse("2026-10-18T00:00:00Z"))
        val root = Json.parseToJsonElement(encoded).jsonObject

        assertEquals(1, root.getValue("version").jsonPrimitive.content.toInt())
        assertEquals("2026-10-18T00:00:00Z", root.getValue("exportedAt").jsonPrimitive.content)
        assertEquals(2, root.getValue("sites").jsonArray.size)
    }

    @Test
    fun decodesRawArray() {
        val raw = """
            [
              {"id":"x","name":"One","url":"example.com","createdAt":1},
              {"id":"y","name":"Two","url":"https://two.example","createdAt":2}
            ]
        """.trimIndent()

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(2, decoded.size)
        assertEquals("https://example.com", decoded[0].url)
    }

    @Test
    fun filtersInvalidEntriesAndDuplicates() {
        val raw = """
            {
              "version": 1,
              "sites": [
                {"id":"1","name":"Valid","url":"example.com"},
                {"id":"1","name":"Duplicate id","url":"https://duplicate.example"},
                {"id":"2","name":"  ","url":"https://blank-name.example"},
                {"id":"3","name":"Bad URL","url":"ftp://example.com"},
                {"id":"4","name":"Trimmed","url":"  https://trimmed.example  "}
              ]
            }
        """.trimIndent()

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(2, decoded.size)
        assertEquals("Valid", decoded[0].name)
        assertEquals("Trimmed", decoded[1].name)
    }

    @Test
    fun assignsIdsWhenMissing() {
        val raw = """[{"id":"","name":"No id","url":"https://example.com"}]"""

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(1, decoded.size)
        assertTrue(decoded[0].id.isNotBlank())
    }
}
