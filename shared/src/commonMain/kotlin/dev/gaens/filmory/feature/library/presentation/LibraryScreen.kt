package dev.gaens.filmory.feature.library.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.spanParentPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryFab
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.ScreenHeader
import dev.gaens.filmory.core.presentation.designsystem.components.TextInputDialog
import dev.gaens.filmory.core.presentation.designsystem.components.TitleGridCell
import dev.gaens.filmory.core.presentation.designsystem.components.TitleListRow
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.settings.domain.LibraryLayout
import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_more
import dev.gaens.filmory.resources.action_close
import dev.gaens.filmory.resources.action_search
import dev.gaens.filmory.resources.ic_close
import dev.gaens.filmory.resources.collection_delete
import dev.gaens.filmory.resources.collection_delete_message
import dev.gaens.filmory.resources.collection_name
import dev.gaens.filmory.resources.collection_rename
import dev.gaens.filmory.resources.collections_default_name
import dev.gaens.filmory.resources.ic_more
import dev.gaens.filmory.resources.ic_search
import dev.gaens.filmory.resources.ic_tune
import dev.gaens.filmory.resources.library_add_title
import dev.gaens.filmory.resources.library_all_statuses
import dev.gaens.filmory.resources.library_dropped_title
import dev.gaens.filmory.resources.library_empty
import dev.gaens.filmory.resources.library_movies
import dev.gaens.filmory.resources.library_on_hold_title
import dev.gaens.filmory.resources.library_search_hint
import dev.gaens.filmory.resources.library_series
import dev.gaens.filmory.resources.library_settings
import dev.gaens.filmory.resources.library_sort
import dev.gaens.filmory.resources.library_sort_added
import dev.gaens.filmory.resources.library_sort_name
import dev.gaens.filmory.resources.library_sort_rating
import dev.gaens.filmory.resources.library_sort_updated
import dev.gaens.filmory.resources.library_status_all
import dev.gaens.filmory.resources.library_status_filter
import dev.gaens.filmory.resources.library_title
import dev.gaens.filmory.resources.library_wishlist_title
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LibraryScreenRoot(
    preset: LibraryPreset,
    collectionId: String?,
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit,
    onAddTitleClick: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel { parametersOf(preset, collectionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LibraryEvent.CollectionDeleted -> onBackClick()
        }
    }
    LibraryScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LibraryAction.OnBackClick -> onBackClick()
                is LibraryAction.OnTitleClick -> onTitleClick(action.titleId)
                LibraryAction.OnAddTitleClick -> onAddTitleClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

private val FabClearance = PaddingValues(bottom = 120.dp)

@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
) {
    val isGrid = state.settings.layout == LibraryLayout.GRID
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isGrid) 3 else 1),
            contentPadding = PaddingValues(horizontal = if (isGrid) ScreenPadding else 0.dp) + FabClearance,
            verticalArrangement = Arrangement.spacedBy(if (isGrid) 18.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
                Column(if (isGrid) Modifier.spanParentPadding() else Modifier) { LibraryHeader(state, onAction) }
            }
            if (state.items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    Text(
                        text = stringResource(Res.string.library_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = if (isGrid) 0.dp else ScreenPadding, vertical = 24.dp),
                    )
                }
            }
            itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                val status = item.status.takeIf { state.settings.showStatusIcon }
                val stars = item.stars.takeIf { state.settings.showStars }
                if (isGrid) {
                    TitleGridCell(
                        id = item.id,
                        name = item.name,
                        posterUrl = item.posterUrl,
                        status = status,
                        stars = stars,
                        onClick = { onAction(LibraryAction.OnTitleClick(item.id)) },
                        modifier = Modifier.rise(index + 3),
                    )
                } else {
                    TitleListRow(
                        id = item.id,
                        name = item.name,
                        details = listOfNotNull(item.type.label(), item.year?.toString()).joinToString(" · "),
                        posterUrl = item.posterUrl,
                        status = status,
                        stars = stars,
                        onClick = { onAction(LibraryAction.OnTitleClick(item.id)) },
                        modifier = Modifier.rise(index + 3),
                    )
                }
            }
        }
        FilmoryFab(
            text = stringResource(Res.string.library_add_title),
            onClick = { onAction(LibraryAction.OnAddTitleClick) },
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp),
        )
    }
    LibraryDialogs(state, onAction)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LibraryHeader(state: LibraryState, onAction: (LibraryAction) -> Unit) {
    ScreenHeader(
        title = state.headline(),
        subtitle = pluralStringResource(Res.plurals.title_count, state.items.size, state.items.size),
        onBackClick = { onAction(LibraryAction.OnBackClick) },
        actions = {
            FilmoryIconButton(Res.drawable.ic_search, stringResource(Res.string.action_search), {
                onAction(LibraryAction.OnSearchClick)
            })
            FilmoryIconButton(Res.drawable.ic_tune, stringResource(Res.string.library_settings), {
                onAction(LibraryAction.OnDialogOpen(LibraryDialog.SETTINGS))
            })
            if (state.canEditCollection) CollectionMenu(onAction)
        },
    )
    if (state.isSearchOpen) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        // Back closes the search first, then leaves the screen.
        BackHandler { onAction(LibraryAction.OnSearchClick) }
        OutlinedTextField(
            value = state.searchText,
            onValueChange = { onAction(LibraryAction.OnSearchTextChange(it)) },
            placeholder = { Text(stringResource(Res.string.library_search_hint)) },
            trailingIcon = {
                FilmoryIconButton(Res.drawable.ic_close, stringResource(Res.string.action_close), {
                    onAction(LibraryAction.OnSearchClick)
                })
            },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 8.dp).focusRequester(focusRequester),
        )
    }
    ChipRow(Modifier.padding(top = 12.dp, bottom = 12.dp).rise(2)) {
        if (state.canFilterStatus) StatusFilterChip(state.statusFilter, onAction)
        FilmoryChip(
            label = stringResource(Res.string.library_movies),
            selected = state.typeFilter == TitleType.MOVIE,
            onClick = { onAction(LibraryAction.OnTypeFilterClick(TitleType.MOVIE)) },
        )
        FilmoryChip(
            label = stringResource(Res.string.library_series),
            selected = state.typeFilter == TitleType.SERIES,
            onClick = { onAction(LibraryAction.OnTypeFilterClick(TitleType.SERIES)) },
        )
        SortChip(state.settings.sort, onAction)
    }
}

