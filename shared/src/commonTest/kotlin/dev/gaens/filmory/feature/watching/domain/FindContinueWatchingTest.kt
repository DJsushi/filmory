package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals

class FindContinueWatchingTest {
    private val find = FindContinueWatching()

    @Test
    fun `only started in-progress watchings count, newest start first`() {
        val titles = listOf(aTitle(id = "a"), aTitle(id = "b"), aTitle(id = "c"), aTitle(id = "d"))
        val watchings = listOf(
            aWatching(id = "wa", titleId = "a", startedAt = at(1)),
            aWatching(id = "wb", titleId = "b", startedAt = at(5)),
            aWatching(id = "wc", titleId = "c", startedAt = null),
            aWatching(id = "wd", titleId = "d", state = WatchingState.ON_HOLD),
        )

        assertEquals(listOf("b", "a"), find(titles, watchings).map { it.title.id })
    }

    @Test
    fun `a rewatch of a watched title continues too`() {
        val titles = listOf(aTitle(status = TitleStatus.WATCHED))
        val watchings = listOf(
            aWatching(id = "first", state = WatchingState.FINISHED, createdAt = at(1)),
            aWatching(id = "rewatch", startedAt = at(9), createdAt = at(9)),
        )

        assertEquals("rewatch", find(titles, watchings).single().watching.id)
    }
}
