package dev.gaens.filmory.feature.backup.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.core.presentation.ui.rememberFilePicker
import dev.gaens.filmory.core.presentation.ui.rememberFileSharer
import dev.gaens.filmory.feature.settings.presentation.SettingsCard
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.backup_description
import dev.gaens.filmory.resources.backup_export
import dev.gaens.filmory.resources.backup_restore
import dev.gaens.filmory.resources.backup_restore_confirm
import dev.gaens.filmory.resources.backup_restore_confirm_message
import dev.gaens.filmory.resources.backup_restore_confirm_title
import dev.gaens.filmory.resources.backup_title

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val BACKUP_MIME_TYPE = "application/octet-stream"

/** The backup and restore card on the Profile tab. Messages go to [onMessage]. */
@Composable
fun BackupSection(
    onMessage: suspend (UiText) -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shareFile = rememberFileSharer()
    val pickFile = rememberFilePicker { viewModel.onAction(BackupAction.OnFilePicked(it)) }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BackupEvent.ShareFile -> scope.launch { shareFile(event.path, BACKUP_MIME_TYPE) }
            is BackupEvent.ShowMessage -> scope.launch { onMessage(event.message) }
        }
    }
    SectionHeader(stringResource(Res.string.backup_title))
    SettingsCard(Modifier.rise(3)) {
        Text(
            text = stringResource(Res.string.backup_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.onAction(BackupAction.OnExportClick) }, enabled = !state.isWorking) {
                Text(stringResource(Res.string.backup_export))
            }
            FilledTonalButton(onClick = pickFile, enabled = !state.isWorking) {
                Text(stringResource(Res.string.backup_restore))
            }
        }
    }
    if (state.pendingRestorePath != null) {
        ConfirmDialog(
            title = stringResource(Res.string.backup_restore_confirm_title),
            message = stringResource(Res.string.backup_restore_confirm_message),
            confirmLabel = stringResource(Res.string.backup_restore_confirm),
            onConfirm = { viewModel.onAction(BackupAction.OnRestoreConfirm) },
            onDismiss = { viewModel.onAction(BackupAction.OnRestoreDismiss) },
        )
    }
}
