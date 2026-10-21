package dev.gaens.filmory.feature.backup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.backup.domain.BackupError
import dev.gaens.filmory.feature.backup.domain.BackupRepository
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.backup_error_invalid
import dev.gaens.filmory.resources.backup_error_storage
import dev.gaens.filmory.resources.backup_error_version
import dev.gaens.filmory.resources.backup_restored
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<BackupEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: BackupAction) {
        when (action) {
            BackupAction.OnExportClick -> export()
            is BackupAction.OnFilePicked -> _state.update { it.copy(pendingRestorePath = action.path) }
            BackupAction.OnRestoreConfirm -> restore()
            BackupAction.OnRestoreDismiss -> _state.update { it.copy(pendingRestorePath = null) }
        }
    }

    private fun export() {
        if (_state.value.isWorking) return
        _state.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            val result = backupRepository.createBackup()
            _state.update { it.copy(isWorking = false) }
            eventChannel.send(
                when (result) {
                    is Result.Success -> BackupEvent.ShareFile(result.data)
                    is Result.Error -> BackupEvent.ShowMessage(result.error.asUiText())
                },
            )
        }
    }

    private fun restore() {
        val path = _state.value.pendingRestorePath ?: return
        _state.update { it.copy(isWorking = true, pendingRestorePath = null) }
        viewModelScope.launch {
            val result = backupRepository.restore(path)
            _state.update { it.copy(isWorking = false) }
            eventChannel.send(
                BackupEvent.ShowMessage(
                    when (result) {
                        is Result.Success -> UiText.Resource(Res.string.backup_restored)
                        is Result.Error -> result.error.asUiText()
                    },
                ),
            )
        }
    }

    private fun BackupError.asUiText() = UiText.Resource(
        when (this) {
            BackupError.INVALID_FILE -> Res.string.backup_error_invalid
            BackupError.UNSUPPORTED_VERSION -> Res.string.backup_error_version
            BackupError.STORAGE -> Res.string.backup_error_storage
        },
    )
}
