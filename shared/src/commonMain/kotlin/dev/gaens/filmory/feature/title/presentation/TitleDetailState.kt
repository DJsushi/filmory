package dev.gaens.filmory.feature.title.presentation

import dev.gaens.filmory.feature.note.presentation.NoteUi
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlin.time.Instant

data class TitleDetailState(
    val title: TitleDetailUi? = null,
    val collections: List<CollectionToggleUi> = emptyList(),
    val watchings: List<WatchingUi> = emptyList(),
    val series: SeriesProgressUi? = null,
    val selectedSeasonNumber: Int? = null,
    val notes: List<NoteUi> = emptyList(),
    val noteCount: Int = 0,
    val noteWatchingFilter: String? = null,
    val noteEpisodeFilter: NoteEpisodeFilter = NoteEpisodeFilter.All,
    val noteEpisodeChips: List<EpisodeChipUi> = emptyList(),
    val isOverviewExpanded: Boolean = false,
    val sheet: TitleDetailSheet? = null,
)

data class TitleDetailUi(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val type: TitleType,
    val year: Int?,
    val genre: String?,
    val runtimeMinutes: Int?,
    val creator: String?,
    val overview: String?,
    val status: TitleStatus,
    val isWishlisted: Boolean,
    val isWatchLater: Boolean,
    val seasonCount: Int,
)

data class CollectionToggleUi(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val containsTitle: Boolean,
)

data class WatchingUi(
    val id: String,
    val number: Int,
    val state: WatchingState,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val stars: Float?,
    val review: String?,
)

data class SeriesProgressUi(
    val currentSeasonNumber: Int?,
    val currentEpisodeNumber: Int?,
    val isOnLastEpisode: Boolean,
    val checkedCount: Int,
    val episodeCount: Int,
    val seasons: List<SeasonUi>,
)

data class SeasonUi(
    val number: Int,
    val checkedCount: Int,
    val episodes: List<EpisodeUi>,
)

data class EpisodeUi(
    val id: String,
    val number: Int,
    val name: String?,
    val runtimeMinutes: Int?,
    val isChecked: Boolean,
    val isCurrent: Boolean,
)

data class EpisodeChipUi(val episodeId: String, val seasonNumber: Int, val episodeNumber: Int)

sealed interface NoteEpisodeFilter {
    data object All : NoteEpisodeFilter
    data object SeriesOnly : NoteEpisodeFilter
    data class Episode(val episodeId: String) : NoteEpisodeFilter
}

sealed interface TitleDetailSheet {
    data class EditWatching(val draft: Watching) : TitleDetailSheet
    data object CollectionPicker : TitleDetailSheet
    data object DeleteTitle : TitleDetailSheet
    data class DeleteWatching(val watchingId: String) : TitleDetailSheet
}
