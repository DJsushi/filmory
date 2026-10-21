package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.settings.domain.HomeBlock

sealed interface HomeAction {
    data class OnTitleClick(val titleId: String) : HomeAction
    data class OnLibraryClick(val preset: LibraryPreset, val collectionId: String? = null) : HomeAction
    data object OnCalendarClick : HomeAction
    data object OnMoreCollectionsClick : HomeAction
    data object OnEditWatchLaterClick : HomeAction
    data object OnWatchLaterEditorDismiss : HomeAction
    data class OnMoveWatchLater(val titleId: String, val moveEarlier: Boolean) : HomeAction
    data class OnRemoveWatchLater(val titleId: String) : HomeAction
    data object OnEditLayoutClick : HomeAction
    data object OnEditLayoutDone : HomeAction
    data object OnResetLayout : HomeAction

    /** Applied to the screen at once. [OnLayoutDropped] saves it. */
    data class OnMoveBlock(val block: HomeBlock, val target: HomeBlock) : HomeAction
    data class OnNudgeBlock(val block: HomeBlock, val offset: Int) : HomeAction
    data object OnLayoutDropped : HomeAction
}
