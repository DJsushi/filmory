package dev.gaens.filmory.feature.backup.presentation

import dev.gaens.filmory.core.presentation.ui.UiText

data class BackupState(
    val isWorking: Boolean = false,
    /** A picked file waiting for the user to confirm replacing their data. */
    val pendingRestorePath: String? = null,
)

sealed interface BackupAction {
    data object OnSaveClick : BackupAction
    data object OnShareClick : BackupAction
    data class OnFilePicked(val path: String) : BackupAction
    data object OnRestoreConfirm : BackupAction
    data object OnRestoreDismiss : BackupAction
}

sealed interface BackupEvent {
    data class SaveFile(val path: String) : BackupEvent
    data class ShareFile(val path: String) : BackupEvent
    data class ShowMessage(val message: UiText) : BackupEvent
}
