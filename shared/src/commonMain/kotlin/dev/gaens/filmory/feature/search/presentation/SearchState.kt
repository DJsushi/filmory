package dev.gaens.filmory.feature.search.presentation

import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.search.domain.TitleSearchResult

data class SearchState(
    val query: String = "",
    val results: List<SearchResultUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val addingTmdbId: Long? = null,
)

data class SearchResultUi(val result: TitleSearchResult, val isInLibrary: Boolean)

sealed interface SearchAction {
    data object OnBackClick : SearchAction
    data class OnQueryChange(val query: String) : SearchAction
    data class OnResultClick(val result: TitleSearchResult) : SearchAction
    data object OnAddManuallyClick : SearchAction
}

sealed interface SearchEvent {
    data class OpenTitle(val titleId: String) : SearchEvent
    data class ShowMessage(val message: UiText) : SearchEvent
}
