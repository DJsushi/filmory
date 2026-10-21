package dev.gaens.filmory.feature.watched.domain

import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlin.time.Instant

data class WatchedEntry(
    val title: Title,
    /** When the title was last finished. Null for titles marked watched without a date. */
    val finishedAt: Instant?,
    val rating: Rating?,
)

enum class WatchedSort { FINISHED, RATING, NAME }

/** Bookmory's achievement list: watched titles, by default the most recently finished first. */
class BuildWatchedList {
    operator fun invoke(
        titles: List<Title>,
        watchings: List<Watching>,
        type: TitleType? = null,
        sort: WatchedSort = WatchedSort.FINISHED,
    ): List<WatchedEntry> {
        val watchingsByTitle = watchings.groupBy { it.titleId }
        val entries = titles
            .filter { it.status == TitleStatus.WATCHED && (type == null || it.type == type) }
            .map { title ->
                val forTitle = watchingsByTitle[title.id].orEmpty()
                WatchedEntry(
                    title = title,
                    finishedAt = forTitle.filter { it.state == WatchingState.FINISHED }.mapNotNull { it.finishedAt }.maxOrNull(),
                    rating = forTitle.maxByOrNull { it.createdAt }?.rating,
                )
            }
        return when (sort) {
            WatchedSort.FINISHED -> entries.sortedWith(compareByDescending<WatchedEntry> { it.finishedAt }.thenBy { it.title.name })
            WatchedSort.RATING -> entries.sortedWith(
                compareByDescending<WatchedEntry> { it.rating?.halfStars ?: -1 }.thenBy { it.title.name.lowercase() },
            )
            WatchedSort.NAME -> entries.sortedBy { it.title.name.lowercase() }
        }
    }
}
