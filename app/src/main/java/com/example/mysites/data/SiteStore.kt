package com.example.mysites.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SiteStore(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val _sites = MutableStateFlow<List<Site>>(emptyList())
    val sites: StateFlow<List<Site>> = _sites.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var saveJob: Job? = null
    private var dirty = false

    init {
        scope.launch {
            val restored = runCatching {
                if (file.exists()) SiteBackupCodec.decode(file.readText()) else SiteBackup()
            }.getOrDefault(SiteBackup())
            synchronized(lock) {
                _sites.value = restored.sites
                _tags.value = restored.tags
            }
            _loaded.value = true
        }
    }

    fun add(name: String, url: String, tagIds: List<String> = emptyList()): Site {
        val site = Site.create(name, url)
        synchronized(lock) {
            val validTagIds = normalizedTagIds(tagIds)
            val newSite = site.copy(tagIds = validTagIds)
            updateSitesLocked { it + newSite }
            return newSite
        }
    }

    fun update(site: Site) {
        synchronized(lock) {
            val normalized = site.copy(
                name = site.name.trim(),
                tagIds = normalizedTagIds(site.tagIds),
            )
            updateSitesLocked { sites ->
                sites.map { if (it.id == normalized.id) normalized else it }
            }
        }
    }

    fun delete(id: String) {
        mutateSites { sites -> sites.filterNot { it.id == id } }
    }

    fun move(from: Int, to: Int) {
        mutateSites { sites ->
            if (from !in sites.indices || to !in sites.indices || from == to) {
                sites
            } else {
                sites.toMutableList().apply { add(to, removeAt(from)) }
            }
        }
    }

    fun replaceAll(backup: SiteBackup) {
        synchronized(lock) {
            val validTagIds = backup.tags.mapTo(mutableSetOf()) { it.id }
            _tags.value = backup.tags
            _sites.value = backup.sites.map { site ->
                site.copy(
                    tagIds = site.tagIds
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it in validTagIds }
                        .distinct(),
                )
            }
            scheduleSaveLocked()
        }
    }

    fun addTag(name: String, colorIndex: Int): Tag {
        val tag = Tag.create(name, colorIndex)
        mutateTags { tags -> tags + tag }
        return tag
    }

    fun updateTag(tag: Tag) {
        val normalized = tag.copy(
            name = tag.name.trim(),
            colorIndex = tag.colorIndex.coerceIn(0, TAG_COLOR_COUNT - 1),
        )
        mutateTags { tags -> tags.map { if (it.id == normalized.id) normalized else it } }
    }

    fun deleteTag(id: String) {
        synchronized(lock) {
            val remaining = _tags.value.filterNot { it.id == id }
            if (remaining == _tags.value) return

            _tags.value = remaining
            _sites.value = _sites.value.map { site ->
                if (id in site.tagIds) site.copy(tagIds = site.tagIds - id) else site
            }
            scheduleSaveLocked()
        }
    }

    fun moveTag(from: Int, to: Int) {
        mutateTags { tags ->
            if (from !in tags.indices || to !in tags.indices || from == to) {
                tags
            } else {
                tags.toMutableList().apply { add(to, removeAt(from)) }
            }
        }
    }

    fun setFavorite(siteId: String, favorite: Boolean) {
        mutateSites { sites ->
            sites.map { site ->
                if (site.id == siteId) site.copy(favorite = favorite) else site
            }
        }
    }

    fun setTags(siteId: String, tagIds: List<String>) {
        synchronized(lock) {
            val normalized = normalizedTagIds(tagIds)
            updateSitesLocked { sites ->
                sites.map { site ->
                    if (site.id == siteId) site.copy(tagIds = normalized) else site
                }
            }
        }
    }

    fun markUsed(siteId: String) {
        val now = System.currentTimeMillis()
        mutateSites { sites ->
            sites.map { site ->
                if (site.id == siteId) site.copy(lastUsedAt = now) else site
            }
        }
    }

    fun exportJson(): String = SiteBackupCodec.encode(_sites.value, _tags.value)

    fun flush() {
        synchronized(lock) {
            if (!dirty) return
            saveJob?.cancel()
            persist(_sites.value, _tags.value)
            dirty = false
        }
    }

    private fun mutateSites(transform: (List<Site>) -> List<Site>) {
        synchronized(lock) {
            updateSitesLocked(transform)
        }
    }

    private fun mutateTags(transform: (List<Tag>) -> List<Tag>) {
        synchronized(lock) {
            val updated = transform(_tags.value)
            if (updated == _tags.value) return
            _tags.value = updated
            scheduleSaveLocked()
        }
    }

    private fun updateSitesLocked(transform: (List<Site>) -> List<Site>) {
        val updated = transform(_sites.value)
        if (updated == _sites.value) return
        _sites.value = updated
        scheduleSaveLocked()
    }

    private fun normalizedTagIds(tagIds: List<String>): List<String> {
        val validTagIds = _tags.value.mapTo(mutableSetOf()) { it.id }
        return tagIds
            .map { it.trim() }
            .filter { it.isNotEmpty() && it in validTagIds }
            .distinct()
    }

    private fun scheduleSaveLocked() {
        dirty = true
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(SAVE_DEBOUNCE_MS)
            synchronized(lock) {
                persist(_sites.value, _tags.value)
                dirty = false
            }
        }
    }

    private fun persist(sites: List<Site>, tags: List<Tag>) {
        runCatching {
            val temp = File(file.parentFile, "$FILE_NAME.tmp")
            temp.writeText(SiteBackupCodec.encode(sites, tags))
            if (!temp.renameTo(file)) {
                file.writeText(temp.readText())
                temp.delete()
            }
        }
    }

    private companion object {
        const val FILE_NAME = "sites.json"
        const val SAVE_DEBOUNCE_MS = 300L
    }
}
