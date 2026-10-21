package dev.gaens.filmory.feature.backup.data

import dev.gaens.filmory.core.data.AppDirectories
import dev.gaens.filmory.core.database.dao.BackupDao
import dev.gaens.filmory.core.database.entity.DatabaseSnapshot
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.backup.domain.BackupError
import dev.gaens.filmory.feature.backup.domain.BackupRepository
import dev.gaens.filmory.feature.note.data.NOTE_IMAGES_DIRECTORY
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.openZip
import kotlin.time.Clock

internal const val BACKUP_EXTENSION = "filmorybkp"
internal const val BACKUP_FORMAT_VERSION = 1
private const val MANIFEST_ENTRY = "manifest.json"
private const val DATA_ENTRY = "data.json"
private const val EXPORTS_DIRECTORY = "exports"

@Serializable
internal data class BackupManifest(val formatVersion: Int, val createdAtEpochMillis: Long)

/**
 * A backup is a zip with `manifest.json` (format version), `data.json` (every table) and the note
 * images under `note_images/`. Restore validates the whole archive before touching existing data.
 */
class ZipBackupRepository(
    private val backupDao: BackupDao,
    private val directories: AppDirectories,
    private val fileSystem: FileSystem,
    private val clock: Clock,
) : BackupRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createBackup(): Result<String, BackupError> = withContext(Dispatchers.IO) {
        try {
            val now = clock.now()
            val snapshot = backupDao.snapshot()
            val exports = directories.cache / EXPORTS_DIRECTORY
            fileSystem.createDirectories(exports)
            val target = exports / "filmory-${now.toLocalDateTime(TimeZone.UTC).date}.$BACKUP_EXTENSION"
            fileSystem.write(target) {
                StoredZipWriter(this).apply {
                    add(MANIFEST_ENTRY, json.encodeToString(BackupManifest(BACKUP_FORMAT_VERSION, now.toEpochMilliseconds())).encodeToByteArray())
                    add(DATA_ENTRY, json.encodeToString(snapshot).encodeToByteArray())
                    snapshot.notes.mapNotNull { it.imagePath }.distinct().forEach { path ->
                        val file = directories.files / path
                        if (fileSystem.exists(file)) add(path, fileSystem.read(file) { readByteArray() })
                    }
                    finish()
                }
            }
            Result.Success(target.toString())
        } catch (e: IOException) {
            Result.Error(BackupError.STORAGE)
        }
    }

    override suspend fun restore(path: String): EmptyResult<BackupError> = withContext(Dispatchers.IO) {
        try {
            val zip = fileSystem.openZip(path.toPath())
            val manifest = readJson<BackupManifest>(zip, MANIFEST_ENTRY) ?: return@withContext Result.Error(BackupError.INVALID_FILE)
            if (manifest.formatVersion > BACKUP_FORMAT_VERSION) return@withContext Result.Error(BackupError.UNSUPPORTED_VERSION)
            val snapshot = readJson<DatabaseSnapshot>(zip, DATA_ENTRY) ?: return@withContext Result.Error(BackupError.INVALID_FILE)

            val images = snapshot.notes.mapNotNull { it.imagePath }.distinct().filter { isSafeImagePath(it) }
            val imageBytes = images.mapNotNull { name -> readBytes(zip, name)?.let { name to it } }
            backupDao.replaceAll(snapshot)
            replaceImages(imageBytes)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.Error(BackupError.INVALID_FILE)
        } catch (e: SerializationException) {
            Result.Error(BackupError.INVALID_FILE)
        } catch (e: IllegalArgumentException) {
            Result.Error(BackupError.INVALID_FILE)
        } catch (e: Exception) {
            Result.Error(BackupError.STORAGE)
        }
    }

    private inline fun <reified T> readJson(zip: FileSystem, name: String): T? =
        readBytes(zip, name)?.let { json.decodeFromString<T>(it.decodeToString()) }

    private fun readBytes(zip: FileSystem, name: String): ByteArray? {
        val entry = "/$name".toPath()
        return if (zip.exists(entry)) zip.read(entry) { readByteArray() } else null
    }

    /** Image paths come from the archive, so only plain files inside the images folder are accepted. */
    private fun isSafeImagePath(path: String): Boolean =
        path.startsWith("$NOTE_IMAGES_DIRECTORY/") && !path.contains("..") && !path.contains('\\')

    private fun replaceImages(images: List<Pair<String, ByteArray>>) {
        val directory: Path = directories.files / NOTE_IMAGES_DIRECTORY
        fileSystem.deleteRecursively(directory, mustExist = false)
        if (images.isEmpty()) return
        fileSystem.createDirectories(directory)
        images.forEach { (name, bytes) -> fileSystem.write(directories.files / name) { write(bytes) } }
    }
}
