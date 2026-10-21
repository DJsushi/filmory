package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import kotlinx.coroutines.flow.first

/** Titles flagged for watch later, in the user's order. */
fun List<Title>.watchLaterQueue(): List<Title> =
    filter { it.watchLaterPosition != null }.sortedBy { it.watchLaterPosition }

/** Adds a title to the end of the watch-later row, or takes it out. */
class ToggleWatchLater(private val titleRepository: TitleRepository) {
    suspend operator fun invoke(titleId: String): EmptyResult<DataError.Local> {
        val titles = titleRepository.observeTitles().first()
        val title = titles.find { it.id == titleId } ?: return Result.Error(DataError.Local.NOT_FOUND)
        val position = if (title.watchLaterPosition == null) {
            (titles.mapNotNull { it.watchLaterPosition }.maxOrNull() ?: -1) + 1
        } else {
            null
        }
        return titleRepository.upsertTitle(title.copy(watchLaterPosition = position))
    }
}

/** Moves a title one slot earlier or later in the watch-later row. */
class MoveWatchLater(private val titleRepository: TitleRepository) {
    suspend operator fun invoke(titleId: String, moveEarlier: Boolean): EmptyResult<DataError.Local> {
        val queue = titleRepository.observeTitles().first().watchLaterQueue()
        val index = queue.indexOfFirst { it.id == titleId }
        if (index == -1) return Result.Error(DataError.Local.NOT_FOUND)
        val otherIndex = if (moveEarlier) index - 1 else index + 1
        if (otherIndex !in queue.indices) return Result.Success(Unit)

        val reordered = queue.toMutableList().apply { add(otherIndex, removeAt(index)) }
        reordered.forEachIndexed { position, title ->
            if (title.watchLaterPosition != position) {
                val saved = titleRepository.upsertTitle(title.copy(watchLaterPosition = position))
                if (saved is Result.Error) return saved
            }
        }
        return Result.Success(Unit)
    }
}
