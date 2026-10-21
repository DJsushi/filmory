package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlin.time.Clock
import kotlin.time.Instant

data class NoteDraft(
    val titleId: String,
    val watchingId: String?,
    val episodeId: String?,
    val type: NoteType,
    val text: String,
    val timestampMillis: Long?,
    val imagePath: String?,
    val createdAt: Instant,
)

/**
 * Saves a note. Without a given watching it uses the title's latest one, and creates a watching with
 * no start date when the title has none (a wishlisted title, for example).
 */
class AddNote(
    private val noteRepository: NoteRepository,
    private val watchingRepository: WatchingRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(draft: NoteDraft): Result<Note, DataError.Local> {
        val watchingId = draft.watchingId ?: when (val resolved = latestOrNewWatching(draft.titleId)) {
            is Result.Error -> return resolved
            is Result.Success -> resolved.data
        }
        val note = Note(
            id = idGenerator.newId(),
            watchingId = watchingId,
            titleId = draft.titleId,
            episodeId = draft.episodeId,
            type = draft.type,
            text = draft.text,
            timestampMillis = draft.timestampMillis,
            imagePath = draft.imagePath,
            isFavorite = false,
            commentCount = 0,
            createdAt = draft.createdAt,
        )
        val saved = noteRepository.upsertNote(note)
        if (saved is Result.Error) return saved
        return Result.Success(note)
    }

    private suspend fun latestOrNewWatching(titleId: String): Result<String, DataError.Local> {
        val latest = watchingRepository.getWatchings(titleId).maxByOrNull { it.createdAt }
        if (latest != null) return Result.Success(latest.id)

        val created = Watching(
            id = idGenerator.newId(),
            titleId = titleId,
            state = WatchingState.IN_PROGRESS,
            startedAt = null,
            finishedAt = null,
            rating = null,
            review = null,
            currentEpisodeId = null,
            createdAt = clock.now(),
        )
        val saved = watchingRepository.upsertWatching(created)
        if (saved is Result.Error) return saved
        return Result.Success(created.id)
    }
}
