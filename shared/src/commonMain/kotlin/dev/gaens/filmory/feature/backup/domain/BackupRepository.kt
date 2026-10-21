package dev.gaens.filmory.feature.backup.domain

import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Error
import dev.gaens.filmory.core.domain.util.Result

enum class BackupError : Error {
    /** The file is not a Filmory backup, or it is damaged. */
    INVALID_FILE,

    /** The backup was made by a newer version of the app. */
    UNSUPPORTED_VERSION,

    /** Reading or writing storage failed, for example the disk is full. */
    STORAGE,
}

interface BackupRepository {
    /** Writes a `.filmorybkp` zip of the whole database and note images, and returns its absolute path. */
    suspend fun createBackup(): Result<String, BackupError>

    /** Replaces all data with the backup at [path]. On any error the existing data is kept. */
    suspend fun restore(path: String): EmptyResult<BackupError>
}
