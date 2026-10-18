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

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var saveJob: Job? = null
    private var dirty = false

    init {
        scope.launch {
            val restored = runCatching {
                if (file.exists()) SiteBackupCodec.decode(file.readText()) else emptyList()
            }.getOrDefault(emptyList())
            synchronized(lock) {
                _sites.value = restored
            }
            _loaded.value = true
        }
    }

    fun add(name: String, url: String) {
        mutate { it + Site.create(name, url) }
    }

    fun update(site: Site) {
        mutate { sites -> sites.map { if (it.id == site.id) site else it } }
    }

    fun delete(id: String) {
        mutate { sites -> sites.filterNot { it.id == id } }
    }

    fun move(from: Int, to: Int) {
        mutate { sites ->
            if (from !in sites.indices || to !in sites.indices || from == to) {
                sites
            } else {
                sites.toMutableList().apply { add(to, removeAt(from)) }
            }
        }
    }

    fun replaceAll(newSites: List<Site>) {
        mutate { newSites }
    }

    fun exportJson(): String = SiteBackupCodec.encode(_sites.value)

    fun flush() {
        synchronized(lock) {
            if (!dirty) return
            saveJob?.cancel()
            persist(_sites.value)
            dirty = false
        }
    }

    private fun mutate(transform: (List<Site>) -> List<Site>) {
        synchronized(lock) {
            val updated = transform(_sites.value)
            if (updated == _sites.value) return
            _sites.value = updated
            dirty = true
            saveJob?.cancel()
            saveJob = scope.launch {
                delay(SAVE_DEBOUNCE_MS)
                synchronized(lock) {
                    persist(_sites.value)
                    dirty = false
                }
            }
        }
    }

    private fun persist(sites: List<Site>) {
        runCatching {
            val temp = File(file.parentFile, "$FILE_NAME.tmp")
            temp.writeText(SiteBackupCodec.encode(sites))
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
