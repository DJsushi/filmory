package dev.gaens.filmory.feature.collection.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.onSuccess
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.presentation.toLook
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
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

/**
 * The collection itself: its name and what can be done to it. Its titles are listed by the library
 * part. A [isNew] collection opens with its name already being edited.
 */
class CollectionDetailViewModel(
    private val collectionId: String,
    isNew: Boolean,
    private val collectionRepository: CollectionRepository,
    private val imageStorage: NoteImageStorage,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(CollectionDetailState(isEditingName = isNew))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeCollection()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), _state.value)

    private val eventChannel = Channel<CollectionDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: CollectionDetailAction) {
        when (action) {
            CollectionDetailAction.OnNameTap -> viewModelScope.launch { eventChannel.send(CollectionDetailEvent.ShowRenameHint) }
            CollectionDetailAction.OnEditNameStart -> _state.update { it.copy(isEditingName = true) }
            is CollectionDetailAction.OnNameCommit -> commitName(action.name)
            CollectionDetailAction.OnDeleteClick -> _state.update { it.copy(isDeleteDialogOpen = true) }
            CollectionDetailAction.OnDeleteDismiss -> _state.update { it.copy(isDeleteDialogOpen = false) }
            CollectionDetailAction.OnDeleteConfirm -> delete()
        }
    }

    private fun observeCollection() {
        collectionRepository.observeCollections()
            .map { list -> list.find { it.id == collectionId } }
            .onEach { collection ->
                if (collection != null) {
                    _state.update {
                        it.copy(
                            name = collection.name,
                            isDefault = collection.isDefault,
                            look = collection.toLook(imageStorage::resolve),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun commitName(name: String) {
        _state.update { it.copy(isEditingName = false) }
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val collection = collectionRepository.observeCollections().first().find { it.id == collectionId } ?: return@launch
            if (collection.name != trimmed) collectionRepository.upsertCollection(collection.copy(name = trimmed))
        }
    }

    private fun delete() {
        _state.update { it.copy(isDeleteDialogOpen = false) }
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId).onSuccess { eventChannel.send(CollectionDetailEvent.Deleted) }
        }
    }
}
