package dev.gaens.filmory.feature.library.presentation

import kotlinx.serialization.Serializable

/** Which slice of the library a list screen opens with. */
@Serializable
enum class LibraryPreset { ALL, WISHLIST, ON_HOLD, DROPPED, COLLECTION }
