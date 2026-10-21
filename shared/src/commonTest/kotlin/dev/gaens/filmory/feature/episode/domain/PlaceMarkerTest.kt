package dev.gaens.filmory.feature.episode.domain

import dev.gaens.filmory.feature.settings.domain.MarkerTarget
import dev.gaens.filmory.feature.settings.domain.SeriesSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceMarkerTest {
    private val episodes = listOf("e1", "e2", "e3", "e4")
    private val following = SeriesSettings(markerFollowsTicks = true)

    @Test
    fun `the marker stays when ticks do not move it`() {
        assertEquals("e2", placeMarker(episodes, "e2", "e3", SeriesSettings()))
    }

    @Test
    fun `a tick moves the marker to the episode after it`() {
        assertEquals("e4", placeMarker(episodes, "e2", "e3", following))
    }

    @Test
    fun `ticking the episode with the marker moves it on`() {
        assertEquals("e3", placeMarker(episodes, "e2", "e2", following))
    }

    @Test
    fun `a tick can put the marker on the ticked episode`() {
        val settings = following.copy(markerTarget = MarkerTarget.TICKED)

        assertEquals("e3", placeMarker(episodes, "e2", "e3", settings))
    }

    @Test
    fun `the last episode keeps the marker on itself`() {
        assertEquals("e4", placeMarker(episodes, "e3", "e4", following))
    }

    @Test
    fun `an older episode leaves the marker unless moving backwards is allowed`() {
        assertEquals("e4", placeMarker(episodes, "e4", "e1", following))
        assertEquals("e2", placeMarker(episodes, "e4", "e1", following.copy(markerMovesBackwards = true)))
    }

    @Test
    fun `a series without a marker gets one on the first tick`() {
        assertEquals("e3", placeMarker(episodes, null, "e2", following))
    }

    @Test
    fun `an unknown episode changes nothing`() {
        assertEquals("e2", placeMarker(episodes, "e2", "gone", following))
    }
}
