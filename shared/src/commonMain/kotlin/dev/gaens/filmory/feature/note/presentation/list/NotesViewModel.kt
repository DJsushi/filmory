package dev.gaens.filmory.feature.note.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.collection.domain.inDisplayOrder
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.library.domain.BuildLibrary
import dev.gaens.filmory.feature.library.domain.LibraryQuery
import dev.gaens.filmory.feature.library.presentation.components.CollectionOptionUi
import dev.gaens.filmory.feature.library.presentation.toUi
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.note.presentation.NoteUi
import dev.gaens.filmory.feature.note.presentation.buildNoteUis
import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.Title
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
    private val collectionRepository: CollectionRepository,
    private val buildLibrary: BuildLibrary,
    private val random: Random = Random.Default,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val allNotes = MutableStateFlow<List<NoteUi>>(emptyList())
    private val allTitles = MutableStateFlow<List<Title>>(emptyList())
    private val allCollections = MutableStateFlow<List<TitleCollection>>(emptyList())
    private val _state = MutableStateFlow(NotesState())

    val state = combine(_state, allNotes, allTitles, allCollections) { ui, notes, titles, collections ->
        ui.withNotes(notes).withTitlePicker(titles, collections)
    }
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
            NotesAction.OnAddNoteClick -> _state.update { it.copy(isTitlePickerOpen = true, pickerFilters = TitlePickerFilters()) }
            NotesAction.OnTitlePickerDismiss -> _state.update { it.copy(isTitlePickerOpen = false) }
            is NotesAction.OnTitlePicked -> {
                _state.update { it.copy(isTitlePickerOpen = false) }
                viewModelScope.launch { eventChannel.send(NotesEvent.AddNoteTo(action.titleId)) }
            }
            is NotesAction.OnPickerSearchChange -> updatePickerFilters { it.copy(searchText = action.text) }
            is NotesAction.OnPickerTypeClick -> updatePickerFilters {
                it.copy(type = if (it.type == action.type) null else action.type)
            }
            is NotesAction.OnPickerStatusChange -> updatePickerFilters { it.copy(status = action.status) }
            is NotesAction.OnPickerCollectionChange -> updatePickerFilters { it.copy(collectionId = action.collectionId) }
            else -> Unit
        }
    }

    private fun observeNotes() {
        combine(
            noteRepository.observeNotes(),
            titleRepository.observeTitles(),
            watchingRepository.observeAllWatchings(),
            episodeRepository.observeAllSeasons(),
            collectionRepository.observeCollections(),
        ) { notes, titles, watchings, seasons, collections ->
            allNotes.value = buildNoteUis(notes, titles, watchings, seasons)
            allTitles.value = titles
            allCollections.value = collections
        }.launchIn(viewModelScope)
    }

    private fun updatePickerFilters(transform: (TitlePickerFilters) -> TitlePickerFilters) =
        _state.update { it.copy(pickerFilters = transform(it.pickerFilters)) }

    /** The picker lists titles the way the library does, by name, narrowed by its search and filters. */
    private fun NotesState.withTitlePicker(titles: List<Title>, collections: List<TitleCollection>): NotesState {
        if (!isTitlePickerOpen) return this
        // A deleted collection no longer filters anything.
        val collection = collections.find { it.id == pickerFilters.collectionId }
        val query = LibraryQuery(
            status = pickerFilters.status,
            type = pickerFilters.type,
            onlyTitleIds = collection?.titleIds?.toSet(),
            searchText = pickerFilters.searchText,
            sort = LibrarySort.NAME,
        )
        return copy(
            // Ratings and activity are for other sorts, so no watchings are needed here.
            titles = buildLibrary(titles, emptyList(), query).map { it.toUi() },
            hasTitles = titles.isNotEmpty(),
            pickerCollections = collections.inDisplayOrder().map { CollectionOptionUi(it.id, it.name) },
        )
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
