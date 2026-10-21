package dev.gaens.filmory.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.asUiText
import dev.gaens.filmory.feature.search.domain.AddTitleFromRemote
import dev.gaens.filmory.feature.search.domain.RemoteTitleDataSource
import dev.gaens.filmory.feature.search.domain.TitleSearchResult
import dev.gaens.filmory.feature.title.domain.TitleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val MIN_QUERY_LENGTH = 2

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val remote: RemoteTitleDataSource,
    private val addTitleFromRemote: AddTitleFromRemote,
    titleRepository: TitleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    private val rawResults = MutableStateFlow<List<TitleSearchResult>>(emptyList())

    val state = combine(_state, rawResults, titleRepository.observeTitles()) { ui, results, titles ->
        val inLibrary = titles.mapNotNullTo(HashSet()) { title -> title.tmdbId?.let { it to title.type } }
        ui.copy(results = results.map { SearchResultUi(it, (it.tmdbId to it.type) in inLibrary) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SearchState())

    private val eventChannel = Channel<SearchEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        _state.map { it.query.trim() }
            .distinctUntilChanged()
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .mapLatest { query -> search(query) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
            is SearchAction.OnResultClick -> add(action.result)
            else -> Unit
        }
    }

    private suspend fun search(query: String) {
        if (query.length < MIN_QUERY_LENGTH) {
            rawResults.value = emptyList()
            _state.update { it.copy(isLoading = false, error = null) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        when (val result = remote.search(query)) {
            is Result.Success -> {
                rawResults.value = result.data
                _state.update { it.copy(isLoading = false) }
            }
            is Result.Error -> _state.update { it.copy(isLoading = false, error = result.error.asUiText()) }
        }
    }

    private fun add(result: TitleSearchResult) {
        if (_state.value.addingTmdbId != null) return
        _state.update { it.copy(addingTmdbId = result.tmdbId) }
        viewModelScope.launch {
            val added = addTitleFromRemote(result)
            _state.update { it.copy(addingTmdbId = null) }
            when (added) {
                is Result.Success -> eventChannel.send(SearchEvent.OpenTitle(added.data))
                is Result.Error -> eventChannel.send(SearchEvent.ShowMessage(added.error.asUiText()))
            }
        }
    }
}
