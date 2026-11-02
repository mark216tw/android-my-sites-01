package com.example.mysites.data

import kotlinx.serialization.Serializable
import java.util.UUID

const val TAG_COLOR_COUNT = 16

@Serializable
data class Tag(
    val id: String = "",
    val name: String,
    val colorIndex: Int = 0,
) {
    companion object {
        fun create(name: String, colorIndex: Int): Tag = Tag(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            colorIndex = colorIndex.coerceIn(0, TAG_COLOR_COUNT - 1),
        )
    }
}

@Serializable
data class Site(
    val id: String = "",
    val name: String,
    val url: String,
    val createdAt: Long = 0L,
    val textZoom: Int = DEFAULT_WEB_TEXT_ZOOM,
    val favorite: Boolean = false,
    val lastUsedAt: Long = 0L,
    val tagIds: List<String> = emptyList(),
) {
    companion object {
        fun create(name: String, url: String): Site = Site(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            url = url,
            createdAt = System.currentTimeMillis(),
        )
    }
}

@Serializable
data class SiteBackup(
    val version: Int = 2,
    val exportedAt: String = "",
    val sites: List<Site> = emptyList(),
    val tags: List<Tag> = emptyList(),
)
