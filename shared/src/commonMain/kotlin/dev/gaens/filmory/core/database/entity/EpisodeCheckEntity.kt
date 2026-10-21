package dev.gaens.filmory.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "episode_checks",
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["watchingId", "episodeId"], unique = true), Index("episodeId")],
)
@Serializable
data class EpisodeCheckEntity(
    @PrimaryKey val id: String,
    val watchingId: String,
    val episodeId: String,
    val checkedAtEpochMillis: Long,
    // The default also lets backups made before skipping existed be read.
    @ColumnInfo(defaultValue = "0") val isSkipped: Boolean = false,
)
