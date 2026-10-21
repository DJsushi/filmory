package dev.gaens.filmory.feature.watching.domain

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

class SaveAndEnsureWatchingTest {
    private val clock = FixedClock(at(500))
    private val titles = FakeTitleRepository(listOf(aTitle()))
    private val watchings = FakeWatchingRepository()
    private val sync = SyncTitleStatus(titles, watchings, DetermineTitleStatus())
    private val ensure = EnsureActiveWatching(watchings, StartWatching(watchings, sync, SequentialIdGenerator(), clock), sync, clock)

    @Test
    fun `a finish date before the start date is rejected`() = runTest {
        val result = SaveWatching(watchings, sync)(aWatching(startedAt = at(10), finishedAt = at(5)))

        assertEquals(Result.Error(SaveWatchingError.FinishBeforeStart), result)
        assertEquals(emptyList(), watchings.watchings.value)
    }

    @Test
    fun `saving a finished watching makes the title watched`() = runTest {
        SaveWatching(watchings, sync)(aWatching(state = WatchingState.FINISHED, finishedAt = at(20)))

        assertEquals(TitleStatus.WATCHED, titles.getTitle("title")?.status)
    }

    @Test
    fun `episode actions on a title without watchings start one`() = runTest {
        val watching = (ensure("title") as Result.Success).data

        assertEquals(at(500), watching.startedAt)
        assertEquals(TitleStatus.WATCHING, titles.getTitle("title")?.status)
    }

    @Test
    fun `a watching created by a note gets started`() = runTest {
        watchings.upsertWatching(aWatching(startedAt = null))

        val watching = (ensure("title") as Result.Success).data

        assertEquals("watching", watching.id)
        assertEquals(at(500), watching.startedAt)
        assertEquals(TitleStatus.WATCHING, titles.getTitle("title")?.status)
    }

    @Test
    fun `a started watching is returned untouched`() = runTest {
        watchings.upsertWatching(aWatching(state = WatchingState.ON_HOLD, startedAt = at(1)))

        assertEquals(at(1), (ensure("title") as Result.Success).data.startedAt)
    }
}
