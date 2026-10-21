package dev.gaens.filmory.feature.title.data

import dev.gaens.filmory.core.data.toEpochMillis
import dev.gaens.filmory.core.data.toInstant
import dev.gaens.filmory.core.database.entity.TitleEntity
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

fun TitleEntity.toTitle(): Title = Title(
    id = id,
    type = TitleType.valueOf(type),
    name = name,
    year = year,
    overview = overview,
    posterUrl = posterUrl,
    tmdbId = tmdbId,
    status = TitleStatus.valueOf(status),
    isWishlisted = isWishlisted,
    watchLaterPosition = watchLaterPosition,
    addedAt = addedAtEpochMillis.toInstant(),
)

fun Title.toTitleEntity(): TitleEntity = TitleEntity(
    id = id,
    type = type.name,
    name = name,
    year = year,
    overview = overview,
    posterUrl = posterUrl,
    tmdbId = tmdbId,
    status = status.name,
    isWishlisted = isWishlisted,
    watchLaterPosition = watchLaterPosition,
    addedAtEpochMillis = addedAt.toEpochMillis(),
)
