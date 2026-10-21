package dev.gaens.filmory.feature.collection.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.ScreenHeader
import dev.gaens.filmory.core.presentation.designsystem.components.TextInputDialog
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.feature.library.presentation.LibraryContentRoot
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_more
import dev.gaens.filmory.resources.collection_delete
import dev.gaens.filmory.resources.collection_delete_message
import dev.gaens.filmory.resources.collection_name
import dev.gaens.filmory.resources.collection_rename
import dev.gaens.filmory.resources.collections_default_name
import dev.gaens.filmory.resources.ic_more
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * One collection: its own header (name and menu) over the shared title list. The header is looked
 * after by [CollectionDetailViewModel], the list by the library view model.
 */
@Composable
fun CollectionDetailScreenRoot(
    collectionId: String,
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit,
    onAddTitleClick: () -> Unit,
    viewModel: CollectionDetailViewModel = koinViewModel { parametersOf(collectionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CollectionDetailEvent.Deleted -> onBackClick()
        }
    }
    LibraryContentRoot(LibraryPreset.COLLECTION, collectionId, onTitleClick, onAddTitleClick) { list, listActions ->
        ScreenHeader(
            title = if (state.isDefault) stringResource(Res.string.collections_default_name) else state.name.orEmpty(),
            subtitle = pluralStringResource(Res.plurals.title_count, list.items.size, list.items.size),
            onBackClick = onBackClick,
            actions = {
                listActions()
                if (!state.isDefault) CollectionMenu { viewModel.onAction(CollectionDetailAction.OnDialogOpen(it)) }
            },
        )
    }
    CollectionDialogs(state, viewModel::onAction)
}

@Composable
private fun CollectionMenu(onOpen: (CollectionDetailDialog) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryIconButton(Res.drawable.ic_more, stringResource(Res.string.action_more), { isMenuOpen = true })
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.collection_rename)) },
                onClick = {
                    isMenuOpen = false
                    onOpen(CollectionDetailDialog.RENAME)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.collection_delete)) },
                onClick = {
                    isMenuOpen = false
                    onOpen(CollectionDetailDialog.DELETE)
                },
            )
        }
    }
}

@Composable
private fun CollectionDialogs(state: CollectionDetailState, onAction: (CollectionDetailAction) -> Unit) {
    val dismiss = { onAction(CollectionDetailAction.OnDialogDismiss) }
    when (state.dialog) {
        CollectionDetailDialog.RENAME -> TextInputDialog(
            title = stringResource(Res.string.collection_rename),
            label = stringResource(Res.string.collection_name),
            initialText = state.name.orEmpty(),
            onConfirm = { onAction(CollectionDetailAction.OnRenameConfirm(it)) },
            onDismiss = dismiss,
        )
        CollectionDetailDialog.DELETE -> ConfirmDialog(
            title = stringResource(Res.string.collection_delete),
            message = stringResource(Res.string.collection_delete_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { onAction(CollectionDetailAction.OnDeleteConfirm) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}
