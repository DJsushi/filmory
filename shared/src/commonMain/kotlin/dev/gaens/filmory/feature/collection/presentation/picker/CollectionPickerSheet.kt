package dev.gaens.filmory.feature.collection.presentation.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.TextInputDialog
import dev.gaens.filmory.feature.collection.presentation.CollectionIconView
import dev.gaens.filmory.feature.collection.presentation.CollectionLook
import dev.gaens.filmory.feature.collection.presentation.collectionBackground
import dev.gaens.filmory.feature.collection.presentation.collectionContentColor
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.action_edit
import dev.gaens.filmory.resources.collection_delete
import dev.gaens.filmory.resources.collection_delete_message
import dev.gaens.filmory.resources.collection_name
import dev.gaens.filmory.resources.collection_new_name
import dev.gaens.filmory.resources.collection_picker_drag
import dev.gaens.filmory.resources.collection_picker_edit_title
import dev.gaens.filmory.resources.collection_picker_pin
import dev.gaens.filmory.resources.collection_picker_unpin
import dev.gaens.filmory.resources.collection_rename
import dev.gaens.filmory.resources.collections_add
import dev.gaens.filmory.resources.detail_collections_title
import dev.gaens.filmory.resources.ic_add
import dev.gaens.filmory.resources.ic_delete
import dev.gaens.filmory.resources.ic_drag_handle
import dev.gaens.filmory.resources.ic_folder
import dev.gaens.filmory.resources.ic_pin
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val HEADER_KEY = "header"
private const val ADD_KEY = "add"

/**
 * Sheet to put a title into collections. Its Edit button switches to management mode, where
 * collections can be dragged into order, pinned, renamed, deleted and added.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionPickerSheet(
    titleId: String,
    onDismiss: () -> Unit,
    viewModel: CollectionPickerViewModel = koinViewModel { parametersOf(titleId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        CollectionPickerContent(state, viewModel::onAction)
    }
    CollectionPickerDialogs(state, viewModel::onAction)
}

@Composable
private fun CollectionPickerContent(state: CollectionPickerState, onAction: (CollectionPickerAction) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        onAction(CollectionPickerAction.OnMove(from.key as? String ?: return@rememberReorderableLazyListState, to.key as? String ?: return@rememberReorderableLazyListState))
        haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    LazyColumn(state = listState, modifier = Modifier.padding(bottom = 24.dp)) {
        item(key = HEADER_KEY) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding, end = 12.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(if (state.isEditing) Res.string.collection_picker_edit_title else Res.string.detail_collections_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onAction(if (state.isEditing) CollectionPickerAction.OnEditDone else CollectionPickerAction.OnEditClick) }) {
                    Text(stringResource(if (state.isEditing) Res.string.action_done else Res.string.action_edit))
                }
            }
        }
        items(state.collections, key = { it.id }) { collection ->
            ReorderableItem(reorderableState, key = collection.id) { isDragging ->
                val lift = if (isDragging) 1.03f else 1f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = lift
                            scaleY = lift
                        }
                        .background(if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.isEditing) {
                        val handle = Modifier.draggableHandle(onDragStopped = { onAction(CollectionPickerAction.OnMoveDropped) })
                        EditRow(collection, handle, onAction)
                    } else {
                        PickRow(collection, onAction)
                    }
                }
            }
        }
        if (state.isEditing) {
            item(key = ADD_KEY) {
                val newName = stringResource(Res.string.collection_new_name)
                TextButton(
                    onClick = { onAction(CollectionPickerAction.OnAddClick(newName)) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_add), null, Modifier.size(20.dp))
                    Text(stringResource(Res.string.collections_add), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.PickRow(collection: CollectionPickerItemUi, onAction: (CollectionPickerAction) -> Unit) {
    Checkbox(checked = collection.containsTitle, onCheckedChange = { onAction(CollectionPickerAction.OnToggle(collection.id)) })
    Badge(collection.look)
    Text(
        text = collection.name,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f).padding(start = 12.dp),
    )
    if (collection.isPinned) {
        Icon(painterResource(Res.drawable.ic_pin), null, Modifier.padding(end = 12.dp).size(18.dp), MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RowScope.EditRow(
    collection: CollectionPickerItemUi,
    dragHandle: Modifier,
    onAction: (CollectionPickerAction) -> Unit,
) {
    IconButton(onClick = {}, modifier = dragHandle) {
        Icon(painterResource(Res.drawable.ic_drag_handle), stringResource(Res.string.collection_picker_drag), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Badge(collection.look)
    Text(
        text = collection.name,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .weight(1f)
            .clickable(onClickLabel = stringResource(Res.string.collection_rename)) { onAction(CollectionPickerAction.OnRenameClick(collection.id)) }
            .padding(horizontal = 12.dp, vertical = 14.dp),
    )
    IconButton(onClick = { onAction(CollectionPickerAction.OnPinClick(collection.id)) }) {
        Icon(
            painter = painterResource(Res.drawable.ic_pin),
            contentDescription = stringResource(if (collection.isPinned) Res.string.collection_picker_unpin else Res.string.collection_picker_pin),
            tint = if (collection.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    // Favorites is the one collection the app relies on, so it has no delete button.
    if (!collection.isDefault) {
        IconButton(onClick = { onAction(CollectionPickerAction.OnDeleteClick(collection.id)) }) {
            Icon(painterResource(Res.drawable.ic_delete), stringResource(Res.string.collection_delete), tint = MaterialTheme.colorScheme.error)
        }
    } else {
        Box(Modifier.size(48.dp))
    }
}

@Composable
private fun Badge(look: CollectionLook) {
    val contentColor = collectionContentColor(look)
    Box(
        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(collectionBackground(look, "")),
        contentAlignment = Alignment.Center,
    ) {
        if (look.iconKey != null) {
            CollectionIconView(look, contentColor, 22.dp)
        } else {
            Icon(painterResource(Res.drawable.ic_folder), null, Modifier.size(20.dp), contentColor)
        }
    }
}

@Composable
private fun CollectionPickerDialogs(state: CollectionPickerState, onAction: (CollectionPickerAction) -> Unit) {
    val dismiss = { onAction(CollectionPickerAction.OnDialogDismiss) }
    when (val dialog = state.dialog) {
        is CollectionPickerDialog.Rename -> TextInputDialog(
            title = stringResource(Res.string.collection_rename),
            label = stringResource(Res.string.collection_name),
            initialText = dialog.name,
            onConfirm = { onAction(CollectionPickerAction.OnRenameConfirm(it)) },
            onDismiss = dismiss,
        )
        is CollectionPickerDialog.Delete -> ConfirmDialog(
            title = stringResource(Res.string.collection_delete),
            message = stringResource(Res.string.collection_delete_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { onAction(CollectionPickerAction.OnDeleteConfirm) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}
