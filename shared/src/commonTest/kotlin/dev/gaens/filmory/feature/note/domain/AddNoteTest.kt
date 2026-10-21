package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.testing.FakeNoteRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddNoteTest {
    private val notes = FakeNoteRepository()
    private val watchings = FakeWatchingRepository()
    private val addNote = AddNote(notes, watchings, SequentialIdGenerator(), FixedClock(at(500)))

    private fun draft(watchingId: String? = null) = NoteDraft(
        titleId = "title",
        watchingId = watchingId,
        episodeId = null,
        type = NoteType.SCENE,
        text = "great scene",
        timestampMillis = 61_000,
        imagePath = null,
        createdAt = at(300),
    )

    @Test
    fun `a note on a title without a watching creates one with no start date`() = runTest {
        val result = addNote(draft())

        val note = (result as Result.Success).data
        val watching = watchings.getWatchings("title").single()
        assertEquals(watching.id, note.watchingId)
        assertNull(watching.startedAt)
        assertEquals(WatchingState.IN_PROGRESS, watching.state)
        assertEquals(note, notes.notes.value.single())
    }

    @Test
    fun `a note without a given watching goes on the latest one`() = runTest {
        watchings.upsertWatching(aWatching(id = "old", createdAt = at(1)))
        watchings.upsertWatching(aWatching(id = "new", createdAt = at(2)))

        val note = (addNote(draft()) as Result.Success).data

        assertEquals("new", note.watchingId)
        assertEquals(2, watchings.watchings.value.size)
    }

    @Test
    fun `a note on a given watching keeps it and its optional fields`() = runTest {
        watchings.upsertWatching(aWatching(id = "old", createdAt = at(1)))
        watchings.upsertWatching(aWatching(id = "new", createdAt = at(2)))

        val note = (addNote(draft(watchingId = "old")) as Result.Success).data

        assertEquals("old", note.watchingId)
        assertEquals(61_000L, note.timestampMillis)
        assertEquals(at(300), note.createdAt)
    }
}
