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
    private val tags = listOf(
        Tag(id = "tag-work", name = "工作", colorIndex = 3),
        Tag(id = "tag-fun", name = "娛樂", colorIndex = 12),
    )

    private val sites = listOf(
        Site(
            id = "a",
            name = "Example",
            url = "https://example.com",
            createdAt = 1000L,
            favorite = true,
            lastUsedAt = 3000L,
            tagIds = listOf("tag-work"),
        ),
        Site(
            id = "b",
            name = "Local",
            url = "http://localhost:8080",
            createdAt = 2000L,
            tagIds = listOf("tag-work", "tag-fun"),
        ),
    )

    @Test
    fun roundTripPreservesOrderAndFields() {
        val encoded = SiteBackupCodec.encode(
            sites = sites,
            tags = tags,
            exportedAt = Instant.parse("2026-10-18T00:00:00Z"),
        )
        val decoded = SiteBackupCodec.decode(encoded)

        assertEquals(sites, decoded.sites)
        assertEquals(tags, decoded.tags)
    }

    @Test
    fun encodedJsonContainsVersionAndExportTime() {
        val encoded = SiteBackupCodec.encode(
            sites = sites,
            tags = tags,
            exportedAt = Instant.parse("2026-10-18T00:00:00Z"),
        )
        val root = Json.parseToJsonElement(encoded).jsonObject

        assertEquals(2, root.getValue("version").jsonPrimitive.content.toInt())
        assertEquals("2026-10-18T00:00:00Z", root.getValue("exportedAt").jsonPrimitive.content)
        assertEquals(2, root.getValue("sites").jsonArray.size)
        assertEquals(2, root.getValue("tags").jsonArray.size)
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

        assertEquals(2, decoded.sites.size)
        assertTrue(decoded.tags.isEmpty())
        assertEquals("https://example.com", decoded.sites[0].url)
    }

    @Test
    fun decodesLegacyVersionOneWithoutTags() {
        val raw = """
            {
              "version": 1,
              "exportedAt": "2026-10-18T00:00:00Z",
              "sites": [
                {"id":"1","name":"Legacy","url":"example.com"}
              ]
            }
        """.trimIndent()

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(1, decoded.sites.size)
        assertTrue(decoded.tags.isEmpty())
        assertEquals(false, decoded.sites[0].favorite)
        assertEquals(0L, decoded.sites[0].lastUsedAt)
        assertTrue(decoded.sites[0].tagIds.isEmpty())
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

        assertEquals(2, decoded.sites.size)
        assertEquals("Valid", decoded.sites[0].name)
        assertEquals("Trimmed", decoded.sites[1].name)
    }

    @Test
    fun assignsIdsWhenMissing() {
        val raw = """[{"id":"","name":"No id","url":"https://example.com"}]"""

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(1, decoded.sites.size)
        assertTrue(decoded.sites[0].id.isNotBlank())
    }

    @Test
    fun missingTextZoomDefaultsToDefault() {
        val raw = """[{"id":"x","name":"No zoom","url":"https://example.com"}]"""

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(1, decoded.sites.size)
        assertEquals(DEFAULT_WEB_TEXT_ZOOM, decoded.sites[0].textZoom)
    }

    @Test
    fun textZoomRoundTripsAndIsClamped() {
        val withZoom = listOf(
            Site(
                id = "a",
                name = "Zoomed",
                url = "https://example.com",
                createdAt = 1000L,
                textZoom = 140,
            ),
        )
        assertEquals(withZoom, SiteBackupCodec.decode(SiteBackupCodec.encode(withZoom)).sites)

        val raw = """
            [
              {"id":"a","name":"Too large","url":"https://example.com","textZoom":500},
              {"id":"b","name":"Too small","url":"https://example.org","textZoom":10}
            ]
        """.trimIndent()
        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(MAX_WEB_TEXT_ZOOM, decoded.sites[0].textZoom)
        assertEquals(MIN_WEB_TEXT_ZOOM, decoded.sites[1].textZoom)
    }

    @Test
    fun validatesTagsAndFiltersUnknownSiteTagIds() {
        val raw = """
            {
              "version": 2,
              "tags": [
                {"id":"t1","name":" 工作 ","colorIndex":3},
                {"id":"t1","name":"Duplicate","colorIndex":4},
                {"id":"","name":"No id","colorIndex":99},
                {"id":"t2","name":"   ","colorIndex":0}
              ],
              "sites": [
                {"id":"s1","name":"Site","url":"example.com","tagIds":["t1","t1","missing"]}
              ]
            }
        """.trimIndent()

        val decoded = SiteBackupCodec.decode(raw)

        assertEquals(2, decoded.tags.size)
        assertEquals("工作", decoded.tags[0].name)
        assertEquals(3, decoded.tags[0].colorIndex)
        assertTrue(decoded.tags[1].id.isNotBlank())
        assertEquals(TAG_COLOR_COUNT - 1, decoded.tags[1].colorIndex)
        assertEquals(listOf("t1"), decoded.sites[0].tagIds)
    }
}
