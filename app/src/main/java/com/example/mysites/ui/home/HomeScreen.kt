package com.example.mysites.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mysites.R
import com.example.mysites.data.SettingsStore
import com.example.mysites.data.Site
import com.example.mysites.data.SiteBackup
import com.example.mysites.data.SiteBackupCodec
import com.example.mysites.data.SiteFilter
import com.example.mysites.data.SiteSortMode
import com.example.mysites.data.SiteStore
import com.example.mysites.data.Tag
import com.example.mysites.data.UrlNormalizer
import com.example.mysites.data.filterSites
import com.example.mysites.data.sortSites
import com.example.mysites.ui.tags.TagBadge
import com.example.mysites.ui.tags.TagDot
import com.example.mysites.ui.tags.TagPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    store: SiteStore,
    settingsStore: SettingsStore,
    onOpenSite: (Site) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val sites by store.sites.collectAsStateWithLifecycle()
    val tags by store.tags.collectAsStateWithLifecycle()
    val settings by settingsStore.settings.collectAsStateWithLifecycle()
    val loaded by store.loaded.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var query by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var editingSite by remember { mutableStateOf<Site?>(null) }
    var deletingSite by remember { mutableStateOf<Site?>(null) }
    var pendingImport by remember { mutableStateOf<SiteBackup?>(null) }

    var filterTypeName by rememberSaveable { mutableStateOf(HomeFilterType.ALL.name) }
    var filterTagId by rememberSaveable { mutableStateOf<String?>(null) }

    val filterType = HomeFilterType.entries.firstOrNull { it.name == filterTypeName }
        ?: HomeFilterType.ALL
    val selectedFilter = when (filterType) {
        HomeFilterType.ALL -> SiteFilter.All
        HomeFilterType.FAVORITES -> SiteFilter.Favorites
        HomeFilterType.TAG -> filterTagId
            ?.let { SiteFilter.Tag(it) }
            ?: SiteFilter.All
    }

    LaunchedEffect(tags, filterType, filterTagId) {
        if (filterType == HomeFilterType.TAG && tags.none { it.id == filterTagId }) {
            filterTypeName = HomeFilterType.ALL.name
            filterTagId = null
        }
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        val json = store.exportJson()
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            output.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Unable to open output stream")
                    }
                }
                val message = if (result.isSuccess) {
                    context.getString(
                        R.string.export_success,
                        store.sites.value.size,
                        store.tags.value.size,
                    )
                } else {
                    context.getString(R.string.export_failed)
                }
                showMessage(message)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.getOrNull()
                }
                val imported = text?.let { runCatching { SiteBackupCodec.decode(it) }.getOrNull() }

                when {
                    imported == null -> showMessage(context.getString(R.string.import_failed))
                    imported.sites.isEmpty() -> showMessage(context.getString(R.string.import_empty))
                    else -> pendingImport = imported
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_site),
                        )
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more),
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_json)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    importLauncher.launch(IMPORT_MIME_TYPES)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    exportLauncher.launch("my-sites-${LocalDate.now()}.json")
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenSettings()
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                )

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort),
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        SortMenuEntries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(stringResource(entry.labelRes)) },
                                leadingIcon = {
                                    if (settings.siteSortMode == entry.mode) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                },
                                onClick = {
                                    settingsStore.setSiteSortMode(entry.mode)
                                    showSortMenu = false
                                },
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filterType == HomeFilterType.ALL,
                        onClick = {
                            filterTypeName = HomeFilterType.ALL.name
                            filterTagId = null
                        },
                        label = { Text(stringResource(R.string.filter_all)) },
                    )
                }
                item {
                    FilterChip(
                        selected = filterType == HomeFilterType.FAVORITES,
                        onClick = {
                            filterTypeName = HomeFilterType.FAVORITES.name
                            filterTagId = null
                        },
                        label = { Text(stringResource(R.string.filter_favorites)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                            )
                        },
                    )
                }
                itemsIndexed(tags, key = { _, tag -> tag.id }) { _, tag ->
                    FilterChip(
                        selected = filterType == HomeFilterType.TAG && filterTagId == tag.id,
                        onClick = {
                            filterTypeName = HomeFilterType.TAG.name
                            filterTagId = tag.id
                        },
                        label = {
                            Text(
                                text = tag.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            TagDot(colorIndex = tag.colorIndex, size = 10.dp)
                        },
                    )
                }
            }

            val visibleSites = sortSites(
                sites = filterSites(sites, query, selectedFilter),
                mode = settings.siteSortMode,
            )
            val canReorder = filterType == HomeFilterType.ALL &&
                query.isBlank() &&
                settings.siteSortMode == SiteSortMode.CUSTOM

            when {
                !loaded -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                sites.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.no_sites),
                        hint = stringResource(R.string.no_sites_hint),
                        modifier = Modifier.weight(1f),
                    )
                }

                visibleSites.isEmpty() -> {
                    val titleRes = when {
                        query.isNotBlank() -> R.string.no_results
                        filterType == HomeFilterType.FAVORITES -> R.string.no_favorites
                        filterType == HomeFilterType.TAG -> R.string.no_tagged_sites
                        else -> R.string.no_results
                    }
                    val hintRes = when {
                        query.isNotBlank() -> null
                        filterType == HomeFilterType.FAVORITES -> R.string.no_favorites_hint
                        filterType == HomeFilterType.TAG -> R.string.no_tagged_sites_hint
                        else -> null
                    }
                    EmptyState(
                        title = stringResource(titleRes),
                        hint = hintRes?.let { stringResource(it) },
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    SiteList(
                        sites = visibleSites,
                        tags = tags,
                        reorderEnabled = canReorder,
                        onOpenSite = onOpenSite,
                        onEditSite = { editingSite = it },
                        onDeleteSite = { deletingSite = it },
                        onToggleFavorite = { site ->
                            store.setFavorite(site.id, !site.favorite)
                        },
                        onMove = { from, to -> store.move(from, to) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        SiteEditorDialog(
            title = stringResource(R.string.add_site),
            initialSite = null,
            tags = tags,
            store = store,
            onDismiss = { showAddDialog = false },
            onSave = { name, url, tagIds ->
                store.add(name, url, tagIds)
                showAddDialog = false
            },
        )
    }

    editingSite?.let { site ->
        SiteEditorDialog(
            title = stringResource(R.string.edit_site),
            initialSite = site,
            tags = tags,
            store = store,
            onDismiss = { editingSite = null },
            onSave = { name, url, tagIds ->
                if (url != site.url) {
                    settingsStore.clearLastWebUrl(site.id)
                }
                store.update(site.copy(name = name, url = url, tagIds = tagIds))
                editingSite = null
            },
        )
    }

    deletingSite?.let { site ->
        AlertDialog(
            onDismissRequest = { deletingSite = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_message, site.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.delete(site.id)
                        settingsStore.clearLastWebUrl(site.id)
                        deletingSite = null
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSite = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    pendingImport?.let { imported ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(stringResource(R.string.import_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.import_message,
                        imported.sites.size,
                        imported.tags.size,
                        sites.size,
                        tags.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.replaceAll(imported)
                        settingsStore.clearAllLastWebUrls()
                        pendingImport = null
                        showMessage(
                            context.getString(
                                R.string.import_success,
                                imported.sites.size,
                                imported.tags.size,
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SiteList(
    sites: List<Site>,
    tags: List<Tag>,
    reorderEnabled: Boolean,
    onOpenSite: (Site) -> Unit,
    onEditSite: (Site) -> Unit,
    onDeleteSite: (Site) -> Unit,
    onToggleFavorite: (Site) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (reorderEnabled) {
            onMove(from.index, to.index)
        }
    }
    val tagsById = remember(tags) { tags.associateBy { it.id } }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(sites, key = { _, site -> site.id }) { _, site ->
            ReorderableItem(reorderableState, key = site.id) { isDragging ->
                SiteRow(
                    site = site,
                    tags = site.tagIds.mapNotNull(tagsById::get),
                    isDragging = isDragging,
                    reorderEnabled = reorderEnabled,
                    dragHandleModifier = Modifier.draggableHandle(),
                    onClick = { onOpenSite(site) },
                    onEdit = { onEditSite(site) },
                    onDelete = { onDeleteSite(site) },
                    onToggleFavorite = { onToggleFavorite(site) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Suppress("ModifierParameter")
private fun SiteRow(
    site: Site,
    tags: List<Tag>,
    isDragging: Boolean,
    reorderEnabled: Boolean,
    dragHandleModifier: Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "siteCardElevation",
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (reorderEnabled) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.drag_handle),
                    modifier = dragHandleModifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = site.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        tags.take(MAX_VISIBLE_TAGS).forEach { tag ->
                            TagBadge(
                                tag = tag,
                                modifier = Modifier.widthIn(max = 140.dp),
                            )
                        }
                        if (tags.size > MAX_VISIBLE_TAGS) {
                            Text(
                                text = "+${tags.size - MAX_VISIBLE_TAGS}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (site.favorite) {
                        Icons.Default.Star
                    } else {
                        Icons.Default.StarBorder
                    },
                    contentDescription = stringResource(
                        if (site.favorite) R.string.remove_favorite else R.string.add_favorite,
                    ),
                    tint = if (site.favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Box {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more),
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    hint: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hint != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SiteEditorDialog(
    title: String,
    initialSite: Site?,
    tags: List<Tag>,
    store: SiteStore,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String, tagIds: List<String>) -> Unit,
) {
    var name by rememberSaveable(initialSite?.id) { mutableStateOf(initialSite?.name.orEmpty()) }
    var url by rememberSaveable(initialSite?.id) { mutableStateOf(initialSite?.url.orEmpty()) }
    var tagIds by remember(initialSite?.id) { mutableStateOf(initialSite?.tagIds.orEmpty()) }
    var showTagPicker by remember { mutableStateOf(false) }

    val normalizedUrl = remember(url) { UrlNormalizer.normalize(url) }
    val urlInvalid = url.isNotBlank() && normalizedUrl == null
    val canSave = name.isNotBlank() && normalizedUrl != null
    val selectedTags = tags.filter { it.id in tagIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.site_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.site_url)) },
                    singleLine = true,
                    isError = urlInvalid,
                    supportingText = {
                        Text(
                            if (urlInvalid) {
                                stringResource(R.string.invalid_url)
                            } else {
                                stringResource(R.string.url_hint)
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSave) {
                                onSave(name.trim(), normalizedUrl!!, tagIds)
                            }
                        },
                    ),
                )

                Text(
                    text = stringResource(R.string.tags),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedTags.forEach { tag ->
                        TagBadge(
                            tag = tag,
                            modifier = Modifier.widthIn(max = 160.dp),
                        )
                    }
                    AssistChip(
                        onClick = { showTagPicker = true },
                        label = { Text(stringResource(R.string.select_tags)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), normalizedUrl!!, tagIds) },
                enabled = canSave,
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (showTagPicker) {
        TagPickerDialog(
            tags = tags,
            initialSelectedIds = tagIds,
            onDismiss = { showTagPicker = false },
            onConfirm = { selected ->
                tagIds = selected
                showTagPicker = false
            },
            onAdd = { tagName, colorIndex -> store.addTag(tagName, colorIndex) },
            onUpdate = { tag -> store.updateTag(tag) },
            onDelete = { id -> store.deleteTag(id) },
            onMove = { from, to -> store.moveTag(from, to) },
        )
    }
}

private enum class HomeFilterType {
    ALL,
    FAVORITES,
    TAG,
}

private data class SortMenuEntry(
    val mode: SiteSortMode,
    @param:StringRes val labelRes: Int,
)

private val SortMenuEntries = listOf(
    SortMenuEntry(SiteSortMode.CUSTOM, R.string.sort_custom),
    SortMenuEntry(SiteSortMode.NAME_ASC, R.string.sort_name_asc),
    SortMenuEntry(SiteSortMode.NAME_DESC, R.string.sort_name_desc),
    SortMenuEntry(SiteSortMode.LAST_USED_DESC, R.string.sort_last_used_desc),
    SortMenuEntry(SiteSortMode.LAST_USED_ASC, R.string.sort_last_used_asc),
    SortMenuEntry(SiteSortMode.CREATED_DESC, R.string.sort_created_desc),
    SortMenuEntry(SiteSortMode.CREATED_ASC, R.string.sort_created_asc),
)

private const val MAX_VISIBLE_TAGS = 3

private val IMPORT_MIME_TYPES = arrayOf(
    "application/json",
    "text/plain",
    "application/octet-stream",
)
