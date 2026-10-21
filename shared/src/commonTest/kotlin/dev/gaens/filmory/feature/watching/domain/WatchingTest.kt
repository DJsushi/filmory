package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatchingTest {
    private val now = at(100)

    @Test
    fun `finishing sets the finish date`() {
        val finished = aWatching().withState(WatchingState.FINISHED, now)

        assertEquals(now, finished.finishedAt)
    }

    @Test
    fun `starting sets the start date only when it is empty`() {
        val fresh = aWatching(state = WatchingState.ON_HOLD, startedAt = null)
        val resumed = aWatching(state = WatchingState.ON_HOLD, startedAt = at(1))

        assertEquals(now, fresh.withState(WatchingState.IN_PROGRESS, now).startedAt)
        assertEquals(at(1), resumed.withState(WatchingState.IN_PROGRESS, now).startedAt)
    }

    @Test
    fun `resuming a finished watching clears the finish date`() {
        val finished = aWatching(state = WatchingState.FINISHED, finishedAt = at(50))

        assertNull(finished.withState(WatchingState.IN_PROGRESS, now).finishedAt)
    }

    @Test
    fun `dropping keeps the dates`() {
        val dropped = aWatching(startedAt = at(1)).withState(WatchingState.DROPPED, now)

        assertEquals(at(1), dropped.startedAt)
        assertNull(dropped.finishedAt)
    }

    @Test
    fun `setting the same state changes nothing`() {
        val finished = aWatching(state = WatchingState.FINISHED, finishedAt = at(50))

        assertEquals(finished, finished.withState(WatchingState.FINISHED, now))
    }

    @Test
    fun `in progress without a start date has not started`() {
        assertFalse(aWatching(startedAt = null).hasStarted)
        assertTrue(aWatching(startedAt = at(1)).hasStarted)
        assertTrue(aWatching(state = WatchingState.DROPPED, startedAt = null).hasStarted)
    }

    @Test
    fun `rating accepts half steps from 0 to 5 stars and rejects the rest`() {
        assertEquals(3.5f, Rating(7).stars)
        assertEquals(0f, Rating(0).stars)
        assertEquals(5f, Rating(10).stars)
        assertFailsWith<IllegalArgumentException> { Rating(11) }
        assertFailsWith<IllegalArgumentException> { Rating(-1) }
    }
}
