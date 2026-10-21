package dev.gaens.filmory.feature.library.presentation

import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType

data class LibraryState(
    val preset: LibraryPreset = LibraryPreset.ALL,
    val collectionName: String? = null,
    val isDefaultCollection: Boolean = false,
    val items: List<LibraryItemUi> = emptyList(),
    val statusFilter: TitleStatus? = null,
    val typeFilter: TitleType? = null,
    val searchText: String = "",
    val isSearchOpen: Boolean = false,
    val settings: LibrarySettings = LibrarySettings(),
    val dialog: LibraryDialog? = null,
) {
    val canFilterStatus: Boolean get() = preset == LibraryPreset.ALL || preset == LibraryPreset.WISHLIST ||
        preset == LibraryPreset.COLLECTION
    val canEditCollection: Boolean get() = preset == LibraryPreset.COLLECTION && !isDefaultCollection
}

enum class LibraryDialog { SETTINGS, RENAME_COLLECTION, DELETE_COLLECTION }

data class LibraryItemUi(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val type: TitleType,
    val year: Int?,
    val status: TitleStatus,
    val stars: Float?,
)
