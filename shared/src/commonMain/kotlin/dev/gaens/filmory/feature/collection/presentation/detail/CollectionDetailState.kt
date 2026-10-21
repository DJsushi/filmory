package dev.gaens.filmory.feature.collection.presentation.detail

import dev.gaens.filmory.feature.collection.domain.CollectionIcon
import dev.gaens.filmory.feature.collection.presentation.CollectionLook

data class CollectionDetailState(
    val name: String? = null,
    val isDefault: Boolean = false,
    val look: CollectionLook = CollectionLook(),
    /** The name is a text field right now. */
    val isEditingName: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val isAppearanceSheetOpen: Boolean = false,
)

sealed interface CollectionDetailAction {
    data object OnNameTap : CollectionDetailAction
    data object OnEditNameStart : CollectionDetailAction

    /** Saves [name] when it is not blank, and leaves the name field either way. */
    data class OnNameCommit(val name: String) : CollectionDetailAction
    data object OnDeleteClick : CollectionDetailAction
    data object OnDeleteDismiss : CollectionDetailAction
    data object OnDeleteConfirm : CollectionDetailAction
    data object OnAppearanceClick : CollectionDetailAction
    data object OnAppearanceDismiss : CollectionDetailAction

    /** Null for no icon. */
    data class OnIconChange(val icon: CollectionIcon?) : CollectionDetailAction

    /** Null for the default gradient. */
    data class OnColorChange(val colorArgb: Long?) : CollectionDetailAction
    data class OnPhotoPicked(val bytes: ByteArray) : CollectionDetailAction
}

sealed interface CollectionDetailEvent {
    /** A short tap on the name: tell the user how to rename. */
    data object ShowRenameHint : CollectionDetailEvent
    data object Deleted : CollectionDetailEvent
}