@Composable
private fun LibraryState.headline(): String = when (preset) {
    LibraryPreset.ALL -> stringResource(Res.string.library_title)
    LibraryPreset.WISHLIST -> stringResource(Res.string.library_wishlist_title)
    LibraryPreset.ON_HOLD -> stringResource(Res.string.library_on_hold_title)
    LibraryPreset.DROPPED -> stringResource(Res.string.library_dropped_title)
    LibraryPreset.COLLECTION ->
        if (isDefaultCollection) stringResource(Res.string.collections_default_name) else collectionName.orEmpty()
}

@Composable
private fun StatusFilterChip(selected: TitleStatus?, onAction: (LibraryAction) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryChip(
            label = if (selected == null) {
                stringResource(Res.string.library_status_all)
            } else {
                stringResource(Res.string.library_status_filter, selected.label())
            },
            selected = true,
            onClick = { isMenuOpen = true },
        )
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_all_statuses)) },
                onClick = {
                    isMenuOpen = false
                    onAction(LibraryAction.OnStatusFilterChange(null))
                },
            )
            TitleStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label()) },
                    onClick = {
                        isMenuOpen = false
                        onAction(LibraryAction.OnStatusFilterChange(status))
                    },
                )
            }
        }
    }
}

@Composable
private fun SortChip(sort: LibrarySort, onAction: (LibraryAction) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryChip(
            label = stringResource(Res.string.library_sort, sort.label()),
            selected = false,
            onClick = { isMenuOpen = true },
        )
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            LibrarySort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        isMenuOpen = false
                        onAction(LibraryAction.OnSortChange(option))
                    },
                )
            }
        }
    }
}

@Composable
private fun LibrarySort.label(): String = stringResource(
    when (this) {
        LibrarySort.UPDATED -> Res.string.library_sort_updated
        LibrarySort.ADDED -> Res.string.library_sort_added
        LibrarySort.NAME -> Res.string.library_sort_name
        LibrarySort.RATING -> Res.string.library_sort_rating
    },
)

@Composable
private fun CollectionMenu(onAction: (LibraryAction) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryIconButton(Res.drawable.ic_more, stringResource(Res.string.action_more), { isMenuOpen = true })
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.collection_rename)) },
                onClick = {
                    isMenuOpen = false
                    onAction(LibraryAction.OnDialogOpen(LibraryDialog.RENAME_COLLECTION))
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.collection_delete)) },
                onClick = {
                    isMenuOpen = false
                    onAction(LibraryAction.OnDialogOpen(LibraryDialog.DELETE_COLLECTION))
                },
            )
        }
    }
}

@Composable
private fun LibraryDialogs(state: LibraryState, onAction: (LibraryAction) -> Unit) {
    val dismiss = { onAction(LibraryAction.OnDialogDismiss) }
    when (state.dialog) {
        LibraryDialog.SETTINGS -> LibrarySettingsSheet(
            settings = state.settings,
            onChange = { onAction(LibraryAction.OnSettingsChange(it)) },
            onDismiss = dismiss,
        )
        LibraryDialog.RENAME_COLLECTION -> TextInputDialog(
            title = stringResource(Res.string.collection_rename),
            label = stringResource(Res.string.collection_name),
            initialText = state.collectionName.orEmpty(),
            onConfirm = { onAction(LibraryAction.OnRenameCollection(it)) },
            onDismiss = dismiss,
        )
        LibraryDialog.DELETE_COLLECTION -> ConfirmDialog(
            title = stringResource(Res.string.collection_delete),
            message = stringResource(Res.string.collection_delete_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { onAction(LibraryAction.OnDeleteCollection) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}

@Preview
@Composable
private fun LibraryScreenPreview() {
    FilmoryTheme {
        LibraryScreen(
            state = LibraryState(
                items = listOf(
                    LibraryItemUi("1", "Severance", null, TitleType.SERIES, 2022, TitleStatus.WATCHING, 4.5f),
                    LibraryItemUi("2", "Arrival", null, TitleType.MOVIE, 2016, TitleStatus.WATCHED, 4.5f),
                    LibraryItemUi("3", "Andor", null, TitleType.SERIES, 2022, TitleStatus.ON_HOLD, null),
                ),
            ),
            onAction = {},
        )
    }
}
