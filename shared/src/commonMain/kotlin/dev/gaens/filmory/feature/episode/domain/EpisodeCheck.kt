package dev.gaens.filmory.feature.episode.domain

import kotlin.time.Instant

data class EpisodeCheck(
    val id: String,
    val watchingId: String,
    val episodeId: String,
    val checkedAt: Instant,
    /** Left out on purpose, such as filler. Counts as checked for progress but was not watched. */
    val isSkipped: Boolean = false,
)
