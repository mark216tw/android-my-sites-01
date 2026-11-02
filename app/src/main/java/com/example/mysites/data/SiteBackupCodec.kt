package com.example.mysites.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

object SiteBackupCodec {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(
        sites: List<Site>,
        tags: List<Tag> = emptyList(),
        exportedAt: Instant = Instant.now(),
    ): String {
        return json.encodeToString(
            SiteBackup(
                version = 2,
                exportedAt = exportedAt.toString(),
                sites = sites,
                tags = tags,
            ),
        )
    }

    fun decode(text: String): SiteBackup {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "JSON is empty" }

        val raw = if (trimmed.startsWith("[")) {
            SiteBackup(
                version = 1,
                sites = json.decodeFromString<List<Site>>(trimmed),
            )
        } else {
            json.decodeFromString<SiteBackup>(trimmed)
        }

        val tags = decodeTags(raw.tags)
        val validTagIds = tags.mapTo(mutableSetOf()) { it.id }
        val sites = decodeSites(raw.sites, validTagIds)

        return SiteBackup(
            version = 2,
            exportedAt = raw.exportedAt,
            sites = sites,
            tags = tags,
        )
    }

    private fun decodeTags(rawTags: List<Tag>): List<Tag> {
        val seenIds = mutableSetOf<String>()
        return rawTags.mapNotNull { tag ->
            val name = tag.name.trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val id = tag.id.trim().ifEmpty { UUID.randomUUID().toString() }
            if (!seenIds.add(id)) return@mapNotNull null

            Tag(
                id = id,
                name = name,
                colorIndex = tag.colorIndex.coerceIn(0, TAG_COLOR_COUNT - 1),
            )
        }
    }

    private fun decodeSites(rawSites: List<Site>, validTagIds: Set<String>): List<Site> {
        val seenIds = mutableSetOf<String>()
        return rawSites.mapNotNull { site ->
            val name = site.name.trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val url = UrlNormalizer.normalize(site.url) ?: return@mapNotNull null
            val id = site.id.trim().ifEmpty { UUID.randomUUID().toString() }
            if (!seenIds.add(id)) return@mapNotNull null

            Site(
                id = id,
                name = name,
                url = url,
                createdAt = if (site.createdAt > 0L) site.createdAt else System.currentTimeMillis(),
                textZoom = site.textZoom.coerceIn(MIN_WEB_TEXT_ZOOM, MAX_WEB_TEXT_ZOOM),
                favorite = site.favorite,
                lastUsedAt = site.lastUsedAt.coerceAtLeast(0L),
                tagIds = site.tagIds
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && it in validTagIds }
                    .distinct(),
            )
        }
    }
}
