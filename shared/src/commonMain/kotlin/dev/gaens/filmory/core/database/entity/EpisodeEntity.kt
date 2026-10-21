package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("seasonId")],
)
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val seasonId: String,
    val number: Int,
    val name: String?,
    val runtimeMinutes: Int?,
)
