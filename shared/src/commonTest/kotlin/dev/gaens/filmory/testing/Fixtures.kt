package dev.gaens.filmory.testing

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlin.time.Clock
import kotlin.time.Instant

fun at(epochMillis: Long): Instant = Instant.fromEpochMilliseconds(epochMillis)

class FixedClock(var now: Instant = at(1_000_000)) : Clock {
    override fun now(): Instant = now
}

class SequentialIdGenerator : IdGenerator {
    private var next = 1
    override fun newId(): String = "id-${next++}"
}

fun aTitle(
    id: String = "title",
    type: TitleType = TitleType.SERIES,
    status: TitleStatus = TitleStatus.NOT_WATCHED,
) = Title(
    id = id,
    type = type,
    name = "Name",
    year = 2020,
    genre = null,
    runtimeMinutes = null,
    creator = null,
    overview = null,
    posterUrl = null,
    tmdbId = null,
    status = status,
    isWishlisted = false,
    watchLaterPosition = null,
    addedAt = at(0),
    updatedAt = at(0),
)

fun aWatching(
    id: String = "watching",
    titleId: String = "title",
    state: WatchingState = WatchingState.IN_PROGRESS,
    startedAt: Instant? = at(1),
    finishedAt: Instant? = null,
    currentEpisodeId: String? = null,
    createdAt: Instant = at(1),
) = Watching(
    id = id,
    titleId = titleId,
    state = state,
    startedAt = startedAt,
    finishedAt = finishedAt,
    rating = null,
    review = null,
    currentEpisodeId = currentEpisodeId,
    createdAt = createdAt,
)

fun aNote(id: String = "note", watchingId: String = "watching", titleId: String = "title") = Note(
    id = id,
    watchingId = watchingId,
    titleId = titleId,
    episodeId = null,
    type = NoteType.THOUGHT,
    text = "text",
    timestampMillis = null,
    imagePath = null,
    isFavorite = false,
    commentCount = 0,
    createdAt = at(5),
)

/** Season 1 with episodes e1 and e2, season 2 with episode e3. */
fun aSeriesStructure(titleId: String = "title") = listOf(
    Season("s1", titleId, 1, listOf(Episode("e1", "s1", 1, null, null), Episode("e2", "s1", 2, null, null))),
    Season("s2", titleId, 2, listOf(Episode("e3", "s2", 1, null, null))),
)
