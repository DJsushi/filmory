package dev.gaens.filmory.feature.collection.presentation.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.painterResource
import dev.gaens.filmory.feature.collection.presentation.CollectionIconView
import dev.gaens.filmory.feature.collection.presentation.CollectionLook
import dev.gaens.filmory.feature.collection.presentation.collectionBackground
import dev.gaens.filmory.feature.collection.presentation.collectionContentColor
import dev.gaens.filmory.resources.collection_appearance_open
import dev.gaens.filmory.resources.ic_folder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.ScreenHeader
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.feature.library.presentation.LibraryContentRoot
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_more
import dev.gaens.filmory.resources.collection_delete
import dev.gaens.filmory.resources.collection_delete_message
import dev.gaens.filmory.resources.collection_rename
import dev.gaens.filmory.resources.collection_rename_hint
import dev.gaens.filmory.resources.ic_more
import dev.gaens.filmory.resources.title_count
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * One collection: its own header (editable name and menu) over the shared title list. The header is
 * looked after by [CollectionDetailViewModel], the list by the library view model.
 */
@Composable
fun CollectionDetailScreenRoot(
    collectionId: String,
    isNew: Boolean,
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit,
    onAddTitleClick: () -> Unit,
    viewModel: CollectionDetailViewModel = koinViewModel { parametersOf(collectionId, isNew) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CollectionDetailEvent.ShowRenameHint -> scope.launch {
                snackbarHostState.showSnackbar(getString(Res.string.collection_rename_hint))
            }
            CollectionDetailEvent.Deleted -> onBackClick()
        }
    }
    Box(Modifier.fillMaxSize()) {
        LibraryContentRoot(LibraryPreset.COLLECTION, collectionId, onTitleClick, onAddTitleClick) { list, listActions ->
            ScreenHeader(
                title = state.name.orEmpty(),
                subtitle = pluralStringResource(Res.plurals.title_count, list.items.size, list.items.size),
                onBackClick = onBackClick,
                actions = {
                    listActions()
                    CollectionMenu(state.isDefault, viewModel::onAction)
                },
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CollectionBadge(state.look, state.name != null) { viewModel.onAction(CollectionDetailAction.OnAppearanceClick) }
                        Box(Modifier.weight(1f)) {
                            CollectionTitle(
                                name = state.name.orEmpty(),
                                isEditing = state.isEditingName && state.name != null,
                                onAction = viewModel::onAction,
                            )
                        }
                    }
                },
            )
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp))
    }
    if (state.isAppearanceSheetOpen) {
        CollectionAppearanceSheet(collectionId, state.name.orEmpty(), state.look, viewModel::onAction)
    }
    if (state.isDeleteDialogOpen) {
        ConfirmDialog(
            title = stringResource(Res.string.collection_delete),
            message = stringResource(Res.string.collection_delete_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { viewModel.onAction(CollectionDetailAction.OnDeleteConfirm) },
            onDismiss = { viewModel.onAction(CollectionDetailAction.OnDeleteDismiss) },
        )
    }
}

/** The collection's icon on its color, in front of the name. Tapping it opens the icon and color sheet. */
@Composable
private fun CollectionBadge(look: CollectionLook, isEnabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val contentColor = collectionContentColor(look)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(collectionBackground(look, ""))
            .clickable(enabled = isEnabled, onClickLabel = stringResource(Res.string.collection_appearance_open), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (look.iconKey != null) {
            CollectionIconView(look, contentColor, 28.dp)
        } else {
            Icon(painterResource(Res.drawable.ic_folder), null, Modifier.size(26.dp), contentColor)
        }
    }
}

/**
 * The collection's name as the screen headline. A long press turns it into a text field with all of
 * the text selected; a short tap only asks to press and hold, so a stray tap cannot start editing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectionTitle(name: String, isEditing: Boolean, onAction: (CollectionDetailAction) -> Unit) {
    val style = MaterialTheme.typography.headlineLarge
    if (!isEditing) {
        Text(
            text = name,
            style = style,
            modifier = Modifier.combinedClickable(
                onLongClickLabel = stringResource(Res.string.collection_rename),
                onLongClick = { onAction(CollectionDetailAction.OnEditNameStart) },
                onClick = { onAction(CollectionDetailAction.OnNameTap) },
            ),
        )
        return
    }
    val focusRequester = remember { FocusRequester() }
    var field by remember { mutableStateOf(TextFieldValue(name, selection = TextRange(0, name.length))) }
    var hadFocus by remember { mutableStateOf(false) }
    val underline = MaterialTheme.colorScheme.primary
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BasicTextField(
        value = field,
        onValueChange = { field = it },
        textStyle = style.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(underline),
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAction(CollectionDetailAction.OnNameCommit(field.text)) }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                // Leaving the field by any other way than Done saves it too.
                if (it.isFocused) hadFocus = true else if (hadFocus) onAction(CollectionDetailAction.OnNameCommit(field.text))
            }
            .drawBehind {
                val y = size.height + 2.dp.toPx()
                drawLine(underline, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
            },
    )
}

@Composable
private fun CollectionMenu(isDefault: Boolean, onAction: (CollectionDetailAction) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryIconButton(Res.drawable.ic_more, stringResource(Res.string.action_more), { isMenuOpen = true })
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.collection_rename)) },
                onClick = {
                    isMenuOpen = false
                    onAction(CollectionDetailAction.OnEditNameStart)
                },
            )
            // Favorites is the one collection the app relies on, so it cannot be deleted.
            if (!isDefault) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.collection_delete)) },
                    onClick = {
                        isMenuOpen = false
                        onAction(CollectionDetailAction.OnDeleteClick)
                    },
                )
            }
        }
    }
}
