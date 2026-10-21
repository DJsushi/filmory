package dev.gaens.filmory.feature.collection.domain

import kotlin.time.Instant

data class TitleCollection(
    val id: String,
    val name: String,
    val titleIds: List<String>,
    val createdAt: Instant,
    val sortOrder: Int = 0,
    val isPinned: Boolean = false,
    /** What stands for the collection: `vector:<name>`, `emoji:<emoji>` or `image:<path>`. Null for none. */
    val iconKey: String? = null,
    /** The card color as ARGB. Null keeps the title-based gradient. */
    val colorArgb: Long? = null,
) {
    val isDefault: Boolean get() = id == DEFAULT_COLLECTION_ID

    companion object {
        const val DEFAULT_COLLECTION_ID = "favorites"
    }
}

/** Pinned collections first, then the user's own order, with the oldest first when order is equal. */
fun List<TitleCollection>.inDisplayOrder(): List<TitleCollection> =
    sortedWith(compareByDescending<TitleCollection> { it.isPinned }.thenBy { it.sortOrder }.thenBy { it.createdAt })
