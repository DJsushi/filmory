package dev.gaens.filmory.feature.watched.presentation

import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watched.domain.WatchedSort

data class WatchedState(
    val items: List<WatchedItemUi> = emptyList(),
    val totalCount: Int = 0,
    val typeFilter: TitleType? = null,
    val sort: WatchedSort = WatchedSort.FINISHED,
    val isSearchOpen: Boolean = false,
    val searchText: String = "",
)

data class WatchedItemUi(val id: String, val name: String, val posterUrl: String?, val stars: Float?)

sealed interface WatchedAction {
    data class OnTitleClick(val titleId: String) : WatchedAction
    data class OnTypeFilterChange(val type: TitleType?) : WatchedAction
    data class OnSortChange(val sort: WatchedSort) : WatchedAction
    data object OnSearchClick : WatchedAction
    data class OnSearchTextChange(val text: String) : WatchedAction
}
