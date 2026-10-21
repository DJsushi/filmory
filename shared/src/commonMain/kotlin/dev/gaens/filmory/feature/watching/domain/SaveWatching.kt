package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Error
import dev.gaens.filmory.core.domain.util.Result

sealed interface SaveWatchingError : Error {
    data object FinishBeforeStart : SaveWatchingError
    data class Storage(val error: DataError.Local) : SaveWatchingError
}

/** Saves an edited watching (dates, state, rating, review) and updates the title status. */
class SaveWatching(
    private val watchingRepository: WatchingRepository,
    private val syncTitleStatus: SyncTitleStatus,
) {
    suspend operator fun invoke(watching: Watching): Result<Unit, SaveWatchingError> {
        val start = watching.startedAt
        val finish = watching.finishedAt
        if (start != null && finish != null && finish < start) {
            return Result.Error(SaveWatchingError.FinishBeforeStart)
        }
        val saved = watchingRepository.upsertWatching(watching)
        if (saved is Result.Error) return Result.Error(SaveWatchingError.Storage(saved.error))
        val synced = syncTitleStatus(watching.titleId)
        if (synced is Result.Error) return Result.Error(SaveWatchingError.Storage(synced.error))
        return Result.Success(Unit)
    }
}
