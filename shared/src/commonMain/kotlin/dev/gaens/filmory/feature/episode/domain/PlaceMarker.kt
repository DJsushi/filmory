package dev.gaens.filmory.feature.episode.domain

import dev.gaens.filmory.feature.settings.domain.MarkerTarget
import dev.gaens.filmory.feature.settings.domain.SeriesSettings

/**
 * Where the "You are here" marker goes after the user ticks [tickedId]; [episodeIds] is every
 * episode of the series in watching order and [currentId] is where the marker is now, or null.
 *
 * Returns [currentId] unchanged when the marker should stay. Unticking never moves the marker, so
 * only ticks are passed in. Ticking a season counts as ticking its last episode.
 */
fun placeMarker(episodeIds: List<String>, currentId: String?, tickedId: String, settings: SeriesSettings): String? {
    if (!settings.markerFollowsTicks) return currentId
    val tickedIndex = episodeIds.indexOf(tickedId)
    if (tickedIndex == -1) return currentId

    val currentIndex = episodeIds.indexOf(currentId)
    if (currentIndex != -1 && tickedIndex < currentIndex && !settings.markerMovesBackwards) return currentId

    return when (settings.markerTarget) {
        MarkerTarget.TICKED -> tickedId
        // Stays on the last episode when there is no next one, like AdvanceEpisode does.
        MarkerTarget.NEXT -> episodeIds.getOrElse(tickedIndex + 1) { tickedId }
    }
}
