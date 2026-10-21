package dev.gaens.filmory.feature.title.presentation

import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.presentation.buildNoteUis
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.watching.domain.Watching

/** What the detail screen reads from the database. */
data class TitleDetailData(
    val title: Title,
    val watchings: List<Watching>,
    val seasons: List<Season>,
    val checks: List<EpisodeCheck>,
    val notes: List<Note>,
    val collections: List<TitleCollection>,
)

/** The watching that progress, checks and new notes go to. */
val TitleDetailData.currentWatching: Watching? get() = watchings.maxByOrNull { it.createdAt }

fun TitleDetailState.withData(data: TitleDetailData): TitleDetailState {
    val title = data.title
    val numbersById = data.watchings.sortedBy { it.createdAt }.mapIndexed { index, it -> it.id to index + 1 }.toMap()
    val allNotes = buildNoteUis(data.notes, listOf(title), data.watchings, data.seasons)
    val episodeFilter = noteEpisodeFilter
    val notesWithEpisodes = data.notes.associate { it.id to it.episodeId }
    val notesWithWatchings = data.notes.associate { it.id to it.watchingId }
    val episodeChips = data.seasons.flatMap { season ->
        season.episodes
            .filter { episode -> data.notes.any { it.episodeId == episode.id } }
            .map { EpisodeChipUi(it.id, season.number, it.number) }
    }
    return copy(
        title = TitleDetailUi(
            id = title.id,
            name = title.name,
            posterUrl = title.posterUrl,
            type = title.type,
            year = title.year,
            genre = title.genre,
            runtimeMinutes = title.runtimeMinutes,
            creator = title.creator,
            overview = title.overview,
            status = title.status,
            isWishlisted = title.isWishlisted,
            isWatchLater = title.watchLaterPosition != null,
            seasonCount = data.seasons.size,
        ),
        collections = data.collections.map {
            CollectionToggleUi(it.id, it.name, it.isDefault, title.id in it.titleIds)
        },
        watchings = data.watchings.sortedByDescending { it.createdAt }.map { watching ->
            WatchingUi(
                id = watching.id,
                number = numbersById.getValue(watching.id),
                state = watching.state,
                startedAt = watching.startedAt,
                finishedAt = watching.finishedAt,
                stars = watching.rating?.stars,
                review = watching.review,
            )
        },
        series = if (data.seasons.isEmpty()) null else data.toSeriesProgress(),
        selectedSeasonNumber = selectedSeasonNumber ?: data.currentSeasonNumber() ?: data.seasons.firstOrNull()?.number,
        noteCount = allNotes.size,
        notes = allNotes
            .filter { noteWatchingFilter.isEmpty() || notesWithWatchings[it.id] in noteWatchingFilter }
            .filter { note ->
                when (episodeFilter) {
                    NoteEpisodeFilter.All -> true
                    NoteEpisodeFilter.SeriesOnly -> notesWithEpisodes[note.id] == null
                    is NoteEpisodeFilter.Episode -> notesWithEpisodes[note.id] == episodeFilter.episodeId
                }
            }
            .sortedByDescending { it.createdAt },
        noteEpisodeChips = episodeChips,
    )
}

private fun TitleDetailData.currentSeasonNumber(): Int? {
    val currentId = currentWatching?.currentEpisodeId ?: return null
    return seasons.find { season -> season.episodes.any { it.id == currentId } }?.number
}

private fun TitleDetailData.toSeriesProgress(): SeriesProgressUi {
    val watching = currentWatching
    val skippedByEpisodeId = checks.filter { it.watchingId == watching?.id }.associate { it.episodeId to it.isSkipped }
    fun markOf(episodeId: String) = when (skippedByEpisodeId[episodeId]) {
        null -> EpisodeMark.NOT_WATCHED
        true -> EpisodeMark.SKIPPED
        false -> EpisodeMark.WATCHED
    }
    val allEpisodes = seasons.flatMap { it.episodes }
    val currentId = watching?.currentEpisodeId
    val currentSeason = seasons.find { season -> season.episodes.any { it.id == currentId } }
    return SeriesProgressUi(
        currentSeasonNumber = currentSeason?.number,
        currentEpisodeNumber = currentSeason?.episodes?.find { it.id == currentId }?.number,
        isOnLastEpisode = currentId != null && allEpisodes.lastOrNull()?.id == currentId,
        watchedCount = allEpisodes.count { markOf(it.id) == EpisodeMark.WATCHED },
        skippedCount = allEpisodes.count { markOf(it.id) == EpisodeMark.SKIPPED },
        episodeCount = allEpisodes.size,
        seasons = seasons.map { season ->
            SeasonUi(
                number = season.number,
                checkedCount = season.episodes.count { markOf(it.id) != EpisodeMark.NOT_WATCHED },
                episodes = season.episodes.map {
                    EpisodeUi(it.id, it.number, it.name, it.runtimeMinutes, markOf(it.id), it.id == currentId)
                },
            )
        },
    )
}
