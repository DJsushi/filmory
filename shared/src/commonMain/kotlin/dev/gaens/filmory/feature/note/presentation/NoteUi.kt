package dev.gaens.filmory.feature.note.presentation

import androidx.compose.runtime.Composable
import dev.gaens.filmory.core.presentation.ui.formatTimestamp
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.detail_season_episode_short
import dev.gaens.filmory.resources.detail_watching_number
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** A note as it appears in lists and on its detail screen. */
data class NoteUi(
    val id: String,
    val titleId: String,
    val titleName: String,
    val titlePosterUrl: String?,
    val titleType: TitleType,
    val type: NoteType,
    val text: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val timestampMillis: Long?,
    /** 1 for the first watching of the title, 2 for the first rewatch and so on. */
    val watchingNumber: Int?,
    val commentCount: Int,
    val isFavorite: Boolean,
    val createdAt: Instant,
)

/** Episode and timestamp, for example "S2 · E3 · 0:24:10". Null when the note has neither. */
@Composable
fun NoteUi.positionLabel(): String? {
    val parts = listOfNotNull(
        if (seasonNumber != null && episodeNumber != null) {
            stringResource(Res.string.detail_season_episode_short, seasonNumber, episodeNumber)
        } else {
            null
        },
        timestampMillis?.let(::formatTimestamp),
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

/** For example "Movie · Watching 2" or "Series · S2 · E3 · Watching 1". */
@Composable
fun NoteUi.contextLabel(): String = listOfNotNull(
    titleType.label(),
    if (seasonNumber != null && episodeNumber != null) {
        stringResource(Res.string.detail_season_episode_short, seasonNumber, episodeNumber)
    } else {
        null
    },
    watchingNumber?.let { stringResource(Res.string.detail_watching_number, it) },
).joinToString(" · ")

/** Numbers each title's watchings from 1 in the order they were created. */
fun watchingNumbers(watchings: List<Watching>): Map<String, Int> = watchings
    .groupBy { it.titleId }
    .values
    .flatMap { forTitle -> forTitle.sortedBy { it.createdAt }.mapIndexed { index, watching -> watching.id to index + 1 } }
    .toMap()

fun buildNoteUis(
    notes: List<Note>,
    titles: List<Title>,
    watchings: List<Watching>,
    seasons: List<Season>,
): List<NoteUi> {
    val titlesById = titles.associateBy { it.id }
    val numbers = watchingNumbers(watchings)
    val episodePositions = seasons
        .flatMap { season -> season.episodes.map { it.id to (season.number to it.number) } }
        .toMap()
    return notes.mapNotNull { note ->
        val title = titlesById[note.titleId] ?: return@mapNotNull null
        val position = note.episodeId?.let(episodePositions::get)
        NoteUi(
            id = note.id,
            titleId = title.id,
            titleName = title.name,
            titlePosterUrl = title.posterUrl,
            titleType = title.type,
            type = note.type,
            text = note.text,
            seasonNumber = position?.first,
            episodeNumber = position?.second,
            timestampMillis = note.timestampMillis,
            watchingNumber = numbers[note.watchingId],
            commentCount = note.commentCount,
            isFavorite = note.isFavorite,
            createdAt = note.createdAt,
        )
    }
}
