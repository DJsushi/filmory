package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.title.domain.TitleStatus
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

class WatchingUseCasesTest {
    private val clock = FixedClock(at(500))
    private val titles = FakeTitleRepository(listOf(aTitle()))
    private val watchings = FakeWatchingRepository()
    private val sync = SyncTitleStatus(titles, watchings, DetermineTitleStatus())

    private suspend fun status() = titles.getTitle("title")?.status

    @Test
    fun `starting a watching sets the start date and makes a series watching`() = runTest {
        val result = StartWatching(watchings, sync, SequentialIdGenerator(), clock)("title")

        val watching = (result as Result.Success).data
        assertEquals(at(500), watching.startedAt)
        assertEquals(TitleStatus.WATCHING, status())
    }

    @Test
    fun `finishing a watching makes the title watched`() = runTest {
        watchings.upsertWatching(aWatching())

        ChangeWatchingState(watchings, sync, clock)("watching", WatchingState.FINISHED)

        assertEquals(TitleStatus.WATCHED, status())
        assertEquals(at(500), watchings.getWatching("watching")?.finishedAt)
    }

    @Test
    fun `dropping a rewatch keeps the title watched`() = runTest {
        watchings.upsertWatching(aWatching(id = "first", state = WatchingState.FINISHED, createdAt = at(1)))
        watchings.upsertWatching(aWatching(id = "rewatch", createdAt = at(2)))

        ChangeWatchingState(watchings, sync, clock)("rewatch", WatchingState.DROPPED)

        assertEquals(TitleStatus.WATCHED, status())
        assertEquals(WatchingState.DROPPED, watchings.getWatching("rewatch")?.state)
    }

    @Test
    fun `deleting the only watching returns the title to not watched`() = runTest {
        watchings.upsertWatching(aWatching(state = WatchingState.DROPPED))
        sync("title")

        DeleteWatching(watchings, sync)("watching")

        assertEquals(TitleStatus.NOT_WATCHED, status())
    }

    @Test
    fun `changing an unknown watching reports not found`() = runTest {
        val result = ChangeWatchingState(watchings, sync, clock)("missing", WatchingState.DROPPED)

        assertEquals(Result.Error(DataError.Local.NOT_FOUND), result)
    }
}
