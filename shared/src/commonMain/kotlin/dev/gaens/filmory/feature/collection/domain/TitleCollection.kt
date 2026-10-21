package dev.gaens.filmory.feature.collection.domain

import kotlin.time.Instant

data class TitleCollection(
    val id: String,
    val name: String,
    val titleIds: List<String>,
    val createdAt: Instant,
) {
    val isDefault: Boolean get() = id == DEFAULT_COLLECTION_ID

    companion object {
        const val DEFAULT_COLLECTION_ID = "favorites"
    }
}
