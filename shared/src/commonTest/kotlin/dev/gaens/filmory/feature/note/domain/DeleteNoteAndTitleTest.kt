package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.feature.title.domain.DeleteTitle
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.testing.FakeNoteRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.aNote
import dev.gaens.filmory.testing.aTitle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteNoteAndTitleTest {
    private val images = FakeNoteImageStorage().apply {
        files["note_images/a.jpg"] = byteArrayOf(1)
        files["note_images/b.jpg"] = byteArrayOf(2)
    }
    private val notes = FakeNoteRepository(
        listOf(
            aNote(id = "a").copy(imagePath = "note_images/a.jpg"),
            aNote(id = "b").copy(imagePath = "note_images/b.jpg"),
        ),
    )

    @Test
    fun `deleting a note removes its image`() = runTest {
        DeleteNote(notes, images)("a")

        assertEquals(setOf("note_images/b.jpg"), images.files.keys)
        assertEquals(listOf("b"), notes.notes.value.map { it.id })
    }

    @Test
    fun `deleting a title removes the images of its notes`() = runTest {
        val titles = FakeTitleRepository(listOf(aTitle()))

        DeleteTitle(titles, notes, images)("title")

        assertTrue(images.files.isEmpty())
        assertTrue(titles.titles.value.isEmpty())
    }
}
