package dev.gaens.filmory.feature.episode.domain

import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.aSeriesStructure
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdvanceEpisodeTest {
    private val watchings = FakeWatchingRepository()
    private val episodes = FakeEpisodeRepository(aSeriesStructure())
    private val advance = AdvanceEpisode(watchings, episodes, SequentialIdGenerator(), FixedClock(at(500)))

    private suspend fun pointer() = watchings.getWatching("watching")?.currentEpisodeId

    @Test
    fun `without a pointer it points at the first episode and checks nothing`() = runTest {
        watchings.upsertWatching(aWatching())

        advance("watching")

        assertEquals("e1", pointer())
        assertTrue(episodes.checks.value.isEmpty())
    }

    @Test
    fun `it checks the current episode and moves to the next one`() = runTest {
        watchings.upsertWatching(aWatching(currentEpisodeId = "e1"))

        advance("watching")

        assertEquals("e2", pointer())
        assertEquals(listOf("e1"), episodes.checks.value.map { it.episodeId })
        assertEquals(at(500), episodes.checks.value.single().checkedAt)
    }

    @Test
    fun `it moves across a season boundary`() = runTest {
        watchings.upsertWatching(aWatching(currentEpisodeId = "e2"))

        advance("watching")

        assertEquals("e3", pointer())
    }

    @Test
    fun `on the last episode it checks it and stays there`() = runTest {
        watchings.upsertWatching(aWatching(currentEpisodeId = "e3"))

        advance("watching")
        advance("watching")

        assertEquals("e3", pointer())
        assertEquals(listOf("e3"), episodes.checks.value.map { it.episodeId })
    }
}
