package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChangeTitleStatusTest {
    private val clock = FixedClock(at(500))
    private val titles = FakeTitleRepository()
    private val watchings = FakeWatchingRepository()
    private val changeTitleStatus = ChangeTitleStatus(titles, watchings, SequentialIdGenerator(), clock)

    @Test
    fun `marking a movie watched creates a finished watching`() = runTest {
        titles.upsertTitle(aTitle(type = TitleType.MOVIE))

        val result = changeTitleStatus("title", TitleStatus.WATCHED)

        assertEquals(Result.Success(Unit), result)
        assertEquals(TitleStatus.WATCHED, titles.getTitle("title")?.status)
        val watching = watchings.getWatchings("title").single()
        assertEquals(WatchingState.FINISHED, watching.state)
        assertEquals(at(500), watching.finishedAt)
    }

    @Test
    fun `a movie cannot be set to watching`() = runTest {
        titles.upsertTitle(aTitle(type = TitleType.MOVIE))

        val result = changeTitleStatus("title", TitleStatus.WATCHING)

        assertEquals(Result.Error(ChangeTitleStatusError.MovieCannotBeWatching), result)
        assertEquals(TitleStatus.NOT_WATCHED, titles.getTitle("title")?.status)
    }

    @Test
    fun `the latest unfinished watching follows the manual status`() = runTest {
        titles.upsertTitle(aTitle(status = TitleStatus.WATCHING))
        watchings.upsertWatching(aWatching())

        changeTitleStatus("title", TitleStatus.ON_HOLD)

        assertEquals(WatchingState.ON_HOLD, watchings.getWatching("watching")?.state)
        assertEquals(TitleStatus.ON_HOLD, titles.getTitle("title")?.status)
    }

    @Test
    fun `a manual change never reopens a finished watching`() = runTest {
        titles.upsertTitle(aTitle(status = TitleStatus.WATCHED))
        watchings.upsertWatching(aWatching(state = WatchingState.FINISHED, finishedAt = at(9)))

        changeTitleStatus("title", TitleStatus.DROPPED)

        assertEquals(WatchingState.FINISHED, watchings.getWatching("watching")?.state)
        assertEquals(TitleStatus.DROPPED, titles.getTitle("title")?.status)
    }

    @Test
    fun `setting not watched leaves watchings alone`() = runTest {
        titles.upsertTitle(aTitle(status = TitleStatus.ON_HOLD))

        changeTitleStatus("title", TitleStatus.NOT_WATCHED)

        assertNull(watchings.getWatchings("title").firstOrNull())
        assertEquals(TitleStatus.NOT_WATCHED, titles.getTitle("title")?.status)
    }
}
