package dev.gaens.filmory.feature.watching.data

import dev.gaens.filmory.core.data.toEpochMillis
import dev.gaens.filmory.core.data.toInstant
import dev.gaens.filmory.core.database.entity.WatchingEntity
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState

fun WatchingEntity.toWatching(): Watching = Watching(
    id = id,
    titleId = titleId,
    state = WatchingState.valueOf(state),
    startedAt = startedAtEpochMillis?.toInstant(),
    finishedAt = finishedAtEpochMillis?.toInstant(),
    rating = ratingHalfStars?.let(::Rating),
    review = review,
    currentEpisodeId = currentEpisodeId,
    createdAt = createdAtEpochMillis.toInstant(),
)

fun Watching.toWatchingEntity(): WatchingEntity = WatchingEntity(
    id = id,
    titleId = titleId,
    state = state.name,
    startedAtEpochMillis = startedAt?.toEpochMillis(),
    finishedAtEpochMillis = finishedAt?.toEpochMillis(),
    ratingHalfStars = rating?.halfStars,
    review = review,
    currentEpisodeId = currentEpisodeId,
    createdAtEpochMillis = createdAt.toEpochMillis(),
)
