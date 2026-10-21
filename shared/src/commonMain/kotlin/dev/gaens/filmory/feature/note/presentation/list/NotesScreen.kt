package dev.gaens.filmory.feature.note.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FabBottomOffset
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.bottomBarContentPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryFab
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryTile
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.note.presentation.components.NoteCard
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.app_name
import dev.gaens.filmory.resources.detail_add_note
import dev.gaens.filmory.resources.ic_heart
import dev.gaens.filmory.resources.ic_shuffle
import dev.gaens.filmory.resources.note_count
import dev.gaens.filmory.resources.notes_all_types
import dev.gaens.filmory.resources.notes_empty
import dev.gaens.filmory.resources.notes_favorites
import dev.gaens.filmory.resources.notes_random
import dev.gaens.filmory.resources.notes_random_subtitle
import dev.gaens.filmory.resources.notes_sort_newest
import dev.gaens.filmory.resources.notes_sort_oldest
import dev.gaens.filmory.resources.notes_subtitle
import dev.gaens.filmory.resources.notes_subtitle_empty
import dev.gaens.filmory.resources.notes_title
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesScreenRoot(
    onNoteClick: (String) -> Unit,
    onTitleClick: (String) -> Unit,
    onAddNote: (titleId: String) -> Unit,
    viewModel: NotesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NotesEvent.OpenNote -> onNoteClick(event.noteId)
            is NotesEvent.AddNoteTo -> onAddNote(event.titleId)
        }
    }
    NotesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is NotesAction.OnNoteClick -> onNoteClick(action.noteId)
                is NotesAction.OnTitleClick -> onTitleClick(action.titleId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun NotesScreen(
    state: NotesState,
    onAction: (NotesAction) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = bottomBarContentPadding()) {
            item(key = "hero") { NotesHero(state, onAction) }
            item(key = "filters") { NotesFilters(state, onAction) }
            if (state.notes.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(Res.string.notes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(ScreenPadding),
                    )
                }
            }
            itemsIndexed(state.notes, key = { _, note -> note.id }) { index, note ->
                NoteCard(
                    note = note,
                    onClick = { onAction(NotesAction.OnNoteClick(note.id)) },
                    onFavoriteClick = { onAction(NotesAction.OnFavoriteToggle(note.id)) },
                    onTitleClick = { onAction(NotesAction.OnTitleClick(note.titleId)) },
                    modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, bottom = 14.dp).rise(index + 5),
                )
            }
        }
        FilmoryFab(
            text = stringResource(Res.string.detail_add_note),
            onClick = { onAction(NotesAction.OnAddNoteClick) },
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 16.dp, bottom = FabBottomOffset),
        )
    }
    if (state.isTitlePickerOpen) TitlePickerSheet(state, onAction)
}

@Composable
private fun NotesHero(state: NotesState, onAction: (NotesAction) -> Unit) {
    HeroHeader {
        FilmoryAppBar(title = stringResource(Res.string.app_name))
        Text(
            text = stringResource(Res.string.notes_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp).rise(0),
        )
        Text(
            text = if (state.totalCount == 0) {
                stringResource(Res.string.notes_subtitle_empty)
            } else {
                stringResource(
                    Res.string.notes_subtitle,
                    pluralStringResource(Res.plurals.note_count, state.totalCount, state.totalCount),
                    pluralStringResource(Res.plurals.title_count, state.titleCount, state.titleCount),
                )
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp).rise(1),
        )
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FilmoryTile(
                title = stringResource(Res.string.notes_random),
                subtitle = stringResource(Res.string.notes_random_subtitle),
                icon = Res.drawable.ic_shuffle,
                onClick = { onAction(NotesAction.OnRandomClick) },
                modifier = Modifier.weight(1f).rise(2),
            )
            FilmoryTile(
                title = stringResource(Res.string.notes_favorites),
                subtitle = pluralStringResource(Res.plurals.note_count, state.favoriteCount, state.favoriteCount),
                icon = Res.drawable.ic_heart,
                onClick = { onAction(NotesAction.OnFavoritesClick) },
                modifier = Modifier.weight(1f).rise(3),
            )
        }
    }
}

@Composable
private fun NotesFilters(state: NotesState, onAction: (NotesAction) -> Unit) {
    Column(Modifier.padding(top = 12.dp, bottom = 12.dp).rise(4), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ChipRow {
            FilmoryChip(
                label = stringResource(Res.string.notes_all_types),
                selected = state.typeFilter == null,
                showCheckWhenSelected = false,
                onClick = { onAction(NotesAction.OnTypeFilterChange(null)) },
            )
            NoteType.entries.forEach { type ->
                FilmoryChip(
                    label = type.label(),
                    selected = state.typeFilter == type,
                    showCheckWhenSelected = false,
                    onClick = { onAction(NotesAction.OnTypeFilterChange(type)) },
                )
            }
        }
        ChipRow {
            FilmoryChip(
                label = stringResource(Res.string.notes_favorites),
                selected = state.isFavoritesOnly,
                icon = Res.drawable.ic_heart,
                onClick = { onAction(NotesAction.OnFavoritesClick) },
            )
            FilmoryChip(
                label = stringResource(if (state.isNewestFirst) Res.string.notes_sort_newest else Res.string.notes_sort_oldest),
                selected = false,
                onClick = { onAction(NotesAction.OnSortClick) },
            )
        }
    }
}

@Preview
@Composable
private fun NotesScreenPreview() {
    FilmoryTheme { NotesScreen(NotesState(totalCount = 0), onAction = {}) }
}
