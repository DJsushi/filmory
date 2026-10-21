package dev.gaens.filmory.feature.search.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(val results: List<SearchResultDto> = emptyList())

@Serializable
data class SearchResultDto(
    val id: Long,
    @SerialName("media_type") val mediaType: String? = null,
    val title: String? = null,
    val name: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val overview: String? = null,
)

@Serializable
data class GenreDto(val name: String)

@Serializable
data class CrewDto(val job: String? = null, val name: String)

@Serializable
data class CreditsDto(val crew: List<CrewDto> = emptyList())

@Serializable
data class MovieDetailsDto(
    val id: Long,
    val title: String,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<GenreDto> = emptyList(),
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val credits: CreditsDto = CreditsDto(),
)

@Serializable
data class CreatorDto(val name: String)

@Serializable
data class SeasonSummaryDto(
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_count") val episodeCount: Int = 0,
)

@Serializable
data class TvDetailsDto(
    val id: Long,
    val name: String,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("created_by") val createdBy: List<CreatorDto> = emptyList(),
    val seasons: List<SeasonSummaryDto> = emptyList(),
)

@Serializable
data class SeasonDto(
    @SerialName("season_number") val seasonNumber: Int,
    val episodes: List<EpisodeDto> = emptyList(),
)

@Serializable
data class EpisodeDto(
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String? = null,
    val runtime: Int? = null,
)
