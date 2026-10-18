package com.example.mysites.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Site(
    val id: String = "",
    val name: String,
    val url: String,
    val createdAt: Long = 0L,
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
    val version: Int = 1,
    val exportedAt: String = "",
    val sites: List<Site> = emptyList(),
)
