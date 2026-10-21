package dev.gaens.filmory.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Placeholder so Room has an entity; milestone 2 replaces it with the real schema.
@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: String,
    val name: String,
)
