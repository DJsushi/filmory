package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import kotlin.time.Clock

class ChangeWatchingState(
    private val watchingRepository: WatchingRepository,
    private val syncTitleStatus: SyncTitleStatus,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(watchingId: String, state: WatchingState): EmptyResult<DataError.Local> {
        val watching = watchingRepository.getWatching(watchingId) ?: return Result.Error(DataError.Local.NOT_FOUND)
        val saved = watchingRepository.upsertWatching(watching.withState(state, clock.now()))
        if (saved is Result.Error) return saved
        return syncTitleStatus(watching.titleId)
    }
}
