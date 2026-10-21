package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import kotlin.time.Clock

/**
 * The watching that episode actions apply to: the latest one. Starts a watching when the title has
 * none, and sets the start date of one that a note created but nobody started.
 */
class EnsureActiveWatching(
    private val watchingRepository: WatchingRepository,
    private val startWatching: StartWatching,
    private val syncTitleStatus: SyncTitleStatus,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(titleId: String): Result<Watching, DataError.Local> {
        val latest = watchingRepository.getWatchings(titleId).maxByOrNull { it.createdAt }
            ?: return startWatching(titleId)
        if (latest.hasStarted) return Result.Success(latest)

        val started = latest.copy(startedAt = clock.now())
        val saved = watchingRepository.upsertWatching(started)
        if (saved is Result.Error) return saved
        val synced = syncTitleStatus(titleId)
        if (synced is Result.Error) return synced
        return Result.Success(started)
    }
}
