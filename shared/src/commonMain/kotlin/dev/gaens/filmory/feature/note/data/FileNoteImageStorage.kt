package dev.gaens.filmory.feature.note.data

import dev.gaens.filmory.core.data.AppDirectories
import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path

internal const val NOTE_IMAGES_DIRECTORY = "note_images"

/** Stores note images as files under [rootDirectory]/note_images. */
class FileNoteImageStorage(
    appDirectories: AppDirectories,
    private val idGenerator: IdGenerator,
    private val fileSystem: FileSystem,
) : NoteImageStorage {
    private val rootDirectory: Path = appDirectories.files

    override suspend fun save(bytes: ByteArray): Result<String, DataError.Local> = withContext(Dispatchers.IO) {
        val relativePath = "$NOTE_IMAGES_DIRECTORY/${idGenerator.newId()}.jpg"
        try {
            fileSystem.createDirectories(rootDirectory / NOTE_IMAGES_DIRECTORY)
            fileSystem.write(rootDirectory / relativePath) { write(bytes) }
            Result.Success(relativePath)
        } catch (e: IOException) {
            val isDiskFull = e.message?.contains("space", ignoreCase = true) == true
            Result.Error(if (isDiskFull) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN)
        }
    }

    override suspend fun delete(relativePath: String) = withContext(Dispatchers.IO) {
        fileSystem.delete(rootDirectory / relativePath, mustExist = false)
    }

    override fun resolve(relativePath: String): String = (rootDirectory / relativePath).toString()
}
