package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = WatchingEntity::class,
            parentColumns = ["id"],
            childColumns = ["watchingId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("watchingId"), Index("episodeId")],
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val watchingId: String,
    val episodeId: String?,
    val type: String,
    val text: String,
    val timestampMillis: Long?,
    val imagePath: String?,
    val isFavorite: Boolean,
    val createdAtEpochMillis: Long,
)
