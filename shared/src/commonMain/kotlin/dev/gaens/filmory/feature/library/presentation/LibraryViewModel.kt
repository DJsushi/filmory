package dev.gaens.filmory.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.inDisplayOrder
import dev.gaens.filmory.feature.library.domain.BuildLibrary
import dev.gaens.filmory.feature.library.domain.LibraryQuery
import dev.gaens.filmory.feature.library.presentation.components.CollectionOptionUi
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
            is LibraryAction.OnCollectionFilterChange -> _state.update { it.copy(collectionFilter = action.collectionId) }
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
        val filters = _state
            .map { Filters(it.statusFilter, it.typeFilter, it.collectionFilter, it.searchText) }
            .distinctUntilChanged()
        combine(
            titleRepository.observeTitles(),
            watchingRepository.observeAllWatchings(),
            collectionRepository.observeCollections(),
            settingsRepository.observeSettings(),
            filters,
        ) { titles, watchings, collections, settings, filters ->
            // A collection screen is its own collection; a deleted collection no longer filters anything.
            val activeCollectionId = if (preset == LibraryPreset.COLLECTION) {
                collectionId
            } else {
                filters.collectionId?.takeIf { id -> collections.any { it.id == id } }
            }
            val query = LibraryQuery(
                status = presetStatus() ?: filters.status,
                type = filters.type,
                onlyWishlisted = preset == LibraryPreset.WISHLIST,
                onlyTitleIds = activeCollectionId?.let { id -> collections.find { it.id == id }?.titleIds.orEmpty().toSet() },
                includeWishlist = settings.library.includeWishlist,
                searchText = filters.searchText,
                sort = settings.library.sort,
            )
            val items = buildLibrary(titles, watchings, query).map { it.toUi() }
            _state.update {
                it.copy(
                    items = items,
                    settings = settings.library,
                    collections = collections.inDisplayOrder().map { collection -> CollectionOptionUi(collection.id, collection.name) },
                )
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

    private data class Filters(
        val status: TitleStatus?,
        val type: TitleType?,
        val collectionId: String?,
        val searchText: String,
    )
}
