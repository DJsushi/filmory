package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchings",
    foreignKeys = [
        ForeignKey(
            entity = TitleEntity::class,
            parentColumns = ["id"],
            childColumns = ["titleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["currentEpisodeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("titleId"), Index("currentEpisodeId")],
)
data class WatchingEntity(
    @PrimaryKey val id: String,
    val titleId: String,
    val state: String,
    val startedAtEpochMillis: Long?,
    val finishedAtEpochMillis: Long?,
    val ratingHalfStars: Int?,
    val review: String?,
    val currentEpisodeId: String?,
    val createdAtEpochMillis: Long,
)
