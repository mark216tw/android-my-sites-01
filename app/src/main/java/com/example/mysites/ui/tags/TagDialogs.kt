package com.example.mysites.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mysites.R
import com.example.mysites.data.TAG_COLOR_COUNT
import com.example.mysites.data.Tag
import com.example.mysites.ui.theme.tagColor
import com.example.mysites.ui.theme.tagOnColor
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TagDot(
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tagColor(colorIndex)),
    )
}

@Composable
fun TagBadge(
    tag: Tag,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tagColor(tag.colorIndex).copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TagDot(colorIndex = tag.colorIndex, size = 8.dp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditorDialog(
    tag: Tag?,
    existingTags: List<Tag>,
    onDismiss: () -> Unit,
    onSave: (name: String, colorIndex: Int) -> Unit,
) {
    var name by rememberSaveable(tag?.id) { mutableStateOf(tag?.name.orEmpty()) }
    var colorIndex by rememberSaveable(tag?.id) { mutableStateOf(tag?.colorIndex ?: 0) }

    val trimmedName = name.trim()
    val duplicated = existingTags.any { other ->
        other.id != tag?.id && other.name.equals(trimmedName, ignoreCase = true)
    }
    val canSave = trimmedName.isNotEmpty() && !duplicated

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (tag == null) R.string.add_tag else R.string.edit_tag,
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    isError = duplicated,
                    supportingText = {
                        if (duplicated) {
                            Text(stringResource(R.string.tag_name_duplicate))
                        }
                    },
                )

                Text(
                    text = stringResource(R.string.tag_color),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 8,
                ) {
                    repeat(TAG_COLOR_COUNT) { index ->
                        val colorName = stringResource(R.string.tag_color_number, index + 1)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { colorIndex = index }
                                .semantics { contentDescription = colorName },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(tagColor(index))
                                    .border(
                                        width = if (colorIndex == index) 3.dp else 1.dp,
                                        color = if (colorIndex == index) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (colorIndex == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = tagOnColor(index),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(trimmedName, colorIndex) },
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

@Composable
fun TagManagerDialog(
    tags: List<Tag>,
    onDismiss: () -> Unit,
    onAdd: (name: String, colorIndex: Int) -> Tag,
    onUpdate: (Tag) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var deletingTag by remember { mutableStateOf<Tag?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.tag_management),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { showAdd = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_tag),
                        )
                    }
                }

                if (tags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_tags),
                        modifier = Modifier.padding(vertical = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TagReorderableList(
                        tags = tags,
                        onEdit = { editingTag = it },
                        onDelete = { deletingTag = it },
                        onMove = onMove,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }

    if (showAdd) {
        TagEditorDialog(
            tag = null,
            existingTags = tags,
            onDismiss = { showAdd = false },
            onSave = { name, colorIndex ->
                onAdd(name, colorIndex)
                showAdd = false
            },
        )
    }

    editingTag?.let { tag ->
        TagEditorDialog(
            tag = tag,
            existingTags = tags,
            onDismiss = { editingTag = null },
            onSave = { name, colorIndex ->
                onUpdate(tag.copy(name = name, colorIndex = colorIndex))
                editingTag = null
            },
        )
    }

    deletingTag?.let { tag ->
        AlertDialog(
            onDismissRequest = { deletingTag = null },
            title = { Text(stringResource(R.string.delete_tag_title)) },
            text = { Text(stringResource(R.string.delete_tag_message, tag.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(tag.id)
                        deletingTag = null
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTag = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun TagReorderableList(
    tags: List<Tag>,
    onEdit: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        state = lazyListState,
    ) {
        itemsIndexed(tags, key = { _, tag -> tag.id }) { _, tag ->
            ReorderableItem(reorderableState, key = tag.id) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = stringResource(R.string.drag_handle),
                        modifier = Modifier
                            .draggableHandle()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TagDot(colorIndex = tag.colorIndex, size = 14.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tag.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { onEdit(tag) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_tag),
                        )
                    }
                    IconButton(onClick = { onDelete(tag) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_tag),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagPickerDialog(
    tags: List<Tag>,
    initialSelectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    onAdd: (name: String, colorIndex: Int) -> Tag,
    onUpdate: (Tag) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    var selectedIds by remember(initialSelectedIds) {
        mutableStateOf(initialSelectedIds.toSet())
    }
    var showAdd by remember { mutableStateOf(false) }
    var showManager by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_tags)) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showAdd = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_tag))
                    }
                    TextButton(onClick = { showManager = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.manage_tags))
                    }
                }

                if (tags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_tags),
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        itemsIndexed(tags, key = { _, tag -> tag.id }) { _, tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (tag.id in selectedIds) {
                                            selectedIds - tag.id
                                        } else {
                                            selectedIds + tag.id
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = tag.id in selectedIds,
                                    onCheckedChange = {
                                        selectedIds = if (tag.id in selectedIds) {
                                            selectedIds - tag.id
                                        } else {
                                            selectedIds + tag.id
                                        }
                                    },
                                )
                                TagDot(colorIndex = tag.colorIndex, size = 14.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val existingIds = tags.mapTo(mutableSetOf()) { it.id }
                    onConfirm(selectedIds.filter { it in existingIds })
                },
            ) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (showAdd) {
        TagEditorDialog(
            tag = null,
            existingTags = tags,
            onDismiss = { showAdd = false },
            onSave = { name, colorIndex ->
                val added = onAdd(name, colorIndex)
                selectedIds = selectedIds + added.id
                showAdd = false
            },
        )
    }

    if (showManager) {
        TagManagerDialog(
            tags = tags,
            onDismiss = { showManager = false },
            onAdd = onAdd,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onMove = onMove,
        )
    }
}
