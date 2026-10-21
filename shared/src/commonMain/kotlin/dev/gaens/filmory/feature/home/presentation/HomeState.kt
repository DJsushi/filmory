package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.title.domain.TitleType
import kotlinx.datetime.LocalDate

data class HomeState(
    val dayPeriod: DayPeriod = DayPeriod.EVENING,
    val today: LocalDate? = null,
    val continueWatching: List<ContinueWatchingUi> = emptyList(),
    val watchLater: List<TitleThumbUi> = emptyList(),
    val wishlistCount: Int = 0,
    val collections: List<CollectionUi> = emptyList(),
    val onHoldCount: Int = 0,
    val droppedCount: Int = 0,
    val libraryCount: Int = 0,
    val isEditingWatchLater: Boolean = false,
)

enum class DayPeriod { MORNING, AFTERNOON, EVENING }

data class ContinueWatchingUi(
    val titleId: String,
    val name: String,
    val posterUrl: String?,
    val type: TitleType,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val progress: Float?,
)

data class TitleThumbUi(
    val id: String,
    val name: String,
    val posterUrl: String?,
)

data class CollectionUi(
    val id: String,
    val name: String,
    val titleCount: Int,
)
