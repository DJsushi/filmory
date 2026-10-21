package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.title.domain.TitleRepository

/** Recomputes a title's status from its watchings after they changed. */
class SyncTitleStatus(
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val determineTitleStatus: DetermineTitleStatus,
) {
    suspend operator fun invoke(titleId: String): EmptyResult<DataError.Local> {
        val title = titleRepository.getTitle(titleId) ?: return Result.Error(DataError.Local.NOT_FOUND)
        val status = determineTitleStatus(title.type, watchingRepository.getWatchings(titleId))
        if (status == title.status) return Result.Success(Unit)
        return titleRepository.upsertTitle(title.copy(status = status))
    }
}
