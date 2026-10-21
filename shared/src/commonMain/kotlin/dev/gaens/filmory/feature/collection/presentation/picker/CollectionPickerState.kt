package dev.gaens.filmory.feature.collection.presentation.picker

import dev.gaens.filmory.feature.collection.presentation.CollectionLook

data class CollectionPickerState(
    val collections: List<CollectionPickerItemUi> = emptyList(),
    /** The sheet is in management mode: reorder, pin, rename, delete and add. */
    val isEditing: Boolean = false,
    val dialog: CollectionPickerDialog? = null,
)

data class CollectionPickerItemUi(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val isPinned: Boolean,
    val containsTitle: Boolean,
    val look: CollectionLook,
)

sealed interface CollectionPickerDialog {
    /** [name] is what the field starts with. It travels with the dialog because a new collection is not in the list yet. */
    data class Rename(val collectionId: String, val name: String) : CollectionPickerDialog
    data class Delete(val collectionId: String) : CollectionPickerDialog
}

sealed interface CollectionPickerAction {
    data class OnToggle(val collectionId: String) : CollectionPickerAction
    data object OnEditClick : CollectionPickerAction
    data object OnEditDone : CollectionPickerAction
    data class OnPinClick(val collectionId: String) : CollectionPickerAction

    /** Moves [collectionId] to where [targetId] is. Applied at once, saved by [OnMoveDropped]. */
    data class OnMove(val collectionId: String, val targetId: String) : CollectionPickerAction
    data object OnMoveDropped : CollectionPickerAction
    data class OnRenameClick(val collectionId: String) : CollectionPickerAction
    data class OnRenameConfirm(val name: String) : CollectionPickerAction
    data class OnDeleteClick(val collectionId: String) : CollectionPickerAction
    data object OnDeleteConfirm : CollectionPickerAction
    data object OnDialogDismiss : CollectionPickerAction

    /** Creates a collection called [newName] (numbered if taken) and asks for its real name. */
    data class OnAddClick(val newName: String) : CollectionPickerAction
}
