package dev.gaens.filmory.feature.library.presentation

import dev.gaens.filmory.feature.library.domain.LibraryEntry
import dev.gaens.filmory.feature.library.presentation.components.CollectionOptionUi
import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

data class LibraryState(
    val preset: LibraryPreset = LibraryPreset.ALL,
    val items: List<LibraryItemUi> = emptyList(),
    val statusFilter: TitleStatus? = null,
    val typeFilter: TitleType? = null,
    val collectionFilter: String? = null,
    val collections: List<CollectionOptionUi> = emptyList(),
    val searchText: String = "",
    val isSearchOpen: Boolean = false,
    val settings: LibrarySettings = LibrarySettings(),
    val dialog: LibraryDialog? = null,
) {
    val canFilterStatus: Boolean get() = preset == LibraryPreset.ALL || preset == LibraryPreset.WISHLIST ||
        preset == LibraryPreset.COLLECTION

    /** A collection screen is already one collection, so it has nothing to filter by. */
    val canFilterCollection: Boolean get() = preset != LibraryPreset.COLLECTION
}

enum class LibraryDialog { SETTINGS }

data class LibraryItemUi(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val type: TitleType,
    val year: Int?,
    val status: TitleStatus,
    val stars: Float?,
)

fun LibraryEntry.toUi() = LibraryItemUi(
    id = title.id,
    name = title.name,
    posterUrl = title.posterUrl,
    type = title.type,
    year = title.year,
    status = title.status,
    stars = rating?.stars,
)
