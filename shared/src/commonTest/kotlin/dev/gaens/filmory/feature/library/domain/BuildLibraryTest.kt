package dev.gaens.filmory.feature.library.domain

import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildLibraryTest {
    private val build = BuildLibrary()
    private val titles = listOf(
        aTitle(id = "a", type = TitleType.MOVIE, status = TitleStatus.WATCHED).copy(name = "Arrival", addedAt = at(1)),
        aTitle(id = "b", status = TitleStatus.WATCHING).copy(name = "bear", addedAt = at(3)),
        aTitle(id = "w").copy(name = "Wish", isWishlisted = true, addedAt = at(2)),
        aTitle(id = "x", status = TitleStatus.WATCHED).copy(name = "Xwatched", isWishlisted = true, addedAt = at(0)),
    )
    private val watchings = listOf(
        aWatching(id = "a1", titleId = "a", createdAt = at(10)).copy(rating = Rating(6)),
        aWatching(id = "a2", titleId = "a", createdAt = at(20), startedAt = at(20)).copy(rating = Rating(9)),
        aWatching(id = "b1", titleId = "b", createdAt = at(5), startedAt = at(5)).copy(rating = Rating(7)),
    )

    private fun ids(query: LibraryQuery) = build(titles, watchings, query).map { it.title.id }

    @Test
    fun `filters by status, type, wishlist and search`() {
        assertEquals(listOf("b"), ids(LibraryQuery(status = TitleStatus.WATCHING)))
        assertEquals(listOf("a"), ids(LibraryQuery(type = TitleType.MOVIE)))
        assertEquals(setOf("w", "x"), ids(LibraryQuery(onlyWishlisted = true)).toSet())
        assertEquals(listOf("b"), ids(LibraryQuery(searchText = " BE ")))
    }

    @Test
    fun `only-wishlisted titles leave the library when wishlist titles are excluded`() {
        val ids = ids(LibraryQuery(includeWishlist = false, sort = LibrarySort.NAME))

        assertEquals(listOf("a", "b", "x"), ids)
    }

    @Test
    fun `the rating shown is the latest watching's`() {
        val arrival = build(titles, watchings, LibraryQuery()).first { it.title.id == "a" }

        assertEquals(Rating(9), arrival.rating)
    }

    @Test
    fun `sorts by activity, added date, name and rating`() {
        assertEquals(listOf("a", "b", "w", "x"), ids(LibraryQuery(sort = LibrarySort.UPDATED)))
        assertEquals(listOf("b", "w", "a", "x"), ids(LibraryQuery(sort = LibrarySort.ADDED)))
        assertEquals(listOf("a", "b", "w", "x"), ids(LibraryQuery(sort = LibrarySort.NAME)))
        assertEquals(listOf("a", "b", "w", "x"), ids(LibraryQuery(sort = LibrarySort.RATING)))
    }
}
