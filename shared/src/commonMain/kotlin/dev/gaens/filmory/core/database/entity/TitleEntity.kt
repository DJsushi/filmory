package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "titles")
@Serializable
data class TitleEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String,
    val year: Int?,
    val genre: String?,
    val runtimeMinutes: Int?,
    val creator: String?,
    val overview: String?,
    val posterUrl: String?,
    val tmdbId: Long?,
    val status: String,
    val isWishlisted: Boolean,
    val watchLaterPosition: Int?,
    val addedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
