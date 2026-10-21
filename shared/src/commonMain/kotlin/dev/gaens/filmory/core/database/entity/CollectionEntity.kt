package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
)
