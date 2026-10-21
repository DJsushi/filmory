package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result

class DeleteWatching(
    private val watchingRepository: WatchingRepository,
    private val syncTitleStatus: SyncTitleStatus,
) {
    suspend operator fun invoke(watchingId: String): EmptyResult<DataError.Local> {
        val watching = watchingRepository.getWatching(watchingId) ?: return Result.Error(DataError.Local.NOT_FOUND)
        val deleted = watchingRepository.deleteWatching(watchingId)
        if (deleted is Result.Error) return deleted
        return syncTitleStatus(watching.titleId)
    }
}
