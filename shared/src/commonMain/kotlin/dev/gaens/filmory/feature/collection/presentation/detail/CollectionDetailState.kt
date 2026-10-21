package dev.gaens.filmory.feature.collection.presentation.detail

data class CollectionDetailState(
    val name: String? = null,
    val isDefault: Boolean = false,
    /** The name is a text field right now. */
    val isEditingName: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
)

sealed interface CollectionDetailAction {
    data object OnNameTap : CollectionDetailAction
    data object OnEditNameStart : CollectionDetailAction

    /** Saves [name] when it is not blank, and leaves the name field either way. */
    data class OnNameCommit(val name: String) : CollectionDetailAction
    data object OnDeleteClick : CollectionDetailAction
    data object OnDeleteDismiss : CollectionDetailAction
    data object OnDeleteConfirm : CollectionDetailAction
}

sealed interface CollectionDetailEvent {
    /** A short tap on the name: tell the user how to rename. */
    data object ShowRenameHint : CollectionDetailEvent
    data object Deleted : CollectionDetailEvent
}
