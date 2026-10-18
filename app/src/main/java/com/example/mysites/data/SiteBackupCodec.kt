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

    fun encode(sites: List<Site>, exportedAt: Instant = Instant.now()): String {
        return json.encodeToString(
            SiteBackup(
                version = 1,
                exportedAt = exportedAt.toString(),
                sites = sites,
            ),
        )
    }

    fun decode(text: String): List<Site> {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "JSON is empty" }

        val rawSites = if (trimmed.startsWith("[")) {
            json.decodeFromString<List<Site>>(trimmed)
        } else {
            json.decodeFromString<SiteBackup>(trimmed).sites
        }

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
            )
        }
    }
}
