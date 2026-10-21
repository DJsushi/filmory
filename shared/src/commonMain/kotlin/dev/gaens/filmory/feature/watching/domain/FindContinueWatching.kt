package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.feature.title.domain.Title

data class ContinueWatching(val title: Title, val watching: Watching)

/**
 * Titles with a started, in-progress watching, most recently started first. A watched title on a
 * rewatch shows here too. Watchings created by a note and never started do not count.
 */
class FindContinueWatching {
    operator fun invoke(titles: List<Title>, watchings: List<Watching>): List<ContinueWatching> {
        val titlesById = titles.associateBy { it.id }
        return watchings
            .filter { it.state == WatchingState.IN_PROGRESS && it.hasStarted }
            .groupBy { it.titleId }
            .mapNotNull { (titleId, inProgress) ->
                val title = titlesById[titleId] ?: return@mapNotNull null
                ContinueWatching(title, inProgress.maxBy { it.createdAt })
            }
            .sortedByDescending { it.watching.startedAt }
    }
}
