package dev.gaens.filmory.feature.collection.presentation.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.onSuccess
import dev.gaens.filmory.feature.collection.domain.CollectionIcon
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.CreateCollection
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.collection.domain.collectionIconOf
import dev.gaens.filmory.feature.collection.presentation.toLook
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The "add to collection" sheet of one title, and the management mode that lets collections be changed from it. */
class CollectionPickerViewModel(
    private val titleId: String,
    private val collectionRepository: CollectionRepository,
    private val createCollection: CreateCollection,
    private val imageStorage: NoteImageStorage,
) : ViewModel() {

    private val local = MutableStateFlow(Local())

    /** The order being dragged, shown at once until the saved order catches up. */
    private data class Local(
        val isEditing: Boolean = false,
        val dialog: CollectionPickerDialog? = null,
        val draggedOrder: List<String>? = null,
    )

    val state = combine(collectionRepository.observeCollections(), local) { collections, local ->
        val shown = local.draggedOrder?.let { order -> collections.sortedBy { c -> order.indexOf(c.id).takeIf { it >= 0 } ?: Int.MAX_VALUE } }
            ?: collections
        CollectionPickerState(
            collections = shown.map { it.toItem() },
            isEditing = local.isEditing,
            dialog = local.dialog,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), CollectionPickerState())

    fun onAction(action: CollectionPickerAction) {
        when (action) {
            is CollectionPickerAction.OnToggle -> toggle(action.collectionId)
            CollectionPickerAction.OnEditClick -> local.update { it.copy(isEditing = true) }
            CollectionPickerAction.OnEditDone -> local.update { it.copy(isEditing = false, dialog = null) }
            is CollectionPickerAction.OnPinClick -> togglePin(action.collectionId)
            is CollectionPickerAction.OnMove -> move(action.collectionId, action.targetId)
            CollectionPickerAction.OnMoveDropped -> saveOrder()
            is CollectionPickerAction.OnRenameClick -> viewModelScope.launch {
                val name = find(action.collectionId)?.name ?: return@launch
                local.update { it.copy(dialog = CollectionPickerDialog.Rename(action.collectionId, name)) }
            }
            is CollectionPickerAction.OnRenameConfirm -> rename(action.name)
            is CollectionPickerAction.OnDeleteClick -> {
                if (action.collectionId != TitleCollection.DEFAULT_COLLECTION_ID) {
                    local.update { it.copy(dialog = CollectionPickerDialog.Delete(action.collectionId)) }
                }
            }
            CollectionPickerAction.OnDeleteConfirm -> delete()
            CollectionPickerAction.OnDialogDismiss -> local.update { it.copy(dialog = null) }
            is CollectionPickerAction.OnAddClick -> add(action.newName)
        }
    }

    private fun toggle(collectionId: String) {
        viewModelScope.launch {
            val collection = find(collectionId) ?: return@launch
            if (titleId in collection.titleIds) {
                collectionRepository.removeTitle(collectionId, titleId)
            } else {
                collectionRepository.addTitle(collectionId, titleId)
            }
        }
    }

    private fun togglePin(collectionId: String) {
        viewModelScope.launch {
            val collection = find(collectionId) ?: return@launch
            collectionRepository.upsertCollection(collection.copy(isPinned = !collection.isPinned))
        }
    }

    /** Collections can only be dragged within their own group: pinned ones always come first. */
    private fun move(collectionId: String, targetId: String) {
        val current = state.value.collections
        val from = current.find { it.id == collectionId } ?: return
        val to = current.find { it.id == targetId } ?: return
        if (from.isPinned != to.isPinned) return
        val ids = current.map { it.id }.toMutableList()
        ids.add(ids.indexOf(targetId), ids.removeAt(ids.indexOf(collectionId)))
        local.update { it.copy(draggedOrder = ids) }
    }

    private fun saveOrder() {
        val order = local.value.draggedOrder ?: return
        viewModelScope.launch {
            collectionRepository.reorder(order)
            local.update { it.copy(draggedOrder = null) }
        }
    }

    private fun rename(name: String) {
        val dialog = local.value.dialog as? CollectionPickerDialog.Rename ?: return
        local.update { it.copy(dialog = null) }
        viewModelScope.launch {
            val collection = find(dialog.collectionId) ?: return@launch
            if (name.isNotBlank()) collectionRepository.upsertCollection(collection.copy(name = name.trim()))
        }
    }

    private fun delete() {
        val dialog = local.value.dialog as? CollectionPickerDialog.Delete ?: return
        local.update { it.copy(dialog = null) }
        viewModelScope.launch {
            val collection = find(dialog.collectionId) ?: return@launch
            collectionRepository.deleteCollection(dialog.collectionId)
            // The collection's photo icon is no longer used by anything.
            (collectionIconOf(collection.iconKey) as? CollectionIcon.Photo)?.let { imageStorage.delete(it.path) }
        }
    }

    private fun add(newName: String) {
        viewModelScope.launch {
            createCollection(newName).onSuccess { id ->
                val name = find(id)?.name ?: newName
                local.update { it.copy(dialog = CollectionPickerDialog.Rename(id, name)) }
            }
        }
    }

    private suspend fun find(collectionId: String) = collectionRepository.observeCollections().first().find { it.id == collectionId }

    private fun TitleCollection.toItem() = CollectionPickerItemUi(
        id = id,
        name = name,
        isDefault = isDefault,
        isPinned = isPinned,
        containsTitle = titleId in titleIds,
        look = toLook(imageStorage::resolve),
    )
}
