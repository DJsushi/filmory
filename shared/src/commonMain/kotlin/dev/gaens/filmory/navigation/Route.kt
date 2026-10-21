package dev.gaens.filmory.navigation

import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Notes : Route

    @Serializable
    data object Watched : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class Library(
        val preset: LibraryPreset = LibraryPreset.ALL,
        val collectionId: String? = null,
    ) : Route

    @Serializable
    data object Collections : Route

    @Serializable
    data class TitleDetail(val titleId: String) : Route

    @Serializable
    data class AddNote(val titleId: String, val episodeId: String? = null) : Route

    @Serializable
    data class EditNote(val noteId: String) : Route

    @Serializable
    data class NoteDetail(val noteId: String) : Route

    @Serializable
    data object Calendar : Route
}
