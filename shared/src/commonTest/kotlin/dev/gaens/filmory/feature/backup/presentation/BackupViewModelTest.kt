package dev.gaens.filmory.feature.backup.presentation

import app.cash.turbine.test
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.backup.domain.BackupError
import dev.gaens.filmory.feature.backup.domain.BackupRepository
import dev.gaens.filmory.testing.ViewModelTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class BackupViewModelTest : ViewModelTest() {
    private class FakeBackupRepository : BackupRepository {
        var restoreResult: EmptyResult<BackupError> = Result.Success(Unit)
        val restored = mutableListOf<String>()
        override suspend fun createBackup(): Result<String, BackupError> = Result.Success("/cache/b.filmorybkp")
        override suspend fun restore(path: String): EmptyResult<BackupError> {
            restored += path
            return restoreResult
        }
    }

    private val repository = FakeBackupRepository()
    private val viewModel = BackupViewModel(repository)

    @Test
    fun `exporting emits the file to share`() = runTest {
        viewModel.events.test {
            viewModel.onAction(BackupAction.OnExportClick)

            assertEquals(BackupEvent.ShareFile("/cache/b.filmorybkp"), awaitItem())
        }
    }

    @Test
    fun `a picked file is only restored after confirmation`() = runTest {
        viewModel.onAction(BackupAction.OnFilePicked("/cache/in.tmp"))
        assertEquals("/cache/in.tmp", viewModel.state.value.pendingRestorePath)
        assertEquals(emptyList(), repository.restored)

        viewModel.events.test {
            viewModel.onAction(BackupAction.OnRestoreConfirm)
            assertIs<BackupEvent.ShowMessage>(awaitItem())
        }
        assertEquals(listOf("/cache/in.tmp"), repository.restored)
        assertNull(viewModel.state.value.pendingRestorePath)
    }

    @Test
    fun `dismissing the confirmation restores nothing`() = runTest {
        viewModel.onAction(BackupAction.OnFilePicked("/cache/in.tmp"))
        viewModel.onAction(BackupAction.OnRestoreDismiss)

        assertNull(viewModel.state.value.pendingRestorePath)
        assertEquals(emptyList(), repository.restored)
    }
}
