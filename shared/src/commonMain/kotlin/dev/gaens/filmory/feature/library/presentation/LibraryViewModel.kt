package dev.gaens.filmory.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.library.domain.BuildLibrary
import dev.gaens.filmory.feature.library.domain.LibraryEntry
import dev.gaens.filmory.feature.library.domain.LibraryQuery
import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val preset: LibraryPreset,
    private val collectionId: String?,
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val collectionRepository: CollectionRepository,
    private val settingsRepository: SettingsRepository,
    private val buildLibrary: BuildLibrary,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(LibraryState(preset = preset))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeLibrary()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), LibraryState(preset = preset))

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnStatusFilterChange -> _state.update { it.copy(statusFilter = action.status) }
            is LibraryAction.OnTypeFilterClick -> _state.update {
                it.copy(typeFilter = if (it.typeFilter == action.type) null else action.type)
            }
            is LibraryAction.OnSortChange -> updateSettings { it.copy(sort = action.sort) }
            LibraryAction.OnSearchClick -> _state.update { it.copy(isSearchOpen = !it.isSearchOpen, searchText = "") }
            is LibraryAction.OnSearchTextChange -> _state.update { it.copy(searchText = action.text) }
            is LibraryAction.OnDialogOpen -> _state.update { it.copy(dialog = action.dialog) }
            LibraryAction.OnDialogDismiss -> _state.update { it.copy(dialog = null) }
            is LibraryAction.OnSettingsChange -> updateSettings { action.settings }
            else -> Unit
        }
    }

    private fun observeLibrary() {
        val collection = collectionRepository.observeCollections().map { list -> list.find { it.id == collectionId } }
        val filters = _state.map { Filters(it.statusFilter, it.typeFilter, it.searchText) }.distinctUntilChanged()
        combine(
            titleRepository.observeTitles(),
            watchingRepository.observeAllWatchings(),
            collection,
            settingsRepository.observeSettings(),
            filters,
        ) { titles, watchings, collection, settings, filters ->
            val query = LibraryQuery(
                status = presetStatus() ?: filters.status,
                type = filters.type,
                onlyWishlisted = preset == LibraryPreset.WISHLIST,
                onlyTitleIds = if (preset == LibraryPreset.COLLECTION) collection?.titleIds.orEmpty().toSet() else null,
                includeWishlist = settings.library.includeWishlist,
                searchText = filters.searchText,
                sort = settings.library.sort,
            )
            val items = buildLibrary(titles, watchings, query).map { it.toUi() }
            _state.update {
                it.copy(items = items, settings = settings.library)
            }
        }.launchIn(viewModelScope)
    }

    private fun presetStatus(): TitleStatus? = when (preset) {
        LibraryPreset.ON_HOLD -> TitleStatus.ON_HOLD
        LibraryPreset.DROPPED -> TitleStatus.DROPPED
        else -> null
    }

    private fun updateSettings(transform: (LibrarySettings) -> LibrarySettings) {
        viewModelScope.launch { settingsRepository.update { it.copy(library = transform(it.library)) } }
    }

    private data class Filters(val status: TitleStatus?, val type: TitleType?, val searchText: String)
}

private fun LibraryEntry.toUi() = LibraryItemUi(
    id = title.id,
    name = title.name,
    posterUrl = title.posterUrl,
    type = title.type,
    year = title.year,
    status = title.status,
    stars = rating?.stars,
)
