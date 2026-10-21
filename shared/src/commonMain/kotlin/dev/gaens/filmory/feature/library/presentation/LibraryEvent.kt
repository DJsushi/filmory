package dev.gaens.filmory.feature.library.presentation

sealed interface LibraryEvent {
    data object CollectionDeleted : LibraryEvent
}
