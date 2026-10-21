package dev.gaens.filmory.feature.watching.domain

import kotlin.jvm.JvmInline

/** A rating of 0 to 5 stars in half-star steps, stored as the number of half stars (0 to 10). */
@JvmInline
value class Rating(val halfStars: Int) {
    init {
        require(halfStars in 0..MAX_HALF_STARS) { "Rating must be between 0 and 5 stars" }
    }

    val stars: Float get() = halfStars / 2f

    companion object {
        const val MAX_HALF_STARS = 10
    }
}
