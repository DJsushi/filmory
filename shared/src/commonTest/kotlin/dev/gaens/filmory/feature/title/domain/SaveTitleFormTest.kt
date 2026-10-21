package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.aSeriesStructure
import dev.gaens.filmory.testing.aTitle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTitleFormTest {
    private val titles = FakeTitleRepository()
    private val episodes = FakeEpisodeRepository()
    private val save = SaveTitleForm(titles, episodes, SequentialIdGenerator(), FixedClock())

    private fun form(
        type: TitleType = TitleType.SERIES,
        name: String = "Show",
        year: Int? = 2020,
        runtime: Int? = null,
        seasons: List<Int> = listOf(3, 2),
    ) = TitleForm(type, name, year, null, null, runtime, null, seasons)

    @Test
    fun `invalid input is reported field by field`() {
        assertEquals(
            setOf(TitleFormError.BLANK_NAME, TitleFormError.INVALID_YEAR, TitleFormError.INVALID_EPISODE_COUNT),
            save.validate(form(name = " ", year = 1200, seasons = listOf(3, 0))),
        )
        assertEquals(setOf(TitleFormError.NO_SEASONS), save.validate(form(seasons = emptyList())))
        assertEquals(emptySet(), save.validate(form(type = TitleType.MOVIE, seasons = emptyList(), runtime = 120)))
    }

    @Test
    fun `a manual series gets numbered seasons and episodes`() = runTest {
        val id = (save(form(), existingId = null) as Result.Success).data

        val seasons = episodes.getSeasons(id)
        assertEquals(listOf(1, 2), seasons.map { it.number })
        assertEquals(listOf(1, 2, 3), seasons[0].episodes.map { it.number })
        assertEquals("Show", titles.getTitle(id)?.name)
    }

    @Test
    fun `editing counts keeps existing episode ids and names`() = runTest {
        titles.upsertTitle(aTitle())
        episodes.replaceSeasons("title", aSeriesStructure())

        save(form(seasons = listOf(3)), existingId = "title")

        val season = episodes.getSeasons("title").single()
        assertEquals("s1", season.id)
        assertEquals("e1", season.episodes[0].id)
        assertEquals("e2", season.episodes[1].id)
        assertEquals(3, season.episodes.size)
    }
}
