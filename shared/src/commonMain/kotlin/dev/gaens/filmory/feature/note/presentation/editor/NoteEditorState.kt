package dev.gaens.filmory.feature.note.presentation.editor

import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.core.presentation.ui.parseTimestamp
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.TitleType
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class NoteEditorState(
    val isEditing: Boolean = false,
    val titleId: String = "",
    val titleName: String = "",
    val titlePosterUrl: String? = null,
    val titleType: TitleType = TitleType.MOVIE,
    val watchingNumbers: List<WatchingOption> = emptyList(),
    val selectedWatchingId: String? = null,
    val noteType: NoteType = NoteType.THOUGHT,
    val seasons: List<SeasonOption> = emptyList(),
    val selectedEpisodeId: String? = null,
    val timestampText: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val createdAt: Instant = Instant.fromEpochMilliseconds(0),
    val isSaving: Boolean = false,
) {
    val isTimestampValid: Boolean get() = timestampText.isBlank() || parsedTimestamp != null
    val parsedTimestamp: Long? get() = parseTimestamp(timestampText)
    val canSave: Boolean get() = text.isNotBlank() && isTimestampValid && !isSaving
    val selectedSeasonNumber: Int?
        get() = seasons.find { season -> season.episodes.any { it.id == selectedEpisodeId } }?.number
}

data class WatchingOption(val id: String, val number: Int)

data class SeasonOption(val number: Int, val episodes: List<EpisodeOption>)

data class EpisodeOption(val id: String, val number: Int, val name: String?)

sealed interface NoteEditorAction {
    data object OnCloseClick : NoteEditorAction
    data object OnSaveClick : NoteEditorAction
    data class OnWatchingChange(val watchingId: String?) : NoteEditorAction
    data class OnTypeChange(val type: NoteType) : NoteEditorAction
    data class OnSeasonChange(val seasonNumber: Int?) : NoteEditorAction
    data class OnEpisodeChange(val episodeId: String) : NoteEditorAction
    data class OnTimestampChange(val text: String) : NoteEditorAction
    data class OnTextChange(val text: String) : NoteEditorAction
    data class OnDateChange(val date: LocalDate) : NoteEditorAction
    data class OnImagePicked(val bytes: ByteArray) : NoteEditorAction
    data object OnImageRemove : NoteEditorAction
}

sealed interface NoteEditorEvent {
    data object Close : NoteEditorEvent
    data class ShowMessage(val message: UiText) : NoteEditorEvent
}
