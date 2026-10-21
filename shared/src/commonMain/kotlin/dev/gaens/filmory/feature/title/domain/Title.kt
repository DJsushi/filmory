package dev.gaens.filmory.feature.title.domain

import kotlin.time.Instant

data class Title(
    val id: String,
    val type: TitleType,
    val name: String,
    val year: Int?,
    val overview: String?,
    val posterUrl: String?,
    val tmdbId: Long?,
    val status: TitleStatus,
    val isWishlisted: Boolean,
    val watchLaterPosition: Int?,
    val addedAt: Instant,
)

enum class TitleType { MOVIE, SERIES }

enum class TitleStatus { NOT_WATCHED, WATCHING, WATCHED, ON_HOLD, DROPPED }
