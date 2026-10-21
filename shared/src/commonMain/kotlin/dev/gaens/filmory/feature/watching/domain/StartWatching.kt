package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import kotlin.time.Clock

/** Starts a first watching or a rewatch. */
class StartWatching(
    private val watchingRepository: WatchingRepository,
    private val syncTitleStatus: SyncTitleStatus,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(titleId: String): Result<Watching, DataError.Local> {
        val now = clock.now()
        val watching = Watching(
            id = idGenerator.newId(),
            titleId = titleId,
            state = WatchingState.IN_PROGRESS,
            startedAt = now,
            finishedAt = null,
            rating = null,
            review = null,
            currentEpisodeId = null,
            createdAt = now,
        )
        val saved = watchingRepository.upsertWatching(watching)
        if (saved is Result.Error) return saved
        val synced = syncTitleStatus(titleId)
        if (synced is Result.Error) return synced
        return Result.Success(watching)
    }
}
