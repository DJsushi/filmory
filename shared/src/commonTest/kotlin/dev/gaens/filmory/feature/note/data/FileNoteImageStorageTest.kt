package dev.gaens.filmory.feature.note.data

import dev.gaens.filmory.core.data.AppDirectories
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.testing.SequentialIdGenerator
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FileNoteImageStorageTest {
    private val fileSystem = FakeFileSystem()
    private val storage = FileNoteImageStorage(AppDirectories("/app".toPath()), SequentialIdGenerator(), fileSystem)

    @Test
    fun `saved images get a relative path and can be deleted`() = runTest {
        val path = (storage.save(byteArrayOf(1, 2, 3)) as Result.Success).data

        assertEquals("note_images/id-1.jpg", path)
        assertEquals(3L, fileSystem.metadata("/app/note_images/id-1.jpg".toPath()).size)
        assertEquals("/app/note_images/id-1.jpg", storage.resolve(path))

        storage.delete(path)
        assertFalse(fileSystem.exists("/app/note_images/id-1.jpg".toPath()))
    }
}
