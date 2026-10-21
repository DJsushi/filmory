package dev.gaens.filmory.feature.note.presentation.list

import dev.gaens.filmory.feature.library.presentation.LibraryItemUi
import dev.gaens.filmory.feature.library.presentation.components.CollectionOptionUi
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.note.presentation.NoteUi
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

data class NotesState(
    val notes: List<NoteUi> = emptyList(),
    val totalCount: Int = 0,
    val titleCount: Int = 0,
    val favoriteCount: Int = 0,
    val typeFilter: NoteType? = null,
    val isFavoritesOnly: Boolean = false,
    val isNewestFirst: Boolean = true,
    val isTitlePickerOpen: Boolean = false,
    val pickerFilters: TitlePickerFilters = TitlePickerFilters(),
    /** The titles that pass [pickerFilters], by name. */
    val titles: List<LibraryItemUi> = emptyList(),
    /** Whether the library has any title at all, to tell "nothing yet" from "nothing matches". */
    val hasTitles: Boolean = false,
    val pickerCollections: List<CollectionOptionUi> = emptyList(),
)

/** What the "add a note to" picker is narrowed by. The same search and filters as the library. */
data class TitlePickerFilters(
    val searchText: String = "",
    val type: TitleType? = null,
    val status: TitleStatus? = null,
    val collectionId: String? = null,
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
    data class OnPickerSearchChange(val text: String) : NotesAction
    data class OnPickerTypeClick(val type: TitleType) : NotesAction
    data class OnPickerStatusChange(val status: TitleStatus?) : NotesAction
    data class OnPickerCollectionChange(val collectionId: String?) : NotesAction
}

sealed interface NotesEvent {
    data class OpenNote(val noteId: String) : NotesEvent
    data class AddNoteTo(val titleId: String) : NotesEvent
}
