package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals

class DetermineTitleStatusTest {
    private val determine = DetermineTitleStatus()

    @Test
    fun `a title with no watching is not watched`() {
        assertEquals(TitleStatus.NOT_WATCHED, determine(TitleType.SERIES, emptyList()))
    }

    @Test
    fun `a series mirrors the state of its latest watching until one is finished`() {
        val expected = mapOf(
            WatchingState.IN_PROGRESS to TitleStatus.WATCHING,
            WatchingState.ON_HOLD to TitleStatus.ON_HOLD,
            WatchingState.DROPPED to TitleStatus.DROPPED,
            WatchingState.FINISHED to TitleStatus.WATCHED,
        )

        expected.forEach { (state, status) ->
            assertEquals(status, determine(TitleType.SERIES, listOf(aWatching(state = state))), "state $state")
        }
    }

    @Test
    fun `a movie in progress is still not watched`() {
        val status = determine(TitleType.MOVIE, listOf(aWatching(state = WatchingState.IN_PROGRESS)))

        assertEquals(TitleStatus.NOT_WATCHED, status)
    }

    @Test
    fun `a watching created by a note without a start date does not start the title`() {
        val status = determine(TitleType.SERIES, listOf(aWatching(startedAt = null)))

        assertEquals(TitleStatus.NOT_WATCHED, status)
    }

    @Test
    fun `only the latest watching counts before any is finished`() {
        val watchings = listOf(
            aWatching(id = "old", state = WatchingState.ON_HOLD, createdAt = at(1)),
            aWatching(id = "new", state = WatchingState.DROPPED, createdAt = at(2)),
        )

        assertEquals(TitleStatus.DROPPED, determine(TitleType.SERIES, watchings))
    }

    @Test
    fun `a title stays watched when a rewatch is dropped or put on hold`() {
        listOf(WatchingState.DROPPED, WatchingState.ON_HOLD, WatchingState.IN_PROGRESS).forEach { state ->
            val watchings = listOf(
                aWatching(id = "first", state = WatchingState.FINISHED, createdAt = at(1)),
                aWatching(id = "rewatch", state = state, createdAt = at(2)),
            )

            assertEquals(TitleStatus.WATCHED, determine(TitleType.SERIES, watchings), "rewatch $state")
        }
    }
}
