package dev.gaens.filmory.feature.episode.domain

data class Season(
    val id: String,
    val titleId: String,
    val number: Int,
    val episodes: List<Episode>,
)

data class Episode(
    val id: String,
    val seasonId: String,
    val number: Int,
    val name: String?,
)
