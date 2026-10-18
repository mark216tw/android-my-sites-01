package com.example.mysites.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.example.mysites.data.Site
import com.example.mysites.data.SiteBackupCodec
import com.example.mysites.data.SiteStore
import com.example.mysites.data.UrlNormalizer
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
    onOpenSite: (Site) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val sites by store.sites.collectAsStateWithLifecycle()
    val loaded by store.loaded.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var query by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingSite by remember { mutableStateOf<Site?>(null) }
    var deletingSite by remember { mutableStateOf<Site?>(null) }
    var pendingImport by remember { mutableStateOf<List<Site>?>(null) }

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
                    context.getString(R.string.export_success, store.sites.value.size)
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
                    imported.isEmpty() -> showMessage(context.getString(R.string.import_empty))
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
                    IconButton(onClick = { importLauncher.launch(IMPORT_MIME_TYPES) }) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = stringResource(R.string.import_json),
                        )
                    }
                    IconButton(
                        onClick = {
                            exportLauncher.launch("my-sites-${LocalDate.now()}.json")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = stringResource(R.string.export),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_site),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

                else -> {
                    val filteredSites = if (query.isBlank()) {
                        sites
                    } else {
                        sites.filter { site ->
                            site.name.contains(query, ignoreCase = true) ||
                                site.url.contains(query, ignoreCase = true)
                        }
                    }

                    if (filteredSites.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.no_results),
                            hint = null,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        SiteList(
                            sites = filteredSites,
                            reorderEnabled = query.isBlank(),
                            onOpenSite = onOpenSite,
                            onEditSite = { editingSite = it },
                            onDeleteSite = { deletingSite = it },
                            onMove = { from, to -> store.move(from, to) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SiteEditorDialog(
            title = stringResource(R.string.add_site),
            initialSite = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, url ->
                store.add(name, url)
                showAddDialog = false
            },
        )
    }

    editingSite?.let { site ->
        SiteEditorDialog(
            title = stringResource(R.string.edit_site),
            initialSite = site,
            onDismiss = { editingSite = null },
            onSave = { name, url ->
                store.update(site.copy(name = name, url = url))
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
                        imported.size,
                        sites.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.replaceAll(imported)
                        pendingImport = null
                        showMessage(context.getString(R.string.import_success, imported.size))
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
    reorderEnabled: Boolean,
    onOpenSite: (Site) -> Unit,
    onEditSite: (Site) -> Unit,
    onDeleteSite: (Site) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (reorderEnabled) {
            onMove(from.index, to.index)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(sites, key = { _, site -> site.id }) { _, site ->
            ReorderableItem(reorderableState, key = site.id) { isDragging ->
                SiteRow(
                    site = site,
                    isDragging = isDragging,
                    reorderEnabled = reorderEnabled,
                    dragHandleModifier = Modifier.draggableHandle(),
                    onClick = { onOpenSite(site) },
                    onEdit = { onEditSite(site) },
                    onDelete = { onDeleteSite(site) },
                )
            }
        }
    }
}

@Composable
@Suppress("ModifierParameter")
private fun SiteRow(
    site: Site,
    isDragging: Boolean,
    reorderEnabled: Boolean,
    dragHandleModifier: Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "siteCardElevation",
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun SiteEditorDialog(
    title: String,
    initialSite: Site?,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String) -> Unit,
) {
    var name by rememberSaveable(initialSite?.id) { mutableStateOf(initialSite?.name.orEmpty()) }
    var url by rememberSaveable(initialSite?.id) { mutableStateOf(initialSite?.url.orEmpty()) }

    val normalizedUrl = remember(url) { UrlNormalizer.normalize(url) }
    val urlInvalid = url.isNotBlank() && normalizedUrl == null
    val canSave = name.isNotBlank() && normalizedUrl != null

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
                                onSave(name.trim(), normalizedUrl!!)
                            }
                        },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), normalizedUrl!!) },
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
}

private val IMPORT_MIME_TYPES = arrayOf(
    "application/json",
    "text/plain",
    "application/octet-stream",
)
