package dev.gaens.filmory.feature.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppSettingsTest {
    @Test
    fun `no saved layout gives the default order`() {
        assertEquals(HomeBlock.entries, homeLayoutOf(emptyList()))
    }

    @Test
    fun `saved order is kept and unknown or repeated names are dropped`() {
        val layout = homeLayoutOf(listOf("LIBRARY", "GONE", "LIBRARY", "WISHLIST"))

        assertEquals(listOf(HomeBlock.LIBRARY, HomeBlock.WISHLIST), layout.take(2))
        assertEquals(HomeBlock.entries.toSet(), layout.toSet())
        assertEquals(HomeBlock.entries.size, layout.size)
    }

    @Test
    fun `blocks missing from the saved order go last in default order`() {
        val layout = homeLayoutOf(listOf("DROPPED", "WATCH_LATER"))

        assertEquals(
            listOf(HomeBlock.DROPPED, HomeBlock.WATCH_LATER) +
                HomeBlock.entries.filter { it != HomeBlock.DROPPED && it != HomeBlock.WATCH_LATER },
            layout,
        )
    }
}
