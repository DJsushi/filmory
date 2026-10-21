package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

/**
 * The title status rule. Once any watching is finished the title stays Watched. Before that it
 * mirrors the latest watching. Movies are never "Watching".
 */
class DetermineTitleStatus {
    operator fun invoke(type: TitleType, watchings: List<Watching>): TitleStatus {
        if (watchings.any { it.state == WatchingState.FINISHED }) return TitleStatus.WATCHED
        val latest = watchings.maxByOrNull { it.createdAt } ?: return TitleStatus.NOT_WATCHED
        return when (latest.state) {
            WatchingState.IN_PROGRESS -> when {
                !latest.hasStarted || type == TitleType.MOVIE -> TitleStatus.NOT_WATCHED
                else -> TitleStatus.WATCHING
            }
            WatchingState.ON_HOLD -> TitleStatus.ON_HOLD
            WatchingState.DROPPED -> TitleStatus.DROPPED
            WatchingState.FINISHED -> TitleStatus.WATCHED
        }
    }
}
