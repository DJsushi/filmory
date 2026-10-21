package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.library.presentation.LibraryPreset

sealed interface HomeAction {
    data class OnTitleClick(val titleId: String) : HomeAction
    data class OnLibraryClick(val preset: LibraryPreset, val collectionId: String? = null) : HomeAction
    data object OnCalendarClick : HomeAction
    data object OnMoreCollectionsClick : HomeAction
    data object OnEditWatchLaterClick : HomeAction
    data object OnWatchLaterEditorDismiss : HomeAction
    data class OnMoveWatchLater(val titleId: String, val moveEarlier: Boolean) : HomeAction
    data class OnRemoveWatchLater(val titleId: String) : HomeAction
}
