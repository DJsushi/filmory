package dev.gaens.filmory.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// The fields after createdAtEpochMillis came in database version 2. Their defaults let old rows,
// and backups made before them, load unchanged.
@Entity(tableName = "collections")
@Serializable
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    @ColumnInfo(defaultValue = "0") val isPinned: Boolean = false,
    val iconKey: String? = null,
    val colorArgb: Long? = null,
)
