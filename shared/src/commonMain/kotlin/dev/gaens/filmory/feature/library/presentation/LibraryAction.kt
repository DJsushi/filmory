package dev.gaens.filmory.feature.library.presentation

import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

sealed interface LibraryAction {
    data object OnBackClick : LibraryAction
    data class OnTitleClick(val titleId: String) : LibraryAction
    data object OnAddTitleClick : LibraryAction
    data class OnStatusFilterChange(val status: TitleStatus?) : LibraryAction
    data class OnTypeFilterClick(val type: TitleType) : LibraryAction
    data class OnCollectionFilterChange(val collectionId: String?) : LibraryAction
    data class OnSortChange(val sort: LibrarySort) : LibraryAction
    data object OnSearchClick : LibraryAction
    data class OnSearchTextChange(val text: String) : LibraryAction
    data class OnDialogOpen(val dialog: LibraryDialog) : LibraryAction
    data object OnDialogDismiss : LibraryAction
    data class OnSettingsChange(val settings: LibrarySettings) : LibraryAction
}
