package dev.gaens.filmory.feature.collection.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.onSuccess
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The collection itself: its name and what can be done to it. Its titles are listed by the library part. */
class CollectionDetailViewModel(
    private val collectionId: String,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(CollectionDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeCollection()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), CollectionDetailState())

    private val eventChannel = Channel<CollectionDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: CollectionDetailAction) {
        when (action) {
            is CollectionDetailAction.OnDialogOpen -> _state.update { it.copy(dialog = action.dialog) }
            CollectionDetailAction.OnDialogDismiss -> _state.update { it.copy(dialog = null) }
            is CollectionDetailAction.OnRenameConfirm -> rename(action.name)
            CollectionDetailAction.OnDeleteConfirm -> delete()
        }
    }

    private fun observeCollection() {
        collectionRepository.observeCollections()
            .map { list -> list.find { it.id == collectionId } }
            .onEach { collection ->
                if (collection != null) _state.update { it.copy(name = collection.name, isDefault = collection.isDefault) }
            }
            .launchIn(viewModelScope)
    }

    private fun rename(name: String) {
        _state.update { it.copy(dialog = null) }
        viewModelScope.launch {
            val collection = collectionRepository.observeCollections().first().find { it.id == collectionId } ?: return@launch
            collectionRepository.upsertCollection(collection.copy(name = name))
        }
    }

    private fun delete() {
        _state.update { it.copy(dialog = null) }
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId).onSuccess { eventChannel.send(CollectionDetailEvent.Deleted) }
        }
    }
}
