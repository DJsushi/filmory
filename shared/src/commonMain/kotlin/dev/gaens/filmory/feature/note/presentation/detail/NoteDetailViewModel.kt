package dev.gaens.filmory.feature.note.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.asUiText
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.note.domain.DeleteNote
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.note.presentation.buildNoteUis
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Suppress("LongParameterList")
class NoteDetailViewModel(
    private val noteId: String,
    private val noteRepository: NoteRepository,
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val imageStorage: NoteImageStorage,
    private val deleteNote: DeleteNote,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var isDeleting = false

    private val _state = MutableStateFlow(NoteDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeNote()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NoteDetailState())

    private val eventChannel = Channel<NoteDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: NoteDetailAction) {
        when (action) {
            NoteDetailAction.OnDeleteClick -> _state.update { it.copy(isDeleteDialogOpen = true) }
            NoteDetailAction.OnDeleteDismiss -> _state.update { it.copy(isDeleteDialogOpen = false) }
            NoteDetailAction.OnDeleteConfirm -> delete()
            is NoteDetailAction.OnDraftChange -> _state.update { it.copy(draft = action.text) }
            NoteDetailAction.OnSendClick -> sendComment()
            is NoteDetailAction.OnDeleteCommentClick -> report { noteRepository.deleteComment(action.commentId) }
            else -> Unit
        }
    }

    private fun observeNote() {
        val note = noteRepository.observeNote(noteId).filterNotNull()
        combine(
            note,
            titleRepository.observeTitles(),
            watchingRepository.observeAllWatchings(),
            episodeRepository.observeAllSeasons(),
        ) { note, titles, watchings, seasons ->
            val ui = buildNoteUis(listOf(note), titles, watchings, seasons).firstOrNull()
            _state.update { it.copy(note = ui, imageUrl = note.imagePath?.let(imageStorage::resolve)) }
        }.launchIn(viewModelScope)
        noteRepository.observeComments(noteId)
            .onEach { comments ->
                _state.update { state -> state.copy(comments = comments.map { CommentUi(it.id, it.text, it.createdAt) }) }
            }
            .launchIn(viewModelScope)
    }

    private fun sendComment() {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        _state.update { it.copy(draft = "") }
        report { noteRepository.addComment(NoteComment(idGenerator.newId(), noteId, text, clock.now())) }
    }

    private fun delete() {
        if (isDeleting) return
        isDeleting = true
        _state.update { it.copy(isDeleteDialogOpen = false) }
        viewModelScope.launch {
            when (val result = deleteNote(noteId)) {
                is Result.Success -> eventChannel.send(NoteDetailEvent.NoteDeleted)
                is Result.Error -> {
                    isDeleting = false
                    eventChannel.send(NoteDetailEvent.ShowMessage(result.error.asUiText()))
                }
            }
        }
    }

    private fun report(block: suspend () -> Result<*, DataError.Local>) {
        viewModelScope.launch {
            val result = block()
            if (result is Result.Error) eventChannel.send(NoteDetailEvent.ShowMessage(result.error.asUiText()))
        }
    }
}
