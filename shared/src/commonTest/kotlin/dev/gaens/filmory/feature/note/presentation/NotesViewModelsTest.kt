package dev.gaens.filmory.feature.note.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.note.domain.AddNote
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.note.presentation.editor.NoteEditorAction
import dev.gaens.filmory.feature.note.presentation.editor.NoteEditorViewModel
import dev.gaens.filmory.feature.note.presentation.list.NotesAction
import dev.gaens.filmory.feature.note.presentation.list.NotesViewModel
import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.testing.FakeNoteRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.aNote
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.aWatching
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NotesViewModelsTest : ViewModelTest() {
    private val titles = FakeTitleRepository(listOf(aTitle()))
    private val watchings = FakeWatchingRepository(listOf(aWatching()))
    private val episodes = FakeEpisodeRepository()
    private val notes = FakeNoteRepository(
        listOf(
            aNote(id = "scene").copy(type = NoteType.SCENE, isFavorite = true),
            aNote(id = "question").copy(type = NoteType.QUESTION),
        ),
    )
    private val images = FakeNoteImageStorage()
    private val ids = SequentialIdGenerator()
    private val clock = FixedClock()

    private fun editor(noteId: String? = null) = NoteEditorViewModel(
        "title", null, noteId, titles, watchings, episodes, notes, images,
        AddNote(notes, watchings, ids, clock), clock,
    )

    @Test
    fun `the notes tab filters by type and favorites`() = runTest {
        val viewModel = NotesViewModel(notes, titles, watchings, episodes)
        viewModel.state.test {
            assertEquals(2, expectMostRecentItem().totalCount)

            viewModel.onAction(NotesAction.OnTypeFilterChange(NoteType.QUESTION))
            assertEquals(listOf("question"), expectMostRecentItem().notes.map { it.id })

            viewModel.onAction(NotesAction.OnTypeFilterChange(null))
            viewModel.onAction(NotesAction.OnFavoritesClick)
            val state = expectMostRecentItem()
            assertEquals(listOf("scene"), state.notes.map { it.id })
            assertEquals(1, state.favoriteCount)
        }
    }

    @Test
    fun `a new note saves its parsed timestamp on the latest watching`() = runTest {
        val viewModel = editor()
        viewModel.onAction(NoteEditorAction.OnTextChange("A scene"))
        viewModel.onAction(NoteEditorAction.OnTimestampChange("1:02:03"))
        viewModel.onAction(NoteEditorAction.OnSaveClick)

        val saved = notes.notes.value.single { it.text == "A scene" }
        assertEquals(3_723_000L, saved.timestampMillis)
        assertEquals("watching", saved.watchingId)
    }

    @Test
    fun `an invalid timestamp blocks saving`() = runTest {
        val viewModel = editor()
        viewModel.onAction(NoteEditorAction.OnTextChange("A scene"))
        viewModel.onAction(NoteEditorAction.OnTimestampChange("1:99"))

        assertFalse(viewModel.state.value.canSave)
        viewModel.onAction(NoteEditorAction.OnSaveClick)
        assertEquals(2, notes.notes.value.size)
    }

    @Test
    fun `replacing an image on save removes the old file, closing drops unsaved images`() = runTest {
        images.files["note_images/old.jpg"] = byteArrayOf(0)
        notes.upsertNote(aNote(id = "edited").copy(imagePath = "note_images/old.jpg"))

        val viewModel = editor(noteId = "edited")
        viewModel.onAction(NoteEditorAction.OnImagePicked(byteArrayOf(1)))
        viewModel.onAction(NoteEditorAction.OnSaveClick)
        assertEquals(setOf("note_images/1.jpg"), images.files.keys)

        val discarded = editor(noteId = "edited")
        discarded.onAction(NoteEditorAction.OnImagePicked(byteArrayOf(2)))
        discarded.onAction(NoteEditorAction.OnCloseClick)
        assertEquals(setOf("note_images/1.jpg"), images.files.keys)
    }
}
