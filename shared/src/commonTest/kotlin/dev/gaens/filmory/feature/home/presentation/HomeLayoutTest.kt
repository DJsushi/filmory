package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.settings.domain.HomeBlock
import dev.gaens.filmory.feature.settings.domain.HomeBlock.CALENDAR
import dev.gaens.filmory.feature.settings.domain.HomeBlock.COLLECTIONS
import dev.gaens.filmory.feature.settings.domain.HomeBlock.DROPPED
import dev.gaens.filmory.feature.settings.domain.HomeBlock.LIBRARY
import dev.gaens.filmory.feature.settings.domain.HomeBlock.ON_HOLD
import dev.gaens.filmory.feature.settings.domain.HomeBlock.WATCH_LATER
import dev.gaens.filmory.feature.settings.domain.HomeBlock.WISHLIST
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeLayoutTest {
    @Test
    fun `default layout pairs the tiles and gives library the full width`() {
        val spans = homeSpans(HomeBlock.entries)

        assertEquals(
            mapOf(
                WATCH_LATER to 2, WISHLIST to 1, CALENDAR to 1, COLLECTIONS to 2,
                ON_HOLD to 1, DROPPED to 1, LIBRARY to 2,
            ),
            spans,
        )
    }

    @Test
    fun `a tile before a section takes the full width`() {
        val spans = homeSpans(listOf(WISHLIST, WATCH_LATER, CALENDAR, ON_HOLD))

        assertEquals(listOf(2, 2, 1, 1), spans.values.toList())
    }

    @Test
    fun `an odd run of tiles ends on a full width tile`() {
        val spans = homeSpans(listOf(WISHLIST, CALENDAR, ON_HOLD, COLLECTIONS, DROPPED, LIBRARY, WATCH_LATER))

        assertEquals(listOf(1, 1, 2, 2, 1, 1, 2), spans.values.toList())
    }

    @Test
    fun `move puts the block where the target was`() {
        val layout = listOf(WATCH_LATER, WISHLIST, CALENDAR, LIBRARY)

        assertEquals(listOf(WISHLIST, CALENDAR, WATCH_LATER, LIBRARY), layout.move(WATCH_LATER, CALENDAR))
        assertEquals(listOf(LIBRARY, WATCH_LATER, WISHLIST, CALENDAR), layout.move(LIBRARY, WATCH_LATER))
        assertEquals(layout, layout.move(WISHLIST, WISHLIST))
        assertEquals(layout, layout.move(DROPPED, WISHLIST))
    }

    @Test
    fun `moveBy stops at both ends`() {
        val layout = listOf(WATCH_LATER, WISHLIST, CALENDAR)

        assertEquals(listOf(WISHLIST, WATCH_LATER, CALENDAR), layout.moveBy(WATCH_LATER, 1))
        assertEquals(layout, layout.moveBy(WATCH_LATER, -1))
        assertEquals(layout, layout.moveBy(CALENDAR, 1))
    }
}
