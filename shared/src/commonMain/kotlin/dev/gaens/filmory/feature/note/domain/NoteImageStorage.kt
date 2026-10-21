package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result

/**
 * App-private storage for images attached to notes. Notes keep a path relative to this storage, so
 * backups can carry the files and a restore on another device still finds them.
 */
interface NoteImageStorage {
    suspend fun save(bytes: ByteArray): Result<String, DataError.Local>
    suspend fun delete(relativePath: String)

    /** Absolute path for showing the image. */
    fun resolve(relativePath: String): String
}
