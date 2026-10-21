package dev.gaens.filmory.feature.watching.domain

import kotlin.time.Instant

/** One pass through a title. A title can have several (rewatches). */
data class Watching(
    val id: String,
    val titleId: String,
    val state: WatchingState,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val rating: Rating?,
    val review: String?,
    val currentEpisodeId: String?,
    val createdAt: Instant,
) {
    /** In progress with no start date means "created by a note, not started yet". */
    val hasStarted: Boolean get() = state != WatchingState.IN_PROGRESS || startedAt != null
}

enum class WatchingState { IN_PROGRESS, ON_HOLD, DROPPED, FINISHED }

/** Moving to in progress sets an empty start date, and moving to finished sets the finish date. */
fun Watching.withState(newState: WatchingState, now: Instant): Watching {
    if (newState == state) return this
    return when (newState) {
        WatchingState.IN_PROGRESS -> copy(state = newState, startedAt = startedAt ?: now, finishedAt = null)
        WatchingState.FINISHED -> copy(state = newState, finishedAt = now)
        WatchingState.ON_HOLD, WatchingState.DROPPED -> copy(state = newState)
    }
}
