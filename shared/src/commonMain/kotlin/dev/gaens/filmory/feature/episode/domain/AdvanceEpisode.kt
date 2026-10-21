package dev.gaens.filmory.feature.episode.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlin.time.Clock

/**
 * The "next episode" action. Without a pointer it points at the first episode. Otherwise it checks
 * the current episode and moves on, staying on the last episode when there is no next one.
 */
class AdvanceEpisode(
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(watchingId: String): EmptyResult<DataError.Local> {
        val watching = watchingRepository.getWatching(watchingId) ?: return Result.Error(DataError.Local.NOT_FOUND)
        val episodes = episodeRepository.getSeasons(watching.titleId).flatMap { it.episodes }
        val first = episodes.firstOrNull() ?: return Result.Error(DataError.Local.NOT_FOUND)

        val currentIndex = episodes.indexOfFirst { it.id == watching.currentEpisodeId }
        if (currentIndex == -1) {
            return watchingRepository.upsertWatching(watching.copy(currentEpisodeId = first.id))
        }

        val current = episodes[currentIndex]
        val check = EpisodeCheck(idGenerator.newId(), watchingId, current.id, clock.now())
        val checked = episodeRepository.checkEpisode(check)
        if (checked is Result.Error) return checked

        val next = episodes.getOrElse(currentIndex + 1) { current }
        return watchingRepository.upsertWatching(watching.copy(currentEpisodeId = next.id))
    }
}
