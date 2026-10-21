package dev.gaens.filmory.feature.note.presentation.list

import dev.gaens.filmory.feature.home.presentation.TitleThumbUi
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.note.presentation.NoteUi

data class NotesState(
    val notes: List<NoteUi> = emptyList(),
    val totalCount: Int = 0,
    val titleCount: Int = 0,
    val favoriteCount: Int = 0,
    val typeFilter: NoteType? = null,
    val isFavoritesOnly: Boolean = false,
    val isNewestFirst: Boolean = true,
    val isTitlePickerOpen: Boolean = false,
    val titles: List<TitleThumbUi> = emptyList(),
)

sealed interface NotesAction {
    data class OnNoteClick(val noteId: String) : NotesAction
    data class OnTitleClick(val titleId: String) : NotesAction
    data object OnRandomClick : NotesAction
    data object OnFavoritesClick : NotesAction
    data class OnTypeFilterChange(val type: NoteType?) : NotesAction
    data object OnSortClick : NotesAction
    data class OnFavoriteToggle(val noteId: String) : NotesAction
    data object OnAddNoteClick : NotesAction
    data object OnTitlePickerDismiss : NotesAction
    data class OnTitlePicked(val titleId: String) : NotesAction
}

sealed interface NotesEvent {
    data class OpenNote(val noteId: String) : NotesEvent
    data class AddNoteTo(val titleId: String) : NotesEvent
}
