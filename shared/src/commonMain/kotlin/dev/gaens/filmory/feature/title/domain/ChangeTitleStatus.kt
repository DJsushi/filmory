package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Error
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.feature.watching.domain.withState
import kotlin.time.Clock

sealed interface ChangeTitleStatusError : Error {
    data object MovieCannotBeWatching : ChangeTitleStatusError
    data class Storage(val error: DataError.Local) : ChangeTitleStatusError
}

/**
 * Sets a title's status by hand. The user's choice always wins for the title. The latest watching
 * follows it, except a finished one, which a manual change never reopens.
 */
class ChangeTitleStatus(
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(titleId: String, status: TitleStatus): Result<Unit, ChangeTitleStatusError> {
        val title = titleRepository.getTitle(titleId)
            ?: return storageError(DataError.Local.NOT_FOUND)
        if (status == TitleStatus.WATCHING && title.type == TitleType.MOVIE) {
            return Result.Error(ChangeTitleStatusError.MovieCannotBeWatching)
        }

        val watchingState = status.toWatchingState()
        if (watchingState != null) {
            val applied = followWithLatestWatching(titleId, watchingState)
            if (applied is Result.Error) return storageError(applied.error)
        }

        val saved = titleRepository.upsertTitle(title.copy(status = status))
        if (saved is Result.Error) return storageError(saved.error)
        return Result.Success(Unit)
    }

    private suspend fun followWithLatestWatching(
        titleId: String,
        state: WatchingState,
    ): Result<Unit, DataError.Local> {
        val now = clock.now()
        val latest = watchingRepository.getWatchings(titleId).maxByOrNull { it.createdAt }
        val updated = when {
            latest == null -> newWatching(titleId, now).withState(state, now)
            latest.state == WatchingState.FINISHED -> return Result.Success(Unit)
            else -> latest.withState(state, now)
        }
        return watchingRepository.upsertWatching(updated)
    }

    private fun newWatching(titleId: String, now: kotlin.time.Instant) = Watching(
        id = idGenerator.newId(),
        titleId = titleId,
        state = WatchingState.IN_PROGRESS,
        startedAt = null,
        finishedAt = null,
        rating = null,
        review = null,
        currentEpisodeId = null,
        createdAt = now,
    )

    private fun storageError(error: DataError.Local) = Result.Error(ChangeTitleStatusError.Storage(error))

    private fun TitleStatus.toWatchingState(): WatchingState? = when (this) {
        TitleStatus.NOT_WATCHED -> null
        TitleStatus.WATCHING -> WatchingState.IN_PROGRESS
        TitleStatus.WATCHED -> WatchingState.FINISHED
        TitleStatus.ON_HOLD -> WatchingState.ON_HOLD
        TitleStatus.DROPPED -> WatchingState.DROPPED
    }
}
