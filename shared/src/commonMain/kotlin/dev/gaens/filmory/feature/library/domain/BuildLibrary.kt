package dev.gaens.filmory.feature.library.domain

import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.Watching
import kotlin.time.Instant

data class LibraryQuery(
    val status: TitleStatus? = null,
    val type: TitleType? = null,
    val onlyWishlisted: Boolean = false,
    val onlyTitleIds: Set<String>? = null,
    val includeWishlist: Boolean = true,
    val searchText: String = "",
    val sort: LibrarySort = LibrarySort.UPDATED,
)

data class LibraryEntry(
    val title: Title,
    /** Rating of the latest watching, which is what title cards show. */
    val rating: Rating?,
    val lastActivityAt: Instant,
)

/**
 * Filters and sorts the library. With "include wishlist titles" off, titles that are only
 * wishlisted (flagged and not watched) stay out of the library.
 */
class BuildLibrary {
    operator fun invoke(titles: List<Title>, watchings: List<Watching>, query: LibraryQuery): List<LibraryEntry> {
        val watchingsByTitle = watchings.groupBy { it.titleId }
        val search = query.searchText.trim()
        return titles
            .asSequence()
            .filter { query.status == null || it.status == query.status }
            .filter { query.type == null || it.type == query.type }
            .filter { !query.onlyWishlisted || it.isWishlisted }
            .filter { query.onlyTitleIds == null || it.id in query.onlyTitleIds }
            .filter { query.includeWishlist || query.onlyWishlisted || !it.isOnlyWishlisted() }
            .filter { search.isEmpty() || it.name.contains(search, ignoreCase = true) }
            .map { title -> title.toEntry(watchingsByTitle[title.id].orEmpty()) }
            .sortedWith(query.sort.comparator())
            .toList()
    }

    private fun Title.isOnlyWishlisted() = isWishlisted && status == TitleStatus.NOT_WATCHED

    private fun Title.toEntry(watchings: List<Watching>): LibraryEntry {
        val latest = watchings.maxByOrNull { it.createdAt }
        val activity = watchings.flatMap { listOfNotNull(it.createdAt, it.startedAt, it.finishedAt) } + updatedAt
        return LibraryEntry(title = this, rating = latest?.rating, lastActivityAt = activity.max())
    }

    private fun LibrarySort.comparator(): Comparator<LibraryEntry> = when (this) {
        LibrarySort.UPDATED -> compareByDescending { it.lastActivityAt }
        LibrarySort.ADDED -> compareByDescending { it.title.addedAt }
        LibrarySort.NAME -> compareBy { it.title.name.lowercase() }
        LibrarySort.RATING -> compareByDescending<LibraryEntry> { it.rating?.halfStars ?: -1 }
            .thenBy { it.title.name.lowercase() }
    }
}
