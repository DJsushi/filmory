package dev.gaens.filmory.feature.search.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.aTitle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AddTitleFromRemoteTest {
    private val titles = FakeTitleRepository()
    private val episodes = FakeEpisodeRepository()
    private var detailCalls = 0
    private var failWith: DataError.Network? = null

    private val remote = object : RemoteTitleDataSource {
        override suspend fun search(query: String) = Result.Success(emptyList<TitleSearchResult>())
        override suspend fun getDetails(tmdbId: Long, type: TitleType): Result<RemoteTitleDetails, DataError.Network> {
            detailCalls++
            failWith?.let { return Result.Error(it) }
            return Result.Success(
                RemoteTitleDetails(
                    tmdbId, type, "Severance", 2022, "Drama", null, "Dan Erickson", "Work.", null,
                    listOf(RemoteSeason(1, listOf(RemoteEpisode(1, "Pilot", 57), RemoteEpisode(2, null, null)))),
                ),
            )
        }
    }
    private val add = AddTitleFromRemote(remote, titles, episodes, SequentialIdGenerator(), FixedClock())
    private val result = TitleSearchResult(95396, TitleType.SERIES, "Severance", 2022, null, null)

    @Test
    fun `adding a series stores the title with all its episodes`() = runTest {
        val id = (add(result) as Result.Success).data

        val title = titles.getTitle(id)
        assertEquals(95396L, title?.tmdbId)
        assertEquals(TitleStatus.NOT_WATCHED, title?.status)
        assertEquals("Dan Erickson", title?.creator)
        assertEquals(listOf("Pilot", null), episodes.getSeasons(id).single().episodes.map { it.name })
    }

    @Test
    fun `a title already in the library is reused`() = runTest {
        titles.upsertTitle(aTitle(id = "existing").copy(tmdbId = 95396))

        assertEquals(Result.Success("existing"), add(result))
        assertEquals(0, detailCalls)
    }

    @Test
    fun `a network error adds nothing`() = runTest {
        failWith = DataError.Network.NO_INTERNET

        assertEquals(Result.Error(DataError.Network.NO_INTERNET), add(result))
        assertEquals(emptyList(), titles.titles.value)
    }
}
