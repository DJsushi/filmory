package dev.gaens.filmory.feature.collection.presentation

data class CollectionsState(
    val collections: List<CollectionCardUi> = emptyList(),
    val isAddDialogOpen: Boolean = false,
)

data class CollectionCardUi(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val titleCount: Int,
)

sealed interface CollectionsAction {
    data object OnBackClick : CollectionsAction
    data class OnCollectionClick(val collectionId: String) : CollectionsAction
    data object OnAddClick : CollectionsAction
    data object OnAddDismiss : CollectionsAction
    data class OnAddConfirm(val name: String) : CollectionsAction
}
