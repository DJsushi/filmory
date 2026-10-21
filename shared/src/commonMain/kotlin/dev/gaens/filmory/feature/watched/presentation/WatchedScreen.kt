package dev.gaens.filmory.feature.watched.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.bottomBarContentPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.components.TitleGridCell
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.spanParentPadding
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watched.domain.WatchedSort
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_search
import dev.gaens.filmory.resources.app_name
import dev.gaens.filmory.resources.ic_search
import dev.gaens.filmory.resources.library_movies
import dev.gaens.filmory.resources.library_search_hint
import dev.gaens.filmory.resources.library_series
import dev.gaens.filmory.resources.library_sort
import dev.gaens.filmory.resources.library_sort_name
import dev.gaens.filmory.resources.library_sort_rating
import dev.gaens.filmory.resources.watched_all
import dev.gaens.filmory.resources.watched_sort_finished
import dev.gaens.filmory.resources.watched_subtitle
import dev.gaens.filmory.resources.watched_subtitle_empty
import dev.gaens.filmory.resources.watched_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WatchedScreenRoot(
    onTitleClick: (String) -> Unit,
    viewModel: WatchedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WatchedScreen(
        state = state,
        onAction = { action ->
            if (action is WatchedAction.OnTitleClick) onTitleClick(action.titleId)
            viewModel.onAction(action)
        },
    )
}

@Composable
fun WatchedScreen(
    state: WatchedState,
    onAction: (WatchedAction) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = ScreenPadding) + bottomBarContentPadding(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
            Column(Modifier.spanParentPadding()) { WatchedHeader(state, onAction) }
        }
        itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
            TitleGridCell(
                id = item.id,
                name = item.name,
                posterUrl = item.posterUrl,
                stars = item.stars,
                onClick = { onAction(WatchedAction.OnTitleClick(item.id)) },
                modifier = Modifier.rise(index + 3),
            )
        }
    }
}

@Composable
private fun WatchedHeader(state: WatchedState, onAction: (WatchedAction) -> Unit) {
    HeroHeader {
        FilmoryAppBar(
            title = stringResource(Res.string.app_name),
            actions = {
                FilmoryIconButton(Res.drawable.ic_search, stringResource(Res.string.action_search), {
                    onAction(WatchedAction.OnSearchClick)
                })
            },
        )
        Text(
            text = stringResource(Res.string.watched_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp).rise(0),
        )
        Text(
            text = if (state.totalCount == 0) {
                stringResource(Res.string.watched_subtitle_empty)
            } else {
                pluralStringResource(Res.plurals.watched_subtitle, state.totalCount, state.totalCount)
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp).rise(1),
        )
    }
    if (state.isSearchOpen) {
        OutlinedTextField(
            value = state.searchText,
            onValueChange = { onAction(WatchedAction.OnSearchTextChange(it)) },
            placeholder = { Text(stringResource(Res.string.library_search_hint)) },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding, end = ScreenPadding, top = 12.dp),
        )
    }
    ChipRow(Modifier.padding(top = 12.dp, bottom = 4.dp).rise(2)) {
        FilmoryChip(
            label = stringResource(Res.string.watched_all),
            selected = state.typeFilter == null,
            showCheckWhenSelected = false,
            onClick = { onAction(WatchedAction.OnTypeFilterChange(null)) },
        )
        FilmoryChip(
            label = stringResource(Res.string.library_movies),
            selected = state.typeFilter == TitleType.MOVIE,
            showCheckWhenSelected = false,
            onClick = { onAction(WatchedAction.OnTypeFilterChange(TitleType.MOVIE)) },
        )
        FilmoryChip(
            label = stringResource(Res.string.library_series),
            selected = state.typeFilter == TitleType.SERIES,
            showCheckWhenSelected = false,
            onClick = { onAction(WatchedAction.OnTypeFilterChange(TitleType.SERIES)) },
        )
        SortChip(state.sort, onAction)
    }
}

@Composable
private fun SortChip(sort: WatchedSort, onAction: (WatchedAction) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryChip(stringResource(Res.string.library_sort, sort.label()), selected = false, onClick = { isOpen = true })
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }) {
            WatchedSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        isOpen = false
                        onAction(WatchedAction.OnSortChange(option))
                    },
                )
            }
        }
    }
}

@Composable
private fun WatchedSort.label(): String = stringResource(
    when (this) {
        WatchedSort.FINISHED -> Res.string.watched_sort_finished
        WatchedSort.RATING -> Res.string.library_sort_rating
        WatchedSort.NAME -> Res.string.library_sort_name
    },
)

@Preview
@Composable
private fun WatchedScreenPreview() {
    FilmoryTheme {
        WatchedScreen(
            state = WatchedState(items = listOf(WatchedItemUi("1", "Arrival", null, 4.5f)), totalCount = 1),
            onAction = {},
        )
    }
}
