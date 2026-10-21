package dev.gaens.filmory.feature.note.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.home.presentation.toThumbUi
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.note.presentation.NoteUi
import dev.gaens.filmory.feature.note.presentation.buildNoteUis
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class NotesViewModel(
    private val noteRepository: NoteRepository,
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val random: Random = Random.Default,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val allNotes = MutableStateFlow<List<NoteUi>>(emptyList())
    private val _state = MutableStateFlow(NotesState())

    val state = combine(_state, allNotes) { ui, notes -> ui.withNotes(notes) }
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeNotes()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NotesState())

    private val eventChannel = Channel<NotesEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: NotesAction) {
        when (action) {
            NotesAction.OnRandomClick -> openRandomNote()
            NotesAction.OnFavoritesClick -> _state.update { it.copy(isFavoritesOnly = !it.isFavoritesOnly) }
            is NotesAction.OnTypeFilterChange -> _state.update { it.copy(typeFilter = action.type) }
            NotesAction.OnSortClick -> _state.update { it.copy(isNewestFirst = !it.isNewestFirst) }
            is NotesAction.OnFavoriteToggle -> toggleFavorite(action.noteId)
            NotesAction.OnAddNoteClick -> _state.update { it.copy(isTitlePickerOpen = true) }
            NotesAction.OnTitlePickerDismiss -> _state.update { it.copy(isTitlePickerOpen = false) }
            is NotesAction.OnTitlePicked -> {
                _state.update { it.copy(isTitlePickerOpen = false) }
                viewModelScope.launch { eventChannel.send(NotesEvent.AddNoteTo(action.titleId)) }
            }
            else -> Unit
        }
    }

    private fun observeNotes() {
        combine(
            noteRepository.observeNotes(),
            titleRepository.observeTitles(),
            watchingRepository.observeAllWatchings(),
            episodeRepository.observeAllSeasons(),
        ) { notes, titles, watchings, seasons ->
            allNotes.value = buildNoteUis(notes, titles, watchings, seasons)
            _state.update { state -> state.copy(titles = titles.sortedBy { it.name.lowercase() }.map { it.toThumbUi() }) }
        }.launchIn(viewModelScope)
    }

    private fun openRandomNote() {
        val notes = allNotes.value
        if (notes.isEmpty()) return
        viewModelScope.launch { eventChannel.send(NotesEvent.OpenNote(notes[random.nextInt(notes.size)].id)) }
    }

    private fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.observeNote(noteId).first() ?: return@launch
            noteRepository.upsertNote(note.copy(isFavorite = !note.isFavorite))
        }
    }
}

private fun NotesState.withNotes(all: List<NoteUi>): NotesState {
    val filtered = all
        .filter { typeFilter == null || it.type == typeFilter }
        .filter { !isFavoritesOnly || it.isFavorite }
    return copy(
        notes = if (isNewestFirst) filtered.sortedByDescending { it.createdAt } else filtered.sortedBy { it.createdAt },
        totalCount = all.size,
        titleCount = all.distinctBy { it.titleId }.size,
        favoriteCount = all.count { it.isFavorite },
    )
}
