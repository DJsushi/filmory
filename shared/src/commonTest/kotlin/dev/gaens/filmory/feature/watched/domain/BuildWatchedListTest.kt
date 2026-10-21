package dev.gaens.filmory.feature.watched.domain

import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildWatchedListTest {
    private val build = BuildWatchedList()
    private val titles = listOf(
        aTitle(id = "old", type = TitleType.MOVIE, status = TitleStatus.WATCHED).copy(name = "Old"),
        aTitle(id = "new", status = TitleStatus.WATCHED).copy(name = "New"),
        aTitle(id = "nodate", status = TitleStatus.WATCHED).copy(name = "A no date"),
        aTitle(id = "watching", status = TitleStatus.WATCHING),
    )
    private val watchings = listOf(
        aWatching(id = "o", titleId = "old", state = WatchingState.FINISHED, finishedAt = at(10)).copy(rating = Rating(10)),
        aWatching(id = "n1", titleId = "new", state = WatchingState.FINISHED, finishedAt = at(50), createdAt = at(1)),
        aWatching(id = "n2", titleId = "new", state = WatchingState.DROPPED, createdAt = at(60)).copy(rating = Rating(4)),
    )

    @Test
    fun `lists watched titles by last finish date, undated last`() {
        val entries = build(titles, watchings)

        assertEquals(listOf("new", "old", "nodate"), entries.map { it.title.id })
        assertEquals(at(50), entries.first().finishedAt)
    }

    @Test
    fun `a dropped rewatch keeps the title with the rewatch rating`() {
        assertEquals(Rating(4), build(titles, watchings).first { it.title.id == "new" }.rating)
    }

    @Test
    fun `filters by type and sorts by rating or name`() {
        assertEquals(listOf("old"), build(titles, watchings, type = TitleType.MOVIE).map { it.title.id })
        assertEquals(listOf("old", "new", "nodate"), build(titles, watchings, sort = WatchedSort.RATING).map { it.title.id })
        assertEquals(listOf("nodate", "new", "old"), build(titles, watchings, sort = WatchedSort.NAME).map { it.title.id })
    }
}
