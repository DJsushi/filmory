package dev.gaens.filmory.feature.collection.presentation

data class CollectionsState(
    val collections: List<CollectionCardUi> = emptyList(),
)

data class CollectionCardUi(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val titleCount: Int,
    val look: CollectionLook = CollectionLook(),
)

sealed interface CollectionsAction {
    data object OnBackClick : CollectionsAction
    data class OnCollectionClick(val collectionId: String) : CollectionsAction

    /** Creates a collection called [newName] (numbered if taken) and opens it. */
    data class OnAddClick(val newName: String) : CollectionsAction
}

sealed interface CollectionsEvent {
    data class OpenNewCollection(val collectionId: String) : CollectionsEvent
}
