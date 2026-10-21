package dev.gaens.filmory.feature.note.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.asUiText
import dev.gaens.filmory.core.presentation.ui.formatTimestamp
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.note.domain.AddNote
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteDraft
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.note.presentation.watchingNumbers
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Adds a note when [noteId] is null, otherwise edits that note. */
@Suppress("LongParameterList")
class NoteEditorViewModel(
    private val titleId: String?,
    private val initialEpisodeId: String?,
    private val noteId: String?,
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val noteRepository: NoteRepository,
    private val imageStorage: NoteImageStorage,
    private val addNote: AddNote,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorState(isEditing = noteId != null, createdAt = clock.now()))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<NoteEditorEvent>()
    val events = eventChannel.receiveAsFlow()

    private var editedNote: Note? = null
    private var imagePath: String? = null
    private val imagesAddedHere = mutableSetOf<String>()

    init {
        viewModelScope.launch { load() }
    }

    fun onAction(action: NoteEditorAction) {
        when (action) {
            NoteEditorAction.OnCloseClick -> close(saved = false)
            NoteEditorAction.OnSaveClick -> save()
            is NoteEditorAction.OnWatchingChange -> _state.update { it.copy(selectedWatchingId = action.watchingId) }
            is NoteEditorAction.OnTypeChange -> _state.update { it.copy(noteType = action.type) }
            is NoteEditorAction.OnSeasonChange -> _state.update { state ->
                val season = state.seasons.find { it.number == action.seasonNumber }
                state.copy(selectedEpisodeId = season?.episodes?.firstOrNull()?.id)
            }
            is NoteEditorAction.OnEpisodeChange -> _state.update { it.copy(selectedEpisodeId = action.episodeId) }
            is NoteEditorAction.OnTimestampChange -> _state.update { it.copy(timestampText = action.text) }
            is NoteEditorAction.OnTextChange -> _state.update { it.copy(text = action.text) }
            is NoteEditorAction.OnDateChange -> _state.update { state ->
                val zone = TimeZone.currentSystemDefault()
                val time = state.createdAt.toLocalDateTime(zone).time
                state.copy(createdAt = action.date.atTime(time).toInstant(zone))
            }
            is NoteEditorAction.OnImagePicked -> saveImage(action.bytes)
            NoteEditorAction.OnImageRemove -> {
                imagePath = null
                _state.update { it.copy(imageUrl = null) }
            }
        }
    }

    private suspend fun load() {
        val note = noteId?.let { noteRepository.observeNote(it).first() }
        editedNote = note
        val resolvedTitleId = note?.titleId ?: titleId ?: return close(saved = false)
        val title = titleRepository.getTitle(resolvedTitleId) ?: return close(saved = false)
        val watchings = watchingRepository.getWatchings(resolvedTitleId)
        val numbers = watchingNumbers(watchings)
        imagePath = note?.imagePath
        _state.update { state ->
            state.copy(
                titleId = title.id,
                titleName = title.name,
                titlePosterUrl = title.posterUrl,
                titleType = title.type,
                watchingNumbers = watchings.sortedBy { it.createdAt }.map { WatchingOption(it.id, numbers.getValue(it.id)) },
                selectedWatchingId = note?.watchingId ?: watchings.maxByOrNull { it.createdAt }?.id,
                seasons = episodeRepository.getSeasons(title.id).map { season ->
                    SeasonOption(season.number, season.episodes.map { EpisodeOption(it.id, it.number, it.name) })
                },
                selectedEpisodeId = note?.episodeId ?: initialEpisodeId,
                noteType = note?.type ?: state.noteType,
                timestampText = note?.timestampMillis?.let(::formatTimestamp).orEmpty(),
                text = note?.text.orEmpty(),
                imageUrl = note?.imagePath?.let(imageStorage::resolve),
                createdAt = note?.createdAt ?: state.createdAt,
            )
        }
    }

    private fun saveImage(bytes: ByteArray) {
        viewModelScope.launch {
            when (val result = imageStorage.save(bytes)) {
                is Result.Success -> {
                    imagePath = result.data
                    imagesAddedHere += result.data
                    _state.update { it.copy(imageUrl = imageStorage.resolve(result.data)) }
                }
                is Result.Error -> eventChannel.send(NoteEditorEvent.ShowMessage(result.error.asUiText()))
            }
        }
    }

    private fun save() {
        val state = _state.value
        if (!state.canSave) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = editedNote?.let { saveEdit(it, state) } ?: saveNew(state)
            if (result is Result.Error) {
                _state.update { it.copy(isSaving = false) }
                eventChannel.send(NoteEditorEvent.ShowMessage(result.error.asUiText()))
            } else {
                close(saved = true)
            }
        }
    }

    private suspend fun saveNew(state: NoteEditorState): Result<*, DataError.Local> = addNote(
        NoteDraft(
            titleId = state.titleId,
            watchingId = state.selectedWatchingId,
            episodeId = state.selectedEpisodeId,
            type = state.noteType,
            text = state.text.trim(),
            timestampMillis = state.parsedTimestamp,
            imagePath = imagePath,
            createdAt = state.createdAt,
        ),
    )

    private suspend fun saveEdit(note: Note, state: NoteEditorState): Result<*, DataError.Local> {
        val result = noteRepository.upsertNote(
            note.copy(
                watchingId = state.selectedWatchingId ?: note.watchingId,
                episodeId = state.selectedEpisodeId,
                type = state.noteType,
                text = state.text.trim(),
                timestampMillis = state.parsedTimestamp,
                imagePath = imagePath,
                createdAt = state.createdAt,
            ),
        )
        val replacedImage = note.imagePath
        if (result is Result.Success && replacedImage != null && replacedImage != imagePath) {
            imageStorage.delete(replacedImage)
        }
        return result
    }

    /** Leaves the editor. Images picked here but not saved with the note are removed. */
    private fun close(saved: Boolean) {
        val unused = if (saved) imagesAddedHere - setOfNotNull(imagePath) else imagesAddedHere.toSet()
        viewModelScope.launch {
            unused.forEach { imageStorage.delete(it) }
            eventChannel.send(NoteEditorEvent.Close)
        }
    }
}
