package dev.gaens.filmory.feature.watched.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.watched.domain.BuildWatchedList
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class WatchedViewModel(
    titleRepository: TitleRepository,
    watchingRepository: WatchingRepository,
    private val buildWatchedList: BuildWatchedList,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchedState())

    val state = combine(_state, titleRepository.observeTitles(), watchingRepository.observeAllWatchings()) { ui, titles, watchings ->
        val search = ui.searchText.trim()
        val items = buildWatchedList(titles, watchings, ui.typeFilter, ui.sort)
            .filter { search.isEmpty() || it.title.name.contains(search, ignoreCase = true) }
            .map { WatchedItemUi(it.title.id, it.title.name, it.title.posterUrl, it.rating?.stars) }
        ui.copy(items = items, totalCount = titles.count { it.status == TitleStatus.WATCHED })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), WatchedState())

    fun onAction(action: WatchedAction) {
        when (action) {
            is WatchedAction.OnTypeFilterChange -> _state.update { it.copy(typeFilter = action.type) }
            is WatchedAction.OnSortChange -> _state.update { it.copy(sort = action.sort) }
            WatchedAction.OnSearchClick -> _state.update { it.copy(isSearchOpen = !it.isSearchOpen, searchText = "") }
            is WatchedAction.OnSearchTextChange -> _state.update { it.copy(searchText = action.text) }
            is WatchedAction.OnTitleClick -> Unit
        }
    }
}
