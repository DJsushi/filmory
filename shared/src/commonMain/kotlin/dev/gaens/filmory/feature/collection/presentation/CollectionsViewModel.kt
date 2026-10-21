package dev.gaens.filmory.feature.collection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.onSuccess
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.CreateCollection
import dev.gaens.filmory.feature.title.domain.TitleRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionsViewModel(
    private val collectionRepository: CollectionRepository,
    private val titleRepository: TitleRepository,
    private val createCollection: CreateCollection,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(CollectionsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeCollections()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), CollectionsState())

    private val eventChannel = Channel<CollectionsEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: CollectionsAction) {
        when (action) {
            is CollectionsAction.OnAddClick -> addCollection(action.newName)
            else -> Unit
        }
    }

    private fun observeCollections() {
        combine(collectionRepository.observeCollections(), titleRepository.observeTitles()) { collections, titles ->
            val titleIds = titles.mapTo(HashSet()) { it.id }
            val cards = collections.map { collection ->
                CollectionCardUi(
                    id = collection.id,
                    name = collection.name,
                    isDefault = collection.isDefault,
                    titleCount = collection.titleIds.count { it in titleIds },
                )
            }
            _state.update { it.copy(collections = cards) }
        }.launchIn(viewModelScope)
    }

    private fun addCollection(name: String) {
        viewModelScope.launch {
            createCollection(name).onSuccess { eventChannel.send(CollectionsEvent.OpenNewCollection(it)) }
        }
    }
}
