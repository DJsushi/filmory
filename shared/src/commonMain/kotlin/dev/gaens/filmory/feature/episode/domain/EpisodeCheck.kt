package dev.gaens.filmory.feature.episode.domain

import kotlin.time.Instant

data class EpisodeCheck(
    val id: String,
    val watchingId: String,
    val episodeId: String,
    val checkedAt: Instant,
)
