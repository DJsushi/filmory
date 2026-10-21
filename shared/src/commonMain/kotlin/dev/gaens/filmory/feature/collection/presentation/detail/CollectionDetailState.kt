package dev.gaens.filmory.feature.collection.presentation.detail

data class CollectionDetailState(
    val name: String? = null,
    val isDefault: Boolean = false,
    val dialog: CollectionDetailDialog? = null,
)

enum class CollectionDetailDialog { RENAME, DELETE }

sealed interface CollectionDetailAction {
    data class OnDialogOpen(val dialog: CollectionDetailDialog) : CollectionDetailAction
    data object OnDialogDismiss : CollectionDetailAction
    data class OnRenameConfirm(val name: String) : CollectionDetailAction
    data object OnDeleteConfirm : CollectionDetailAction
}

sealed interface CollectionDetailEvent {
    data object Deleted : CollectionDetailEvent
}
