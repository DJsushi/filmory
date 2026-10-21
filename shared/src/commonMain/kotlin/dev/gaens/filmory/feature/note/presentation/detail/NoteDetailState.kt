package dev.gaens.filmory.feature.note.presentation.detail

import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.note.presentation.NoteUi
import kotlin.time.Instant

data class NoteDetailState(
    val note: NoteUi? = null,
    val imageUrl: String? = null,
    val comments: List<CommentUi> = emptyList(),
    val draft: String = "",
    val isDeleteDialogOpen: Boolean = false,
)

data class CommentUi(val id: String, val text: String, val createdAt: Instant)

sealed interface NoteDetailAction {
    data object OnBackClick : NoteDetailAction
    data object OnEditClick : NoteDetailAction
    data class OnTitleClick(val titleId: String) : NoteDetailAction
    data object OnDeleteClick : NoteDetailAction
    data object OnDeleteConfirm : NoteDetailAction
    data object OnDeleteDismiss : NoteDetailAction
    data class OnDraftChange(val text: String) : NoteDetailAction
    data object OnSendClick : NoteDetailAction
    data class OnDeleteCommentClick(val commentId: String) : NoteDetailAction
}

sealed interface NoteDetailEvent {
    data object NoteDeleted : NoteDetailEvent
    data class ShowMessage(val message: UiText) : NoteDetailEvent
}
